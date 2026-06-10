package com.circleguard.auth.controller;

import com.circleguard.auth.service.QrTokenService;
import com.circleguard.auth.security.SecurityConfig;
import com.circleguard.auth.security.DualChainAuthenticationProvider;
import com.circleguard.auth.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QrTokenController.class)
@Import(SecurityConfig.class)
class QrTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrTokenService qrService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private DualChainAuthenticationProvider dualChainAuthenticationProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private static final String QR_TOKEN = "mock-qr-token-12345";

    @Test
    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000")
    void generateToken_AuthenticatedUser_ReturnsQrToken() throws Exception {
        // Arrange
        when(qrService.generateQrToken(any(UUID.class))).thenReturn(QR_TOKEN);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/qr/generate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrToken").value(QR_TOKEN))
                .andExpect(jsonPath("$.expiresIn").value("60"));
    }

    @Test
    void generateToken_Unauthenticated_Returns403() throws Exception {
        // Act & Assert - Spring Security returns 403 Forbidden when authentication is required
        mockMvc.perform(get("/api/v1/auth/qr/generate"))
                .andExpect(status().isForbidden());
    }
}