package com.emergent.doom.factorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Investigation into clustering vs fitness hypothesis with large semiprimes (4-6 digits).
 * Testing C3 advantage when search space expands and distractors become more dilute.
 */
@Tag("investigation")
public class ClusteringVsFitnessLargeScaleInvestigationTest {

    private static final String BASE_OUTPUT_DIR = "experiments/large_scale_investigation_2026_01_11";

    @Test
    @DisplayName("Run investigation with 4, 5, and 6 digit semiprimes")
    void runLargeScaleInvestigation() throws IOException {
        // 4-Digit: 4087 (61 * 67)
        // Fermat strategy fits well (sqrt 63.9). SmallPrimes are distractors.
        runPermutation(4087, 61, 67, "target_4087");

        // 5-Digit: 40803 (201 * 203)
        // Fermat strategy fits well (sqrt 202). SmallPrimes [2..59] are strong distractors/junk.
        runPermutation(40803, 201, 203, "target_40803");

        // 6-Digit: 249991 (499 * 501)
        // Fermat strategy fits well (sqrt 500). Search space [2..500] is large.
        runPermutation(249991, 499, 501, "target_249991");
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
                    // v2 CSV format: indices 4 and 5 are factor positions
                    // header: step,strategy_agg,fitness_clust,factor_local,factor_11_pos,factor_13_pos,...
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
