package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.execution.GenericExecutionEngine;

import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Collections;

/**
 * Aggregation Threshold Sweep Experiment - Hypothesis 1
 *
 * <p><strong>HYPOTHESIS:</strong> There exists a critical aggregation threshold below which
 * factor localization converges successfully. Phase 3 findings show sigmoid transition:
 * <ul>
 *   <li>C3 (0% aggregation):   50% convergence</li>
 *   <li>C2 (75% aggregation):  0% convergence</li>
 *   <li>C1 (50% aggregation):  0% convergence</li>
 * </ul>
 * This experiment sweeps aggregation levels [0%, 5%, 10%, 15%, 20%, 25%, 30%, 40%, 50%]
 * to identify the transition point using computationally challenging 6, 7, and 8-digit semiprimes.</p>
 *
 * <p><strong>SCIENTIFIC CONTEXT:</strong></p>
 * <ul>
 *   <li><strong>Reference:</strong> Phase 3 Clustering vs Fitness Experiment (PR #169, merged 2026-01-12)</li>
 *   <li><strong>Research Question:</strong> At what aggregation level does factor localization fail?</li>
 *   <li><strong>Predicted Outcome:</strong> Sigmoid transition with critical threshold shifting LEFT
 *       as semiprime magnitude increases (larger search space requires more fluidity → lower aggregation ceiling)</li>
 * </ul>
 *
 * <p><strong>EXPERIMENT PARAMETERS:</strong></p>
 * <ul>
 *   <li><strong>Target Semiprimes:</strong>
 *     <ul>
 *       <li>6-digit: 143063 (337 × 421), 294409 (503 × 587), 524287 (727 × 721)</li>
 *       <li>7-digit: 1003001 (769 × 1303), 2091667 (1279 × 1637), 5765761 (2399 × 2399)</li>
 *       <li>8-digit: 10003001 (2939 × 3401), 25326001 (4001 × 6329), 99990001 (9901 × 10099)</li>
 *     </ul>
 *   </li>
 *   <li><strong>Aggregation Levels:</strong> 0%, 5%, 10%, 15%, 20%, 25%, 30%, 40%, 50%</li>
 *   <li><strong>Runs per Level:</strong> 30 (for statistical power)</li>
 *   <li><strong>Max Steps:</strong> 500 (increased from 100 for larger search space)</li>
 *   <li><strong>Stagnation Threshold:</strong> 50 consecutive zero-swap steps (scaled from 20)</li>
 *   <li><strong>Convergence Criteria:</strong> Both factors in positions [0, 4]</li>
 * </ul>
 *
 * <p><strong>OUTPUT ARTIFACTS:</strong></p>
 * <pre>
 * experiments/aggregation_threshold_sweep_YYYY_MM_DD/
 * ├── results/
 * │   ├── 6_digit/
 * │   │   ├── agg_00_semiprime_143063_rep_001.csv
 * │   │   ├── agg_05_semiprime_143063_rep_001.csv
 * │   │   └── ...
 * │   ├── 7_digit/
 * │   └── 8_digit/
 * ├── snapshots/
 * └── THRESHOLD_ANALYSIS.md
 * </pre>
 *
 * <p><strong>CSV SCHEMA (v2 format from PR #169):</strong></p>
 * <pre>
 * step,strategy_agg,fitness_clust,factor_local,factor_p_pos,factor_q_pos,
 * mean_factor_dist,fitness_grad_mean,fitness_grad_std,entropy_global,
 * entropy_front,swaps,consec_zero_swaps,stagnant
 * </pre>
 *
 * <p><strong>AS A USER:</strong> I want to identify the critical aggregation threshold
 * where factor localization fails, so that I can design adaptive disaggregation strategies
 * that maintain localization while enabling exploration.</p>
 */
@Tag("aggregation-sweep")
public class AggregationThresholdSweepTest {
    
    // ==================== EXPERIMENT CONFIGURATION ====================
    
    /**
     * Aggregation levels to sweep.
     * 
     * <p><strong>PURPOSE:</strong> Test hypothesis that convergence rate decreases
     * as aggregation increases, with critical threshold somewhere in [0%, 50%] range.</p>
     * 
     * <p><strong>AS A USER:</strong> I want fine-grained resolution near expected transition
     * (0-30%) and coarser resolution in known-failure region (30-50%), so that I can
     * precisely locate the inflection point.</p>
     */
    private static final double[] AGGREGATION_LEVELS = {
        0.00, 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.40, 0.50
    };
    
    /**
     * Number of repetitions per aggregation level per semiprime.
     * 
     * <p><strong>PURPOSE:</strong> Provide statistical power to detect convergence
     * rate differences. 30 runs allow detection of 20% rate differences with p<0.05.</p>
     * 
     * <p><strong>AS A USER:</strong> I want sufficient statistical power to distinguish
     * genuine threshold effects from stochastic noise, so that I can confidently
     * identify the critical aggregation level.</p>
     */
    private static final int REPS_PER_LEVEL = 30;
    
    /**
     * Maximum execution steps per run.
     * 
     * <p><strong>PURPOSE:</strong> Allow more time for larger semiprimes to converge.
     * Increased from 100 (Phase 3) to 500 for 6-8 digit semiprimes.</p>
     * 
     * <p><strong>AS A USER:</strong> I want sufficient execution time for convergence
     * without wasting computational resources on stagnant runs, so that I can
     * accurately measure localization dynamics.</p>
     */
    private static final int MAX_STEPS = 500;
    
    /**
     * Stagnation threshold (consecutive zero-swap steps before declaring stagnation).
     * 
     * <p><strong>PURPOSE:</strong> Detect when run is stuck in local attractor vs
     * still making progress. Scaled from 20 (Phase 3) to 50 for larger search space.</p>
     * 
     * <p><strong>AS A USER:</strong> I want to distinguish genuinely stagnant runs
     * from slow-but-progressing runs, so that I don't prematurely terminate runs
     * that could still converge.</p>
     */
    private static final int STAGNATION_THRESHOLD = 50;
    
    /**
     * Convergence position threshold (both factors must be in [0, CONVERGENCE_POSITION]).
     * 
     * <p><strong>PURPOSE:</strong> Define what "converged" means. Inherited from Phase 3
     * for consistency ([0, 4] = front 10% of 50-cell array).</p>
     * 
     * <p><strong>AS A USER:</strong> I want a consistent convergence criterion across
     * all experiments, so that I can compare results to Phase 3 findings.</p>
     */
    private static final int CONVERGENCE_POSITION = 4;
    
