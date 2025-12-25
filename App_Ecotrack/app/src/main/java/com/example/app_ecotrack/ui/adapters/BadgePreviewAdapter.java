package com.example.app_ecotrack.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.api.models.BadgeData;
import com.example.app_ecotrack.databinding.ItemBadgePreviewBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * BadgePreviewAdapter - Adapter cho hiển thị preview huy hiệu trong Profile
 */
public class BadgePreviewAdapter extends RecyclerView.Adapter<BadgePreviewAdapter.BadgePreviewViewHolder> {

    private List<BadgeData> badges = new ArrayList<>();
    private static final int MAX_DISPLAY = 5; // Maximum badges to display in preview

    public void setBadges(List<BadgeData> badges) {
        this.badges = badges != null ? badges : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BadgePreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBadgePreviewBinding binding = ItemBadgePreviewBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BadgePreviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgePreviewViewHolder holder, int position) {
        if (position < badges.size()) {
            holder.bind(badges.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(badges.size(), MAX_DISPLAY);
    }

    static class BadgePreviewViewHolder extends RecyclerView.ViewHolder {
        private final ItemBadgePreviewBinding binding;

        BadgePreviewViewHolder(ItemBadgePreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BadgeData badge) {
            // Set badge icon (emoji)
            String icon = badge.icon != null ? badge.icon : "🏆";
            binding.textBadgeIcon.setText(icon);
        }
    }
}
