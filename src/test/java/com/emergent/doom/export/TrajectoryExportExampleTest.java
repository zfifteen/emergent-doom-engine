package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Example demonstration of complete trajectory export pipeline.
 * 
 * <p>This test demonstrates the end-to-end workflow for exporting experiment
 * trajectories as specified in the issue requirements.</p>
 */
class TrajectoryExportExampleTest {

    @TempDir
    Path tempDir;

    /**
     * PURPOSE: Demonstrate complete pipeline from probe recording to CSV export.
     * 
     * This is a working example that shows how to:
     * 1. Record snapshots during experiment execution
     * 2. Build trajectory from probe
     * 3. Export trajectory to CSV
     * 4. Access exported data for analysis
     * 
     * EXPECTED OUTPUT: CSV file with metadata and per-step trajectory data
     */
    @Test
    @DisplayName("Complete trajectory export pipeline example")
    void completeTrajectoryExportPipelineExample() throws IOException {
        // Step 1: Create probe and simulate experiment
        Probe<GenericCell> probe = new Probe<>();
        
        // Simulate bubble sort execution with snapshots
        // Initial state: [5, 2, 8, 1, 3]
        GenericCell[] step0 = {
            new GenericCell(5), new GenericCell(2), new GenericCell(8),
            new GenericCell(1), new GenericCell(3)
        };
        probe.recordSnapshot(0, step0, 0);
        
        // After step 1: [2, 5, 1, 3, 8] - swapped 5 and 2
        GenericCell[] step1 = {
            new GenericCell(2), new GenericCell(5), new GenericCell(1),
            new GenericCell(3), new GenericCell(8)
        };
        probe.recordSnapshot(1, step1, 1);
        
        // After step 2: [2, 1, 3, 5, 8] - more swaps
        GenericCell[] step2 = {
            new GenericCell(2), new GenericCell(1), new GenericCell(3),
            new GenericCell(5), new GenericCell(8)
        };
        probe.recordSnapshot(2, step2, 3);
        
        // After step 3: [1, 2, 3, 5, 8] - getting closer to sorted
        GenericCell[] step3 = {
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(5), new GenericCell(8)
        };
        probe.recordSnapshot(3, step3, 5);
        
        // After step 4: [1, 2, 3, 5, 8] - fully sorted
        GenericCell[] step4 = {
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(5), new GenericCell(8)
        };
        probe.recordSnapshot(4, step4, 5);
        
        // Step 2: Build trajectory from probe snapshots
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe,
            "Bubble",          // algotype
            0,                 // frozenCells
            0,                 // trialNumber
            5,                 // arraySize
            System.currentTimeMillis()  // timestamp
        );
        
        // Step 3: Export trajectory to CSV
        File csvFile = tempDir.resolve("experiment_001_trajectory.csv").toFile();
        TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
        
        // Step 4: Verify exported data
        System.out.println("Trajectory exported to: " + csvFile.getAbsolutePath());
        System.out.println("\nCSV Contents:");
        System.out.println("=".repeat(80));
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
        
        System.out.println("=".repeat(80));
        
        // Demonstrate how to access trajectory data programmatically
        System.out.println("\nTrajectory Analysis:");
        System.out.println("Total steps: " + trajectory.getStepCount());
        System.out.println("Algotype: " + trajectory.getMetadata().algotype());
        System.out.println("Array size: " + trajectory.getMetadata().arraySize());
        System.out.println("\nPer-step metrics:");
        
        for (ExperimentTrajectory.TrajectoryStep step : trajectory.getSteps()) {
            System.out.printf(
                "  Step %d: Sortedness=%.1f%%, MonotonicityError=%d, Swaps=%d%n",
                step.stepNumber(),
                step.sortedness(),
                step.monotonicityError(),
                step.cumulativeSwaps()
            );
        }
        
        // This demonstrates the complete workflow:
        // 1. Probe records snapshots during execution
        // 2. TrajectoryBuilder computes metrics from snapshots
        // 3. TrajectoryDataExporter writes CSV with metadata and trajectory data
        // 4. Exported CSV can be imported into Python/R for dashboard validation
    }
}
