package com.circleguard.notification.controller;

import com.circleguard.notification.service.NotificationDispatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationDispatcher dispatcher;

    @Test
    void dispatchAcceptsValidNotificationRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-123\",\"status\":\"SUSPECT\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Notification dispatch accepted"));

        verify(dispatcher).dispatch("user-123", "SUSPECT");
    }

    @Test
    void dispatchRejectsMissingStatus() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/dispatch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user-123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("userId and status are required"));

        verifyNoInteractions(dispatcher);
    }
}
