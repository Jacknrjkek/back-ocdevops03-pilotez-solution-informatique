package com.datashare.backend.repository;

import com.datashare.backend.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository pour les Partages.
 * Gère la récupération des liens de partage via le token unique.
 */
public interface ShareRepository extends JpaRepository<Share, Long> {

    // Récupère un partage via son token UUID.
    // Utilise @EntityGraph pour charger eageringly le 'file' associé (Optimisation
    // N+1)
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "file")
    Optional<Share> findByUniqueToken(String uniqueToken);

    // Retrouve le partage associé à un fichier donné
    Optional<Share> findByFileId(Long fileId);
}
