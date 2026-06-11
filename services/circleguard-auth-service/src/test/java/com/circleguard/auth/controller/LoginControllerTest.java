package com.circleguard.auth.controller;

import com.circleguard.auth.client.IdentityClient;
import com.circleguard.auth.service.JwtTokenService;
import com.circleguard.auth.service.CustomUserDetailsService;
import com.circleguard.auth.client.IdentityServiceUnavailableException;
import com.circleguard.auth.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@Import(SecurityConfig.class)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authManager;

    @MockBean
    private JwtTokenService jwtService;

    @MockBean
    private IdentityClient identityClient;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";
    private static final UUID ANONYMOUS_ID = UUID.randomUUID();
    private static final String JWT_TOKEN = "mock-jwt-token";

    @Test
    void login_Success_ReturnsTokenAndAnonymousId() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(identityClient.getAnonymousId(VALID_USERNAME)).thenReturn(ANONYMOUS_ID);
        when(jwtService.generateToken(ANONYMOUS_ID, auth)).thenReturn(JWT_TOKEN);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"password\": \"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(JWT_TOKEN))
                .andExpect(jsonPath("$.anonymousId").value(ANONYMOUS_ID.toString()))
                .andExpect(jsonPath("$.type").value("Bearer"));
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        // Arrange
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"wrong\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_IdentityServiceUnavailable_Returns500() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(identityClient.getAnonymousId(VALID_USERNAME))
                                .thenThrow(new IdentityServiceUnavailableException("identity-service is down", new RuntimeException("Connection refused")));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"password\": \"password123\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(org.hamcrest.CoreMatchers.containsString("identity-service")));
    }

    @Test
    void login_MissingUsername_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\": \"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_MissingPassword_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void visitorHandoff_ValidAnonymousId_ReturnsToken() throws Exception {
        // Arrange
        String anonymousIdStr = ANONYMOUS_ID.toString();
        when(jwtService.generateToken(any(UUID.class), any(Authentication.class))).thenReturn(JWT_TOKEN);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/visitor/handoff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\": \"" + anonymousIdStr + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(JWT_TOKEN))
                .andExpect(jsonPath("$.handoffPayload").exists());
    }

    @Test
    void visitorHandoff_MissingAnonymousId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/visitor/handoff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void visitorHandoff_InvalidAnonymousId_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/visitor/handoff")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anonymousId\": \"not-a-valid-uuid\"}"))
                .andExpect(status().isBadRequest());
    }
}