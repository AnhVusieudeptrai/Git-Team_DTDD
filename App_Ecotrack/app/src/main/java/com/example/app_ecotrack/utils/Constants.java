package com.example.app_ecotrack.utils;

/**
 * Application constants
 */
public final class Constants {
    
    private Constants() {
        // Prevent instantiation
    }
    
    // API Configuration
    public static final String BASE_URL = "https://ecotrack-api.example.com/";
    
    // SharedPreferences Keys
    public static final String PREF_NAME = "ecotrack_prefs";
    public static final String PREF_TOKEN = "jwt_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USERNAME = "username";
    public static final String PREF_FCM_TOKEN = "fcm_token";
    
    // CO2 Factors (kg CO2 saved per 10 points)
    public static final double CO2_FACTOR_TRANSPORT = 0.5;
    public static final double CO2_FACTOR_ENERGY = 0.3;
    public static final double CO2_FACTOR_WATER = 0.1;
    public static final double CO2_FACTOR_WASTE = 0.2;
    public static final double CO2_FACTOR_GREEN = 0.4;
    public static final double CO2_FACTOR_CONSUMPTION = 0.15;
    public static final double CO2_FACTOR_DEFAULT = 0.1;
    
    // CO2 Equivalents
    public static final double CO2_PER_TREE_KG = 21.77;
    public static final double CO2_PER_KM_DRIVEN = 0.21;
    public static final double CO2_PER_KWH = 0.5;
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
    
    // Animation Durations
    public static final int ANIMATION_DURATION_SHORT = 200;
    public static final int ANIMATION_DURATION_MEDIUM = 300;
    public static final int ANIMATION_DURATION_LONG = 500;
    
    // Badge Rarity
    public static final String RARITY_COMMON = "common";
    public static final String RARITY_RARE = "rare";
    public static final String RARITY_EPIC = "epic";
    public static final String RARITY_LEGENDARY = "legendary";
    
    // Activity Categories
    public static final String CATEGORY_TRANSPORT = "transport";
    public static final String CATEGORY_ENERGY = "energy";
    public static final String CATEGORY_WATER = "water";
    public static final String CATEGORY_WASTE = "waste";
    public static final String CATEGORY_GREEN = "green";
    public static final String CATEGORY_CONSUMPTION = "consumption";
}
