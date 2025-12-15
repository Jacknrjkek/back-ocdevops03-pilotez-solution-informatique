package com.datashare.backend.services;

// === Imports Spring ===
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

// === Imports I/O ===
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service gérant les opérations bas niveau sur le système de fichiers.
 *
 * - Initialise le dossier d'upload
 * - Stocke les fichiers avec un nom unique (UUID)
 * - Charge les fichiers sous forme de Resource
 * - Supprime les fichiers physiques
 */
@Service
public class FileStorageService {

    /**
     * Chemin du dossier de stockage (injecté depuis application.properties).
     */
    @Value("${datashare.app.uploadDir}")
    private String uploadDir;

    private Path rootLocation;

    /**
     * Initialisation du service au démarrage de l'application.
     * Crée le dossier d'upload s'il n'existe pas.
     */
    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir);
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Stocke un fichier reçu.
     *
     * @param file le fichier Multipart reçu du contrôleur
     * @return le nom de fichier unique généré
     */
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file.");
            }
            // Génère un nom unique pour éviter les collisions (UUID + nom d'origine)
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Résout le chemin absolu de destination
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            // Vérification de sécurité : empêche l'écriture en dehors du dossier prévu
            // (Path Traversal)
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new RuntimeException("Cannot store file outside current directory.");
            }

            // Copie le flux du fichier vers la destination
            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    /**
     * Supprime un fichier physique.
     *
     * @param filename nom du fichier stocké
     */
    public void delete(String filename) {
        try {
            Path file = rootLocation.resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file: " + filename, e);
        }
    }

    /**
     * Charge un fichier en tant que Resource (pour le téléchargement).
     *
     * @param filename nom du fichier
     * @return Resource prête à être streamée
     */
    public org.springframework.core.io.Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.rootLocation.resolve(filename).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(
                    filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filename);
            }
        } catch (java.net.MalformedURLException e) {
            throw new RuntimeException("File not found " + filename, e);
        }
    }
}
