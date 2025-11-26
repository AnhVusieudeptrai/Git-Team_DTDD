package com.example.app_ecotrack.fragments;

import android.content.SharedPreferences;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.app_ecotrack.DatabaseHelper;

public class StatisticsFragment {
    private TextView tvLevelProgress, tvProgressPercent;
    private ProgressBar progressLevel;
    private LinearLayout containerCategories, containerRecent, containerWeekly;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;
}
