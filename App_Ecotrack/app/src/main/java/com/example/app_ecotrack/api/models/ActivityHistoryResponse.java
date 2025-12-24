package com.example.app_ecotrack.api.models;

import java.util.List;

public class ActivityHistoryResponse {
    public List<HistoryItem> history;
    public Pagination pagination;
}

class HistoryItem {
    public String id;
    public ActivityData activity;
    public int pointsEarned;
    public String completedAt;
}

class Pagination {
    public int page;
    public int limit;
    public int total;
    public int pages;
}
