package com.example.app_ecotrack.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.LeaderboardActivity;
import com.example.app_ecotrack.LoginActivity;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.RewardsActivity;
import com.example.app_ecotrack.SettingsActivity;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.ProfileResponse;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView tvFullname, tvUsername, tvEmail;
    private TextView tvTotalPoints, tvLevel, tvTotalActivities, tvRank;
    private TextView tvAvatarEmoji, tvLevelBadge;
    private MaterialCardView cardLeaderboard, cardRewards, cardSettings, cardLogout;
    private LinearLayout containerAchievements;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        initViews(view);
        loadProfileFromPrefs();
        loadProfileFromApi();
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
        tvAvatarEmoji = view.findViewById(R.id.tvAvatarEmoji);
        tvLevelBadge = view.findViewById(R.id.tvLevelBadge);
        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardSettings = view.findViewById(R.id.cardSettings);
        cardLogout = view.findViewById(R.id.cardLogout);
        containerAchievements = view.findViewById(R.id.containerAchievements);
    }

    private void loadProfileFromPrefs() {
        String fullname = prefs.getString("fullname", "User");
        String username = prefs.getString("username", "user");
        String email = prefs.getString("email", "");
        int points = getIntFromPrefs("points", 0);
        int level = getIntFromPrefs("level", 1);

        tvFullname.setText(fullname);
        tvUsername.setText("@" + username);
        tvEmail.setText(email);
        tvTotalPoints.setText(String.valueOf(points));
        tvLevel.setText(String.valueOf(level));
        tvLevelBadge.setText(String.valueOf(level));
        tvTotalActivities.setText("0");
        tvRank.setText("#-");

        // Set avatar emoji based on level
        setAvatarEmoji(level);
    }

    private void setAvatarEmoji(int level) {
        String emoji;
        if (level >= 10) emoji = "🦸";
        else if (level >= 7) emoji = "🌟";
        else if (level >= 5) emoji = "🌳";
        else if (level >= 3) emoji = "🌱";
        else emoji = "👤";
        tvAvatarEmoji.setText(emoji);
    }

    private void setupClickListeners() {
        cardLeaderboard.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), LeaderboardActivity.class));
        });

        cardRewards.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), RewardsActivity.class));
        });

        cardSettings.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), SettingsActivity.class));
        });

        cardLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        ApiClient.clearAuthToken();
        prefs.edit().clear().apply();
        
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void loadProfileFromApi() {
        ApiClient.getApiService().getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null && isAdded()) {
                    ProfileResponse profile = response.body();
                    
                    tvFullname.setText(profile.user.fullname);
                    tvUsername.setText("@" + profile.user.username);
                    tvEmail.setText(profile.user.email);
                    tvTotalPoints.setText(String.valueOf(profile.user.points));
                    tvLevel.setText(String.valueOf(profile.user.level));
                    tvLevelBadge.setText(String.valueOf(profile.user.level));
                    tvTotalActivities.setText(String.valueOf(profile.stats.totalActivities));
                    tvRank.setText("#" + profile.stats.rank);
                    
                    setAvatarEmoji(profile.user.level);
                    loadAchievements(profile.user.points, profile.stats.totalActivities);
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi tải hồ sơ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAchievements(int points, int activitiesCount) {
        if (containerAchievements == null) return;
        containerAchievements.removeAllViews();

        Achievement[] achievements = {
                new Achievement("🌟", "Người mới", "Hoàn thành đăng ký", true),
                new Achievement("🔥", "Nhiệt huyết", "10 hoạt động", activitiesCount >= 10),
                new Achievement("💯", "Trăm điểm", "Đạt 100 điểm", points >= 100),
                new Achievement("⚔️", "Chiến binh xanh", "50 hoạt động", activitiesCount >= 50),
                new Achievement("👑", "Huyền thoại", "500 điểm", points >= 500),
                new Achievement("🌳", "Người trồng cây", "Trồng 5 cây", false)
        };

        for (Achievement ach : achievements) {
            addAchievementItem(ach);
        }
    }

    private void addAchievementItem(Achievement achievement) {
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

        containerAchievements.addView(view);
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

    @Override
    public void onResume() {
        super.onResume();
        loadProfileFromApi();
    }

    private static class Achievement {
        String icon, name, description;
        boolean unlocked;

        Achievement(String icon, String name, String description, boolean unlocked) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.unlocked = unlocked;
        }
    }
}
