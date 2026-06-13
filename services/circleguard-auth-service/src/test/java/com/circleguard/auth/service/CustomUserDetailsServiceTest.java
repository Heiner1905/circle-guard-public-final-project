package com.circleguard.auth.service;

import com.circleguard.auth.model.LocalUser;
import com.circleguard.auth.model.Permission;
import com.circleguard.auth.model.Role;
import com.circleguard.auth.repository.LocalUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private LocalUserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsernameReturnsRolesAndPermissions() {
        Permission lookup = Permission.builder().name("identity:lookup").build();
        Role healthCenter = Role.builder().name("HEALTH_CENTER").permissions(Set.of(lookup)).build();
        LocalUser user = LocalUser.builder()
                .username("nurse")
                .password("{noop}secret")
                .isActive(true)
                .roles(Set.of(healthCenter))
                .build();

        when(userRepository.findByUsername("nurse")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("nurse");

        assertEquals("nurse", result.getUsername());
        assertEquals("{noop}secret", result.getPassword());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_HEALTH_CENTER")));
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("identity:lookup")));
    }

    @Test
    void loadUserByUsernameRejectsInactiveUser() {
        LocalUser inactive = LocalUser.builder()
                .username("disabled")
                .password("{noop}secret")
                .isActive(false)
                .roles(Set.of())
                .build();

        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(inactive));

        assertThrows(DisabledException.class, () -> service.loadUserByUsername("disabled"));
    }

    @Test
    void loadUserByUsernameRejectsMissingUser() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}
