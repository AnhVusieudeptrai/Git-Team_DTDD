package com.example.app_ecotrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.AuthResponse;
import com.example.app_ecotrack.api.models.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullname, etUsername, etPassword, etEmail;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupAnimation() {
        // Animation removed - cardRegister not needed
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

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        RegisterRequest request = new RegisterRequest(username, password, fullname, email);
        ApiClient.getApiService().register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, 
                            "Đăng ký thành công! Vui lòng đăng nhập.", 
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, 
                            "Đăng ký thất bại! Tên đăng nhập hoặc email đã tồn tại.", 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        btnRegister.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
}
