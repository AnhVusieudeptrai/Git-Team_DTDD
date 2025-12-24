package com.example.app_ecotrack;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class LeaderboardActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private LinearLayout containerLeaderboard;
    private DatabaseHelper db;

    // Top 3 views
    private TextView tvTop1Name, tvTop2Name, tvTop3Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        db = new DatabaseHelper(this);
        
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        containerLeaderboard = findViewById(R.id.containerLeaderboard);
        
        loadLeaderboard();
    }

    private void loadLeaderboard() {
        containerLeaderboard.removeAllViews();
        
        Cursor cursor = db.getLeaderboard();
        if (cursor != null && cursor.getCount() > 0) {
            int rank = 1;
            
            // Get top 3 card views from layout
            LinearLayout topCardsContainer = (LinearLayout) ((LinearLayout) findViewById(R.id.containerLeaderboard).getParent()).getChildAt(1);
            
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                int points = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                int level = cursor.getInt(cursor.getColumnIndexOrThrow("level"));
                int activityCount = db.getUserActivityCount(userId);

                // Update top 3 cards in header
                if (rank <= 3) {
                    updateTopCard(rank, fullname, points);
                }

                // Add to list
                View itemView = LayoutInflater.from(this).inflate(R.layout.item_leaderboard, containerLeaderboard, false);
                
                TextView tvRank = itemView.findViewById(R.id.tvRank);
                TextView tvName = itemView.findViewById(R.id.tvName);
                TextView tvLevel = itemView.findViewById(R.id.tvLevel);
                TextView tvActivities = itemView.findViewById(R.id.tvActivities);
                TextView tvPoints = itemView.findViewById(R.id.tvPoints);
                View highlightView = itemView.findViewById(R.id.highlightView);

                // Set rank with medal emoji for top 3
                String rankText;
                if (rank == 1) {
                    rankText = "🥇";
                    highlightView.setVisibility(View.VISIBLE);
                } else if (rank == 2) {
                    rankText = "🥈";
                } else if (rank == 3) {
                    rankText = "🥉";
                } else {
                    rankText = "#" + rank;
                }
                
                tvRank.setText(rankText);
                tvName.setText(fullname);
                tvLevel.setText("Cấp " + level);
                tvActivities.setText(activityCount + " hoạt động");
                tvPoints.setText(points + " điểm");

                containerLeaderboard.addView(itemView);
                rank++;
            }
            cursor.close();
        } else {
            // No users yet
            TextView emptyText = new TextView(this);
            emptyText.setText("Chưa có người dùng nào");
            emptyText.setTextSize(16);
            emptyText.setPadding(0, 32, 0, 32);
            containerLeaderboard.addView(emptyText);
        }
    }

    private void updateTopCard(int rank, String name, int points) {
        try {
            // Find the horizontal LinearLayout containing top 3 cards
            LinearLayout scrollContent = (LinearLayout) ((android.widget.ScrollView) 
                    findViewById(R.id.containerLeaderboard).getParent().getParent()).getChildAt(0);
            LinearLayout topCardsRow = (LinearLayout) scrollContent.getChildAt(1);
            
            int cardIndex;
            if (rank == 1) cardIndex = 1; // Middle card
            else if (rank == 2) cardIndex = 0; // Left card
            else cardIndex = 2; // Right card
            
            MaterialCardView card = (MaterialCardView) topCardsRow.getChildAt(cardIndex);
            LinearLayout cardContent = (LinearLayout) card.getChildAt(0);
            
            // Update name (3rd child - index 2)
            TextView tvName = (TextView) cardContent.getChildAt(2);
            tvName.setText(name);
        } catch (Exception e) {
            // Ignore if layout structure is different
        }
    }
}
