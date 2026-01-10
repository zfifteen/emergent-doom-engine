package com.emergent.doom.factorization;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.execution.GenericExecutionEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Scientific experiment testing whether factor LOCALIZATION is caused by fitness-field clustering or fitness-driven sorting.
 *
 * <p><strong>CENTRAL HYPOTHESIS:</strong> Factor localization in the Emergent Doom Engine
 * factorization domain is driven by FITNESS-BASED SORTING, not by pre-existing fitness-field clustering.
 * This experiment creates five experimental conditions with varying spatial structure
 * to isolate the causal mechanism.</p>
 *
 * <p><strong>SEMANTIC ALIGNMENT (v2):</strong> This version uses Levin-consistent terminology:</p>
 * <ul>
 *   <li><strong>Localization:</strong> Concentration of high-fitness configurations in morphospace (measured by inter-factor proximity)</li>
 *   <li><strong>Fitness Clustering:</strong> Spatial aggregation of similar FITNESS values (not strategy labels)</li>
 *   <li><strong>Strategy Aggregation:</strong> Spatial grouping of same-STRATEGY cells (v1 metric, for comparison)</li>
 * </ul>
 *
 * <p><strong>EXPERIMENTAL CONDITIONS:</strong></p>
 * <table border="1">
 * <tr><th>Condition</th><th>Strategy Aggregation</th><th>Purpose</th></tr>
 * <tr><td>C1: Baseline</td><td>~50-60%</td><td>Natural chimeric distribution (reference)</td></tr>
 * <tr><td>C2: High Strategy Aggregation</td><td>~75%</td><td>Pre-clustered by strategy (test clustering hypothesis)</td></tr>
 * <tr><td>C3: Zero Strategy Aggregation</td><td>~0-10%</td><td>Maximally mixed strategies (test if strategy grouping is necessary)</td></tr>
 * <tr><td>C4: Fitness Control</td><td>~50-60%</td><td>No true factors (test if fitness is necessary)</td></tr>
 * <tr><td>C5: Homogeneous</td><td>100%</td><td>Single strategy (test if strategy diversity is necessary)</td></tr>
 * </table>
 *
 * <p><strong>PREDICTION MATRIX:</strong></p>
 * <table border="1">
 * <tr><th>Hypothesis</th><th>C1</th><th>C2</th><th>C3</th><th>C4</th><th>C5</th></tr>
 * <tr><td>Fitness Clustering Causes Localization</td><td>Localizes</td><td><strong>Depends on fitness clustering</strong></td><td><strong>Depends on fitness clustering</strong></td><td>?</td><td>Depends on fitness clustering</td></tr>
 * <tr><td>Fitness Causes Localization</td><td>Localizes</td><td>Same speed</td><td>Same speed</td><td><strong>No localization</strong></td><td>Same speed</td></tr>
 * </table>
 *
 * <p><strong>KEY METRICS (v2):</strong></p>
 * <ul>
 *   <li>Strategy aggregation (v1 metric for comparison)</li>
 *   <li>Fitness clustering (v2 fitness-field metric)</li>
 *   <li>Factor localization (inter-factor proximity)</li>
 *   <li>Factor positions (array positions of 11 and 13)</li>
 *   <li>Mean factor distance from front (convergence speed)</li>
 *   <li>Fitness gradient (sorting progress)</li>
 *   <li>Strategy entropy (diversity measure)</li>
 * </ul>
 *
 * <p><strong>EXECUTION PROTOCOL:</strong></p>
 * <ol>
 *   <li>Run 30 repetitions per condition (150 total runs)</li>
 *   <li>Each run: 50 cells, target N=143 (11×13), max 100 steps</li>
 *   <li>Record per-step metrics to CSV</li>
 *   <li>Save array snapshots every 5 steps to JSON</li>
 *   <li>Convergence: both factors in positions 0-4 OR swaps=0</li>
 * </ol>
 *
 * <p><strong>OUTPUT STRUCTURE:</strong></p>
 * <pre>
 * experiments/clustering_vs_fitness_experiment_2026_01_10/
 * ├── results/
 * │   ├── C1_baseline_rep_001.csv
 * │   ├── C2_high_aggregation_rep_001.csv
 * │   └── ...
 * └── snapshots/
 *     ├── C1_baseline_rep_001_step_000.json
 *     ├── C1_baseline_rep_001_step_005.json
 *     └── ...
 * </pre>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Five conditions isolate fitness-field clustering vs fitness-driven sorting as causal factors</li>
 *   <li>30 reps per condition provide statistical power</li>
 *   <li>Per-step metrics enable time-series analysis</li>
 *   <li>Snapshots enable visualization and post-hoc analysis</li>
 *   <li>Reproducible: fixed random seeds (rep number = seed)</li>
 *   <li>Fitness-field metrics avoid circular reasoning in hypothesis testing</li>
 * </ul>
 *
 * <p><strong>REFERENCE:</strong> See experiments/clustering_vs_fitness_experiment_2026_01_10/EXPERIMENT_SETUP_AUDIT.md
 * for detailed analysis of v1 issues and rationale for v2 semantic realignment.</p>
 */
