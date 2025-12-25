package com.example.app_ecotrack.ui.leaderboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.databinding.FragmentLeaderboardBinding;
import com.example.app_ecotrack.ui.adapters.LeaderboardAdapter;
import com.example.app_ecotrack.viewmodel.LeaderboardViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * LeaderboardFragment - Bảng xếp hạng với tabs (Tổng, Tuần này, Streak)
 */
public class LeaderboardFragment extends Fragment {

    private FragmentLeaderboardBinding binding;
    private LeaderboardViewModel viewModel;
    private LeaderboardAdapter adapter;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLeaderboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(LeaderboardViewModel.class);
        
        // Setup UI
        setupTabs();
        setupRecyclerView();
        setupRetryButton();
        
        // Observe data
        observeViewModel();
        
        // Load initial data
        viewModel.loadAllLeaderboards();
    }

    /**
     * Setup tab layout with 3 tabs
     */
    private void setupTabs() {
        // Add tabs
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_total));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_weekly));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_streak));

        // Tab selection listener
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                viewModel.setCurrentTab(position);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Refresh on reselect
                viewModel.refresh();
            }
        });
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        binding.recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerLeaderboard.setAdapter(adapter);
    }

    /**
     * Setup retry button
     */
    private void setupRetryButton() {
        binding.buttonRetry.setOnClickListener(v -> {
            binding.layoutError.setVisibility(View.GONE);
            viewModel.refresh();
        });
    }


    /**
     * Observe ViewModel LiveData
     */
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
                boolean hasData = hasCurrentTabData();
                if (!hasData) {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.textError.setText(error);
                } else {
                    // Show snackbar if we have data
                    Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
                }
                viewModel.clearError();
            }
        });

        // Observe current tab
        viewModel.getCurrentTab().observe(getViewLifecycleOwner(), tab -> {
            if (tab != null) {
                updateUIForTab(tab);
            }
        });

        // Observe global leaderboard
        viewModel.getGlobalLeaderboard().observe(getViewLifecycleOwner(), items -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_TOTAL) {
                updateLeaderboardList(items, LeaderboardViewModel.LeaderboardType.TOTAL);
            }
        });

        // Observe global current user
        viewModel.getGlobalCurrentUser().observe(getViewLifecycleOwner(), user -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_TOTAL && user != null) {
                updateCurrentUserCard(user);
            }
        });

        // Observe weekly leaderboard
        viewModel.getWeeklyLeaderboard().observe(getViewLifecycleOwner(), items -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_WEEKLY) {
                updateLeaderboardList(items, LeaderboardViewModel.LeaderboardType.WEEKLY);
            }
        });

        // Observe weekly current user
        viewModel.getWeeklyCurrentUser().observe(getViewLifecycleOwner(), user -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_WEEKLY && user != null) {
                updateCurrentUserCard(user);
            }
        });

        // Observe streak leaderboard
        viewModel.getStreakLeaderboard().observe(getViewLifecycleOwner(), items -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_STREAK) {
                updateLeaderboardList(items, LeaderboardViewModel.LeaderboardType.STREAK);
            }
        });

        // Observe streak current user
        viewModel.getStreakCurrentUser().observe(getViewLifecycleOwner(), user -> {
            Integer currentTab = viewModel.getCurrentTab().getValue();
            if (currentTab != null && currentTab == LeaderboardViewModel.TAB_STREAK && user != null) {
                updateCurrentUserCard(user);
            }
        });
    }

    /**
     * Update UI when tab changes
     */
    private void updateUIForTab(int tab) {
        binding.layoutError.setVisibility(View.GONE);
        
        switch (tab) {
            case LeaderboardViewModel.TAB_TOTAL:
                List<LeaderboardViewModel.LeaderboardItem> globalItems = viewModel.getGlobalLeaderboard().getValue();
                LeaderboardViewModel.CurrentUserRank globalUser = viewModel.getGlobalCurrentUser().getValue();
                updateLeaderboardList(globalItems, LeaderboardViewModel.LeaderboardType.TOTAL);
                if (globalUser != null) updateCurrentUserCard(globalUser);
                break;
                
            case LeaderboardViewModel.TAB_WEEKLY:
                List<LeaderboardViewModel.LeaderboardItem> weeklyItems = viewModel.getWeeklyLeaderboard().getValue();
                LeaderboardViewModel.CurrentUserRank weeklyUser = viewModel.getWeeklyCurrentUser().getValue();
                updateLeaderboardList(weeklyItems, LeaderboardViewModel.LeaderboardType.WEEKLY);
                if (weeklyUser != null) updateCurrentUserCard(weeklyUser);
                break;
                
            case LeaderboardViewModel.TAB_STREAK:
                List<LeaderboardViewModel.LeaderboardItem> streakItems = viewModel.getStreakLeaderboard().getValue();
                LeaderboardViewModel.CurrentUserRank streakUser = viewModel.getStreakCurrentUser().getValue();
                updateLeaderboardList(streakItems, LeaderboardViewModel.LeaderboardType.STREAK);
                if (streakUser != null) updateCurrentUserCard(streakUser);
                break;
        }
    }

    /**
     * Update leaderboard list
     */
    private void updateLeaderboardList(List<LeaderboardViewModel.LeaderboardItem> items, 
                                        LeaderboardViewModel.LeaderboardType type) {
        if (items != null && !items.isEmpty()) {
            binding.recyclerLeaderboard.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
            adapter.setLeaderboardType(type);
            adapter.setItems(items);
        } else {
            binding.recyclerLeaderboard.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update current user card
     */
    private void updateCurrentUserCard(LeaderboardViewModel.CurrentUserRank user) {
        binding.cardCurrentUser.setVisibility(View.VISIBLE);
        
        // Set rank
        binding.textCurrentRank.setText("#" + user.rank);
        
        // Set name
        binding.textCurrentName.setText(R.string.you);
        
        // Set level (hide for streak)
        if (user.type == LeaderboardViewModel.LeaderboardType.STREAK) {
            binding.textCurrentLevel.setVisibility(View.GONE);
        } else {
            binding.textCurrentLevel.setVisibility(View.VISIBLE);
            binding.textCurrentLevel.setText(getString(R.string.level_format, user.level));
        }
        
        // Set value with appropriate label
        String valueText = numberFormat.format(user.value);
        if (user.type == LeaderboardViewModel.LeaderboardType.STREAK) {
            valueText += " " + getString(R.string.streak_value);
        } else {
            valueText += " " + getString(R.string.points_value);
        }
        binding.textCurrentValue.setText(valueText);
    }

    /**
     * Check if current tab has data
     */
    private boolean hasCurrentTabData() {
        Integer tab = viewModel.getCurrentTab().getValue();
        if (tab == null) return false;
        
        switch (tab) {
            case LeaderboardViewModel.TAB_TOTAL:
                List<LeaderboardViewModel.LeaderboardItem> global = viewModel.getGlobalLeaderboard().getValue();
                return global != null && !global.isEmpty();
            case LeaderboardViewModel.TAB_WEEKLY:
                List<LeaderboardViewModel.LeaderboardItem> weekly = viewModel.getWeeklyLeaderboard().getValue();
                return weekly != null && !weekly.isEmpty();
            case LeaderboardViewModel.TAB_STREAK:
                List<LeaderboardViewModel.LeaderboardItem> streak = viewModel.getStreakLeaderboard().getValue();
                return streak != null && !streak.isEmpty();
            default:
                return false;
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
