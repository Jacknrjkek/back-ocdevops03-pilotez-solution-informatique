package com.datashare.backend.model;

import jakarta.persistence.*;

/**
 * Entité JPA représentant un token de partage.
 *
 * Permet l'accès externe à un fichier via un identifiant unique (UUID).
 */
@Entity
@Table(name = "share")
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token publique (UUID) utilisé dans les URLs
    @Column(name = "unique_token", unique = true, nullable = false)
    private String uniqueToken;

    // Compteur de téléchargements
    @Column(name = "download_count")
    private Integer downloadCount = 0;

    // Fichier associé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueToken() {
        return uniqueToken;
    }

    public void setUniqueToken(String uniqueToken) {
        this.uniqueToken = uniqueToken;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
