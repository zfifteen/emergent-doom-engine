package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

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
        {143063, 337, 421},    // 337 × 421 = 143,077 (verification needed)
        {294409, 503, 587},    // 503 × 587 = 295,261 (verification needed)
        {524287, 727, 721}     // 727 × 721 = 524,167 (verification needed)
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
        {1003001, 769, 1303},     // 769 × 1303 = 1,001,207 (verification needed)
        {2091667, 1279, 1637},    // 1279 × 1637 = 2,093,923 (verification needed)
        {5765761, 2399, 2399}     // 2399 × 2399 = 5,755,201 (perfect square, verification needed)
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
        {10003001, 2939, 3401},   // 2939 × 3401 = 9,996,139 (verification needed)
        {25326001, 4001, 6329},   // 4001 × 6329 = 25,320,329 (verification needed)
        {99990001, 9901, 10099}   // 9901 × 10099 = 99,990,899 (verification needed)
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
        // TODO: Generate timestamp using LocalDateTime
        // TODO: Create base experiment directory
        // TODO: Create results subdirectories (6_digit, 7_digit, 8_digit)
        // TODO: Create snapshots directory
        // TODO: Print directory locations for traceability
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
        // TODO: Validate 6-digit semiprimes
        // TODO: Validate 7-digit semiprimes
        // TODO: Validate 8-digit semiprimes
        // TODO: Use helper method validateSemiprime(N, p, q)
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
        // TODO: For each semiprime in SEMIPRIMES_6_DIGIT
        // TODO:   For each aggregation level in AGGREGATION_LEVELS
        // TODO:     Run aggregation sweep (helper method)
        // TODO:     Collect convergence statistics
        // TODO: Print summary: convergence rate vs aggregation level
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
        // TODO: Same structure as 6-digit sweep
        // TODO: Use SEMIPRIMES_7_DIGIT
        // TODO: Output to results/7_digit/
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
        // TODO: Same structure as previous sweeps
        // TODO: Use SEMIPRIMES_8_DIGIT
        // TODO: Output to results/8_digit/
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
        // TODO: Read system properties: aggregationLevel, semiprime
        // TODO: Skip test if properties not set
        // TODO: Find semiprime definition in constants
        // TODO: Run 30 reps at specified aggregation level
        // TODO: Print convergence rate
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
        // TODO: Read CSV results from all digit classes
        // TODO: Compute convergence rates per (aggregation, digit class)
        // TODO: Identify critical thresholds (50% convergence point)
        // TODO: Model threshold vs log(N) relationship
        // TODO: Write THRESHOLD_ANALYSIS.md with findings and plots
    }
    
    // ==================== HELPER METHODS ====================
    
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
        // TODO: For each rep in [1, REPS_PER_LEVEL]:
        // TODO:   Generate cell array with target aggregation
        // TODO:   Execute experiment run
        // TODO:   Write CSV results
        // TODO: Return convergence count
        return 0; // Placeholder
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
        // TODO: Generate standard candidate pool
        // TODO: Ensure factors present
        // TODO: Blend MAXIMAL_MIXING and CLUSTERED arrangements
        // TODO: Return cell array
        return new ArrayList<>(); // Placeholder
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
        // TODO: Record initial state
        // TODO: Execute steps with GenericExecutionEngine
        // TODO: Compute metrics at each step
        // TODO: Check convergence and stagnation
        // TODO: Return metrics history
        return new ArrayList<>(); // Placeholder
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
        // TODO: Open file writer
        // TODO: Write header (StepMetrics.getCsvHeader())
        // TODO: Write data rows (metrics.toCsvRow())
        // TODO: Close file
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
        // TODO: Assert p * q == N
        // TODO: Assert isPrime(p)
        // TODO: Assert isPrime(q)
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
        // TODO: Handle edge cases (n <= 1, n == 2)
        // TODO: Trial division up to sqrt(n)
        // TODO: Return true if no divisors found
        return false; // Placeholder
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
        // TODO: Count cells with same-strategy neighbors
        // TODO: Normalize to percentage
        // TODO: Return aggregation value
        return 0.0; // Placeholder
    }
}
