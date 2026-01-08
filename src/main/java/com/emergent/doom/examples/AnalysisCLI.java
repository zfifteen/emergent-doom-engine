package com.emergent.doom.examples;

import com.emergent.doom.factorization.BatchExperimentRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Command-line interface for running batch factorization experiments.
 *
 * <p><strong>USAGE:</strong></p>
 * <pre>
 * java com.emergent.doom.examples.AnalysisCLI [numTrials] [outputDir]
 * 
 * # Default: 100 trials
 * java com.emergent.doom.examples.AnalysisCLI
 * 
 * # Custom: 50 trials
 * java com.emergent.doom.examples.AnalysisCLI 50
 * 
 * # Custom: 200 trials, specific output
 * java com.emergent.doom.examples.AnalysisCLI 200 ./results
 * </pre>
 *
 * <p><strong>OUTPUTS:</strong></p>
 * <ul>
 *   <li>Individual trial CSVs: trial_N_seed_S.csv</li>
 *   <li>Aggregate statistics: statistics.csv</li>
 *   <li>Summary printed to stdout</li>
 * </ul>
 */
public class AnalysisCLI {
    
    public static void main(String[] args) throws IOException {
        // Parse arguments
        int numTrials = 100;  // Default
        String outputDir = "./factorization_results";
        
        if (args.length > 0) {
            try {
                numTrials = Integer.parseInt(args[0]);
                if (numTrials <= 0) {
                    System.err.println("Trial count must be positive, got: " + numTrials);
                    System.exit(1);
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid trial count: " + args[0]);
                System.exit(1);
            }
        }
        
        if (args.length > 1) {
            outputDir = args[1];
        }
        
        // Create output directory
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
        
        // Print header
        System.out.println();
        System.out.println("===== Factorization Experiment Batch Analysis =====");
        System.out.println("Started: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        System.out.println("Configuration:");
        System.out.println("  Target (N): 143 (11 \u00d7 13)");
        System.out.println("  Array size: 50 candidates");
        System.out.println("  Strategy distribution: 33%/33%/34%");
        System.out.println("  Number of trials: " + numTrials);
        System.out.println("  Output directory: " + outputDir);
        System.out.println();
        System.out.println("===== Running Trials =====");
        System.out.println();
        
        // Run batch
        BatchExperimentRunner runner = new BatchExperimentRunner(
            143,  // target N
            50,   // array size
            numTrials,
            42L   // base seed
        );
        
        long startTime = System.currentTimeMillis();
        BatchExperimentRunner.BatchResults results = runner.runBatch();
        long elapsedMillis = System.currentTimeMillis() - startTime;
        
        // Print results
        System.out.println();
        System.out.println("===== Batch Completed =====");
        System.out.println("Elapsed time: " + formatDuration(elapsedMillis));
        System.out.println();
        
        results.printSummary();
        
        // Export results
        System.out.println("===== Exporting Results =====");
        System.out.println();
        
        System.out.print("Exporting individual trial CSVs...");
        System.out.flush();
        results.exportAllTrialsToCSV(outputDir);
        System.out.println(" done");
        
        System.out.print("Exporting aggregate statistics...");
        System.out.flush();
        results.exportStatisticsToCSV(java.nio.file.Paths.get(outputDir, "statistics.csv").toString());
        System.out.println(" done");
        
        System.out.println();
        System.out.println("Results exported to: " + outputDir);
        System.out.println("✓ Analysis complete!");
    }
    
    /**
     * Format duration in milliseconds as readable string.
     */
    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        } else {
            return seconds + "s";
        }
    }
}
