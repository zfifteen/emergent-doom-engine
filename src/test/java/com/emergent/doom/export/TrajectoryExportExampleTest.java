package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example test demonstrating the complete trajectory export pipeline.
 * 
 * <p>This test shows end-to-end usage of:
 * <ul>
 *   <li>Probe for recording snapshots</li>
 *   <li>TrajectoryBuilder for converting probe data</li>
 *   <li>TrajectoryDataExporter for CSV export</li>
 * </ul>
 * 
 * <p>Prints CSV output to console for visual inspection.</p>
 */
@DisplayName("Trajectory Export Example Pipeline")
class TrajectoryExportExampleTest {
    
    /**
     * PURPOSE: Demonstrate complete trajectory export pipeline from probe to CSV.
     * 
     * INPUTS: Simulated bubble sort on reversed 10-element array
     * EXPECTED OUTPUT: Console shows CSV with metadata and per-step metrics
     * TEST DATA: Array [10, 9, 8, 7, 6, 5, 4, 3, 2, 1] sorted to [1, 2, 3...10]
     * REPRODUCTION: Fixed initial array ensures deterministic output
     */
    @Test
    @DisplayName("demonstrates probe to CSV export pipeline")
    void demonstratesProbeToCSVExportPipeline() throws IOException {
        System.out.println("\n=== Trajectory Export Example ===\n");
        
        // Step 1: Create probe and simulate experiment
        System.out.println("Step 1: Creating probe and simulating bubble sort...");
        Probe<GenericCell> probe = new Probe<>();
        
        // Initial state: reversed array
        GenericCell[] initial = new GenericCell[10];
        for (int i = 0; i < 10; i++) {
            initial[i] = new GenericCell(10 - i);
        }
        probe.recordSnapshot(0, initial, 0);
        System.out.println("  Recorded initial snapshot: [10, 9, 8, 7, 6, 5, 4, 3, 2, 1]");
        
        // After first pass (some swaps)
        GenericCell[] step1 = new GenericCell[]{
            new GenericCell(9), new GenericCell(8), new GenericCell(7),
            new GenericCell(6), new GenericCell(5), new GenericCell(4),
            new GenericCell(3), new GenericCell(2), new GenericCell(1),
            new GenericCell(10)
        };
        probe.recordSnapshot(1, step1, 9);
        System.out.println("  Recorded step 1 snapshot: [9, 8, 7, 6, 5, 4, 3, 2, 1, 10]");
        
        // After second pass (more sorting progress)
        GenericCell[] step2 = new GenericCell[]{
            new GenericCell(8), new GenericCell(7), new GenericCell(6),
            new GenericCell(5), new GenericCell(4), new GenericCell(3),
            new GenericCell(2), new GenericCell(1), new GenericCell(9),
            new GenericCell(10)
        };
        probe.recordSnapshot(2, step2, 8);
        System.out.println("  Recorded step 2 snapshot: [8, 7, 6, 5, 4, 3, 2, 1, 9, 10]");
        
        // Partially sorted state
        GenericCell[] step5 = new GenericCell[]{
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(4), new GenericCell(5), new GenericCell(8),
            new GenericCell(7), new GenericCell(6), new GenericCell(9),
            new GenericCell(10)
        };
        probe.recordSnapshot(5, step5, 12);
        System.out.println("  Recorded step 5 snapshot: [1, 2, 3, 4, 5, 8, 7, 6, 9, 10]");
        
        // Fully sorted state
        GenericCell[] finalState = new GenericCell[]{
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(4), new GenericCell(5), new GenericCell(6),
            new GenericCell(7), new GenericCell(8), new GenericCell(9),
            new GenericCell(10)
        };
        probe.recordSnapshot(10, finalState, 5);
        System.out.println("  Recorded final snapshot: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]");
        
        // Step 2: Build trajectory from probe
        System.out.println("\nStep 2: Building trajectory from probe...");
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe,
            "Bubble",
            0,    // frozenCells
            0,    // trialNumber
            10,   // arraySize
            System.currentTimeMillis()
        );
        
        System.out.println("  Trajectory built with " + trajectory.getStepCount() + " steps");
        
        // Step 3: Print trajectory metrics to console
        System.out.println("\nStep 3: Displaying trajectory metrics...\n");
        printTrajectoryAsCSV(trajectory);
        
        // Step 4: Verify metrics show expected progression
        System.out.println("\nStep 4: Validating metrics...");
        
        List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
        
        // Verify we have all snapshots
        assertEquals(5, steps.size(), "Should have 5 trajectory steps");
        
        // Verify sortedness progression (should generally increase)
        double initialSortedness = steps.get(0).sortedness();
        double finalSortedness = steps.get(steps.size() - 1).sortedness();
        
        System.out.println("  ✓ Initial sortedness: " + String.format("%.1f%%", initialSortedness));
        System.out.println("  ✓ Final sortedness: " + String.format("%.1f%%", finalSortedness));
        assertTrue(finalSortedness > initialSortedness, 
            "Sortedness should increase from initial to final");
        
        // Verify monotonicity error progression (should generally decrease)
        int initialError = steps.get(0).monotonicityError();
        int finalError = steps.get(steps.size() - 1).monotonicityError();
        
        System.out.println("  ✓ Initial monotonicity error: " + initialError);
        System.out.println("  ✓ Final monotonicity error: " + finalError);
        assertTrue(finalError < initialError, 
            "Monotonicity error should decrease from initial to final");
        
        // Verify cumulative swaps increase
        int initialSwaps = steps.get(0).cumulativeSwaps();
        int finalSwaps = steps.get(steps.size() - 1).cumulativeSwaps();
        
        System.out.println("  ✓ Initial cumulative swaps: " + initialSwaps);
        System.out.println("  ✓ Final cumulative swaps: " + finalSwaps);
        assertTrue(finalSwaps > initialSwaps, 
            "Cumulative swaps should increase");
        
        System.out.println("\n=== Example Complete ===\n");
    }
    
    /**
     * Prints trajectory in CSV format to console for visual inspection.
     */
    private void printTrajectoryAsCSV(ExperimentTrajectory trajectory) {
        System.out.println("=== CSV Output Preview ===");
        System.out.println("# Metadata");
        System.out.println("algotype," + trajectory.getAlgotype());
        System.out.println("frozen_cells," + trajectory.getFrozenCells());
        System.out.println("trial_number," + trajectory.getTrialNumber());
        System.out.println("array_size," + trajectory.getArraySize());
        System.out.println("timestamp," + trajectory.getTimestamp());
        System.out.println("# Trajectory Data");
        
        if (trajectory.hasAggregation()) {
            System.out.println("step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons,aggregation");
        } else {
            System.out.println("step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons");
        }
        
        for (ExperimentTrajectory.TrajectoryStep step : trajectory.getSteps()) {
            System.out.printf("%d,%.1f,%d,%d,%d",
                step.stepNumber(),
                step.sortedness(),
                step.monotonicityError(),
                step.cumulativeSwaps(),
                step.cumulativeComparisons()
            );
            
            if (trajectory.hasAggregation() && step.aggregation() != null) {
                System.out.printf(",%.1f", step.aggregation());
            }
            
            System.out.println();
        }
        System.out.println("=== End CSV ===\n");
    }
}
