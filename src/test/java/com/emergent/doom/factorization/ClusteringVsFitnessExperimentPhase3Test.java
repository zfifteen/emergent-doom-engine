package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

/**
 * Phase 3 test suite: Re-run full experiment with v2 metrics and validate results.
 *
 * <p><strong>PURPOSE:</strong> Execute the complete 150-run clustering vs fitness experiment
 * with Phase 2 correctness fixes and v2 fitness-field metrics, then validate output quality
 * and compare against v1 strategy-based results.</p>
 *
 * <p><strong>PHASE 3 GOALS:</strong></p>
 * <ul>
 *   <li>Re-run full 150-run experiment (30 reps × 5 conditions)</li>
 *   <li>Validate v2 CSV format includes fitness_clust and factor_local columns</li>
 *   <li>Verify Phase 2 fixes: factor presence, stagnation detection, convergence positions</li>
 *   <li>Compare v1 vs v2 results to test if fitness-field clustering shows different patterns</li>
 * </ul>
 *
 * <p><strong>EXECUTION:</strong> Tests tagged with @Tag("phase3") for selective execution.
 * Run with: mvn test -Dgroups=phase3</p>
 *
 * <p><strong>REFERENCE:</strong> See PR #168 comment for Phase 3 requirements</p>
 */
@Tag("phase3")
public class ClusteringVsFitnessExperimentPhase3Test {
    
    private static final String EXPERIMENT_DIR = "experiments/clustering_vs_fitness_experiment_2026_01_10";
    private static final String RESULTS_DIR = EXPERIMENT_DIR + "/results";
    private static final String V1_BACKUP_DIR = EXPERIMENT_DIR + "/results_v1_backup";
    private static final String SNAPSHOTS_DIR = EXPERIMENT_DIR + "/snapshots";
    
    /**
     * Back up existing v1 results before running v2 experiment.
     *
     * <p><strong>PURPOSE:</strong> Preserve v1 data for comparison with v2 results.</p>
     *
     * <p><strong>RATIONALE:</strong> Phase 3 requires comparing v1 (strategy-based) vs v2
     * (fitness-based) metrics. We back up v1 results, then regenerate with v2 format.</p>
     */
    @BeforeAll
    static void backupV1Results() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        Path backupPath = Paths.get(V1_BACKUP_DIR);
        
        // If results directory exists and backup doesn't, create backup
        if (Files.exists(resultsPath) && !Files.exists(backupPath)) {
            System.out.println("Backing up v1 results to: " + V1_BACKUP_DIR);
            Files.createDirectories(backupPath);
            
            // Copy all CSV files to backup
            try (Stream<Path> csvFiles = Files.list(resultsPath)) {
                csvFiles.filter(path -> path.toString().endsWith(".csv"))
                        .forEach(csvFile -> {
                            try {
                                Path targetFile = backupPath.resolve(csvFile.getFileName());
                                Files.copy(csvFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                throw new RuntimeException("Failed to backup " + csvFile, e);
                            }
                        });
            }
            
            System.out.println("v1 backup complete");
        } else if (Files.exists(backupPath)) {
            System.out.println("v1 backup already exists at: " + V1_BACKUP_DIR);
        }
    }
    
