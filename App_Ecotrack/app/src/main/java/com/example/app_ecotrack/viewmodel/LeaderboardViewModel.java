package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.LeaderboardResponse;
import com.example.app_ecotrack.api.models.StreakLeaderboardResponse;
import com.example.app_ecotrack.api.models.WeeklyLeaderboardResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * LeaderboardViewModel - ViewModel cho màn hình bảng xếp hạng
 * Quản lý dữ liệu leaderboard với 3 tabs: Tổng, Tuần này, Streak
 */
public class LeaderboardViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // Tab types
    public static final int TAB_TOTAL = 0;
    public static final int TAB_WEEKLY = 1;
    public static final int TAB_STREAK = 2;

    // LiveData cho UI - Global leaderboard
    private final MutableLiveData<List<LeaderboardItem>> globalLeaderboard = new MutableLiveData<>();
    private final MutableLiveData<CurrentUserRank> globalCurrentUser = new MutableLiveData<>();

    // LiveData cho UI - Weekly leaderboard
    private final MutableLiveData<List<LeaderboardItem>> weeklyLeaderboard = new MutableLiveData<>();
    private final MutableLiveData<CurrentUserRank> weeklyCurrentUser = new MutableLiveData<>();

    // LiveData cho UI - Streak leaderboard
    private final MutableLiveData<List<LeaderboardItem>> streakLeaderboard = new MutableLiveData<>();
    private final MutableLiveData<CurrentUserRank> streakCurrentUser = new MutableLiveData<>();

    // Loading and error states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Current selected tab
    private final MutableLiveData<Integer> currentTab = new MutableLiveData<>(TAB_TOTAL);

    public LeaderboardViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<List<LeaderboardItem>> getGlobalLeaderboard() { return globalLeaderboard; }
    public LiveData<CurrentUserRank> getGlobalCurrentUser() { return globalCurrentUser; }
    public LiveData<List<LeaderboardItem>> getWeeklyLeaderboard() { return weeklyLeaderboard; }
    public LiveData<CurrentUserRank> getWeeklyCurrentUser() { return weeklyCurrentUser; }
    public LiveData<List<LeaderboardItem>> getStreakLeaderboard() { return streakLeaderboard; }
    public LiveData<CurrentUserRank> getStreakCurrentUser() { return streakCurrentUser; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getCurrentTab() { return currentTab; }


    /**
     * Set current tab and load data if needed
     */
    public void setCurrentTab(int tab) {
        currentTab.setValue(tab);
        loadDataForTab(tab);
    }

    /**
     * Load data for specific tab
     */
    private void loadDataForTab(int tab) {
        switch (tab) {
            case TAB_TOTAL:
                if (globalLeaderboard.getValue() == null || globalLeaderboard.getValue().isEmpty()) {
                    loadGlobalLeaderboard();
                }
                break;
            case TAB_WEEKLY:
                if (weeklyLeaderboard.getValue() == null || weeklyLeaderboard.getValue().isEmpty()) {
                    loadWeeklyLeaderboard();
                }
                break;
            case TAB_STREAK:
                if (streakLeaderboard.getValue() == null || streakLeaderboard.getValue().isEmpty()) {
                    loadStreakLeaderboard();
                }
                break;
        }
    }

    /**
     * Load all leaderboard data
     */
    public void loadAllLeaderboards() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        loadGlobalLeaderboard();
    }

    /**
     * Load global (total points) leaderboard
     */
    public void loadGlobalLeaderboard() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        apiService.getLeaderboard(10).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<LeaderboardResponse> call,
                                   @NonNull Response<LeaderboardResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    LeaderboardResponse data = response.body();
                    
                    // Convert to LeaderboardItem list
                    List<LeaderboardItem> items = new ArrayList<>();
                    if (data.leaderboard != null) {
                        for (LeaderboardResponse.LeaderboardUser user : data.leaderboard) {
                            items.add(new LeaderboardItem(
                                user.rank,
                                user.id,
                                user.username,
                                user.fullname,
                                user.points,
                                user.level,
                                user.avatar,
                                user.isCurrentUser,
                                LeaderboardType.TOTAL
                            ));
                        }
                    }
                    globalLeaderboard.setValue(items);

                    // Set current user rank
                    if (data.currentUser != null) {
                        globalCurrentUser.setValue(new CurrentUserRank(
                            data.currentUser.rank,
                            data.currentUser.points,
                            data.currentUser.level,
                            LeaderboardType.TOTAL
                        ));
                    }
                } else {
                    globalLeaderboard.setValue(new ArrayList<>());
                    errorMessage.setValue("Không thể tải bảng xếp hạng");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LeaderboardResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                globalLeaderboard.setValue(new ArrayList<>());
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Load weekly leaderboard
     */
    public void loadWeeklyLeaderboard() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        apiService.getWeeklyLeaderboard().enqueue(new Callback<WeeklyLeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<WeeklyLeaderboardResponse> call,
                                   @NonNull Response<WeeklyLeaderboardResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    WeeklyLeaderboardResponse data = response.body();
                    
                    // Convert to LeaderboardItem list
                    List<LeaderboardItem> items = new ArrayList<>();
                    if (data.leaderboard != null) {
                        for (WeeklyLeaderboardResponse.WeeklyLeaderboardUser user : data.leaderboard) {
                            items.add(new LeaderboardItem(
                                user.rank,
                                user.id,
                                user.username,
                                user.fullname,
                                user.weeklyPoints,
                                user.level,
                                user.avatar,
                                user.isCurrentUser,
                                LeaderboardType.WEEKLY
                            ));
                        }
                    }
                    weeklyLeaderboard.setValue(items);

                    // Set current user rank
                    if (data.currentUser != null) {
                        weeklyCurrentUser.setValue(new CurrentUserRank(
                            data.currentUser.rank,
                            data.currentUser.weeklyPoints,
                            0,
                            LeaderboardType.WEEKLY
                        ));
                    }
                } else {
                    weeklyLeaderboard.setValue(new ArrayList<>());
                    errorMessage.setValue("Không thể tải bảng xếp hạng tuần");
                }
            }

            @Override
            public void onFailure(@NonNull Call<WeeklyLeaderboardResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                weeklyLeaderboard.setValue(new ArrayList<>());
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }


    /**
     * Load streak leaderboard
     */
    public void loadStreakLeaderboard() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        apiService.getStreakLeaderboard().enqueue(new Callback<StreakLeaderboardResponse>() {
            @Override
            public void onResponse(@NonNull Call<StreakLeaderboardResponse> call,
                                   @NonNull Response<StreakLeaderboardResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    StreakLeaderboardResponse data = response.body();
                    
                    // Convert to LeaderboardItem list
                    List<LeaderboardItem> items = new ArrayList<>();
                    if (data.leaderboard != null) {
                        for (StreakLeaderboardResponse.StreakLeaderboardUser user : data.leaderboard) {
                            items.add(new LeaderboardItem(
                                user.rank,
                                user.id,
                                user.username,
                                user.fullname,
                                user.currentStreak,
                                0, // No level for streak
                                user.avatar,
                                user.isCurrentUser,
                                LeaderboardType.STREAK
                            ));
                        }
                    }
                    streakLeaderboard.setValue(items);

                    // Set current user rank
                    if (data.currentUser != null) {
                        streakCurrentUser.setValue(new CurrentUserRank(
                            data.currentUser.rank,
                            data.currentUser.currentStreak,
                            0,
                            LeaderboardType.STREAK
                        ));
                    }
                } else {
                    streakLeaderboard.setValue(new ArrayList<>());
                    errorMessage.setValue("Không thể tải bảng xếp hạng streak");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StreakLeaderboardResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                streakLeaderboard.setValue(new ArrayList<>());
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Refresh current tab data
     */
    public void refresh() {
        Integer tab = currentTab.getValue();
        if (tab != null) {
            switch (tab) {
                case TAB_TOTAL:
                    loadGlobalLeaderboard();
                    break;
                case TAB_WEEKLY:
                    loadWeeklyLeaderboard();
                    break;
                case TAB_STREAK:
                    loadStreakLeaderboard();
                    break;
            }
        }
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // ==================== Data Classes ====================

    /**
     * Enum for leaderboard types
     */
    public enum LeaderboardType {
        TOTAL,
        WEEKLY,
        STREAK
    }

    /**
     * Unified leaderboard item for all tabs
     */
    public static class LeaderboardItem {
        public final int rank;
        public final String id;
        public final String username;
        public final String fullname;
        public final int value; // points or streak days
        public final int level;
        public final String avatar;
        public final boolean isCurrentUser;
        public final LeaderboardType type;

        public LeaderboardItem(int rank, String id, String username, String fullname,
                               int value, int level, String avatar, boolean isCurrentUser,
                               LeaderboardType type) {
            this.rank = rank;
            this.id = id;
            this.username = username;
            this.fullname = fullname;
            this.value = value;
            this.level = level;
            this.avatar = avatar;
            this.isCurrentUser = isCurrentUser;
            this.type = type;
        }

        public String getDisplayName() {
            return fullname != null && !fullname.isEmpty() ? fullname : username;
        }
    }

    /**
     * Current user rank info
     */
    public static class CurrentUserRank {
        public final int rank;
        public final int value; // points or streak days
        public final int level;
        public final LeaderboardType type;

        public CurrentUserRank(int rank, int value, int level, LeaderboardType type) {
            this.rank = rank;
            this.value = value;
            this.level = level;
            this.type = type;
        }
    }
}
