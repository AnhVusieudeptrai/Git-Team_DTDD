package com.example.app_ecotrack.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.BadgeData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * BadgeAdapter - Adapter cho RecyclerView hiển thị danh sách huy hiệu
 * Hỗ trợ earned/unearned status và rarity colors
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<BadgeData> badges = new ArrayList<>();
    private OnBadgeClickListener listener;

    public interface OnBadgeClickListener {
        void onBadgeClick(BadgeData badge);
    }

    public void setOnBadgeClickListener(OnBadgeClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set badges list
     */
    public void setBadges(List<BadgeData> badgeList) {
        badges.clear();
        if (badgeList != null) {
            badges.addAll(badgeList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeData badge = badges.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badges.size();
    }

    /**
     * ViewHolder for badge items
     */
    class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardBadge;
        private final FrameLayout frameBadgeIcon;
        private final View viewRarityBorder;
        private final TextView textBadgeIcon;
        private final ImageView iconLock;
        private final TextView textBadgeName;
        private final TextView textRarity;
        private final LinearLayout layoutProgress;
        private final LinearProgressIndicator progressBadge;
        private final TextView textProgress;

        BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardBadge = itemView.findViewById(R.id.card_badge);
            frameBadgeIcon = itemView.findViewById(R.id.frame_badge_icon);
            viewRarityBorder = itemView.findViewById(R.id.view_rarity_border);
            textBadgeIcon = itemView.findViewById(R.id.text_badge_icon);
            iconLock = itemView.findViewById(R.id.icon_lock);
            textBadgeName = itemView.findViewById(R.id.text_badge_name);
            textRarity = itemView.findViewById(R.id.text_rarity);
            layoutProgress = itemView.findViewById(R.id.layout_progress);
            progressBadge = itemView.findViewById(R.id.progress_badge);
            textProgress = itemView.findViewById(R.id.text_progress);
        }

        void bind(BadgeData badge) {
            // Set badge icon
            String icon = badge.icon != null ? badge.icon : "🏆";
            textBadgeIcon.setText(icon);

            // Set badge name
            textBadgeName.setText(badge.name);

            // Get rarity color
            int rarityColor = getRarityColor(badge.rarity);
            String rarityText = getRarityText(badge.rarity);

            // Set rarity text and background
            textRarity.setText(rarityText);
            GradientDrawable rarityBackground = new GradientDrawable();
            rarityBackground.setShape(GradientDrawable.RECTANGLE);
            rarityBackground.setCornerRadius(dpToPx(4));
            rarityBackground.setColor(ContextCompat.getColor(itemView.getContext(), rarityColor));
            textRarity.setBackground(rarityBackground);

            // Set rarity border color
            GradientDrawable borderDrawable = (GradientDrawable) viewRarityBorder.getBackground();
            if (borderDrawable != null) {
                borderDrawable.setStroke(dpToPx(3), ContextCompat.getColor(itemView.getContext(), rarityColor));
            }

            // Handle earned/unearned state
            if (badge.earned) {
                // Earned badge - full color
                cardBadge.setAlpha(1.0f);
                textBadgeIcon.setAlpha(1.0f);
                iconLock.setVisibility(View.GONE);
                layoutProgress.setVisibility(View.GONE);
                
                // Set card stroke color to rarity
                cardBadge.setStrokeColor(ContextCompat.getColor(itemView.getContext(), rarityColor));
                cardBadge.setStrokeWidth(dpToPx(2));
            } else {
                // Unearned badge - grayed out
                cardBadge.setAlpha(0.7f);
                textBadgeIcon.setAlpha(0.5f);
                iconLock.setVisibility(View.VISIBLE);
                
                // Show progress if available
                if (badge.progress > 0 || badge.progressPercent > 0) {
                    layoutProgress.setVisibility(View.VISIBLE);
                    int progress = badge.progressPercent > 0 ? badge.progressPercent : 
                            (badge.requirement > 0 ? (badge.progress * 100 / badge.requirement) : 0);
                    progressBadge.setProgress(Math.min(100, progress));
                    progressBadge.setIndicatorColor(ContextCompat.getColor(itemView.getContext(), rarityColor));
                    textProgress.setText(badge.progress + "/" + badge.requirement);
                } else {
                    layoutProgress.setVisibility(View.GONE);
                }
                
                // Remove stroke for unearned
                cardBadge.setStrokeWidth(0);
            }

            // Click listener
            cardBadge.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBadgeClick(badge);
                }
            });
        }

        /**
         * Get rarity color resource
         */
        private int getRarityColor(String rarity) {
            if (rarity == null) return R.color.rarity_common;
            switch (rarity) {
                case "rare": return R.color.rarity_rare;
                case "epic": return R.color.rarity_epic;
                case "legendary": return R.color.rarity_legendary;
                default: return R.color.rarity_common;
            }
        }

        /**
         * Get rarity text in Vietnamese
         */
        private String getRarityText(String rarity) {
            if (rarity == null) return itemView.getContext().getString(R.string.rarity_common);
            switch (rarity) {
                case "rare": return itemView.getContext().getString(R.string.rarity_rare);
                case "epic": return itemView.getContext().getString(R.string.rarity_epic);
                case "legendary": return itemView.getContext().getString(R.string.rarity_legendary);
                default: return itemView.getContext().getString(R.string.rarity_common);
            }
        }

        /**
         * Convert dp to pixels
         */
        private int dpToPx(int dp) {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        }
    }
}
