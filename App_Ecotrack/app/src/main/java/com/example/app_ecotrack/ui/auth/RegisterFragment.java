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
import com.example.app_ecotrack.databinding.FragmentRegisterBinding;
import com.example.app_ecotrack.viewmodel.AuthViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * RegisterFragment - Fragment xử lý đăng ký tài khoản mới
 * Requirements: 1.4 - Form với username, email, password, fullname
 *               Auto-login sau khi đăng ký thành công
 */
public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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

        // Register button click
        binding.btnRegister.setOnClickListener(v -> attemptRegister());

        // Handle keyboard done action
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });

        // Navigate to Login
        binding.tvLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Clear errors when user starts typing
        setupTextFieldFocusListeners();
    }

    private void setupTextFieldFocusListeners() {
        binding.etFullname.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilFullname.setError(null);
        });

        binding.etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilUsername.setError(null);
        });

        binding.etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilEmail.setError(null);
        });

        binding.etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilPassword.setError(null);
        });

        binding.etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) binding.tilConfirmPassword.setError(null);
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
                    // Auto-login: Navigate to MainActivity after successful registration
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

        // Observe error messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                viewModel.resetState();
            }
        });
    }

    /**
     * Thực hiện đăng ký
     * Requirement 1.4: Validation và error handling
     */
    private void attemptRegister() {
        // Clear previous errors
        clearErrors();

        String fullname = getTextFromEditText(binding.etFullname);
        String username = getTextFromEditText(binding.etUsername);
        String email = getTextFromEditText(binding.etEmail);
        String password = getTextFromEditText(binding.etPassword);
        String confirmPassword = getTextFromEditText(binding.etConfirmPassword);

        // Validate input
        boolean isValid = true;

        if (fullname.isEmpty()) {
            binding.tilFullname.setError(getString(R.string.error_fullname_required));
            isValid = false;
        }

        if (username.isEmpty()) {
            binding.tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        }

        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.error_email_invalid));
            isValid = false;
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.error_password_short));
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }

        if (isValid) {
            // Hide keyboard
            hideKeyboard();
            // Attempt registration
            viewModel.register(fullname, username, email, password);
        }
    }

    private void clearErrors() {
        binding.tilFullname.setError(null);
        binding.tilUsername.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private String getTextFromEditText(com.google.android.material.textfield.TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Hiển thị/ẩn loading state
     */
    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!isLoading);
        binding.btnRegister.setText(isLoading ? "" : getString(R.string.register));
        
        // Disable all input fields during loading
        binding.etFullname.setEnabled(!isLoading);
        binding.etUsername.setEnabled(!isLoading);
        binding.etEmail.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.etConfirmPassword.setEnabled(!isLoading);
        binding.tvLogin.setEnabled(!isLoading);
        binding.btnBack.setEnabled(!isLoading);
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
