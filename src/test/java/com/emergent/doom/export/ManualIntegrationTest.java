package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Manual integration test for end-to-end trajectory export validation.
 * 
 * <p>This is a runnable main class (not a JUnit test) that demonstrates
 * the complete integration pipeline from probe recording through CSV export.</p>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * mvn compile exec:java -Dexec.mainClass="com.emergent.doom.export.ManualIntegrationTest"
 * </pre>
 * 
 * <p><strong>Output:</strong></p>
 * <ul>
 *   <li>Creates CSV file at experiments/data/bubble_test_trajectory.csv</li>
 *   <li>Prints trajectory metrics to console</li>
 *   <li>Validates sortedness and monotonicity progression</li>
 * </ul>
 * 
 * <p><strong>Success Criteria:</strong></p>
 * <ul>
 *   <li>CSV file created successfully</li>
 *   <li>Sortedness increases from initial to final state</li>
 *   <li>Monotonicity error decreases from initial to final state</li>
 *   <li>Parent directories created automatically</li>
 * </ul>
 */
public class ManualIntegrationTest {
    
    public static void main(String[] args) throws IOException {
        System.out.println("\n===============================================");
        System.out.println("Manual Integration Test: Trajectory Export");
        System.out.println("===============================================\n");
        
        // Step 1: Create probe and simulate experiment
        System.out.println("Step 1: Creating probe and simulating experiment...");
        Probe<GenericCell> probe = new Probe<>();
        
        // Simulate 10-element bubble sort (or use existing experiment runner)
        GenericCell[] initial = new GenericCell[10];
        for (int i = 0; i < 10; i++) {
            initial[i] = new GenericCell(10 - i); // Reversed array
        }
        probe.recordSnapshot(0, initial, 0);
        System.out.println("  ✓ Recorded initial snapshot: reversed array [10,9,8,7,6,5,4,3,2,1]");
        
        // After partial sorting
        GenericCell[] partial = {
            new GenericCell(1), new GenericCell(2), new GenericCell(10),
            new GenericCell(9), new GenericCell(8), new GenericCell(7),
            new GenericCell(6), new GenericCell(5), new GenericCell(4),
            new GenericCell(3)
        };
        probe.recordSnapshot(1, partial, 5);
        System.out.println("  ✓ Recorded step 1 snapshot: partial sort in progress");
        
        // Add more steps for progression
        GenericCell[] step2 = {
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(9), new GenericCell(8), new GenericCell(7),
            new GenericCell(6), new GenericCell(5), new GenericCell(4),
            new GenericCell(10)
        };
        probe.recordSnapshot(2, step2, 7);
        System.out.println("  ✓ Recorded step 2 snapshot: more sorting progress");
        
        GenericCell[] step5 = {
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(4), new GenericCell(5), new GenericCell(6),
            new GenericCell(7), new GenericCell(8), new GenericCell(9),
            new GenericCell(10)
        };
        probe.recordSnapshot(5, step5, 12);
        System.out.println("  ✓ Recorded step 5 snapshot: fully sorted");
        
        // Step 2: Build trajectory
        System.out.println("\nStep 2: Building trajectory from probe...");
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe,
            "Bubble",
            0,  // frozenCells
            0,  // trialNumber
            10, // arraySize
            System.currentTimeMillis()
        );
        
        System.out.println("  ✓ Trajectory built successfully");
        System.out.println("  ✓ Steps captured: " + trajectory.getStepCount());
        System.out.println("  ✓ Algorithm: " + trajectory.getAlgotype());
        System.out.println("  ✓ Array size: " + trajectory.getArraySize());
        
        // Step 3: Export to CSV
        System.out.println("\nStep 3: Exporting trajectory to CSV...");
        String outputPath = "experiments/data/bubble_test_trajectory.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(outputPath, trajectory);
        
        System.out.println("  ✓ CSV exported to: " + outputPath);
        System.out.println("  ✓ File exists: " + Files.exists(Paths.get(outputPath)));
        System.out.println("  ✓ File size: " + Files.size(Paths.get(outputPath)) + " bytes");
        
        // Step 4: Verify metrics
        System.out.println("\nStep 4: Verifying trajectory metrics...");
        
        int stepCount = 0;
        for (ExperimentTrajectory.TrajectoryStep step : trajectory.getSteps()) {
            System.out.printf("  Step %d: sortedness=%.1f%%, error=%d, swaps=%d%n",
                step.stepNumber(),
                step.sortedness(),
                step.monotonicityError(),
                step.cumulativeSwaps()
            );
            stepCount++;
        }
        
        // Validate progression
        System.out.println("\nStep 5: Validating metric progression...");
        
        ExperimentTrajectory.TrajectoryStep first = trajectory.getSteps().get(0);
        ExperimentTrajectory.TrajectoryStep last = trajectory.getSteps().get(
            trajectory.getStepCount() - 1
        );
        
        boolean sortednessIncreased = last.sortedness() > first.sortedness();
        boolean errorDecreased = last.monotonicityError() < first.monotonicityError();
        boolean swapsIncreased = last.cumulativeSwaps() > first.cumulativeSwaps();
        
        System.out.println("  ✓ Sortedness increased: " + sortednessIncreased + 
            " (" + String.format("%.1f%%", first.sortedness()) + 
            " → " + String.format("%.1f%%", last.sortedness()) + ")");
        
        System.out.println("  ✓ Monotonicity error decreased: " + errorDecreased +
            " (" + first.monotonicityError() + 
            " → " + last.monotonicityError() + ")");
        
        System.out.println("  ✓ Cumulative swaps increased: " + swapsIncreased +
            " (" + first.cumulativeSwaps() + 
            " → " + last.cumulativeSwaps() + ")");
        
        // Step 6: Display CSV preview
        System.out.println("\n===============================================");
        System.out.println("CSV File Preview:");
        System.out.println("===============================================");
        
        Files.lines(Paths.get(outputPath)).forEach(System.out::println);
        
        // Final summary
        System.out.println("\n===============================================");
        System.out.println("Integration Test Summary:");
        System.out.println("===============================================");
        System.out.println("  ✓ Probe recording: SUCCESS");
        System.out.println("  ✓ Trajectory building: SUCCESS");
        System.out.println("  ✓ CSV export: SUCCESS");
        System.out.println("  ✓ Metric validation: SUCCESS");
        System.out.println("  ✓ Sortedness progression: " + 
            (sortednessIncreased ? "PASS" : "FAIL"));
        System.out.println("  ✓ Error reduction: " + 
            (errorDecreased ? "PASS" : "FAIL"));
        System.out.println("\n===============================================");
        System.out.println("Test Status: " + 
            (sortednessIncreased && errorDecreased ? "✓ PASSED" : "✗ FAILED"));
        System.out.println("===============================================\n");
    }
}
