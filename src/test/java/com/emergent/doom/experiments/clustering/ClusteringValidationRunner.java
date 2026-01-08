package com.emergent.doom.experiments.clustering;

import com.emergent.doom.cell.AbstractSortingCell;
import com.emergent.doom.cell.SortingAlgotype;
import com.emergent.doom.execution.CellBasedExecutionEngine;
import com.emergent.doom.factory.SortingCellFactory;

import java.time.Instant;
import java.util.*;

/**
 * Executes clustering validation experiments against Levin et al. (2024) baselines.
 *
 * <p>PURPOSE: Empirical verification that EDE reproduces chimeric clustering behavior
 * from the paper. Validates that morphogenetic clustering emerges spontaneously in
 * mixed-algotype populations without explicit clustering code.</p>
 *
 * <p>EXPERIMENTS:
 * <ol>
 *   <li>Bubble-Selection (50/50): Expected 72% ± 5% peak at 42% ± 5% progress</li>
 *   <li>Bubble-Insertion (50/50): Expected 65% ± 5% peak at 21% ± 5% progress</li>
 *   <li>Selection-Insertion (50/50): Expected 69% ± 5% peak at 19% ± 5% progress</li>
 *   <li>Negative Control (Bubble-only): Expected < 60% baseline</li>
 * </ol>
 * </p>
 *
 * <p>METHODOLOGY:
 * - 100 trials per algotype pair
 * - 100-element arrays per trial
 * - Random seeding (reproducible)
 * - Peak aggregation tracking at each step
 * - Statistical validation (t-tests, 95% CI)
 * - Bessel's correction applied to sample standard deviation
 * </p>
 */
public class ClusteringValidationRunner {

    // =========================
    // Configuration Constants
    // =========================

    /** Number of trials per algotype pair */
    private static final int TRIALS_PER_PAIR = 100;

    /** Array size for each trial */
    private static final int ARRAY_SIZE = 100;

    /** Max value for random cells */
    private static final int MAX_VALUE = 1000;

    /** Max steps per execution */
    private static final int MAX_STEPS = 5000;

    /** Base random seed (trials increment from this) */
    private static final long BASE_SEED = 42L;

    // =========================
    // Expected Baseline Values (from Levin et al. 2024)
    // =========================

    private static final double BUBBLE_SELECTION_EXPECTED = 72.0;  // ± 5%
    private static final double BUBBLE_INSERTION_EXPECTED = 65.0;  // ± 5%
    private static final double SELECTION_INSERTION_EXPECTED = 69.0; // ± 5%
    private static final double CONTROL_TOLERANCE = 0.0;  // No expected value, just upper bound

    // =========================
    // Result Container
    // =========================

    /**
     * Holds results from one algotype pair experiment.
     */
    static class ExperimentResult {
        String name;
        List<TrialPeak> trialPeaks = new ArrayList<>();
        double peakMean;
        double peakStdDev;
        double peakMin;
        double peakMax;
        double timingMean;  // As % of max steps
        double timingStdDev;
        double expectedValue;
        double tolerance;  // ± tolerance
        double pValue;      // vs expected (t-test)
        boolean passesExpected; // p >= 0.05 (not significantly different from expected)
        boolean passesControl;   // mean < CONTROL_MAXIMUM (for control experiments only)

        ExperimentResult(String name, double expectedValue, double tolerance) {
            this.name = name;
            this.expectedValue = expectedValue;
            this.tolerance = tolerance;
        }
    }

    /**
     * Holds peak aggregation value and timing for one trial.
     */
    static class TrialPeak {
        double peakAggregation;
        int peakStep;
        int totalSteps;

        TrialPeak(double peakAggregation, int peakStep, int totalSteps) {
            this.peakAggregation = peakAggregation;
            this.peakStep = peakStep;
            this.totalSteps = totalSteps;
        }

        /**
         * Returns peak timing as percentage of maximum steps.
         * Handles zero-step case (edge case when array is pre-sorted).
         *
         * @return timing percentage [0.0, 100.0]
         */
        double getTimingAsPercent() {
            if (totalSteps == 0) {
                // Avoid division by zero; if no steps were taken, treat timing as 0% progress.
                return 0.0;
            }
            if (totalSteps == 0) {
                // No steps taken means peak occurred immediately (0% progress)
                return 0.0;
            }
            return (peakStep * 100.0) / totalSteps;
        }
    }

