package com.example.app_ecotrack;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class RewardsActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvUserPoints;
    private GridLayout gridRewards;
    private DatabaseHelper db;
    private int userId;
    private int userPoints;

    // Reward data
    private final String[][] rewards = {
            {"🌱", "Hạt giống cây", "Nhận hạt giống cây xanh", "50"},
            {"🎋", "Cây tre mini", "Cây tre để bàn làm việc", "100"},
            {"🧴", "Bình nước inox", "Bình giữ nhiệt thân thiện", "150"},
            {"🛍️", "Túi vải canvas", "Túi vải thời trang", "80"},
            {"📚", "Sách môi trường", "Sách về bảo vệ môi trường", "120"},
            {"🎁", "Voucher 50K", "Voucher mua sắm xanh", "200"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        db = new DatabaseHelper(this);
        
        SharedPreferences prefs = getSharedPreferences("EcoTrack", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvUserPoints = findViewById(R.id.tvUserPoints);
        gridRewards = findViewById(R.id.gridRewards);

        loadUserPoints();
        loadRewards();
    }

    private void loadUserPoints() {
        Cursor cursor = db.getUserById(userId);
        if (cursor != null && cursor.moveToFirst()) {
            userPoints = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
            tvUserPoints.setText("Điểm của bạn: " + userPoints + " ⭐");
            cursor.close();
        }
    }

    private void loadRewards() {
        gridRewards.removeAllViews();
        
        for (int i = 0; i < rewards.length; i++) {
            final String[] reward = rewards[i];
            final int requiredPoints = Integer.parseInt(reward[3]);
            
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_reward, gridRewards, false);
            
            TextView tvIcon = itemView.findViewById(R.id.tvRewardIcon);
            TextView tvName = itemView.findViewById(R.id.tvRewardName);
            TextView tvDesc = itemView.findViewById(R.id.tvRewardDescription);
            TextView tvPoints = itemView.findViewById(R.id.tvRewardPoints);
            Button btnRedeem = itemView.findViewById(R.id.btnRedeem);

            tvIcon.setText(reward[0]);
            tvName.setText(reward[1]);
            tvDesc.setText(reward[2]);
            tvPoints.setText(reward[3] + " điểm");

            // Check if user can afford
            if (userPoints >= requiredPoints) {
                btnRedeem.setEnabled(true);
                btnRedeem.setAlpha(1f);
                btnRedeem.setOnClickListener(v -> showRedeemDialog(reward[1], requiredPoints));
            } else {
                btnRedeem.setEnabled(false);
                btnRedeem.setAlpha(0.5f);
                btnRedeem.setText("Chưa đủ điểm");
            }

            // Set GridLayout params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(i % 2, 1f);
            params.rowSpec = GridLayout.spec(i / 2);
            params.setMargins(8, 8, 8, 8);
            itemView.setLayoutParams(params);

            gridRewards.addView(itemView);
        }
    }

    private void showRedeemDialog(String rewardName, int points) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đổi thưởng")
                .setMessage("Bạn có muốn đổi \"" + rewardName + "\" với " + points + " điểm?")
                .setPositiveButton("Đổi ngay", (dialog, which) -> {
                    redeemReward(rewardName, points);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void redeemReward(String rewardName, int points) {
        // Deduct points (negative value)
        if (db.updateUserPoints(userId, -points)) {
            Toast.makeText(this, "🎉 Đổi thưởng thành công!\nBạn đã nhận: " + rewardName, Toast.LENGTH_LONG).show();
            loadUserPoints();
            loadRewards();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserPoints();
        loadRewards();
    }
}
