package com.example.app_ecotrack.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {
    private TextView tvTodayPoints, tvWeekPoints, tvTotalPoints, tvTodayActivities, tvTotalActivities, tvRank;
    private TextView tvCurrentStreak, tvLongestStreak, tvBadgeCount;
    private TextView tvChallengeTitle, tvChallengeDesc, tvChallengeBonus, tvChallengeProgress;
    private TextView tvEcoTip;
    private ProgressBar progressChallenge;
    private CardView cardActivities, cardRewards, cardLeaderboard;
    private MaterialCardView cardStreak, cardDailyChallenge;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews(view);
        loadData();
        loadStreakData();
        loadDailyChallenge();
        loadEcoTip();
        setupClickListeners();
        setupAnimations(view);

        return view;
    }

    private void initViews(View view) {
        tvTodayPoints = view.findViewById(R.id.tvTodayPoints);
        tvWeekPoints = view.findViewById(R.id.tvWeekPoints);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvTodayActivities = view.findViewById(R.id.tvTodayActivities);
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities);
        tvRank = view.findViewById(R.id.tvRank);

        // Streak views
        tvCurrentStreak = view.findViewById(R.id.tvCurrentStreak);
        tvLongestStreak = view.findViewById(R.id.tvLongestStreak);
        tvBadgeCount = view.findViewById(R.id.tvBadgeCount);
        cardStreak = view.findViewById(R.id.cardStreak);

        // Daily Challenge views
        cardDailyChallenge = view.findViewById(R.id.cardDailyChallenge);
        tvChallengeTitle = view.findViewById(R.id.tvChallengeTitle);
        tvChallengeDesc = view.findViewById(R.id.tvChallengeDesc);
        tvChallengeBonus = view.findViewById(R.id.tvChallengeBonus);
        tvChallengeProgress = view.findViewById(R.id.tvChallengeProgress);
        progressChallenge = view.findViewById(R.id.progressChallenge);

        // Eco Tip
        tvEcoTip = view.findViewById(R.id.tvEcoTip);

        cardActivities = view.findViewById(R.id.cardActivities);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
    }

    private void setupAnimations(View view) {
        // Animate cards on load
        if (cardStreak != null) {
            cardStreak.setAlpha(0f);
            cardStreak.setTranslationY(50);
            cardStreak.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(100).start();
        }

        if (cardDailyChallenge != null) {
            cardDailyChallenge.setAlpha(0f);
            cardDailyChallenge.setTranslationY(50);
            cardDailyChallenge.animate().alpha(1f).translationY(0).setDuration(500).setStartDelay(200).start();
        }
    }

    private void loadData() {
        // Today's points
        int todayPoints = db.getTodayPoints(userId);
        tvTodayPoints.setText(String.valueOf(todayPoints));

        // Week points
        int weekPoints = getWeekPoints();
        tvWeekPoints.setText(String.valueOf(weekPoints));

        // Total points
        int totalPoints = prefs.getInt("points", 0);
        tvTotalPoints.setText(String.valueOf(totalPoints));

        // Today's activities count
        Cursor todayCursor = db.getTodayActivities(userId);
        int todayCount = todayCursor != null ? todayCursor.getCount() : 0;
        tvTodayActivities.setText(String.valueOf(todayCount));
        if (todayCursor != null) todayCursor.close();

        // Total activities count
        Cursor totalCursor = db.getUserActivities(userId);
        int totalCount = totalCursor != null ? totalCursor.getCount() : 0;
        tvTotalActivities.setText(String.valueOf(totalCount));
        if (totalCursor != null) totalCursor.close();

        // Rank
        int rank = getUserRank();
        tvRank.setText("#" + rank);

        // Badge count
        int badgeCount = db.getUserBadgeCount(userId);
        tvBadgeCount.setText("🏅 " + badgeCount);
    }

    private void loadStreakData() {
        int[] streakData = db.getUserStreak(userId);
        tvCurrentStreak.setText(String.valueOf(streakData[0]));
        tvLongestStreak.setText(streakData[1] + " ngày");
    }

    private void loadDailyChallenge() {
        Cursor cursor = db.getDailyChallenge();
        if (cursor != null && !cursor.isAfterLast()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            int targetCount = cursor.getInt(cursor.getColumnIndexOrThrow("target_count"));
            int bonusPoints = cursor.getInt(cursor.getColumnIndexOrThrow("bonus_points"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));

            tvChallengeTitle.setText(title);
            tvChallengeDesc.setText(desc);
            tvChallengeBonus.setText("+" + bonusPoints);

            // Get current progress
            int currentCount = db.getTodayCategoryCount(userId, category);
            int progress = Math.min(currentCount, targetCount);
            
            tvChallengeProgress.setText(progress + "/" + targetCount);
            progressChallenge.setMax(targetCount);
            progressChallenge.setProgress(progress);

            // Check if completed
            if (progress >= targetCount) {
                tvChallengeTitle.setText("✅ " + title);
            }

            cursor.close();
        }
    }

    private void loadEcoTip() {
        String tip = db.getRandomEcoTip();
        tvEcoTip.setText(tip);
    }

    private int getWeekPoints() {
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        Cursor cursor = db.getUserActivities(userId);
        int weekPoints = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("completed_date"));
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(dateStr);
                    if (date != null && date.getTime() > weekAgo) {
                        weekPoints += cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return weekPoints;
    }

    private int getUserRank() {
        Cursor leaderboard = db.getLeaderboard();
        int rank = 1;
        if (leaderboard != null) {
            while (leaderboard.moveToNext()) {
                int id = leaderboard.getInt(leaderboard.getColumnIndexOrThrow("id"));
                if (id == userId) {
                    break;
                }
                rank++;
            }
            leaderboard.close();
        }
        return rank;
    }

    private void setupClickListeners() {
        cardActivities.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                androidx.viewpager2.widget.ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(1);
            }
        });

        cardLeaderboard.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), 
                    com.example.app_ecotrack.LeaderboardActivity.class);
            startActivity(intent);
        });

        cardRewards.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), 
                    com.example.app_ecotrack.RewardsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        loadStreakData();
        loadDailyChallenge();
        loadEcoTip();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshData();
        }
    }
}