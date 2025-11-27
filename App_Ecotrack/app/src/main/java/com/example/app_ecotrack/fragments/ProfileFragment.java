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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews(view);
        loadProfileData();
        loadAchievements();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvFullname = view.findViewById(R.id.tvFullname);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvTotalPoints = view.findViewById(R.id.tvTotalPoints);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvTotalActivities = view.findViewById(R.id.tvTotalActivities);
        tvRank = view.findViewById(R.id.tvRank);

        cardLeaderboard = view.findViewById(R.id.cardLeaderboard);
        cardRewards = view.findViewById(R.id.cardRewards);
        cardSettings = view.findViewById(R.id.cardSettings);

        containerAchievements = view.findViewById(R.id.containerAchievements);
    }
