package com.example.app_ecotrack.api.models;

import java.util.List;

public class WeeklyLeaderboardResponse {
    public List<WeeklyLeaderboardUser> leaderboard;
    public WeeklyCurrentUser currentUser;
}

class WeeklyLeaderboardUser {
    public int rank;
    public String id;
    public String username;
    public String fullname;
    public int weeklyPoints;
    public int level;
    public String avatar;
    public boolean isCurrentUser;
}

class WeeklyCurrentUser {
    public int weeklyPoints;
}
