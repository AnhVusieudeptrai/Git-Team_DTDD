package com.example.app_ecotrack.ui.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.viewmodel.LeaderboardViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * LeaderboardAdapter - Adapter cho RecyclerView hiển thị bảng xếp hạng
 * Hỗ trợ highlight current user và medal cho top 3
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private final List<LeaderboardViewModel.LeaderboardItem> items = new ArrayList<>();
    private LeaderboardViewModel.LeaderboardType currentType = LeaderboardViewModel.LeaderboardType.TOTAL;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /**
     * Set leaderboard items
     */
    public void setItems(List<LeaderboardViewModel.LeaderboardItem> itemList) {
        items.clear();
        if (itemList != null) {
            items.addAll(itemList);
        }
        notifyDataSetChanged();
    }

    /**
     * Set current leaderboard type (for value label)
     */
    public void setLeaderboardType(LeaderboardViewModel.LeaderboardType type) {
        this.currentType = type;
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        LeaderboardViewModel.LeaderboardItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    /**
     * ViewHolder for leaderboard items
     */
    class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardItem;
        private final FrameLayout frameRank;
        private final ImageView imageMedal;
        private final TextView textRank;
        private final ShapeableImageView imageAvatar;
        private final TextView textName;
        private final TextView textLevel;
        private final TextView textValue;
        private final TextView textValueLabel;

        LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.card_leaderboard_item);
            frameRank = itemView.findViewById(R.id.text_rank).getParent() instanceof FrameLayout 
                    ? (FrameLayout) itemView.findViewById(R.id.text_rank).getParent() 
                    : null;
            imageMedal = itemView.findViewById(R.id.image_medal);
            textRank = itemView.findViewById(R.id.text_rank);
            imageAvatar = itemView.findViewById(R.id.image_avatar);
            textName = itemView.findViewById(R.id.text_name);
            textLevel = itemView.findViewById(R.id.text_level);
            textValue = itemView.findViewById(R.id.text_value);
            textValueLabel = itemView.findViewById(R.id.text_value_label);
        }

        void bind(LeaderboardViewModel.LeaderboardItem item) {
            // Set rank with special styling for top 3
            setupRank(item.rank);

            // Set user name
            textName.setText(item.getDisplayName());

            // Set level (hide for streak leaderboard)
            if (item.type == LeaderboardViewModel.LeaderboardType.STREAK) {
                textLevel.setVisibility(View.GONE);
            } else {
                textLevel.setVisibility(View.VISIBLE);
                textLevel.setText(itemView.getContext().getString(R.string.level_format, item.level));
            }

            // Set value (points or streak days)
            textValue.setText(numberFormat.format(item.value));

            // Set value label based on type
            String label;
            switch (item.type) {
                case STREAK:
                    label = itemView.getContext().getString(R.string.streak_value);
                    break;
                case WEEKLY:
                case TOTAL:
                default:
                    label = itemView.getContext().getString(R.string.points_value);
                    break;
            }
            textValueLabel.setText(label);

            // Highlight current user
            if (item.isCurrentUser) {
                cardItem.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_primaryContainer));
                cardItem.setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_primary));
                cardItem.setStrokeWidth(dpToPx(2));
                textName.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_onPrimaryContainer));
                textLevel.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_onPrimaryContainer));
            } else {
                // Reset to default styling
                cardItem.setCardBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
                cardItem.setStrokeColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_outlineVariant));
                cardItem.setStrokeWidth(dpToPx(1));
                textName.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_onSurface));
                textLevel.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.md_theme_light_onSurfaceVariant));
            }

            // Set avatar placeholder with first letter
            setupAvatar(item);
        }

        /**
         * Setup rank display with medals for top 3
         */
        private void setupRank(int rank) {
            if (rank <= 3) {
                // Show medal for top 3
                imageMedal.setVisibility(View.VISIBLE);
                textRank.setVisibility(View.GONE);
                
                int medalColor;
                switch (rank) {
                    case 1:
                        medalColor = R.color.medal_gold;
                        break;
                    case 2:
                        medalColor = R.color.medal_silver;
                        break;
                    case 3:
                        medalColor = R.color.medal_bronze;
                        break;
                    default:
                        medalColor = R.color.md_theme_light_onSurfaceVariant;
                }
                imageMedal.setImageResource(R.drawable.ic_leaderboard);
                imageMedal.setImageTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(itemView.getContext(), medalColor)));
            } else {
                // Show rank number
                imageMedal.setVisibility(View.GONE);
                textRank.setVisibility(View.VISIBLE);
                textRank.setText(String.valueOf(rank));
            }
        }

        /**
         * Setup avatar with first letter of name
         */
        private void setupAvatar(LeaderboardViewModel.LeaderboardItem item) {
            // For now, use default icon
            // In a real app, you would load the avatar image from URL
            imageAvatar.setImageResource(R.drawable.ic_person);
            
            // Set background color based on rank
            int bgColor;
            if (item.rank == 1) {
                bgColor = R.color.medal_gold;
            } else if (item.rank == 2) {
                bgColor = R.color.medal_silver;
            } else if (item.rank == 3) {
                bgColor = R.color.medal_bronze;
            } else {
                bgColor = R.color.md_theme_light_primaryContainer;
            }
            imageAvatar.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), bgColor));
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
