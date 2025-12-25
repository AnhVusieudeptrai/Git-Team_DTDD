package com.example.app_ecotrack.ui.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.example.app_ecotrack.databinding.FragmentChallengesBinding;
import com.example.app_ecotrack.viewmodel.ChallengesViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.TimeUnit;

/**
 * ChallengesFragment - Màn hình hiển thị thử thách tuần/tháng
 * Sử dụng TabLayout + ViewPager2 cho 3 tabs: Đang diễn ra, Đã tham gia, Hoàn thành
 */
public class ChallengesFragment extends Fragment {

    private FragmentChallengesBinding binding;
    private ChallengesViewModel viewModel;
    private ChallengePagerAdapter pagerAdapter;

    private final String[] tabTitles = new String[3];

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChallengesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize tab titles
        tabTitles[0] = getString(R.string.tab_active);
        tabTitles[1] = getString(R.string.tab_joined);
        tabTitles[2] = getString(R.string.tab_completed);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ChallengesViewModel.class);

        // Setup ViewPager and TabLayout
        setupViewPager();

        // Observe ViewModel
        observeViewModel();

        // Load data
        viewModel.loadAllChallenges();
    }

    private void setupViewPager() {
        pagerAdapter = new ChallengePagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.loadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getResources().getColor(R.color.md_theme_light_primary, null))
                        .show();
                viewModel.clearSuccess();
            }
        });

        // Observe joined challenge for switching tab
        viewModel.getJoinedChallenge().observe(getViewLifecycleOwner(), challenge -> {
            if (challenge != null) {
                // Switch to "Đã tham gia" tab
                binding.viewPager.setCurrentItem(1, true);
                viewModel.clearJoinedChallenge();
            }
        });
    }

    /**
     * Show challenge detail dialog
     * Called from ChallengeListFragment
     */
    public void showChallengeDetailDialog(ChallengeData challenge) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_challenge_detail, null);

        // Find views
        TextView textChallengeIcon = dialogView.findViewById(R.id.text_challenge_icon);
        TextView textChallengeName = dialogView.findViewById(R.id.text_challenge_name);
        TextView textChallengeType = dialogView.findViewById(R.id.text_challenge_type);
        TextView textTimeRemaining = dialogView.findViewById(R.id.text_time_remaining);
        TextView textDescription = dialogView.findViewById(R.id.text_description);
        com.google.android.material.card.MaterialCardView cardProgress = dialogView.findViewById(R.id.card_progress);
        LinearProgressIndicator progressChallenge = dialogView.findViewById(R.id.progress_challenge);
        TextView textProgress = dialogView.findViewById(R.id.text_progress);
        TextView textProgressPercent = dialogView.findViewById(R.id.text_progress_percent);
        TextView textReward = dialogView.findViewById(R.id.text_reward);
        MaterialButton btnClose = dialogView.findViewById(R.id.btn_close);
        MaterialButton btnJoin = dialogView.findViewById(R.id.btn_join);

        // Set challenge icon
        String icon = getChallengeIcon(challenge.type, challenge.targetType);
        textChallengeIcon.setText(icon);

        // Set challenge name
        textChallengeName.setText(challenge.name);

        // Set type badge
        String typeText = "weekly".equals(challenge.type) 
                ? getString(R.string.challenge_type_weekly)
                : getString(R.string.challenge_type_monthly);
        textChallengeType.setText(typeText);

        // Set time remaining
        String timeRemaining = formatTimeRemaining(challenge.timeRemainingMs);
        textTimeRemaining.setText(timeRemaining);

        // Set description
        if (challenge.description != null && !challenge.description.isEmpty()) {
            textDescription.setText(challenge.description);
            textDescription.setVisibility(View.VISIBLE);
        } else {
            textDescription.setVisibility(View.GONE);
        }

        // Set progress
        if (challenge.joined || challenge.isCompleted) {
            cardProgress.setVisibility(View.VISIBLE);
            
            int progressPercent = calculateProgressPercent(challenge);
            progressChallenge.setProgress(progressPercent);
            
            String targetUnit = "points".equals(challenge.targetType) ? "điểm" : "hoạt động";
            String progressText = String.format("%d/%d %s", 
                    challenge.progress, challenge.targetValue, targetUnit);
            textProgress.setText(progressText);
            textProgressPercent.setText(progressPercent + "%");
        } else {
            cardProgress.setVisibility(View.GONE);
        }

        // Set reward text
        String rewardText = "+" + challenge.rewardPoints + " điểm";
        if (challenge.rewardBadge != null && challenge.rewardBadge.name != null) {
            rewardText += " + Huy hiệu " + challenge.rewardBadge.name;
        }
        textReward.setText(rewardText);

        // Create dialog
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Setup buttons
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Show join button only for available challenges
        if (!challenge.joined && !challenge.isCompleted) {
            btnJoin.setVisibility(View.VISIBLE);
            btnJoin.setOnClickListener(v -> {
                dialog.dismiss();
                viewModel.joinChallenge(challenge.id);
            });
        } else {
            btnJoin.setVisibility(View.GONE);
        }

        dialog.show();
    }

    /**
     * Calculate progress percentage
     */
    private int calculateProgressPercent(ChallengeData challenge) {
        if (challenge.isCompleted) {
            return 100;
        }
        if (challenge.progressPercent > 0) {
            return Math.min(100, challenge.progressPercent);
        }
        if (challenge.targetValue > 0) {
            return Math.min(100, (challenge.progress * 100) / challenge.targetValue);
        }
        return 0;
    }

    /**
     * Get challenge icon based on type
     */
    private String getChallengeIcon(String type, String targetType) {
        if ("weekly".equals(type)) {
            return "📅";
        } else if ("monthly".equals(type)) {
            return "🗓️";
        }
        
        if ("points".equals(targetType)) {
            return "⭐";
        } else if ("activities".equals(targetType)) {
            return "🎯";
        }
        
        return "🏆";
    }

    /**
     * Format time remaining in human readable format
     */
    private String formatTimeRemaining(long timeMs) {
        if (timeMs <= 0) {
            return getString(R.string.time_ended);
        }

        long days = TimeUnit.MILLISECONDS.toDays(timeMs);
        long hours = TimeUnit.MILLISECONDS.toHours(timeMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60;

        if (days > 0) {
            return getString(R.string.time_remaining_days, (int) days);
        } else if (hours > 0) {
            return getString(R.string.time_remaining_hours, (int) hours);
        } else {
            return getString(R.string.time_remaining_minutes, (int) minutes);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
