package com.circleguard.promotion.controller;

import com.circleguard.promotion.service.MacSessionRegistry;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SessionHandshakeController.class)
@Import(SecurityConfig.class)
class SessionHandshakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MacSessionRegistry sessionRegistry;

    @Test
    void handshake_WithValidData_ShouldRegisterSession() throws Exception {
        String json = "{\"macAddress\": \"AA:BB:CC:DD:EE:FF\", \"anonymousId\": \"user-123\"}";

        mockMvc.perform(post("/api/v1/sessions/handshake")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(sessionRegistry).registerSession("AA:BB:CC:DD:EE:FF", "user-123");
    }

    @Test
    void handshake_WithMissingMacAddress_ShouldReturnBadRequest() throws Exception {
        String json = "{\"anonymousId\": \"user-123\"}";

        mockMvc.perform(post("/api/v1/sessions/handshake")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(sessionRegistry, never()).registerSession(any(), any());
    }

    @Test
    void handshake_WithMissingAnonymousId_ShouldReturnBadRequest() throws Exception {
        String json = "{\"macAddress\": \"AA:BB:CC:DD:EE:FF\"}";

        mockMvc.perform(post("/api/v1/sessions/handshake")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verify(sessionRegistry, never()).registerSession(any(), any());
    }

    @Test
    void closeSession_ShouldCloseSession() throws Exception {
        String macAddress = "AA:BB:CC:DD:EE:FF";

        mockMvc.perform(delete("/api/v1/sessions/{macAddress}", macAddress))
                .andExpect(status().isNoContent());

        verify(sessionRegistry).closeSession(macAddress);
    }
}