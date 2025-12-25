package com.example.app_ecotrack.ui.challenges;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ChallengePagerAdapter - Adapter cho ViewPager2 trong ChallengesFragment
 * Quản lý 3 tabs: Đang diễn ra, Đã tham gia, Hoàn thành
 */
public class ChallengePagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 3;

    public ChallengePagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ChallengeListFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
