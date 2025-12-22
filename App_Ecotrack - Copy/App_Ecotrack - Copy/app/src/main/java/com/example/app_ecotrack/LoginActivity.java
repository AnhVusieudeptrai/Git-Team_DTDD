package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.checkUser(username, password);
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));
            String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
            int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
            int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

            // Save session
            SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("userId", userId);
            editor.putString("username", username);
            editor.putString("fullname", fullname);
            editor.putString("role", role);
            editor.putInt("points", points);
            editor.putInt("level", level);
            editor.apply();

            cursor.close();

            Toast.makeText(this, "Chào mừng " + fullname + "!", Toast.LENGTH_SHORT).show();

            Intent intent;
            if (role.equals("admin")) {
                intent = new Intent(LoginActivity.this, AdminActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng", Toast.LENGTH_SHORT).show();
        }
    }
}