    /**
     * Array size (number of candidate cells).
     * 
     * <p><strong>PURPOSE:</strong> Standard array size from Phase 3 experiments.
     * Kept constant to isolate aggregation as independent variable.</p>
     * 
     * <p><strong>AS A USER:</strong> I want to test aggregation effects while
     * controlling for array size, so that observed differences are attributable
     * to aggregation rather than scale effects.</p>
     */
    private static final int ARRAY_SIZE = 50;
    
    // ==================== SEMIPRIME DEFINITIONS ====================
    
    /**
     * 6-digit semiprimes for testing.
     * 
     * <p><strong>SELECTION CRITERIA:</strong>
     * <ul>
     *   <li>All products of two primes (validated)</li>
     *   <li>Factors in range [100, 1000] for 6-digit products</li>
     *   <li>Diverse factor magnitudes to test robustness</li>
     * </ul>
     * 
     * <p><strong>AS A USER:</strong> I want validated semiprime targets that span
     * different factor distributions, so that results generalize across 6-digit range.</p>
     */
    private static final int[][] SEMIPRIMES_6_DIGIT = {
        {184507, 307, 601},    // 307 × 601 = 184,507
        {186349, 307, 607},    // 307 × 607 = 186,349
        {188191, 307, 613}     // 307 × 613 = 188,191
    };
    
    /**
     * 7-digit semiprimes for testing.
     * 
     * <p><strong>SELECTION CRITERIA:</strong>
     * <ul>
     *   <li>Products of two primes</li>
     *   <li>Factors in range [500, 5000] for 7-digit products</li>
     *   <li>Avoids perfect squares for factor disambiguation</li>
     * </ul>
     * 
     * <p><strong>AS A USER:</strong> I want 7-digit semiprimes that test scaling
     * effects on aggregation threshold, so that I can model how threshold shifts
     * with problem magnitude.</p>
     */
    private static final int[][] SEMIPRIMES_7_DIGIT = {
        {1007509, 503, 2003},     // 503 × 2003 = 1,007,509
        {1011533, 503, 2011},     // 503 × 2011 = 1,011,533
        {1014551, 503, 2017}      // 503 × 2017 = 1,014,551
    };
    
    /**
     * 8-digit semiprimes for testing.
     * 
     * <p><strong>SELECTION CRITERIA:</strong>
     * <ul>
     *   <li>Products of two primes</li>
     *   <li>Factors in range [2000, 10000] for 8-digit products</li>
     *   <li>Tests extreme scaling effects on aggregation threshold</li>
     * </ul>
     * 
     * <p><strong>AS A USER:</strong> I want 8-digit semiprimes to test whether
     * aggregation threshold continues shifting leftward at large scales, so that
     * I can validate the log-scale threshold model.</p>
     */
    private static final int[][] SEMIPRIMES_8_DIGIT = {
        {10021009, 2003, 5003},   // 2003 × 5003 = 10,021,009
        {10033027, 2003, 5009},   // 2003 × 5009 = 10,033,027
        {10037033, 2003, 5011}    // 2003 × 5011 = 10,037,033
    };
    
    // ==================== OUTPUT DIRECTORIES ====================
    
    /** Base experiment directory (will be timestamped) */
    // Implementation: Generate timestamp-based directory name in BeforeAll
    private static String experimentDir;
    
    /** Results subdirectory */
    // Implementation: experimentDir + "/results"
    private static String resultsDir;
    
    /** Snapshots subdirectory */
    // Implementation: experimentDir + "/snapshots"
    private static String snapshotsDir;
    
    // ==================== SETUP ====================
    
    /**
     * Initialize experiment directories before any tests run.
     * 
     * <p><strong>PURPOSE:</strong> Create timestamped output directory structure
     * to avoid overwriting previous experiment runs.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Generate timestamp (YYYY_MM_DD format)</li>
     *   <li>Create base directory: experiments/aggregation_threshold_sweep_TIMESTAMP</li>
     *   <li>Create subdirectories: results/ (with 6_digit/, 7_digit/, 8_digit/), snapshots/</li>
     *   <li>Print directory locations for user reference</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want separate timestamped directories for each
     * experimental run, so that I can compare results across different runs without
     * data loss or confusion.</p>
     */
    @BeforeAll
    static void setupExperimentDirectories() throws IOException {
        // Generate timestamp using LocalDateTime
        // Purpose: Create unique directory for this experimental run to avoid overwriting previous results
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd");
        String timestamp = LocalDateTime.now().format(formatter);
        
        // Create base experiment directory with timestamp
        // Purpose: Organize all outputs under timestamped root for traceability
        experimentDir = "experiments/aggregation_threshold_sweep_" + timestamp;
        resultsDir = experimentDir + "/results";
        snapshotsDir = experimentDir + "/snapshots";
        
        // Create directory structure
        // Purpose: Ensure all output directories exist before tests write to them
        Files.createDirectories(Paths.get(resultsDir + "/6_digit"));
        Files.createDirectories(Paths.get(resultsDir + "/7_digit"));
        Files.createDirectories(Paths.get(resultsDir + "/8_digit"));
        Files.createDirectories(Paths.get(snapshotsDir));
        
        // Print directory locations for user traceability
        // Purpose: User can navigate to output directory to inspect results during/after test execution
        System.out.println("=== Aggregation Threshold Sweep Experiment ===");
        System.out.println("Experiment directory: " + experimentDir);
        System.out.println("Results directory: " + resultsDir);
        System.out.println("Snapshots directory: " + snapshotsDir);
        System.out.println();
    }
    
    // ==================== VALIDATION TESTS ====================
    
    /**
     * Validate that all semiprime factorizations are correct.
     * 
     * <p><strong>PURPOSE:</strong> Ensure experimental integrity by verifying
     * that all target semiprimes are correctly factored before running sweep.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>For each semiprime definition [N, p, q]:</li>
     *   <li>  Verify p × q = N</li>
     *   <li>  Verify p and q are both prime (using isPrime helper)</li>
     *   <li>  Fail test if any verification fails</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want confidence that experimental targets
     * are mathematically valid, so that any convergence failures are due to
     * aggregation effects rather than invalid problem definitions.</p>
     */
    @Test
    @DisplayName("Validate all semiprime factorizations are correct")
    void shouldValidateAllSemiprimeFactorizations() {
        // Validate 6-digit semiprimes
        // Purpose: Ensure experimental targets are mathematically valid before running full sweep
        System.out.println("Validating 6-digit semiprimes...");
        for (int[] semiprime : SEMIPRIMES_6_DIGIT) {
            validateSemiprime(semiprime[0], semiprime[1], semiprime[2]);
            System.out.printf("  ✓ %d = %d × %d%n", semiprime[0], semiprime[1], semiprime[2]);
        }
        
        // Validate 7-digit semiprimes
        // Purpose: Catch any invalid problem definitions before wasting computation on invalid targets
        System.out.println("Validating 7-digit semiprimes...");
        for (int[] semiprime : SEMIPRIMES_7_DIGIT) {
            validateSemiprime(semiprime[0], semiprime[1], semiprime[2]);
            System.out.printf("  ✓ %d = %d × %d%n", semiprime[0], semiprime[1], semiprime[2]);
        }
        
        // Validate 8-digit semiprimes
        // Purpose: Final validation before committing to expensive 8-digit sweeps
        System.out.println("Validating 8-digit semiprimes...");
        for (int[] semiprime : SEMIPRIMES_8_DIGIT) {
            validateSemiprime(semiprime[0], semiprime[1], semiprime[2]);
            System.out.printf("  ✓ %d = %d × %d%n", semiprime[0], semiprime[1], semiprime[2]);
        }
        
        System.out.println("All semiprimes validated successfully!");
    }
    
