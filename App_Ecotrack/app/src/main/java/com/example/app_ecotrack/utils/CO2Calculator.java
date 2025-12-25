package com.example.app_ecotrack.utils;

import com.example.app_ecotrack.api.models.ActivityHistoryResponse;
import com.example.app_ecotrack.api.models.StatsResponse;
import com.example.app_ecotrack.api.models.TodayActivitiesResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CO2Calculator - Utility class for calculating CO2 savings
 * Calculates CO2 saved per activity and generates CO2 reports with equivalents
 * 
 * Requirements: 7.3, 7.4, 7.6
 */
public class CO2Calculator {

    // CO2 saved per activity category (kg CO2 per 10 points)
    // Based on design document specifications
    private static final Map<String, Double> CO2_FACTORS = new HashMap<>();
    
    static {
        CO2_FACTORS.put("transport", 0.5);    // Đi xe đạp thay xe máy
        CO2_FACTORS.put("energy", 0.3);       // Tiết kiệm điện
        CO2_FACTORS.put("water", 0.1);        // Tiết kiệm nước
        CO2_FACTORS.put("waste", 0.2);        // Phân loại rác, tái chế
        CO2_FACTORS.put("green", 0.4);        // Trồng cây
        CO2_FACTORS.put("consumption", 0.15); // Tiêu dùng xanh
    }

    // Conversion factors for CO2 equivalents
    private static final double KG_CO2_PER_TREE_PER_YEAR = 21.77;  // kg CO2 absorbed by one tree per year
    private static final double KG_CO2_PER_KM_DRIVEN = 0.21;       // kg CO2 emitted per km driven
    private static final double KG_CO2_PER_KWH = 0.5;              // kg CO2 per kWh of electricity

    /**
     * Calculate CO2 saved for a single activity
     * 
     * @param category Activity category (transport, energy, water, waste, green, consumption)
     * @param points Points earned from the activity
     * @return CO2 saved in kg
     */
    public static double calculateCO2(String category, int points) {
        if (category == null || points <= 0) {
            return 0.0;
        }
        
        Double factor = CO2_FACTORS.get(category.toLowerCase());
        if (factor == null) {
            factor = 0.1; // Default factor for unknown categories
        }
        
        return points * factor / 10.0;
    }

    /**
     * Calculate total CO2 saved from a list of today's activities
     * 
     * @param activities List of user activity items
     * @return Total CO2 saved in kg
     */
    public static double calculateTotalCO2FromTodayActivities(List<TodayActivitiesResponse.UserActivityItem> activities) {
        if (activities == null || activities.isEmpty()) {
            return 0.0;
        }
        
        double totalCO2 = 0.0;
        for (TodayActivitiesResponse.UserActivityItem item : activities) {
            if (item.activity != null) {
                totalCO2 += calculateCO2(item.activity.category, item.pointsEarned);
            }
        }
        
        return totalCO2;
    }

    /**
     * Calculate total CO2 saved from activity history
     * 
     * @param activities List of activity history items
     * @return Total CO2 saved in kg
     */
    public static double calculateTotalCO2FromHistory(List<ActivityHistoryResponse.HistoryItem> activities) {
        if (activities == null || activities.isEmpty()) {
            return 0.0;
        }
        
        double totalCO2 = 0.0;
        for (ActivityHistoryResponse.HistoryItem item : activities) {
            if (item.activity != null) {
                totalCO2 += calculateCO2(item.activity.category, item.pointsEarned);
            }
        }
        
        return totalCO2;
    }

    /**
     * Calculate total CO2 saved from category stats
     * 
     * @param categories List of category statistics
     * @return Total CO2 saved in kg
     */
    public static double calculateTotalCO2FromCategories(List<StatsResponse.CategoryStat> categories) {
        if (categories == null || categories.isEmpty()) {
            return 0.0;
        }
        
        double totalCO2 = 0.0;
        for (StatsResponse.CategoryStat stat : categories) {
            totalCO2 += calculateCO2(stat._id, stat.points);
        }
        
        return totalCO2;
    }

    /**
     * Generate a complete CO2 report with equivalents
     * 
     * @param totalCO2 Total CO2 saved in kg
     * @return CO2Report object with all equivalents calculated
     */
    public static CO2Report generateReport(double totalCO2) {
        return new CO2Report(
            totalCO2,
            calculateTreesEquivalent(totalCO2),
            calculateKmNotDriven(totalCO2),
            calculateKwhSaved(totalCO2)
        );
    }

    /**
     * Calculate trees equivalent (how many trees would absorb this CO2 in a year)
     * 
     * @param totalCO2 Total CO2 in kg
     * @return Number of trees equivalent
     */
    public static double calculateTreesEquivalent(double totalCO2) {
        if (totalCO2 <= 0) {
            return 0.0;
        }
        return totalCO2 / KG_CO2_PER_TREE_PER_YEAR;
    }

    /**
     * Calculate km not driven equivalent
     * 
     * @param totalCO2 Total CO2 in kg
     * @return Kilometers not driven
     */
    public static double calculateKmNotDriven(double totalCO2) {
        if (totalCO2 <= 0) {
            return 0.0;
        }
        return totalCO2 / KG_CO2_PER_KM_DRIVEN;
    }

    /**
     * Calculate kWh saved equivalent
     * 
     * @param totalCO2 Total CO2 in kg
     * @return kWh of electricity saved
     */
    public static double calculateKwhSaved(double totalCO2) {
        if (totalCO2 <= 0) {
            return 0.0;
        }
        return totalCO2 / KG_CO2_PER_KWH;
    }

    /**
     * Get the CO2 factor for a specific category
     * 
     * @param category Activity category
     * @return CO2 factor (kg per 10 points)
     */
    public static double getCO2Factor(String category) {
        if (category == null) {
            return 0.1;
        }
        Double factor = CO2_FACTORS.get(category.toLowerCase());
        return factor != null ? factor : 0.1;
    }

    /**
     * CO2Report - Data class containing CO2 savings and equivalents
     */
    public static class CO2Report {
        public final double totalCO2Saved;      // in kg
        public final double treesEquivalent;    // number of trees
        public final double kmNotDriven;        // kilometers
        public final double kwhSaved;           // kilowatt-hours

        public CO2Report(double totalCO2Saved, double treesEquivalent, double kmNotDriven, double kwhSaved) {
            this.totalCO2Saved = totalCO2Saved;
            this.treesEquivalent = treesEquivalent;
            this.kmNotDriven = kmNotDriven;
            this.kwhSaved = kwhSaved;
        }

        @Override
        public String toString() {
            return "CO2Report{" +
                    "totalCO2Saved=" + totalCO2Saved +
                    ", treesEquivalent=" + treesEquivalent +
                    ", kmNotDriven=" + kmNotDriven +
                    ", kwhSaved=" + kwhSaved +
                    '}';
        }
    }
}
