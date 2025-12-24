package com.example.app_ecotrack.api.models;

import java.util.List;

public class TodayActivitiesResponse {
    public List<UserActivityData> activities;
    public int count;
    public int totalPoints;
}

class UserActivityData {
    public String id;
    public ActivityData activity;
    public int pointsEarned;
    public String completedAt;
}
