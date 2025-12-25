package com.example.app_ecotrack.api.models;

public class ChallengeData {
    public String id;
    public String name;
    public String description;
    public String type; // weekly, monthly
    public String targetType; // points, activities
    public int targetValue;
    public String targetCategory;
    public int rewardPoints;
    public BadgeData rewardBadge;
    public String startDate;
    public String endDate;
    public boolean joined;
    public int progress;
    public int progressPercent;
    public boolean isCompleted;
    public long timeRemainingMs;
}
