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
 * Very large scale investigation of maximal mixing hypothesis (6, 7, 8 digit semiprimes)
 * using scaled array sizes.
 */
@Tag("investigation")
public class ClusteringVsFitnessVeryLargeScaleTest {

    private static final String BASE_OUTPUT_DIR = "experiments/very_large_scale_investigation_2026_01_11";

    @Test
    @DisplayName("Run investigation with 6, 7, and 8 digit semiprimes and scaled arrays")
    void runVeryLargeScaleInvestigation() throws IOException {
        // 6-Digit: 249991 (499 * 501)
        // Previous run (Size 50) failed (3.3%). Trying Size 100 to see if it helps.
        // Ratio: 500 candidates / 100 cells = 5:1.
        runPermutation(249991, 499, 501, "target_249991_size_100", 100, 200);

        // 7-Digit: 1000489 (1009 * 991)
        // Sqrt ~1000. Candidates ~1000.
        // Array Size 200 => Ratio 5:1.
        runPermutation(1000489, 1009, 991, "target_1000489_size_200", 200, 300);

        // 8-Digit: 10004963 (3163 * 3163) (Perfect square, actually 3163 is prime? wait 3163*3163=10004569. Close enough.)
        // Let's use 99980001 (9999 * 9999).
        // Let's pick distinct primes near sqrt(10^7) ~ 3162.
        // 3163 is prime. 3167 is prime.
        // 3163 * 3167 = 10017221.
        // Candidates ~3167. Array Size 300 => Ratio ~10:1.
        runPermutation(10017221, 3163, 3167, "target_10017221_size_300", 300, 400);
    }

    private void runPermutation(int target, int factorA, int factorB, String subDirName, int arraySize, int maxSteps) throws IOException {
        String outputDir = BASE_OUTPUT_DIR + "/" + subDirName;
        System.out.println("----------------------------------------------------------------");
        System.out.printf("Starting Permutation: Target %d (%d x %d)\n", target, factorA, factorB);
        System.out.printf("Config: Array Size %d, Max Steps %d\n", arraySize, maxSteps);
        System.out.println("Output Directory: " + outputDir);
        
        ClusteringVsFitnessExperiment experiment = new ClusteringVsFitnessExperiment(
            target, factorA, factorB, outputDir, arraySize, maxSteps
        );
        experiment.runFullExperiment();
        
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
                    int posA = Integer.parseInt(lastStep[4]);
                    int posB = Integer.parseInt(lastStep[5]);
                    
                    // Convergence: [0, 4]
                    if (posA >= 0 && posA <= 4 && posB >= 0 && posB <= 4) {
                        convergedCount++;
                        convergedRuns++;
                        totalSteps += Integer.parseInt(lastStep[0]); 
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
