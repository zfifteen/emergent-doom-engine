package com.emergent.doom.experiments.clustering;

/**
 * Runner for clustering validation experiment.
 * 
 * <p><strong>PURPOSE:</strong> As an experimenter, I want to run the full clustering
 * validation suite and generate a comprehensive report so that I can verify the EDE
 * reproduces Levin paper results.</p>
 *
 * <p><strong>USAGE:</strong>
 * <pre>{@code
 * mvn test-compile exec:java -Dexec.mainClass="com.emergent.doom.experiments.clustering.ClusteringValidationRunner" -Dexec.classpathScope=test
 * }</pre>
 * </p>
 *
 * <p><strong>OUTPUT:</strong> Console output with statistical results and validation status</p>
 *
 * <p><strong>EXPECTED DURATION:</strong> ~30-60 seconds for 400 total trials</p>
 */
public class ClusteringValidationRunner {
    
    public static void main(String[] args) {
        System.out.println("Emergent Doom Engine - Clustering Validation");
        System.out.println("Running full validation suite (400 trials)...");
        System.out.println();
        
        // Create and run experiment
        ClusteringValidationExperiment experiment = new ClusteringValidationExperiment();
        ClusteringValidationExperiment.ValidationReport report = experiment.runFullValidation();
        
        // Print summary
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println("VALIDATION SUMMARY");
        System.out.println("=".repeat(70));
        System.out.println();
        
        System.out.printf("Timestamp: %tF %<tT%n", report.timestamp());
        System.out.println("Hardware: " + report.hardwareInfo());
        System.out.printf("Total trials: %d%n%n", report.totalTrialsRun());
        
        // Print results for each pair
        for (var entry : report.pairResults().entrySet()) {
            ClusteringValidationExperiment.AlgotypePair pair = entry.getKey();
            ClusteringValidationExperiment.PairValidationResult result = entry.getValue();
            
            System.out.println(pair + ":");
            System.out.printf("  Peak aggregation: %.2f%% ± %.2f%%%n", 
                result.meanPeakAggregation(), result.stdPeakAggregation());
            System.out.printf("  Peak timing: %.2f%% ± %.2f%% of sorting progress%n",
                result.meanPeakTiming() * 100, result.stdPeakTiming() * 100);
            System.out.printf("  p-value vs paper: %.4f %s%n",
                result.pValueVsPaper(),
                result.pValueVsPaper() >= 0.05 ? "✓" : "✗");
            System.out.printf("  p-value vs control: %.4f %s%n",
                result.pValueVsControl(),
                result.pValueVsControl() < 0.05 ? "✓" : "✗");
            System.out.println();
        }
        
        // Print control result
        ClusteringValidationExperiment.PairValidationResult control = report.controlResult();
        System.out.println("Control (homogeneous):");
        System.out.printf("  Peak aggregation: %.2f%% ± %.2f%%%n", 
            control.meanPeakAggregation(), control.stdPeakAggregation());
        System.out.println();
        
        // Overall validation status
        System.out.println("=".repeat(70));
        System.out.println("VALIDATION STATUS");
        System.out.println("=".repeat(70));
        
        boolean allPassed = true;
        for (var result : report.pairResults().values()) {
            boolean matchesPaper = result.pValueVsPaper() >= 0.05;
            boolean differsFromControl = result.pValueVsControl() < 0.05;
            if (!matchesPaper || !differsFromControl) {
                allPassed = false;
                System.out.printf("✗ %s: ", result.pair());
                if (!matchesPaper) System.out.print("does not match paper ");
                if (!differsFromControl) System.out.print("does not differ from control");
                System.out.println();
            } else {
                System.out.printf("✓ %s: matches paper and differs from control%n", result.pair());
            }
        }
        
        System.out.println();
        if (allPassed) {
            System.out.println("✓ ALL VALIDATION CRITERIA MET");
            System.out.println("Clustering baseline validated - ready to extract ClusteringPrimitive API");
        } else {
            System.out.println("✗ SOME VALIDATION CRITERIA NOT MET");
            System.out.println("Review results and debug implementation");
        }
        
        System.out.println("=".repeat(70));
    }
}
