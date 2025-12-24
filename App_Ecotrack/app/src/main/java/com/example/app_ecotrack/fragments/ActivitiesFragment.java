package com.example.app_ecotrack.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.app_ecotrack.Activity;
import com.example.app_ecotrack.adapters.ActivityAdapter;
import com.example.app_ecotrack.MainActivity;
import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.ApiClient;
import com.example.app_ecotrack.api.models.ActivitiesResponse;
import com.example.app_ecotrack.api.models.ActivityData;
import com.example.app_ecotrack.api.models.CompleteActivityResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitiesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;
    private List<Activity> filteredList;
    private EditText etSearch;
    private Spinner spCategory;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

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
        progressBar = view.findViewById(R.id.progressBar);
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
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        ApiClient.getApiService().getActivities().enqueue(new Callback<ActivitiesResponse>() {
            @Override
            public void onResponse(Call<ActivitiesResponse> call, Response<ActivitiesResponse> response) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    activityList.clear();
                    for (ActivityData data : response.body().activities) {
                        Activity activity = new Activity();
                        activity.setId(data.id.hashCode()); // Convert string ID to int for compatibility
                        activity.setApiId(data.id); // Store original API ID
                        activity.setName(data.name);
                        activity.setDescription(data.description);
                        activity.setPoints(data.points);
                        activity.setCategory(data.category);
                        activity.setIcon(data.icon);
                        activity.setCompleted(data.completedToday);
                        activityList.add(activity);
                    }
                    filterActivities();
                } else {
                    Toast.makeText(requireContext(), "Không thể tải hoạt động", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ActivitiesResponse> call, Throwable t) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        if (activity.isCompleted()) {
            Toast.makeText(requireContext(), "Bạn đã hoàn thành hoạt động này hôm nay!", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.getApiService().completeActivity(activity.getApiId()).enqueue(new Callback<CompleteActivityResponse>() {
            @Override
            public void onResponse(Call<CompleteActivityResponse> call, Response<CompleteActivityResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CompleteActivityResponse result = response.body();
                    Toast.makeText(requireContext(), 
                            "🎉 Hoàn thành! +" + result.pointsEarned + " điểm", 
                            Toast.LENGTH_SHORT).show();
                    
                    // Reload activities
                    loadActivities();
                    
                    // Refresh main activity
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).refreshData();
                    }
                } else {
                    Toast.makeText(requireContext(), "Có lỗi xảy ra!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CompleteActivityResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadActivities();
    }
}
