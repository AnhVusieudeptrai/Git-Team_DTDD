package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.*;
import com.example.app_ecotrack.utils.TokenManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileViewModel - ViewModel cho màn hình hồ sơ cá nhân
 * Quản lý dữ liệu profile, stats, badges và xử lý update profile, change password, logout
 */
public class ProfileViewModel extends AndroidViewModel {

    private final ApiService apiService;
    private final TokenManager tokenManager;

    // LiveData cho UI
    private final MutableLiveData<UserData> user = new MutableLiveData<>();
    private final MutableLiveData<ProfileStats> stats = new MutableLiveData<>();
    private final MutableLiveData<List<BadgeData>> earnedBadges = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBadges = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateProfileSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> changePasswordSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutEvent = new MutableLiveData<>();

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

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
        tokenManager = TokenManager.getInstance(application);
    }

    // Getters for LiveData
    public LiveData<UserData> getUser() { return user; }
    public LiveData<ProfileStats> getStats() { return stats; }
    public LiveData<List<BadgeData>> getEarnedBadges() { return earnedBadges; }
    public LiveData<Integer> getTotalBadges() { return totalBadges; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getUpdateProfileSuccess() { return updateProfileSuccess; }
    public LiveData<Boolean> getChangePasswordSuccess() { return changePasswordSuccess; }
    public LiveData<Boolean> getLogoutEvent() { return logoutEvent; }

    /**
     * Load tất cả dữ liệu cho Profile screen
     */
    public void loadProfileData() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Load profile
        loadProfile();
        // Load stats
        loadStats();
        // Load earned badges
        loadEarnedBadges();
    }

    /**
     * Load user profile
     */
    private void loadProfile() {
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse data = response.body();
                    user.setValue(data.user);
                    
                    // Set stats from profile response
                    if (data.stats != null) {
                        ProfileStats profileStats = new ProfileStats();
                        profileStats.totalActivities = data.stats.totalActivities;
                        profileStats.rank = data.stats.rank;
                        profileStats.co2Saved = calculateTotalCO2(data.user != null ? data.user.points : 0);
                        stats.setValue(profileStats);
                    }
                } else {
                    errorMessage.setValue("Không thể tải thông tin hồ sơ");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Load user stats
     */
    private void loadStats() {
        apiService.getStats().enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatsResponse> call, @NonNull Response<StatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatsResponse data = response.body();
                    
                    // Calculate CO2 from categories
                    double totalCO2 = 0;
                    if (data.categories != null) {
                        for (StatsResponse.CategoryStat cat : data.categories) {
                            totalCO2 += calculateCO2(cat._id, cat.points);
                        }
                    }
                    
                    ProfileStats profileStats = stats.getValue();
                    if (profileStats == null) {
                        profileStats = new ProfileStats();
                    }
                    
                    if (data.total != null) {
                        profileStats.totalActivities = data.total.activities;
                        profileStats.rank = data.total.rank;
                    }
                    profileStats.co2Saved = totalCO2;
                    stats.setValue(profileStats);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatsResponse> call, @NonNull Throwable t) {
                // Stats load failed, but don't block other data
            }
        });
    }

    /**
     * Load earned badges
     */
    private void loadEarnedBadges() {
        apiService.getMyBadges().enqueue(new Callback<MyBadgesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyBadgesResponse> call, @NonNull Response<MyBadgesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MyBadgesResponse data = response.body();
                    earnedBadges.setValue(data.badges);
                    totalBadges.setValue(data.totalEarned);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyBadgesResponse> call, @NonNull Throwable t) {
                // Badges load failed, but don't block other data
            }
        });
    }

    /**
     * Update user profile (fullname)
     */
    public void updateProfile(String fullname) {
        isLoading.setValue(true);
        
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.fullname = fullname;
        
        apiService.updateProfile(request).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    user.setValue(response.body().user);
                    updateProfileSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Cập nhật hồ sơ thất bại");
                    updateProfileSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
                updateProfileSuccess.setValue(false);
            }
        });
    }

    /**
     * Change password
     */
    public void changePassword(String oldPassword, String newPassword) {
        isLoading.setValue(true);
        
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.oldPassword = oldPassword;
        request.newPassword = newPassword;
        
        apiService.changePassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful()) {
                    changePasswordSuccess.setValue(true);
                } else {
                    errorMessage.setValue("Đổi mật khẩu thất bại. Vui lòng kiểm tra mật khẩu hiện tại.");
                    changePasswordSuccess.setValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
                changePasswordSuccess.setValue(false);
            }
        });
    }

    /**
     * Logout - clear token and trigger logout event
     */
    public void logout() {
        tokenManager.clearToken();
        logoutEvent.setValue(true);
    }

    /**
     * Calculate CO2 saved for an activity category
     */
    private double calculateCO2(String category, int points) {
        Double factor = CO2_FACTORS.get(category);
        if (factor == null) {
            factor = 0.1; // Default factor
        }
        return points * factor / 10.0;
    }

    /**
     * Calculate total CO2 from total points (rough estimate)
     */
    private double calculateTotalCO2(int totalPoints) {
        // Average factor across all categories
        return totalPoints * 0.25 / 10.0;
    }

    /**
     * Clear update profile success event
     */
    public void clearUpdateProfileEvent() {
        updateProfileSuccess.setValue(null);
    }

    /**
     * Clear change password success event
     */
    public void clearChangePasswordEvent() {
        changePasswordSuccess.setValue(null);
    }

    /**
     * Clear logout event
     */
    public void clearLogoutEvent() {
        logoutEvent.setValue(null);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Profile stats data class
     */
    public static class ProfileStats {
        public int totalActivities;
        public int rank;
        public double co2Saved;
    }
}
