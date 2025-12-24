package com.example.app_ecotrack.api.models;

public class RegisterRequest {
    public String username;
    public String password;
    public String fullname;
    public String email;

    public RegisterRequest(String username, String password, String fullname, String email) {
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
    }
}