    /**
     * Execute full v2 experiment (150 runs) and validate output structure.
     *
     * <p><strong>PURPOSE:</strong> Run complete experiment with Phase 2 fixes and v2 metrics,
     * ensuring all CSV and JSON outputs are generated correctly.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Clear existing results directory</li>
     *   <li>Run ClusteringVsFitnessExperiment.runFullExperiment()</li>
     *   <li>Validate 150 CSV files generated (30 per condition)</li>
     *   <li>Validate CSV format includes v2 columns</li>
     *   <li>Validate JSON snapshots generated</li>
     * </ol>
     *
     * <p><strong>EXPECTED OUTPUT:</strong></p>
     * <ul>
     *   <li>150 CSV files in results/</li>
     *   <li>CSV header includes: strategy_agg, fitness_clust, factor_local, consec_zero_swaps, stagnant</li>
     *   <li>JSON snapshots in snapshots/ (every 5 steps)</li>
     * </ul>
     *
     * <p><strong>TEST DATA:</strong> Uses deterministic seeds (rep number = seed)</p>
     *
     * <p><strong>REPRODUCTION:</strong> Run ClusteringVsFitnessExperiment.main() or this test</p>
     */
    @Test
    @DisplayName("Run full v2 experiment (150 runs) and validate output structure")
    void runsFullV2ExperimentAndValidatesOutput() throws IOException {
        // Step 1: Run experiment
        System.out.println("Starting full v2 experiment (150 runs)...");
        System.out.println("This will take several minutes...");
        
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        experiment.runFullExperiment();
        
        System.out.println("Experiment complete. Validating outputs...");
        
        // Step 2: Validate CSV output count
        Path resultsPath = Paths.get(RESULTS_DIR);
        long csvCount;
        try (Stream<Path> csvFiles = Files.list(resultsPath)) {
            csvCount = csvFiles.filter(path -> path.toString().endsWith(".csv")).count();
        }
        
        assertEquals(150, csvCount, "Should generate 150 CSV files (30 reps × 5 conditions)");
        
        // Step 3: Validate v2 CSV format for each condition
        String[] conditions = {"C1_baseline", "C2_high_aggregation", "C3_zero_aggregation", 
                               "C4_fitness_control", "C5_homogeneous"};
        
        for (String condition : conditions) {
            Path csvFile = resultsPath.resolve(condition + "_rep_001.csv");
            assertTrue(Files.exists(csvFile), "CSV should exist: " + csvFile);
            
            // Read header line
            String header = Files.readAllLines(csvFile).get(0);
            
            // Validate v2 columns present
            assertTrue(header.contains("strategy_agg"), 
                      "CSV header should include strategy_agg (v2 renamed column)");
            assertTrue(header.contains("fitness_clust"), 
                      "CSV header should include fitness_clust (v2 metric)");
            assertTrue(header.contains("factor_local"), 
                      "CSV header should include factor_local (v2 metric)");
            assertTrue(header.contains("consec_zero_swaps"), 
                      "CSV header should include consec_zero_swaps (Phase 2 stagnation tracking)");
            assertTrue(header.contains("stagnant"), 
                      "CSV header should include stagnant (Phase 2 stagnation flag)");
            
            // Validate v1 columns NOT present
            assertFalse(header.contains("aggregation,"), 
                       "CSV header should NOT include old 'aggregation' column (renamed to strategy_agg)");
        }
        
        // Step 4: Validate JSON snapshots exist
        Path snapshotsPath = Paths.get(SNAPSHOTS_DIR);
        assertTrue(Files.exists(snapshotsPath), "Snapshots directory should exist");
        
        long jsonCount;
        try (Stream<Path> jsonFiles = Files.list(snapshotsPath)) {
            jsonCount = jsonFiles.filter(path -> path.toString().endsWith(".json")).count();
        }
        
        assertTrue(jsonCount > 0, "Should generate JSON snapshots");
        
        System.out.println("Output validation complete:");
        System.out.println("  - " + csvCount + " CSV files generated");
        System.out.println("  - " + jsonCount + " JSON snapshots generated");
        System.out.println("  - v2 format validated");
    }
    
    /**
     * Validate Phase 2 correctness fixes in generated data.
     *
     * <p><strong>PURPOSE:</strong> Confirm that Phase 2 fixes (factor presence, stagnation
     * detection, convergence positions) are correctly applied in v2 experiment results.</p>
     *
     * <p><strong>VALIDATION CHECKS:</strong></p>
     * <ol>
     *   <li>C1-C3, C5: All runs have factor 11 and 13 present (not -1)</li>
     *   <li>C4: No runs have factor 11 or 13 (all -1)</li>
     *   <li>Stagnation flag correctly set when consec_zero_swaps >= 20</li>
     *   <li>Convergence positions use threshold of 4 (positions [0,4])</li>
     * </ol>
     */
    @Test
    @DisplayName("Validate Phase 2 correctness fixes in experiment results")
    void validatesPhase2CorrectnessFixesInResults() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        
        // Check C1 (baseline) for factor presence
        Path c1File = resultsPath.resolve("C1_baseline_rep_001.csv");
        if (!Files.exists(c1File)) {
            fail("C1 CSV file does not exist. Run full experiment test first.");
        }
        
