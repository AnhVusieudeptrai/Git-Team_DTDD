package com.example.app_ecotrack.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.LeaderboardActivity;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.RewardsActivity;
import com.example.app_ecotrack.SettingsActivity;

public class ProfileFragment extends Fragment {
    private TextView tvFullname, tvUsername, tvEmail, tvTotalPoints, tvLevel, tvTotalActivities, tvRank;
    private CardView cardLeaderboard, cardRewards, cardSettings;
    private LinearLayout containerAchievements;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

