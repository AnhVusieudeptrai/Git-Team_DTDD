package com.example.app_ecotrack;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class LeaderboardActivity extends AppCompatActivity {
    private LinearLayout containerLeaderboard;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        setupToolbar();
        initViews();
        loadLeaderboard();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🏆 Bảng Xếp Hạng");
        }
    }

    private void initViews() {
        containerLeaderboard = findViewById(R.id.containerLeaderboard);
    }

    private void loadLeaderboard() {
        containerLeaderboard.removeAllViews();

        Cursor cursor = db.getLeaderboard();
        int rank = 1;

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));

                Cursor actCursor = db.getUserActivities(id);
                int activitiesCount = actCursor != null ? actCursor.getCount() : 0;
                if (actCursor != null) actCursor.close();

                boolean isCurrentUser = (id == currentUserId);

                View rankView = createRankView(rank, fullname, points, level, activitiesCount, isCurrentUser);
                containerLeaderboard.addView(rankView);

                rank++;
            } while (cursor.moveToNext());
            cursor.close();
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