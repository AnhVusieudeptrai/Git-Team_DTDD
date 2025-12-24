package com.example.app_ecotrack.api;

import com.example.app_ecotrack.api.models.*;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // ==================== AUTH ====================
    
    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @GET("api/auth/me")
    Call<UserResponse> getCurrentUser();

    // ==================== ACTIVITIES ====================
    
    @GET("api/activities")
    Call<ActivitiesResponse> getActivities();

    @POST("api/activities/{id}/complete")
    Call<CompleteActivityResponse> completeActivity(@Path("id") String activityId);

    @GET("api/activities/today")
    Call<TodayActivitiesResponse> getTodayActivities();

    @GET("api/activities/history")
    Call<ActivityHistoryResponse> getActivityHistory(@Query("page") int page, @Query("limit") int limit);

    // ==================== USERS ====================
    
    @GET("api/users/profile")
    Call<ProfileResponse> getProfile();

    @PUT("api/users/profile")
    Call<ProfileResponse> updateProfile(@Body UpdateProfileRequest request);

    @GET("api/users/stats")
    Call<StatsResponse> getStats();

    // ==================== LEADERBOARD ====================
    
    @GET("api/leaderboard")
    Call<LeaderboardResponse> getLeaderboard(@Query("limit") int limit);

    @GET("api/leaderboard/weekly")
    Call<WeeklyLeaderboardResponse> getWeeklyLeaderboard();
}
