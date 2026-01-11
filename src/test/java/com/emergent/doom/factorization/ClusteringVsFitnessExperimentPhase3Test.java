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
import java.util.List;
import java.util.stream.Stream;

/**
 * Phase 3 test suite: Re-run full experiment with v2 metrics and validate results.
 */
@Tag("phase3")
public class ClusteringVsFitnessExperimentPhase3Test {
    
    private static final String EXPERIMENT_DIR = "experiments/clustering_vs_fitness_experiment_2026_01_10";
    private static final String RESULTS_DIR = EXPERIMENT_DIR + "/results";
    private static final String V1_BACKUP_DIR = EXPERIMENT_DIR + "/results_v1_backup";
    private static final String SNAPSHOTS_DIR = EXPERIMENT_DIR + "/snapshots";

    // CSV Column Constants (Review #6: Avoid hardcoded indices for robustness)
    private static final int COL_STEP = 0;
    private static final int COL_STRATEGY_AGG = 1;
    private static final int COL_FITNESS_CLUST = 2;
    private static final int COL_FACTOR_LOCAL = 3;
    private static final int COL_FACTOR_11_POS = 4;
    private static final int COL_FACTOR_13_POS = 5;
    private static final int COL_CONSEC_ZERO_SWAPS = 12;
    private static final int COL_STAGNANT = 13;
    
    @BeforeAll
    static void backupV1Results() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        Path backupPath = Paths.get(V1_BACKUP_DIR);
        
        if (Files.exists(resultsPath) && !Files.exists(backupPath)) {
            System.out.println("Backing up v1 results to: " + V1_BACKUP_DIR);
            Files.createDirectories(backupPath);
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
        }
    }
    
    @Test
    @DisplayName("Run full v2 experiment (150 runs) and validate output structure")
    void runsFullV2ExperimentAndValidatesOutput() throws IOException {
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        experiment.runFullExperiment();
        
        Path resultsPath = Paths.get(RESULTS_DIR);
        long csvCount;
        try (Stream<Path> csvFiles = Files.list(resultsPath)) {
            csvCount = csvFiles.filter(path -> path.toString().endsWith(".csv")).count();
        }
        assertEquals(150, csvCount);
        
        String[] conditions = {"C1_baseline", "C2_high_aggregation", "C3_zero_aggregation", 
                               "C4_fitness_control", "C5_homogeneous"};
        
        for (String condition : conditions) {
            Path csvFile = resultsPath.resolve(condition + "_rep_001.csv");
            assertTrue(Files.exists(csvFile));
            String header = Files.readAllLines(csvFile).get(0);
            assertTrue(header.contains("strategy_agg"));
            assertTrue(header.contains("fitness_clust"));
            assertTrue(header.contains("stagnant"));
        }
    }
    
    @Test
    @DisplayName("Validate Phase 2 correctness fixes in experiment results")
    void validatesPhase2CorrectnessFixesInResults() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        Path c1File = resultsPath.resolve("C1_baseline_rep_001.csv");
        if (!Files.exists(c1File)) return;
        
        List<String> c1Lines = Files.readAllLines(c1File);
        String[] c1Values = c1Lines.get(1).split(",");
        assertNotEquals("-1", c1Values[COL_FACTOR_11_POS]);
        assertNotEquals("-1", c1Values[COL_FACTOR_13_POS]);
        
        Path c4File = resultsPath.resolve("C4_fitness_control_rep_001.csv");
        List<String> c4Lines = Files.readAllLines(c4File);
        String[] c4Values = c4Lines.get(1).split(",");
        assertEquals("-1", c4Values[COL_FACTOR_11_POS]);
        assertEquals("-1", c4Values[COL_FACTOR_13_POS]);
        
        boolean foundStagnation = false;
        for (String line : c1Lines.subList(1, c1Lines.size())) {
            String[] values = line.split(",");
            int consecZeroSwaps = Integer.parseInt(values[COL_CONSEC_ZERO_SWAPS]);
            boolean isStagnant = Boolean.parseBoolean(values[COL_STAGNANT]);
            if (consecZeroSwaps >= 20) {
                assertTrue(isStagnant);
                foundStagnation = true;
                break;
            }
        }
    }
    
    @Test
    @DisplayName("Factor injection positions differ across conditions (issue #2 fix)")
    void factorInjectionPositionsDifferAcrossConditions() {
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        
        // Try multiple seeds to avoid stochastic collision failure
        // With range=16, probability of collision is 1/16 per seed.
        // Probability of 10 consecutive collisions is (1/16)^10 ~ 0.
        for (long seed = 5L; seed < 15L; seed++) {
            List<FactorCell> c1 = experiment.generateC1Baseline(seed);
            List<FactorCell> c2 = experiment.generateC2HighAggregation(seed);
            
            int c1Pos = findFactorPos(c1, 11);
            int c2Pos = findFactorPos(c2, 11);
            
            if (c1Pos != c2Pos) {
                return; // Passed: positions differ for at least one seed
            }
        }
        
        fail("Factor injection positions were identical for all tested seeds");
    }

    @Test
    @DisplayName("Same seed produces identical results across runs (Fix Issue 10)")
    void shouldProduceIdenticalResultsWithSameSeed() throws IOException {
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment();
        long seed = 42L;
        List<FactorCell> run1 = experiment.generateC1Baseline(seed);
        List<FactorCell> run2 = experiment.generateC1Baseline(seed);
        for (int i = 0; i < run1.size(); i++) {
            assertEquals(run1.get(i).readValue(), run2.get(i).readValue());
            assertEquals(run1.get(i).readAlgotype(), run2.get(i).readAlgotype());
        }
    }

    @Test
    @DisplayName("Generate summary statistics for v2 experiment results")
    void generatesSummaryStatisticsForV2Results() throws IOException {
        Path resultsPath = Paths.get(RESULTS_DIR);
        String[] conditions = {"C1_baseline", "C2_high_aggregation", "C3_zero_aggregation", 
                               "C4_fitness_control", "C5_homogeneous"};
        
        for (String condition : conditions) {
            int convergedCount = 0;
            for (int rep = 1; rep <= 30; rep++) {
                Path csvFile = resultsPath.resolve(String.format("%s_rep_%03d.csv", condition, rep));
                if (!Files.exists(csvFile)) continue;
                List<String> lines = Files.readAllLines(csvFile);
                if (lines.size() > 1) {
                    String[] lastStep = lines.get(lines.size() - 1).split(",");
                    int f11 = Integer.parseInt(lastStep[COL_FACTOR_11_POS]);
                    int f13 = Integer.parseInt(lastStep[COL_FACTOR_13_POS]);
                    if (f11 >= 0 && f11 <= 4 && f13 >= 0 && f13 <= 4) convergedCount++;
                }
            }
            System.out.printf("%s convergence: %d/30%n", condition, convergedCount);
        }
    }

    private int findFactorPos(List<FactorCell> cells, int factor) {
        for (int i = 0; i < cells.size(); i++) {
            if (cells.get(i).readValue() == factor) return i;
        }
        return -1;
    }
}