package com.circleguard.identity.service;

import com.circleguard.identity.model.IdentityMapping;
import com.circleguard.identity.repository.IdentityMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdentityVaultServiceTest {

    @Mock
    private IdentityMappingRepository repository;

    @InjectMocks
    private IdentityVaultService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "hashSalt", "unit-test-salt");
    }

    @Test
    void getOrCreateAnonymousIdReturnsExistingMapping() {
        UUID anonymousId = UUID.randomUUID();
        IdentityMapping existing = IdentityMapping.builder().anonymousId(anonymousId).build();
        when(repository.findByIdentityHash(anyString())).thenReturn(Optional.of(existing));

        UUID result = service.getOrCreateAnonymousId("student@example.edu");

        assertEquals(anonymousId, result);
        verify(repository, never()).save(any());
    }

    @Test
    void getOrCreateAnonymousIdCreatesNewMappingWithHashedIdentity() {
        UUID anonymousId = UUID.randomUUID();
        when(repository.findByIdentityHash(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(IdentityMapping.class))).thenAnswer(invocation -> {
            IdentityMapping mapping = invocation.getArgument(0);
            mapping.setAnonymousId(anonymousId);
            return mapping;
        });

        UUID result = service.getOrCreateAnonymousId("student@example.edu");

        assertEquals(anonymousId, result);
        ArgumentCaptor<IdentityMapping> mappingCaptor = ArgumentCaptor.forClass(IdentityMapping.class);
        verify(repository).save(mappingCaptor.capture());
        assertEquals("student@example.edu", mappingCaptor.getValue().getRealIdentity());
        assertNotNull(mappingCaptor.getValue().getIdentityHash());
        assertNotNull(mappingCaptor.getValue().getSalt());
    }

    @Test
    void resolveRealIdentityReturnsStoredIdentity() {
        UUID anonymousId = UUID.randomUUID();
        IdentityMapping mapping = IdentityMapping.builder()
                .anonymousId(anonymousId)
                .realIdentity("student@example.edu")
                .build();
        when(repository.findById(anonymousId)).thenReturn(Optional.of(mapping));

        assertEquals("student@example.edu", service.resolveRealIdentity(anonymousId));
    }

    @Test
    void resolveRealIdentityThrowsNotFoundWhenMissing() {
        UUID anonymousId = UUID.randomUUID();
        when(repository.findById(anonymousId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.resolveRealIdentity(anonymousId));
    }
}
