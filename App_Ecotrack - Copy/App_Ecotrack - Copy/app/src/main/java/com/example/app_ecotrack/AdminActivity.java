package com.example.app_ecotrack;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminActivity extends AppCompatActivity {
    private TextView tvAdminName, tvTotalUsers, tvTotalActivities, tvTotalCompleted, tvTotalPoints;
    private CardView cardManageActivities, cardManageUsers, cardStatistics, cardLogout;
    private DatabaseHelper db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);

        initViews();
        loadStats();
        setupClickListeners();
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
        cardLogout = findViewById(R.id.cardLogout);
    }

    private void loadStats() {
        String fullname = prefs.getString("fullname", "Admin");
        tvAdminName.setText("Xin chào, " + fullname);

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

        // Total points
        Cursor usersCursor = db.getAllUsers();
        int totalPoints = 0;
        if (usersCursor != null) {
            while (usersCursor.moveToNext()) {
                String role = usersCursor.getString(usersCursor.getColumnIndexOrThrow("role"));
                if (role.equals("user")) {
                    totalPoints += usersCursor.getInt(usersCursor.getColumnIndexOrThrow("points"));
                }
            }
            usersCursor.close();
        }
        tvTotalPoints.setText(String.valueOf(totalPoints));
    }

    private void setupClickListeners() {
        cardManageActivities.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManageActivitiesActivity.class));
        });

        cardManageUsers.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminManageUsersActivity.class));
        });

        cardStatistics.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminStatisticsActivity.class));
        });

        cardLogout.setOnClickListener(v -> logout());
    }

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
        loadStats();
    }
}