package com.example.app_ecotrack.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.Activity;
import com.example.app_ecotrack.R;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {
    private Context context;
    private List<Activity> activityList;
    private OnActivityClickListener listener;

    public interface OnActivityClickListener {
        void onCompleteClick(Activity activity);
    }

    public ActivityAdapter(Context context, List<Activity> activityList, OnActivityClickListener listener) {
        this.context = context;
        this.activityList = activityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activityList.get(position);

        holder.tvName.setText(activity.getName());
        holder.tvDescription.setText(activity.getDescription());
        holder.tvPoints.setText("+" + activity.getPoints() + " điểm");
        holder.tvCategory.setText(getCategoryName(activity.getCategory()));

        if (activity.isCompleted()) {
            holder.btnComplete.setEnabled(false);
            holder.btnComplete.setText("✓ Đã hoàn thành");
            holder.cardView.setAlpha(0.6f);
        } else {
            holder.btnComplete.setEnabled(true);
            holder.btnComplete.setText("Hoàn thành");
            holder.cardView.setAlpha(1.0f);
        }

        holder.btnComplete.setOnClickListener(v -> {
            if (listener != null && !activity.isCompleted()) {
                listener.onCompleteClick(activity);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activityList.size();
    }

    private String getCategoryName(String category) {
        switch (category) {
            case "transport": return "🚴 Giao thông";
            case "energy": return "💡 Năng lượng";
            case "water": return "💧 Nước";
            case "waste": return "♻️ Rác thải";
            case "green": return "🌳 Cây xanh";
            case "consumption": return "🛒 Tiêu dùng";
            default: return category;
        }
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvDescription, tvPoints, tvCategory;
        Button btnComplete;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvName = itemView.findViewById(R.id.tvActivityName);
            tvDescription = itemView.findViewById(R.id.tvActivityDescription);
            tvPoints = itemView.findViewById(R.id.tvActivityPoints);
            tvCategory = itemView.findViewById(R.id.tvActivityCategory);
            btnComplete = itemView.findViewById(R.id.btnComplete);
        }
    }
}