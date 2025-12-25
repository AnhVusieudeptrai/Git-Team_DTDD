package com.example.app_ecotrack.api.models;

public class VerifyResetTokenRequest {
    public String email;
    public String token;

    public VerifyResetTokenRequest() {
    }

    public VerifyResetTokenRequest(String email, String token) {
        this.email = email;
        this.token = token;
    }
}
