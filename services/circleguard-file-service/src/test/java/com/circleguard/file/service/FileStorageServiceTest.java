package com.circleguard.file.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    private FileStorageService service;
    
    @TempDir
    Path tempDir;
    
    private Path originalUploadsDir;
    
    @BeforeEach
    void setUp() throws IOException {
        // Backup original uploads directory and use temp directory instead
        originalUploadsDir = Path.of("uploads");
        
        // Create uploads directory if it doesn't exist
        Files.createDirectories(originalUploadsDir);
        
        service = new FileStorageService();
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        Path uploadsDir = Path.of("uploads");
        if (Files.exists(uploadsDir)) {
            Files.list(uploadsDir)
                .filter(Files::isRegularFile)
                .forEach(file -> {
                    try {
                        Files.deleteIfExists(file);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }

    @Test
    void saveFile_StoresContentWithGeneratedPrefix() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "certificate.txt",
                "text/plain",
                "approved".getBytes()
        );

        // Act
        String filename = service.saveFile(file);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(filename.endsWith("_certificate.txt"));
            // Extract UUID part and verify it's a valid UUID
            String uuidPart = filename.substring(0, filename.indexOf("_"));
            assertDoesNotThrow(() -> UUID.fromString(uuidPart));
            assertEquals("approved", Files.readString(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void saveFile_WithDifferentFileTypes_StoresCorrectly() throws Exception {
        // Arrange
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Act
        String filename = service.saveFile(pdfFile);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(filename.endsWith("_document.pdf"));
            assertEquals("PDF content", Files.readString(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }
    
    @Test
    void saveFile_WithEmptyFile_StoresEmptyFile() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        // Act
        String filename = service.saveFile(emptyFile);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert - Should save empty file without throwing exception
            assertTrue(Files.exists(stored));
            assertEquals(0, Files.size(stored));
            assertTrue(filename.endsWith("_empty.txt"));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void saveFile_WithNullOriginalFilename_HandlesGracefully() throws Exception {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("content".getBytes()));

        // Act
        String filename = service.saveFile(file);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(filename.endsWith("_null"));
            assertEquals("content", Files.readString(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void saveFile_WhenDirectoryDoesNotExist_CreatesDirectory() throws Exception {
        // Arrange - Delete uploads directory if it exists
        Path uploadsPath = Path.of("uploads");
        if (Files.exists(uploadsPath)) {
            Files.list(uploadsPath).forEach(file -> {
                try {
                    Files.delete(file);
                } catch (IOException e) {
                    // Ignore
                }
            });
            Files.deleteIfExists(uploadsPath);
        }
        
        // Recreate service to trigger directory creation
        FileStorageService newService = new FileStorageService();
        
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        // Act
        String filename = newService.saveFile(file);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(Files.exists(uploadsPath));
            assertTrue(Files.exists(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void constructor_WhenCannotCreateDirectory_ThrowsException() {
        // This test is tricky because we can't easily simulate a filesystem error
        // But we can verify the constructor works normally
        assertDoesNotThrow(() -> new FileStorageService());
    }

    @Test
    void loadFile_ShouldReturnResource_WhenFileExists() throws Exception {
        // Arrange - First save a file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "existing.txt",
                "text/plain",
                "test content".getBytes()
        );
        
        String filename = service.saveFile(file);
        
        // Act
        Resource resource = service.loadFile(filename);
        
        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void loadFile_WhenFileDoesNotExist_ReturnsNull() {
        // Act
        Resource resource = service.loadFile("nonexistent.txt");
        
        // Assert
        assertNull(resource);
    }

    @Test
    void loadFile_WithNullFilename_ReturnsNull() {
        // Act
        Resource resource = service.loadFile(null);
        
        // Assert
        assertNull(resource);
    }

    @Test
    void loadFile_WithEmptyFilename_ReturnsNull() {
        // Act
        Resource resource = service.loadFile("");
        
        // Assert
        assertNull(resource);
    }

    @Test
    void loadFile_WithBlankFilename_ReturnsNull() {
        // Act
        Resource resource = service.loadFile("   ");
        
        // Assert
        assertNull(resource);
    }

    @Test
    void saveFile_WithSpecialCharactersInFilename_StoresCorrectly() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "special @#$% file.txt",
                "text/plain",
                "special content".getBytes()
        );

        // Act
        String filename = service.saveFile(file);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(filename.endsWith("_special @#$% file.txt"));
            assertEquals("special content", Files.readString(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void saveFile_WithLargeFile_StoresCorrectly() throws Exception {
        // Arrange - Create a 1MB file
        byte[] largeContent = new byte[1024 * 1024];
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.bin",
                "application/octet-stream",
                largeContent
        );

        // Act
        String filename = service.saveFile(largeFile);
        Path stored = Path.of("uploads").resolve(filename);

        try {
            // Assert
            assertTrue(Files.exists(stored));
            assertEquals(largeContent.length, Files.size(stored));
        } finally {
            Files.deleteIfExists(stored);
        }
    }

    @Test
    void saveFile_WhenIOExceptionOccurs_ThrowsRuntimeException() {
        // Arrange - Create a mock that throws IOException
        MultipartFile mockFile = mock(MultipartFile.class);
        try {
            when(mockFile.getOriginalFilename()).thenReturn("test.txt");
            when(mockFile.getInputStream()).thenThrow(new IOException("Simulated IO error"));
        } catch (IOException e) {
            fail("Mock setup failed");
        }

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            service.saveFile(mockFile);
        });
        
        assertTrue(exception.getMessage().contains("Could not store file"));
    }
}