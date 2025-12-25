package com.example.app_ecotrack.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.StatsResponse;
import com.example.app_ecotrack.utils.CO2Calculator;
import com.example.app_ecotrack.viewmodel.StatsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.tabs.TabLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * StatsFragment - Fragment hiển thị thống kê và báo cáo CO2
 * 
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5
 */
public class StatsFragment extends Fragment {

    private StatsViewModel viewModel;

    // Views
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayoutPeriod;
    private TextView textStatsPoints;
    private TextView textStatsActivities;
    private BarChart chartWeekly;
    private LinearLayout layoutCategories;
    private TextView textCO2Total;
    private TextView textCO2Trees;
    private TextView textCO2Km;
    private TextView textCO2Kwh;
    private View loadingContainer;
    private View errorContainer;
    private TextView textError;

    // Category colors map
    private static final Map<String, Integer> CATEGORY_COLORS = new HashMap<>();
    static {
        CATEGORY_COLORS.put("transport", R.color.category_transport);
        CATEGORY_COLORS.put("energy", R.color.category_energy);
        CATEGORY_COLORS.put("water", R.color.category_water);
        CATEGORY_COLORS.put("waste", R.color.category_waste);
        CATEGORY_COLORS.put("green", R.color.category_green);
        CATEGORY_COLORS.put("consumption", R.color.category_consumption);
    }

    // Category names in Vietnamese
    private static final Map<String, String> CATEGORY_NAMES = new HashMap<>();
    static {
        CATEGORY_NAMES.put("transport", "Giao thông");
        CATEGORY_NAMES.put("energy", "Năng lượng");
        CATEGORY_NAMES.put("water", "Nước");
        CATEGORY_NAMES.put("waste", "Rác thải");
        CATEGORY_NAMES.put("green", "Xanh");
        CATEGORY_NAMES.put("consumption", "Tiêu dùng");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(StatsViewModel.class);

        initViews(view);
        setupListeners();
        observeViewModel();

        // Load data
        viewModel.loadStats();
    }

    private void initViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        tabLayoutPeriod = view.findViewById(R.id.tab_layout_period);
        textStatsPoints = view.findViewById(R.id.text_stats_points);
        textStatsActivities = view.findViewById(R.id.text_stats_activities);
        chartWeekly = view.findViewById(R.id.chart_weekly);
        layoutCategories = view.findViewById(R.id.layout_categories);
        textCO2Total = view.findViewById(R.id.text_co2_total);
        textCO2Trees = view.findViewById(R.id.text_co2_trees);
        textCO2Km = view.findViewById(R.id.text_co2_km);
        textCO2Kwh = view.findViewById(R.id.text_co2_kwh);
        loadingContainer = view.findViewById(R.id.loading_container);
        errorContainer = view.findViewById(R.id.error_container);
        textError = view.findViewById(R.id.text_error);