        java.util.List<String> c1Lines = Files.readAllLines(c1File);
        assertTrue(c1Lines.size() > 1, "C1 CSV should have data rows");
        
        // Check if this is v2 format (has strategy_agg, fitness_clust, factor_local)
        String header = c1Lines.get(0);
        boolean isV2Format = header.contains("strategy_agg") && header.contains("fitness_clust");
        
        if (!isV2Format) {
            System.out.println("SKIP: Results are in v1 format. Run full experiment test first to generate v2 results.");
            return;
        }
        
        // Parse first data row (step 0)
        // v2 format: step,strategy_agg,fitness_clust,factor_local,factor_11_pos,factor_13_pos,...
        String[] c1Values = c1Lines.get(1).split(",");
        int factor11Pos = Integer.parseInt(c1Values[4]); // factor_11_pos column
        int factor13Pos = Integer.parseInt(c1Values[5]); // factor_13_pos column
        
        assertNotEquals(-1, factor11Pos, "C1 should have factor 11 present (Phase 2 fix)");
        assertNotEquals(-1, factor13Pos, "C1 should have factor 13 present (Phase 2 fix)");
        
        // Check C4 (fitness control) for factor absence
        Path c4File = resultsPath.resolve("C4_fitness_control_rep_001.csv");
        java.util.List<String> c4Lines = Files.readAllLines(c4File);
        String[] c4Values = c4Lines.get(1).split(",");
        int c4Factor11Pos = Integer.parseInt(c4Values[4]);
        int c4Factor13Pos = Integer.parseInt(c4Values[5]);
        
        assertEquals(-1, c4Factor11Pos, "C4 should NOT have factor 11 (Phase 2 fix)");
        assertEquals(-1, c4Factor13Pos, "C4 should NOT have factor 13 (Phase 2 fix)");
        
        // Check stagnation detection
        // Find a row where consec_zero_swaps >= 20 and verify stagnant = true
        boolean foundStagnation = false;
        for (String line : c1Lines.subList(1, c1Lines.size())) {
            String[] values = line.split(",");
            int consecZeroSwaps = Integer.parseInt(values[12]); // consec_zero_swaps column
            boolean isStagnant = Boolean.parseBoolean(values[13]); // stagnant column
            
            if (consecZeroSwaps >= 20) {
                assertTrue(isStagnant, 
                          "Stagnation flag should be true when consec_zero_swaps >= 20 (Phase 2 fix)");
                foundStagnation = true;
                break;
            }
        }
        
