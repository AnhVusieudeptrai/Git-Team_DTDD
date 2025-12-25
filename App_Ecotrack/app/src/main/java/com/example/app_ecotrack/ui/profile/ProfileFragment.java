package com.example.app_ecotrack.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.BadgeData;
import com.example.app_ecotrack.api.models.UserData;
import com.example.app_ecotrack.databinding.DialogChangePasswordBinding;
import com.example.app_ecotrack.databinding.DialogEditProfileBinding;
import com.example.app_ecotrack.databinding.FragmentProfileBinding;
import com.example.app_ecotrack.ui.adapters.BadgePreviewAdapter;
import com.example.app_ecotrack.ui.auth.AuthActivity;
import com.example.app_ecotrack.viewmodel.ProfileViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * ProfileFragment - Thông tin cá nhân, settings
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private BadgePreviewAdapter badgeAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        setupUI();
        setupObservers();
        setupClickListeners();
        
        // Load profile data
        viewModel.loadProfileData();
    }

    private void setupUI() {
        // Setup badges RecyclerView
        badgeAdapter = new BadgePreviewAdapter();
        binding.recyclerBadges.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerBadges.setAdapter(badgeAdapter);
        
        // Set app version
        binding.textVersion.setText(getString(R.string.profile_version, "1.0.0"));
    }

    private void setupObservers() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.scrollContent.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        // Observe error state
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                binding.layoutError.setVisibility(View.VISIBLE);
                binding.scrollContent.setVisibility(View.GONE);
                binding.textError.setText(error);
            } else {
                binding.layoutError.setVisibility(View.GONE);
            }
        });

        // Observe user data
        viewModel.getUser().observe(getViewLifecycleOwner(), this::updateUserInfo);

        // Observe stats
        viewModel.getStats().observe(getViewLifecycleOwner(), this::updateStats);

        // Observe earned badges
        viewModel.getEarnedBadges().observe(getViewLifecycleOwner(), this::updateBadges);

        // Observe total badges count
        viewModel.getTotalBadges().observe(getViewLifecycleOwner(), count -> {
            if (count != null) {
                binding.textBadgesCount.setText(getString(R.string.profile_badges_count, count));
            }
        });

        // Observe update profile success
        viewModel.getUpdateProfileSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSnackbar(getString(R.string.profile_update_success));
                viewModel.clearUpdateProfileEvent();
            }
        });

        // Observe change password success
        viewModel.getChangePasswordSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSnackbar(getString(R.string.change_password_success));
                viewModel.clearChangePasswordEvent();
            }
        });

        // Observe logout event
        viewModel.getLogoutEvent().observe(getViewLifecycleOwner(), logout -> {
            if (logout != null && logout) {
                navigateToLogin();
                viewModel.clearLogoutEvent();
            }
        });
    }

    private void setupClickListeners() {
        // Retry button
        binding.btnRetry.setOnClickListener(v -> {
            viewModel.clearError();
            viewModel.loadProfileData();
        });

        // Edit profile button
        binding.btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Change password button
        binding.btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        // Logout button
        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());

        // View Stats button - navigate to stats screen
        binding.btnViewStats.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_stats);
        });

        // Badges card click - navigate to badges screen
        binding.cardBadges.setOnClickListener(v -> {
            // Navigate to badges tab in bottom navigation
            // This will be handled by the parent activity
        });
    }

    private void updateUserInfo(UserData user) {
        if (user == null) return;

        // Set fullname
        String displayName = user.fullname != null && !user.fullname.isEmpty() 
                ? user.fullname : user.username;
        binding.textFullname.setText(displayName);

        // Set email
        binding.textEmail.setText(user.email);

        // Set level
        binding.chipLevel.setText(getString(R.string.level_format, user.level));

        // Set total points
        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        binding.textTotalPoints.setText(numberFormat.format(user.points) + " " + getString(R.string.points));
    }

    private void updateStats(ProfileViewModel.ProfileStats stats) {
        if (stats == null) return;

        // Set total activities
        binding.textStatActivities.setText(String.valueOf(stats.totalActivities));

        // Set rank
        binding.textStatRank.setText("#" + stats.rank);

        // Set CO2 saved
        binding.textStatCo2.setText(String.format(Locale.getDefault(), "%.1fkg", stats.co2Saved));
    }

    private void updateBadges(List<BadgeData> badges) {
        if (badges == null || badges.isEmpty()) {
            binding.recyclerBadges.setVisibility(View.GONE);
            binding.textNoBadges.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerBadges.setVisibility(View.VISIBLE);
            binding.textNoBadges.setVisibility(View.GONE);
            badgeAdapter.setBadges(badges);
        }
    }

    /**
     * Show edit profile dialog
     * Requirements: 6.3
     */
    private void showEditProfileDialog() {
        DialogEditProfileBinding dialogBinding = DialogEditProfileBinding.inflate(getLayoutInflater());
        
        // Pre-fill current fullname
        UserData currentUser = viewModel.getUser().getValue();
        if (currentUser != null && currentUser.fullname != null) {
            dialogBinding.editFullname.setText(currentUser.fullname);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnSave.setOnClickListener(v -> {
            String fullname = dialogBinding.editFullname.getText().toString().trim();
            
            if (fullname.isEmpty()) {
                dialogBinding.tilFullname.setError(getString(R.string.error_fullname_required));
                return;
            }
            
            dialogBinding.tilFullname.setError(null);
            viewModel.updateProfile(fullname);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Show change password dialog
     * Requirements: 6.4, 6.5
     */
    private void showChangePasswordDialog() {
        DialogChangePasswordBinding dialogBinding = DialogChangePasswordBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogBinding.getRoot())
                .create();

        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnChange.setOnClickListener(v -> {
            String oldPassword = dialogBinding.editOldPassword.getText().toString();
            String newPassword = dialogBinding.editNewPassword.getText().toString();
            String confirmPassword = dialogBinding.editConfirmPassword.getText().toString();

            // Validate old password
            if (oldPassword.isEmpty()) {
                dialogBinding.tilOldPassword.setError(getString(R.string.error_old_password_required));
                return;
            }
            dialogBinding.tilOldPassword.setError(null);

            // Validate new password
            if (newPassword.isEmpty()) {
                dialogBinding.tilNewPassword.setError(getString(R.string.error_new_password_required));
                return;
            }
            if (newPassword.length() < 6) {
                dialogBinding.tilNewPassword.setError(getString(R.string.error_new_password_short));
                return;
            }
            dialogBinding.tilNewPassword.setError(null);

            // Validate confirm password
            if (confirmPassword.isEmpty()) {
                dialogBinding.tilConfirmPassword.setError(getString(R.string.error_confirm_password_required));
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                dialogBinding.tilConfirmPassword.setError(getString(R.string.error_new_password_mismatch));
                return;
            }
            dialogBinding.tilConfirmPassword.setError(null);

            viewModel.changePassword(oldPassword, newPassword);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Show logout confirmation dialog
     * Requirements: 6.6
     */
    private void showLogoutConfirmDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_message)
                .setPositiveButton(R.string.logout, (dialog, which) -> viewModel.logout())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /**
     * Navigate to login screen after logout
     */
    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