public class ClusteringVsFitnessExperiment {
    
    // ==================== CONFIGURATION CONSTANTS ====================
    
    /** Target semiprime to factor */
    private static final int TARGET = 143;
    
    /** Array size (number of candidate cells) */
    private static final int ARRAY_SIZE = 50;
    
    /** Maximum execution steps per run */
    private static final int MAX_STEPS = 100;
    
    /** Number of repetitions per condition */
    private static final int REPS_PER_CONDITION = 30;
    
    /** Snapshot interval (save array state every N steps) */
    private static final int SNAPSHOT_INTERVAL = 5;
    
    /** Experiment output directory */
    private static final String OUTPUT_DIR = "experiments/clustering_vs_fitness_experiment_2026_01_10";
    
    /** Results subdirectory */
    private static final String RESULTS_DIR = OUTPUT_DIR + "/results";
    
    /** Snapshots subdirectory */
    private static final String SNAPSHOTS_DIR = OUTPUT_DIR + "/snapshots";
    
    // ==================== EXPERIMENT EXECUTION ====================
    
    /**
     * Run complete experiment: all five conditions, all repetitions.
     *
     * <p><strong>PURPOSE:</strong> Execute full experimental protocol and generate
     * all CSV and JSON output files.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Create output directories</li>
     *   <li>For each condition (C1-C5):</li>
     *   <li>  For each repetition (1-30):</li>
     *   <li>    Generate cell array for condition</li>
     *   <li>    Execute experiment with metric recording</li>
     *   <li>    Write CSV and JSON snapshots</li>
     *   <li>  Print progress after each condition</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> CSV files in results/ and JSON files in snapshots/</p>
     *
     * @throws IOException if file I/O fails
     */
    public void runFullExperiment() throws IOException {
        System.out.println("=== Clustering vs Fitness Experiment ===");
        System.out.println("Target: N = " + TARGET);
        System.out.println("Array size: " + ARRAY_SIZE);
        System.out.println("Max steps: " + MAX_STEPS);
        System.out.println("Reps per condition: " + REPS_PER_CONDITION);
        System.out.println();
        
        // Create output directories
        createOutputDirectories();
        
        // Run all conditions
        runCondition("C1_baseline", this::generateC1Baseline);
        runCondition("C2_high_aggregation", this::generateC2HighAggregation);
        runCondition("C3_zero_aggregation", this::generateC3ZeroAggregation);
        runCondition("C4_fitness_control", this::generateC4FitnessControl);
        runCondition("C5_homogeneous", this::generateC5Homogeneous);
        
        System.out.println("\n=== Experiment Complete ===");
        System.out.println("Output directory: " + OUTPUT_DIR);
    }
    
    /**
     * Run all repetitions for a single condition.
     *
     * <p><strong>PURPOSE:</strong> Execute 30 repetitions with consistent condition
     * generator, writing results and snapshots for each rep.</p>
     *
     * @param conditionName the condition name (e.g., "C1_baseline")
     * @param generator the condition generator function
     * @throws IOException if file I/O fails
     */
    private void runCondition(String conditionName, ConditionGenerator generator) throws IOException {
        System.out.println("Running condition: " + conditionName);
        
        for (int rep = 1; rep <= REPS_PER_CONDITION; rep++) {
            // Generate cell array for this condition
            List<FactorCell> cells = generator.generate(rep);
            
            // Run experiment and collect metrics
            List<StepMetrics> metrics = executeExperimentRun(cells);
            
            // Write results CSV
            String csvFilename = String.format("%s/%s_rep_%03d.csv", RESULTS_DIR, conditionName, rep);
            writeCsvResults(csvFilename, metrics);
            
            // Write snapshots
            writeSnapshots(conditionName, rep, cells, metrics);
            
            // Progress indicator
            if (rep % 5 == 0) {
                System.out.printf("  Completed %d/%d reps\n", rep, REPS_PER_CONDITION);
            }
        }
        
        System.out.println("  ✓ Condition complete\n");
    }
    
