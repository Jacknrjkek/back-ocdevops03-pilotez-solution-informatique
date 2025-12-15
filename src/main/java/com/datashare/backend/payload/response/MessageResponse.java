package com.datashare.backend.payload.response;

/**
 * DTO simple pour retourner des messages textuels (Succès, Erreur).
 */
public class MessageResponse {
    private String message;

    public MessageResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
