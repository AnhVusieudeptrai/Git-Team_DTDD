package com.example.app_ecotrack;


public class Activity {
    private int id;
    private String apiId; // ID từ API (MongoDB ObjectId)
    private String name;
    private String description;
    private int points;
    private String category;
    private String icon;
    private String createdAt;
    private boolean completed;

    // Constructor rỗng
    public Activity() {
    }

    // Constructor đầy đủ
    public Activity(int id, String name, String description, int points, String category, String icon) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.points = points;
        this.category = category;
        this.icon = icon;
        this.completed = false;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getApiId() {
        return apiId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getPoints() {
        return points;
    }

    public String getCategory() {
        return category;
    }

    public String getIcon() {
        return icon;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}