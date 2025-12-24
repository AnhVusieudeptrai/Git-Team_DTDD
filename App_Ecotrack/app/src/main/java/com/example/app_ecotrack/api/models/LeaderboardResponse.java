package com.example.app_ecotrack.api.models;

import java.util.List;

public class LeaderboardResponse {
    public List<LeaderboardUser> leaderboard;
    public CurrentUserRank currentUser;
}

class LeaderboardUser {
    public int rank;
    public String id;
    public String username;
    public String fullname;
    public int points;
    public int level;
    public String avatar;
    public boolean isCurrentUser;
}

class CurrentUserRank {
    public int rank;
    public int points;
    public int level;
}
