package com.datashare.backend.repository;

import com.datashare.backend.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByStoragePath(String storagePath);

    List<File> findByOwnerId(Long ownerId);

    List<File> findByExpirationDateBefore(java.time.LocalDateTime date);
}
