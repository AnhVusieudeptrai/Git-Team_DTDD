package com.example.app_ecotrack.adapters;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.app_ecotrack.fragments.HomeFragment;
import com.example.app_ecotrack.fragments.ProfileFragment;
import com.example.app_ecotrack.fragments.StatisticsFragment;
import com.example.app_ecotrack.fragments.ActivitiesFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new ActivitiesFragment();
            case 2: return new StatisticsFragment();
            case 3: return new ProfileFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}