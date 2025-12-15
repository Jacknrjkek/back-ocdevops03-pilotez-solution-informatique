package com.datashare.backend.repository;

import com.datashare.backend.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour les Fichiers.
 * Permet de récupérer les fichiers par propriétaire, chemin ou date
 * d'expiration.
 */
public interface FileRepository extends JpaRepository<File, Long> {

    // Retrouve un fichier via son chemin de stockage unique
    Optional<File> findByStoragePath(String storagePath);

    // Liste tous les fichiers d'un utilisateur spécifique (ID)
    List<File> findByOwnerId(Long ownerId);

    // Trouve les fichiers expirés (date d'expiration < date donnée) pour le
    // nettoyage
    List<File> findByExpirationDateBefore(java.time.LocalDateTime date);
}
