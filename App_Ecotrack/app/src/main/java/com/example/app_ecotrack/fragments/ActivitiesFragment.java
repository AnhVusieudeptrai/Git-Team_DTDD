package com.example.app_ecotrack.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.Activity;
import com.example.app_ecotrack.adapters.ActivityAdapter;
import com.example.app_ecotrack.DatabaseHelper;
import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;
    private List<Activity> activityList;
    private List<Activity> filteredList;
    private EditText etSearch;
    private Spinner spCategory;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // THAY ĐỔI TÊN LAYOUT NẾU CẦN
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        db = new DatabaseHelper(requireContext());
        prefs = requireActivity().getSharedPreferences("EcoTrackPrefs", requireContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        initViews(view);
        setupRecyclerView();
        setupFilters();
        loadActivities();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewActivities);
        etSearch = view.findViewById(R.id.etSearch);
        spCategory = view.findViewById(R.id.spCategory);
    }

    private void setupRecyclerView() {
        activityList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new ActivityAdapter(requireContext(), filteredList, activity -> completeActivity(activity));

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        String[] categories = {"Tất cả", "Giao thông", "Năng lượng", "Nước", "Rác thải", "Cây xanh", "Tiêu dùng"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(categoryAdapter);

        spCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                filterActivities();
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterActivities();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadActivities() {
        activityList.clear();
        Cursor cursor = db.getAllActivities();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Activity activity = new Activity();
                activity.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                activity.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
                activity.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
                activity.setPoints(cursor.getInt(cursor.getColumnIndexOrThrow("points")));
                activity.setCategory(cursor.getString(cursor.getColumnIndexOrThrow("category")));
                activity.setCompleted(isCompletedToday(activity.getId()));
                activityList.add(activity);
            } while (cursor.moveToNext());
            cursor.close();
        }
        filterActivities();
    }

    private boolean isCompletedToday(int activityId) {
        Cursor todayCursor = db.getTodayActivities(userId);
        boolean completed = false;
        if (todayCursor != null) {
            while (todayCursor.moveToNext()) {
                int id = todayCursor.getInt(todayCursor.getColumnIndexOrThrow("activity_id"));
                if (id == activityId) {
                    completed = true;
                    break;
                }
            }
            todayCursor.close();
        }
        return completed;
    }

    private void filterActivities() {
        filteredList.clear();
        String searchText = etSearch.getText().toString().toLowerCase().trim();
        String selectedCategory = spCategory.getSelectedItem().toString();

        for (Activity activity : activityList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    activity.getName().toLowerCase().contains(searchText) ||
                    activity.getDescription().toLowerCase().contains(searchText);

            boolean matchesCategory = selectedCategory.equals("Tất cả") ||
                    getCategoryInVietnamese(activity.getCategory()).equals(selectedCategory);

            if (matchesSearch && matchesCategory) {
                filteredList.add(activity);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private String getCategoryInVietnamese(String category) {
        switch (category) {
            case "transport": return "Giao thông";
            case "energy": return "Năng lượng";
            case "water": return "Nước";
            case "waste": return "Rác thải";
            case "green": return "Cây xanh";
            case "consumption": return "Tiêu dùng";
            default: return category;
        }
    }

    private void completeActivity(Activity activity) {
        boolean success = db.completeActivity(userId, activity.getId(), activity.getPoints());
        if (success) {
            Toast.makeText(requireContext(), "🎉 Hoàn thành! +" + activity.getPoints() + " điểm",
                    Toast.LENGTH_SHORT).show();
            loadActivities();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).refreshData();
            }
        } else {
            Toast.makeText(requireContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivities();
    }
}