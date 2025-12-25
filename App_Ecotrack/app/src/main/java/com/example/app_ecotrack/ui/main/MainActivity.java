package com.example.app_ecotrack.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.app_ecotrack.R;
import com.example.app_ecotrack.databinding.ActivityMainBinding;
import com.example.app_ecotrack.services.EcoTrackMessagingService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity - Single Activity với Navigation Component và Bottom Navigation
 * Chứa 5 tabs: Home, Badges, Challenges, Leaderboard, Profile
 * Requirements: 9.4, 8.6
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        
        // Handle notification navigation
        handleNotificationIntent(getIntent());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    private void setupNavigation() {
        // Get NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Setup Bottom Navigation with NavController
            BottomNavigationView bottomNav = binding.bottomNavigation;
            
            // Custom navigation options with animations
            NavOptions navOptions = new NavOptions.Builder()
                    .setEnterAnim(R.anim.fade_in)
                    .setExitAnim(R.anim.fade_out)
                    .setPopEnterAnim(R.anim.fade_in)
                    .setPopExitAnim(R.anim.fade_out)
                    .setLaunchSingleTop(true)
                    .setPopUpTo(navController.getGraph().getStartDestinationId(), false)
                    .build();
            
            // Setup with NavigationUI for automatic handling
            NavigationUI.setupWithNavController(bottomNav, navController);
            
            // Custom item selection to apply animations
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                
                // Avoid re-selecting the same destination
                if (navController.getCurrentDestination() != null && 
                    navController.getCurrentDestination().getId() == itemId) {
                    return false;
                }
                
                navController.navigate(itemId, null, navOptions);
                return true;
            });
        }
    }
    
    /**
     * Handle navigation from push notification tap
     * Requirements: 8.6
     */
    private void handleNotificationIntent(Intent intent) {
        if (intent == null || navController == null) return;
        
        String targetScreen = intent.getStringExtra(EcoTrackMessagingService.EXTRA_TARGET_SCREEN);
        if (targetScreen == null) return;
        
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.fade_in)
                .setExitAnim(R.anim.fade_out)
                .setLaunchSingleTop(true)
                .build();
        
        switch (targetScreen) {
            case "home":
                navController.navigate(R.id.navigation_home, null, navOptions);
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
                break;
            case "badges":
                navController.navigate(R.id.navigation_badges, null, navOptions);
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_badges);
                break;
            case "challenges":
                navController.navigate(R.id.navigation_challenges, null, navOptions);
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_challenges);
                break;
            case "leaderboard":
                navController.navigate(R.id.navigation_leaderboard, null, navOptions);
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_leaderboard);
                break;
            case "profile":
                navController.navigate(R.id.navigation_profile, null, navOptions);
                binding.bottomNavigation.setSelectedItemId(R.id.navigation_profile);
                break;
        }
        
        // Clear the intent extras to prevent re-navigation
        intent.removeExtra(EcoTrackMessagingService.EXTRA_TARGET_SCREEN);
        intent.removeExtra(EcoTrackMessagingService.EXTRA_NOTIFICATION_TYPE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}
