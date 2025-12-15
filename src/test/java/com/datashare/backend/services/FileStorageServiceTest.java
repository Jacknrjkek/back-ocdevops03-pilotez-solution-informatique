package com.datashare.backend.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests Unitaires pour FileStorageService.
 * Vérifie le stockage physique des fichiers sur le disque.
 */
public class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private final String TEST_UPLOAD_DIR = "target/test-uploads";

    @BeforeEach
    public void setUp() throws Exception {
        fileStorageService = new FileStorageService();

        // Injection du répertoire de test via Reflection
        Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, TEST_UPLOAD_DIR);

        // Initialisation manuelle
        fileStorageService.init();
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Nettoyage après chaque test
        FileSystemUtils.deleteRecursively(Paths.get(TEST_UPLOAD_DIR));
    }

    /**
     * Vérifie qu'un fichier valide est bien écrit sur le disque.
     */
    @Test
    public void testStoreFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes());

        String storedFilename = fileStorageService.store(file);

        assertNotNull(storedFilename);
        assertTrue(storedFilename.contains("test.txt"));

        Path path = Paths.get(TEST_UPLOAD_DIR).resolve(storedFilename);
        assertTrue(Files.exists(path));
    }

    /**
     * Vérifie que le stockage d'un fichier vide lève une exception.
     */
    @Test
    public void testStoreEmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                "".getBytes());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.store(file);
        });

        assertEquals("Failed to store empty file.", exception.getMessage());
    }
}
