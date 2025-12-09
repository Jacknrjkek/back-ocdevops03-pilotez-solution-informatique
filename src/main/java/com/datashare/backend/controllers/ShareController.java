package com.datashare.backend.controllers;

import com.datashare.backend.model.File;
import com.datashare.backend.repository.ShareRepository;
import com.datashare.backend.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class ShareController {

    @Autowired
    ShareRepository shareRepository;

    @Autowired
    FileStorageService fileStorageService;

    @GetMapping("/share/{token}")
    public ResponseEntity<?> getShareMetadata(@PathVariable String token) {
        return shareRepository.findByUniqueToken(token)
                .map(share -> {
                    File file = share.getFile();
                    if (file.getExpirationDate() != null && file.getExpirationDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.status(410).body(Map.of("message", "Link expired"));
                    }
                    return ResponseEntity.ok(Map.of(
                            "fileName", file.getOriginalName(),
                            "size", file.getSize(), // Size is now in Entity
                            "expiration", file.getExpirationDate()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<?> downloadFile(@PathVariable String token) {
        return shareRepository.findByUniqueToken(token)
                .map(share -> {
                    File file = share.getFile();
                    if (file.getExpirationDate() != null && file.getExpirationDate().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.status(410).body(Map.of("message", "Link expired"));
                    }

                    // Increment download count
                    share.setDownloadCount(share.getDownloadCount() + 1);
                    shareRepository.save(share);

                    Resource resource = fileStorageService.loadFileAsResource(file.getStoragePath());

                    String contentType = "application/octet-stream";

                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(contentType))
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + file.getOriginalName() + "\"")
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
