package com.example.app_ecotrack.api.models;

import java.util.List;

public class StatsResponse {
    public TodayStats today;
    public WeekStats week;
    public TotalStats total;
    public List<CategoryStat> categories;
    public List<WeeklyChartItem> weeklyChart;

    public static class TodayStats {
        public int points;
        public int activities;
    }

    public static class WeekStats {
        public int points;
        public int activities;
    }

    public static class TotalStats {
        public int points;
        public int activities;
        public int level;
        public int rank;
    }

    public static class CategoryStat {
        public String _id; // category name
        public int count;
        public int points;
    }

    public static class WeeklyChartItem {
        public String date;
        public int points;
        public int count;
    }
}
