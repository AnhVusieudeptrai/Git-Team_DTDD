package com.example.app_ecotrack.api.models;

public class ResetPasswordRequest {
    public String email;
    public String token;
    public String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String email, String token, String newPassword) {
        this.email = email;
        this.token = token;
        this.newPassword = newPassword;
    }
}
