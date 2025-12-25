package com.example.app_ecotrack.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.ui.adapters.HistoryAdapter;
import com.example.app_ecotrack.viewmodel.HistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;

/**
 * HistoryFragment - Fragment hiển thị lịch sử hoạt động
 * Hỗ trợ pagination với infinite scroll
 * 
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
public class HistoryFragment extends Fragment {

    private HistoryViewModel viewModel;
    private HistoryAdapter adapter;

    // Views
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerHistory;
    private View loadingContainer;
    private View emptyContainer;
    private View errorContainer;
    private TextView textError;
    private LinearProgressIndicator progressLoadMore;

    // Pagination state
    private boolean isLoadingMore = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Load initial data
        viewModel.loadHistory(true);
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        recyclerHistory = view.findViewById(R.id.recycler_history);
        loadingContainer = view.findViewById(R.id.loading_container);
        emptyContainer = view.findViewById(R.id.empty_container);
        errorContainer = view.findViewById(R.id.error_container);
        textError = view.findViewById(R.id.text_error);
        progressLoadMore = view.findViewById(R.id.progress_load_more);
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        recyclerHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerHistory.setAdapter(adapter);

        // Setup infinite scroll
        recyclerHistory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) { // Scrolling down
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                        // Load more when near the end
                        if (!isLoadingMore && viewModel.hasMoreData()) {
                            if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                                loadMoreHistory();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Toolbar back navigation
        toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Swipe to refresh
        swipeRefresh.setOnRefreshListener(() -> viewModel.loadHistory(true));
        swipeRefresh.setColorSchemeResources(R.color.md_theme_light_primary);

        // Retry button
        View btnRetry = requireView().findViewById(R.id.btn_retry);
        if (btnRetry != null) {
            btnRetry.setOnClickListener(v -> viewModel.loadHistory(true));
        }
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefresh.setRefreshing(isLoading && !isLoadingMore);
            
            // Show loading container only on initial load
            if (isLoading && viewModel.getHistoryItems().getValue() == null) {
                loadingContainer.setVisibility(View.VISIBLE);
            } else {
                loadingContainer.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoadingMore().observe(getViewLifecycleOwner(), isLoading -> {
            isLoadingMore = isLoading;
            progressLoadMore.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && viewModel.getHistoryItems().getValue() == null) {
                errorContainer.setVisibility(View.VISIBLE);
                textError.setText(error);
                emptyContainer.setVisibility(View.GONE);
            } else {
                errorContainer.setVisibility(View.GONE);
            }
        });

        viewModel.getHistoryItems().observe(getViewLifecycleOwner(), historyItems -> {
            if (historyItems != null) {
                errorContainer.setVisibility(View.GONE);
                
                if (historyItems.isEmpty()) {
                    emptyContainer.setVisibility(View.VISIBLE);
                    recyclerHistory.setVisibility(View.GONE);
                } else {
                    emptyContainer.setVisibility(View.GONE);
                    recyclerHistory.setVisibility(View.VISIBLE);
                    adapter.setHistoryItems(historyItems);
                }
            }
        });

        viewModel.getNewHistoryItems().observe(getViewLifecycleOwner(), newItems -> {
            if (newItems != null && !newItems.isEmpty()) {
                adapter.addHistoryItems(newItems);
            }
        });
    }

    private void loadMoreHistory() {
        viewModel.loadMoreHistory();
    }
}
