package com.example.app_ecotrack;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Auto navigate to LoginActivity after delay
        new android.os.Handler().postDelayed(() -> {
            startActivity(new android.content.Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}