    // ==================== MAIN SWEEP TESTS ====================
    
    /**
     * Run full aggregation sweep for 6-digit semiprimes.
     * 
     * <p><strong>PURPOSE:</strong> Test hypothesis on 6-digit scale:
     * <ul>
     *   <li>9 aggregation levels × 3 semiprimes × 30 reps = 810 total runs</li>
     *   <li>Expected runtime: ~15-30 minutes</li>
     *   <li>CSV output: ~810 files in results/6_digit/</li>
     * </ul>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>For each 6-digit semiprime:</li>
     *   <li>  For each aggregation level:</li>
     *   <li>    Run 30 repetitions with different seeds</li>
     *   <li>    Record per-step metrics to CSV</li>
     *   <li>    Save snapshots every 10 steps</li>
     *   <li>  Print progress after each aggregation level</li>
     *   <li>Print summary statistics: convergence rate per level</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want to identify the aggregation threshold
     * for 6-digit semiprimes, so that I can establish baseline scaling relationship
     * between problem size and critical aggregation.</p>
     */
    @Test
    @DisplayName("Run full aggregation sweep for 6-digit semiprimes")
    void shouldIdentifyCriticalThresholdFor6DigitSemiprimes() throws IOException {
        System.out.println("\n=== 6-Digit Semiprime Aggregation Sweep ===");
        String outputDir = resultsDir + "/6_digit";
        
        // For each semiprime in SEMIPRIMES_6_DIGIT
        // Purpose: Test hypothesis across multiple 6-digit targets
        for (int[] semiprime : SEMIPRIMES_6_DIGIT) {
            System.out.printf("\nProcessing semiprime %d (%d × %d)%n", 
                semiprime[0], semiprime[1], semiprime[2]);
            
            // For each aggregation level in AGGREGATION_LEVELS
            // Purpose: Sweep from 0% to 50% aggregation to find critical threshold
            for (double aggLevel : AGGREGATION_LEVELS) {
                // Run aggregation sweep (helper method)
                // Purpose: Execute 30 reps at this aggregation level
                int converged = runAggregationSweep(semiprime, aggLevel, outputDir);
                
                // Print progress
                // Purpose: User can monitor convergence rates during long sweep
                System.out.printf("  Aggregation %3.0f%%: %2d/30 converged (%.1f%%)%n",
                    aggLevel * 100, converged, (converged * 100.0 / REPS_PER_LEVEL));
            }
        }
        
        System.out.println("\n✓ 6-digit sweep complete");
    }
    
    /**
     * Run full aggregation sweep for 7-digit semiprimes.
     * 
     * <p><strong>PURPOSE:</strong> Test scaling hypothesis: Does critical threshold
     * shift leftward (lower aggregation tolerated) as problem size increases?
     * 
     * <p><strong>EXPECTED OUTCOME:</strong> Critical threshold at ~10-15% aggregation
     * (vs ~15-20% for 6-digit), supporting log-scale threshold shift model.
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Same as 6-digit sweep, but with 7-digit semiprimes</li>
     *   <li>810 total runs (9 levels × 3 semiprimes × 30 reps)</li>
     *   <li>CSV output: results/7_digit/</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want to test whether threshold shifts with
     * problem scale, so that I can predict optimal aggregation levels for arbitrary
     * semiprime sizes.</p>
     */
    @Test
    @DisplayName("Run full aggregation sweep for 7-digit semiprimes")
    void shouldIdentifyCriticalThresholdFor7DigitSemiprimes() throws IOException {
        System.out.println("\n=== 7-Digit Semiprime Aggregation Sweep ===");
        String outputDir = resultsDir + "/7_digit";
        
        for (int[] semiprime : SEMIPRIMES_7_DIGIT) {
            System.out.printf("\nProcessing semiprime %d (%d × %d)%n", 
                semiprime[0], semiprime[1], semiprime[2]);
            
            for (double aggLevel : AGGREGATION_LEVELS) {
                int converged = runAggregationSweep(semiprime, aggLevel, outputDir);
                System.out.printf("  Aggregation %3.0f%%: %2d/30 converged (%.1f%%)%n",
                    aggLevel * 100, converged, (converged * 100.0 / REPS_PER_LEVEL));
            }
        }
        
        System.out.println("\n✓ 7-digit sweep complete");
    }
    
    /**
     * Run full aggregation sweep for 8-digit semiprimes.
     * 
     * <p><strong>PURPOSE:</strong> Test extreme scaling: Does threshold continue
     * shifting leftward, or does it plateau at some minimum aggregation?
     * 
     * <p><strong>EXPECTED OUTCOME:</strong> Critical threshold at ~5-10% aggregation,
     * validating continued leftward shift with log(N) scaling.
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Same as previous sweeps, but with 8-digit semiprimes</li>
     *   <li>May require longer max steps (500+) for convergence</li>
     *   <li>CSV output: results/8_digit/</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want to validate the threshold-shift model
     * at extreme scale, so that I can confidently extrapolate to even larger problems.</p>
     */
    @Test
    @DisplayName("Run full aggregation sweep for 8-digit semiprimes")
    void shouldIdentifyCriticalThresholdFor8DigitSemiprimes() throws IOException {
        System.out.println("\n=== 8-Digit Semiprime Aggregation Sweep ===");
        String outputDir = resultsDir + "/8_digit";
        
        for (int[] semiprime : SEMIPRIMES_8_DIGIT) {
            System.out.printf("\nProcessing semiprime %d (%d × %d)%n", 
                semiprime[0], semiprime[1], semiprime[2]);
            
            for (double aggLevel : AGGREGATION_LEVELS) {
                int converged = runAggregationSweep(semiprime, aggLevel, outputDir);
                System.out.printf("  Aggregation %3.0f%%: %2d/30 converged (%.1f%%)%n",
                    aggLevel * 100, converged, (converged * 100.0 / REPS_PER_LEVEL));
            }
        }
        
        System.out.println("\n✓ 8-digit sweep complete");
    }
    
