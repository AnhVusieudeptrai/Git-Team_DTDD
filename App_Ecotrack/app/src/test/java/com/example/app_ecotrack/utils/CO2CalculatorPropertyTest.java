package com.example.app_ecotrack.utils;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Property-Based Tests for CO2Calculator
 * 
 * Feature: ecotrack-android-app, Property 3: CO2 Calculation Accuracy
 * Validates: Requirements 7.3, 7.4, 7.6
 * 
 * Property: For any completed activity with category C and points P, 
 * the CO2 saved should equal P * CO2_FACTOR[C] / 10, where CO2_FACTOR 
 * is defined per category. The total CO2 report should be the sum of 
 * all individual activity CO2 values.
 */
public class CO2CalculatorPropertyTest {

    // Known CO2 factors from the design document
    private static final double TRANSPORT_FACTOR = 0.5;
    private static final double ENERGY_FACTOR = 0.3;
    private static final double WATER_FACTOR = 0.1;
    private static final double WASTE_FACTOR = 0.2;
    private static final double GREEN_FACTOR = 0.4;
    private static final double CONSUMPTION_FACTOR = 0.15;
    private static final double DEFAULT_FACTOR = 0.1;

    // Conversion constants
    private static final double KG_CO2_PER_TREE_PER_YEAR = 21.77;
    private static final double KG_CO2_PER_KM_DRIVEN = 0.21;
    private static final double KG_CO2_PER_KWH = 0.5;

    private static final double EPSILON = 0.0001;

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
        "transport", "energy", "water", "waste", "green", "consumption"
    );

    /**
     * Property 3.1: CO2 calculation follows the formula P * CO2_FACTOR[C] / 10
     * For any valid category and positive points, the CO2 saved should equal
     * points * factor / 10
     */
    @Property(tries = 100)
    void co2CalculationFollowsFormula(
            @ForAll("validCategories") String category,
            @ForAll @IntRange(min = 1, max = 10000) int points) {
        
        double expectedFactor = getExpectedFactor(category);
        double expectedCO2 = points * expectedFactor / 10.0;
        double actualCO2 = CO2Calculator.calculateCO2(category, points);
        
        assertEquals(
            "CO2 calculation should follow formula: points * factor / 10",
            expectedCO2, 
            actualCO2, 
            EPSILON
        );
    }

    /**
     * Property 3.2: CO2 calculation is non-negative for valid inputs
     * For any category and non-negative points, CO2 saved should be >= 0
     */
    @Property(tries = 100)
    void co2CalculationIsNonNegative(
            @ForAll("anyCategory") String category,
            @ForAll @IntRange(min = 0, max = 10000) int points) {
        
        double co2 = CO2Calculator.calculateCO2(category, points);
        assertTrue("CO2 saved should be non-negative", co2 >= 0);
    }

    /**
     * Property 3.3: CO2 calculation is proportional to points
     * For any category, doubling points should double CO2 saved
     */
    @Property(tries = 100)
    void co2CalculationIsProportionalToPoints(
            @ForAll("validCategories") String category,
            @ForAll @IntRange(min = 1, max = 5000) int points) {
        
        double co2Single = CO2Calculator.calculateCO2(category, points);
        double co2Double = CO2Calculator.calculateCO2(category, points * 2);
        
        assertEquals(
            "Doubling points should double CO2 saved",
            co2Single * 2, 
            co2Double, 
            EPSILON
        );
    }

    /**
     * Property 3.4: CO2 report total equals sum of individual calculations
     * The total CO2 in a report should be the sum of all individual activity CO2 values
     */
    @Property(tries = 100)
    void co2ReportTotalEqualsSumOfIndividuals(
            @ForAll @Size(min = 1, max = 20) List<@From("activityData") ActivityData> activities) {
        
        double expectedTotal = 0.0;
        for (ActivityData activity : activities) {
            expectedTotal += CO2Calculator.calculateCO2(activity.category, activity.points);
        }
        
        CO2Calculator.CO2Report report = CO2Calculator.generateReport(expectedTotal);
        
        assertEquals(
            "Report total should equal sum of individual CO2 calculations",
            expectedTotal, 
            report.totalCO2Saved, 
            EPSILON
        );
    }

    /**
     * Property 3.5: CO2 equivalents are calculated correctly
     * Trees equivalent = totalCO2 / 21.77
     * Km not driven = totalCO2 / 0.21
     * kWh saved = totalCO2 / 0.5
     */
    @Property(tries = 100)
    void co2EquivalentsAreCalculatedCorrectly(
            @ForAll @DoubleRange(min = 0.0, max = 10000.0) double totalCO2) {
        
        CO2Calculator.CO2Report report = CO2Calculator.generateReport(totalCO2);
        
        double expectedTrees = totalCO2 > 0 ? totalCO2 / KG_CO2_PER_TREE_PER_YEAR : 0.0;
        double expectedKm = totalCO2 > 0 ? totalCO2 / KG_CO2_PER_KM_DRIVEN : 0.0;
        double expectedKwh = totalCO2 > 0 ? totalCO2 / KG_CO2_PER_KWH : 0.0;
        
        assertEquals("Trees equivalent should be totalCO2 / 21.77", 
            expectedTrees, report.treesEquivalent, EPSILON);
        assertEquals("Km not driven should be totalCO2 / 0.21", 
            expectedKm, report.kmNotDriven, EPSILON);
        assertEquals("kWh saved should be totalCO2 / 0.5", 
            expectedKwh, report.kwhSaved, EPSILON);
    }

    /**
     * Property 3.6: Unknown categories use default factor
     * For any unknown category, the default factor of 0.1 should be used
     */
    @Property(tries = 100)
    void unknownCategoriesUseDefaultFactor(
            @ForAll("unknownCategories") String category,
            @ForAll @IntRange(min = 1, max = 10000) int points) {
        
        double expectedCO2 = points * DEFAULT_FACTOR / 10.0;
        double actualCO2 = CO2Calculator.calculateCO2(category, points);
        
        assertEquals(
            "Unknown categories should use default factor 0.1",
            expectedCO2, 
            actualCO2, 
            EPSILON
        );
    }

    // --- Arbitrary Providers ---

    @Provide
    Arbitrary<String> validCategories() {
        return Arbitraries.of(VALID_CATEGORIES);
    }

    @Provide
    Arbitrary<String> anyCategory() {
        return Arbitraries.oneOf(
            Arbitraries.of(VALID_CATEGORIES),
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
        );
    }

    @Provide
    Arbitrary<String> unknownCategories() {
        return Arbitraries.strings()
            .alpha()
            .ofMinLength(1)
            .ofMaxLength(20)
            .filter(s -> !VALID_CATEGORIES.contains(s.toLowerCase()));
    }

    @Provide
    Arbitrary<ActivityData> activityData() {
        return Combinators.combine(
            Arbitraries.of(VALID_CATEGORIES),
            Arbitraries.integers().between(1, 1000)
        ).as(ActivityData::new);
    }

    // --- Helper Methods ---

    private double getExpectedFactor(String category) {
        switch (category.toLowerCase()) {
            case "transport": return TRANSPORT_FACTOR;
            case "energy": return ENERGY_FACTOR;
            case "water": return WATER_FACTOR;
            case "waste": return WASTE_FACTOR;
            case "green": return GREEN_FACTOR;
            case "consumption": return CONSUMPTION_FACTOR;
            default: return DEFAULT_FACTOR;
        }
    }

    // --- Helper Classes ---

    static class ActivityData {
        final String category;
        final int points;

        ActivityData(String category, int points) {
            this.category = category;
            this.points = points;
        }
    }
}
