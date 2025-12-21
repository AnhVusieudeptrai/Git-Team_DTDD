package com.example.app_ecotrack;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.app_ecotrack.adapters.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvUserName, tvPoints, tvLevel;
    private SharedPreferences prefs;
    private DatabaseHelper db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        db = new DatabaseHelper(this);
        userId = prefs.getInt("userId", -1);

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
        String fullname = prefs.getString("fullname", "User");
        int points = prefs.getInt("points", 0);
        int level = prefs.getInt("level", 1);

        // Update from database to get latest data
        Cursor cursor = db.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
            level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

            // Update SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("points", points);
            editor.putInt("level", level);
            editor.apply();

            cursor.close();
        }

        tvUserName.setText(fullname);
        tvPoints.setText(points + " điểm");
        tvLevel.setText("Cấp " + level);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        if (id == R.id.action_leaderboard) {
//            startActivity(new Intent(this, LeaderboardActivity.class));
//            return true;
//        } else if (id == R.id.action_rewards) {
//            startActivity(new Intent(this, RewardsActivity.class));
//            return true;
//        } else if (id == R.id.action_settings) {
//            startActivity(new Intent(this, SettingsActivity.class));
//            return true;
//        } else if (id == R.id.action_logout) {
//            logout();
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void logout() {
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