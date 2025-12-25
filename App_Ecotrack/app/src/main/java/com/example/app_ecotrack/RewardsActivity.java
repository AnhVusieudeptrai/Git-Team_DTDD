package com.example.app_ecotrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.ProfileResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardsActivity extends AppCompatActivity {
    private TextView tvUserPoints;
    private GridLayout gridRewards;
    private int userPoints = 0;

    private static final Reward[] REWARDS = {
            new Reward("🌳", "Trồng 1 cây", "Góp phần trồng 1 cây xanh", 100),
            new Reward("☕", "Voucher cafe", "Giảm 20% tại quán cafe xanh", 150),
            new Reward("🚌", "Vé xe buýt", "1 vé xe buýt miễn phí", 80),
            new Reward("🛍️", "Túi vải", "Túi vải thân thiện môi trường", 200),
            new Reward("📖", "Sách xanh", "Sách về bảo vệ môi trường", 250),
            new Reward("🎁", "Quà bất ngờ", "Phần quà đặc biệt", 500)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Phần Thưởng");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvUserPoints = findViewById(R.id.tvUserPoints);
        gridRewards = findViewById(R.id.gridRewards);

        loadUserPoints();
        displayRewards();
    }

    private void loadUserPoints() {
        SharedPreferences prefs = getSharedPreferences("EcoTrackPrefs", MODE_PRIVATE);
        try {
            userPoints = prefs.getInt("points", 0);
        } catch (ClassCastException e) {
            String pointsStr = prefs.getString("points", "0");
            try {
                userPoints = Integer.parseInt(pointsStr);
            } catch (NumberFormatException ex) {
                userPoints = 0;
            }
        }
        tvUserPoints.setText("🌟 Điểm của bạn: " + userPoints);

        // Also load from API
        ApiClient.getApiService().getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userPoints = response.body().user.points;
                    tvUserPoints.setText("🌟 Điểm của bạn: " + userPoints);
                    displayRewards();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                // Use cached points
            }
        });
    }

    private void displayRewards() {
        gridRewards.removeAllViews();

        for (Reward reward : REWARDS) {
            View rewardView = createRewardView(reward);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            rewardView.setLayoutParams(params);
            gridRewards.addView(rewardView);
        }
    }

    private View createRewardView(Reward reward) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_reward, gridRewards, false);

        TextView tvIcon = view.findViewById(R.id.tvRewardIcon);
        TextView tvName = view.findViewById(R.id.tvRewardName);
        TextView tvDesc = view.findViewById(R.id.tvRewardDesc);
        TextView tvCost = view.findViewById(R.id.tvRewardCost);
        MaterialButton btnRedeem = view.findViewById(R.id.btnRedeem);
        MaterialCardView card = (MaterialCardView) view;

        tvIcon.setText(reward.icon);
        tvName.setText(reward.name);
        tvDesc.setText(reward.description);
        tvCost.setText(reward.cost + " điểm");

        boolean canAfford = userPoints >= reward.cost;
        btnRedeem.setEnabled(canAfford);
        btnRedeem.setAlpha(canAfford ? 1.0f : 0.5f);

        btnRedeem.setOnClickListener(v -> {
            if (canAfford) {
                showRedeemDialog(reward);
            } else {
                Toast.makeText(this, "Bạn cần thêm " + (reward.cost - userPoints) + " điểm", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void showRedeemDialog(Reward reward) {
        new AlertDialog.Builder(this)
                .setTitle("Đổi phần thưởng")
                .setMessage("Bạn có muốn đổi " + reward.cost + " điểm để nhận \"" + reward.name + "\"?")
                .setPositiveButton("Đổi ngay", (dialog, which) -> {
                    // In a real app, this would call an API to redeem
                    Toast.makeText(this, "🎉 Đổi thành công! " + reward.name, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private static class Reward {
        String icon;
        String name;
        String description;
        int cost;

        Reward(String icon, String name, String description, int cost) {
            this.icon = icon;
            this.name = name;
            this.description = description;
            this.cost = cost;
        }
    }
}
