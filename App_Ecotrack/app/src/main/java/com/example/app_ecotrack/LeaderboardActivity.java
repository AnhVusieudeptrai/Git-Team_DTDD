package com.example.app_ecotrack;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.LeaderboardResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardActivity extends AppCompatActivity {
    private LinearLayout containerLeaderboard;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        containerLeaderboard = findViewById(R.id.containerLeaderboard);
        
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("userId", "");

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        ApiClient.getApiService().getLeaderboard(20).enqueue(new Callback<LeaderboardResponse>() {
            @Override
            public void onResponse(Call<LeaderboardResponse> call, Response<LeaderboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayLeaderboard(response.body());
                } else {
                    Toast.makeText(LeaderboardActivity.this, "Không thể tải bảng xếp hạng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LeaderboardResponse> call, Throwable t) {
                Toast.makeText(LeaderboardActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayLeaderboard(LeaderboardResponse response) {
        containerLeaderboard.removeAllViews();

        if (response.leaderboard == null || response.leaderboard.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có dữ liệu xếp hạng");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setTextSize(16);
            containerLeaderboard.addView(emptyText);
            return;
        }

        int rank = 1;
        for (LeaderboardResponse.LeaderboardUser user : response.leaderboard) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_leaderboard, containerLeaderboard, false);

            TextView tvRank = itemView.findViewById(R.id.tvRank);
            TextView tvName = itemView.findViewById(R.id.tvName);
            TextView tvLevel = itemView.findViewById(R.id.tvLevel);
            TextView tvActivities = itemView.findViewById(R.id.tvActivities);
            TextView tvPoints = itemView.findViewById(R.id.tvPoints);
            View highlightView = itemView.findViewById(R.id.highlightView);
            LinearLayout rankBadge = itemView.findViewById(R.id.rankBadge);

            // Set rank with medal for top 3
            if (rank == 1) {
                tvRank.setText("🥇");
                rankBadge.setBackgroundResource(R.drawable.bg_rank_gold);
            } else if (rank == 2) {
                tvRank.setText("🥈");
                rankBadge.setBackgroundResource(R.drawable.bg_rank_silver);
            } else if (rank == 3) {
                tvRank.setText("🥉");
                rankBadge.setBackgroundResource(R.drawable.bg_rank_bronze);
            } else {
                tvRank.setText("#" + rank);
            }

            tvName.setText(user.fullname != null ? user.fullname : user.username);
            tvLevel.setText("🌟 Cấp " + user.level);
            tvActivities.setText("🌱 " + user.totalActivities + " HĐ");
            tvPoints.setText(String.valueOf(user.points));

            // Highlight current user
            if (user.id != null && user.id.equals(currentUserId)) {
                highlightView.setVisibility(View.VISIBLE);
            }

            containerLeaderboard.addView(itemView);
            rank++;
        }
    }
}
