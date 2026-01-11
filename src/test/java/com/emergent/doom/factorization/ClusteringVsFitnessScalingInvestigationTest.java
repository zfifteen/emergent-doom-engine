package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

/**
 * Investigation into scaling effects on clustering vs fitness hypothesis.
 * Testing if minimal aggregation (C3) advantage holds for larger semiprimes.
 */
@Tag("investigation")
public class ClusteringVsFitnessScalingInvestigationTest {

    private static final String BASE_OUTPUT_DIR = "experiments/scaling_investigation_2026_01_11";
    private static final int COL_FACTOR_11_POS = 4; // These indices match v2 CSV format
    private static final int COL_FACTOR_13_POS = 5;

    @Test
    @DisplayName("Run scaling investigation with larger semiprimes")
    void runScalingInvestigation() throws IOException {
        // Permutation 1: Target 221 (13 * 17)
        runPermutation(221, 13, 17, "target_221");

        // Permutation 2: Target 323 (17 * 19)
        runPermutation(323, 17, 19, "target_323");

        // Permutation 3: Target 437 (19 * 23)
        runPermutation(437, 19, 23, "target_437");
    }

    private void runPermutation(int target, int factorA, int factorB, String subDirName) throws IOException {
        String outputDir = BASE_OUTPUT_DIR + "/" + subDirName;
        System.out.println("----------------------------------------------------------------");
        System.out.printf("Starting Permutation: Target %d (%d x %d)\n", target, factorA, factorB);
        System.out.println("Output Directory: " + outputDir);
        
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment(
            target, factorA, factorB, outputDir
        );
        experiment.runFullExperiment();
        
        // Analyze and print summary
        printSummary(outputDir, target, factorA, factorB);
    }

    private void printSummary(String outputDir, int target, int factorA, int factorB) throws IOException {
        Path resultsPath = Paths.get(outputDir + "/results");
        String[] conditions = {"C1_baseline", "C2_high_aggregation", "C3_zero_aggregation", 
                               "C4_fitness_control", "C5_homogeneous"};
        
        System.out.println("\nSummary for Target " + target + ":");
        System.out.printf("%-25s | %-12s | %-15s\n", "Condition", "Convergence", "Mean Steps");
        System.out.println("--------------------------|--------------|---------------");

        for (String condition : conditions) {
            int convergedCount = 0;
            long totalSteps = 0;
            int convergedRuns = 0;

            for (int rep = 1; rep <= 30; rep++) {
                Path csvFile = resultsPath.resolve(String.format("%s_rep_%03d.csv", condition, rep));
                if (!Files.exists(csvFile)) continue;
                
                List<String> lines = Files.readAllLines(csvFile);
                if (lines.size() > 1) {
                    String[] lastStep = lines.get(lines.size() - 1).split(",");
                    // Note: Indices 4 and 5 are factor positions
                    int posA = Integer.parseInt(lastStep[4]);
                    int posB = Integer.parseInt(lastStep[5]);
                    
                    // Convergence: [0, 4]
                    if (posA >= 0 && posA <= 4 && posB >= 0 && posB <= 4) {
                        convergedCount++;
                        convergedRuns++;
                        totalSteps += Integer.parseInt(lastStep[0]); // step number
                    }
                }
            }
            
            double convergenceRate = (convergedCount / 30.0) * 100.0;
            String meanSteps = convergedRuns > 0 ? String.format("%.1f", (double)totalSteps / convergedRuns) : "N/A";
            
            System.out.printf("%-25s | %5.1f%% (%2d/30) | %s\n", condition, convergenceRate, convergedCount, meanSteps);
        }
        System.out.println();
    }
}
