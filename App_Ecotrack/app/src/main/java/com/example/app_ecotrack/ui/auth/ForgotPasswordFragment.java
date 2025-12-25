package com.example.app_ecotrack.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.databinding.FragmentForgotPasswordBinding;
import com.example.app_ecotrack.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * ForgotPasswordFragment - Fragment xử lý quên mật khẩu
 * Requirements: 1.5, 1.6 - Email input và submit, Success/error feedback
 */
public class ForgotPasswordFragment extends Fragment {

    private FragmentForgotPasswordBinding binding;
    private AuthViewModel viewModel;
    private String currentEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Submit button click
        binding.btnSubmit.setOnClickListener(v -> attemptForgotPassword());

        // Handle keyboard done action
        binding.etEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptForgotPassword();
                return true;
            }
            return false;
        });

        // Back to Login link
        binding.tvBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Clear error when user starts typing
        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.tilEmail.setError(null);
            }
        });
    }

    private void observeViewModel() {
        // Observe auth state
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            switch (state) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                case ERROR:
                case IDLE:
                    showLoading(false);
                    break;
            }
        });

        // Observe forgot password success - Navigate to reset password screen
        viewModel.getForgotPasswordSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success && currentEmail != null) {
                navigateToResetPassword();
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.resetState();
            }
        });
    }

    /**
     * Thực hiện gửi yêu cầu đặt lại mật khẩu
     * Requirements: 1.5, 1.6
     */
    private void attemptForgotPassword() {
        // Clear previous errors
        binding.tilEmail.setError(null);
        binding.cardSuccess.setVisibility(View.GONE);

        String email = binding.etEmail.getText() != null ? 
                binding.etEmail.getText().toString().trim() : "";

        // Validate input
        boolean isValid = true;

        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        }

        if (isValid) {
            // Save email for navigation
            currentEmail = email;
            // Hide keyboard
            hideKeyboard();
            // Attempt forgot password
            viewModel.forgotPassword(email);
        }
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Hiển thị/ẩn loading state
     */
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!isLoading);
        binding.btnSubmit.setText(isLoading ? "" : getString(R.string.send_reset_link));
        binding.etEmail.setEnabled(!isLoading);
        binding.tvBackToLogin.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
    }

    /**
     * Navigate to reset password screen with email
     */
    private void navigateToResetPassword() {
        if (getView() != null && currentEmail != null) {
            // Show success message briefly
            binding.cardSuccess.setVisibility(View.VISIBLE);
            binding.tvSuccessMessage.setText(getString(R.string.reset_email_sent));
            
            // Navigate after short delay
            binding.getRoot().postDelayed(() -> {
                if (getView() != null) {
                    ForgotPasswordFragmentDirections.ActionForgotPasswordToResetPassword action =
                        ForgotPasswordFragmentDirections.actionForgotPasswordToResetPassword(currentEmail);
                    Navigation.findNavController(getView()).navigate(action);
                }
            }, 1000);
        }
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.md_theme_light_error, null))
                    .setTextColor(getResources().getColor(R.color.md_theme_light_onError, null))
                    .show();
        }
    }

    /**
     * Ẩn bàn phím
     */
    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            android.view.inputmethod.InputMethodManager imm = 
                (android.view.inputmethod.InputMethodManager) getActivity()
                    .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
