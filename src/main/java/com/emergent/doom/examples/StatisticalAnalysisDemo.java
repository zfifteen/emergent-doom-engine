package com.emergent.doom.examples;

import com.emergent.doom.statistics.StatisticalTests;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration of statistical analysis capabilities.
 * 
 * <p>This example shows how to use the StatisticalTests utility to perform
 * statistical analysis similar to Table 1 in the paper (p.10), comparing
 * cell-view vs traditional sorting algorithms.</p>
 */
public class StatisticalAnalysisDemo {
    private static final Logger logger = LoggerFactory.getLogger(StatisticalAnalysisDemo.class);
    
    public static void main(String[] args) {
        logger.info("=".repeat(70));
        logger.info("Statistical Analysis Demonstration");
        logger.info("Emergent Doom Engine - Category 7 Implementation");
        logger.info("=".repeat(70));
        logger.info("");
        
        // Example 1: Z-Score Calculation
        // Simulate comparing Selection sort (cell-view vs traditional)
        // Traditional selection: ~100 swaps
        // Cell-view selection: ~1100 swaps
        demonstrateZScore();
        
        // Example 2: One-Sample T-Test
        demonstrateTTest();
        
        // Example 3: Two-Sample Comparison
        demonstrateTwoSampleComparison();
        
        // Example 4: Confidence Intervals
        demonstrateConfidenceIntervals();
        
        logger.info("\n" + "=".repeat(70));
        logger.info("Statistical Analysis Demo Complete");
        logger.info("=".repeat(70));
    }
    
    private static void demonstrateZScore() {
        logger.info("Example 1: Z-Score Calculation");
        logger.info("-".repeat(70));
        
        // Simulate results from 100 experiments
        // Traditional selection sort: mean ~100 swaps, stddev ~5
        // Cell-view selection sort: mean ~1100 swaps
        
        double cellViewMean = 1100.0;
        double traditionalMean = 100.0;
        double traditionalStdDev = 5.0;
        int sampleSize = 100;
        
        double zScore = StatisticalTests.calculateZScore(
            cellViewMean, traditionalMean, traditionalStdDev, sampleSize
        );
        
        logger.info("Comparing Selection Sort Implementations:");
        logger.info("  Traditional mean:   {} swaps", traditionalMean);
        logger.info("  Cell-view mean:     {} swaps", cellViewMean);
        logger.info("  Traditional stddev: {}", traditionalStdDev);
        logger.info("  Sample size:        {}", sampleSize);
        logger.info("");
        logger.info("  Z-Score:            {:.2f}", zScore);
        
        if (Math.abs(zScore) > 2.58) {
            logger.info("  Significance:       HIGHLY SIGNIFICANT (p < 0.01)");
        } else if (Math.abs(zScore) > 1.96) {
            logger.info("  Significance:       SIGNIFICANT (p < 0.05)");
        } else {
            logger.info("  Significance:       NOT SIGNIFICANT");
        }
        
        logger.info("\nInterpretation: Cell-view selection sort requires significantly");
        logger.info("more swaps due to lack of global knowledge (10x difference).");
        logger.info("");
    }
    
    private static void demonstrateTTest() {
        logger.info("Example 2: One-Sample T-Test");
        logger.info("-".repeat(70));
        
        // Sample data: bubble sort swap counts from 10 trials
        List<Double> bubbleSwaps = Arrays.asList(
            2450.0, 2510.0, 2480.0, 2490.0, 2520.0,
            2475.0, 2505.0, 2495.0, 2485.0, 2500.0
        );
        
        double expectedMean = 2500.0; // Traditional bubble sort
        
        double pValue = StatisticalTests.tTestOneSample(bubbleSwaps, expectedMean);
        double sampleMean = StatisticalTests.calculateMean(bubbleSwaps);
        double sampleStdDev = StatisticalTests.calculateStdDev(bubbleSwaps);
        
        logger.info("Cell-view Bubble Sort Analysis:");
        logger.info("  Sample size:        {}", bubbleSwaps.size());
        logger.info("  Sample mean:        {:.2f}", sampleMean);
        logger.info("  Sample stddev:      {:.2f}", sampleStdDev);
        logger.info("  Expected mean:      {}", expectedMean);
        logger.info("");
        logger.info("  T-Test p-value:     {:.4f}", pValue);
        
        boolean significant = StatisticalTests.isSignificant(pValue, 0.05);
        logger.info("  Significant (α=0.05): {}", (significant ? "YES" : "NO"));
        
        logger.info("\nInterpretation: {}Cell-view bubble sort differs significantly from traditional.",
            (significant ? "" : "No "));
        logger.info("");
    }
    
    private static void demonstrateTwoSampleComparison() {
        logger.info("Example 3: Two-Sample Comparison");
        logger.info("-".repeat(70));
        
        // Compare bubble vs insertion sort (cell-view)
        List<Double> bubbleSwaps = Arrays.asList(
            2450.0, 2510.0, 2480.0, 2490.0, 2520.0
        );
        
        List<Double> insertionSwaps = Arrays.asList(
            2460.0, 2500.0, 2485.0, 2495.0, 2515.0
        );
        
        double pValue = StatisticalTests.tTestTwoSample(bubbleSwaps, insertionSwaps);
        
        double bubbleMean = StatisticalTests.calculateMean(bubbleSwaps);
        double insertionMean = StatisticalTests.calculateMean(insertionSwaps);
        
        logger.info("Comparing Cell-View Algorithms:");
        logger.info("  Bubble mean:        {:.2f}", bubbleMean);
        logger.info("  Insertion mean:     {:.2f}", insertionMean);
        logger.info("");
        logger.info("  T-Test p-value:     {:.4f}", pValue);
        
        boolean significant = StatisticalTests.isSignificant(pValue, 0.05);
        logger.info("  Significant (α=0.05): {}", (significant ? "YES" : "NO"));
        
        logger.info("\nInterpretation: {}The two algorithms show significantly different performance.",
            (significant ? "" : "No "));
        logger.info("");
    }
    
    private static void demonstrateConfidenceIntervals() {
        logger.info("Example 4: Confidence Intervals");
        logger.info("-".repeat(70));
        
        List<Double> swapCounts = Arrays.asList(
            2450.0, 2510.0, 2480.0, 2490.0, 2520.0,
            2475.0, 2505.0, 2495.0, 2485.0, 2500.0
        );
        
        double mean = StatisticalTests.calculateMean(swapCounts);
        double stdDev = StatisticalTests.calculateStdDev(swapCounts);
        
        double[] ci95 = StatisticalTests.calculateConfidenceInterval(
            mean, stdDev, swapCounts.size(), 0.95
        );
        
        double[] ci99 = StatisticalTests.calculateConfidenceInterval(
            mean, stdDev, swapCounts.size(), 0.99
        );
        
        logger.info("Swap Count Analysis:");
        logger.info("  Sample size:        {}", swapCounts.size());
        logger.info("  Mean:               {:.2f}", mean);
        logger.info("  Std deviation:      {:.2f}", stdDev);
        logger.info("");
        logger.info("  95% Confidence Interval: [{} , {} ]", 
            String.format("%.2f", ci95[0]), String.format("%.2f", ci95[1]));
        logger.info("  99% Confidence Interval: [{} , {} ]", 
            String.format("%.2f", ci99[0]), String.format("%.2f", ci99[1]));
        
        logger.info("\nInterpretation: We are 95% confident the true mean lies");
        logger.info("between {} and {} swaps.", String.format("%.2f", ci95[0]), 
            String.format("%.2f", ci95[1]));
        logger.info("");
    }
}
