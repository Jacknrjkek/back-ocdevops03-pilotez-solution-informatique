package com.datashare.backend.controllers;

import com.datashare.backend.model.File;
import com.datashare.backend.model.Share;
import com.datashare.backend.repository.ShareRepository;
import com.datashare.backend.services.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Tests Unitaires pour ShareController.
 * Gère l'accès public (non authentifié) aux fichiers via Token.
 */
@ExtendWith(MockitoExtension.class)
public class ShareControllerTest {

    @Mock
    private ShareRepository shareRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ShareController shareController;

    /**
     * Récupération des métadonnées (Nom, Taille) avec un token valide.
     */
    @Test
    void getShareMetadata_Success() {
        String token = "valid-token";
        File file = new File();
        file.setOriginalName("test.txt");
        file.setSize(123L);
        Share share = new Share();
        share.setFile(file);

        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.of(share));

        ResponseEntity<?> response = shareController.getShareMetadata(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("test.txt", body.get("fileName"));
        assertEquals(123L, body.get("size"));
    }

    @Test
    void getShareMetadata_NotFound() {
        String token = "invalid-token";
        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.empty());

        ResponseEntity<?> response = shareController.getShareMetadata(token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    /**
     * Vérifie le code 410 (Gone) si le fichier associé est expiré.
     */
    @Test
    void getShareMetadata_Expired() {
        String token = "expired-token";
        File file = new File();
        file.setExpirationDate(LocalDateTime.now().minusDays(1));
        Share share = new Share();
        share.setFile(file);

        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.of(share));

        ResponseEntity<?> response = shareController.getShareMetadata(token);

        assertEquals(HttpStatus.GONE, response.getStatusCode()); // 410 Gone
    }

    /**
     * Téléchargement réussi : Doit incrémenter le compteur de téléchargements.
     */
    @Test
    void downloadFile_Success() throws MalformedURLException {
        String token = "valid-token";
        File file = new File();
        file.setOriginalName("test.txt");
        file.setStoragePath("path/to/test.txt");
        Share share = new Share();
        share.setFile(file);

        // Mock Resource
        Resource resource = mock(Resource.class);

        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.of(share));
        when(fileStorageService.loadFileAsResource("path/to/test.txt")).thenReturn(resource);

        ResponseEntity<?> response = shareController.downloadFile(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(shareRepository).save(share); // Check download count increment
        assertEquals(1, share.getDownloadCount());
    }

    @Test
    void downloadFile_NotFound() {
        String token = "invalid-token";
        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.empty());

        ResponseEntity<?> response = shareController.downloadFile(token);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void downloadFile_Expired() {
        String token = "expired-token";
        File file = new File();
        file.setExpirationDate(LocalDateTime.now().minusDays(1));
        Share share = new Share();
        share.setFile(file);

        when(shareRepository.findByUniqueToken(token)).thenReturn(Optional.of(share));

        ResponseEntity<?> response = shareController.downloadFile(token);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
    }
}
