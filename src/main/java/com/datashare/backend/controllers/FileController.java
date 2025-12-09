package com.datashare.backend.controllers;

import com.datashare.backend.model.AppUser;
import com.datashare.backend.model.File;
import com.datashare.backend.model.Share;
import com.datashare.backend.payload.response.FileResponse;
import com.datashare.backend.payload.response.MessageResponse;
import com.datashare.backend.repository.AppUserRepository;
import com.datashare.backend.repository.FileRepository;
import com.datashare.backend.repository.ShareRepository;
import com.datashare.backend.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    AppUserRepository userRepository;

    @Autowired
    ShareRepository shareRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            AppUser user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String fileName = fileStorageService.store(file);

            File fileEntity = new File();
            fileEntity.setOriginalName(file.getOriginalFilename());
            fileEntity.setStoragePath(fileName);
            fileEntity.setSize(file.getSize());
            fileEntity.setOwner(user);
            fileEntity.setExpirationDate(LocalDateTime.now().plusDays(30)); // Default 30 days as per user update

            fileRepository.save(fileEntity);

            // Create default share
            Share share = new Share();
            share.setFile(fileEntity);
            share.setUniqueToken(UUID.randomUUID().toString());
            shareRepository.save(share);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "fileId", fileEntity.getId(),
                    "shareToken", share.getUniqueToken(),
                    "message", "File uploaded successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new MessageResponse(
                    "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<FileResponse>> getListFiles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal(); // Assuming auth filter ensures principal
                                                                               // is UserDetails
        AppUser user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<File> files = fileRepository.findByOwnerId(user.getId());

        List<FileResponse> fileResponses = files.stream().map(dbFile -> {
            String token = shareRepository.findByFileId(dbFile.getId())
                    .map(Share::getUniqueToken)
                    .orElse(null);

            return new FileResponse(
                    dbFile.getId(),
                    dbFile.getOriginalName(),
                    dbFile.getSize(),
                    dbFile.getCreatedAt(),
                    dbFile.getExpirationDate(),
                    token);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(fileResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AppUser user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        return fileRepository.findById(id).map(file -> {
            if (!file.getOwner().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            fileStorageService.delete(file.getStoragePath());
            fileRepository.delete(file);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
