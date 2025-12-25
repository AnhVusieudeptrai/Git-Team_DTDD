package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.StatsResponse;
import com.example.app_ecotrack.utils.CO2Calculator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * StatsViewModel - ViewModel cho màn hình thống kê
 * Quản lý dữ liệu thống kê và báo cáo CO2
 * 
 * Requirements: 7.1-7.6
 */
public class StatsViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData cho UI
    private final MutableLiveData<StatsResponse> stats = new MutableLiveData<>();
    private final MutableLiveData<CO2Calculator.CO2Report> co2Report = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedPeriod = new MutableLiveData<>(0); // 0=today, 1=week, 2=total

    public StatsViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<StatsResponse> getStats() { return stats; }
    public LiveData<CO2Calculator.CO2Report> getCO2Report() { return co2Report; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getSelectedPeriod() { return selectedPeriod; }

    /**
     * Load stats data from API
     */
    public void loadStats() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        apiService.getStats().enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatsResponse> call, @NonNull Response<StatsResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    StatsResponse statsData = response.body();
                    stats.setValue(statsData);
                    
                    // Calculate CO2 report from categories
                    calculateCO2Report(statsData.categories);
                } else {
                    errorMessage.setValue("Không thể tải thống kê");
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatsResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Calculate CO2 report from category statistics
     */
    private void calculateCO2Report(List<StatsResponse.CategoryStat> categories) {
        double totalCO2 = CO2Calculator.calculateTotalCO2FromCategories(categories);
        CO2Calculator.CO2Report report = CO2Calculator.generateReport(totalCO2);
        co2Report.setValue(report);
    }

    /**
     * Set selected period tab
     * @param period 0=today, 1=week, 2=total
     */
    public void setSelectedPeriod(int period) {
        selectedPeriod.setValue(period);
    }

    /**
     * Get points for the selected period
     */
    public int getPointsForPeriod(StatsResponse statsData, int period) {
        if (statsData == null) return 0;
        
        switch (period) {
            case 0: // Today
                return statsData.today != null ? statsData.today.points : 0;
            case 1: // Week
                return statsData.week != null ? statsData.week.points : 0;
            case 2: // Total
                return statsData.total != null ? statsData.total.points : 0;
            default:
                return 0;
        }
    }

    /**
     * Get activities count for the selected period
     */
    public int getActivitiesForPeriod(StatsResponse statsData, int period) {
        if (statsData == null) return 0;
        
        switch (period) {
            case 0: // Today
                return statsData.today != null ? statsData.today.activities : 0;
            case 1: // Week
                return statsData.week != null ? statsData.week.activities : 0;
            case 2: // Total
                return statsData.total != null ? statsData.total.activities : 0;
            default:
                return 0;
        }
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }
}
