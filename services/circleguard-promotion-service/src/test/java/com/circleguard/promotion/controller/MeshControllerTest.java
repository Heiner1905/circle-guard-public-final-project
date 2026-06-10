package com.circleguard.promotion.controller;

import com.circleguard.promotion.repository.graph.UserNodeRepository;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MeshController.class)
@Import(SecurityConfig.class)
class MeshControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserNodeRepository userRepository;

    @Test
    void getMeshStats_ShouldReturnStats() throws Exception {
        String anonymousId = "user-123";
        
        when(userRepository.getConfirmedConnectionCount(anonymousId)).thenReturn(5L);
        when(userRepository.getUnconfirmedConnectionCount(anonymousId)).thenReturn(3L);

        mockMvc.perform(get("/api/v1/mesh/stats/{anonymousId}", anonymousId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(5))
                .andExpect(jsonPath("$.unconfirmedCount").value(3));
    }

    @Test
    void getMeshStats_WithNoConnections_ShouldReturnZero() throws Exception {
        String anonymousId = "user-456";
        
        when(userRepository.getConfirmedConnectionCount(anonymousId)).thenReturn(0L);
        when(userRepository.getUnconfirmedConnectionCount(anonymousId)).thenReturn(0L);

        mockMvc.perform(get("/api/v1/mesh/stats/{anonymousId}", anonymousId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedCount").value(0))
                .andExpect(jsonPath("$.unconfirmedCount").value(0));
    }

    // Tests para la clase interna MeshStatsResponse
    @Test
    void meshStatsResponse_Builder_ShouldCreateCorrectObject() {
        MeshController.MeshStatsResponse response = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        assert response.getConfirmedCount() == 10L;
        assert response.getUnconfirmedCount() == 5L;
    }

    @Test
    void meshStatsResponse_Equals_ShouldWorkCorrectly() {
        MeshController.MeshStatsResponse response1 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        MeshController.MeshStatsResponse response2 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        MeshController.MeshStatsResponse response3 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(7L)
                .unconfirmedCount(3L)
                .build();

        // Reflexivo
        assert response1.equals(response1);
        
        // Simétrico
        assert response1.equals(response2);
        assert response2.equals(response1);
        
        // Transitivo
        assert response1.equals(response2);
        
        // Consistente
        assert response1.equals(response2);
        
        // Comparación con null
        assert !response1.equals(null);
        
        // Comparación con objeto diferente
        assert !response1.equals("string");
        
        // Comparación con valores diferentes
        assert !response1.equals(response3);
    }

    @Test
    void meshStatsResponse_HashCode_ShouldBeConsistent() {
        MeshController.MeshStatsResponse response1 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        MeshController.MeshStatsResponse response2 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        MeshController.MeshStatsResponse response3 = MeshController.MeshStatsResponse.builder()
                .confirmedCount(7L)
                .unconfirmedCount(3L)
                .build();

        // Objetos iguales deben tener el mismo hashCode
        assert response1.hashCode() == response2.hashCode();
        
        // Objetos diferentes pueden tener hashCode diferentes (no es obligatorio pero es esperable)
        // Solo verificamos que sea consistente
        assert response1.hashCode() == response1.hashCode();
    }

    @Test
    void meshStatsResponse_ToString_ShouldNotBeEmpty() {
        MeshController.MeshStatsResponse response = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        String toString = response.toString();
        assert toString != null;
        assert !toString.isEmpty();
        assert toString.contains("confirmedCount");
        assert toString.contains("unconfirmedCount");
        assert toString.contains("10");
        assert toString.contains("5");
    }

    @Test
    void meshStatsResponse_Setters_ShouldUpdateValues() {
        MeshController.MeshStatsResponse response = MeshController.MeshStatsResponse.builder()
                .confirmedCount(10L)
                .unconfirmedCount(5L)
                .build();

        response.setConfirmedCount(20L);
        response.setUnconfirmedCount(15L);

        assert response.getConfirmedCount() == 20L;
        assert response.getUnconfirmedCount() == 15L;
    }

    @Test
    void meshStatsResponse_AllArgsConstructor_ShouldWork() {
        MeshController.MeshStatsResponse response = new MeshController.MeshStatsResponse(10L, 5L);
        
        assert response.getConfirmedCount() == 10L;
        assert response.getUnconfirmedCount() == 5L;
    }
}