    // ==================== SINGLE-LEVEL VALIDATION TEST ====================
    
    /**
     * Run single aggregation level for quick validation.
     * 
     * <p><strong>PURPOSE:</strong> Enable rapid testing of experiment infrastructure
     * without running full 810-run sweep. Useful for CI/CD validation.
     * 
     * <p><strong>USAGE:</strong>
     * <pre>
     * mvn test -Dtest=AggregationThresholdSweepTest#shouldRunSingleAggregationLevel \
     *     -DaggregationLevel=0.15 \
     *     -Dsemiprime=1003001
     * </pre>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Read aggregationLevel and semiprime from system properties</li>
     *   <li>Run 30 repetitions at specified level</li>
     *   <li>Print convergence statistics</li>
     *   <li>Skip if properties not set (for batch test runs)</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want to test single aggregation levels quickly,
     * so that I can validate experiment setup before committing to full sweep.</p>
     */
    @Test
    @DisplayName("Run single aggregation level for validation")
    void shouldRunSingleAggregationLevel() throws IOException {
        // Read system properties: aggregationLevel, semiprime
        // Purpose: Enable quick testing of single configurations
        String aggLevelStr = System.getProperty("aggregationLevel");
        String semiprimeStr = System.getProperty("semiprime");
        
        // Skip test if properties not set
        // Purpose: Don't fail when running full test suite
        if (aggLevelStr == null || semiprimeStr == null) {
            System.out.println("Skipping single-level test (no system properties set)");
            System.out.println("Usage: -DaggregationLevel=0.15 -Dsemiprime=184507");
            return;
        }
        
        double aggregationLevel = Double.parseDouble(aggLevelStr);
        int targetSemiprime = Integer.parseInt(semiprimeStr);
        
        // Find semiprime definition in constants
        // Purpose: Look up full factorization for the target
        int[] semiprime = null;
        for (int[] sp : SEMIPRIMES_6_DIGIT) {
            if (sp[0] == targetSemiprime) {
                semiprime = sp;
                break;
            }
        }
        if (semiprime == null) {
            for (int[] sp : SEMIPRIMES_7_DIGIT) {
                if (sp[0] == targetSemiprime) {
                    semiprime = sp;
                    break;
                }
            }
        }
        if (semiprime == null) {
            for (int[] sp : SEMIPRIMES_8_DIGIT) {
                if (sp[0] == targetSemiprime) {
                    semiprime = sp;
                    break;
                }
            }
        }
        
        assertNotNull(semiprime, "Semiprime " + targetSemiprime + " not found in definitions");
        
        // Run 30 reps at specified aggregation level
        // Purpose: Quick validation of experimental infrastructure
        System.out.printf("Running single level: aggregation=%.2f, semiprime=%d%n", 
            aggregationLevel, targetSemiprime);
        String outputDir = resultsDir + "/single_level";
        Files.createDirectories(Paths.get(outputDir));
        
        int converged = runAggregationSweep(semiprime, aggregationLevel, outputDir);
        
        // Print convergence rate
        // Purpose: Quick feedback on convergence success
        System.out.printf("Convergence rate: %d/30 (%.1f%%)%n", 
            converged, (converged * 100.0 / REPS_PER_LEVEL));
    }
    
    // ==================== CROSS-DIGIT-CLASS COMPARISON ====================
    
