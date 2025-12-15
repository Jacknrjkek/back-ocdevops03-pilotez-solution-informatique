package com.datashare.backend.payload.response;

/**
 * DTO contenant le token JWT généré et les infos utilisateur.
 * Renvoyé après une connexion réussie.
 */
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String username;

    public JwtResponse(String accessToken, Long id, String email) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.username = email;
    }

    public String getAccessToken() {
        return token;
    }

    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }

    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
