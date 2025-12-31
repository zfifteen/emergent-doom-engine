package com.emergent.doom.examples;

import com.emergent.doom.cell.BubbleCell;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.NoSwapConvergence;
import com.emergent.doom.experiment.SortDirection;
import com.emergent.doom.export.TrajectoryDataExporter;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.visualization.PlotData;
import com.emergent.doom.visualization.TrajectoryAnalyzer;
import com.emergent.doom.visualization.TrajectoryPlotter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Demonstrates the visualization and export capabilities of the Emergent Doom Engine.
 * 
 * <p>This example shows how to:
 * <ul>
 *   <li>Run a sorting experiment with probe recording</li>
 *   <li>Analyze metric trajectories (sortedness, swap count)</li>
 *   <li>Generate plot-ready data structures</li>
 *   <li>Export data to CSV and JSON for external visualization</li>
 * </ul>
 * </p>
 * 
 * <p>The generated files can be visualized using Python/matplotlib, R, Excel,
 * or web-based charting libraries.</p>
 * 
 * @see TrajectoryAnalyzer
 * @see TrajectoryPlotter
 * @see TrajectoryDataExporter
 */
public class VisualizationDemo {
    
    static class TestCell extends BubbleCell<TestCell> {
        public TestCell(int value) {
            super(value);
        }
        
        @Override
        public int compareTo(TestCell other) {
            return Integer.compare(this.getValue(), other.getValue());
        }
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("=== Emergent Doom Engine: Visualization Demo ===\n");
        
        // Step 1: Set up the experiment
        System.out.println("Step 1: Creating cell array and initializing components...");
        int arraySize = 50;
        long seed = 42L;
        
        // Create shuffled array
        Random random = new Random(seed);
        TestCell[] cells = new TestCell[arraySize];
        for (int i = 0; i < arraySize; i++) {
            cells[i] = new TestCell(i + 1);
        }
        // Shuffle using Fisher-Yates
        for (int i = arraySize - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            TestCell temp = cells[i];
            cells[i] = cells[j];
            cells[j] = temp;
        }
        
        FrozenCellStatus frozen = new FrozenCellStatus();
        SwapEngine<TestCell> swapEngine = new SwapEngine<>(frozen);
        Probe<TestCell> probe = new Probe<>();
        probe.setRecordingEnabled(true);
        ConvergenceDetector<TestCell> detector = new NoSwapConvergence<>(10);
        
        // Step 2: Run the sorting experiment
        System.out.println("Step 2: Running sorting experiment with probe recording...");
        ExecutionEngine<TestCell> engine = new ExecutionEngine<>(
            cells,
            swapEngine,
            probe,
            detector
        );
        
        int steps = engine.runUntilConvergence(1000);
        
        System.out.println("  Experiment complete!");
        System.out.println("  Total steps: " + steps);
        System.out.println("  Total swaps: " + swapEngine.getSwapCount());
        System.out.println("  Snapshots recorded: " + probe.getSnapshotCount());
        System.out.println();
        
        // Step 3: Analyze trajectories
        System.out.println("Step 3: Analyzing metric trajectories...");
        TrajectoryAnalyzer<TestCell> analyzer = new TrajectoryAnalyzer<>();
        
        List<Double> sortednessTrajectory = analyzer.computeSortednessTrajectory(
            probe, 
            SortDirection.INCREASING
        );
        
        List<Integer> swapTrajectory = analyzer.computeSwapCountTrajectory(probe);
        
        System.out.println("  Sortedness trajectory computed: " + sortednessTrajectory.size() + " points");
        System.out.println("  Initial sortedness: " + String.format("%.2f%%", sortednessTrajectory.get(0)));
        System.out.println("  Final sortedness: " + String.format("%.2f%%", 
            sortednessTrajectory.get(sortednessTrajectory.size() - 1)));
        System.out.println();
        
        // Step 4: Generate plot data
        System.out.println("Step 4: Generating plot data structures...");
        TrajectoryPlotter plotter = new TrajectoryPlotter();
        
        PlotData sortednessPlot = plotter.generatePlotData(
            "Sortedness Value", 
            sortednessTrajectory
        );
        
        System.out.println("  Plot data generated:");
        System.out.println("    Metric: " + sortednessPlot.getMetricName());
        System.out.println("    Data points: " + sortednessPlot.getYValues().length);
        System.out.println("    Min value: " + String.format("%.2f", sortednessPlot.getMetadata().get("min")));
        System.out.println("    Max value: " + String.format("%.2f", sortednessPlot.getMetadata().get("max")));
        System.out.println("    Mean value: " + String.format("%.2f", sortednessPlot.getMetadata().get("mean")));
        System.out.println();
        
        // Step 5: Export data to files
        System.out.println("Step 5: Exporting data to files...");
        
        // Export trajectories to CSV
        Map<String, List<Double>> trajectories = new HashMap<>();
        trajectories.put("Sortedness", sortednessTrajectory);
        
        // Convert Integer list to Double list for export
        List<Double> swapTrajectoriesDouble = swapTrajectory.stream()
            .map(Integer::doubleValue)
            .collect(java.util.stream.Collectors.toList());
        trajectories.put("CumulativeSwaps", swapTrajectoriesDouble);
        
        String csvFile = "visualization_demo_trajectories.csv";
        TrajectoryDataExporter.exportToCSV(csvFile, trajectories);
        System.out.println("  ✓ CSV exported: " + csvFile);
        
        // Export trajectories to JSON
        String jsonFile = "visualization_demo_trajectories.json";
        TrajectoryDataExporter.exportToJSON(jsonFile, trajectories);
        System.out.println("  ✓ JSON exported: " + jsonFile);
        
        // Export raw snapshots
        String snapshotsFile = "visualization_demo_snapshots.json";
        TrajectoryDataExporter.exportSnapshotsToJSON(snapshotsFile, probe);
        System.out.println("  ✓ Snapshots exported: " + snapshotsFile);
        System.out.println();
        
        // Step 6: Show how to visualize in Python
        System.out.println("Step 6: Visualization instructions\n");
        System.out.println("To visualize the exported data in Python:");
        System.out.println("----------------------------------------");
        System.out.println("import pandas as pd");
        System.out.println("import matplotlib.pyplot as plt");
        System.out.println();
        System.out.println("# Load CSV data");
        System.out.println("df = pd.read_csv('" + csvFile + "')");
        System.out.println();
        System.out.println("# Plot sortedness trajectory");
        System.out.println("plt.figure(figsize=(10, 6))");
        System.out.println("plt.plot(df['step_number'], df['Sortedness'], label='Sortedness')");
        System.out.println("plt.xlabel('Execution Steps')");
        System.out.println("plt.ylabel('Sortedness (%)')");
        System.out.println("plt.title('Sortedness Trajectory - Bubble Sort')");
        System.out.println("plt.legend()");
        System.out.println("plt.grid(True)");
        System.out.println("plt.savefig('sortedness_trajectory.png')");
        System.out.println("plt.show()");
        System.out.println("----------------------------------------");
        System.out.println();
        
        System.out.println("=== Demo Complete! ===");
        System.out.println("Files generated:");
        System.out.println("  - " + csvFile + " (CSV format for Excel/R/Python)");
        System.out.println("  - " + jsonFile + " (JSON format for web visualization)");
        System.out.println("  - " + snapshotsFile + " (Complete execution history)");
    }
}