    /**
     * Generate comparative analysis across all digit classes.
     * 
     * <p><strong>PURPOSE:</strong> After running all three sweeps, generate summary
     * showing how threshold shifts across 6/7/8-digit semiprimes.
     * 
     * <p><strong>OUTPUT:</strong> THRESHOLD_ANALYSIS.md with:
     * <ul>
     *   <li>Convergence rate table (aggregation × digit class)</li>
     *   <li>Estimated critical threshold per digit class</li>
     *   <li>Log-scale threshold model parameters</li>
     *   <li>Recommendations for Hypothesis 2 (adaptive disaggregation)</li>
     * </ul>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Read all CSV files from results/ subdirectories</li>
     *   <li>Compute convergence rates per (digit class, aggregation level)</li>
     *   <li>Fit sigmoid curves to identify inflection points</li>
     *   <li>Model threshold vs log(semiprime) relationship</li>
     *   <li>Write THRESHOLD_ANALYSIS.md</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want a comprehensive analysis showing threshold
     * shift across scales, so that I can design optimal adaptive disaggregation
     * strategies for Hypothesis 2.</p>
     */
    @Test
    @DisplayName("Generate threshold comparison across digit classes")
    void shouldCompareThresholdAcrossDigitClasses() throws IOException {
        System.out.println("\n=== Cross-Digit-Class Threshold Analysis ===");
        System.out.println("NOTE: This test requires results from full sweep tests to have been run first.");
        System.out.println("Skipping analysis generation for now - implement in future iteration.");
        System.out.println("Expected output: THRESHOLD_ANALYSIS.md with comparative statistics");
        
        // TODO: Read CSV results from all digit classes
        // TODO: Compute convergence rates per (aggregation, digit class)
        // TODO: Identify critical thresholds (50% convergence point)
        // TODO: Model threshold vs log(N) relationship
        // TODO: Write THRESHOLD_ANALYSIS.md with findings and plots
        
        // For now, just report that this would be implemented
        // Purpose: Document the analysis step without blocking current implementation
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Compute all metrics for current array state.
     * 
     * <p><strong>PURPOSE:</strong> Measure all relevant metrics in a single pass
     * through the cell array, following Phase 3 v2 metrics design.</p>
     * 
     * @param step the step number
     * @param cells the cell array
     * @param factorA the first factor (for position tracking)
     * @param factorB the second factor (for position tracking)
     * @param swaps the number of swaps in this step
     * @param consecutiveZeroSwaps consecutive steps with zero swaps
     * @param isStagnant whether run is stagnant
     * @return StepMetrics object with all measurements
     */
    private StepMetrics computeMetrics(int step, List<FactorCell> cells, int factorA, int factorB,
                                      int swaps, int consecutiveZeroSwaps, boolean isStagnant) {
        // Compute strategy aggregation (v1 metric for comparison)
        // Purpose: Verify that aggregation levels are maintained during execution
        double strategyAgg = computeStrategyAggregation(cells);
        
        // Compute fitness clustering (v2 metric)
        // Purpose: Measure fitness-field spatial clustering
        double fitnessClustering = computeFitnessClustering(cells);
        
        // Find factor positions and compute localization
        // Purpose: Track factor movement toward front of array
        int[] factorPositions = findFactorPositions(cells, factorA, factorB);
        double factorLocalization = computeFactorLocalization(factorPositions, cells.size());
        
        // Compute other metrics matching Phase 3 design
        // Purpose: Provide complete metrics for time-series analysis
        double meanFactorDist = computeMeanFactorDistance(factorPositions);
        double[] fitnessGradient = computeFitnessGradient(cells);
        double entropyGlobal = computeStrategyEntropy(cells, 0, cells.size());
        double entropyFront = computeStrategyEntropy(cells, 0, Math.min(10, cells.size()));
        
        // Return StepMetrics object with all measurements
        // Purpose: Package metrics for CSV export
        return new StepMetrics(
            step,
            strategyAgg,
            fitnessClustering,
            factorLocalization,
            factorPositions,
            meanFactorDist,
            fitnessGradient[0], // mean
            fitnessGradient[1], // std
            entropyGlobal,
            entropyFront,
            swaps,
            consecutiveZeroSwaps,
            isStagnant
        );
    }
    
    /**
     * Compute fitness clustering value (v2 metric from Phase 3).
     * 
     * <p><strong>PURPOSE:</strong> Measure FITNESS-SIMILARITY adjacency,
     * independent of strategy labels.</p>
     * 
     * @param cells the cell array
     * @return fitness clustering percentage [0.0, 100.0]
     */
    private double computeFitnessClustering(List<FactorCell> cells) {
        if (cells.size() <= 1) {
            return 100.0;
        }
        
        final double FITNESS_THRESHOLD = 0.1;
        int similarFitnessNeighborCount = 0;
        
        for (int i = 0; i < cells.size(); i++) {
            double currentFitness = cells.get(i).getFitness();
            
            boolean hasLeftSimilar = (i > 0) && 
                (Math.abs(cells.get(i - 1).getFitness() - currentFitness) < FITNESS_THRESHOLD);
            boolean hasRightSimilar = (i < cells.size() - 1) && 
                (Math.abs(cells.get(i + 1).getFitness() - currentFitness) < FITNESS_THRESHOLD);
            
            if (hasLeftSimilar || hasRightSimilar) {
                similarFitnessNeighborCount++;
            }
        }
        
        return (similarFitnessNeighborCount * 100.0) / cells.size();
    }
    
    /**
     * Find positions of true factors in cell array.
     * 
     * @param cells the cell array
     * @param factorA the first factor
     * @param factorB the second factor
     * @return array of [pos_A, pos_B] (or -1 if not found)
     */
    private int[] findFactorPositions(List<FactorCell> cells, int factorA, int factorB) {
        int posA = -1;
        int posB = -1;
        
        for (int i = 0; i < cells.size(); i++) {
            int candidate = cells.get(i).readValue();
            if (candidate == factorA) {
                posA = i;
            } else if (candidate == factorB) {
                posB = i;
            }
        }
        
        return new int[]{posA, posB};
    }
    
    /**
     * Compute factor localization index.
     * 
     * @param factorPositions array of [pos_factor1, pos_factor2]
     * @param arraySize size of array (for normalization)
     * @return localization index [0.0, 1.0]
     */
    private double computeFactorLocalization(int[] factorPositions, int arraySize) {
        int pos1 = factorPositions[0];
        int pos2 = factorPositions[1];
        
        if (pos1 < 0 || pos2 < 0) {
            return 0.0;
        }
        
        int distance = Math.abs(pos1 - pos2);
        int maxDistance = arraySize - 1;
        return 1.0 - ((double) distance / maxDistance);
    }
    
    /**
     * Compute mean distance of factors from array front.
     * 
     * @param factorPositions array of [pos_A, pos_B]
     * @return mean distance from front, or -1.0 if no factors present
     */
    private double computeMeanFactorDistance(int[] factorPositions) {
        int posA = factorPositions[0];
        int posB = factorPositions[1];
        
        if (posA >= 0 && posB >= 0) {
            return (posA + posB) / 2.0;
        } else if (posA >= 0) {
            return (double) posA;
        } else if (posB >= 0) {
            return (double) posB;
        } else {
            return -1.0;
        }
    }
    
    /**
     * Compute fitness gradient statistics.
     * 
     * @param cells the cell array
     * @return array of [mean, std]
     */
    private double[] computeFitnessGradient(List<FactorCell> cells) {
        if (cells.size() < 2) {
            return new double[]{0.0, 0.0};
        }
        
        List<Double> gradients = new ArrayList<>();
        for (int i = 0; i < cells.size() - 1; i++) {
            double diff = Math.abs(cells.get(i).getFitness() - cells.get(i + 1).getFitness());
            gradients.add(diff);
        }
        
        double mean = gradients.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double variance = gradients.stream()
            .mapToDouble(g -> Math.pow(g - mean, 2))
            .average()
            .orElse(0.0);
        double std = Math.sqrt(variance);
        
        return new double[]{mean, std};
    }
    
    /**
     * Compute Shannon entropy of strategy distribution.
     * 
     * @param cells the cell array
     * @param startIdx start index (inclusive)
     * @param endIdx end index (exclusive)
     * @return Shannon entropy in bits
     */
    private double computeStrategyEntropy(List<FactorCell> cells, int startIdx, int endIdx) {
        if (startIdx >= endIdx) {
            return 0.0;
        }
        
        Map<FactorStrategy, Integer> counts = new HashMap<>();
        for (int i = startIdx; i < endIdx; i++) {
            FactorStrategy strategy = cells.get(i).readAlgotype();
            counts.put(strategy, counts.getOrDefault(strategy, 0) + 1);
        }
        
        int total = endIdx - startIdx;
        double entropy = 0.0;
        
        for (int count : counts.values()) {
            if (count > 0) {
                double p = (double) count / total;
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        
        return entropy;
    }
    
    /**
     * Check if experiment has converged.
     * 
     * @param metrics the current step metrics
     * @return true if converged (both factors in [0, CONVERGENCE_POSITION])
     */
    private boolean isConverged(StepMetrics metrics) {
        int posA = metrics.factorPositions[0];
        int posB = metrics.factorPositions[1];
        
        return (posA >= 0 && posA <= CONVERGENCE_POSITION && 
                posB >= 0 && posB <= CONVERGENCE_POSITION);
    }
    
    /**
     * Cast List<FactorCell> to List<AbstractCell> for generic engine.
     * 
     * @param cells the FactorCell list
     * @return same list cast to AbstractCell generic type
     */
    @SuppressWarnings("unchecked")
    private List<AbstractCell<Integer, FactorStrategy>> castToAbstractCells(List<FactorCell> cells) {
        return (List<AbstractCell<Integer, FactorStrategy>>) (List<?>) cells;
    }
    
    // ==================== EXPERIMENT CORE METHODS ====================
    
    /**
     * Run aggregation sweep for single semiprime at single aggregation level.
     * 
     * <p><strong>PURPOSE:</strong> Core experiment logic - run REPS_PER_LEVEL
     * repetitions and write CSV results.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>For each repetition (seed = rep number):</li>
     *   <li>  Generate cell array with specified aggregation level</li>
     *   <li>  Execute experiment run (max MAX_STEPS)</li>
     *   <li>  Collect per-step metrics</li>
     *   <li>  Write CSV: agg_XX_semiprime_NNNN_rep_RRR.csv</li>
     *   <li>Return convergence statistics for this level</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want each aggregation level tested with
     * sufficient repetitions, so that convergence rates are statistically reliable.</p>
     * 
     * @param semiprime the target semiprime [N, p, q]
     * @param aggregationLevel the target aggregation level [0.0, 1.0]
     * @param outputDir the directory for CSV results
     * @return convergence count (number of converged runs out of REPS_PER_LEVEL)
     */
    private int runAggregationSweep(int[] semiprime, double aggregationLevel, String outputDir) throws IOException {
        int N = semiprime[0];
        int factorA = semiprime[1];
        int factorB = semiprime[2];
        
        int convergedCount = 0;
        
        // For each rep in [1, REPS_PER_LEVEL]
        // Purpose: Run sufficient repetitions for statistical power
        for (int rep = 1; rep <= REPS_PER_LEVEL; rep++) {
            long seed = rep; // Use rep number as seed for reproducibility
            
            // Generate cell array with target aggregation
            // Purpose: Create controlled aggregation level for this run
            List<FactorCell> cells = generateCellsWithAggregation(N, factorA, factorB, aggregationLevel, seed);
            
            // Execute experiment run
            // Purpose: Let cells sort and track convergence dynamics
            List<StepMetrics> metrics = executeExperimentRun(cells, factorA, factorB);
            
            // Check if converged
            // Purpose: Count successful localizations for convergence rate calculation
            StepMetrics lastStep = metrics.get(metrics.size() - 1);
            if (isConverged(lastStep)) {
                convergedCount++;
            }
            
            // Write CSV results
            // Purpose: Export per-step metrics for detailed analysis
            String filename = String.format("%s/agg_%02d_semiprime_%d_rep_%03d.csv",
                outputDir,
                (int)(aggregationLevel * 100),
                N,
                rep);
            writeCsvResults(filename, metrics);
        }
        
        // Return convergence count
        // Purpose: Report success rate for this aggregation level
        return convergedCount;
    }
    
    /**
     * Generate cell array with specified aggregation level.
     * 
     * <p><strong>PURPOSE:</strong> Create cell arrays with controlled aggregation
     * by blending CLUSTERED and MAXIMAL_MIXING spatial arrangements.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Generate standard candidate pool (33% SMALL_PRIMES, 34% FERMAT, 33% RANDOM)</li>
     *   <li>Ensure both factors present (inject if missing)</li>
     *   <li>If aggregationLevel = 0.0: Use MAXIMAL_MIXING arrangement</li>
     *   <li>If aggregationLevel = 1.0: Use CLUSTERED arrangement</li>
     *   <li>If 0.0 < aggregationLevel < 1.0:</li>
     *   <li>  Generate both MAXIMAL_MIXING and CLUSTERED versions</li>
     *   <li>  Blend: For each position, choose CLUSTERED with probability = aggregationLevel</li>
     *   <li>Return blended cell array</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want fine-grained control over aggregation
     * levels, so that I can precisely locate the critical threshold.</p>
     * 
     * @param target the semiprime N
     * @param factorA the first factor p
     * @param factorB the second factor q
     * @param aggregationLevel the target aggregation [0.0, 1.0]
     * @param seed the random seed for reproducibility
     * @return cell array with specified aggregation level
     */
    private List<FactorCell> generateCellsWithAggregation(
            int target, int factorA, int factorB, double aggregationLevel, long seed) {
        // Create random source for reproducibility
        // Purpose: Enable deterministic experiments with fixed seeds
        Random rand = new Random(seed);
        
        // Generate standard candidate pool (33% SMALL_PRIMES, 34% FERMAT, 33% RANDOM)
        // Purpose: Use same distribution as Phase 3 for consistency
        List<FactorCell> cells = generateStandardPool(target, rand);
        
        // Ensure both factors present (inject if missing)
        // Purpose: Guarantee that convergence is possible (both factors in array)
        ensureFactorsPresent(cells, factorA, factorB, seed);
        
        // Apply spatial arrangement based on aggregation level
        // Purpose: Create precise aggregation levels by blending MAXIMAL_MIXING and CLUSTERED
        if (aggregationLevel == 0.0) {
            // Use MAXIMAL_MIXING arrangement (0% aggregation)
            // Purpose: Minimally-aggregated baseline
            return SpatialArranger.arrange(cells, SpatialArranger.LayoutMode.MAXIMAL_MIXING, rand);
        } else if (aggregationLevel >= 1.0) {
            // Use CLUSTERED arrangement (100% aggregation)
            // Purpose: Maximally-aggregated configuration
            return SpatialArranger.arrange(cells, SpatialArranger.LayoutMode.CLUSTERED, rand);
        } else {
            // Blend MAXIMAL_MIXING and CLUSTERED arrangements
            // Purpose: Create intermediate aggregation levels
            
            // Generate both extreme arrangements
            List<FactorCell> mixed = SpatialArranger.arrange(new ArrayList<>(cells), 
                SpatialArranger.LayoutMode.MAXIMAL_MIXING, new Random(seed));
            List<FactorCell> clustered = SpatialArranger.arrange(new ArrayList<>(cells), 
                SpatialArranger.LayoutMode.CLUSTERED, new Random(seed + 1));
            
            // Blend: For each position, choose CLUSTERED with probability = aggregationLevel
            // Purpose: Achieve target aggregation level through probabilistic blending
            List<FactorCell> blended = new ArrayList<>();
            for (int i = 0; i < cells.size(); i++) {
                if (rand.nextDouble() < aggregationLevel) {
                    // Use clustered cell at this position
                    blended.add(clustered.get(i));
                } else {
                    // Use mixed cell at this position
                    blended.add(mixed.get(i));
                }
            }
            
            // Update positions to match array indices
            // Purpose: Ensure cell.readCurrentPosition() matches array index
            for (int i = 0; i < blended.size(); i++) {
                blended.get(i).updatePositionTo(i);
            }
            
            return blended;
        }
    }
    
    /**
     * Generate standard pool of candidates (33% SmallPrimes, 34% Fermat, 33% Random).
     * 
     * <p><strong>PURPOSE:</strong> Create same candidate distribution as Phase 3 experiments
     * for consistency and comparability.</p>
     * 
     * @param target the semiprime to factor
     * @param rand random source
     * @return unordered list of cells
     */
    private List<FactorCell> generateStandardPool(int target, Random rand) {
        List<FactorCell> cells = new ArrayList<>();
        int sqrtN = (int) Math.sqrt(target);
        
        // Distribution: 33%, 34%, 33% of ARRAY_SIZE
        // Purpose: Balanced mix of three strategies for chimeric population
        int countSmall = (int) (ARRAY_SIZE * 0.33);
        int countRandom = (int) (ARRAY_SIZE * 0.33);
        int countFermat = ARRAY_SIZE - countSmall - countRandom;
        
        int[] counts = {countSmall, countFermat, countRandom};
        FactorStrategy[] strategies = {
            FactorStrategy.SMALL_PRIMES,
            FactorStrategy.FERMAT_NEAR_SQRT,
            FactorStrategy.RANDOM_SAMPLE
        };
        
        int position = 0;
        for (int stratIdx = 0; stratIdx < 3; stratIdx++) {
            FactorStrategy strategy = strategies[stratIdx];
            int count = counts[stratIdx];
            
            for (int i = 0; i < count; i++) {
                int candidate;
                if (strategy == FactorStrategy.SMALL_PRIMES) {
                    List<Integer> primes = CandidateGenerator.generateSmallPrimes(target, sqrtN, rand);
                    if (primes.isEmpty()) primes.add(2);
                    candidate = primes.get(rand.nextInt(primes.size()));
                } else if (strategy == FactorStrategy.FERMAT_NEAR_SQRT) {
                    candidate = Math.max(2, sqrtN - 5 + rand.nextInt(11));
                    candidate = Math.min(candidate, sqrtN);
                } else {
                    candidate = 2 + rand.nextInt(sqrtN - 1);
                }
                
                cells.add(new FactorCell(candidate, target, strategy, position++));
            }
        }
        return cells;
    }
    
    /**
     * Ensure both true factors are present in cell array.
     * 
     * <p><strong>PURPOSE:</strong> Guarantee convergence is possible by injecting
     * missing factors into candidate pool.</p>
     * 
     * @param cells the cell array (modified in-place if factors missing)
     * @param factorA the first factor
     * @param factorB the second factor
     * @param seed the random seed for deterministic positioning
     */
    private void ensureFactorsPresent(List<FactorCell> cells, int factorA, int factorB, long seed) {
        boolean hasFactorA = false;
        boolean hasFactorB = false;
        int target = cells.get(0).readValue(); // Get target from first cell
        
        // Find target from any cell (they all have the same target)
        for (FactorCell cell : cells) {
            int value = cell.readValue();
            if (value == factorA) hasFactorA = true;
            if (value == factorB) hasFactorB = true;
        }
        
        if (hasFactorA && hasFactorB) {
            return; // Both present, no injection needed
        }
        
        // Get target value from examining cells (all cells have same target)
        // Purpose: We need target to construct new FactorCell objects
        int actualTarget = -1;
        for (FactorCell cell : cells) {
            // Access the target via a calculation: candidate * multiplier should approximately equal target
            // Actually, we need to extract target another way. Let's pass it as parameter instead.
            break;
        }
        
        // Inject missing factors deterministically
        Random rand = new Random(seed);
        int range = ARRAY_SIZE / 3;
        if (range < 1) range = 1;
        
        if (!hasFactorA) {
            int pos = rand.nextInt(range);
            FactorCell oldCell = cells.get(pos);
            // We can't easily get target from FactorCell, so we'll need to pass it
            // For now, calculate it from factorA and factorB
            int inferredTarget = factorA * factorB;
            cells.set(pos, new FactorCell(factorA, inferredTarget, FactorStrategy.SMALL_PRIMES, pos));
        }
        
        if (!hasFactorB) {
            int pos = range + rand.nextInt(range);
            int inferredTarget = factorA * factorB;
            cells.set(pos, new FactorCell(factorB, inferredTarget, FactorStrategy.SMALL_PRIMES, pos));
        }
        
        // Update positions
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).updatePositionTo(i);
        }
    }
    
    /**
     * Execute single experiment run and collect metrics.
     * 
     * <p><strong>PURPOSE:</strong> Run execution steps until convergence, stagnation,
     * or max steps, recording v2 metrics at each step.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Record initial state (step 0)</li>
     *   <li>While not converged/stagnated and steps < MAX_STEPS:</li>
     *   <li>  Execute step with GenericExecutionEngine</li>
     *   <li>  Compute and record metrics (StepMetrics)</li>
     *   <li>  Check convergence (both factors in [0, CONVERGENCE_POSITION])</li>
     *   <li>  Check stagnation (consecutive zero swaps >= STAGNATION_THRESHOLD)</li>
     *   <li>Return metrics history</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want complete per-step metrics for each run,
     * so that I can analyze convergence dynamics beyond binary success/failure.</p>
     * 
     * @param cells the cell array to execute
     * @param factorA the first factor (for position tracking)
     * @param factorB the second factor (for position tracking)
     * @return list of StepMetrics (one per step including initial state)
     */
    private List<StepMetrics> executeExperimentRun(List<FactorCell> cells, int factorA, int factorB) {
        // Initialize metrics history and execution engine
        // Purpose: Track all per-step metrics for convergence analysis
        List<StepMetrics> metricsHistory = new ArrayList<>();
        GenericExecutionEngine<Integer, FactorStrategy> engine = new GenericExecutionEngine<>();
        
        // Record initial state (step 0)
        // Purpose: Capture starting configuration before any swaps occur
        StepMetrics initialMetrics = computeMetrics(0, cells, factorA, factorB, 0, 0, false);
        metricsHistory.add(initialMetrics);
        
        // Stagnation tracking
        // Purpose: Detect when run is stuck in local attractor vs still making progress
        int consecutiveZeroSwaps = 0;
        
        // Execute steps until convergence, stagnation, or max steps
        // Purpose: Run experiment with standard termination criteria
        int step = 0;
        while (step < MAX_STEPS) {
            // Execute one step (swaps reflect this step's activity)
            // Purpose: Let cells swap based on fitness comparison
            int swaps = engine.executeStep(castToAbstractCells(cells));
            step++; // Increment step counter AFTER execution
            
            // Track consecutive zero-swap steps
            // Purpose: Detect stagnation (stuck in local attractor)
            if (swaps == 0) {
                consecutiveZeroSwaps++;
            } else {
                consecutiveZeroSwaps = 0; // Reset on any swap activity
            }
            
            // Flag stagnation when threshold reached
            // Purpose: Mark runs that are stuck vs still converging
            boolean isStagnant = consecutiveZeroSwaps >= STAGNATION_THRESHOLD;
            
            // Compute and record metrics for this completed step
            // Purpose: Capture full state for time-series analysis
            StepMetrics stepMetrics = computeMetrics(step, cells, factorA, factorB, swaps, consecutiveZeroSwaps, isStagnant);
            metricsHistory.add(stepMetrics);
            
            // Check convergence (both factors in [0, CONVERGENCE_POSITION])
            // Purpose: Stop early if localization succeeds
            if (isConverged(stepMetrics)) {
                break;
            }
            
            // Check stagnation (stop if stuck)
            // Purpose: Don't waste computation on runs that won't converge
            if (isStagnant) {
                break;
            }
        }
        
        // Return metrics history
        // Purpose: Provide complete time-series for CSV export and analysis
        return metricsHistory;
    }
    
    /**
     * Write experiment metrics to CSV file.
     * 
     * <p><strong>PURPOSE:</strong> Export per-step metrics in v2 CSV format for
     * downstream analysis.</p>
     * 
     * <p><strong>FORMAT:</strong> Header row + one row per step, matching
     * Phase 3 v2 schema.</p>
     * 
     * <p><strong>AS A USER:</strong> I want CSV output compatible with Phase 3 analysis
     * tools, so that I can reuse existing visualization and statistical scripts.</p>
     * 
     * @param filename the CSV output filename
     * @param metrics the metrics history
     * @throws IOException if write fails
     */
    private void writeCsvResults(String filename, List<StepMetrics> metrics) throws IOException {
        // Open file writer
        // Purpose: Create CSV file for per-step metrics export
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header (StepMetrics.getCsvHeader())
            // Purpose: Provide column names matching v2 schema from Phase 3
            writer.println(StepMetrics.getCsvHeader());
            
            // Write data rows (metrics.toCsvRow())
            // Purpose: Export all per-step metrics for downstream analysis
            for (StepMetrics m : metrics) {
                writer.println(m.toCsvRow());
            }
            
            // File automatically closed by try-with-resources
            // Purpose: Ensure file handle is released even if exception occurs
        }
    }
    
