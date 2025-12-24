package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

public class SettingsActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private SwitchCompat switchNotifications, switchDarkMode;
    private CardView cardLanguage, cardSecurity, cardSupport, cardAbout;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("EcoTrack", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews();
        loadSettings();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        cardLanguage = findViewById(R.id.cardLanguage);
        cardSecurity = findViewById(R.id.cardSecurity);
        cardSupport = findViewById(R.id.cardSupport);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void loadSettings() {
        switchNotifications.setChecked(prefs.getBoolean("notifications", true));
        switchDarkMode.setChecked(prefs.getBoolean("darkMode", false));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications", isChecked).apply();
            String msg = isChecked ? "Đã bật thông báo" : "Đã tắt thông báo";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("darkMode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        cardLanguage.setOnClickListener(v -> showLanguageDialog());
        cardSecurity.setOnClickListener(v -> showSecurityDialog());
        cardSupport.setOnClickListener(v -> showSupportDialog());
        cardAbout.setOnClickListener(v -> showAboutDialog());
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};
        int currentLang = prefs.getInt("language", 0);
        
        new AlertDialog.Builder(this)
                .setTitle("Chọn ngôn ngữ")
                .setSingleChoiceItems(languages, currentLang, (dialog, which) -> {
                    prefs.edit().putInt("language", which).apply();
                    Toast.makeText(this, "Đã chọn: " + languages[which], Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSecurityDialog() {
        String[] options = {"Đổi mật khẩu", "Chỉnh sửa thông tin", "Đăng xuất"};
        
        new AlertDialog.Builder(this)
                .setTitle("Bảo mật")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showChangePasswordDialog();
                            break;
                        case 1:
                            showEditProfileDialog();
                            break;
                        case 2:
                            logout();
                            break;
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText etOldPassword = new EditText(this);
        etOldPassword.setHint("Mật khẩu hiện tại");
        etOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etOldPassword);

        EditText etNewPassword = new EditText(this);
        etNewPassword.setHint("Mật khẩu mới");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        EditText etConfirmPassword = new EditText(this);
        etConfirmPassword.setHint("Xác nhận mật khẩu mới");
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(layout)
                .setPositiveButton("Đổi", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString().trim();
                    String newPass = etNewPassword.getText().toString().trim();
                    String confirmPass = etConfirmPassword.getText().toString().trim();

                    if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPass.length() < 6) {
                        Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!db.checkPassword(userId, oldPass)) {
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (db.updateUserPassword(userId, newPass)) {
                        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditProfileDialog() {
        Cursor cursor = db.getUserById(userId);
        String currentName = "";
        String currentEmail = "";
        
        if (cursor != null && cursor.moveToFirst()) {
            currentName = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
            currentEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            cursor.close();
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);

        EditText etFullname = new EditText(this);
        etFullname.setHint("Họ và tên");
        etFullname.setText(currentName);
        layout.addView(etFullname);

        EditText etEmail = new EditText(this);
        etEmail.setHint("Email");
        etEmail.setText(currentEmail);
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa thông tin")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String fullname = etFullname.getText().toString().trim();
                    String email = etEmail.getText().toString().trim();

                    if (fullname.isEmpty()) {
                        Toast.makeText(this, "Họ tên không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (db.updateUserProfile(userId, fullname, email)) {
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showSupportDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hỗ trợ")
                .setMessage("📧 Email: support@ecotrack.com\n\n📞 Hotline: 1900-xxxx\n\n🌐 Website: www.ecotrack.com\n\nChúng tôi luôn sẵn sàng hỗ trợ bạn!")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Giới thiệu EcoTrack")
                .setMessage("🌿 EcoTrack v1.0\n\nỨng dụng theo dõi và khuyến khích các hoạt động bảo vệ môi trường.\n\n✨ Tính năng:\n• Theo dõi hoạt động xanh\n• Tích điểm và đổi thưởng\n• Thử thách hàng ngày\n• Bảng xếp hạng\n\n💚 Hãy cùng nhau bảo vệ Trái Đất!")
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