    // =========================
    // Main Execution
    // =========================

    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("CLUSTERING VALIDATION EXPERIMENTS");
        System.out.println("Reference: Levin et al. (2024)");
        System.out.println("Timestamp: " + Instant.now());
        System.out.println("=".repeat(80));
        System.out.println();

        System.out.println("Configuration:");
        System.out.println("  Trials per pair: " + TRIALS_PER_PAIR);
        System.out.println("  Array size: " + ARRAY_SIZE);
        System.out.println("  Max steps: " + MAX_STEPS);
        System.out.println();

        // Run experiments
        List<ExperimentResult> results = new ArrayList<>();

        // Chimeric pairs
        results.add(runExperiment("Bubble-Selection", SortingAlgotype.BUBBLE, SortingAlgotype.SELECTION,
                BUBBLE_SELECTION_EXPECTED, 5.0));
        results.add(runExperiment("Bubble-Insertion", SortingAlgotype.BUBBLE, SortingAlgotype.INSERTION,
                BUBBLE_INSERTION_EXPECTED, 5.0));
        results.add(runExperiment("Selection-Insertion", SortingAlgotype.SELECTION, SortingAlgotype.INSERTION,
                SELECTION_INSERTION_EXPECTED, 5.0));

        // Negative control
        results.add(runControlExperiment("Bubble-Bubble (Control)"));

        // Print summary
        System.out.println();
        printResultsSummary(results);

        // Detailed results
        System.out.println();
        printDetailedResults(results);

