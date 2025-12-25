package com.example.app_ecotrack.api.models;

import java.util.List;

public class ActivityHistoryResponse {
    public List<HistoryItem> activities;
    public int total;
    public int page;
    public int limit;

    public static class HistoryItem {
        public String id;
        public ActivityData activity;
        public int pointsEarned;
        public String completedAt;
    }
}
