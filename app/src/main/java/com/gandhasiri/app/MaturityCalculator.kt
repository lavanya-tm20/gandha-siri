package com.gandhasiri.app

import java.util.concurrent.TimeUnit

object MaturityCalculator {

    // Sandalwood (Santalum album) Valuation & Maturity Logic
    
    fun calculateMaturityPercentage(girth: Double, datePlantedMillis: Long): Double {
        val ageMillis = System.currentTimeMillis() - datePlantedMillis
        val ageYears = TimeUnit.MILLISECONDS.toDays(ageMillis) / 365
        val girthProgress = Math.min(100.0, (girth / 60.0) * 100.0)
        val ageProgress = Math.min(100.0, (ageYears / 20.0).toDouble() * 100.0)
        return (girthProgress * 0.6) + (ageProgress * 0.4)
    }

    fun estimateYearsToHarvest(girth: Double, datePlantedMillis: Long): Int {
        val ageMillis = System.currentTimeMillis() - datePlantedMillis
        val ageYears = TimeUnit.MILLISECONDS.toDays(ageMillis) / 365
        val yearsLeft = (20 - ageYears).toInt()
        return Math.max(0, yearsLeft)
    }

    fun calculateTreeValue(girth: Double): Double {
        // Base Ecological Value (Soil, Sapling, Protection)
        val baseValue = 2500.0
        
        // Biological Growth Value (Value increases as trunk thickens)
        val growthValue = girth * 450.0
        
        // Premium Heartwood Value (Significant after 30cm girth)
        val heartwoodWeight = estimateHeartwoodWeight(girth)
        val heartwoodValue = heartwoodWeight * 16500 // Market rate per kg
        
        return baseValue + growthValue + heartwoodValue
    }

    fun estimateHeartwoodWeight(girth: Double): Double {
        if (girth < 30) return 0.0
        // Approximation formula for heartwood mass
        return (girth - 30) * 0.85
    }
}
