package com.datashare.backend.repository;

import com.datashare.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository gérant l'accès aux données des Utilisateurs.
 * Hérite de JpaRepository pour bénéficier des méthodes CRUD standard.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Recherche d'un user par email (optionnel car peut être null)
    Optional<AppUser> findByEmail(String email);

    // Vérifie l'existence d'un email (pour l'inscription)
    boolean existsByEmail(String email);
}
