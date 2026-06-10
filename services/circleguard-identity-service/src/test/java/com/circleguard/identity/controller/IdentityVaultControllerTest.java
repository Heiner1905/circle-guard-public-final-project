package com.circleguard.identity.controller;

import com.circleguard.identity.service.IdentityVaultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import com.circleguard.identity.config.SecurityConfig;

@WebMvcTest(IdentityVaultController.class)
@Import(SecurityConfig.class)
class IdentityVaultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentityVaultService vaultService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @WithMockUser(authorities = "identity:lookup")
    void lookupIdentity_WithPermission_ReturnsRealIdentity() throws Exception {
        UUID anonymousId = UUID.randomUUID();
        when(vaultService.resolveRealIdentity(anonymousId)).thenReturn("user@example.com");

        mockMvc.perform(get("/api/v1/identities/lookup/{id}", anonymousId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.realIdentity").value("user@example.com"));

        verify(kafkaTemplate).send(eq("audit.identity.accessed"), any());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void lookupIdentity_WithoutPermission_Returns403() throws Exception {
        UUID anonymousId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/identities/lookup/{id}", anonymousId))
                .andExpect(status().isForbidden());
    }

    @Test
    void lookupIdentity_Unauthenticated_Returns401() throws Exception {
        UUID anonymousId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/identities/lookup/{id}", anonymousId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "identity:lookup")
    void lookupIdentity_NotFound_Returns404ProblemDetail() throws Exception {
        UUID anonymousId = UUID.randomUUID();
        when(vaultService.resolveRealIdentity(anonymousId))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Identity not found"));

        mockMvc.perform(get("/api/v1/identities/lookup/{id}", anonymousId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404));

        verify(kafkaTemplate).send(eq("audit.identity.accessed"), any());
    }

    @Test
    @WithMockUser
    void mapIdentity_ShouldMapRealIdentityToAnonymousId() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String realIdentity = "user@example.com";
        
        when(vaultService.getOrCreateAnonymousId(realIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realIdentity\": \"user@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(realIdentity);
    }

    @Test
    @WithMockUser
    void mapIdentity_WithMissingRealIdentity_UsesAnonymousDefault() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        
        // The controller passes null to the service, but we can still return a valid UUID
        when(vaultService.getOrCreateAnonymousId(null)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(null);
    }

    @Test
    @WithMockUser
    void mapIdentity_WithEmptyRealIdentity_UsesAnonymousDefault() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        
        // The controller passes empty string to the service
        when(vaultService.getOrCreateAnonymousId("")).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/map")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"realIdentity\": \"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId("");
    }

    @Test
    @WithMockUser
    void registerVisitor_ShouldRegisterAndReturnAnonymousId() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String name = "John Doe";
        String email = "john@example.com";
        String reason = "Meeting";
        String expectedRealIdentity = "VISITOR|john@example.com|John Doe|Meeting";
        
        when(vaultService.getOrCreateAnonymousId(expectedRealIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/visitor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"John Doe\", \"email\": \"john@example.com\", \"reason_for_visit\": \"Meeting\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(expectedRealIdentity);
    }

    @Test
    @WithMockUser
    void registerVisitor_WithMissingName_UsesNullDefault() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String email = "john@example.com";
        String reason = "Meeting";
        String expectedRealIdentity = "VISITOR|john@example.com|null|Meeting";
        
        when(vaultService.getOrCreateAnonymousId(expectedRealIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/visitor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"john@example.com\", \"reason_for_visit\": \"Meeting\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(expectedRealIdentity);
    }

    @Test
    @WithMockUser
    void registerVisitor_WithMissingEmail_UsesNullDefault() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String name = "John Doe";
        String reason = "Meeting";
        String expectedRealIdentity = "VISITOR|null|John Doe|Meeting";
        
        when(vaultService.getOrCreateAnonymousId(expectedRealIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/visitor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"John Doe\", \"reason_for_visit\": \"Meeting\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(expectedRealIdentity);
    }

    @Test
    @WithMockUser
    void registerVisitor_WithMissingReason_UsesNullDefault() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String name = "John Doe";
        String email = "john@example.com";
        String expectedRealIdentity = "VISITOR|john@example.com|John Doe|null";
        
        when(vaultService.getOrCreateAnonymousId(expectedRealIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/visitor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"John Doe\", \"email\": \"john@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(expectedRealIdentity);
    }

    @Test
    @WithMockUser
    void registerVisitor_WithAllFieldsMissing_UsesNullDefaults() throws Exception {
        UUID expectedAnonymousId = UUID.randomUUID();
        String expectedRealIdentity = "VISITOR|null|null|null";
        
        when(vaultService.getOrCreateAnonymousId(expectedRealIdentity)).thenReturn(expectedAnonymousId);

        mockMvc.perform(post("/api/v1/identities/visitor")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anonymousId").value(expectedAnonymousId.toString()));
        
        verify(vaultService, times(1)).getOrCreateAnonymousId(expectedRealIdentity);
    }
}