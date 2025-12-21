package com.example.app_ecotrack;

public class User {
    private int id;
    private String username;
    private String fullname;
    private String email;
    private String role;
    private int points;
    private int level;
    private String createdAt;

    // Constructor rỗng
    public User() {
    }

    // Constructor đầy đủ
    public User(int id, String username, String fullname, String email, String role, int points, int level, String createdAt) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.points = points;
        this.level = level;
        this.createdAt = createdAt;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public int getPoints() {
        return points;
    }

    public int getLevel() {
        return level;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}