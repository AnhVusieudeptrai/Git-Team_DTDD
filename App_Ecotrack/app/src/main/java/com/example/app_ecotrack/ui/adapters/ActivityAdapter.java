package com.example.app_ecotrack.ui.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ActivityData;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ActivityAdapter - Adapter cho RecyclerView hiển thị danh sách hoạt động
 * Hỗ trợ category headers và click to complete với animation
 */
public class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ACTIVITY = 1;

    private final List<Object> items = new ArrayList<>();
    private OnActivityClickListener listener;

    // Category icons mapping
    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    private static final Map<String, String> ACTIVITY_ICONS = new LinkedHashMap<>();
    
    static {
        CATEGORY_ICONS.put("transport", "🚴");
        CATEGORY_ICONS.put("energy", "💡");
        CATEGORY_ICONS.put("water", "💧");
        CATEGORY_ICONS.put("waste", "♻️");
        CATEGORY_ICONS.put("green", "🌱");
        CATEGORY_ICONS.put("consumption", "🛍️");
        
        CATEGORY_NAMES.put("transport", "Giao thông");
        CATEGORY_NAMES.put("energy", "Năng lượng");
        CATEGORY_NAMES.put("water", "Nước");
        CATEGORY_NAMES.put("waste", "Rác thải");
        CATEGORY_NAMES.put("green", "Xanh");
        CATEGORY_NAMES.put("consumption", "Tiêu dùng");
        
        // Activity icon mapping (text to emoji)
        ACTIVITY_ICONS.put("bus", "🚌");
        ACTIVITY_ICONS.put("bike", "🚴");
        ACTIVITY_ICONS.put("walk", "🚶");
        ACTIVITY_ICONS.put("train", "🚆");
        ACTIVITY_ICONS.put("metro", "🚇");
        ACTIVITY_ICONS.put("car", "🚗");
        ACTIVITY_ICONS.put("electric", "⚡");
        ACTIVITY_ICONS.put("light", "💡");
        ACTIVITY_ICONS.put("bulb", "💡");
        ACTIVITY_ICONS.put("computer", "💻");
        ACTIVITY_ICONS.put("com", "💻");
        ACTIVITY_ICONS.put("ac", "❄️");
        ACTIVITY_ICONS.put("fan", "🌀");
        ACTIVITY_ICONS.put("water", "💧");
        ACTIVITY_ICONS.put("shower", "🚿");
        ACTIVITY_ICONS.put("tap", "🚰");
        ACTIVITY_ICONS.put("bottle", "🍶");
        ACTIVITY_ICONS.put("recycle", "♻️");
        ACTIVITY_ICONS.put("trash", "🗑️");
        ACTIVITY_ICONS.put("bag", "👜");
        ACTIVITY_ICONS.put("plastic", "🥤");
        ACTIVITY_ICONS.put("tree", "🌳");
        ACTIVITY_ICONS.put("plant", "🌱");
        ACTIVITY_ICONS.put("flower", "🌸");
        ACTIVITY_ICONS.put("garden", "🏡");
        ACTIVITY_ICONS.put("shop", "🛒");
        ACTIVITY_ICONS.put("product", "📦");
        ACTIVITY_ICONS.put("pro", "📦");
        ACTIVITY_ICONS.put("local", "🏪");
        ACTIVITY_ICONS.put("food", "🍽️");
        ACTIVITY_ICONS.put("vegan", "🥗");
        ACTIVITY_ICONS.put("meat", "🥩");
        ACTIVITY_ICONS.put("coffee", "☕");
        ACTIVITY_ICONS.put("cup", "🥤");
    }

    public interface OnActivityClickListener {
        void onActivityClick(ActivityData activity);
    }

    public void setOnActivityClickListener(OnActivityClickListener listener) {
        this.listener = listener;
    }

    /**
     * Set activities and group by category
     */
    public void setActivities(List<ActivityData> activities) {
        items.clear();
        
        if (activities == null || activities.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Group activities by category
        Map<String, List<ActivityData>> grouped = new LinkedHashMap<>();
        for (ActivityData activity : activities) {
            String category = activity.category != null ? activity.category : "other";
            if (!grouped.containsKey(category)) {
                grouped.put(category, new ArrayList<>());
            }
            grouped.get(category).add(activity);
        }

        // Build items list with headers
        for (Map.Entry<String, List<ActivityData>> entry : grouped.entrySet()) {
            String category = entry.getKey();
            List<ActivityData> categoryActivities = entry.getValue();
            
            // Add header
            CategoryHeader header = new CategoryHeader();
            header.category = category;
            header.icon = CATEGORY_ICONS.getOrDefault(category, "📋");
            header.name = CATEGORY_NAMES.getOrDefault(category, category);
            items.add(header);
            
            // Add activities
            items.addAll(categoryActivities);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof CategoryHeader ? TYPE_HEADER : TYPE_ACTIVITY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_activity_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_activity, parent, false);
            return new ActivityViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            CategoryHeader header = (CategoryHeader) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ActivityViewHolder) {
            ActivityData activity = (ActivityData) items.get(position);
            ((ActivityViewHolder) holder).bind(activity);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Category header data class
     */
    static class CategoryHeader {
        String category;
        String icon;
        String name;
    }

    /**
     * ViewHolder for category headers
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textIcon;
        private final TextView textName;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textIcon = itemView.findViewById(R.id.text_category_icon);
            textName = itemView.findViewById(R.id.text_category_name);
        }

        void bind(CategoryHeader header) {
            textIcon.setText(header.icon);
            textName.setText(header.name);
        }
    }

    /**
     * ViewHolder for activity items
     */
    class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardActivity;
        private final TextView textIcon;
        private final TextView textName;
        private final TextView textCategory;
        private final Chip chipPoints;
        private final ImageView iconCompleted;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardActivity = itemView.findViewById(R.id.card_activity);
            textIcon = itemView.findViewById(R.id.text_icon);
            textName = itemView.findViewById(R.id.text_activity_name);
            textCategory = itemView.findViewById(R.id.text_activity_category);
            chipPoints = itemView.findViewById(R.id.chip_points);
            iconCompleted = itemView.findViewById(R.id.icon_completed);
        }

        void bind(ActivityData activity) {
            // Set icon - convert text icon to emoji
            String icon = activity.icon;
            if (icon != null && !icon.isEmpty()) {
                // Check if it's already an emoji (starts with unicode)
                if (icon.length() <= 4 && !icon.matches("[a-zA-Z]+")) {
                    textIcon.setText(icon);
                } else {
                    // Convert text icon to emoji
                    String emoji = ACTIVITY_ICONS.get(icon.toLowerCase());
                    if (emoji != null) {
                        textIcon.setText(emoji);
                    } else {
                        // Fallback to category icon
                        textIcon.setText(CATEGORY_ICONS.getOrDefault(activity.category, "📋"));
                    }
                }
            } else {
                textIcon.setText(CATEGORY_ICONS.getOrDefault(activity.category, "📋"));
            }

            // Set name
            textName.setText(activity.name);

            // Set category name
            String categoryName = CATEGORY_NAMES.getOrDefault(activity.category, activity.category);
            textCategory.setText(categoryName);

            // Set points
            chipPoints.setText("+" + activity.points);

            // Set completed state
            if (activity.completedToday) {
                iconCompleted.setVisibility(View.VISIBLE);
                chipPoints.setVisibility(View.GONE);
                cardActivity.setAlpha(0.7f);
            } else {
                iconCompleted.setVisibility(View.GONE);
                chipPoints.setVisibility(View.VISIBLE);
                cardActivity.setAlpha(1.0f);
            }

            // Click listener
            cardActivity.setOnClickListener(v -> {
                if (!activity.completedToday && listener != null) {
                    // Play completion animation
                    playCompletionAnimation(cardActivity, () -> {
                        listener.onActivityClick(activity);
                    });
                }
            });
        }

        /**
         * Play completion animation
         */
        private void playCompletionAnimation(View view, Runnable onComplete) {
            // Scale animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1.05f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1.05f, 1f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY);
            animatorSet.setDuration(300);
            
            animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            
            animatorSet.start();
        }
    }
}