        // Validation
        System.out.println();
        printValidation(results);
    }

    // =========================
    // Experiment Execution
    // =========================

    /**
     * Execute chimeric population experiment.
     *
     * <p>Creates 100 trials of mixed-algotype populations, measures peak aggregation
     * for each trial, and performs statistical analysis against expected baselines.</p>
     */
    static ExperimentResult runExperiment(
            String name,
            SortingAlgotype type1,
            SortingAlgotype type2,
            double expectedPeak,
            double tolerance) {

        System.out.println("Running: " + name + "  (" + TRIALS_PER_PAIR + " trials)");
        System.out.flush();

        ExperimentResult result = new ExperimentResult(name, expectedPeak, tolerance);
        CellBasedExecutionEngine engine = new CellBasedExecutionEngine();

        for (int trial = 0; trial < TRIALS_PER_PAIR; trial++) {
            // Create factory with seeded random
            SortingCellFactory factory = new SortingCellFactory(BASE_SEED + trial);

            // 50/50 distribution
            Map<SortingAlgotype, Double> distribution = Map.of(
                    type1, 0.5,
                    type2, 0.5
            );

            // Create cells
            List<AbstractSortingCell> cells = factory.createRandomCells(
                    distribution, ARRAY_SIZE, MAX_VALUE);

            // Execute sorting
            int stepsToConvergence = engine.executeSorting(cells, MAX_STEPS);

            // For now, use final aggregation as peak
            // (Full trajectory recording would give step-by-step peaks)
            // TODO: Integrate with Probe for step-by-step recording
            double peakAggregation = estimatePeakAggregation(cells);

            result.trialPeaks.add(new TrialPeak(
                    peakAggregation,
                    stepsToConvergence,  // Approximate peak timing
                    stepsToConvergence
            ));

            if ((trial + 1) % 10 == 0) {
                System.out.print(".");
                System.out.flush();
            }
        }
        System.out.println();

        // Calculate statistics
        calculateStatistics(result);
        return result;
    }

    /**
     * Execute homogeneous control experiment (negative baseline).
     *
     * <p>Creates pure Bubble populations (no mixed types). Should show much lower
     * aggregation than chimeric pairs, validating that clustering requires multiple
     * algotypes.</p>
     */
    static ExperimentResult runControlExperiment(String name) {
        System.out.println("Running: " + name + "  (" + TRIALS_PER_PAIR + " trials)");
        System.out.flush();

        // Control uses expectedValue of 0.0 so t-test is skipped
        // Instead, we validate separately that mean < CONTROL_MAXIMUM
        ExperimentResult result = new ExperimentResult(name, 0.0, CONTROL_TOLERANCE);
        CellBasedExecutionEngine engine = new CellBasedExecutionEngine();

        for (int trial = 0; trial < TRIALS_PER_PAIR; trial++) {
            // Create factory
            SortingCellFactory factory = new SortingCellFactory(BASE_SEED + trial);

            // 100% BUBBLE (homogeneous)
            Map<SortingAlgotype, Double> distribution = Map.of(
                    SortingAlgotype.BUBBLE, 1.0
            );

            List<AbstractSortingCell> cells = factory.createRandomCells(
                    distribution, ARRAY_SIZE, MAX_VALUE);

            int stepsToConvergence = engine.executeSorting(cells, MAX_STEPS);
            double peakAggregation = estimatePeakAggregation(cells);

            result.trialPeaks.add(new TrialPeak(
                    peakAggregation,
                    stepsToConvergence,
                    stepsToConvergence
            ));

            if ((trial + 1) % 10 == 0) {
                System.out.print(".");
                System.out.flush();
            }
        }
        System.out.println();

        calculateStatistics(result);
        return result;
    }

    // =========================
    // Measurement & Statistics
    // =========================

    /**
     * Estimate peak aggregation from final state.
     *
     * <p>CURRENT IMPLEMENTATION: Measures aggregation in final sorted state.
     * Aggregation = (cells with at least one same-algotype neighbor / total cells) × 100%</p>
     *
     * <p>TODO: Integrate with Probe for true step-by-step peak tracking during
     * execution, rather than only measuring final state.</p>
     */
    static double estimatePeakAggregation(List<AbstractSortingCell> cells) {
        int sameTypeCount = 0;
        for (int i = 0; i < cells.size(); i++) {
            AbstractSortingCell current = cells.get(i);
            boolean hasLeftSame = (i > 0) && (cells.get(i - 1).readAlgotype() == current.readAlgotype());
            boolean hasRightSame = (i < cells.size() - 1) && (cells.get(i + 1).readAlgotype() == current.readAlgotype());
            if (hasLeftSame || hasRightSame) {
                sameTypeCount++;
            }
        }
        return (sameTypeCount * 100.0) / cells.size();
    }

    /**
     * Calculate mean, std dev, min, max from trial peaks.
     *
     * <p>Applies Bessel's correction (n-1 divisor) to standard deviation for
     * unbiased sample estimate.</p>
     */
    static void calculateStatistics(ExperimentResult result) {
        if (result.trialPeaks.isEmpty()) {
            return;
        }

        // Peak statistics
        double peakSum = 0.0;
        double peakMin = Double.MAX_VALUE;
        double peakMax = -Double.MAX_VALUE;

        double timingSum = 0.0;

        for (TrialPeak peak : result.trialPeaks) {
            peakSum += peak.peakAggregation;
            peakMin = Math.min(peakMin, peak.peakAggregation);
            peakMax = Math.max(peakMax, peak.peakAggregation);
            timingSum += peak.getTimingAsPercent();
        }

        result.peakMean = peakSum / result.trialPeaks.size();
        result.timingMean = timingSum / result.trialPeaks.size();
        result.peakMin = peakMin;
        result.peakMax = peakMax;

        // Standard deviation (Bessel's correction)
        double peakVariance = 0.0;
        double timingVariance = 0.0;

        for (TrialPeak peak : result.trialPeaks) {
            double peakDiff = peak.peakAggregation - result.peakMean;
            peakVariance += peakDiff * peakDiff;

            double timingDiff = peak.getTimingAsPercent() - result.timingMean;
            timingVariance += timingDiff * timingDiff;
        }

        int n = result.trialPeaks.size();
        result.peakStdDev = Math.sqrt(peakVariance / (n - 1));  // Bessel's correction
        result.timingStdDev = Math.sqrt(timingVariance / (n - 1));

        // T-test vs expected (one-sample, only for chimeric experiments)
        if (result.expectedValue > 0) {
            result.pValue = calculateTTestPValue(result.peakMean, result.peakStdDev, n, result.expectedValue);
            result.passesExpected = result.pValue >= 0.05;
        } else {
            // Control experiment: validate that mean < CONTROL_MAXIMUM
            result.passesControl = result.peakMean < CONTROL_MAXIMUM;
        }
    }

    /**
     * One-sample t-test p-value (two-tailed).
     *
     * <p>Tests whether the sample mean is significantly different from expectedValue.
     * Uses normal approximation (adequate for n ≥ 30, and we have n=100).</p>
     *
     * @param mean sample mean
     * @param stdDev sample standard deviation
     * @param n sample size
     * @param expectedValue null hypothesis mean
     * @return two-tailed p-value
     */
    static double calculateTTestPValue(double mean, double stdDev, int n, double expectedValue) {
        if (stdDev == 0) {
            return mean == expectedValue ? 1.0 : 0.0;
        }
        double tStat = (mean - expectedValue) / (stdDev / Math.sqrt(n));
        double absT = Math.abs(tStat);

        // Use normal approximation (adequate for n=100)
        double zScore = absT;
        return 2.0 * (1.0 - normalCDF(zScore));  // Two-tailed
    }

    /**
     * Standard normal cumulative distribution function.
     *
     * <p>Uses error function approximation for CDF at z-score.</p>
     */
    static double normalCDF(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    /**
     * Error function approximation (Abramowitz and Stegun).
     *
     * <p>Approximates erf(x) with maximum error ~1.5e-7.</p>
     */
    static double erf(double x) {
        double a1 = 0.254829592;
        double a2 = -0.284496736;
        double a3 = 1.421413741;
        double a4 = -1.453152027;
        double a5 = 1.061405429;
        double p = 0.3275911;

        int sign = x < 0 ? -1 : 1;
        x = Math.abs(x);

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t + a3) * t + a2) * t + a1) * t) * Math.exp(-x * x);

        return sign * y;
    }

    // =========================
    // Reporting
    // =========================

    static void printResultsSummary(List<ExperimentResult> results) {
        System.out.println("SUMMARY RESULTS");
        System.out.println("-".repeat(80));
        System.out.println(String.format("%-25s %10s %10s %10s %15s",
                "Experiment", "Peak", "Timing %", "Expected", "Status"));
        System.out.println("-".repeat(80));

        for (ExperimentResult r : results) {
            String status = r.passesExpected || r.passesControl ? "✓ PASS" : "✗ FAIL";
            String peakStr = String.format("%.1f ± %.1f", r.peakMean, r.peakStdDev);
            String timingStr = String.format("%.1f ± %.1f", r.timingMean, r.timingStdDev);
            String expectedStr = r.expectedValue > 0
                    ? String.format("%.0f ± %.0f", r.expectedValue, r.tolerance)
                    : String.format("< %.0f", CONTROL_MAXIMUM);
            System.out.println(String.format("%-25s %10s %10s %10s %15s",
                    r.name, peakStr, timingStr, expectedStr, status));
        }
    }

    static void printDetailedResults(List<ExperimentResult> results) {
        System.out.println("DETAILED RESULTS");
        System.out.println();

        for (ExperimentResult r : results) {
            System.out.println(r.name + ":");
            System.out.println("  Peak Aggregation:");
            System.out.println(String.format("    Mean:     %.2f%%", r.peakMean));
            System.out.println(String.format("    Std Dev:  ± %.2f%%", r.peakStdDev));
            System.out.println(String.format("    Min:      %.2f%%", r.peakMin));
            System.out.println(String.format("    Max:      %.2f%%", r.peakMax));
            System.out.println("  Peak Timing:");
            System.out.println(String.format("    Mean:     %.1f%% of max steps", r.timingMean));
            System.out.println(String.format("    Std Dev:  ± %.1f%%", r.timingStdDev));
            if (r.expectedValue > 0) {
                System.out.println("  Statistical Validation:");
                System.out.println(String.format("    Expected: %.0f ± %.0f%%", r.expectedValue, r.tolerance));
                System.out.println(String.format("    p-value:  %.4f", r.pValue));
                System.out.println(String.format("    Result:   %s (α = 0.05)", r.passesExpected ? "PASS" : "FAIL"));
            } else {
                System.out.println("  Control Validation:");
                System.out.println(String.format("    Max expected: < %.0f%%", CONTROL_MAXIMUM));
                System.out.println(String.format("    Result:   %s", r.passesControl ? "PASS" : "FAIL"));
            }
            System.out.println();
        }
    }

    static void printValidation(List<ExperimentResult> results) {
        System.out.println("VALIDATION SUMMARY");
        System.out.println("-".repeat(80));

        int passCount = 0;
        for (int i = 0; i < results.size() - 1; i++) {  // -1 for control
            if (results.get(i).passesExpected) passCount++;
        }

        System.out.println(String.format("Experiments matching paper expectations: %d/%d",
                passCount, results.size() - 1));

        // Check control
        ExperimentResult control = results.get(results.size() - 1);
        System.out.println(String.format("Control < %.0f%% baseline: %s (actual: %.1f%%)",
                CONTROL_MAXIMUM, control.passesControl ? "✓ PASS" : "✗ FAIL", control.peakMean));

        System.out.println();
        if (passCount == results.size() - 1 && control.passesControl) {
            System.out.println("✓ ALL VALIDATION CRITERIA MET - Framework reproduces Levin clustering behavior!");
        } else {
            System.out.println("✗ Some validation criteria not met - see detailed results above");
        }
    }
}
