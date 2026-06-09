package com.circleguard.file.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    @Test
    void saveFileStoresContentWithGeneratedPrefix() throws Exception {
        FileStorageService service = new FileStorageService();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "certificate.txt",
                "text/plain",
                "approved".getBytes()
        );

        String filename = service.saveFile(file);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            assertTrue(filename.endsWith("_certificate.txt"));
            assertEquals("approved", Files.readString(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }
}
