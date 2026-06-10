package com.circleguard.form.controller;

import com.circleguard.form.model.HealthSurvey;
import com.circleguard.form.model.ValidationStatus;
import com.circleguard.form.service.HealthSurveyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CertificateValidationController.class)
class CertificateValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthSurveyService surveyService;

    @Test
    void getPending_ShouldReturnListOfPendingSurveys() throws Exception {
        // Arrange
        HealthSurvey survey1 = new HealthSurvey();
        survey1.setValidationStatus(ValidationStatus.PENDING);
        
        HealthSurvey survey2 = new HealthSurvey();
        survey2.setValidationStatus(ValidationStatus.PENDING);
        
        HealthSurvey survey3 = new HealthSurvey();
        survey3.setValidationStatus(ValidationStatus.PENDING);
        
        List<HealthSurvey> pendingSurveys = Arrays.asList(survey1, survey2, survey3);
        
        when(surveyService.getPendingSurveys()).thenReturn(pendingSurveys);

        // Act & Assert
        mockMvc.perform(get("/api/v1/certificates/pending")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
        
        verify(surveyService, times(1)).getPendingSurveys();
    }

    @Test
    void getPending_WhenNoPendingSurveys_ReturnsEmptyList() throws Exception {
        // Arrange
        when(surveyService.getPendingSurveys()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/certificates/pending")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        
        verify(surveyService, times(1)).getPendingSurveys();
    }

    @Test
    void validate_WithApprovedStatus_ShouldValidateSurvey() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        doNothing().when(surveyService).validateSurvey(surveyId, ValidationStatus.APPROVED, adminId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "APPROVED")
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(surveyService, times(1)).validateSurvey(surveyId, ValidationStatus.APPROVED, adminId);
    }

    @Test
    void validate_WithRejectedStatus_ShouldValidateSurvey() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        doNothing().when(surveyService).validateSurvey(surveyId, ValidationStatus.REJECTED, adminId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "REJECTED")
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(surveyService, times(1)).validateSurvey(surveyId, ValidationStatus.REJECTED, adminId);
    }

    @Test
    void validate_WithPendingStatus_ShouldValidateSurvey() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        doNothing().when(surveyService).validateSurvey(surveyId, ValidationStatus.PENDING, adminId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "PENDING")
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        verify(surveyService, times(1)).validateSurvey(surveyId, ValidationStatus.PENDING, adminId);
    }

    @Test
    void validate_WithInvalidSurveyId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID adminId = UUID.randomUUID();
        
        // Act & Assert - Invalid UUID format
        mockMvc.perform(post("/api/v1/certificates/invalid-uuid/validate")
                .param("status", "APPROVED")
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        verify(surveyService, never()).validateSurvey(any(), any(), any());
    }

    @Test
    void validate_WithInvalidAdminId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        
        // Act & Assert - Invalid adminId UUID format
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "APPROVED")
                .param("adminId", "invalid-uuid")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        verify(surveyService, never()).validateSurvey(any(), any(), any());
    }

    @Test
    void validate_WithMissingStatus_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        // Act & Assert - Missing status parameter
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        verify(surveyService, never()).validateSurvey(any(), any(), any());
    }

    @Test
    void validate_WithMissingAdminId_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        
        // Act & Assert - Missing adminId parameter
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "APPROVED")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        verify(surveyService, never()).validateSurvey(any(), any(), any());
    }

    @Test
    void validate_WithInvalidStatusValue_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        // Act & Assert - Invalid enum value
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "INVALID_STATUS")
                .param("adminId", adminId.toString())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        
        verify(surveyService, never()).validateSurvey(any(), any(), any());
    }

    @Test
    void validate_ShouldReturnOk_WhenServiceCompletesSuccessfully() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        doNothing().when(surveyService).validateSurvey(surveyId, ValidationStatus.APPROVED, adminId);

        // Act & Assert
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "APPROVED")
                .param("adminId", adminId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getPending_ShouldReturnOk_WithJsonContentType() throws Exception {
        // Arrange
        HealthSurvey survey = new HealthSurvey();
        survey.setValidationStatus(ValidationStatus.PENDING);
        
        List<HealthSurvey> pendingSurveys = Arrays.asList(survey);
        when(surveyService.getPendingSurveys()).thenReturn(pendingSurveys);

        // Act & Assert
        mockMvc.perform(get("/api/v1/certificates/pending"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getPending_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        when(surveyService.getPendingSurveys()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert - The controller will return 500 error
        mockMvc.perform(get("/api/v1/certificates/pending"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void validate_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        // Arrange
        UUID surveyId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        
        doThrow(new RuntimeException("Validation failed")).when(surveyService)
                .validateSurvey(surveyId, ValidationStatus.APPROVED, adminId);

        // Act & Assert - The controller will return 500 error
        mockMvc.perform(post("/api/v1/certificates/{id}/validate", surveyId)
                .param("status", "APPROVED")
                .param("adminId", adminId.toString()))
                .andExpect(status().is5xxServerError());
    }
}