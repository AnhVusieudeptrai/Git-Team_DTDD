package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.BadgeData;
import com.example.app_ecotrack.api.models.BadgesResponse;
import com.example.app_ecotrack.api.models.MyBadgesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * BadgesViewModel - ViewModel cho màn hình huy hiệu
 * Quản lý dữ liệu badges với filter theo type và earned status
 */
public class BadgesViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData cho UI
    private final MutableLiveData<List<BadgeData>> allBadges = new MutableLiveData<>();
    private final MutableLiveData<List<BadgeData>> filteredBadges = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<BadgeStats> badgeStats = new MutableLiveData<>();

    // Current filter
    private String currentFilter = FILTER_ALL;

    // Filter constants
    public static final String FILTER_ALL = "all";
    public static final String FILTER_STREAK = "streak";
    public static final String FILTER_POINTS = "points";
    public static final String FILTER_ACTIVITIES = "activities";

    public BadgesViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<List<BadgeData>> getFilteredBadges() { return filteredBadges; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<BadgeStats> getBadgeStats() { return badgeStats; }

    /**
     * Load all badges with earned status
     */
    public void loadBadges() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Load all badges first
        apiService.getBadges().enqueue(new Callback<BadgesResponse>() {
            @Override
            public void onResponse(@NonNull Call<BadgesResponse> call, @NonNull Response<BadgesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<BadgeData> badges = response.body().badges;
                    // Now load user's earned badges to merge status
                    loadMyBadgesAndMerge(badges);
                } else {
                    isLoading.setValue(false);
                    errorMessage.setValue("Không thể tải danh sách huy hiệu");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BadgesResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Load user's earned badges and merge with all badges
     */
    private void loadMyBadgesAndMerge(List<BadgeData> allBadgesList) {
        apiService.getMyBadges().enqueue(new Callback<MyBadgesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyBadgesResponse> call, @NonNull Response<MyBadgesResponse> response) {
                isLoading.setValue(false);
                
                List<BadgeData> mergedBadges = new ArrayList<>();
                int earnedCount = 0;

                if (response.isSuccessful() && response.body() != null) {
                    List<BadgeData> myBadges = response.body().badges;
                    
                    // Create a map of earned badges for quick lookup
                    java.util.Map<String, BadgeData> earnedMap = new java.util.HashMap<>();
                    if (myBadges != null) {
                        for (BadgeData badge : myBadges) {
                            earnedMap.put(badge.id, badge);
                        }
                    }

                    // Merge earned status into all badges
                    for (BadgeData badge : allBadgesList) {
                        BadgeData earnedBadge = earnedMap.get(badge.id);
                        if (earnedBadge != null) {
                            badge.earned = true;
                            badge.earnedAt = earnedBadge.earnedAt;
                            badge.progress = badge.requirement;
                            badge.progressPercent = 100;
                            earnedCount++;
                        } else {
                            badge.earned = false;
                            // Progress should come from API, but ensure it's set
                            if (badge.progressPercent == 0 && badge.progress > 0 && badge.requirement > 0) {
                                badge.progressPercent = Math.min(100, (badge.progress * 100) / badge.requirement);
                            }
                        }
                        mergedBadges.add(badge);
                    }
                } else {
                    // If my badges fails, just use all badges without earned status
                    mergedBadges.addAll(allBadgesList);
                }

                // Update stats
                BadgeStats stats = new BadgeStats();
                stats.totalBadges = mergedBadges.size();
                stats.earnedBadges = earnedCount;
                badgeStats.setValue(stats);

                // Store all badges and apply filter
                allBadges.setValue(mergedBadges);
                applyFilter(currentFilter);
            }

            @Override
            public void onFailure(@NonNull Call<MyBadgesResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                // If my badges fails, just use all badges without earned status
                allBadges.setValue(allBadgesList);
                
                BadgeStats stats = new BadgeStats();
                stats.totalBadges = allBadgesList.size();
                stats.earnedBadges = 0;
                badgeStats.setValue(stats);
                
                applyFilter(currentFilter);
            }
        });
    }

    /**
     * Apply filter to badges list
     */
    public void applyFilter(String filterType) {
        currentFilter = filterType;
        List<BadgeData> badges = allBadges.getValue();
        
        if (badges == null || badges.isEmpty()) {
            filteredBadges.setValue(new ArrayList<>());
            return;
        }

        if (FILTER_ALL.equals(filterType)) {
            filteredBadges.setValue(badges);
            return;
        }

        List<BadgeData> filtered = new ArrayList<>();
        for (BadgeData badge : badges) {
            if (badge.type != null && badge.type.equals(filterType)) {
                filtered.add(badge);
            }
        }
        filteredBadges.setValue(filtered);
    }

    /**
     * Get current filter
     */
    public String getCurrentFilter() {
        return currentFilter;
    }

    /**
     * Badge statistics data class
     */
    public static class BadgeStats {
        public int totalBadges;
        public int earnedBadges;
    }
}
