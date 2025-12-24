package com.example.app_ecotrack.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatsResponse {
    public DayStats today;
    public DayStats week;
    public TotalStats total;
    public List<CategoryStat> categories;
    public List<WeeklyChartData> weeklyChart;
}

class DayStats {
    public int points;
    public int activities;
}

class TotalStats {
    public int points;
    public int activities;
    public int level;
}

class CategoryStat {
    @SerializedName("_id")
    public String category;
    public int count;
    public int points;
}

class WeeklyChartData {
    public String date;
    public int points;
    public int count;
}
