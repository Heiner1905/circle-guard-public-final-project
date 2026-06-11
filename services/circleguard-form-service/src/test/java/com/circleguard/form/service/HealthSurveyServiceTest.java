package com.circleguard.form.service;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.model.Questionnaire;
import com.circleguard.form.model.ValidationStatus;
import com.circleguard.form.repository.HealthSurveyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthSurveyServiceTest {

    @Mock
    private HealthSurveyRepository repository;

    @Mock
    private QuestionnaireService questionnaireService;

    @Mock
    private SymptomMapper symptomMapper;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private HealthSurveyService service;

    @Test
    void submitSurveySetsPendingValidationAndPublishesSurveyEvent() {
        UUID anonymousId = UUID.randomUUID();
        HealthSurvey survey = HealthSurvey.builder()
                .anonymousId(anonymousId)
                .attachmentPath("certificates/test.pdf")
                .build();
        Questionnaire questionnaire = Questionnaire.builder().title("Daily").build();

        when(questionnaireService.getActiveQuestionnaire()).thenReturn(Optional.of(questionnaire));
        when(symptomMapper.hasSymptoms(survey, questionnaire)).thenReturn(true);
        when(repository.save(survey)).thenReturn(survey);

        HealthSurvey result = service.submitSurvey(survey);

        assertSame(survey, result);
        assertEquals(ValidationStatus.PENDING, result.getValidationStatus());
        assertTrue(result.getHasFever());
        assertTrue(result.getHasCough());
        verify(kafkaTemplate).send(eq("survey.submitted"), eq(anonymousId.toString()), argThat(event ->
                Boolean.TRUE.equals(((Map<?, ?>) event).get("hasSymptoms"))
        ));
    }

    @Test
    void getPendingSurveysUsesPendingAttachmentQuery() {
        HealthSurvey pending = HealthSurvey.builder().validationStatus(ValidationStatus.PENDING).build();
        when(repository.findByAttachmentPathIsNotNullAndValidationStatus(ValidationStatus.PENDING))
                .thenReturn(List.of(pending));

        assertEquals(List.of(pending), service.getPendingSurveys());
    }

    @Test
    void validateSurveyPublishesApprovalEvent() {
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        UUID anonymousId = UUID.randomUUID();
        HealthSurvey survey = HealthSurvey.builder().anonymousId(anonymousId).build();
        when(repository.findById(surveyId)).thenReturn(Optional.of(survey));

        service.validateSurvey(surveyId, ValidationStatus.APPROVED, adminId);

        assertEquals(ValidationStatus.APPROVED, survey.getValidationStatus());
        assertEquals(adminId, survey.getValidatedBy());
        verify(repository).save(survey);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(kafkaTemplate).send(eq("certificate.validated"), eq(anonymousId.toString()), eventCaptor.capture());
        assertEquals("APPROVED", ((Map<?, ?>) eventCaptor.getValue()).get("status"));
    }

    @Test
    void validateSurveyRejectsMissingSurvey() {
        UUID surveyId = UUID.randomUUID();
        when(repository.findById(surveyId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.validateSurvey(surveyId, ValidationStatus.REJECTED, UUID.randomUUID()));
    }
}
