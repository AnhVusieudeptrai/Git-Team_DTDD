package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.AuthResponse;
import com.example.app_ecotrack.api.models.LoginRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== LoginActivity onCreate STARTED ===");
        
        setContentView(R.layout.activity_login);
        Log.d(TAG, "setContentView done");

        initViews();
        Log.d(TAG, "initViews done");
        
        setupListeners();
        Log.d(TAG, "setupListeners done");
        
        // Check if already logged in
        ApiClient.loadToken(this);
        String token = ApiClient.getAuthToken();
        Log.d(TAG, "Loaded token: " + (token != null ? "exists" : "null"));
        
        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "Token found, navigating to main");
            navigateToMain();
            return;
        }
        
        Log.d(TAG, "=== LoginActivity onCreate FINISHED - Ready for login ===");
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        Log.d(TAG, "Setting up listeners...");
        
        if (btnLogin != null) {
            Log.d(TAG, "btnLogin found: " + btnLogin.toString());
            Log.d(TAG, "btnLogin isEnabled: " + btnLogin.isEnabled());
            Log.d(TAG, "btnLogin isClickable: " + btnLogin.isClickable());
            
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "=== LOGIN BUTTON CLICKED ===");
                    Toast.makeText(LoginActivity.this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();
                    login();
                }
            });
            Log.d(TAG, "Click listener set successfully");
        } else {
            Log.e(TAG, "btnLogin is NULL!");
            Toast.makeText(this, "Lỗi: Không tìm thấy nút đăng nhập", Toast.LENGTH_LONG).show();
        }
        
        if (tvRegister != null) {
            tvRegister.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }
    }
    
    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        
        Log.d(TAG, "Attempting login with username: " + username);

        LoginRequest request = new LoginRequest(username, password);
        ApiClient.getApiService().login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                setLoading(false);
                
                Log.d(TAG, "Response code: " + response.code());
                
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "Login successful for: " + authResponse.user.fullname);
                    
                    // Save token
                    ApiClient.saveToken(LoginActivity.this, authResponse.token);
                    
                    // Save user info to SharedPreferences
                    saveUserInfo(authResponse);
                    
                    Toast.makeText(LoginActivity.this, 
                            "Chào mừng " + authResponse.user.fullname + "!", 
                            Toast.LENGTH_SHORT).show();
                    
                    // Navigate based on role
                    if ("admin".equals(authResponse.user.role)) {
                        startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    } else {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e(TAG, "Login failed. Code: " + response.code() + ", Error: " + errorBody);
                    Toast.makeText(LoginActivity.this, 
                            "Tên đăng nhập hoặc mật khẩu không đúng", 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, 
                        "Lỗi kết nối: " + t.getMessage(), 
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserInfo(AuthResponse authResponse) {
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Clear old data first to avoid type conflicts
        editor.clear();
        editor.putString("authToken", authResponse.token);
        editor.putString("userId", authResponse.user.id);
        editor.putString("username", authResponse.user.username);
        editor.putString("fullname", authResponse.user.fullname);
        editor.putString("email", authResponse.user.email);
        editor.putString("role", authResponse.user.role);
        editor.putInt("points", authResponse.user.points);
        editor.putInt("level", authResponse.user.level);
        editor.apply();
    }

    private void setLoading(boolean isLoading) {
        btnLogin.setEnabled(!isLoading);
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }

    private void navigateToMain() {
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        String role = prefs.getString("role", "user");
        
        if ("admin".equals(role)) {
            startActivity(new Intent(this, AdminActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }
}
