package com.circleguard.form.controller;

import com.circleguard.form.model.ValidationStatus;
import com.circleguard.form.service.HealthSurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/certificates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CertificateValidationController {

    private final HealthSurveyService surveyService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        try {
            return ResponseEntity.ok(surveyService.getPendingSurveys());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch pending surveys: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validate(
            @PathVariable UUID id,
            @RequestParam ValidationStatus status,
            @RequestParam UUID adminId) {
        try {
            surveyService.validateSurvey(id, status, adminId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to validate survey: " + e.getMessage()));
        }
    }
}