    /**
     * Execute single experiment run with metric recording.
     *
     * <p><strong>PURPOSE:</strong> Run execution steps until convergence or max steps,
     * recording metrics at each step.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Record initial state (step 0)</li>
     *   <li>While not converged and steps < MAX_STEPS:</li>
     *   <li>  Execute step with GenericExecutionEngine</li>
     *   <li>  Compute and record metrics</li>
     *   <li>  Check convergence (both factors in positions 0-4 or swaps=0)</li>
     * </ol>
     *
     * <p><strong>CONVERGENCE CRITERIA:</strong></p>
     * <ul>
     *   <li>Both factors in positions 0-4 (successful localization), OR</li>
     *   <li>swapCount = 0 (no beneficial swaps remain)</li>
     * </ul>
     *
     * @param cells the cell array to execute
     * @return list of StepMetrics (one per step including initial state)
     */
    private List<StepMetrics> executeExperimentRun(List<FactorCell> cells) {
        List<StepMetrics> metricsHistory = new ArrayList<>();
        GenericExecutionEngine<Integer, FactorStrategy> engine = new GenericExecutionEngine<>();
        
        // Record initial state (step 0, swaps 0)
        StepMetrics initialMetrics = computeMetrics(0, cells, 0);
        metricsHistory.add(initialMetrics);
        
        // Execute steps
        int step = 0;
        while (step < MAX_STEPS) {
            // Execute one step
            int swaps = engine.executeStep(castToAbstractCells(cells));
            step++;
            
            // Compute and record metrics
            StepMetrics stepMetrics = computeMetrics(step, cells, swaps);
            metricsHistory.add(stepMetrics);
            
            // Check convergence
            if (isConverged(stepMetrics)) {
                break;
            }
        }
        
        return metricsHistory;
    }
    
    /**
     * Check if experiment has converged.
     *
     * <p><strong>CONVERGENCE CRITERIA:</strong></p>
     * <ul>
     *   <li>Both factors in positions 0-4 (successful localization), OR</li>
     *   <li>swapCount = 0 (no beneficial swaps remain)</li>
     * </ul>
     *
     * @param metrics the current step metrics
     * @return true if converged
     */
    private boolean isConverged(StepMetrics metrics) {
        // No swaps = converged
        if (metrics.swapCount == 0) {
            return true;
        }
        
        // Both factors in positions 0-4 = successful localization
        int pos11 = metrics.factorPositions[0];
        int pos13 = metrics.factorPositions[1];
        
        if (pos11 >= 0 && pos11 <= 4 && pos13 >= 0 && pos13 <= 4) {
            return true;
        }
        
        return false;
    }
    
    // ==================== CONDITION GENERATORS ====================
    
    /**
     * C1: Baseline (Natural Chimeric Distribution).
     *
     * <p><strong>PURPOSE:</strong> Reference condition with random strategy distribution.</p>
     *
     * <p><strong>CONFIGURATION:</strong></p>
     * <ul>
     *   <li>Distribution: 33% SMALL_PRIMES, 34% FERMAT_NEAR_SQRT, 33% RANDOM_SAMPLE</li>
     *   <li>Expected aggregation: ~50-60% (random baseline)</li>
     *   <li>True factors: Present (11 and 13 may appear in any strategy's candidates)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong> Should show standard factor localization dynamics.
     * Serves as reference for comparing other conditions.</p>
     *
     * @param seed random seed (use rep number for reproducibility)
     * @return list of FactorCells with random spatial distribution
     */
    private List<FactorCell> generateC1Baseline(long seed) {
        Random rand = new Random(seed);
        List<FactorCell> cells = new ArrayList<>();
        int sqrtN = (int) Math.sqrt(TARGET);
        
        // Generate candidates with duplicate handling to ensure we get exactly 50 cells
        // 33% SMALL_PRIMES, 34% FERMAT, 33% RANDOM
        int[] counts = {17, 17, 16}; // SMALL_PRIMES, FERMAT, RANDOM
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
                    // Generate from small primes with wrapping
                    List<Integer> primes = CandidateGenerator.generateSmallPrimes(TARGET, sqrtN, rand);
                    if (primes.isEmpty()) primes.add(2);
                    candidate = primes.get(rand.nextInt(primes.size()));
                } else if (strategy == FactorStrategy.FERMAT_NEAR_SQRT) {
                    // Generate near sqrt
                    candidate = Math.max(2, sqrtN - 5 + rand.nextInt(11));
                    candidate = Math.min(candidate, sqrtN);
                } else {
                    // Random sample
                    candidate = 2 + rand.nextInt(sqrtN - 1);
                }
                
