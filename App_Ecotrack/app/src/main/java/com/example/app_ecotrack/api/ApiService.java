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

    @POST("api/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("api/auth/verify-reset-token")
    Call<MessageResponse> verifyResetToken(@Body VerifyResetTokenRequest request);

    @POST("api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);

    @POST("api/auth/change-password")
    Call<MessageResponse> changePassword(@Body ChangePasswordRequest request);

    // ==================== ACTIVITIES ====================
    
    @GET("api/activities")
    Call<ActivitiesResponse> getActivities();

    @POST("api/activities/{id}/complete")
    Call<CompleteActivityResponse> completeActivity(@Path("id") String activityId);

    @GET("api/activities/today")
    Call<TodayActivitiesResponse> getTodayActivities();

    @GET("api/activities/history")
    Call<ActivityHistoryResponse> getActivityHistory(@Query("page") int page, @Query("limit") int limit);

    // ==================== BADGES ====================

    @GET("api/badges")
    Call<BadgesResponse> getBadges();

    @GET("api/badges/my")
    Call<MyBadgesResponse> getMyBadges();

    // ==================== CHALLENGES ====================

    @GET("api/challenges")
    Call<ChallengesResponse> getChallenges();

    @POST("api/challenges/{id}/join")
    Call<JoinChallengeResponse> joinChallenge(@Path("id") String challengeId);

    @GET("api/challenges/my")
    Call<MyChallengesResponse> getMyChallenges();

    // ==================== STREAKS ====================

    @GET("api/streaks")
    Call<StreakResponse> getStreak();

    @GET("api/streaks/leaderboard")
    Call<StreakLeaderboardResponse> getStreakLeaderboard();

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

    // ==================== FCM TOKEN ====================
    
    @POST("api/users/fcm-token")
    Call<MessageResponse> registerFcmToken(@Body FcmTokenRequest request);

}
