package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * HomeViewModel - ViewModel cho màn hình chính
 * Quản lý dữ liệu streak, activities, challenges và xử lý complete activity
 */
public class HomeViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData cho UI
    private final MutableLiveData<StreakResponse.StreakData> streak = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityData>> activities = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeData>> activeChallenges = new MutableLiveData<>();
    private final MutableLiveData<TodayStats> todayStats = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<CompleteActivityResult> activityCompleted = new MutableLiveData<>();
    private final MutableLiveData<BadgeData> newBadgeEarned = new MutableLiveData<>();
    private final MutableLiveData<ChallengeData> challengeCompleted = new MutableLiveData<>();

    // CO2 factors per category (kg CO2 per 10 points)
    private static final Map<String, Double> CO2_FACTORS = new HashMap<>();
    static {
        CO2_FACTORS.put("transport", 0.5);
        CO2_FACTORS.put("energy", 0.3);
        CO2_FACTORS.put("water", 0.1);
        CO2_FACTORS.put("waste", 0.2);
        CO2_FACTORS.put("green", 0.4);
        CO2_FACTORS.put("consumption", 0.15);
    }

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<StreakResponse.StreakData> getStreak() { return streak; }
    public LiveData<List<ActivityData>> getActivities() { return activities; }
    public LiveData<List<ChallengeData>> getActiveChallenges() { return activeChallenges; }
    public LiveData<TodayStats> getTodayStats() { return todayStats; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<CompleteActivityResult> getActivityCompleted() { return activityCompleted; }
    public LiveData<BadgeData> getNewBadgeEarned() { return newBadgeEarned; }
    public LiveData<ChallengeData> getChallengeCompleted() { return challengeCompleted; }

    /**
     * Load tất cả dữ liệu cho Home screen
     */
    public void loadHomeData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Load streak
        loadStreak();
        // Load activities
        loadActivities();
        // Load today activities for stats
        loadTodayActivities();
        // Load active challenges
        loadActiveChallenges();
    }

    /**
     * Load streak data
     */
    private void loadStreak() {
        apiService.getStreak().enqueue(new Callback<StreakResponse>() {
            @Override
            public void onResponse(@NonNull Call<StreakResponse> call, @NonNull Response<StreakResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    streak.setValue(response.body().streak);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StreakResponse> call, @NonNull Throwable t) {
                // Streak load failed, but don't block other data
            }
        });
    }

    /**
     * Load available activities
     */
    private void loadActivities() {
        apiService.getActivities().enqueue(new Callback<ActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivitiesResponse> call, @NonNull Response<ActivitiesResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    activities.setValue(response.body().activities);
                } else {
                    errorMessage.setValue("Không thể tải danh sách hoạt động");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivitiesResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Load today's activities for stats calculation
     */
    private void loadTodayActivities() {
        apiService.getTodayActivities().enqueue(new Callback<TodayActivitiesResponse>() {
            @Override
            public void onResponse(@NonNull Call<TodayActivitiesResponse> call, @NonNull Response<TodayActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TodayActivitiesResponse data = response.body();
                    
                    // Calculate CO2 saved from today's activities
                    double co2Saved = 0;
                    if (data.activities != null) {
                        for (TodayActivitiesResponse.UserActivityItem item : data.activities) {
                            if (item.activity != null) {
                                co2Saved += calculateCO2(item.activity.category, item.pointsEarned);
                            }
                        }
                    }
                    
                    TodayStats stats = new TodayStats();
                    stats.todayPoints = data.todayPoints;
                    stats.todayCount = data.todayCount;
                    stats.co2Saved = co2Saved;
                    todayStats.setValue(stats);
                }
            }

            @Override
            public void onFailure(@NonNull Call<TodayActivitiesResponse> call, @NonNull Throwable t) {
                // Stats load failed, but don't block other data
            }
        });
    }

    /**
     * Load active challenges user has joined
     */
    private void loadActiveChallenges() {
        apiService.getMyChallenges().enqueue(new Callback<MyChallengesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyChallengesResponse> call, @NonNull Response<MyChallengesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activeChallenges.setValue(response.body().active);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyChallengesResponse> call, @NonNull Throwable t) {
                // Challenges load failed, but don't block other data
            }
        });
    }

    /**
     * Complete an activity
     */
    public void completeActivity(String activityId) {
        apiService.completeActivity(activityId).enqueue(new Callback<CompleteActivityResponse>() {
            @Override
            public void onResponse(@NonNull Call<CompleteActivityResponse> call, @NonNull Response<CompleteActivityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CompleteActivityResponse data = response.body();
                    
                    CompleteActivityResult result = new CompleteActivityResult();
                    result.activityId = activityId;
                    result.pointsEarned = data.pointsEarned;
                    result.totalPoints = data.totalPoints;
                    result.level = data.level;
                    result.success = true;
                    activityCompleted.setValue(result);
                    
                    // Update activity as completed in the list
                    updateActivityCompleted(activityId);
                    
                    // Reload data to get updated stats
                    loadTodayActivities();
                    loadStreak();
                    loadActiveChallenges();
                } else {
                    CompleteActivityResult result = new CompleteActivityResult();
                    result.activityId = activityId;
                    result.success = false;
                    result.errorMessage = "Không thể hoàn thành hoạt động";
                    activityCompleted.setValue(result);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CompleteActivityResponse> call, @NonNull Throwable t) {
                CompleteActivityResult result = new CompleteActivityResult();
                result.activityId = activityId;
                result.success = false;
                result.errorMessage = "Lỗi kết nối: " + t.getMessage();
                activityCompleted.setValue(result);
            }
        });
    }

    /**
     * Update activity as completed in the local list
     */
    private void updateActivityCompleted(String activityId) {
        List<ActivityData> currentActivities = activities.getValue();
        if (currentActivities != null) {
            List<ActivityData> updatedList = new ArrayList<>();
            for (ActivityData activity : currentActivities) {
                if (activity.id.equals(activityId)) {
                    activity.completedToday = true;
                }
                updatedList.add(activity);
            }
            activities.setValue(updatedList);
        }
    }

    /**
     * Calculate CO2 saved for an activity
     */
    public static double calculateCO2(String category, int points) {
        Double factor = CO2_FACTORS.get(category);
        if (factor == null) {
            factor = 0.1; // Default factor
        }
        return points * factor / 10.0;
    }

    /**
     * Clear activity completed event (after handling)
     */
    public void clearActivityCompletedEvent() {
        activityCompleted.setValue(null);
    }

    /**
     * Clear new badge event (after handling)
     */
    public void clearNewBadgeEvent() {
        newBadgeEarned.setValue(null);
    }

    /**
     * Clear challenge completed event (after handling)
     */
    public void clearChallengeCompletedEvent() {
        challengeCompleted.setValue(null);
    }

    /**
     * Today stats data class
     */
    public static class TodayStats {
        public int todayPoints;
        public int todayCount;
        public double co2Saved;
    }

    /**
     * Complete activity result data class
     */
    public static class CompleteActivityResult {
        public String activityId;
        public int pointsEarned;
        public int totalPoints;
        public int level;
        public boolean success;
        public String errorMessage;
    }
}
