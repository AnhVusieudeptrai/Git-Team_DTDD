package com.example.app_ecotrack.ui.badges;

import android.animation.ObjectAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.BadgeData;
import com.example.app_ecotrack.databinding.FragmentBadgesBinding;
import com.example.app_ecotrack.ui.adapters.BadgeAdapter;
import com.example.app_ecotrack.viewmodel.BadgesViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * BadgesFragment - Màn hình hiển thị huy hiệu theo loại
 * Hỗ trợ filter by type và badge detail dialog
 */
public class BadgesFragment extends Fragment {

    private FragmentBadgesBinding binding;
    private BadgesViewModel viewModel;
    private BadgeAdapter badgeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBadgesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(BadgesViewModel.class);
        
        // Setup RecyclerView
        setupBadgesRecyclerView();
        
        // Setup filter chips
        setupFilterChips();
        
        // Setup retry button
        setupRetryButton();
        
        // Observe LiveData
        observeViewModel();
        
        // Load data
        viewModel.loadBadges();
    }

    private void setupRetryButton() {
        binding.btnRetry.setOnClickListener(v -> {
            binding.layoutError.setVisibility(View.GONE);
            viewModel.loadBadges();
        });
    }

    private void setupBadgesRecyclerView() {
        badgeAdapter = new BadgeAdapter();
        badgeAdapter.setOnBadgeClickListener(this::showBadgeDetailDialog);
        
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 3);
        binding.recyclerBadges.setLayoutManager(layoutManager);
        binding.recyclerBadges.setAdapter(badgeAdapter);
        binding.recyclerBadges.setNestedScrollingEnabled(false);
    }

    private void setupFilterChips() {
        binding.chipAll.setOnClickListener(v -> {
            viewModel.applyFilter(BadgesViewModel.FILTER_ALL);
        });
        
        binding.chipStreak.setOnClickListener(v -> {
            viewModel.applyFilter(BadgesViewModel.FILTER_STREAK);
        });
        
        binding.chipPoints.setOnClickListener(v -> {
            viewModel.applyFilter(BadgesViewModel.FILTER_POINTS);
        });
        
        binding.chipActivities.setOnClickListener(v -> {
            viewModel.applyFilter(BadgesViewModel.FILTER_ACTIVITIES);
        });
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
                boolean hasData = viewModel.getFilteredBadges().getValue() != null && 
                                  !viewModel.getFilteredBadges().getValue().isEmpty();
                if (!hasData) {
                    // Show error state with retry
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.textErrorMessage.setText(error);
                } else {
                    // Show snackbar with retry action
                    Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                            .setAction(R.string.retry, v -> viewModel.loadBadges())
                            .show();
                }
            }
        });

        // Observe badge stats
        viewModel.getBadgeStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                binding.textEarnedCount.setText(
                    getString(R.string.badges_earned_count, stats.earnedBadges, stats.totalBadges)
                );
            }
        });

        // Observe filtered badges
        viewModel.getFilteredBadges().observe(getViewLifecycleOwner(), badges -> {
            if (badges != null && !badges.isEmpty()) {
                badgeAdapter.setBadges(badges);
                binding.recyclerBadges.setVisibility(View.VISIBLE);
                binding.layoutEmpty.setVisibility(View.GONE);
            } else {
                binding.recyclerBadges.setVisibility(View.GONE);
                binding.layoutEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Show badge detail dialog
     */
    private void showBadgeDetailDialog(BadgeData badge) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_badge_detail, null);

        // Find views
        MaterialCardView cardBadgeIcon = dialogView.findViewById(R.id.card_badge_icon);
        TextView textBadgeIcon = dialogView.findViewById(R.id.text_badge_icon);
        TextView textBadgeName = dialogView.findViewById(R.id.text_badge_name);
        Chip chipRarity = dialogView.findViewById(R.id.chip_rarity);
        TextView textBadgeDescription = dialogView.findViewById(R.id.text_badge_description);
        TextView textRequirement = dialogView.findViewById(R.id.text_requirement);
        LinearLayout layoutProgress = dialogView.findViewById(R.id.layout_progress);
        TextView textProgressPercent = dialogView.findViewById(R.id.text_progress_percent);
        LinearProgressIndicator progressBadge = dialogView.findViewById(R.id.progress_badge);
        TextView textProgressDetail = dialogView.findViewById(R.id.text_progress_detail);
        TextView textEarnedDate = dialogView.findViewById(R.id.text_earned_date);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close);

        // Set badge icon
        String icon = badge.icon != null ? badge.icon : "🏆";
        textBadgeIcon.setText(icon);

        // Set badge name
        textBadgeName.setText(badge.name);

        // Set description
        textBadgeDescription.setText(badge.description);

        // Get rarity color and text
        int rarityColor = getRarityColor(badge.rarity);
        String rarityText = getRarityText(badge.rarity);

        // Set rarity chip
        chipRarity.setText(rarityText);
        chipRarity.setChipBackgroundColor(
            android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), rarityColor)
            )
        );

        // Set card background color based on rarity
        cardBadgeIcon.setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), rarityColor)
        );

        // Set requirement text
        String requirementText = getRequirementText(badge.type, badge.requirement);
        textRequirement.setText(requirementText);

        // Handle earned/unearned state
        if (badge.earned) {
            // Show earned date
            layoutProgress.setVisibility(View.GONE);
            textEarnedDate.setVisibility(View.VISIBLE);
            
            String formattedDate = formatEarnedDate(badge.earnedAt);
            textEarnedDate.setText(getString(R.string.badge_earned_on, formattedDate));
        } else {
            // Show progress
            textEarnedDate.setVisibility(View.GONE);
            
            if (badge.progress > 0 || badge.progressPercent > 0) {
                layoutProgress.setVisibility(View.VISIBLE);
                
                int progress = badge.progressPercent > 0 ? badge.progressPercent : 
                        (badge.requirement > 0 ? (badge.progress * 100 / badge.requirement) : 0);
                
                progressBadge.setProgress(Math.min(100, progress));
                progressBadge.setIndicatorColor(ContextCompat.getColor(requireContext(), rarityColor));
                textProgressPercent.setText(progress + "%");
                textProgressDetail.setText(badge.progress + "/" + badge.requirement + " " + getProgressUnit(badge.type));
            } else {
                layoutProgress.setVisibility(View.GONE);
            }
        }

        // Create and show dialog
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Animate badge icon
        animateBadgeIcon(cardBadgeIcon);
    }

    /**
     * Animate badge icon with scale and rotation
     */
    private void animateBadgeIcon(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.1f, 1f);
        
        scaleX.setDuration(400);
        scaleY.setDuration(400);
        
        scaleX.start();
        scaleY.start();
    }

    /**
     * Get rarity color resource
     */
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
     * Get rarity text in Vietnamese
     */
    private String getRarityText(String rarity) {
        if (rarity == null) return getString(R.string.rarity_common);
        switch (rarity) {
            case "rare": return getString(R.string.rarity_rare);
            case "epic": return getString(R.string.rarity_epic);
            case "legendary": return getString(R.string.rarity_legendary);
            default: return getString(R.string.rarity_common);
        }
    }

    /**
     * Get requirement text based on badge type
     */
    private String getRequirementText(String type, int requirement) {
        if (type == null) return String.valueOf(requirement);
        switch (type) {
            case "streak":
                return getString(R.string.badge_type_streak, requirement);
            case "points":
                return getString(R.string.badge_type_points, requirement);
            case "activities":
                return getString(R.string.badge_type_activities, requirement);
            default:
                return String.valueOf(requirement);
        }
    }

    /**
     * Get progress unit based on badge type
     */
    private String getProgressUnit(String type) {
        if (type == null) return "";
        switch (type) {
            case "streak":
                return "ngày";
            case "points":
                return "điểm";
            case "activities":
                return "hoạt động";
            default:
                return "";
        }
    }

    /**
     * Format earned date string
     */
    private String formatEarnedDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        
        try {
            // Try parsing ISO format
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // Try another format
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e2) {
                // Return original string if parsing fails
            }
        }
        
        return dateString;
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
