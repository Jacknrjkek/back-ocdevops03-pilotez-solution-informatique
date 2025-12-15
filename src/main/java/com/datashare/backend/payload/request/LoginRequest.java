package com.datashare.backend.payload.request;

/**
 * DTO (Data Transfer Object) pour la requête de connexion.
 * Reçoit l'email et le mot de passe du client.
 */
public class LoginRequest {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
