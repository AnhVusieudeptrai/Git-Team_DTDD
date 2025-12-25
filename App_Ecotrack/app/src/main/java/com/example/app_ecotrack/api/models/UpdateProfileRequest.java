package com.example.app_ecotrack.api.models;

public class UpdateProfileRequest {
    public String fullname;
    public String avatar;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String fullname, String avatar) {
        this.fullname = fullname;
        this.avatar = avatar;
    }
}