        // Setup chart
        setupChart();
    }

    private void setupListeners() {
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadStats());

        swipeRefresh.setColorSchemeResources(R.color.md_theme_light_primary);

        tabLayoutPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewModel.setSelectedPeriod(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        View btnRetry = requireView().findViewById(R.id.btn_retry);
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> viewModel.loadStats());
        }

        // View History button
        MaterialButton btnViewHistory = requireView().findViewById(R.id.btn_view_history);
        if (btnViewHistory != null) {
            btnViewHistory.setOnClickListener(v -> {
                Navigation.findNavController(v).navigate(R.id.action_stats_to_history);
            });
        }
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefresh.setRefreshing(isLoading);
            loadingContainer.setVisibility(isLoading && viewModel.getStats().getValue() == null ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && viewModel.getStats().getValue() == null) {
                errorContainer.setVisibility(View.VISIBLE);
                textError.setText(error);
            } else {
                errorContainer.setVisibility(View.GONE);
            }
        });

        viewModel.getStats().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                errorContainer.setVisibility(View.GONE);
                updateStatsUI(stats);
                updateChart(stats.weeklyChart);
                updateCategories(stats.categories);
            }
        });

        viewModel.getCO2Report().observe(getViewLifecycleOwner(), report -> {
            if (report != null) {
                updateCO2Report(report);
            }
        });

        viewModel.getSelectedPeriod().observe(getViewLifecycleOwner(), period -> {
            StatsResponse stats = viewModel.getStats().getValue();
            if (stats != null) {
                updateStatsForPeriod(stats, period);
            }
        });
    }

    private void setupChart() {
        chartWeekly.getDescription().setEnabled(false);
        chartWeekly.setDrawGridBackground(false);
        chartWeekly.setDrawBarShadow(false);
        chartWeekly.setHighlightFullBarEnabled(false);
        chartWeekly.setDrawValueAboveBar(true);
        chartWeekly.setPinchZoom(false);
        chartWeekly.setDoubleTapToZoomEnabled(false);
        chartWeekly.getLegend().setEnabled(false);

        // X axis
        XAxis xAxis = chartWeekly.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSurface));

        // Y axis
        YAxis leftAxis = chartWeekly.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSurface));

        chartWeekly.getAxisRight().setEnabled(false);
    }

    private void updateStatsUI(StatsResponse stats) {
        Integer period = viewModel.getSelectedPeriod().getValue();
        if (period == null) period = 0;
        updateStatsForPeriod(stats, period);
    }

    private void updateStatsForPeriod(StatsResponse stats, int period) {
        int points = viewModel.getPointsForPeriod(stats, period);
        int activities = viewModel.getActivitiesForPeriod(stats, period);

        textStatsPoints.setText(String.valueOf(points));
        textStatsActivities.setText(String.valueOf(activities));
    }

    private void updateChart(List<StatsResponse.WeeklyChartItem> weeklyChart) {
        if (weeklyChart == null || weeklyChart.isEmpty()) {
            chartWeekly.clear();
            chartWeekly.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE", new Locale("vi"));

        for (int i = 0; i < weeklyChart.size(); i++) {
            StatsResponse.WeeklyChartItem item = weeklyChart.get(i);
            entries.add(new BarEntry(i, item.points));

            // Format date to day name
            String dayLabel;
            try {
                Date date = inputFormat.parse(item.date);
                dayLabel = outputFormat.format(date);
            } catch (ParseException e) {
                dayLabel = item.date.substring(5); // Fallback to MM-dd
            }
            labels.add(dayLabel);
        }

        BarDataSet dataSet = new BarDataSet(entries, "Điểm");
        dataSet.setColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary));
        dataSet.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.md_theme_light_onSurface));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        chartWeekly.setData(barData);
        chartWeekly.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        chartWeekly.animateY(500);
        chartWeekly.invalidate();
    }

    private void updateCategories(List<StatsResponse.CategoryStat> categories) {
        layoutCategories.removeAllViews();

        if (categories == null || categories.isEmpty()) {
            return;
        }

        // Calculate max points for progress bar
        int maxPoints = 0;
        for (StatsResponse.CategoryStat stat : categories) {
            if (stat.points > maxPoints) {
                maxPoints = stat.points;
            }
        }

        for (StatsResponse.CategoryStat stat : categories) {
            View itemView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_category_stat, layoutCategories, false);

            View colorView = itemView.findViewById(R.id.view_category_color);
            TextView nameText = itemView.findViewById(R.id.text_category_name);
            TextView countText = itemView.findViewById(R.id.text_category_count);
            TextView pointsText = itemView.findViewById(R.id.text_category_points);
            LinearProgressIndicator progressBar = itemView.findViewById(R.id.progress_category);

            // Set category color
            Integer colorRes = CATEGORY_COLORS.get(stat._id);
            if (colorRes != null) {
                colorView.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), colorRes));
                progressBar.setIndicatorColor(ContextCompat.getColor(requireContext(), colorRes));
            }

            // Set category name
            String categoryName = CATEGORY_NAMES.get(stat._id);
            nameText.setText(categoryName != null ? categoryName : stat._id);

            // Set count and points
            countText.setText(stat.count + " hoạt động");
            pointsText.setText(stat.points + " điểm");

            // Set progress
            int progress = maxPoints > 0 ? (int) ((stat.points * 100.0) / maxPoints) : 0;
            progressBar.setProgress(progress);

            layoutCategories.addView(itemView);
        }
    }

    private void updateCO2Report(CO2Calculator.CO2Report report) {
        textCO2Total.setText(String.format(Locale.getDefault(), "%.2f kg", report.totalCO2Saved));
        textCO2Trees.setText(String.format(Locale.getDefault(), "%.1f", report.treesEquivalent));
        textCO2Km.setText(String.format(Locale.getDefault(), "%.1f", report.kmNotDriven));
        textCO2Kwh.setText(String.format(Locale.getDefault(), "%.1f", report.kwhSaved));
    }
}
