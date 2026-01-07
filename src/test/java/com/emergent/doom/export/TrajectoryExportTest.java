package com.emergent.doom.export;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for trajectory export functionality.
 * 
 * <p>Validates CSV file creation, format, metadata headers, and data integrity.</p>
 * 
 * <p><strong>Test Coverage:</strong> 6 tests</p>
 * <ul>
 *   <li>CSV file creation</li>
 *   <li>Metadata header format</li>
 *   <li>Column headers</li>
 *   <li>Per-step data rows</li>
 *   <li>Aggregation column for chimeric experiments</li>
 *   <li>Parent directory creation</li>
 * </ul>
 */
@DisplayName("Trajectory Export Tests")
class TrajectoryExportTest {
    
    private Path testOutputDir;
    private Path testCsvFile;
    
    @BeforeEach
    void setUp() throws IOException {
        testOutputDir = Paths.get("target", "test-trajectory-exports");
        testCsvFile = testOutputDir.resolve("test_trajectory.csv");
        
        // Ensure clean test directory
        if (Files.exists(testOutputDir)) {
            Files.walk(testOutputDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }
    
    @AfterEach
    void tearDown() throws IOException {
        // Clean up test files
        if (Files.exists(testOutputDir)) {
            Files.walk(testOutputDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }
    
    /**
     * PURPOSE: Verify CSV file is created at specified path.
     * 
     * INPUTS: Valid trajectory and file path
     * EXPECTED OUTPUT: File exists after export
     * TEST DATA: Simple 2-step trajectory
     */
    @Test
    @DisplayName("creates CSV file at specified path")
    void createsCSVFileAtSpecifiedPath() throws IOException {
        // Arrange
        ExperimentTrajectory trajectory = createSimpleTrajectory(2);
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(testCsvFile.toString(), trajectory);
        
        // Assert
        assertTrue(Files.exists(testCsvFile), "CSV file should be created");
        assertTrue(Files.size(testCsvFile) > 0, "CSV file should have content");
    }
    
    /**
     * PURPOSE: Verify CSV contains correct metadata header.
     * 
     * INPUTS: Trajectory with known metadata values
     * EXPECTED OUTPUT: CSV starts with metadata section
     * TEST DATA: "Bubble", 2 frozen, trial 5, size 30
     * REPRODUCTION: Fixed seed not needed (metadata only)
     */
    @Test
    @DisplayName("exports metadata header correctly")
    void exportsMetadataHeaderCorrectly() throws IOException {
        // Arrange
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Bubble",
            2,
            5,
            30,
            1704675000000L,
            createSteps(3, false)
        );
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(testCsvFile.toString(), trajectory);
        
        // Assert
        List<String> lines = Files.readAllLines(testCsvFile);
        assertTrue(lines.size() >= 7, "Should have metadata + header + data");
        
        // Check metadata section
        assertEquals("# Metadata", lines.get(0));
        assertEquals("algotype,Bubble", lines.get(1));
        assertEquals("frozen_cells,2", lines.get(2));
        assertEquals("trial_number,5", lines.get(3));
        assertEquals("array_size,30", lines.get(4));
        assertTrue(lines.get(5).startsWith("timestamp,"), "Timestamp line should exist");
    }
    
    /**
     * PURPOSE: Verify CSV contains correct column headers.
     * 
     * INPUTS: Non-chimeric trajectory
     * EXPECTED OUTPUT: Header row with step_number, sortedness, monotonicity_error, swaps, comparisons
     * TEST DATA: Simple trajectory without aggregation
     */
    @Test
    @DisplayName("exports column headers correctly")
    void exportsColumnHeadersCorrectly() throws IOException {
        // Arrange
        ExperimentTrajectory trajectory = createSimpleTrajectory(2);
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(testCsvFile.toString(), trajectory);
        
        // Assert
        List<String> lines = Files.readAllLines(testCsvFile);
        
        // Find trajectory data header (after metadata)
        int headerIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).equals("# Trajectory Data")) {
                headerIndex = i + 1;
                break;
            }
        }
        
        assertTrue(headerIndex > 0, "Should find trajectory data header");
        String columnHeader = lines.get(headerIndex);
        assertEquals("step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons",
            columnHeader, "Column headers should match expected format");
    }
    
