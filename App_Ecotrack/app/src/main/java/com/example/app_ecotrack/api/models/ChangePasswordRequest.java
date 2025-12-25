package com.example.app_ecotrack.api.models;

public class ChangePasswordRequest {
    public String oldPassword;
    public String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
