package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.ActivityHistoryResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * HistoryViewModel - ViewModel cho màn hình lịch sử hoạt động
 * Quản lý dữ liệu lịch sử với pagination
 * 
 * Requirements: 10.1, 10.3
 */
public class HistoryViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // Pagination constants
    private static final int PAGE_SIZE = 20;

    // Pagination state
    private int currentPage = 1;
    private int totalItems = 0;
    private boolean hasMore = true;

    // LiveData cho UI
    private final MutableLiveData<List<ActivityHistoryResponse.HistoryItem>> historyItems = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityHistoryResponse.HistoryItem>> newHistoryItems = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoadingMore = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // All loaded items
    private final List<ActivityHistoryResponse.HistoryItem> allItems = new ArrayList<>();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<List<ActivityHistoryResponse.HistoryItem>> getHistoryItems() { return historyItems; }
    public LiveData<List<ActivityHistoryResponse.HistoryItem>> getNewHistoryItems() { return newHistoryItems; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Boolean> getIsLoadingMore() { return isLoadingMore; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * Check if there's more data to load
     */
    public boolean hasMoreData() {
        return hasMore;
    }

    /**
     * Load history data from API
     * @param refresh true to refresh from page 1, false to continue pagination
     */
    public void loadHistory(boolean refresh) {
        if (refresh) {
            currentPage = 1;
            hasMore = true;
            allItems.clear();
        }

        if (Boolean.TRUE.equals(isLoading.getValue()) || Boolean.TRUE.equals(isLoadingMore.getValue())) {
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        apiService.getActivityHistory(currentPage, PAGE_SIZE).enqueue(new Callback<ActivityHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityHistoryResponse> call, @NonNull Response<ActivityHistoryResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ActivityHistoryResponse historyResponse = response.body();
                    
                    // Update pagination state
                    totalItems = historyResponse.total;
                    
                    if (historyResponse.activities != null) {
                        allItems.addAll(historyResponse.activities);
                        historyItems.setValue(new ArrayList<>(allItems));
                        
                        // Check if there's more data
                        hasMore = allItems.size() < totalItems;
                    } else {
                        historyItems.setValue(new ArrayList<>());
                        hasMore = false;
                    }
                } else {
                    errorMessage.setValue("Không thể tải lịch sử hoạt động");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityHistoryResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Load more history items (pagination)
     */
    public void loadMoreHistory() {
        if (!hasMore || Boolean.TRUE.equals(isLoading.getValue()) || Boolean.TRUE.equals(isLoadingMore.getValue())) {
            return;
        }

        currentPage++;
        isLoadingMore.setValue(true);

        apiService.getActivityHistory(currentPage, PAGE_SIZE).enqueue(new Callback<ActivityHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<ActivityHistoryResponse> call, @NonNull Response<ActivityHistoryResponse> response) {
                isLoadingMore.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    ActivityHistoryResponse historyResponse = response.body();
                    
                    // Update pagination state
                    totalItems = historyResponse.total;
                    
                    if (historyResponse.activities != null && !historyResponse.activities.isEmpty()) {
                        allItems.addAll(historyResponse.activities);
                        newHistoryItems.setValue(historyResponse.activities);
                        
                        // Check if there's more data
                        hasMore = allItems.size() < totalItems;
                    } else {
                        hasMore = false;
                    }
                } else {
                    // Revert page on error
                    currentPage--;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ActivityHistoryResponse> call, @NonNull Throwable t) {
                isLoadingMore.setValue(false);
                // Revert page on error
                currentPage--;
            }
        });
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
