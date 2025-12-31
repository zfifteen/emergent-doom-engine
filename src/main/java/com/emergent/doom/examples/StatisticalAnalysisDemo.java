package com.emergent.doom.examples;

import com.emergent.doom.statistics.StatisticalTests;

import java.util.Arrays;
import java.util.List;

/**
 * Demonstration of statistical analysis capabilities.
 * 
 * <p>This example shows how to use the StatisticalTests utility to perform
 * statistical analysis similar to Table 1 in the paper (p.10), comparing
 * cell-view vs traditional sorting algorithms.</p>
 */
public class StatisticalAnalysisDemo {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("Statistical Analysis Demonstration");
        System.out.println("Emergent Doom Engine - Category 7 Implementation");
        System.out.println("=".repeat(70));
        System.out.println();
        
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
        
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Statistical Analysis Demo Complete");
        System.out.println("=".repeat(70));
    }
    
    private static void demonstrateZScore() {
        System.out.println("Example 1: Z-Score Calculation");
        System.out.println("-".repeat(70));
        
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
        
        System.out.println("Comparing Selection Sort Implementations:");
        System.out.println("  Traditional mean:   " + traditionalMean + " swaps");
        System.out.println("  Cell-view mean:     " + cellViewMean + " swaps");
        System.out.println("  Traditional stddev: " + traditionalStdDev);
        System.out.println("  Sample size:        " + sampleSize);
        System.out.println();
        System.out.println("  Z-Score:            " + String.format("%.2f", zScore));
        
        if (Math.abs(zScore) > 2.58) {
            System.out.println("  Significance:       HIGHLY SIGNIFICANT (p < 0.01)");
        } else if (Math.abs(zScore) > 1.96) {
            System.out.println("  Significance:       SIGNIFICANT (p < 0.05)");
        } else {
            System.out.println("  Significance:       NOT SIGNIFICANT");
        }
        
        System.out.println("\nInterpretation: Cell-view selection sort requires significantly");
        System.out.println("more swaps due to lack of global knowledge (10x difference).");
        System.out.println();
    }
    
    private static void demonstrateTTest() {
        System.out.println("Example 2: One-Sample T-Test");
        System.out.println("-".repeat(70));
        
        // Sample data: bubble sort swap counts from 10 trials
        List<Double> bubbleSwaps = Arrays.asList(
            2450.0, 2510.0, 2480.0, 2490.0, 2520.0,
            2475.0, 2505.0, 2495.0, 2485.0, 2500.0
        );
        
        double expectedMean = 2500.0; // Traditional bubble sort
        
        double pValue = StatisticalTests.tTestOneSample(bubbleSwaps, expectedMean);
        double sampleMean = StatisticalTests.calculateMean(bubbleSwaps);
        double sampleStdDev = StatisticalTests.calculateStdDev(bubbleSwaps);
        
        System.out.println("Cell-view Bubble Sort Analysis:");
        System.out.println("  Sample size:        " + bubbleSwaps.size());
        System.out.println("  Sample mean:        " + String.format("%.2f", sampleMean));
        System.out.println("  Sample stddev:      " + String.format("%.2f", sampleStdDev));
        System.out.println("  Expected mean:      " + expectedMean);
        System.out.println();
        System.out.println("  T-Test p-value:     " + String.format("%.4f", pValue));
        
        boolean significant = StatisticalTests.isSignificant(pValue, 0.05);
        System.out.println("  Significant (α=0.05): " + (significant ? "YES" : "NO"));
        
        System.out.println("\nInterpretation: " + 
            (significant ? "Cell-view bubble sort differs significantly from traditional."
                        : "Cell-view bubble sort performs similarly to traditional."));
        System.out.println();
    }
    
    private static void demonstrateTwoSampleComparison() {
        System.out.println("Example 3: Two-Sample Comparison");
        System.out.println("-".repeat(70));
        
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
        
        System.out.println("Comparing Cell-View Algorithms:");
        System.out.println("  Bubble mean:        " + String.format("%.2f", bubbleMean));
        System.out.println("  Insertion mean:     " + String.format("%.2f", insertionMean));
        System.out.println();
        System.out.println("  T-Test p-value:     " + String.format("%.4f", pValue));
        
        boolean significant = StatisticalTests.isSignificant(pValue, 0.05);
        System.out.println("  Significant (α=0.05): " + (significant ? "YES" : "NO"));
        
        System.out.println("\nInterpretation: " + 
            (significant ? "The two algorithms show significantly different performance."
                        : "The two algorithms show similar performance."));
        System.out.println();
    }
    
    private static void demonstrateConfidenceIntervals() {
        System.out.println("Example 4: Confidence Intervals");
        System.out.println("-".repeat(70));
        
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
        
        System.out.println("Swap Count Analysis:");
        System.out.println("  Sample size:        " + swapCounts.size());
        System.out.println("  Mean:               " + String.format("%.2f", mean));
        System.out.println("  Std deviation:      " + String.format("%.2f", stdDev));
        System.out.println();
        System.out.println("  95% Confidence Interval: [" + 
            String.format("%.2f", ci95[0]) + ", " + 
            String.format("%.2f", ci95[1]) + "]");
        System.out.println("  99% Confidence Interval: [" + 
            String.format("%.2f", ci99[0]) + ", " + 
            String.format("%.2f", ci99[1]) + "]");
        
        System.out.println("\nInterpretation: We are 95% confident the true mean lies");
        System.out.println("between " + String.format("%.2f", ci95[0]) + 
            " and " + String.format("%.2f", ci95[1]) + " swaps.");
        System.out.println();
    }
}
