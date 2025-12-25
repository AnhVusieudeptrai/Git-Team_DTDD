package com.example.app_ecotrack.ui.challenges;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.api.models.ChallengeData;
import com.example.app_ecotrack.databinding.FragmentChallengeListBinding;
import com.example.app_ecotrack.ui.adapters.ChallengeAdapter;
import com.example.app_ecotrack.viewmodel.ChallengesViewModel;

import java.util.List;

/**
 * ChallengeListFragment - Fragment hiển thị danh sách thử thách theo tab
 * Được sử dụng trong ViewPager2 của ChallengesFragment
 */
public class ChallengeListFragment extends Fragment {

    private static final String ARG_TAB_TYPE = "tab_type";

    public static final int TAB_ACTIVE = 0;
    public static final int TAB_JOINED = 1;
    public static final int TAB_COMPLETED = 2;

    private FragmentChallengeListBinding binding;
    private ChallengesViewModel viewModel;
    private ChallengeAdapter adapter;
    private int tabType;

    public static ChallengeListFragment newInstance(int tabType) {
        ChallengeListFragment fragment = new ChallengeListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAB_TYPE, tabType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tabType = getArguments().getInt(ARG_TAB_TYPE, TAB_ACTIVE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChallengeListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get shared ViewModel from parent fragment
        viewModel = new ViewModelProvider(requireParentFragment()).get(ChallengesViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        observeViewModel();
        updateEmptyMessage();
    }

    private void setupRecyclerView() {
        adapter = new ChallengeAdapter();
        
        // Set mode based on tab type
        switch (tabType) {
            case TAB_ACTIVE:
                adapter.setMode(ChallengeAdapter.Mode.ACTIVE);
                break;
            case TAB_JOINED:
                adapter.setMode(ChallengeAdapter.Mode.JOINED);
                break;
            case TAB_COMPLETED:
                adapter.setMode(ChallengeAdapter.Mode.COMPLETED);
                break;
        }

        // Set click listeners
        adapter.setOnChallengeClickListener(challenge -> {
            if (getParentFragment() instanceof ChallengesFragment) {
                ((ChallengesFragment) getParentFragment()).showChallengeDetailDialog(challenge);
            }
        });

        adapter.setOnJoinClickListener(challenge -> {
            viewModel.joinChallenge(challenge.id);
        });

        binding.recyclerChallenges.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerChallenges.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.md_theme_light_primary);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.refresh();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.swipeRefresh.setRefreshing(isLoading);
        });

        // Observe challenges based on tab type
        switch (tabType) {
            case TAB_ACTIVE:
                viewModel.getActiveChallenges().observe(getViewLifecycleOwner(), this::updateChallengesList);
                break;
            case TAB_JOINED:
                viewModel.getJoinedChallenges().observe(getViewLifecycleOwner(), this::updateChallengesList);
                break;
            case TAB_COMPLETED:
                viewModel.getCompletedChallenges().observe(getViewLifecycleOwner(), this::updateChallengesList);
                break;
        }
    }

    private void updateChallengesList(List<ChallengeData> challenges) {
        if (challenges != null && !challenges.isEmpty()) {
            adapter.setChallenges(challenges);
            binding.recyclerChallenges.setVisibility(View.VISIBLE);
            binding.layoutEmpty.setVisibility(View.GONE);
        } else {
            adapter.setChallenges(null);
            binding.recyclerChallenges.setVisibility(View.GONE);
            binding.layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void updateEmptyMessage() {
        int messageResId;
        switch (tabType) {
            case TAB_JOINED:
                messageResId = R.string.no_joined_challenges;
                break;
            case TAB_COMPLETED:
                messageResId = R.string.no_completed_challenges;
                break;
            default:
                messageResId = R.string.no_challenges_available;
                break;
        }
        binding.textEmptyMessage.setText(messageResId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
