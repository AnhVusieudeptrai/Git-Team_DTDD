package com.example.app_ecotrack;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CompoundButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {
    private SwitchCompat switchNotifications, switchDarkMode;
    private CardView cardLanguage, cardSecurity, cardSupport, cardAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        initViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("⚙️ Cài Đặt");
        }
    }

    private void initViews() {
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardSecurity = findViewById(R.id.cardSecurity);
        cardSupport = findViewById(R.id.cardSupport);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void setupListeners() {
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.widget.Toast.makeText(this,
                    isChecked ? "Bật thông báo" : "Tắt thông báo",
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            android.widget.Toast.makeText(this,
                    isChecked ? "Chế độ tối (Coming soon)" : "Chế độ sáng",
                    android.widget.Toast.LENGTH_SHORT).show();
        });

        cardLanguage.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Ngôn ngữ: Tiếng Việt", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardSecurity.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Đổi mật khẩu (Coming soon)", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardSupport.setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Email: support@ecotrack.com", android.widget.Toast.LENGTH_SHORT).show();
        });

        cardAbout.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Về EcoTrack")
                    .setMessage("EcoTrack v1.0.0\n\nỨng dụng theo dõi và khuyến khích lối sống xanh, bảo vệ môi trường.\n\n© 2024 EcoTrack Team")
                    .setPositiveButton("OK", null)
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}