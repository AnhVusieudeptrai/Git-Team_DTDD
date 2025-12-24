package com.example.app_ecotrack.api.models;

public class ProfileResponse {
    public UserData user;
    public ProfileStats stats;
}

class ProfileStats {
    public int totalActivities;
    public int rank;
}
