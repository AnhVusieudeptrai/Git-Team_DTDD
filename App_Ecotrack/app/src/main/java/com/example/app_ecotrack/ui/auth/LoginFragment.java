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
import com.example.app_ecotrack.databinding.FragmentLoginBinding;
import com.example.app_ecotrack.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * LoginFragment - Fragment xử lý đăng nhập
 * Requirements: 1.1, 1.2, 1.3, 1.5, 1.7
 */
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
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
        // Login button click
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Handle keyboard done action
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // Navigate to Register - Requirement 1.3
        binding.tvRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_register);
        });

        // Navigate to Forgot Password - Requirement 1.5
        binding.tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_login_to_forgot_password);
        });

        // Clear errors when user starts typing
        binding.etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.tilUsername.setError(null);
            }
        });

        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.tilPassword.setError(null);
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
                    showLoading(false);
                    // Navigate to MainActivity
                    if (getActivity() instanceof AuthActivity) {
                        ((AuthActivity) getActivity()).navigateToMain();
                    }
                    break;
                case ERROR:
                    showLoading(false);
                    break;
                case IDLE:
                    showLoading(false);
                    break;
            }
        });

        // Observe error messages - Requirement 1.7: hiển thị lỗi không xóa password
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.resetState();
            }
        });
    }

    /**
     * Thực hiện đăng nhập
     * Requirements: 1.2, 1.7
     */
    private void attemptLogin() {
        // Clear previous errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        String username = binding.etUsername.getText() != null ? 
                binding.etUsername.getText().toString().trim() : "";
        String password = binding.etPassword.getText() != null ? 
                binding.etPassword.getText().toString() : "";

        // Validate input
        boolean isValid = true;

        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        }

        if (isValid) {
            // Hide keyboard
            hideKeyboard();
            // Attempt login
            viewModel.login(username, password);
        }
    }

    /**
     * Hiển thị/ẩn loading state
     */
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "" : getString(R.string.login));
        binding.etUsername.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.tvRegister.setEnabled(!isLoading);
        binding.tvForgotPassword.setEnabled(!isLoading);
    }

    /**
     * Hiển thị thông báo lỗi
     * Requirement 1.7: không xóa password field khi lỗi
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
