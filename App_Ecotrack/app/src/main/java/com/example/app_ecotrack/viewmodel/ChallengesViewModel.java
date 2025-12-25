package com.example.app_ecotrack.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.ApiService;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.example.app_ecotrack.api.models.ChallengesResponse;
import com.example.app_ecotrack.api.models.JoinChallengeResponse;
import com.example.app_ecotrack.api.models.MyChallengesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ChallengesViewModel - ViewModel cho màn hình thử thách
 * Quản lý dữ liệu challenges với 3 tabs: active, joined, completed
 */
public class ChallengesViewModel extends AndroidViewModel {

    private final ApiService apiService;

    // LiveData cho UI
    private final MutableLiveData<List<ChallengeData>> activeChallenges = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeData>> joinedChallenges = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeData>> completedChallenges = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<ChallengeData> joinedChallenge = new MutableLiveData<>();

    public ChallengesViewModel(@NonNull Application application) {
        super(application);
        ApiClient.init(application);
        apiService = ApiClient.getApiServiceStatic();
    }

    // Getters for LiveData
    public LiveData<List<ChallengeData>> getActiveChallenges() { return activeChallenges; }
    public LiveData<List<ChallengeData>> getJoinedChallenges() { return joinedChallenges; }
    public LiveData<List<ChallengeData>> getCompletedChallenges() { return completedChallenges; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<ChallengeData> getJoinedChallenge() { return joinedChallenge; }

    /**
     * Load all challenges data
     */
    public void loadAllChallenges() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Load active challenges first
        loadActiveChallenges();
    }

    /**
     * Load active (available) challenges
     */
    private void loadActiveChallenges() {
        apiService.getChallenges().enqueue(new Callback<ChallengesResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChallengesResponse> call, 
                                   @NonNull Response<ChallengesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ChallengeData> challenges = response.body().challenges;
                    if (challenges != null) {
                        // Filter out already joined challenges for active tab
                        List<ChallengeData> available = new ArrayList<>();
                        for (ChallengeData challenge : challenges) {
                            if (!challenge.joined && !challenge.isCompleted) {
                                available.add(challenge);
                            }
                        }
                        activeChallenges.setValue(available);
                    } else {
                        activeChallenges.setValue(new ArrayList<>());
                    }
                } else {
                    activeChallenges.setValue(new ArrayList<>());
                }
                // Continue loading my challenges
                loadMyChallenges();
            }

            @Override
            public void onFailure(@NonNull Call<ChallengesResponse> call, @NonNull Throwable t) {
                activeChallenges.setValue(new ArrayList<>());
                // Continue loading my challenges even if this fails
                loadMyChallenges();
            }
        });
    }

    /**
     * Load user's joined and completed challenges
     */
    private void loadMyChallenges() {
        apiService.getMyChallenges().enqueue(new Callback<MyChallengesResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyChallengesResponse> call, 
                                   @NonNull Response<MyChallengesResponse> response) {
                isLoading.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    MyChallengesResponse data = response.body();
                    
                    // Set joined challenges (active ones user has joined)
                    if (data.active != null) {
                        joinedChallenges.setValue(data.active);
                    } else {
                        joinedChallenges.setValue(new ArrayList<>());
                    }
                    
                    // Set completed challenges
                    if (data.completed != null) {
                        completedChallenges.setValue(data.completed);
                    } else {
                        completedChallenges.setValue(new ArrayList<>());
                    }
                } else {
                    joinedChallenges.setValue(new ArrayList<>());
                    completedChallenges.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyChallengesResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                joinedChallenges.setValue(new ArrayList<>());
                completedChallenges.setValue(new ArrayList<>());
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Join a challenge
     */
    public void joinChallenge(String challengeId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        apiService.joinChallenge(challengeId).enqueue(new Callback<JoinChallengeResponse>() {
            @Override
            public void onResponse(@NonNull Call<JoinChallengeResponse> call, 
                                   @NonNull Response<JoinChallengeResponse> response) {
                isLoading.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    JoinChallengeResponse data = response.body();
                    successMessage.setValue(data.message != null ? data.message : "Đã tham gia thử thách!");
                    
                    // Notify about joined challenge
                    if (data.challenge != null) {
                        joinedChallenge.setValue(data.challenge);
                    }
                    
                    // Reload all challenges to update lists
                    loadAllChallenges();
                } else {
                    errorMessage.setValue("Không thể tham gia thử thách. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JoinChallengeResponse> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * Refresh challenges data
     */
    public void refresh() {
        loadAllChallenges();
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Clear success message
     */
    public void clearSuccess() {
        successMessage.setValue(null);
    }

    /**
     * Clear joined challenge notification
     */
    public void clearJoinedChallenge() {
        joinedChallenge.setValue(null);
    }
}
