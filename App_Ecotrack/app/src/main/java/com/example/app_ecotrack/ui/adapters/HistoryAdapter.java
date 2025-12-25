package com.example.app_ecotrack.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ActivityHistoryResponse;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HistoryAdapter - Adapter cho RecyclerView hiển thị lịch sử hoạt động
 * Hỗ trợ date headers để nhóm hoạt động theo ngày
 * 
 * Requirements: 10.1, 10.2, 10.4
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DATE_HEADER = 0;
    private static final int TYPE_HISTORY_ITEM = 1;

    private final List<Object> items = new ArrayList<>();
    
    // Category icons mapping
    private static final Map<String, String> CATEGORY_ICONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_NAMES = new LinkedHashMap<>();
    
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
    }

    // Date formatters
    private final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Set history items and group by date
     */
    public void setHistoryItems(List<ActivityHistoryResponse.HistoryItem> historyItems) {
        items.clear();
        
        if (historyItems == null || historyItems.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        // Group by date
        Map<String, List<ActivityHistoryResponse.HistoryItem>> grouped = new LinkedHashMap<>();
        for (ActivityHistoryResponse.HistoryItem item : historyItems) {
            String dateKey = extractDateKey(item.completedAt);
            if (!grouped.containsKey(dateKey)) {
                grouped.put(dateKey, new ArrayList<>());
            }
            grouped.get(dateKey).add(item);
        }

        // Build items list with date headers
        for (Map.Entry<String, List<ActivityHistoryResponse.HistoryItem>> entry : grouped.entrySet()) {
            // Add date header
            DateHeader header = new DateHeader();
            header.dateKey = entry.getKey();
            header.displayDate = formatDateHeader(entry.getKey());
            items.add(header);
            
            // Add history items
            items.addAll(entry.getValue());
        }

        notifyDataSetChanged();
    }

    /**
     * Add more history items (for pagination)
     */
    public void addHistoryItems(List<ActivityHistoryResponse.HistoryItem> newItems) {
        if (newItems == null || newItems.isEmpty()) {
            return;
        }

        int startPosition = items.size();
        String lastDateKey = getLastDateKey();

        for (ActivityHistoryResponse.HistoryItem item : newItems) {
            String dateKey = extractDateKey(item.completedAt);
            
            // Check if we need a new date header
            if (!dateKey.equals(lastDateKey)) {
                DateHeader header = new DateHeader();
                header.dateKey = dateKey;
                header.displayDate = formatDateHeader(dateKey);
                items.add(header);
                lastDateKey = dateKey;
            }
            
            items.add(item);
        }

        notifyItemRangeInserted(startPosition, items.size() - startPosition);
    }

    private String getLastDateKey() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (items.get(i) instanceof DateHeader) {
                return ((DateHeader) items.get(i)).dateKey;
            }
        }
        return "";
    }

    private String extractDateKey(String dateTimeStr) {
        try {
            Date date = inputFormat.parse(dateTimeStr);
            return dayFormat.format(date);
        } catch (ParseException e) {
            // Try alternative format without milliseconds
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = altFormat.parse(dateTimeStr);
                return dayFormat.format(date);
            } catch (ParseException e2) {
                return dateTimeStr.substring(0, 10); // Fallback
            }
        }
    }

    private String formatDateHeader(String dateKey) {
        try {
            Date date = dayFormat.parse(dateKey);
            Calendar today = Calendar.getInstance();
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DAY_OF_YEAR, -1);
            Calendar itemDate = Calendar.getInstance();
            itemDate.setTime(date);

            if (isSameDay(itemDate, today)) {
                return "Hôm nay, " + dateFormat.format(date);
            } else if (isSameDay(itemDate, yesterday)) {
                return "Hôm qua, " + dateFormat.format(date);
            } else {
                SimpleDateFormat fullFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi"));
                return fullFormat.format(date);
            }
        } catch (ParseException e) {
            return dateKey;
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private String formatTime(String dateTimeStr) {
        try {
            Date date = inputFormat.parse(dateTimeStr);
            return timeFormat.format(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat altFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                Date date = altFormat.parse(dateTimeStr);
                return timeFormat.format(date);
            } catch (ParseException e2) {
                return "";
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DateHeader ? TYPE_DATE_HEADER : TYPE_HISTORY_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DATE_HEADER) {
            View view = inflater.inflate(R.layout.item_history_date_header, parent, false);
            return new DateHeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_history, parent, false);
            return new HistoryItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DateHeaderViewHolder) {
            DateHeader header = (DateHeader) items.get(position);
            ((DateHeaderViewHolder) holder).bind(header);
        } else if (holder instanceof HistoryItemViewHolder) {
            ActivityHistoryResponse.HistoryItem item = (ActivityHistoryResponse.HistoryItem) items.get(position);
            ((HistoryItemViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Date header data class
     */
    static class DateHeader {
        String dateKey;
        String displayDate;
    }

    /**
     * ViewHolder for date headers
     */
    static class DateHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textDate;

        DateHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.text_date);
        }

        void bind(DateHeader header) {
            textDate.setText(header.displayDate);
        }
    }

    /**
     * ViewHolder for history items
     */
    class HistoryItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView textIcon;
        private final TextView textActivityName;
        private final TextView textActivityCategory;
        private final TextView textCompletedTime;
        private final Chip chipPoints;

        HistoryItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textIcon = itemView.findViewById(R.id.text_icon);
            textActivityName = itemView.findViewById(R.id.text_activity_name);
            textActivityCategory = itemView.findViewById(R.id.text_activity_category);
            textCompletedTime = itemView.findViewById(R.id.text_completed_time);
            chipPoints = itemView.findViewById(R.id.chip_points);
        }

        void bind(ActivityHistoryResponse.HistoryItem item) {
            // Set icon
            String category = item.activity != null ? item.activity.category : "other";
            String icon = item.activity != null && item.activity.icon != null ? 
                    item.activity.icon : CATEGORY_ICONS.getOrDefault(category, "📋");
            textIcon.setText(icon);

            // Set activity name
            String name = item.activity != null ? item.activity.name : "Hoạt động";
            textActivityName.setText(name);

            // Set category name
            String categoryName = CATEGORY_NAMES.getOrDefault(category, category);
            textActivityCategory.setText(categoryName);

            // Set completed time
            String time = formatTime(item.completedAt);
            textCompletedTime.setText(time);

            // Set points
            chipPoints.setText("+" + item.pointsEarned);
        }
    }
}
