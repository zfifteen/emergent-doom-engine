package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for trajectory building and exporting.
 * 
 * <p>Tests the complete pipeline: Probe snapshots → TrajectoryBuilder → CSV export.</p>
 */
class TrajectoryExportTest {

    @TempDir
    Path tempDir;

    /**
     * PURPOSE: As a developer, I want to build a trajectory from probe snapshots
     * so that I can analyze execution dynamics.
     * 
     * INPUTS: Probe with 3 snapshots
     * EXPECTED OUTPUT: Trajectory with 3 steps
     * TEST DATA: Small array with progressing sort states
     * REPRODUCTION: Record snapshots, build trajectory, verify step count
     */
    @Test
    @DisplayName("Build trajectory from probe snapshots")
    void buildTrajectoryFromProbeSnapshots() {
        // Create probe and record snapshots
        Probe<GenericCell> probe = new Probe<>();
        
        // Initial state: [5, 2, 8, 1, 3]
        GenericCell[] cells1 = {
            new GenericCell(5), new GenericCell(2), new GenericCell(8),
            new GenericCell(1), new GenericCell(3)
        };
        probe.recordSnapshot(0, cells1, 0);
        
        // After one step: [2, 5, 1, 3, 8] - one swap
        GenericCell[] cells2 = {
            new GenericCell(2), new GenericCell(5), new GenericCell(1),
            new GenericCell(3), new GenericCell(8)
        };
        probe.recordSnapshot(1, cells2, 1);
        
        // After two steps: [2, 1, 3, 5, 8] - three swaps total
        GenericCell[] cells3 = {
            new GenericCell(2), new GenericCell(1), new GenericCell(3),
            new GenericCell(5), new GenericCell(8)
        };
        probe.recordSnapshot(2, cells3, 3);
        
        // Build trajectory
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Bubble", 0, 0, 5, System.currentTimeMillis()
        );
        
