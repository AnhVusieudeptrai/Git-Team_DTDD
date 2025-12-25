package com.example.app_ecotrack.api.models;

public class StreakResponse {
    public StreakData streak;

    public static class StreakData {
        public int currentStreak;
        public int longestStreak;
        public String lastActivityDate;
        public boolean isActive;
        public int daysUntilLost;
    }
}
