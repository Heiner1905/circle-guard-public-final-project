package com.circleguard.form.service;

import com.circleguard.form.model.Question;
import com.circleguard.form.model.Questionnaire;
import com.circleguard.form.repository.QuestionnaireRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock
    private QuestionnaireRepository repository;

    @InjectMocks
    private QuestionnaireService service;

    @Test
    void saveQuestionnaireBackfillsQuestionParent() {
        Question question = Question.builder().text("Symptoms?").orderIndex(1).build();
        Questionnaire questionnaire = Questionnaire.builder()
                .title("Daily")
                .questions(List.of(question))
                .build();
        when(repository.save(questionnaire)).thenReturn(questionnaire);

        Questionnaire result = service.saveQuestionnaire(questionnaire);

        assertSame(questionnaire, result);
        assertSame(questionnaire, question.getQuestionnaire());
    }

    @Test
    void activateQuestionnaireDeactivatesCurrentAndActivatesTarget() {
        UUID targetId = UUID.randomUUID();
        Questionnaire active = Questionnaire.builder().isActive(true).build();
        Questionnaire inactive = Questionnaire.builder().id(targetId).isActive(false).build();
        when(repository.findAll()).thenReturn(List.of(active, inactive));
        when(repository.findById(targetId)).thenReturn(Optional.of(inactive));

        service.activateQuestionnaire(targetId);

        assertFalse(active.getIsActive());
        assertTrue(inactive.getIsActive());
        verify(repository).save(active);
        verify(repository).save(inactive);
    }

    @Test
    void getActiveQuestionnaireDelegatesToRepository() {
        Questionnaire questionnaire = Questionnaire.builder().title("Latest").isActive(true).build();
        when(repository.findFirstByIsActiveTrueOrderByVersionDesc()).thenReturn(Optional.of(questionnaire));

        assertEquals(Optional.of(questionnaire), service.getActiveQuestionnaire());
    }
}
