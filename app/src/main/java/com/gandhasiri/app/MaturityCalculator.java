package com.gandhasiri.app;

import java.util.concurrent.TimeUnit;

public class MaturityCalculator {

    // Sandalwood (Santalum album) Valuation & Maturity Logic
    
    public static double calculateMaturityPercentage(double girth, long datePlantedMillis) {
        long ageMillis = System.currentTimeMillis() - datePlantedMillis;
        long ageYears = TimeUnit.MILLISECONDS.toDays(ageMillis) / 365;
        double girthProgress = Math.min(100.0, (girth / 60.0) * 100.0);
        double ageProgress = Math.min(100.0, (ageYears / 20.0) * 100.0);
        return (girthProgress * 0.6) + (ageProgress * 0.4);
    }

    public static int estimateYearsToHarvest(double girth, long datePlantedMillis) {
        long ageMillis = System.currentTimeMillis() - datePlantedMillis;
        long ageYears = TimeUnit.MILLISECONDS.toDays(ageMillis) / 365;
        int yearsLeft = (int) (20 - ageYears);
        return Math.max(0, yearsLeft);
    }

    public static double calculateTreeValue(double girth) {
        // Base Ecological Value (Soil, Sapling, Protection)
        double baseValue = 2500.0;
        
        // Biological Growth Value (Value increases as trunk thickens)
        double growthValue = girth * 450.0;
        
        // Premium Heartwood Value (Significant after 30cm girth)
        double heartwoodWeight = estimateHeartwoodWeight(girth);
        double heartwoodValue = heartwoodWeight * 16500; // Market rate per kg
        
        return baseValue + growthValue + heartwoodValue;
    }

    public static double estimateHeartwoodWeight(double girth) {
        if (girth < 30) return 0;
        // Approximation formula for heartwood mass
        return (girth - 30) * 0.85;
    }
}
