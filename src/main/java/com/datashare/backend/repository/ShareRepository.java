package com.datashare.backend.repository;

import com.datashare.backend.model.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ShareRepository extends JpaRepository<Share, Long> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "file")
    Optional<Share> findByUniqueToken(String uniqueToken);

    Optional<Share> findByFileId(Long fileId);
}
