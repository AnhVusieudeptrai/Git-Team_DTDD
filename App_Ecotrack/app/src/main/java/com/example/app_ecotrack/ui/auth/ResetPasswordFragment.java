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
import com.example.app_ecotrack.databinding.FragmentResetPasswordBinding;
import com.example.app_ecotrack.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * ResetPasswordFragment - Fragment nhập mã xác nhận và đặt lại mật khẩu mới
 */
public class ResetPasswordFragment extends Fragment {

    private FragmentResetPasswordBinding binding;
    private AuthViewModel viewModel;
    private String email;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get email from arguments
        if (getArguments() != null) {
            email = ResetPasswordFragmentArgs.fromBundle(getArguments()).getEmail();
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        // Back button
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Submit button
        binding.btnSubmit.setOnClickListener(v -> attemptResetPassword());

        // Keyboard done action
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptResetPassword();
                return true;
            }
            return false;
        });

        // Resend code
        binding.tvResendCode.setOnClickListener(v -> resendCode());

        // Back to login
        binding.tvBackToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(
                ResetPasswordFragmentDirections.actionResetPasswordToLogin()
            );
        });

        // Clear errors on focus
        binding.etResetCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilResetCode.setError(null);
        });
        binding.etNewPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilNewPassword.setError(null);
        });
        binding.etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilConfirmPassword.setError(null);
        });
    }

    private void observeViewModel() {
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

        viewModel.getResetPasswordSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSuccessAndNavigate();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.resetState();
            }
        });
    }

    private void attemptResetPassword() {
        // Clear errors
        binding.tilResetCode.setError(null);
        binding.tilNewPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String code = binding.etResetCode.getText() != null ? 
                binding.etResetCode.getText().toString().trim() : "";
        String newPassword = binding.etNewPassword.getText() != null ? 
                binding.etNewPassword.getText().toString() : "";
        String confirmPassword = binding.etConfirmPassword.getText() != null ? 
                binding.etConfirmPassword.getText().toString() : "";

        // Validate
        boolean isValid = true;

        if (code.isEmpty()) {
            binding.tilResetCode.setError(getString(R.string.error_code_required));
            isValid = false;
        } else if (code.length() != 6) {
            binding.tilResetCode.setError(getString(R.string.error_code_invalid));
            isValid = false;
        }

        if (newPassword.isEmpty()) {
            binding.tilNewPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (newPassword.length() < 6) {
            binding.tilNewPassword.setError(getString(R.string.error_password_min_length));
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.error_confirm_password_required));
            isValid = false;
        } else if (!confirmPassword.equals(newPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        if (isValid) {
            hideKeyboard();
            viewModel.resetPassword(email, code, newPassword);
        }
    }

    private void resendCode() {
        if (email != null && !email.isEmpty()) {
            viewModel.forgotPassword(email);
            showMessage(getString(R.string.code_resent));
        }
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnSubmit.setEnabled(!isLoading);
        binding.btnSubmit.setText(isLoading ? "" : getString(R.string.reset_password));
        binding.etResetCode.setEnabled(!isLoading);
        binding.etNewPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
        binding.tvResendCode.setEnabled(!isLoading);
        binding.tvBackToLogin.setEnabled(!isLoading);
    }

    private void showSuccessAndNavigate() {
        if (getView() != null) {
            Snackbar.make(getView(), R.string.password_reset_success, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.md_theme_light_primary, null))
                    .setTextColor(getResources().getColor(R.color.md_theme_light_onPrimary, null))
                    .show();
        }
        // Navigate to login after short delay
        binding.getRoot().postDelayed(() -> {
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(
                    ResetPasswordFragmentDirections.actionResetPasswordToLogin()
                );
            }
        }, 1500);
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.md_theme_light_error, null))
                    .setTextColor(getResources().getColor(R.color.md_theme_light_onError, null))
                    .show();
        }
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

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