    /**
     * Validate that a semiprime factorization is correct.
     * 
     * <p><strong>PURPOSE:</strong> Ensure N = p × q and both p, q are prime.</p>
     * 
     * <p><strong>PROCESS:</strong>
     * <ol>
     *   <li>Verify p × q = N</li>
     *   <li>Verify isPrime(p)</li>
     *   <li>Verify isPrime(q)</li>
     *   <li>Throw assertion failure if any check fails</li>
     * </ol>
     * 
     * <p><strong>AS A USER:</strong> I want automated validation of semiprime
     * definitions, so that invalid factorizations are caught before experiments run.</p>
     * 
     * @param N the semiprime
     * @param p the first factor
     * @param q the second factor
     */
    private void validateSemiprime(int N, int p, int q) {
        // Verify p × q = N
        // Purpose: Ensure factorization is mathematically correct
        assertEquals(p * q, N, 
            String.format("Invalid factorization: %d × %d = %d, expected %d", p, q, p * q, N));
        
        // Verify isPrime(p)
        // Purpose: Ensure p is prime (semiprime = product of two primes)
        assertTrue(isPrime(p), 
            String.format("Factor p=%d is not prime", p));
        
        // Verify isPrime(q)
        // Purpose: Ensure q is prime (semiprime = product of two primes)
        assertTrue(isPrime(q), 
            String.format("Factor q=%d is not prime", q));
    }
    
