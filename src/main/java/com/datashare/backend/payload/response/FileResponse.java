package com.datashare.backend.payload.response;

import java.time.LocalDateTime;

public class FileResponse {
    private Long id;
    private String originalName;
    private Long size; // Added for US05
    private LocalDateTime uploadDate; // Added for US05
    private LocalDateTime expirationDate;
    private String shareToken; // The unique token for the first share link
    private Integer downloadCount; // Added for download count feature

    public FileResponse(Long id, String originalName, Long size, LocalDateTime uploadDate, LocalDateTime expirationDate,
            String shareToken, Integer downloadCount) {
        this.id = id;
        this.originalName = originalName;
        this.size = size;
        this.uploadDate = uploadDate;
        this.expirationDate = expirationDate;
        this.shareToken = shareToken;
        this.downloadCount = downloadCount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }
}
