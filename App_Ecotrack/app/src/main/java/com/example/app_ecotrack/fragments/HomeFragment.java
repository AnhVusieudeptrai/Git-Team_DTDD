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
import com.example.app_ecotrack.LeaderboardActivity;
import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.RewardsActivity;

public class HomeFragment extends Fragment {
    private TextView tvTodayPoints, tvWeekPoints, tvTotalPoints, tvTodayActivities, tvTotalActivities, tvRank;
    private CardView cardActivities, cardRewards, cardLeaderboard;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

