package com.example.app_ecotrack;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

public class RewardsActivity extends AppCompatActivity {
    private TextView tvUserPoints;
    private GridLayout gridRewards;
    private SharedPreferences prefs;
    private DatabaseHelper db;
    private int userId, userPoints;

    private static final String[][] REWARDS = {
            {"🛍️", "Túi vải EcoTrack", "100", "Túi vải thân thiện môi trường"},
            {"🍶", "Bình nước inox", "150", "Bình giữ nhiệt cao cấp"},
            {"🍱", "Hộp cơm tre", "200", "Hộp đựng thức ăn từ tre"},
            {"🎫", "Voucher 50k", "250", "Voucher mua sắm xanh"},
            {"🌵", "Cây xanh mini", "300", "Sen đá/xương rồng"},
            {"🥤", "Ống hút inox", "80", "Bộ ống hút thân thiện"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        userPoints = prefs.getInt("points", 0);

        setupToolbar();
        initViews();
        loadRewards();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🎁 Phần Thưởng");
        }
    }

    private void initViews() {
        tvUserPoints = findViewById(R.id.tvUserPoints);
        gridRewards = findViewById(R.id.gridRewards);
        tvUserPoints.setText("Điểm của bạn: " + userPoints);
    }

    private void loadRewards() {
        gridRewards.removeAllViews();

        for (String[] reward : REWARDS) {
            View rewardView = createRewardCard(reward[0], reward[1], Integer.parseInt(reward[2]), reward[3]);
            gridRewards.addView(rewardView);
        }
    }

    private View createRewardCard(String icon, String name, int points, String description) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_reward, gridRewards, false);

        TextView tvIcon = view.findViewById(R.id.tvRewardIcon);
        TextView tvName = view.findViewById(R.id.tvRewardName);
        TextView tvPoints = view.findViewById(R.id.tvRewardPoints);
        TextView tvDescription = view.findViewById(R.id.tvRewardDescription);
        Button btnRedeem = view.findViewById(R.id.btnRedeem);

        tvIcon.setText(icon);
        tvName.setText(name);
        tvPoints.setText(points + " điểm");
        tvDescription.setText(description);

        if (userPoints >= points) {
            btnRedeem.setEnabled(true);
            btnRedeem.setText("Đổi ngay");
        } else {
            btnRedeem.setEnabled(false);
            btnRedeem.setText("Chưa đủ điểm");
        }

        btnRedeem.setOnClickListener(v -> showRedeemDialog(icon, name, points, description));

        return view;
    }

    private void showRedeemDialog(String icon, String name, int points, String description) {
        new AlertDialog.Builder(this)
                .setTitle(icon + " " + name)
                .setMessage("Bạn có chắc muốn đổi phần thưởng này?\n\n" +
                        description + "\n\nĐiểm cần: " + points + "\n" +
                        "Điểm hiện tại: " + userPoints)
                .setPositiveButton("Đổi ngay", (dialog, which) -> redeemReward(name, points))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void redeemReward(String name, int points) {
        if (userPoints >= points) {
            int newPoints = userPoints - points;

            Cursor cursor = db.getUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                int currentPoints = cursor.getInt(cursor.getColumnIndexOrThrow("points"));
                int newLevel = (currentPoints - points) / 100 + 1;

                ContentValues values = new ContentValues();
                values.put("points", currentPoints - points);
                values.put("level", newLevel);

                SQLiteDatabase database = db.getWritableDatabase();
                database.update("users", values, "id=?", new String[]{String.valueOf(userId)});

                cursor.close();
            }

            userPoints = newPoints;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("points", newPoints);
            editor.apply();

            tvUserPoints.setText("Điểm của bạn: " + userPoints);
            loadRewards();

            Toast.makeText(this, "🎉 Đổi thưởng thành công: " + name + "!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Không đủ điểm!", Toast.LENGTH_SHORT).show();
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