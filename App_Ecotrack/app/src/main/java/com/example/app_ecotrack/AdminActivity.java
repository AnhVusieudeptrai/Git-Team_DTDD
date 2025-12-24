package com.example.app_ecotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminActivity extends AppCompatActivity {
    private TextView tvAdminName, tvTotalUsers, tvTotalActivities, tvTotalCompleted, tvTotalPoints;
    private CardView cardManageActivities, cardManageUsers, cardStatistics, cardDatabaseManager, cardLogout;
    private DatabaseHelper db;
    private int adminId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);
        
        // Get admin info from session
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        adminId = prefs.getInt("userId", -1);
        String fullname = prefs.getString("fullname", "Admin");
        
        initViews();
        loadStatistics();
        setupListeners();
        
        tvAdminName.setText("Xin chào, " + fullname);
    }

    private void initViews() {
        tvAdminName = findViewById(R.id.tvAdminName);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalActivities = findViewById(R.id.tvTotalActivities);
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        
        cardManageActivities = findViewById(R.id.cardManageActivities);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardDatabaseManager = findViewById(R.id.cardDatabaseManager);
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void loadStatistics() {
        // Count total users (excluding admin)
        int totalUsers = db.getTotalUsers();
        tvTotalUsers.setText(String.valueOf(totalUsers));

        // Count total activities
        Cursor activitiesCursor = db.getAllActivities();
        int totalActivities = activitiesCursor != null ? activitiesCursor.getCount() : 0;
        tvTotalActivities.setText(String.valueOf(totalActivities));
        if (activitiesCursor != null) activitiesCursor.close();

        // Count completed activities
        int totalCompleted = db.getTotalCompletedActivities();
        tvTotalCompleted.setText(String.valueOf(totalCompleted));
        
        // Total points of all users
        int totalPoints = db.getTotalPointsAllUsers();
        tvTotalPoints.setText(String.valueOf(totalPoints));
    }

    private void setupListeners() {
        cardManageActivities.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminManageActivitiesActivity.class);
            startActivity(intent);
        });

        cardManageUsers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminManageUsersActivity.class);
            startActivity(intent);
        });

        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminStatisticsActivity.class);
            startActivity(intent);
        });

        cardDatabaseManager.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, DatabaseManagerActivity.class);
            startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatistics();
    }
}
