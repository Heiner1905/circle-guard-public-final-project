package com.circleguard.promotion.controller;

import com.circleguard.promotion.repository.graph.UserNodeRepository;
import com.circleguard.promotion.service.AutoCircleService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EncounterController.class)
@Import(SecurityConfig.class)
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserNodeRepository userRepository;

    @MockBean
    private AutoCircleService autoCircleService;

    @Test
    void reportEncounter_RecordsAndEvaluates() throws Exception {
        String json = "{\"sourceId\": \"user-1\", \"targetId\": \"user-2\", \"locationId\": \"loc-123\"}";

        mockMvc.perform(post("/api/v1/encounters/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userRepository).recordEncounter(eq("user-1"), eq("user-2"), anyLong(), eq("loc-123"));
        verify(autoCircleService).evaluateEncounter("user-1", "user-2");
    }

    @Test
    void reportEncounter_WithoutLocationId_UsesDefault() throws Exception {
        String json = "{\"sourceId\": \"user-1\", \"targetId\": \"user-2\"}";

        mockMvc.perform(post("/api/v1/encounters/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(userRepository).recordEncounter(eq("user-1"), eq("user-2"), anyLong(), eq("mobile_ble"));
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void toggleValidity_WithHealthCenterRole_Toggles() throws Exception {
        mockMvc.perform(patch("/api/v1/encounters/1/validity"))
                .andExpect(status().isOk());

        verify(userRepository).toggleEncounterValidity(1L);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void forceFence_WithHealthCenterRole_ForcesFence() throws Exception {
        mockMvc.perform(post("/api/v1/encounters/1/force-fence"))
                .andExpect(status().isOk());

        verify(userRepository).forceEncounterFence(1L);
    }

    // ========== Tests para la clase interna EncounterRequest ==========

    @Test
    void encounterRequest_GettersAndSetters_ShouldWork() {
        EncounterController.EncounterRequest request = new EncounterController.EncounterRequest();
        
        request.setSourceId("user-123");
        request.setTargetId("user-456");
        request.setLocationId("loc-789");
        
        assert request.getSourceId().equals("user-123");
        assert request.getTargetId().equals("user-456");
        assert request.getLocationId().equals("loc-789");
    }

    @Test
    void encounterRequest_Equals_ShouldWorkCorrectly() {
        EncounterController.EncounterRequest request1 = new EncounterController.EncounterRequest();
        request1.setSourceId("user-123");
        request1.setTargetId("user-456");
        request1.setLocationId("loc-789");
        
        EncounterController.EncounterRequest request2 = new EncounterController.EncounterRequest();
        request2.setSourceId("user-123");
        request2.setTargetId("user-456");
        request2.setLocationId("loc-789");
        
        EncounterController.EncounterRequest request3 = new EncounterController.EncounterRequest();
        request3.setSourceId("user-111");
        request3.setTargetId("user-222");
        request3.setLocationId("loc-333");
        
        EncounterController.EncounterRequest request4 = new EncounterController.EncounterRequest();
        request4.setSourceId("user-123");
        request4.setTargetId("user-456");
        request4.setLocationId(null);
        
        // Reflexivo
        assert request1.equals(request1);
        
        // Simétrico
        assert request1.equals(request2);
        assert request2.equals(request1);
        
        // Transitivo
        assert request1.equals(request2);
        
        // Consistente
        assert request1.equals(request2);
        
        // Comparación con null
        assert !request1.equals(null);
        
        // Comparación con objeto diferente
        assert !request1.equals("string");
        
        // Comparación con valores diferentes
        assert !request1.equals(request3);
        
        // Comparación con locationId null
        assert !request1.equals(request4);
    }

    @Test
    void encounterRequest_HashCode_ShouldBeConsistent() {
        EncounterController.EncounterRequest request1 = new EncounterController.EncounterRequest();
        request1.setSourceId("user-123");
        request1.setTargetId("user-456");
        request1.setLocationId("loc-789");
        
        EncounterController.EncounterRequest request2 = new EncounterController.EncounterRequest();
        request2.setSourceId("user-123");
        request2.setTargetId("user-456");
        request2.setLocationId("loc-789");
        
        EncounterController.EncounterRequest request3 = new EncounterController.EncounterRequest();
        request3.setSourceId("user-111");
        request3.setTargetId("user-222");
        request3.setLocationId("loc-333");
        
        // Objetos iguales deben tener el mismo hashCode
        assert request1.hashCode() == request2.hashCode();
        
        // Objetos diferentes pueden tener hashCode diferentes
        // Solo verificamos que sea consistente
        assert request1.hashCode() == request1.hashCode();
    }

    @Test
    void encounterRequest_ToString_ShouldNotBeEmpty() {
        EncounterController.EncounterRequest request = new EncounterController.EncounterRequest();
        request.setSourceId("user-123");
        request.setTargetId("user-456");
        request.setLocationId("loc-789");
        
        String toString = request.toString();
        assert toString != null;
        assert !toString.isEmpty();
        assert toString.contains("sourceId");
        assert toString.contains("targetId");
        assert toString.contains("locationId");
        assert toString.contains("user-123");
        assert toString.contains("user-456");
        assert toString.contains("loc-789");
    }

    @Test
    void encounterRequest_CanEqual_ShouldReturnTrueForSameClass() {
        EncounterController.EncounterRequest request1 = new EncounterController.EncounterRequest();
        EncounterController.EncounterRequest request2 = new EncounterController.EncounterRequest();
        
        // canEqual debe devolver true para objetos de la misma clase
        assert request1.canEqual(request2);
        assert request1.canEqual(request1);
        
        // canEqual debe devolver false para objetos de diferente clase
        assert !request1.canEqual("string");
        assert !request1.canEqual(null);
    }

    @Test
    void encounterRequest_DefaultConstructor_ShouldCreateEmptyObject() {
        EncounterController.EncounterRequest request = new EncounterController.EncounterRequest();
        
        assert request.getSourceId() == null;
        assert request.getTargetId() == null;
        assert request.getLocationId() == null;
    }
}