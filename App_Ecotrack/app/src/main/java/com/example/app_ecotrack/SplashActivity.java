package com.example.app_ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app_ecotrack.ui.auth.AuthActivity;
import com.example.app_ecotrack.ui.main.MainActivity;
import com.example.app_ecotrack.utils.TokenManager;

/**
 * SplashActivity - Màn hình khởi động
 * Kiểm tra trạng thái đăng nhập và chuyển hướng phù hợp
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Delay then navigate
        new Handler(Looper.getMainLooper()).postDelayed(this::checkLoginAndNavigate, SPLASH_DELAY);
    }

    private void checkLoginAndNavigate() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        
        Intent intent;
        if (tokenManager.hasToken()) {
            // User is logged in, go to main
            intent = new Intent(this, MainActivity.class);
        } else {
            // User not logged in, go to auth
            intent = new Intent(this, AuthActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
