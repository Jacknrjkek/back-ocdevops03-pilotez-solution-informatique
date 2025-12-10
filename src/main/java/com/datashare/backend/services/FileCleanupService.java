package com.datashare.backend.services;

import com.datashare.backend.model.File;
import com.datashare.backend.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileCleanupService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        List<File> expiredFiles = fileRepository.findByExpirationDateBefore(now);

        for (File file : expiredFiles) {
            try {
                // Delete from physical storage
                fileStorageService.delete(file.getStoragePath());

                // Delete from DB
                fileRepository.delete(file);

                System.out.println("Deleted expired file: " + file.getOriginalName() + " (ID: " + file.getId() + ")");
            } catch (Exception e) {
                System.err.println("Error deleting expired file ID " + file.getId() + ": " + e.getMessage());
            }
        }
    }
}
