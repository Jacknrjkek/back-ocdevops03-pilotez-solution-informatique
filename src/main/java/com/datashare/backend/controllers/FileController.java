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
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "expirationTime", required = false) Integer expirationTime) {
        System.out.println("FileController: uploadFile called. File: " + file.getOriginalFilename() + ", Expiration: "
                + expirationTime);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            AppUser user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("FileController: User found: " + user.getEmail());

            String fileName = fileStorageService.store(file);
            System.out.println("FileController: File stored: " + fileName);

            File fileEntity = new File();
            fileEntity.setOriginalName(file.getOriginalFilename());
            fileEntity.setStoragePath(fileName);
            fileEntity.setSize(file.getSize());
            fileEntity.setOwner(user);

            // Expiration logic: Default 7 days, User request capped at 7 days
            int days = (expirationTime != null) ? Math.min(expirationTime, 7) : 7;
            if (days < 1)
                days = 1; // Minimum 1 day

            fileEntity.setExpirationDate(LocalDateTime.now().plusDays(days));

            fileRepository.save(fileEntity);
            System.out.println("FileController: File entity saved. ID: " + fileEntity.getId());

            // Create default share
            Share share = new Share();
            share.setFile(fileEntity);
            share.setUniqueToken(UUID.randomUUID().toString());
            shareRepository.save(share);
            System.out.println("FileController: Share created. Token: " + share.getUniqueToken());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "fileId", fileEntity.getId(),
                    "shareToken", share.getUniqueToken(),
                    "message", "File uploaded successfully"));

        } catch (Exception e) {
            System.out.println("FileController: Error: " + e.getMessage());
            e.printStackTrace();
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
        System.out.println("FileController: getListFiles called for user " + user.getEmail() + ". Found " + files.size()
                + " files.");

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
            fileStorageService.delete(file.getStoragePath());
            fileRepository.delete(file);
            return ResponseEntity.ok(Map.of("message", "Fichier supprimé avec succès"));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
