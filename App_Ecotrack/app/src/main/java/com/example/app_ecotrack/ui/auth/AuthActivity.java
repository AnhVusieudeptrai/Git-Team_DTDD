package com.example.app_ecotrack.ui.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.databinding.ActivityAuthBinding;
import com.example.app_ecotrack.services.EcoTrackMessagingService;
import com.example.app_ecotrack.ui.main.MainActivity;
import com.example.app_ecotrack.utils.FCMTokenManager;
import com.example.app_ecotrack.utils.TokenManager;

/**
 * AuthActivity - Activity chứa các fragment xác thực (Login, Register, ForgotPassword)
 * Sử dụng Navigation Component để điều hướng giữa các fragment
 * Requirements: 8.1
 */
public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create notification channels
        EcoTrackMessagingService.createNotificationChannels(this);
        
        // Request notification permission for Android 13+
        FCMTokenManager.requestNotificationPermission(this);
        
        // Kiểm tra nếu đã có token thì chuyển thẳng đến MainActivity
        TokenManager tokenManager = TokenManager.getInstance(this);
        if (tokenManager.hasToken()) {
            navigateToMain();
            return;
        }

        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_auth);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }
    }

    /**
     * Chuyển đến MainActivity sau khi đăng nhập thành công
     */
    public void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == FCMTokenManager.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, initialize FCM
                FCMTokenManager fcmTokenManager = new FCMTokenManager(this);
                fcmTokenManager.initialize();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
