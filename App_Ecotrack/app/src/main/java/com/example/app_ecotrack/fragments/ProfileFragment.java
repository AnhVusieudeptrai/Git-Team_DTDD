package com.example.app_ecotrack.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.RewardsActivity;
import com.example.app_ecotrack.SettingsActivity;

public class ProfileFragment extends Fragment {
    private TextView tvFullname, tvUsername, tvEmail, tvTotalPoints, tvLevel, tvTotalActivities, tvRank;
    private CardView cardLeaderboard, cardRewards, cardSettings;
    private LinearLayout containerAchievements;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews(view);
        loadProfileData();
        loadAchievements();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvFullname = view.findViewById(R.id.tvFullname);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities);
        tvRank = view.findViewById(R.id.tvRank);

        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardSettings = view.findViewById(R.id.cardSettings);

        containerAchievements = view.findViewById(R.id.containerAchievements);
    }

    private void loadProfileData() {
        Cursor cursor = db.getUserById(userId);

        if (cursor != null && cursor.moveToFirst()) {
            String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
            int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

            tvFullname.setText(fullname);
            tvUsername.setText("@" + username);
            tvEmail.setText(email);
            tvTotalPoints.setText(String.valueOf(points));
            tvLevel.setText(String.valueOf(level));

            cursor.close();
        }

        // Total activities
        Cursor actCursor = db.getUserActivities(userId);
        int totalAct = actCursor != null ? actCursor.getCount() : 0;
        tvTotalActivities.setText(String.valueOf(totalAct));
        if (actCursor != null) actCursor.close();

        // Rank
        int rank = getUserRank();
        tvRank.setText("#" + rank);
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

    private void loadAchievements() {
        containerAchievements.removeAllViews();

        int points = prefs.getInt("points", 0);
        Cursor cursor = db.getUserActivities(userId);
        int activitiesCount = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();

        // Define achievements
        Achievement[] achievements = {
                new Achievement("🌟", "Người mới", "Hoàn thành đăng ký", true),
                new Achievement("🔥", "Nhiệt huyết", "10 hoạt động", activitiesCount >= 10),
                new Achievement("💯", "Trăm điểm", "Đạt 100 điểm", points >= 100),
                new Achievement("⚔️", "Chiến binh", "50 hoạt động", activitiesCount >= 50),
                new Achievement("👑", "Huyền thoại", "500 điểm", points >= 500),
                new Achievement("🌳", "Cây xanh", "Trồng 5 cây", getCategoryCount("green") >= 5)
        };

        for (Achievement ach : achievements) {
            View achView = createAchievementView(ach);
            containerAchievements.addView(achView);
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

    private View createAchievementView(Achievement achievement) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.item_achievement, containerAchievements, false);

        TextView tvIcon = view.findViewById(R.id.tvAchievementIcon);
        TextView tvName = view.findViewById(R.id.tvAchievementName);
        TextView tvDesc = view.findViewById(R.id.tvAchievementDesc);
        View overlay = view.findViewById(R.id.achievementOverlay);

        tvIcon.setText(achievement.icon);
        tvName.setText(achievement.name);
        tvDesc.setText(achievement.description);

        if (!achievement.unlocked) {
            overlay.setVisibility(View.VISIBLE);
            view.setAlpha(0.5f);
        } else {
            overlay.setVisibility(View.GONE);
            view.setAlpha(1.0f);
        }

        return view;
    }

    private void setupClickListeners() {
        cardRewards.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RewardsActivity.class);
            startActivity(intent);
        });

        cardSettings.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileData();
        loadAchievements();
    }

    private static class Achievement {
        String icon;
        String name;
        String description;
        boolean unlocked;

        Achievement(String icon, String name, String description, boolean unlocked) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.unlocked = unlocked;
        }
    }
}