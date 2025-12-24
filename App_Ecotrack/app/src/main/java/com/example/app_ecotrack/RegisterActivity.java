package com.example.app_ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullname, etUsername, etPassword, etEmail;
    private Button btnRegister;
    private TextView tvLogin;
    private CardView cardRegister;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        initViews();
        setupAnimation();
        setupListeners();
    }

    private void initViews() {
        etFullname = findViewById(R.id.etFullname);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        cardRegister = findViewById(R.id.cardRegister);
    }

    private void setupAnimation() {
        if (cardRegister != null) {
            cardRegister.setAlpha(0f);
            cardRegister.setTranslationY(100);
            cardRegister.animate().alpha(1f).translationY(0).setDuration(800).setStartDelay(200).start();
        }
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String fullname = etFullname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (fullname.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean result = db.insertUser(username, password, fullname, email);

        if (result) {
            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại! Tên đăng nhập đã tồn tại.", Toast.LENGTH_SHORT).show();
        }
    }
}