package com.example.app_ecotrack.ui.home;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ActivityData;
import com.example.app_ecotrack.api.models.BadgeData;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.example.app_ecotrack.databinding.FragmentHomeBinding;
import com.example.app_ecotrack.ui.adapters.ActivityAdapter;
import com.example.app_ecotrack.ui.adapters.ChallengeHomeAdapter;
import com.example.app_ecotrack.viewmodel.HomeViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * HomeFragment - Màn hình chính hiển thị streak, điểm, CO2, danh sách hoạt động
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private ActivityAdapter activityAdapter;
    private ChallengeHomeAdapter challengeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        // Setup RecyclerViews
        setupActivityRecyclerView();
        setupChallengeRecyclerView();
        
        // Setup retry button
        setupRetryButton();
        
        // Observe LiveData
        observeViewModel();
        
        // Load data
        viewModel.loadHomeData();
    }

    private void setupRetryButton() {
        binding.btnRetry.setOnClickListener(v -> {
            binding.layoutError.setVisibility(View.GONE);
            viewModel.loadHomeData();
        });
    }

    private void setupActivityRecyclerView() {
        activityAdapter = new ActivityAdapter();
        activityAdapter.setOnActivityClickListener(this::onActivityClick);
        
        binding.recyclerActivities.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerActivities.setAdapter(activityAdapter);
        binding.recyclerActivities.setNestedScrollingEnabled(false);
    }

    private void setupChallengeRecyclerView() {
        challengeAdapter = new ChallengeHomeAdapter();
        challengeAdapter.setOnChallengeClickListener(this::onChallengeClick);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerChallenges.setLayoutManager(layoutManager);
        binding.recyclerChallenges.setAdapter(challengeAdapter);
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showShimmerLoading();
            } else {
                hideShimmerLoading();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                // Check if we have data to show
                boolean hasData = viewModel.getActivities().getValue() != null && 
                                  !viewModel.getActivities().getValue().isEmpty();
                if (!hasData) {
                    // Show error state with retry
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.textErrorMessage.setText(error);
                } else {
                    // Show snackbar with retry action
                    Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> viewModel.loadHomeData())
                            .show();
                }
            }
        });

        // Observe streak data
        viewModel.getStreak().observe(getViewLifecycleOwner(), this::updateStreakUI);

        // Observe today stats
        viewModel.getTodayStats().observe(getViewLifecycleOwner(), this::updateTodayStatsUI);

        // Observe activities
        viewModel.getActivities().observe(getViewLifecycleOwner(), activities -> {
            activityAdapter.setActivities(activities);
        });

        // Observe active challenges
        viewModel.getActiveChallenges().observe(getViewLifecycleOwner(), challenges -> {
            if (challenges != null && !challenges.isEmpty()) {
                challengeAdapter.setChallenges(challenges);
                binding.recyclerChallenges.setVisibility(View.VISIBLE);
                binding.textNoChallenges.setVisibility(View.GONE);
            } else {
                binding.recyclerChallenges.setVisibility(View.GONE);
                binding.textNoChallenges.setVisibility(View.VISIBLE);
            }
        });

        // Observe activity completed
        viewModel.getActivityCompleted().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                if (result.success) {
                    showActivityCompletedDialog(result.pointsEarned, result.totalPoints);
                } else {
                    Snackbar.make(binding.getRoot(), result.errorMessage, Snackbar.LENGTH_SHORT).show();
                }
                viewModel.clearActivityCompletedEvent();
            }
        });

        // Observe new badge earned
        viewModel.getNewBadgeEarned().observe(getViewLifecycleOwner(), badge -> {
            if (badge != null) {
                showNewBadgeDialog(badge);
                viewModel.clearNewBadgeEvent();
            }
        });

        // Observe challenge completed
        viewModel.getChallengeCompleted().observe(getViewLifecycleOwner(), challenge -> {
            if (challenge != null) {
                showChallengeCompletedDialog(challenge);
                viewModel.clearChallengeCompletedEvent();
            }
        });
    }

    private void updateStreakUI(com.example.app_ecotrack.api.models.StreakResponse.StreakData streak) {
        if (streak == null) {
            binding.textStreakCount.setText(getString(R.string.streak_days, 0));
            binding.textLongestStreak.setText(getString(R.string.longest_streak, 0));
            return;
        }

        binding.textStreakCount.setText(getString(R.string.streak_days, streak.currentStreak));
        binding.textLongestStreak.setText(getString(R.string.longest_streak, streak.longestStreak));

        // Animate streak card if active
        if (streak.isActive && streak.currentStreak > 0) {
            animateStreakFire();
        }
    }

    private void updateTodayStatsUI(HomeViewModel.TodayStats stats) {
        if (stats == null) {
            binding.textTodayPoints.setText("0");
            binding.textCo2Saved.setText(getString(R.string.co2_kg, 0.0));
            return;
        }

        binding.textTodayPoints.setText(String.valueOf(stats.todayPoints));
        binding.textCo2Saved.setText(getString(R.string.co2_kg, stats.co2Saved));
    }

    private void animateStreakFire() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(binding.iconStreak, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(binding.iconStreak, "scaleY", 1f, 1.2f, 1f);
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    private void onActivityClick(ActivityData activity) {
        viewModel.completeActivity(activity.id);
    }

    private void onChallengeClick(ChallengeData challenge) {
        // Navigate to challenges screen or show detail
        // For now, just show a snackbar
        Snackbar.make(binding.getRoot(), challenge.name, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Show activity completed dialog with animation
     */
    private void showActivityCompletedDialog(int pointsEarned, int totalPoints) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_activity_completed, null);

        View cardCheckIcon = dialogView.findViewById(R.id.card_check_icon);
        TextView textPointsEarned = dialogView.findViewById(R.id.text_points_earned);
        TextView textTotalPoints = dialogView.findViewById(R.id.text_total_points);
        com.google.android.material.button.MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);

        textPointsEarned.setText("+" + pointsEarned + " " + getString(R.string.points));
        textTotalPoints.setText(getString(R.string.total_points) + ": " + totalPoints);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Animate check icon with bounce effect
        com.example.app_ecotrack.utils.AnimationUtils.animateBadgeEarned(cardCheckIcon);
        
        // Animate points text
        com.example.app_ecotrack.utils.AnimationUtils.animatePointsEarned(textPointsEarned);
    }

    /**
     * Show new badge earned dialog
     */
    private void showNewBadgeDialog(BadgeData badge) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_new_badge, null);

        TextView textBadgeIcon = dialogView.findViewById(R.id.text_badge_icon);
        TextView textBadgeName = dialogView.findViewById(R.id.text_badge_name);
        TextView textBadgeDescription = dialogView.findViewById(R.id.text_badge_description);
        Chip chipRarity = dialogView.findViewById(R.id.chip_rarity);
        MaterialCardView cardBadgeIcon = dialogView.findViewById(R.id.card_badge_icon);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);

        // Set badge info
        textBadgeIcon.setText(badge.icon != null ? badge.icon : "🏆");
        textBadgeName.setText(badge.name);
        textBadgeDescription.setText(badge.description);

        // Set rarity
        String rarityText = getRarityText(badge.rarity);
        int rarityColor = getRarityColor(badge.rarity);
        chipRarity.setText(rarityText);
        chipRarity.setChipBackgroundColorResource(rarityColor);
        cardBadgeIcon.setCardBackgroundColor(getResources().getColor(rarityColor, null));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Animate badge icon
        animateBadgeIcon(cardBadgeIcon);
    }

    /**
     * Show challenge completed dialog
     */
    private void showChallengeCompletedDialog(ChallengeData challenge) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_challenge_completed, null);

        TextView textChallengeName = dialogView.findViewById(R.id.text_challenge_name);
        TextView textPointsReward = dialogView.findViewById(R.id.text_points_reward);
        LinearLayout layoutBadgeReward = dialogView.findViewById(R.id.layout_badge_reward);
        TextView textBadgeReward = dialogView.findViewById(R.id.text_badge_reward);
        MaterialButton btnOk = dialogView.findViewById(R.id.btn_ok);

        // Set challenge info
        textChallengeName.setText(challenge.name);
        textPointsReward.setText("+" + challenge.rewardPoints + " " + getString(R.string.points));

        // Show badge reward if any
        if (challenge.rewardBadge != null) {
            layoutBadgeReward.setVisibility(View.VISIBLE);
            textBadgeReward.setText(challenge.rewardBadge.name);
        }

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnOk.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void animateBadgeIcon(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        
        scaleX.setDuration(500);
        scaleY.setDuration(500);
        rotation.setDuration(500);
        
        scaleX.start();
        scaleY.start();
        rotation.start();
    }

    private String getRarityText(String rarity) {
        if (rarity == null) return "Thường";
        switch (rarity) {
            case "rare": return "Hiếm";
            case "epic": return "Sử thi";
            case "legendary": return "Huyền thoại";
            default: return "Thường";
        }
    }

    private int getRarityColor(String rarity) {
        if (rarity == null) return R.color.rarity_common;
        switch (rarity) {
            case "rare": return R.color.rarity_rare;
            case "epic": return R.color.rarity_epic;
            case "legendary": return R.color.rarity_legendary;
            default: return R.color.rarity_common;
        }
    }

    /**
     * Show shimmer loading animation
     */
    private void showShimmerLoading() {
        if (binding.shimmerLoading != null) {
            binding.shimmerLoading.setVisibility(View.VISIBLE);
            binding.shimmerLoading.startShimmer();
        }
        binding.loadingContainer.setVisibility(View.GONE);
    }

    /**
     * Hide shimmer loading animation
     */
    private void hideShimmerLoading() {
        if (binding.shimmerLoading != null) {
            binding.shimmerLoading.stopShimmer();
            binding.shimmerLoading.setVisibility(View.GONE);
        }
        binding.loadingContainer.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
