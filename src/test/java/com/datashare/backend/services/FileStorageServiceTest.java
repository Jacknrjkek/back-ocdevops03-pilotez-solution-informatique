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

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private final String TEST_UPLOAD_DIR = "target/test-uploads";

    @BeforeEach
    public void setUp() throws Exception {
        fileStorageService = new FileStorageService();

        // Inject uploadDir using Reflection
        Field uploadDirField = FileStorageService.class.getDeclaredField("uploadDir");
        uploadDirField.setAccessible(true);
        uploadDirField.set(fileStorageService, TEST_UPLOAD_DIR);

        // Manually trigger init
        fileStorageService.init();
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(Paths.get(TEST_UPLOAD_DIR));
    }

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