        // Verify
        assertEquals(3, trajectory.getStepCount());
        assertEquals("Bubble", trajectory.getMetadata().algotype());
        assertEquals(0, trajectory.getMetadata().frozenCells());
        assertEquals(5, trajectory.getMetadata().arraySize());
    }
    
    /**
     * PURPOSE: As a developer, I want trajectory steps to have correct metric values
     * so that analysis is accurate.
     * 
     * INPUTS: Probe with snapshots of known sortedness
     * EXPECTED OUTPUT: Trajectory with accurate metric calculations
     * TEST DATA: Progressing from 20% to 100% sorted
     * REPRODUCTION: Build trajectory, check sortedness progression
     */
    @Test
    @DisplayName("Trajectory steps contain correct metric values")
    void trajectoryStepsContainCorrectMetrics() {
        Probe<GenericCell> probe = new Probe<>();
        
        // Step 0: Mostly unsorted [5,4,3,2,1] - 20% sorted
        GenericCell[] cells1 = {
            new GenericCell(5), new GenericCell(4), new GenericCell(3),
            new GenericCell(2), new GenericCell(1)
        };
        probe.recordSnapshot(0, cells1, 0);
        
        // Step 1: Perfectly sorted [1,2,3,4,5] - 100% sorted
        GenericCell[] cells2 = {
            new GenericCell(1), new GenericCell(2), new GenericCell(3),
            new GenericCell(4), new GenericCell(5)
        };
        probe.recordSnapshot(1, cells2, 10);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 0, 5, System.currentTimeMillis()
        );
        
        List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
        
        // Check first step (reversed array)
        ExperimentTrajectory.TrajectoryStep step0 = steps.get(0);
        assertEquals(0, step0.stepNumber());
        assertEquals(20.0, step0.sortedness(), 0.1); // First element always counts
        assertEquals(4, step0.monotonicityError()); // 4 inversions
        assertEquals(0, step0.cumulativeSwaps());
        
        // Check second step (sorted array)
        ExperimentTrajectory.TrajectoryStep step1 = steps.get(1);
        assertEquals(1, step1.stepNumber());
        assertEquals(100.0, step1.sortedness(), 0.1);
        assertEquals(0, step1.monotonicityError()); // No inversions
        assertEquals(10, step1.cumulativeSwaps());
    }
    
    /**
     * PURPOSE: As a developer, I want to export trajectory to CSV
     * so that I can analyze it with external tools.
     * 
     * INPUTS: Trajectory with metadata and steps
     * EXPECTED OUTPUT: CSV file with metadata header and step data
     * TEST DATA: 2-step trajectory
     * REPRODUCTION: Export to CSV, read file, verify structure
     */
    @Test
    @DisplayName("Export trajectory to CSV file")
    void exportTrajectoryToCsvFile() throws IOException {
        // Create simple trajectory
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
        steps.add(new ExperimentTrajectory.TrajectoryStep(1, 75.0, 5, null, 3, 100));
        
        ExperimentTrajectory.ExperimentMetadata metadata = 
            new ExperimentTrajectory.ExperimentMetadata(
                "Bubble", 2, 0, 50, 1704672000000L
            );
        
        ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
        
        // Export to CSV
        File csvFile = tempDir.resolve("test_trajectory.csv").toFile();
        TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
        
        // Verify file exists
        assertTrue(csvFile.exists());
        
        // Read and verify content
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        // Verify structure
        assertTrue(lines.size() >= 10); // Metadata (6) + header (2) + data (2) = 10
        assertEquals("# Metadata", lines.get(0));
        assertEquals("algotype,Bubble", lines.get(1));
        assertEquals("frozen_cells,2", lines.get(2));
        assertEquals("trial_number,0", lines.get(3));
        assertEquals("array_size,50", lines.get(4));
        assertEquals("timestamp,1704672000000", lines.get(5));
        assertEquals("# Trajectory Data", lines.get(6));
        assertEquals("step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons", 
                     lines.get(7));
        
        // Verify first data row
        String[] row1 = lines.get(8).split(",");
        assertEquals("0", row1[0]); // stepNumber
        assertEquals("50.0", row1[1]); // sortedness
        assertEquals("10", row1[2]); // monotonicityError
        assertEquals("0", row1[3]); // cumulativeSwaps
        assertEquals("50", row1[4]); // cumulativeComparisons
    }
    
    /**
     * PURPOSE: As a developer, I want chimeric trajectories to include aggregation
     * so that I can analyze clustering dynamics.
     * 
     * INPUTS: Trajectory with aggregation data
     * EXPECTED OUTPUT: CSV with aggregation column
     * TEST DATA: Chimeric trajectory with 80% aggregation
     * REPRODUCTION: Export chimeric trajectory, verify aggregation column present
     */
    @Test
    @DisplayName("Export chimeric trajectory includes aggregation column")
    void exportChimericTrajectoryIncludesAggregation() throws IOException {
        // Create chimeric trajectory
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, 75.0, 0, 50));
        steps.add(new ExperimentTrajectory.TrajectoryStep(1, 75.0, 5, 80.0, 3, 100));
        
        ExperimentTrajectory.ExperimentMetadata metadata = 
            new ExperimentTrajectory.ExperimentMetadata(
                "Chimeric", 0, 0, 50, System.currentTimeMillis()
            );
        
        ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
        
        // Export to CSV
        File csvFile = tempDir.resolve("chimeric_trajectory.csv").toFile();
        TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
        
        // Read and verify
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        // Verify header includes aggregation
        String header = lines.get(7);
        assertTrue(header.contains("aggregation"), 
                  "Header should include aggregation column for chimeric trajectories");
        assertEquals("step_number,sortedness,monotonicity_error,aggregation,cumulative_swaps,cumulative_comparisons", 
                     header);
        
        // Verify data includes aggregation values
        String[] row1 = lines.get(8).split(",");
        assertEquals("75.0", row1[3]); // aggregation for step 0
        
        String[] row2 = lines.get(9).split(",");
        assertEquals("80.0", row2[3]); // aggregation for step 1
    }
    
    /**
     * PURPOSE: As a developer, I want trajectory export to create parent directories
     * so that I don't have to manage directory structure manually.
     * 
     * INPUTS: File path with non-existent parent directories
     * EXPECTED OUTPUT: Directories created automatically, CSV exported successfully
     * TEST DATA: Nested directory path
     * REPRODUCTION: Export to nested path, verify file exists
     */
    @Test
    @DisplayName("Trajectory export creates parent directories")
    void trajectoryExportCreatesParentDirectories() throws IOException {
        // Create trajectory
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
        
        ExperimentTrajectory.ExperimentMetadata metadata = 
            new ExperimentTrajectory.ExperimentMetadata(
                "Test", 0, 0, 50, System.currentTimeMillis()
            );
        
        ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
        
        // Export to nested path
        File csvFile = tempDir.resolve("data/experiments/run001/trajectory.csv").toFile();
        TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
        
        // Verify file and directories exist
        assertTrue(csvFile.exists());
        assertTrue(csvFile.getParentFile().exists());
    }
    
    /**
     * PURPOSE: As a developer, I want trajectory export to handle empty metadata gracefully
     * so that edge cases are handled properly.
     * 
     * INPUTS: Trajectory with invalid parameters
     * EXPECTED OUTPUT: IllegalArgumentException
     * TEST DATA: Null filepath, null trajectory
     * REPRODUCTION: Attempt export with invalid inputs
     */
    @Test
    @DisplayName("Trajectory export validates inputs")
    void trajectoryExportValidatesInputs() {
        ExperimentTrajectory.ExperimentMetadata metadata = 
            new ExperimentTrajectory.ExperimentMetadata(
                "Test", 0, 0, 50, System.currentTimeMillis()
            );
        
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
        ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
        
        // Null filepath
        assertThrows(IllegalArgumentException.class, () -> {
            TrajectoryDataExporter.exportTrajectoryToCSV(null, trajectory);
        });
        
        // Null trajectory
        assertThrows(IllegalArgumentException.class, () -> {
            TrajectoryDataExporter.exportTrajectoryToCSV(
                tempDir.resolve("test.csv").toString(), null
            );
        });
    }
}
