package com.example.app_ecotrack.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
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

import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsFragment extends Fragment {
    private TextView tvLevelProgress, tvProgressPercent;
    private ProgressBar progressLevel;
    private LinearLayout containerCategories, containerRecent, containerWeekly;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews(view);
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvLevelProgress = view.findViewById(R.id.tvLevelProgress);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        progressLevel = view.findViewById(R.id.progressLevel);
        containerCategories = view.findViewById(R.id.containerCategories);
        containerRecent = view.findViewById(R.id.containerRecent);
        containerWeekly = view.findViewById(R.id.containerWeekly);
    }

    private void loadStatistics() {
        loadLevelProgress();
        loadCategoryStats();
        loadRecentActivities();
        loadWeeklyChart();
    }

    private void loadLevelProgress() {
        int points = prefs.getInt("points", 0);
        int level = prefs.getInt("level", 1);
        int currentLevelPoints = points % 100;

        progressLevel.setMax(100);
        progressLevel.setProgress(currentLevelPoints);

        tvLevelProgress.setText(currentLevelPoints + "/100 điểm đến cấp tiếp theo");
        tvProgressPercent.setText("Cấp " + level);
    }

    private void loadCategoryStats() {
        containerCategories.removeAllViews();

        String[] categories = {"transport", "energy", "water", "waste", "green", "consumption"};
        String[] categoryNames = {"Giao thông", "Năng lượng", "Nước", "Rác thải", "Cây xanh", "Tiêu dùng"};
        String[] categoryIcons = {"🚴", "💡", "💧", "♻️", "🌳", "🛒"};
        int[] categoryColors = {
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FFC107"),
                Color.parseColor("#03A9F4"),
                Color.parseColor("#8BC34A"),
                Color.parseColor("#009688"),
                Color.parseColor("#9C27B0")
        };

        for (int i = 0; i < categories.length; i++) {
            int count = getCategoryCount(categories[i]);
            View categoryView = createCategoryView(categoryIcons[i], categoryNames[i], count, categoryColors[i]);
            containerCategories.addView(categoryView);
        }
    }

    private int getCategoryCount(String category) {
        Cursor cursor = db.getUserActivities(userId);
        int count = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int activityId = cursor.getInt(cursor.getColumnIndexOrThrow("activity_id"));
                Cursor actCursor = db.getAllActivities();

                if (actCursor != null) {
                    while (actCursor.moveToNext()) {
                        if (actCursor.getInt(actCursor.getColumnIndexOrThrow("id")) == activityId) {
                            String cat = actCursor.getString(actCursor.getColumnIndexOrThrow("category"));
                            if (cat.equals(category)) {
                                count++;
                            }
                            break;
                        }
                    }
                    actCursor.close();
                }
            }
            cursor.close();
        }
        return count;
    }

    private View createCategoryView(String icon, String name, int count, int color) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_category_stat, containerCategories, false);

        TextView tvIcon = view.findViewById(R.id.tvCategoryIcon);
        TextView tvName = view.findViewById(R.id.tvCategoryName);
        TextView tvCount = view.findViewById(R.id.tvCategoryCount);
        ProgressBar progressBar = view.findViewById(R.id.progressCategory);

        tvIcon.setText(icon);
        tvName.setText(name);
        tvCount.setText(count + " lần");

        progressBar.setMax(20);
        progressBar.setProgress(Math.min(count, 20));
        progressBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);

        return view;
    }

    private void loadRecentActivities() {
        containerRecent.removeAllViews();

        Cursor cursor = db.getUserActivities(userId);
        List<Map<String, Object>> recentActivities = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext() && recentActivities.size() < 10) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")));
                activity.put("points", cursor.getInt(cursor.getColumnIndexOrThrow("points_earned")));
                activity.put("date", cursor.getString(cursor.getColumnIndexOrThrow("completed_date")));
                recentActivities.add(0, activity);
            }
            cursor.close();
        }

        for (Map<String, Object> activity : recentActivities) {
            View activityView = createRecentActivityView(
                    (String) activity.get("name"),
                    (Integer) activity.get("points"),
                    (String) activity.get("date")
            );
            containerRecent.addView(activityView);
        }
    }

    private View createRecentActivityView(String name, int points, String dateStr) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_recent_activity, containerRecent, false);

        TextView tvName = view.findViewById(R.id.tvActivityName);
        TextView tvDate = view.findViewById(R.id.tvActivityDate);
        TextView tvPoints = view.findViewById(R.id.tvActivityPoints);

        tvName.setText(name);
        tvPoints.setText("+" + points);

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            tvDate.setText(displayFormat.format(date));
        } catch (Exception e) {
            tvDate.setText(dateStr);
        }

        return view;
    }

    private void loadWeeklyChart() {
        containerWeekly.removeAllViews();

        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        int[] weekPoints = getWeeklyPoints();
        int maxPoints = 1;
        for (int points : weekPoints) {
            if (points > maxPoints) maxPoints = points;
        }

        for (int i = 0; i < 7; i++) {
            View barView = createBarView(days[i], weekPoints[i], maxPoints);
            containerWeekly.addView(barView);
        }
    }

    private int[] getWeeklyPoints() {
        int[] weekPoints = new int[7];
        Calendar calendar = Calendar.getInstance();

        for (int i = 6; i >= 0; i--) {
            String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            weekPoints[i] = getPointsForDate(dateStr);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        return weekPoints;
    }

    private int getPointsForDate(String dateStr) {
        Cursor cursor = db.getUserActivities(userId);
        int points = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String completedDate = cursor.getString(cursor.getColumnIndexOrThrow("completed_date"));
                if (completedDate.startsWith(dateStr)) {
                    points += cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"));
                }
            }
            cursor.close();
        }
        return points;
    }

    private View createBarView(String day, int points, int maxPoints) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_weekly_bar, containerWeekly, false);

        TextView tvDay = view.findViewById(R.id.tvDay);
        TextView tvPoints = view.findViewById(R.id.tvPoints);
        View bar = view.findViewById(R.id.bar);

        tvDay.setText(day);
        tvPoints.setText(String.valueOf(points));

        int height = maxPoints > 0 ? (int) (200 * ((float) points / maxPoints)) : 0;
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = Math.max(height, 20);
        bar.setLayoutParams(params);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }
}