package com.example.app_ecotrack.api.models;

import java.util.List;

public class StreakLeaderboardResponse {
    public List<StreakLeaderboardUser> leaderboard;
    public CurrentUserStreak currentUser;

    public static class StreakLeaderboardUser {
        public int rank;
        public String id;
        public String username;
        public String fullname;
        public int currentStreak;
        public int longestStreak;
        public String avatar;
        public boolean isCurrentUser;
    }

    public static class CurrentUserStreak {
        public int rank;
        public int currentStreak;
        public int longestStreak;
    }
}
