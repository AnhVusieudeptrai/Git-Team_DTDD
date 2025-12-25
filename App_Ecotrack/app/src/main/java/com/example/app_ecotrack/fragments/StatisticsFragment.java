package com.example.app_ecotrack.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.StatsResponse;
import com.example.app_ecotrack.api.models.ActivityHistoryResponse;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsFragment extends Fragment {
    private TextView tvLevelProgress, tvProgressPercent;
    private TextView tvTodayStats, tvWeekStats, tvTotalStats;
    private ProgressBar progressLevel;
    private LinearLayout containerCategories, containerRecent, containerWeekly;

    private static final Map<String, String> CATEGORY_NAMES = new HashMap<>();
    private static final Map<String, String> CATEGORY_ICONS = new HashMap<>();
    private static final Map<String, Integer> CATEGORY_COLORS = new HashMap<>();

    static {
        CATEGORY_NAMES.put("transport", "Giao thông");
        CATEGORY_NAMES.put("energy", "Năng lượng");
        CATEGORY_NAMES.put("water", "Nước");
        CATEGORY_NAMES.put("waste", "Rác thải");
        CATEGORY_NAMES.put("green", "Cây xanh");
        CATEGORY_NAMES.put("consumption", "Tiêu dùng");

        CATEGORY_ICONS.put("transport", "🚲");
        CATEGORY_ICONS.put("energy", "⚡");
        CATEGORY_ICONS.put("water", "💧");
        CATEGORY_ICONS.put("waste", "♻️");
        CATEGORY_ICONS.put("green", "🌿");
        CATEGORY_ICONS.put("consumption", "🛒");

        CATEGORY_COLORS.put("transport", Color.parseColor("#4CAF50"));
        CATEGORY_COLORS.put("energy", Color.parseColor("#FFC107"));
        CATEGORY_COLORS.put("water", Color.parseColor("#03A9F4"));
        CATEGORY_COLORS.put("waste", Color.parseColor("#8BC34A"));
        CATEGORY_COLORS.put("green", Color.parseColor("#009688"));
        CATEGORY_COLORS.put("consumption", Color.parseColor("#9C27B0"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        initViews(view);
        loadStatistics();
        return view;
    }

    private void initViews(View view) {
        tvLevelProgress = view.findViewById(R.id.tvLevelProgress);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressLevel = view.findViewById(R.id.progressLevel);
        tvTodayStats = view.findViewById(R.id.tvTodayStats);
        tvWeekStats = view.findViewById(R.id.tvWeekStats);
        tvTotalStats = view.findViewById(R.id.tvTotalStats);
        containerCategories = view.findViewById(R.id.containerCategories);
        containerRecent = view.findViewById(R.id.containerRecent);
        containerWeekly = view.findViewById(R.id.containerWeekly);
    }

    private void loadStatistics() {
        loadEmptyCategoryStats();
        loadEmptyWeeklyChart();

        ApiClient.getApiService().getStats().enqueue(new Callback<StatsResponse>() {
            @Override
            public void onResponse(Call<StatsResponse> call, Response<StatsResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    StatsResponse stats = response.body();
                    
                    if (stats.today != null) {
                        tvTodayStats.setText(String.valueOf(stats.today.points));
                    }
                    if (stats.week != null) {
                        tvWeekStats.setText(String.valueOf(stats.week.points));
                    }
                    if (stats.total != null) {
                        tvTotalStats.setText(String.valueOf(stats.total.points));
                        updateLevelProgress(stats.total.points, stats.total.level);
                    }
                    if (stats.categories != null) {
                        updateCategoryStats(stats.categories);
                    }
                    if (stats.weeklyChart != null) {
                        updateWeeklyChart(stats.weeklyChart);
                    }
                }
            }

            @Override
            public void onFailure(Call<StatsResponse> call, Throwable t) {}
        });

        loadRecentActivities();
    }

    private void updateLevelProgress(int points, int level) {
        int currentLevelPoints = points % 100;
        progressLevel.setMax(100);
        progressLevel.setProgress(currentLevelPoints);
        tvLevelProgress.setText(currentLevelPoints + "/100 điểm đến cấp tiếp theo");
        tvProgressPercent.setText("Cấp " + level);
    }

    private void loadEmptyCategoryStats() {
        if (containerCategories == null) return;
        containerCategories.removeAllViews();
        String[] categories = {"transport", "energy", "water", "waste", "green", "consumption"};
        for (String cat : categories) {
            addCategoryItem(cat, 0);
        }
    }

    private void updateCategoryStats(List<StatsResponse.CategoryStat> categories) {
        if (containerCategories == null || !isAdded()) return;
        containerCategories.removeAllViews();

        Map<String, Integer> categoryCounts = new HashMap<>();
        for (StatsResponse.CategoryStat stat : categories) {
            categoryCounts.put(stat._id, stat.count);
        }

        String[] allCategories = {"transport", "energy", "water", "waste", "green", "consumption"};
        for (String cat : allCategories) {
            int count = categoryCounts.getOrDefault(cat, 0);
            addCategoryItem(cat, count);
        }
    }

    private void addCategoryItem(String category, int count) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_category_stat, containerCategories, false);

        TextView tvIcon = view.findViewById(R.id.tvCategoryIcon);
        TextView tvName = view.findViewById(R.id.tvCategoryName);
        TextView tvCount = view.findViewById(R.id.tvCategoryCount);
        ProgressBar progressBar = view.findViewById(R.id.progressCategory);

        String icon = CATEGORY_ICONS.getOrDefault(category, "📌");
        String name = CATEGORY_NAMES.getOrDefault(category, category);
        int color = CATEGORY_COLORS.getOrDefault(category, Color.GRAY);

        tvIcon.setText(icon);
        tvName.setText(name);
        tvCount.setText(count + " lần");

        progressBar.setMax(20);
        progressBar.setProgress(Math.min(count, 20));
        if (progressBar.getProgressDrawable() != null) {
            progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        containerCategories.addView(view);
    }

    private void loadRecentActivities() {
        if (containerRecent == null) return;
        containerRecent.removeAllViews();

        ApiClient.getApiService().getActivityHistory(1, 5).enqueue(new Callback<ActivityHistoryResponse>() {
            @Override
            public void onResponse(Call<ActivityHistoryResponse> call, Response<ActivityHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    containerRecent.removeAllViews();
                    
                    if (response.body().activities != null && !response.body().activities.isEmpty()) {
                        for (ActivityHistoryResponse.HistoryItem item : response.body().activities) {
                            String name = item.activity != null ? item.activity.name : "Hoạt động";
                            addRecentItem(name, item.pointsEarned, item.completedAt);
                        }
                    } else {
                        TextView empty = new TextView(requireContext());
                        empty.setText("Chưa có hoạt động nào");
                        empty.setTextColor(Color.GRAY);
                        empty.setPadding(0, 16, 0, 16);
                        containerRecent.addView(empty);
                    }
                }
            }

            @Override
            public void onFailure(Call<ActivityHistoryResponse> call, Throwable t) {
                if (isAdded() && containerRecent != null) {
                    containerRecent.removeAllViews();
                    TextView empty = new TextView(requireContext());
                    empty.setText("Không thể tải dữ liệu");
                    empty.setTextColor(Color.GRAY);
                    containerRecent.addView(empty);
                }
            }
        });
    }

    private void addRecentItem(String name, int points, String dateStr) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_recent_activity, containerRecent, false);

        TextView tvName = view.findViewById(R.id.tvActivityName);
        TextView tvDate = view.findViewById(R.id.tvActivityDate);
        TextView tvPoints = view.findViewById(R.id.tvActivityPoints);

        tvName.setText(name);
        tvPoints.setText("+" + points);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            tvDate.setText(displayFormat.format(date));
        } catch (Exception e) {
            tvDate.setText(dateStr != null && dateStr.length() >= 10 ? dateStr.substring(5, 10) : "");
        }

        containerRecent.addView(view);
    }

    private void loadEmptyWeeklyChart() {
        if (containerWeekly == null) return;
        containerWeekly.removeAllViews();
        String[] days = getDayLabels();
        for (String day : days) {
            addBarItem(day, 0, 1);
        }
    }

    private void updateWeeklyChart(List<StatsResponse.WeeklyChartItem> weeklyData) {
        if (containerWeekly == null || !isAdded()) return;
        containerWeekly.removeAllViews();

        int maxPoints = 1;
        for (StatsResponse.WeeklyChartItem item : weeklyData) {
            if (item.points > maxPoints) maxPoints = item.points;
        }

        String[] dayLabels = getDayLabels();
        for (int i = 0; i < weeklyData.size() && i < 7; i++) {
            addBarItem(dayLabels[i], weeklyData.get(i).points, maxPoints);
        }
    }

    private String[] getDayLabels() {
        String[] labels = new String[7];
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        
        for (int i = 0; i < 7; i++) {
            labels[i] = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1];
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return labels;
    }

    private void addBarItem(String day, int points, int maxPoints) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_weekly_bar, containerWeekly, false);

        TextView tvDay = view.findViewById(R.id.tvDay);
        TextView tvPoints = view.findViewById(R.id.tvPoints);
        View bar = view.findViewById(R.id.bar);

        tvDay.setText(day);
        tvPoints.setText(String.valueOf(points));

        int height = maxPoints > 0 ? (int) (140 * ((float) points / maxPoints)) : 0;
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = Math.max(height, 8);
        bar.setLayoutParams(params);

        containerWeekly.addView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}
