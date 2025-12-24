package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app_ecotrack.adapters.ViewPagerAdapter;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.ProfileResponse;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvUserName, tvPoints, tvLevel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        
        // Load auth token
        ApiClient.loadToken(this);

        initViews();
        setupToolbar();
        setupViewPager();
        loadUserInfo();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tvUserName = findViewById(R.id.tvUserName);
        tvPoints = findViewById(R.id.tvPoints);
        tvLevel = findViewById(R.id.tvLevel);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🌱 EcoTrack");
        }
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Trang chủ"); break;
                case 1: tab.setText("Hoạt động"); break;
                case 2: tab.setText("Thống kê"); break;
                case 3: tab.setText("Hồ sơ"); break;
            }
        }).attach();
    }

    private void loadUserInfo() {
        // Load from SharedPreferences first (for quick display)
        String fullname = prefs.getString("fullname", "User");
        int points = prefs.getInt("points", 0);
        int level = prefs.getInt("level", 1);

        tvUserName.setText(fullname);
        tvPoints.setText(points + " điểm");
        tvLevel.setText("Cấp " + level);

        // Then update from API
        ApiClient.getApiService().getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    
                    // Update UI
                    tvUserName.setText(profile.user.fullname);
                    tvPoints.setText(profile.user.points + " điểm");
                    tvLevel.setText("Cấp " + profile.user.level);

                    // Update SharedPreferences
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("points", profile.user.points);
                    editor.putInt("level", profile.user.level);
                    editor.putString("fullname", profile.user.fullname);
                    editor.apply();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                // Silent fail - use cached data
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_leaderboard) {
            startActivity(new Intent(this, LeaderboardActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Clear token and preferences
        ApiClient.clearAuthToken();
        prefs.edit().clear().apply();
        
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    public void refreshData() {
        loadUserInfo();
    }
}