    /**
     * Check if a number is prime.
     * 
     * <p><strong>PURPOSE:</strong> Primality test for semiprime validation.</p>
     * 
     * <p><strong>ALGORITHM:</strong> Trial division up to sqrt(n).</p>
     * 
     * <p><strong>AS A USER:</strong> I want reliable primality testing for
     * experimental validation, so that I can trust semiprime definitions.</p>
     * 
     * @param n the number to test
     * @return true if n is prime
     */
    private boolean isPrime(int n) {
        // Handle edge cases (n <= 1, n == 2)
        // Purpose: Correct handling of non-prime cases and smallest prime
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        
        // Trial division up to sqrt(n)
        // Purpose: Efficient primality test for semiprime validation
        // Algorithm: Check divisibility by all odd numbers up to sqrt(n)
        int sqrtN = (int) Math.sqrt(n);
        for (int i = 3; i <= sqrtN; i += 2) {
            if (n % i == 0) {
                return false; // Found divisor, not prime
            }
        }
        
        // Return true if no divisors found
        // Purpose: n is prime if no divisors exist in [2, sqrt(n)]
        return true;
    }
    
    /**
     * Compute strategy aggregation value from cell array.
     * 
     * <p><strong>PURPOSE:</strong> Verify that generated arrays achieve target
     * aggregation levels.</p>
     * 
     * <p><strong>FORMULA:</strong> (cells with same-strategy neighbor / total cells) × 100</p>
     * 
     * <p><strong>AS A USER:</strong> I want to measure actual aggregation levels
     * achieved, so that I can validate the cell generation algorithm.</p>
     * 
     * @param cells the cell array
     * @return aggregation percentage [0.0, 100.0]
     */
    private double computeStrategyAggregation(List<FactorCell> cells) {
        // Handle edge case: single cell array has 100% aggregation by definition
        // Purpose: Avoid division by zero and provide sensible default
        if (cells.size() <= 1) {
            return 100.0;
        }
        
        // Count cells with same-strategy neighbors
        // Purpose: Measure spatial clustering of strategy labels
        int sameStrategyNeighborCount = 0;
        for (int i = 0; i < cells.size(); i++) {
            FactorStrategy currentStrategy = cells.get(i).readAlgotype();
            
            // Check left neighbor
            // Purpose: Test if cell has same-strategy neighbor on left
            boolean hasLeftSame = (i > 0) && 
                (cells.get(i - 1).readAlgotype() == currentStrategy);
            
            // Check right neighbor
            // Purpose: Test if cell has same-strategy neighbor on right
            boolean hasRightSame = (i < cells.size() - 1) && 
                (cells.get(i + 1).readAlgotype() == currentStrategy);
            
            // Count if cell has at least one same-strategy neighbor
            // Purpose: Cell contributes to aggregation if it's part of a cluster
            if (hasLeftSame || hasRightSame) {
                sameStrategyNeighborCount++;
            }
        }
        
        // Normalize to percentage
        // Purpose: Return aggregation as percentage [0.0, 100.0] for consistency with Phase 3
        return (sameStrategyNeighborCount * 100.0) / cells.size();
    }
}
