package com.circleguard.auth.controller;

import com.circleguard.auth.model.LocalUser;
import com.circleguard.auth.repository.LocalUserRepository;
import com.circleguard.auth.security.DualChainAuthenticationProvider;
import com.circleguard.auth.security.SecurityConfig;
import com.circleguard.auth.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LocalUserRepository localUserRepository;

    @MockBean
    private DualChainAuthenticationProvider dualChainAuthenticationProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(authorities = "SCOPE_alert:receive_priority")
    void getUsersByPermission_WithPermission_ReturnsUserList() throws Exception {
        // Arrange
        LocalUser user1 = new LocalUser();
        user1.setUsername("admin1");
        user1.setEmail("admin1@circleguard.edu");

        LocalUser user2 = new LocalUser();
        user2.setUsername("admin2");
        user2.setEmail("admin2@circleguard.edu");

        when(localUserRepository.findUsersByPermissionName("alert:receive_priority"))
                .thenReturn(List.of(user1, user2));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/permissions/alert:receive_priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin1"))
                .andExpect(jsonPath("$[1].username").value("admin2"));
    }

    @Test
    @WithMockUser
    void getUsersByPermission_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(localUserRepository.findUsersByPermissionName(anyString()))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/permissions/alert:receive_priority"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}