package com.example.app_ecotrack.api.models;

public class BadgeData {
    public String id;
    public String name;
    public String description;
    public String icon;
    public String type; // streak, points, activities
    public int requirement;
    public String rarity; // common, rare, epic, legendary
    public boolean earned;
    public String earnedAt;
    public int progress;
    public int progressPercent;
}
