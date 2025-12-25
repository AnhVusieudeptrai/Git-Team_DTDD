package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.app_ecotrack.api.ApiClient;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchNotifications, switchDarkMode;
    private CardView cardLanguage, cardSecurity, cardSupport, cardAbout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Cài Đặt");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardSecurity = findViewById(R.id.cardSecurity);
        cardSupport = findViewById(R.id.cardSupport);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void loadSettings() {
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        boolean darkModeEnabled = prefs.getBoolean("dark_mode_enabled", false);

        switchNotifications.setChecked(notificationsEnabled);
        switchDarkMode.setChecked(darkModeEnabled);
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            Toast.makeText(this, isChecked ? "Đã bật thông báo" : "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("dark_mode_enabled", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        cardLanguage.setOnClickListener(v -> showLanguageDialog());

        cardSecurity.setOnClickListener(v -> showSecurityDialog());

        cardSupport.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@ecotrack.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Hỗ trợ EcoTrack");
            try {
                startActivity(Intent.createChooser(intent, "Gửi email"));
            } catch (Exception e) {
                Toast.makeText(this, "Không tìm thấy ứng dụng email", Toast.LENGTH_SHORT).show();
            }
        });

        cardAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};
        int currentLanguage = prefs.getInt("language", 0);

        new AlertDialog.Builder(this)
                .setTitle("Chọn ngôn ngữ")
                .setSingleChoiceItems(languages, currentLanguage, (dialog, which) -> {
                    prefs.edit().putInt("language", which).apply();
                    Toast.makeText(this, "Đã chọn: " + languages[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSecurityDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🔒 Bảo mật")
                .setMessage("Tài khoản của bạn được bảo vệ bằng mật khẩu.\n\nĐể đổi mật khẩu, vui lòng liên hệ hỗ trợ.")
                .setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
                    Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Đăng xuất tất cả thiết bị", (dialog, which) -> {
                    showLogoutAllDialog();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void showLogoutAllDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất tất cả thiết bị")
                .setMessage("Bạn có chắc muốn đăng xuất khỏi tất cả thiết bị?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    logout();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("🌿 Về EcoTrack")
                .setMessage("EcoTrack v1.0\n\n" +
                        "Ứng dụng theo dõi và khuyến khích các hoạt động bảo vệ môi trường.\n\n" +
                        "🌱 Hành động xanh - Tương lai bền vững\n\n" +
                        "© 2024 EcoTrack Team")
                .setPositiveButton("OK", null)
                .show();
    }

    private void logout() {
        ApiClient.clearAuthToken();
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