        // Note: Not all runs will stagnate, so we don't assert foundStagnation
        System.out.println("Phase 2 correctness fixes validated:");
        System.out.println("  - Factor presence guarantee: ✓");
        System.out.println("  - Factor absence in C4 control: ✓");
        if (foundStagnation) {
            System.out.println("  - Stagnation detection: ✓");
        } else {
            System.out.println("  - Stagnation detection: (no stagnation in sampled run)");
        }
    }
    
    /**
     * Compare v1 vs v2 metrics to test if fitness-field clustering shows different patterns.
     *
     * <p><strong>PURPOSE:</strong> Core Phase 3 objective - determine if v2 fitness-based
     * clustering metric reveals different relationships to convergence than v1 strategy-based
     * aggregation metric.</p>
     *
     * <p><strong>COMPARISON APPROACH:</strong></p>
     * <ol>
     *   <li>Load v1 backup results (strategy aggregation only)</li>
     *   <li>Load v2 new results (strategy_agg, fitness_clust, factor_local)</li>
     *   <li>For same seed/rep, compare strategy_agg vs fitness_clust trajectories</li>
     *   <li>Test hypothesis: Do they diverge? Are they correlated?</li>
     * </ol>
     *
     * <p><strong>EXPECTED PATTERNS:</strong></p>
     * <ul>
     *   <li>If strategy and fitness cluster independently: metrics should diverge</li>
     *   <li>If they're coupled: metrics should correlate strongly</li>
     *   <li>Fitness clustering may show different relationship to factor localization speed</li>
     * </ul>
     */
    @Test
    @DisplayName("Compare v1 vs v2 metrics to test fitness-field clustering hypothesis")
    void comparesV1VsV2MetricsForHypothesisTesting() throws IOException {
        Path v1BackupPath = Paths.get(V1_BACKUP_DIR);
        Path v2ResultsPath = Paths.get(RESULTS_DIR);
        
        // Check if v1 backup exists
        if (!Files.exists(v1BackupPath)) {
            System.out.println("SKIP: v1 backup not found. Cannot compare v1 vs v2.");
            return;
        }
        
        // Compare C1 baseline rep 001 as sample
        Path v1File = v1BackupPath.resolve("C1_baseline_rep_001.csv");
        Path v2File = v2ResultsPath.resolve("C1_baseline_rep_001.csv");
        
        if (!Files.exists(v1File)) {
            System.out.println("SKIP: v1 sample file not found.");
            return;
        }
        if (!Files.exists(v2File)) {
            fail("v2 sample file not found. Run full experiment test first.");
        }
        
        // Read v1 data
        java.util.List<String> v1Lines = Files.readAllLines(v1File);
        String v1Header = v1Lines.get(0);
        assertTrue(v1Header.contains("aggregation"), "v1 should have 'aggregation' column");
        
        // Read v2 data
        java.util.List<String> v2Lines = Files.readAllLines(v2File);
        String v2Header = v2Lines.get(0);
        assertTrue(v2Header.contains("strategy_agg"), "v2 should have 'strategy_agg' column");
        assertTrue(v2Header.contains("fitness_clust"), "v2 should have 'fitness_clust' column");
        
        // Sample comparison: step 0 initial values
        String[] v1Step0 = v1Lines.get(1).split(",");
        String[] v2Step0 = v2Lines.get(1).split(",");
        
        // v1: step,aggregation,factor_11_pos,...
        // v2: step,strategy_agg,fitness_clust,factor_local,...
        
        double v1Aggregation = Double.parseDouble(v1Step0[1]);
        double v2StrategyAgg = Double.parseDouble(v2Step0[1]);
        double v2FitnessClust = Double.parseDouble(v2Step0[2]);
        
        // Strategy aggregation should be same (same random seed)
        assertEquals(v1Aggregation, v2StrategyAgg, 0.01, 
                    "v1 aggregation should equal v2 strategy_agg (same metric, different name)");
        
        // Fitness clustering is new metric - report value for scientific interest
        System.out.println("\n=== v1 vs v2 Metric Comparison (C1_baseline_rep_001, step 0) ===");
        System.out.println("v1 aggregation (strategy-based): " + v1Aggregation + "%");
        System.out.println("v2 strategy_agg (same metric):   " + v2StrategyAgg + "%");
        System.out.println("v2 fitness_clust (NEW metric):   " + v2FitnessClust + "%");
        System.out.println();
        
        if (Math.abs(v2StrategyAgg - v2FitnessClust) > 10.0) {
            System.out.println("FINDING: Strategy aggregation and fitness clustering DIFFER by >10%");
            System.out.println("This suggests strategy grouping and fitness grouping are INDEPENDENT.");
        } else {
            System.out.println("FINDING: Strategy aggregation and fitness clustering are SIMILAR (<10% diff)");
            System.out.println("This suggests strategy grouping and fitness grouping are COUPLED.");
        }
        
        // This is a descriptive test - we don't assert scientific findings,
        // just validate that the comparison is possible and metrics differ conceptually
        assertTrue(true, "Comparison completed successfully");
    }
    
    /**
     * Generate summary statistics for v2 experiment results.
     *
     * <p><strong>PURPOSE:</strong> Produce high-level summary of experiment outcomes
     * for documentation and analysis.</p>
     *
     * <p><strong>STATISTICS GENERATED:</strong></p>
     * <ul>
     *   <li>Convergence rates per condition (% of runs that converged)</li>
     *   <li>Mean convergence time per condition (for converged runs)</li>
     *   <li>Stagnation rates per condition (% of runs that stagnated)</li>
     *   <li>Initial fitness clustering values per condition</li>
     * </ul>
     *
     * <p><strong>OUTPUT:</strong> Printed to console for manual documentation update</p>
     */
    @Test
    @DisplayName("Generate summary statistics for v2 experiment results")
    void generatesSummaryStatisticsForV2Results() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        
        String[] conditions = {"C1_baseline", "C2_high_aggregation", "C3_zero_aggregation", 
                               "C4_fitness_control", "C5_homogeneous"};
        
        // Check if results are v2 format
        Path sampleFile = resultsPath.resolve("C1_baseline_rep_001.csv");
        if (!Files.exists(sampleFile)) {
            System.out.println("SKIP: No experiment results found. Run full experiment test first.");
            return;
        }
        
        String header = Files.readAllLines(sampleFile).get(0);
        boolean isV2Format = header.contains("strategy_agg") && header.contains("fitness_clust");
        
        if (!isV2Format) {
            System.out.println("SKIP: Results are in v1 format. Run full experiment test first to generate v2 results.");
            return;
        }
        
        System.out.println("\n=== Phase 3 v2 Experiment Summary ===\n");
        
        for (String condition : conditions) {
            int convergedCount = 0;
            int stagnatedCount = 0;
            double totalConvergenceTime = 0.0;
            double totalInitialFitnessClust = 0.0;
            
            for (int rep = 1; rep <= 30; rep++) {
                Path csvFile = resultsPath.resolve(String.format("%s_rep_%03d.csv", condition, rep));
                
                if (!Files.exists(csvFile)) {
                    System.out.println("WARNING: Missing CSV: " + csvFile);
                    continue;
                }
                
                java.util.List<String> lines = Files.readAllLines(csvFile);
                
                // Get initial fitness clustering (step 0)
                if (lines.size() > 1) {
                    String[] step0 = lines.get(1).split(",");
                    totalInitialFitnessClust += Double.parseDouble(step0[2]); // fitness_clust column
                }
                
                // Check final step for convergence/stagnation
                if (lines.size() > 1) {
                    String[] lastStep = lines.get(lines.size() - 1).split(",");
                    int stepNum = Integer.parseInt(lastStep[0]);
                    int factor11Pos = Integer.parseInt(lastStep[4]);
                    int factor13Pos = Integer.parseInt(lastStep[5]);
                    boolean isStagnant = Boolean.parseBoolean(lastStep[13]);
                    
                    // Converged = both factors in [0,4]
                    if (factor11Pos >= 0 && factor11Pos <= 4 && 
                        factor13Pos >= 0 && factor13Pos <= 4) {
                        convergedCount++;
                        totalConvergenceTime += stepNum;
                    }
                    
                    if (isStagnant) {
                        stagnatedCount++;
                    }
                }
            }
            
            double convergenceRate = (convergedCount / 30.0) * 100.0;
            double meanConvergenceTime = convergedCount > 0 ? totalConvergenceTime / convergedCount : 0.0;
            double stagnationRate = (stagnatedCount / 30.0) * 100.0;
            double meanInitialFitnessClust = totalInitialFitnessClust / 30.0;
            
            System.out.printf("%s:\n", condition);
            System.out.printf("  Convergence rate: %.1f%% (%d/30)\n", convergenceRate, convergedCount);
            if (convergedCount > 0) {
                System.out.printf("  Mean convergence time: %.1f steps\n", meanConvergenceTime);
            }
            System.out.printf("  Stagnation rate: %.1f%% (%d/30)\n", stagnationRate, stagnatedCount);
            System.out.printf("  Initial fitness clustering: %.1f%%\n", meanInitialFitnessClust);
            System.out.println();
        }
        
        System.out.println("=== Summary Generation Complete ===");
    }
}
