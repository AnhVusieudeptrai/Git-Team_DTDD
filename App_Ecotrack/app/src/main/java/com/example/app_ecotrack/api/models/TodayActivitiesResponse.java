package com.example.app_ecotrack.api.models;

import java.util.List;

public class TodayActivitiesResponse {
    public List<UserActivityItem> activities;
    public int count;
    public int totalPoints;
    public int todayPoints;
    public int todayCount;
    public int weekPoints;

    public static class UserActivityItem {
        public String id;
        public ActivityData activity;
        public int pointsEarned;
        public String completedAt;
    }
}
