package com.datashare.backend.services;

import com.datashare.backend.model.File;
import com.datashare.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service de tâche planifiée (CRON job) pour le nettoyage automatique.
 * Supprime les fichiers dont la date d'expiration est dépassée.
 */
@Service
public class FileCleanupService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Exécuté toutes les heures (3600000 ms).
     * Vérifie et supprime les fichiers expirés de la DB et du disque.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        // Recherche des fichiers expirés
        List<File> expiredFiles = fileRepository.findByExpirationDateBefore(now);

        for (File file : expiredFiles) {
            try {
                // 1. Suppression physique du fichier
                fileStorageService.delete(file.getStoragePath());

                // 2. Suppression de l'entrée en base de données
                fileRepository.delete(file);

                System.out.println("Deleted expired file: " + file.getOriginalName() + " (ID: " + file.getId() + ")");
            } catch (Exception e) {
                System.err.println("Error deleting expired file ID " + file.getId() + ": " + e.getMessage());
            }
        }
    }
}
