package com.example.app_ecotrack.fragments;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;

public class HomeFragment extends Fragment {
    private TextView tvTodayPoints, tvWeekPoints, tvTotalPoints, tvTodayActivities, tvTotalActivities, tvRank;
    private CardView cardActivities, cardRewards, cardLeaderboard;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        
        // userId is stored as String from API response
        String userIdStr = prefs.getString("userId", "-1");
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            userId = -1;
        }

        initViews(view);
        loadData();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvTodayPoints = view.findViewById(R.id.tvTodayPoints);
        tvWeekPoints = view.findViewById(R.id.tvWeekPoints);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvTodayActivities = view.findViewById(R.id.tvTodayActivities);
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities);
        tvRank = view.findViewById(R.id.tvRank);

        cardActivities = view.findViewById(R.id.cardActivities);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
    }

    private void loadData() {
        // Today's points
        int todayPoints = db.getTodayPoints(userId);
        tvTodayPoints.setText(String.valueOf(todayPoints));

        // Week points
        int weekPoints = getWeekPoints();
        tvWeekPoints.setText(String.valueOf(weekPoints));

        // Total points - stored as int but may be string from API
        int totalPoints = 0;
        try {
            totalPoints = prefs.getInt("points", 0);
        } catch (ClassCastException e) {
            // If stored as string, parse it
            String pointsStr = prefs.getString("points", "0");
            try {
                totalPoints = Integer.parseInt(pointsStr);
            } catch (NumberFormatException ex) {
                totalPoints = 0;
            }
        }
        tvTotalPoints.setText(String.valueOf(totalPoints));

        // Today's activities count
        Cursor todayCursor = db.getTodayActivities(userId);
        int todayCount = todayCursor != null ? todayCursor.getCount() : 0;
        tvTodayActivities.setText(String.valueOf(todayCount));
        if (todayCursor != null) todayCursor.close();

        // Total activities count
        Cursor totalCursor = db.getUserActivities(userId);
        int totalCount = totalCursor != null ? totalCursor.getCount() : 0;
        tvTotalActivities.setText(String.valueOf(totalCount));
        if (totalCursor != null) totalCursor.close();

        // Rank
        int rank = getUserRank();
        tvRank.setText("#" + rank);
    }

    private int getWeekPoints() {
        long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
        Cursor cursor = db.getUserActivities(userId);
        int weekPoints = 0;

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String dateStr = cursor.getString(cursor.getColumnIndexOrThrow("completed_date"));
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
                    java.util.Date date = sdf.parse(dateStr);
                    if (date != null && date.getTime() > weekAgo) {
                        weekPoints += cursor.getInt(cursor.getColumnIndexOrThrow("points_earned"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return weekPoints;
    }

    private int getUserRank() {
        Cursor leaderboard = db.getLeaderboard();
        int rank = 1;
        if (leaderboard != null) {
            while (leaderboard.moveToNext()) {
                int id = leaderboard.getInt(leaderboard.getColumnIndexOrThrow("id"));
                if (id == userId) {
                    break;
                }
                rank++;
            }
            leaderboard.close();
        }
        return rank;
    }

    private void setupClickListeners() {
        cardActivities.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).findViewById(R.id.viewPager);
                androidx.viewpager2.widget.ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                viewPager.setCurrentItem(1);
            }
        });

//        cardRewards.setOnClickListener(v -> {
//            android.content.Intent intent = new android.content.Intent(getActivity(), RewardsActivity.class);
//            startActivity(intent);
//        });

//        cardLeaderboard.setOnClickListener(v -> {
//            android.content.Intent intent = new android.content.Intent(getActivity(), LeaderboardActivity.class);
//            startActivity(intent);
//        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshData();
        }
    }
}