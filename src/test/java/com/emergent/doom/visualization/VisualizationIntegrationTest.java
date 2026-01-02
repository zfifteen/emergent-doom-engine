package com.emergent.doom.visualization;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.analysis.TrajectoryAnalyzer;
import com.emergent.doom.experiment.SortDirection;
import com.emergent.doom.export.TrajectoryDataExporter;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the visualization and export components.
 * 
 * Tests the complete workflow:
 * 1. Create probe with snapshots
 * 2. Analyze trajectories using TrajectoryAnalyzer
 * 3. Generate plot data using TrajectoryPlotter
 * 4. Export data using TrajectoryDataExporter
 */
class VisualizationIntegrationTest {
    
    @Test
    void testCompleteVisualizationWorkflow(@TempDir Path tempDir) throws IOException {
        // Step 1: Create a probe with some test data
        Probe<GenericCell> probe = new Probe<>();
        
        // Simulate a sorting process with improving sortedness
        GenericCell[] cells1 = {
            new GenericCell(5, Algotype.BUBBLE), 
            new GenericCell(3, Algotype.BUBBLE), 
            new GenericCell(1, Algotype.BUBBLE), 
            new GenericCell(4, Algotype.BUBBLE), 
            new GenericCell(2, Algotype.BUBBLE)
        };
        GenericCell[] cells2 = {
            new GenericCell(3, Algotype.BUBBLE), 
            new GenericCell(1, Algotype.BUBBLE), 
            new GenericCell(5, Algotype.BUBBLE), 
            new GenericCell(2, Algotype.BUBBLE), 
            new GenericCell(4, Algotype.BUBBLE)
        };
        GenericCell[] cells3 = {
            new GenericCell(1, Algotype.BUBBLE), 
            new GenericCell(2, Algotype.BUBBLE), 
            new GenericCell(3, Algotype.BUBBLE), 
            new GenericCell(4, Algotype.BUBBLE), 
            new GenericCell(5, Algotype.BUBBLE)
        };
        
        probe.recordSnapshot(0, cells1, 0);
        probe.recordSnapshot(1, cells2, 1);
        probe.recordSnapshot(2, cells3, 2);
        
        // Step 2: Analyze trajectories
        TrajectoryAnalyzer<GenericCell> analyzer = new TrajectoryAnalyzer<>();
        
        List<Double> sortednessTrajectory = analyzer.computeSortednessTrajectory(probe, SortDirection.INCREASING);
        List<Integer> swapTrajectory = analyzer.computeSwapCountTrajectory(probe);
        
        // Verify trajectory analysis
        assertNotNull(sortednessTrajectory);
        assertEquals(3, sortednessTrajectory.size());
        assertTrue(sortednessTrajectory.get(0) < sortednessTrajectory.get(2), 
                   "Sortedness should improve over time");
        assertEquals(100.0, sortednessTrajectory.get(2), 0.01, 
                     "Final sortedness should be 100%");
        
        // Verify swap counts
        assertEquals(0, swapTrajectory.get(0));
        assertEquals(1, swapTrajectory.get(1));
        assertEquals(2, swapTrajectory.get(2));
        
        // Step 3: Generate plot data
        TrajectoryPlotter plotter = new TrajectoryPlotter();
        PlotData sortednessPlot = plotter.generatePlotData("Sortedness Value", sortednessTrajectory);
        
        assertNotNull(sortednessPlot);
        assertEquals("Sortedness Value", sortednessPlot.getMetricName());
        assertEquals(3, sortednessPlot.getXValues().length);
        assertEquals(3, sortednessPlot.getYValues().length);
        
        // Step 4: Export data to CSV
        Map<String, List<Double>> trajectories = new HashMap<>();
        trajectories.put("Sortedness", sortednessTrajectory);
        
        // Convert Integer list to Double list for export
        List<Double> swapTrajectoriesDouble = swapTrajectory.stream()
                .map(Integer::doubleValue)
                .collect(java.util.stream.Collectors.toList());
        trajectories.put("SwapCount", swapTrajectoriesDouble);
        
        Path csvFile = tempDir.resolve("trajectories.csv");
        TrajectoryDataExporter.exportToCSV(csvFile.toString(), trajectories);
        
        // Verify CSV file was created
        assertTrue(Files.exists(csvFile), "CSV file should be created");
        List<String> lines = Files.readAllLines(csvFile);
        assertTrue(lines.size() > 0, "CSV should have content");
        assertTrue(lines.get(0).contains("step_number"), "CSV should have header");
        
        // Step 5: Export data to JSON
        Path jsonFile = tempDir.resolve("trajectories.json");
        TrajectoryDataExporter.exportToJSON(jsonFile.toString(), trajectories);
        
        // Verify JSON file was created
        assertTrue(Files.exists(jsonFile), "JSON file should be created");
        String jsonContent = Files.readString(jsonFile);
        assertTrue(jsonContent.contains("\"trajectories\""), "JSON should have trajectories key");
        assertTrue(jsonContent.contains("\"Sortedness\""), "JSON should contain Sortedness data");
    }
    
    @Test
    void testMultiSeriesPlotData() {
        // Create multiple trajectories for comparison
        List<Double> bubbleSort = List.of(20.0, 40.0, 60.0, 80.0, 100.0);
        List<Double> selectionSort = List.of(15.0, 35.0, 55.0, 75.0, 100.0);
        
        Map<String, List<Double>> trajectories = new HashMap<>();
        trajectories.put("Bubble Sort", bubbleSort);
        trajectories.put("Selection Sort", selectionSort);
        
        TrajectoryPlotter plotter = new TrajectoryPlotter();
        MultiSeriesPlotData plotData = plotter.generateMultiSeriesPlotData(trajectories);
        
        assertNotNull(plotData);
        assertEquals(2, plotData.getSeriesCount());
        assertNotNull(plotData.getSeriesByName("Bubble Sort"));
        assertNotNull(plotData.getSeriesByName("Selection Sort"));
        
        // Verify data for each series
        PlotData bubbleData = plotData.getSeriesByName("Bubble Sort");
        assertNotNull(bubbleData);
        assertEquals(5, bubbleData.getYValues().length);
    }
    
    @Test
    void testSnapshotExport(@TempDir Path tempDir) throws IOException {
        // Create a probe with snapshots
        Probe<GenericCell> probe = new BasicProbe<>();
        GenericCell[] cells = {
            new GenericCell(3, Algotype.BUBBLE), 
            new GenericCell(1, Algotype.BUBBLE), 
            new GenericCell(2, Algotype.BUBBLE)
        };
        probe.recordSnapshot(0, cells, 0);
        
        // Export snapshots to CSV
        Path csvFile = tempDir.resolve("snapshots.csv");
        TrajectoryDataExporter.exportSnapshotsToCSV(csvFile.toString(), probe);
        
        assertTrue(Files.exists(csvFile));
        List<String> lines = Files.readAllLines(csvFile);
        assertTrue(lines.size() >= 2, "Should have header and at least one data row");
        
        // Export snapshots to JSON
        Path jsonFile = tempDir.resolve("snapshots.json");
        // TrajectoryDataExporter.exportSnapshotsToJSON(jsonFile.toString(), probe);
        
        // assertTrue(Files.exists(jsonFile));
        // String jsonContent = Files.readString(jsonFile);
        // assertTrue(jsonContent.contains("\"snapshots\""));
    }
}
