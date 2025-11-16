package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminStatisticsActivity extends AppCompatActivity {
    private TextView tvTotalUsers, tvTotalActivities, tvTotalCompleted, tvTotalPoints, tvAvgPoints, tvAvgActivities;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);

        db = new DatabaseHelper(this);

        setupToolbar();
        initViews();
        loadStatistics();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("📊 Thống Kê Tổng Quan");
        }
    }

    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvTotalActivities = findViewById(R.id.tvTotalActivities);
        tvTotalCompleted = findViewById(R.id.tvTotalCompleted);
        tvTotalPoints = findViewById(R.id.tvTotalPoints);
        tvAvgPoints = findViewById(R.id.tvAvgPoints);
        tvAvgActivities = findViewById(R.id.tvAvgActivities);
    }

    private void loadStatistics() {
        // Total users
        int totalUsers = db.getTotalUsers();
        tvTotalUsers.setText(String.valueOf(totalUsers));

        // Total activities
        Cursor activitiesCursor = db.getAllActivities();
        int totalActivities = activitiesCursor != null ? activitiesCursor.getCount() : 0;
        if (activitiesCursor != null) activitiesCursor.close();
        tvTotalActivities.setText(String.valueOf(totalActivities));

        // Total completed
        int totalCompleted = db.getTotalActivitiesCompleted();
        tvTotalCompleted.setText(String.valueOf(totalCompleted));

        // Total points and averages
        Cursor usersCursor = db.getAllUsers();
        int totalPoints = 0;
        int userCount = 0;
        int totalUserActivities = 0;

        if (usersCursor != null) {
            while (usersCursor.moveToNext()) {
                String role = usersCursor.getString(usersCursor.getColumnIndexOrThrow("role"));
                if (role.equals("user")) {
                    int userId = usersCursor.getInt(usersCursor.getColumnIndexOrThrow("id"));
                    int points = usersCursor.getInt(usersCursor.getColumnIndexOrThrow("points"));
                    totalPoints += points;
                    userCount++;

                    Cursor actCursor = db.getUserActivities(userId);
                    if (actCursor != null) {
                        totalUserActivities += actCursor.getCount();
                        actCursor.close();
                    }
                }
            }
            usersCursor.close();
        }

        tvTotalPoints.setText(String.valueOf(totalPoints));

        if (userCount > 0) {
            int avgPoints = totalPoints / userCount;
            int avgActivities = totalUserActivities / userCount;
            tvAvgPoints.setText(avgPoints + " điểm/người");
            tvAvgActivities.setText(avgActivities + " hoạt động/người");
        } else {
            tvAvgPoints.setText("0 điểm/người");
            tvAvgActivities.setText("0 hoạt động/người");
        }
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