                cells.add(new FactorCell(candidate, TARGET, strategy, position++));
            }
        }
        
        // Shuffle for random spatial distribution
        java.util.Collections.shuffle(cells, rand);
        
        // Update positions after shuffle
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).updatePositionTo(i);
        }
        
        return cells;
    }
    
    /**
     * C2: High Aggregation (Pre-clustered by Strategy).
     *
     * <p><strong>PURPOSE:</strong> Test if high initial aggregation accelerates factor localization.</p>
     *
     * <p><strong>CONFIGURATION:</strong></p>
     * <ul>
     *   <li>Distribution: Same as C1 (33% / 34% / 33%)</li>
     *   <li>Spatial arrangement: Cells grouped by strategy</li>
     *   <li>  Positions 0-16: SMALL_PRIMES</li>
     *   <li>  Positions 17-33: FERMAT_NEAR_SQRT</li>
     *   <li>  Positions 34-49: RANDOM_SAMPLE</li>
     *   <li>Expected aggregation: ~75% (high clustering)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong></p>
     * <ul>
     *   <li>If clustering causes localization: Should localize FASTER than C1 and C3</li>
     *   <li>If fitness causes localization: Should localize at SAME SPEED as C1 and C3</li>
     * </ul>
     *
     * @param seed random seed
     * @return list of FactorCells pre-clustered by strategy
     */
    private List<FactorCell> generateC2HighAggregation(long seed) {
        Random rand = new Random(seed);
        List<FactorCell> cells = new ArrayList<>();
        int position = 0;
        int sqrtN = (int) Math.sqrt(TARGET);
        
        // Block 1: SMALL_PRIMES (positions 0-16, 17 cells)
        List<Integer> primes = CandidateGenerator.generateSmallPrimes(TARGET, sqrtN, rand);
        if (primes.isEmpty()) primes.add(2);
        for (int i = 0; i < 17; i++) {
            int candidate = primes.get(rand.nextInt(primes.size()));
            cells.add(new FactorCell(candidate, TARGET, FactorStrategy.SMALL_PRIMES, position++));
        }
        
        // Block 2: FERMAT_NEAR_SQRT (positions 17-33, 17 cells)
        for (int i = 0; i < 17; i++) {
            int candidate = Math.max(2, sqrtN - 5 + rand.nextInt(11));
            candidate = Math.min(candidate, sqrtN);
            cells.add(new FactorCell(candidate, TARGET, FactorStrategy.FERMAT_NEAR_SQRT, position++));
        }
        
        // Block 3: RANDOM_SAMPLE (positions 34-49, 16 cells)
        for (int i = 0; i < 16; i++) {
            int candidate = 2 + rand.nextInt(sqrtN - 1);
            cells.add(new FactorCell(candidate, TARGET, FactorStrategy.RANDOM_SAMPLE, position++));
        }
        
        return cells;
    }
    
    /**
     * C3: Zero Aggregation (Maximally Mixed Strategies).
     *
     * <p><strong>PURPOSE:</strong> Test if lack of clustering impedes factor localization.</p>
     *
     * <p><strong>CONFIGURATION:</strong></p>
     * <ul>
     *   <li>Distribution: Same as C1 (33% / 34% / 33%)</li>
     *   <li>Spatial arrangement: Alternating strategies</li>
     *   <li>  Pattern: SMALL_PRIMES, FERMAT, RANDOM, SMALL_PRIMES, FERMAT, RANDOM, ...</li>
     *   <li>Expected aggregation: ~0-10% (no adjacent same-strategy cells)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong></p>
     * <ul>
     *   <li>If clustering necessary for localization: Should NOT localize or localize SLOWLY</li>
     *   <li>If fitness sufficient: Should localize at SAME SPEED as C1</li>
     * </ul>
     *
     * @param seed random seed
     * @return list of FactorCells with maximally mixed strategies
     */
    private List<FactorCell> generateC3ZeroAggregation(long seed) {
        Random rand = new Random(seed);
        
        // Generate candidates for each strategy
        List<Integer> smallPrimes = CandidateGenerator.generateSmallPrimes(TARGET, 17, rand);
        List<Integer> fermat = CandidateGenerator.generateFermatNearSqrt(TARGET, 17, rand);
        List<Integer> random = CandidateGenerator.generateRandomSample(TARGET, 16, rand);
        
        // Interleave strategies
        List<FactorCell> cells = new ArrayList<>();
        FactorStrategy[] strategies = {
            FactorStrategy.SMALL_PRIMES,
            FactorStrategy.FERMAT_NEAR_SQRT,
            FactorStrategy.RANDOM_SAMPLE
        };
        
        int[] indices = {0, 0, 0}; // Track current index for each strategy
        
        for (int position = 0; position < ARRAY_SIZE; position++) {
            FactorStrategy strategy = strategies[position % 3];
            int candidate;
            
            if (strategy == FactorStrategy.SMALL_PRIMES) {
                candidate = smallPrimes.get(indices[0]++ % smallPrimes.size());
            } else if (strategy == FactorStrategy.FERMAT_NEAR_SQRT) {
                candidate = fermat.get(indices[1]++ % fermat.size());
            } else {
                candidate = random.get(indices[2]++ % random.size());
            }
            
            cells.add(new FactorCell(candidate, TARGET, strategy, position));
        }
        
        return cells;
    }
    
    /**
     * C4: Fitness Control (No True Factors).
     *
     * <p><strong>PURPOSE:</strong> Test if fitness gradient is necessary for localization.</p>
     *
     * <p><strong>CONFIGURATION:</strong></p>
     * <ul>
     *   <li>Distribution: Same as C1 (33% / 34% / 33%)</li>
     *   <li>Spatial arrangement: Random (like C1)</li>
     *   <li>Candidates: Manually exclude 11 and 13 (the true factors)</li>
     *   <li>Expected aggregation: ~50-60% (random baseline)</li>
     *   <li>Fitness landscape: No perfect factors (all fitness < 1.0)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong></p>
     * <ul>
     *   <li>If fitness drives localization: Should NOT show localization (no fitness peak)</li>
     *   <li>If clustering drives localization: Should still show some localization pattern</li>
     * </ul>
     *
     * @param seed random seed
     * @return list of FactorCells with NO true factors
     */
    private List<FactorCell> generateC4FitnessControl(long seed) {
        Random rand = new Random(seed);
        
        // Generate candidates for each strategy, then filter out 11 and 13
        List<Integer> smallPrimes = filterOutFactors(
            CandidateGenerator.generateSmallPrimes(TARGET, 20, rand)
        );
        List<Integer> fermat = filterOutFactors(
            CandidateGenerator.generateFermatNearSqrt(TARGET, 20, rand)
        );
        List<Integer> randomSample = filterOutFactors(
            CandidateGenerator.generateRandomSample(TARGET, 20, rand)
        );
        
        // Ensure we have enough candidates after filtering
        // Note: We allow duplicates since valid range [2, 10] has only 9 unique values
        // This is realistic for C4 (no true factors) - candidates can repeat
        int sqrtN = (int) Math.sqrt(TARGET);
        
        while (smallPrimes.size() < 17) {
            int candidate = 2 + rand.nextInt(sqrtN - 1); // [2, sqrtN]
            if (candidate != 11 && candidate != 13) {
                smallPrimes.add(candidate);
            }
        }
        while (fermat.size() < 17) {
            int candidate = 2 + rand.nextInt(sqrtN - 1); // [2, sqrtN]
            if (candidate != 11 && candidate != 13) {
                fermat.add(candidate);
            }
        }
        while (randomSample.size() < 16) {
            int candidate = 2 + rand.nextInt(sqrtN - 1); // [2, sqrtN]
            if (candidate != 11 && candidate != 13) {
                randomSample.add(candidate);
            }
        }
        
        // Create cells with shuffled strategies (like C1)
        List<FactorCell> cells = new ArrayList<>();
        int position = 0;
        
        for (int i = 0; i < 17; i++) {
            cells.add(new FactorCell(smallPrimes.get(i), TARGET, FactorStrategy.SMALL_PRIMES, position++));
        }
        for (int i = 0; i < 17; i++) {
            cells.add(new FactorCell(fermat.get(i), TARGET, FactorStrategy.FERMAT_NEAR_SQRT, position++));
        }
        for (int i = 0; i < 16; i++) {
            cells.add(new FactorCell(randomSample.get(i), TARGET, FactorStrategy.RANDOM_SAMPLE, position++));
        }
        
        // Shuffle for random spatial distribution
        java.util.Collections.shuffle(cells, rand);
        
        // Update positions after shuffle
        for (int i = 0; i < cells.size(); i++) {
            cells.get(i).updatePositionTo(i);
        }
        
        return cells;
    }
    
    /**
     * C5: Homogeneous Strategy (100% Aggregation).
     *
     * <p><strong>PURPOSE:</strong> Test if perfect aggregation alone is sufficient for localization.</p>
     *
     * <p><strong>CONFIGURATION:</strong></p>
     * <ul>
     *   <li>Distribution: 100% FERMAT_NEAR_SQRT</li>
     *   <li>Aggregation: 100% (all cells same strategy by definition)</li>
     *   <li>True factors: Present (FERMAT generates candidates near sqrt(143) ≈ 11.96, includes 11)</li>
     * </ul>
     *
     * <p><strong>HYPOTHESIS TEST:</strong></p>
     * <ul>
     *   <li>If aggregation causes localization: Should localize FASTEST (perfect clustering)</li>
     *   <li>If fitness causes localization: Should localize at SAME SPEED as C1 (aggregation irrelevant)</li>
     * </ul>
     *
     * @param seed random seed
     * @return list of FactorCells with single strategy
     */
    private List<FactorCell> generateC5Homogeneous(long seed) {
        Random rand = new Random(seed);
        List<FactorCell> cells = new ArrayList<>();
        
        // Generate enough FERMAT_NEAR_SQRT candidates (may include duplicates)
        List<Integer> candidates = new ArrayList<>();
        int sqrtN = (int) Math.sqrt(TARGET);
        
        while (candidates.size() < ARRAY_SIZE) {
            // Generate candidates near sqrt(N)
            int candidate = Math.max(2, sqrtN - 5 + rand.nextInt(11)); // [sqrtN-5, sqrtN+5]
            if (candidate <= sqrtN && candidate >= 2) {
                candidates.add(candidate);
            }
        }
        
        for (int position = 0; position < ARRAY_SIZE; position++) {
            cells.add(new FactorCell(candidates.get(position), TARGET, FactorStrategy.FERMAT_NEAR_SQRT, position));
        }
        
        return cells;
    }
    
    /**
     * Filter out true factors (11 and 13) from candidate list.
     *
     * <p><strong>PURPOSE:</strong> Remove perfect factors for C4 control condition.</p>
     *
     * @param candidates the candidate list
     * @return filtered list without 11 or 13
     */
    private List<Integer> filterOutFactors(List<Integer> candidates) {
        List<Integer> filtered = new ArrayList<>();
        for (Integer candidate : candidates) {
            if (candidate != 11 && candidate != 13) {
                filtered.add(candidate);
            }
        }
        return filtered;
    }
    
    // ==================== METRICS COMPUTATION ====================
    
    /**
     * Compute all metrics for current array state.
     *
     * <p><strong>PURPOSE:</strong> Measure all relevant metrics in a single pass
     * through the cell array.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Compute strategy aggregation (v1 metric for comparison)</li>
     *   <li>Compute fitness clustering (v2 fitness-field metric)</li>
     *   <li>Find factor positions and compute localization (v2 metric)</li>
     *   <li>Compute mean factor distance from front</li>
     *   <li>Compute fitness gradient mean and std</li>
     *   <li>Compute strategy entropy (global and front)</li>
     *   <li>Package into StepMetrics object</li>
     * </ol>
     *
     * @param step the step number
     * @param cells the cell array
     * @param swaps the number of swaps in this step
     * @return StepMetrics object with all measurements
     */
    private StepMetrics computeMetrics(int step, List<FactorCell> cells, int swaps) {
        // v1 metric: strategy-label aggregation (for comparison)
        double strategyAgg = computeStrategyAggregation(cells);
        
        // v2 metrics: fitness-field clustering and localization
        double fitnessClustering = computeFitnessClustering(cells);
        int[] factorPositions = findFactorPositions(cells);
        double factorLocalization = computeFactorLocalization(factorPositions, cells.size());
        
        // Other metrics
        double meanFactorDist = computeMeanFactorDistance(factorPositions);
        double[] fitnessGradient = computeFitnessGradient(cells);
        double entropyGlobal = computeStrategyEntropy(cells, 0, cells.size());
        double entropyFront = computeStrategyEntropy(cells, 0, Math.min(10, cells.size()));
        
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
            swaps
        );
    }
    
    /**
     * Compute strategy aggregation value (v1 metric - strategy-label clustering).
     *
     * <p><strong>SEMANTIC NOTE:</strong> This measures STRATEGY-LABEL adjacency,
     * not fitness-field clustering. Renamed from "aggregation" to "strategyAggregation"
     * for clarity in v2 experiment.</p>
     *
     * <p><strong>FORMULA:</strong> (cells with >= 1 same-strategy neighbor / total cells) × 100</p>
     *
     * <p><strong>REFERENCE:</strong> AlgotypeAggregationIndex.java</p>
     *
     * @param cells the cell array
     * @return strategy aggregation percentage [0.0, 100.0]
     */
    private double computeStrategyAggregation(List<FactorCell> cells) {
        if (cells.size() <= 1) {
            return 100.0;
        }
        
        int sameStrategyNeighborCount = 0;
        for (int i = 0; i < cells.size(); i++) {
            FactorStrategy currentStrategy = cells.get(i).readAlgotype();
            
            boolean hasLeftSame = (i > 0) && 
                (cells.get(i - 1).readAlgotype() == currentStrategy);
            boolean hasRightSame = (i < cells.size() - 1) && 
                (cells.get(i + 1).readAlgotype() == currentStrategy);
            
            if (hasLeftSame || hasRightSame) {
                sameStrategyNeighborCount++;
            }
        }
        
        return (sameStrategyNeighborCount * 100.0) / cells.size();
    }
    
    /**
     * Compute fitness clustering value (v2 metric - fitness-field spatial aggregation).
     *
     * <p><strong>SEMANTIC ALIGNMENT:</strong> This measures FITNESS-SIMILARITY adjacency,
     * independent of strategy labels. This avoids circular reasoning in experimental
     * design where clustering hypothesis is tested using clustering-based measurement.</p>
     *
     * <p><strong>FORMULA:</strong> (cells with >= 1 fitness-similar neighbor / total cells) × 100,
     * where "similar" means |fitness[i] - fitness[neighbor]| < threshold (0.1)</p>
     *
     * <p><strong>REFERENCE:</strong> FitnessSimilarityClusteringIndex.java</p>
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
     * Compute factor localization index (v2 metric - inter-factor proximity).
     *
     * <p><strong>SEMANTIC ALIGNMENT:</strong> Per Levin, "localization" is concentration
     * of high-fitness configurations. This metric captures pattern formation independent
     * of task-specific convergence criteria.</p>
     *
     * <p><strong>FORMULA:</strong> 1.0 - (interFactorDistance / maxDistance)</p>
     *
     * <p><strong>REFERENCE:</strong> FactorLocalizationIndex.java</p>
     *
     * @param factorPositions array of [pos_factor1, pos_factor2]
     * @param arraySize size of array (for normalization)
     * @return localization index [0.0, 1.0]
     */
    private double computeFactorLocalization(int[] factorPositions, int arraySize) {
        int pos1 = factorPositions[0];
        int pos2 = factorPositions[1];
        
        // If either factor missing, no localization possible
        if (pos1 < 0 || pos2 < 0) {
            return 0.0;
        }
        
        // Compute inter-factor distance
        int distance = Math.abs(pos1 - pos2);
        
        // Normalize by maximum possible distance and invert
        int maxDistance = arraySize - 1;
        return 1.0 - ((double) distance / maxDistance);
    }
    
    /**
     * Find positions of true factors (candidates 11 and 13).
     *
     * <p><strong>PURPOSE:</strong> Locate factors in array for localization analysis.</p>
     *
     * <p><strong>OUTPUTS:</strong> Two-element array [position of 11, position of 13].
     * If factor not present (C4 control), position = -1.</p>
     *
     * @param cells the cell array
     * @return array of [pos_11, pos_13]
     */
    private int[] findFactorPositions(List<FactorCell> cells) {
        int pos11 = -1;
        int pos13 = -1;
        
        for (int i = 0; i < cells.size(); i++) {
            int candidate = cells.get(i).readValue();
            if (candidate == 11) {
                pos11 = i;
            } else if (candidate == 13) {
                pos13 = i;
            }
        }
        
        return new int[]{pos11, pos13};
    }
    
    /**
     * Compute mean distance of factors from array front.
     *
     * <p><strong>FORMULA:</strong> average(position of 11, position of 13)</p>
     *
     * <p><strong>SPECIAL CASES:</strong></p>
     * <ul>
     *   <li>If both factors present: mean of their positions</li>
     *   <li>If one factor missing: position of present factor</li>
     *   <li>If both missing (C4): return -1.0</li>
     * </ul>
     *
     * @param factorPositions array of [pos_11, pos_13]
     * @return mean distance from front, or -1.0 if no factors present
     */
    private double computeMeanFactorDistance(int[] factorPositions) {
        int pos11 = factorPositions[0];
        int pos13 = factorPositions[1];
        
        if (pos11 >= 0 && pos13 >= 0) {
            return (pos11 + pos13) / 2.0;
        } else if (pos11 >= 0) {
            return (double) pos11;
        } else if (pos13 >= 0) {
            return (double) pos13;
        } else {
            return -1.0; // No factors present
        }
    }
    
    /**
     * Compute fitness gradient statistics.
     *
     * <p><strong>PURPOSE:</strong> Measure fitness landscape smoothness. Fitness gradient
     * quantifies how much fitness changes between adjacent cells.</p>
     *
     * <p><strong>FORMULA:</strong></p>
     * <ul>
     *   <li>gradient[i] = |fitness[i] - fitness[i+1]|</li>
     *   <li>mean = average(gradient)</li>
     *   <li>std = standard deviation(gradient)</li>
     * </ul>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>High mean = rough landscape (large fitness changes between neighbors)</li>
     *   <li>Low mean = smooth landscape (gradual fitness changes)</li>
     *   <li>High std = non-uniform gradient (some regions steep, others flat)</li>
     * </ul>
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
        
        // Compute mean
        double mean = gradients.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        // Compute standard deviation
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
     * <p><strong>PURPOSE:</strong> Measure strategy diversity in a region of the array.</p>
     *
     * <p><strong>FORMULA:</strong> H = -Σ(p_i × log₂(p_i)) where p_i = proportion of strategy i</p>
     *
     * <p><strong>INTERPRETATION:</strong></p>
     * <ul>
     *   <li>log₂(3) ≈ 1.585 = maximum entropy (uniform distribution of 3 strategies)</li>
     *   <li>0 = minimum entropy (single strategy dominates)</li>
     * </ul>
     *
     * <p><strong>USAGE:</strong> Called with different ranges:</p>
     * <ul>
     *   <li>Global: [0, arraySize) for entire array</li>
     *   <li>Front: [0, 10) for top 20% of array</li>
     * </ul>
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
        
        // Count strategies in range
        Map<FactorStrategy, Integer> counts = new HashMap<>();
        for (int i = startIdx; i < endIdx; i++) {
            FactorStrategy strategy = cells.get(i).readAlgotype();
            counts.put(strategy, counts.getOrDefault(strategy, 0) + 1);
        }
        
        // Compute entropy
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
    
    // ==================== FILE I/O ====================
    
    /**
     * Create output directories if they don't exist.
     */
    private void createOutputDirectories() throws IOException {
        Files.createDirectories(Paths.get(RESULTS_DIR));
        Files.createDirectories(Paths.get(SNAPSHOTS_DIR));
    }
    
    /**
     * Write metrics to CSV file.
     *
     * <p><strong>FORMAT:</strong> Header row + one row per step</p>
     *
     * @param filename the CSV filename
     * @param metrics the metrics history
     * @throws IOException if write fails
     */
    private void writeCsvResults(String filename, List<StepMetrics> metrics) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // Write header
            writer.println(StepMetrics.getCsvHeader());
            
            // Write data rows
            for (StepMetrics m : metrics) {
                writer.println(m.toCsvRow());
            }
        }
    }
    
    /**
     * Write array snapshots to JSON files.
     *
     * <p><strong>PURPOSE:</strong> Save array state every SNAPSHOT_INTERVAL steps
     * for visualization and post-hoc analysis.</p>
     *
     * <p><strong>FORMAT:</strong> JSON with step, condition, rep, and cell array</p>
     *
     * @param conditionName the condition name
     * @param rep the repetition number
     * @param cells the cell array
     * @param metrics the metrics history (to determine which steps to snapshot)
     * @throws IOException if write fails
     */
    private void writeSnapshots(
            String conditionName,
            int rep,
            List<FactorCell> cells,
            List<StepMetrics> metrics) throws IOException {
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        for (StepMetrics m : metrics) {
            if (m.stepNumber % SNAPSHOT_INTERVAL == 0) {
                String filename = String.format(
                    "%s/%s_rep_%03d_step_%03d.json",
                    SNAPSHOTS_DIR,
                    conditionName,
                    rep,
                    m.stepNumber
                );
                
                // Build snapshot object
                Map<String, Object> snapshot = new HashMap<>();
                snapshot.put("step", m.stepNumber);
                snapshot.put("condition", conditionName);
                snapshot.put("rep", rep);
                snapshot.put("cells", buildCellsArray(cells));
                
                // Write JSON
                try (FileWriter writer = new FileWriter(filename)) {
                    gson.toJson(snapshot, writer);
                }
            }
        }
    }
    
    /**
     * Build JSON-serializable cell array.
     *
     * <p><strong>FORMAT:</strong> Array of objects with position, candidate, fitness, strategy</p>
     *
     * @param cells the cell array
     * @return list of cell objects
     */
    private List<Map<String, Object>> buildCellsArray(List<FactorCell> cells) {
        List<Map<String, Object>> cellsArray = new ArrayList<>();
        
        for (FactorCell cell : cells) {
            Map<String, Object> cellObj = new HashMap<>();
            cellObj.put("position", cell.readCurrentPosition());
            cellObj.put("candidate", cell.readValue());
            cellObj.put("fitness", cell.getFitness());
            cellObj.put("strategy", cell.readAlgotype().name());
            cellsArray.add(cellObj);
        }
        
        return cellsArray;
    }
    
    // ==================== UTILITY ====================
    
    /**
     * Cast List&lt;FactorCell&gt; to List&lt;AbstractCell&gt; for generic engine.
     */
    @SuppressWarnings("unchecked")
    private List<AbstractCell<Integer, FactorStrategy>> castToAbstractCells(List<FactorCell> cells) {
        return (List<AbstractCell<Integer, FactorStrategy>>) (List<?>) cells;
    }
    
    // ==================== FUNCTIONAL INTERFACE ====================
    
    /**
     * Functional interface for condition generator methods.
     */
    @FunctionalInterface
    private interface ConditionGenerator {
        List<FactorCell> generate(long seed);
    }
    
    // ==================== MAIN ====================
    
    /**
     * Main entry point for running the experiment.
     *
     * @param args command-line arguments (unused)
     * @throws IOException if file I/O fails
     */
    public static void main(String[] args) throws IOException {
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        experiment.runFullExperiment();
    }
}
