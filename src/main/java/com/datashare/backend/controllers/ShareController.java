package com.datashare.backend.controllers;

// === Imports métier ===
import com.datashare.backend.model.File;
import com.datashare.backend.repository.ShareRepository;
import com.datashare.backend.services.FileStorageService;

// === Imports Spring ===
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// === Utilitaires ===
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller responsable de l'accès publique aux fichiers partagés.
 *
 * - Enpoints d'accès Anonyme (pas de vérification JWT)
 * - Récupération des métadonnées via Token
 * - Téléchargement physique du fichier
 */
@RestController
@RequestMapping("/api")
public class ShareController {

    /**
     * Repository pour vérifier la validité des tokens de partage.
     */
    @Autowired
    ShareRepository shareRepository;

    /**
     * Service pour accéder au système de fichiers (stockage).
     */
    @Autowired
    FileStorageService fileStorageService;

    /**
     * Endpoint pour récupérer les infos d'un partage (taille, nom, expiration).
     * Accessible sans authentification.
     *
     * @param token le token unique de partage
     * @return métadonnées du fichier ou erreur 410 (Expiré) / 404 (Introuvable)
     */
    @GetMapping("/share/{token}")
    public ResponseEntity<?> getShareMetadata(@PathVariable String token) {
        return shareRepository.findByUniqueToken(token)
                .map(share -> {
                    File file = share.getFile();

                    // Vérification de la date d'expiration
                    if (file.getExpirationDate() != null && file.getExpirationDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.status(410).body(Map.of("message", "Link expired"));
                    }

                    // Construction de la réponse JSON simplifiée
                    java.util.Map<String, Object> response = new java.util.HashMap<>();
                    response.put("fileName", file.getOriginalName());
                    response.put("size", file.getSize());
                    response.put("expiration",
                            file.getExpirationDate() != null ? file.getExpirationDate().toString() : null);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint de téléchargement du fichier.
     * Accessible sans authentification.
     *
     * @param token le token unique de partage
     * @return flux binaire du fichier (Resource)
     */
    @GetMapping("/download/{token}")
    public ResponseEntity<?> downloadFile(@PathVariable String token) {
        return shareRepository.findByUniqueToken(token)
                .map(share -> {
                    File file = share.getFile();

                    // Vérification de l'expiration avant téléchargement
                    if (file.getExpirationDate() != null && file.getExpirationDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.status(410).body(Map.of("message", "Link expired"));
                    }

                    // Incrémente le compteur de téléchargement
                    share.setDownloadCount(share.getDownloadCount() + 1);
                    shareRepository.save(share);

                    // Charge le fichier physique
                    Resource resource = fileStorageService.loadFileAsResource(file.getStoragePath());

                    String contentType = "application/octet-stream";

                    // Retourne le fichier en attachment (force le téléchargement navigateur)
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + file.getOriginalName() + "\"")
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