    /**
     * PURPOSE: Verify CSV contains correct per-step data.
     * 
     * INPUTS: Trajectory with 3 known steps
     * EXPECTED OUTPUT: Data rows match trajectory step values
     * TEST DATA: 3 steps with specific metric values
     */
    @Test
    @DisplayName("exports per-step data correctly")
    void exportsPerStepDataCorrectly() throws IOException {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 10.0, 9, null, 0, 10));
        steps.add(new ExperimentTrajectory.TrajectoryStep(1, 30.0, 7, null, 5, 25));
        steps.add(new ExperimentTrajectory.TrajectoryStep(2, 50.0, 5, null, 12, 42));
        
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Selection", 0, 0, 10, System.currentTimeMillis(), steps
        );
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(testCsvFile.toString(), trajectory);
        
        // Assert
        List<String> lines = Files.readAllLines(testCsvFile);
        
        // Find first data row (after column headers)
        int dataStartIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("step_number,")) {
                dataStartIndex = i + 1;
                break;
            }
        }
        
        assertTrue(dataStartIndex > 0, "Should find data start");
        
        // Verify data rows
        assertEquals("0,10.0,9,0,10", lines.get(dataStartIndex));
        assertEquals("1,30.0,7,5,25", lines.get(dataStartIndex + 1));
        assertEquals("2,50.0,5,12,42", lines.get(dataStartIndex + 2));
    }
    
    /**
     * PURPOSE: Verify CSV includes aggregation column for chimeric experiments.
     * 
     * INPUTS: Trajectory with aggregation data
     * EXPECTED OUTPUT: Column headers include "aggregation", data rows include values
     * TEST DATA: 2 steps with aggregation percentages
     */
    @Test
    @DisplayName("includes aggregation column for chimeric experiments")
    void includesAggregationColumnForChimericExperiments() throws IOException {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(new ExperimentTrajectory.TrajectoryStep(0, 10.0, 9, 75.0, 0, 10));
        steps.add(new ExperimentTrajectory.TrajectoryStep(1, 20.0, 7, 72.5, 5, 25));
        
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Chimeric", 0, 0, 10, System.currentTimeMillis(), steps
        );
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(testCsvFile.toString(), trajectory);
        
        // Assert
        List<String> lines = Files.readAllLines(testCsvFile);
        
        // Find column header
        String columnHeader = null;
        int dataStartIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("step_number,")) {
                columnHeader = lines.get(i);
                dataStartIndex = i + 1;
                break;
            }
        }
        
        assertNotNull(columnHeader, "Should find column header");
        assertTrue(columnHeader.contains("aggregation"), "Should include aggregation column");
        
        // Verify data includes aggregation values
        assertTrue(lines.get(dataStartIndex).endsWith(",75.0"), "First row should include aggregation");
        assertTrue(lines.get(dataStartIndex + 1).endsWith(",72.5"), "Second row should include aggregation");
    }
    
    /**
     * PURPOSE: Verify parent directories are created automatically.
     * 
     * INPUTS: File path with non-existent parent directories
     * EXPECTED OUTPUT: Directories are created, file is written
     * TEST DATA: Nested directory path
     */
    @Test
    @DisplayName("creates parent directories automatically")
    void createsParentDirectoriesAutomatically() throws IOException {
        // Arrange
        Path nestedPath = testOutputDir.resolve("nested").resolve("path").resolve("trajectory.csv");
        ExperimentTrajectory trajectory = createSimpleTrajectory(2);
        
        // Ensure parent doesn't exist
        assertFalse(Files.exists(nestedPath.getParent()), "Parent should not exist initially");
        
        // Act
        TrajectoryDataExporter.exportTrajectoryToCSV(nestedPath.toString(), trajectory);
        
        // Assert
        assertTrue(Files.exists(nestedPath), "File should be created");
        assertTrue(Files.exists(nestedPath.getParent()), "Parent directories should be created");
    }
    
    // ============ Helper Methods ============
    
    /**
     * Creates a simple trajectory for testing.
     */
    private ExperimentTrajectory createSimpleTrajectory(int stepCount) {
        return new ExperimentTrajectory(
            "Bubble",
            0,
            0,
            10,
            System.currentTimeMillis(),
            createSteps(stepCount, false)
        );
    }
    
    /**
     * Creates a list of trajectory steps.
     * 
     * @param count Number of steps to create
     * @param withAggregation Whether to include aggregation data
     */
    private List<ExperimentTrajectory.TrajectoryStep> createSteps(int count, boolean withAggregation) {
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sortedness = 10.0 + (i * 15.0);
            int monotonicityError = Math.max(0, 9 - (i * 2));
            int cumulativeSwaps = i * 5;
            int cumulativeComparisons = i * 20 + 10;
            Double aggregation = withAggregation ? (75.0 - i * 2.5) : null;
            
            steps.add(new ExperimentTrajectory.TrajectoryStep(
                i,
                sortedness,
                monotonicityError,
                aggregation,
                cumulativeSwaps,
                cumulativeComparisons
            ));
        }
        return steps;
    }
}
