package com.example.app_ecotrack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ChallengeHomeAdapter - Adapter cho RecyclerView hiển thị thử thách trên Home screen
 * Hiển thị dạng horizontal với progress bar
 */
public class ChallengeHomeAdapter extends RecyclerView.Adapter<ChallengeHomeAdapter.ChallengeViewHolder> {

    private final List<ChallengeData> challenges = new ArrayList<>();
    private OnChallengeClickListener listener;

    public interface OnChallengeClickListener {
        void onChallengeClick(ChallengeData challenge);
    }

    public void setOnChallengeClickListener(OnChallengeClickListener listener) {
        this.listener = listener;
    }

    public void setChallenges(List<ChallengeData> newChallenges) {
        challenges.clear();
        if (newChallenges != null) {
            challenges.addAll(newChallenges);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge_home, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        holder.bind(challenges.get(position));
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    class ChallengeViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardChallenge;
        private final TextView textType;
        private final TextView textTimeRemaining;
        private final TextView textName;
        private final LinearProgressIndicator progressChallenge;
        private final TextView textProgress;
        private final TextView textProgressPercent;
        private final TextView textReward;

        ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardChallenge = itemView.findViewById(R.id.card_challenge);
            textType = itemView.findViewById(R.id.text_challenge_type);
            textTimeRemaining = itemView.findViewById(R.id.text_time_remaining);
            textName = itemView.findViewById(R.id.text_challenge_name);
            progressChallenge = itemView.findViewById(R.id.progress_challenge);
            textProgress = itemView.findViewById(R.id.text_progress);
            textProgressPercent = itemView.findViewById(R.id.text_progress_percent);
            textReward = itemView.findViewById(R.id.text_reward);
        }

        void bind(ChallengeData challenge) {
            // Set type badge
            String typeText = "weekly".equals(challenge.type) ? "Tuần" : "Tháng";
            textType.setText(typeText);

            // Set time remaining
            String timeRemaining = formatTimeRemaining(challenge.timeRemainingMs);
            textTimeRemaining.setText(timeRemaining);

            // Set name
            textName.setText(challenge.name);

            // Set progress
            progressChallenge.setProgress(challenge.progressPercent);

            // Set progress text
            String targetUnit = "points".equals(challenge.targetType) ? "điểm" : "hoạt động";
            String progressText = String.format("%d/%d %s", 
                    challenge.progress, challenge.targetValue, targetUnit);
            textProgress.setText(progressText);

            // Set progress percent
            textProgressPercent.setText(challenge.progressPercent + "%");

            // Set reward
            String rewardText = "+" + challenge.rewardPoints + " điểm";
            if (challenge.rewardBadge != null) {
                rewardText += " + Huy hiệu";
            }
            textReward.setText(rewardText);

            // Click listener
            cardChallenge.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onChallengeClick(challenge);
                }
            });
        }

        /**
         * Format time remaining in human readable format
         */
        private String formatTimeRemaining(long timeMs) {
            if (timeMs <= 0) {
                return "Đã kết thúc";
            }

            long days = TimeUnit.MILLISECONDS.toDays(timeMs);
            long hours = TimeUnit.MILLISECONDS.toHours(timeMs) % 24;

            if (days > 0) {
                return "Còn " + days + " ngày";
            } else if (hours > 0) {
                return "Còn " + hours + " giờ";
            } else {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60;
                return "Còn " + minutes + " phút";
            }
        }
    }
}
