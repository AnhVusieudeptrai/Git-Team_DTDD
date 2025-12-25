package com.example.app_ecotrack.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.ProfileResponse;
import com.example.app_ecotrack.api.models.StatsResponse;
import com.example.app_ecotrack.api.models.TodayActivitiesResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private TextView tvTodayPoints, tvWeekPoints, tvTotalPoints, tvTodayActivities, tvTotalActivities, tvRank;
    private CardView cardActivities, cardRewards, cardLeaderboard;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);

        initViews(view);
        loadDataFromPrefs();
        loadDataFromApi();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvTodayPoints = view.findViewById(R.id.tvTodayPoints);
        tvWeekPoints = view.findViewById(R.id.tvWeekPoints);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvTodayActivities = view.findViewById(R.id.tvTodayActivities);
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities);
        tvRank = view.findViewById(R.id.tvRank);

        cardActivities = view.findViewById(R.id.cardActivities);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
    }

    private void loadDataFromPrefs() {
        int totalPoints = getIntFromPrefs("points", 0);
        tvTotalPoints.setText(String.valueOf(totalPoints));
        tvTodayPoints.setText("0");
        tvWeekPoints.setText("0");
        tvTodayActivities.setText("0");
        tvTotalActivities.setText("0");
        tvRank.setText("#-");
    }

    private void loadDataFromApi() {
        // Load profile for total points, rank, total activities
        ApiClient.getApiService().getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    ProfileResponse profile = response.body();
                    tvTotalPoints.setText(String.valueOf(profile.user.points));
                    tvTotalActivities.setText(String.valueOf(profile.stats.totalActivities));
                    tvRank.setText("#" + profile.stats.rank);
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                // Use cached data
            }
        });

        // Load today's activities
        ApiClient.getApiService().getTodayActivities().enqueue(new Callback<TodayActivitiesResponse>() {
            @Override
            public void onResponse(Call<TodayActivitiesResponse> call, Response<TodayActivitiesResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    TodayActivitiesResponse today = response.body();
                    
                    // Handle both old and new API response formats
                    int todayPts = today.todayPoints > 0 ? today.todayPoints : today.totalPoints;
                    int todayCnt = today.todayCount > 0 ? today.todayCount : today.count;
                    
                    tvTodayPoints.setText(String.valueOf(todayPts));
                    tvTodayActivities.setText(String.valueOf(todayCnt));
                    
                    // If weekPoints is 0, load from stats API
                    if (today.weekPoints > 0) {
                        tvWeekPoints.setText(String.valueOf(today.weekPoints));
                    } else {
                        loadWeekPointsFromStats();
                    }
                } else if (isAdded()) {
                    // Try to get data from stats API as fallback
                    loadFromStatsApi();
                }
            }

            @Override
            public void onFailure(Call<TodayActivitiesResponse> call, Throwable t) {
                if (isAdded()) {
                    loadFromStatsApi();
                }
            }
        });
    }

    private void setupClickListeners() {
        cardActivities.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).getViewPager().setCurrentItem(1);
            }
        });

        cardLeaderboard.setOnClickListener(v -> {
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.app_ecotrack.LeaderboardActivity.class);
                startActivity(intent);
            }
        });

        cardRewards.setOnClickListener(v -> {
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.app_ecotrack.RewardsActivity.class);
                startActivity(intent);
            }
        });
    }

    private int getIntFromPrefs(String key, int defaultValue) {
        try {
            return prefs.getInt(key, defaultValue);
        } catch (ClassCastException e) {
            String str = prefs.getString(key, String.valueOf(defaultValue));
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ex) {
                return defaultValue;
            }
        }
    }

    private void loadFromStatsApi() {
        ApiClient.getApiService().getStats().enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    StatsResponse stats = response.body();
                    if (stats.today != null) {
                        tvTodayPoints.setText(String.valueOf(stats.today.points));
                        tvTodayActivities.setText(String.valueOf(stats.today.activities));
                    }
                    if (stats.week != null) {
                        tvWeekPoints.setText(String.valueOf(stats.week.points));
                    }
                }
            }

            @Override
            public void onFailure(Call<StatsResponse> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void loadWeekPointsFromStats() {
        ApiClient.getApiService().getStats().enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    StatsResponse stats = response.body();
                    if (stats.week != null) {
                        tvWeekPoints.setText(String.valueOf(stats.week.points));
                    }
                }
            }

            @Override
            public void onFailure(Call<StatsResponse> call, Throwable t) {
                // Silent fail
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromApi();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshData();
        }
    }
}
