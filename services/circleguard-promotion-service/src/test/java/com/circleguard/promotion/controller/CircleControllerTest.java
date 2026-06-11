package com.circleguard.promotion.controller;

import com.circleguard.promotion.model.graph.CircleNode;
import com.circleguard.promotion.service.CircleService;
import com.circleguard.promotion.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CircleController.class)
@Import(SecurityConfig.class)
class CircleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CircleService circleService;

    @Test
    void createCircle_ShouldReturnCircle() throws Exception {
        CircleNode circle = new CircleNode();
        circle.setId(1L);
        circle.setName("Test Circle");

        when(circleService.createCircle(anyString(), anyString())).thenReturn(circle);

        String json = "{\"name\": \"Test Circle\", \"locationId\": \"loc-123\"}";

        mockMvc.perform(post("/api/v1/circles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void joinCircle_ShouldReturnCircle() throws Exception {
        CircleNode circle = new CircleNode();
        circle.setId(1L);

        when(circleService.joinCircle(eq("user-123"), eq("CODE123"))).thenReturn(circle);

        mockMvc.perform(post("/api/v1/circles/join/CODE123/user/user-123"))
                .andExpect(status().isOk());
    }

    @Test
    void addMember_ShouldReturnCircle() throws Exception {
        CircleNode circle = new CircleNode();
        circle.setId(1L);

        when(circleService.addMember(1L, "user-123")).thenReturn(circle);

        mockMvc.perform(post("/api/v1/circles/1/members/user-123"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserCircles_ShouldReturnList() throws Exception {
        CircleNode circle1 = new CircleNode();
        circle1.setId(1L);
        CircleNode circle2 = new CircleNode();
        circle2.setId(2L);
        List<CircleNode> circles = Arrays.asList(circle1, circle2);

        when(circleService.getUserCircles("user-123")).thenReturn(circles);

        mockMvc.perform(get("/api/v1/circles/user/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void toggleValidity_WithHealthCenterRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(patch("/api/v1/circles/1/validity"))
                .andExpect(status().isOk());

        verify(circleService).toggleCircleValidity(1L);
    }

    @Test
    @WithMockUser(roles = "HEALTH_CENTER")
    void forceFence_WithHealthCenterRole_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/circles/1/force-fence"))
                .andExpect(status().isOk());

        verify(circleService).forceFenceCircle(1L);
    }

    // ========== Tests para la clase interna CircleCreateRequest ==========

    @Test
    void circleCreateRequest_GettersAndSetters_ShouldWork() {
        CircleController.CircleCreateRequest request = new CircleController.CircleCreateRequest();
        
        request.setName("Test Circle");
        request.setLocationId("loc-123");
        
        assert request.getName().equals("Test Circle");
        assert request.getLocationId().equals("loc-123");
    }

    @Test
    void circleCreateRequest_Equals_ShouldWorkCorrectly() {
        CircleController.CircleCreateRequest request1 = new CircleController.CircleCreateRequest();
        request1.setName("Test Circle");
        request1.setLocationId("loc-123");
        
        CircleController.CircleCreateRequest request2 = new CircleController.CircleCreateRequest();
        request2.setName("Test Circle");
        request2.setLocationId("loc-123");
        
        CircleController.CircleCreateRequest request3 = new CircleController.CircleCreateRequest();
        request3.setName("Different Circle");
        request3.setLocationId("loc-456");
        
        CircleController.CircleCreateRequest request4 = new CircleController.CircleCreateRequest();
        request4.setName("Test Circle");
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
    void circleCreateRequest_HashCode_ShouldBeConsistent() {
        CircleController.CircleCreateRequest request1 = new CircleController.CircleCreateRequest();
        request1.setName("Test Circle");
        request1.setLocationId("loc-123");
        
        CircleController.CircleCreateRequest request2 = new CircleController.CircleCreateRequest();
        request2.setName("Test Circle");
        request2.setLocationId("loc-123");
        
        CircleController.CircleCreateRequest request3 = new CircleController.CircleCreateRequest();
        request3.setName("Different Circle");
        request3.setLocationId("loc-456");
        
        // Objetos iguales deben tener el mismo hashCode
        assert request1.hashCode() == request2.hashCode();
        
        // Objetos diferentes pueden tener hashCode diferentes
        // Solo verificamos que sea consistente
        assert request1.hashCode() == request1.hashCode();
    }

    @Test
    void circleCreateRequest_ToString_ShouldNotBeEmpty() {
        CircleController.CircleCreateRequest request = new CircleController.CircleCreateRequest();
        request.setName("Test Circle");
        request.setLocationId("loc-123");
        
        String toString = request.toString();
        assert toString != null;
        assert !toString.isEmpty();
        assert toString.contains("name");
        assert toString.contains("locationId");
        assert toString.contains("Test Circle");
        assert toString.contains("loc-123");
    }

    @Test
    void circleCreateRequest_CanEqual_ShouldReturnTrueForSameClass() {
        CircleController.CircleCreateRequest request1 = new CircleController.CircleCreateRequest();
        CircleController.CircleCreateRequest request2 = new CircleController.CircleCreateRequest();
        
        // canEqual debe devolver true para objetos de la misma clase
        assert request1.canEqual(request2);
        assert request1.canEqual(request1);
        
        // canEqual debe devolver false para objetos de diferente clase
        assert !request1.canEqual("string");
        assert !request1.canEqual(null);
    }

    @Test
    void circleCreateRequest_DefaultConstructor_ShouldCreateEmptyObject() {
        CircleController.CircleCreateRequest request = new CircleController.CircleCreateRequest();
        
        assert request.getName() == null;
        assert request.getLocationId() == null;
    }

    @Test
    void circleCreateRequest_Equality_WithNullValues() {
        CircleController.CircleCreateRequest request1 = new CircleController.CircleCreateRequest();
        request1.setName(null);
        request1.setLocationId(null);
        
        CircleController.CircleCreateRequest request2 = new CircleController.CircleCreateRequest();
        request2.setName(null);
        request2.setLocationId(null);
        
        // Ambos con valores null deben ser iguales
        assert request1.equals(request2);
        assert request1.hashCode() == request2.hashCode();
    }
}