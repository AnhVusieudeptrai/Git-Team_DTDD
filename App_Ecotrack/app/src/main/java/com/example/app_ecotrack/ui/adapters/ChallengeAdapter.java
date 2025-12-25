package com.example.app_ecotrack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ChallengeAdapter - Adapter cho RecyclerView hiển thị danh sách thử thách
 * Hỗ trợ 3 modes: ACTIVE (available), JOINED, COMPLETED
 */
public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    public enum Mode {
        ACTIVE,     // Available challenges to join
        JOINED,     // User's active challenges
        COMPLETED   // User's completed challenges
    }

    private final List<ChallengeData> challenges = new ArrayList<>();
    private Mode mode = Mode.ACTIVE;
    private OnChallengeClickListener clickListener;
    private OnJoinClickListener joinListener;

    public interface OnChallengeClickListener {
        void onChallengeClick(ChallengeData challenge);
    }

    public interface OnJoinClickListener {
        void onJoinClick(ChallengeData challenge);
    }

    public void setOnChallengeClickListener(OnChallengeClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnJoinClickListener(OnJoinClickListener listener) {
        this.joinListener = listener;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
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
                .inflate(R.layout.item_challenge, parent, false);
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
        private final TextView textChallengeIcon;
        private final TextView textChallengeName;
        private final TextView textChallengeType;
        private final TextView textTimeRemaining;
        private final MaterialButton btnJoin;
        private final TextView textDescription;
        private final LinearLayout layoutProgress;
        private final LinearProgressIndicator progressChallenge;
        private final TextView textProgress;
        private final TextView textProgressPercent;
        private final TextView textReward;
        private final TextView textCompletedBadge;

        ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardChallenge = itemView.findViewById(R.id.card_challenge);
            textChallengeIcon = itemView.findViewById(R.id.text_challenge_icon);
            textChallengeName = itemView.findViewById(R.id.text_challenge_name);
            textChallengeType = itemView.findViewById(R.id.text_challenge_type);
            textTimeRemaining = itemView.findViewById(R.id.text_time_remaining);
            btnJoin = itemView.findViewById(R.id.btn_join);
            textDescription = itemView.findViewById(R.id.text_description);
            layoutProgress = itemView.findViewById(R.id.layout_progress);
            progressChallenge = itemView.findViewById(R.id.progress_challenge);
            textProgress = itemView.findViewById(R.id.text_progress);
            textProgressPercent = itemView.findViewById(R.id.text_progress_percent);
            textReward = itemView.findViewById(R.id.text_reward);
            textCompletedBadge = itemView.findViewById(R.id.text_completed_badge);
        }

        void bind(ChallengeData challenge) {
            // Set challenge icon based on type
            String icon = getChallengeIcon(challenge.type, challenge.targetType);
            textChallengeIcon.setText(icon);

            // Set challenge name
            textChallengeName.setText(challenge.name);

            // Set type badge
            String typeText = "weekly".equals(challenge.type) 
                    ? itemView.getContext().getString(R.string.challenge_type_weekly)
                    : itemView.getContext().getString(R.string.challenge_type_monthly);
            textChallengeType.setText(typeText);

            // Set time remaining
            String timeRemaining = formatTimeRemaining(challenge.timeRemainingMs);
            textTimeRemaining.setText(timeRemaining);

            // Set description
            if (challenge.description != null && !challenge.description.isEmpty()) {
                textDescription.setText(challenge.description);
                textDescription.setVisibility(View.VISIBLE);
            } else {
                textDescription.setVisibility(View.GONE);
            }

            // Set reward text
            String rewardText = "+" + challenge.rewardPoints + " điểm";
            if (challenge.rewardBadge != null && challenge.rewardBadge.name != null) {
                rewardText += " + Huy hiệu " + challenge.rewardBadge.name;
            }
            textReward.setText(rewardText);

            // Handle different modes
            switch (mode) {
                case ACTIVE:
                    setupActiveMode(challenge);
                    break;
                case JOINED:
                    setupJoinedMode(challenge);
                    break;
                case COMPLETED:
                    setupCompletedMode(challenge);
                    break;
            }

            // Click listener for card
            cardChallenge.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onChallengeClick(challenge);
                }
            });
        }

        private void setupActiveMode(ChallengeData challenge) {
            // Show join button
            btnJoin.setVisibility(View.VISIBLE);
            btnJoin.setText(R.string.join_challenge);
            btnJoin.setEnabled(true);
            btnJoin.setOnClickListener(v -> {
                if (joinListener != null) {
                    joinListener.onJoinClick(challenge);
                }
            });

            // Hide progress for available challenges
            layoutProgress.setVisibility(View.GONE);
            textCompletedBadge.setVisibility(View.GONE);
        }

        private void setupJoinedMode(ChallengeData challenge) {
            // Hide join button
            btnJoin.setVisibility(View.GONE);

            // Show progress
            layoutProgress.setVisibility(View.VISIBLE);
            textCompletedBadge.setVisibility(View.GONE);

            // Set progress bar
            int progressPercent = calculateProgressPercent(challenge);
            progressChallenge.setProgress(progressPercent);

            // Set progress text
            String targetUnit = "points".equals(challenge.targetType) ? "điểm" : "hoạt động";
            String progressText = String.format("%d/%d %s", 
                    challenge.progress, challenge.targetValue, targetUnit);
            textProgress.setText(progressText);
            textProgressPercent.setText(progressPercent + "%");
        }

        private void setupCompletedMode(ChallengeData challenge) {
            // Hide join button
            btnJoin.setVisibility(View.GONE);

            // Show completed progress (100%)
            layoutProgress.setVisibility(View.VISIBLE);
            progressChallenge.setProgress(100);
            
            String targetUnit = "points".equals(challenge.targetType) ? "điểm" : "hoạt động";
            String progressText = String.format("%d/%d %s", 
                    challenge.targetValue, challenge.targetValue, targetUnit);
            textProgress.setText(progressText);
            textProgressPercent.setText("100%");

            // Show completed badge
            textCompletedBadge.setVisibility(View.VISIBLE);
        }

        /**
         * Calculate progress percentage
         */
        private int calculateProgressPercent(ChallengeData challenge) {
            if (challenge.progressPercent > 0) {
                return Math.min(100, challenge.progressPercent);
            }
            if (challenge.targetValue > 0) {
                return Math.min(100, (challenge.progress * 100) / challenge.targetValue);
            }
            return 0;
        }

        /**
         * Get challenge icon based on type
         */
        private String getChallengeIcon(String type, String targetType) {
            if ("weekly".equals(type)) {
                return "📅";
            } else if ("monthly".equals(type)) {
                return "🗓️";
            }
            
            if ("points".equals(targetType)) {
                return "⭐";
            } else if ("activities".equals(targetType)) {
                return "🎯";
            }
            
            return "🏆";
        }

        /**
         * Format time remaining in human readable format
         */
        private String formatTimeRemaining(long timeMs) {
            if (timeMs <= 0) {
                return itemView.getContext().getString(R.string.time_ended);
            }

            long days = TimeUnit.MILLISECONDS.toDays(timeMs);
            long hours = TimeUnit.MILLISECONDS.toHours(timeMs) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60;

            if (days > 0) {
                return itemView.getContext().getString(R.string.time_remaining_days, (int) days);
            } else if (hours > 0) {
                return itemView.getContext().getString(R.string.time_remaining_hours, (int) hours);
            } else {
                return itemView.getContext().getString(R.string.time_remaining_minutes, (int) minutes);
            }
        }
    }
}
