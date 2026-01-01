package com.emergent.doom.export;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Exports trajectory data to files in CSV and JSON formats.
 *
 * <p>This class provides data export capabilities matching the Python cell_research
 * implementation's use of .npy files. Since Java doesn't have native .npy support,
 * this implementation uses CSV and JSON formats which are universally compatible
 * with visualization tools (matplotlib, R, Excel, web-based charting, etc.).</p>
 *
 * <p><strong>Purpose in Emergent Doom Engine:</strong></p>
 * <ul>
 *   <li>Export metric trajectories for external analysis and plotting</li>
 *   <li>Export raw probe snapshots for reproducibility and archival</li>
 *   <li>Enable integration with Python/R analysis pipelines</li>
 *   <li>Support data sharing and publication requirements</li>
 * </ul>
 *
 * <p><strong>Reference:</strong> Levin et al. (2024), p.6: "information collected by
 * the Probe is stored as a .npy file". This class provides equivalent functionality
 * using CSV/JSON for broader compatibility.</p>
 *
 * <p><strong>Design:</strong> All export methods are static and handle I/O exceptions
 * gracefully. File formats are designed for easy import into common analysis tools.</p>
 *
 * @see Probe
 * @see StepSnapshot
 */
public class TrajectoryDataExporter {
    
    /**
     * PURPOSE: Export multiple metric trajectories to a CSV file.
     * 
     * INPUTS:
     *   - filepath: Path where CSV file should be written
     *   - trajectories: Map of metric names to their value trajectories
     * 
     * PROCESS:
     *   1. Validate inputs (filepath not null, trajectories not null/empty)
     *   2. Validate that all trajectories have same length (shared step numbers)
     *   3. Create parent directories if they don't exist
     *   4. Open file writer with UTF-8 encoding
     *   5. Write header row: "step_number, metric1_name, metric2_name, ..."
     *   6. For each step (0 to trajectory.length-1):
     *      a. Write step number
     *      b. Write value for each metric at that step
     *      c. Separate values with commas
     *      d. End row with newline
     *   7. Flush and close file writer
     *   8. Handle IOExceptions gracefully (wrap or log)
     * 
     * OUTPUTS: void (side effect: creates CSV file)
     * 
     * THROWS: IOException if file operations fail
     * 
     * CSV FORMAT EXAMPLE:
     *   step_number,Sortedness Value,Aggregation Index
     *   0,45.2,33.3
     *   1,52.1,41.7
     *   2,68.7,58.9
     *   ...
     * 
     * USAGE:
     *   - Export sortedness and aggregation trajectories together
     *   - Import into Excel, R, or Python for custom analysis
     *   - Archive experimental results
     * 
     * DESIGN RATIONALE:
     *   - CSV is universally supported by all data analysis tools
     *   - Header row enables self-documenting data files
     *   - Comma separation is standard and widely compatible
     *   - UTF-8 encoding ensures international character support
     * 
     * IMPLEMENTATION NOTES:
     *   This is the MAIN ENTRY POINT for data export. It orchestrates the complete
     *   CSV export workflow by:
     *   - Validating inputs via validateTrajectoriesHaveSameLength()
     *   - Ensuring directory exists via ensureDirectoryExists()
     *   - Writing CSV header and data rows
     *   - Handling I/O operations with proper resource management
     *   
     *   This method triggers the file export pipeline and coordinates all helper methods.
     */
    public static void exportToCSV(String filepath, Map<String, List<Double>> trajectories) 
            throws IOException {
        // Step 1: Validate filepath
        if (filepath == null || filepath.trim().isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
        // Step 2: Validate trajectories map
        if (trajectories == null || trajectories.isEmpty()) {
            throw new IllegalArgumentException("Trajectories cannot be null or empty");
        }
        
        // Step 3: Validate all trajectories have same length
        validateTrajectoriesHaveSameLength(trajectories);
        
        // Step 4: Ensure parent directories exist
        ensureDirectoryExists(filepath);
        
        // Step 5: Open file writer with UTF-8 encoding and write CSV
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filepath), 
                    java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Step 6: Write header row
            writer.write("step_number");
            for (String metricName : trajectories.keySet()) {
                writer.write(",");
                writer.write(escapeCsvValue(metricName));
            }
            writer.newLine();
            
            // Step 7: Get trajectory length (all same length after validation)
            int trajectoryLength = trajectories.values().iterator().next().size();
            
            // Step 8: Write data rows
            for (int step = 0; step < trajectoryLength; step++) {
                writer.write(String.valueOf(step));
                
                for (List<Double> trajectory : trajectories.values()) {
                    writer.write(",");
                    writer.write(String.valueOf(trajectory.get(step)));
                }
                
                writer.newLine();
            }
            
            // Step 9: Flush (implicit with try-with-resources close)
        }
        // File writer is automatically closed by try-with-resources
    }
    
    /**
     * PURPOSE: Export multiple metric trajectories to a JSON file.
     * 
     * INPUTS:
     *   - filepath: Path where JSON file should be written
     *   - trajectories: Map of metric names to their value trajectories
     * 
     * PROCESS:
     *   1. Validate inputs (filepath not null, trajectories not null/empty)
     *   2. Create parent directories if they don't exist
     *   3. Open file writer with UTF-8 encoding
     *   4. Write JSON opening: {"trajectories": {
     *   5. For each metric in trajectories map:
     *      a. Write metric name as JSON key
     *      b. Write trajectory values as JSON array
     *      c. Add comma separator between metrics (except last)
     *   6. Write JSON closing: }}
     *   7. Flush and close file writer
     *   8. Handle IOExceptions gracefully
     * 
     * OUTPUTS: void (side effect: creates JSON file)
     * 
     * THROWS: IOException if file operations fail
     * 
     * JSON FORMAT EXAMPLE:
     *   {
     *     "trajectories": {
     *       "Sortedness Value": [45.2, 52.1, 68.7, ...],
     *       "Aggregation Index": [33.3, 41.7, 58.9, ...]
     *     }
     *   }
     * 
     * USAGE:
     *   - Export for web-based visualization (D3.js, Chart.js)
     *   - Import into JavaScript/Python for processing
     *   - Structured format for programmatic access
     * 
     * DESIGN RATIONALE:
     *   - JSON is standard for web-based tools and APIs
     *   - No external JSON library needed for simple structure
     *   - Compact format compared to CSV for large datasets
     *   - Preserves data types (numbers vs strings)
     * 
     * IMPLEMENTATION:
     *   This section exports trajectories to JSON format.
     *   It integrates with the export system by following the same validation
     *   and directory creation pattern as exportToCSV. Uses simple string
     *   concatenation to build JSON (no external library needed for basic structure).
     */
    public static void exportToJSON(String filepath, Map<String, List<Double>> trajectories) 
            throws IOException {
        // Step 1: Validate filepath
        if (filepath == null || filepath.trim().isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
        // Step 2: Validate trajectories map
        if (trajectories == null || trajectories.isEmpty()) {
            throw new IllegalArgumentException("Trajectories cannot be null or empty");
        }
        
        // Step 3: Validate all trajectories have same length
        validateTrajectoriesHaveSameLength(trajectories);
        
        // Step 4: Ensure parent directories exist
        ensureDirectoryExists(filepath);
        
        // Step 5: Open file writer and write JSON
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filepath), 
                    java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Step 6: Write JSON opening
            writer.write("{\n");
            writer.write("  \"trajectories\": {\n");
            
            // Step 7: Write each metric trajectory
            int metricCount = 0;
            int totalMetrics = trajectories.size();
            
            for (Map.Entry<String, List<Double>> entry : trajectories.entrySet()) {
                String metricName = entry.getKey();
                List<Double> trajectory = entry.getValue();
                
                // Write metric name as JSON key
                writer.write("    \"");
                writer.write(metricName.replace("\"", "\\\"")); // Escape quotes
                writer.write("\": [");
                
                // Write trajectory values
                for (int i = 0; i < trajectory.size(); i++) {
                    writer.write(String.valueOf(trajectory.get(i)));
                    if (i < trajectory.size() - 1) {
                        writer.write(", ");
                    }
                }
                
                writer.write("]");
                
                // Add comma unless last metric
                metricCount++;
                if (metricCount < totalMetrics) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            // Step 8: Write JSON closing
            writer.write("  }\n");
            writer.write("}\n");
        }
    }
    
    /**
     * PURPOSE: Export raw probe snapshots to CSV file.
     * 
     * INPUTS:
     *   - filepath: Path where CSV file should be written
     *   - probe: Probe containing snapshot history
     * 
     * PROCESS:
     *   1. Validate inputs (filepath not null, probe not null/empty)
     *   2. Create parent directories if they don't exist
     *   3. Open file writer with UTF-8 encoding
     *   4. Write header row: "step_number, swap_count, cell_0_value, cell_1_value, ..."
     *   5. For each snapshot in probe:
     *      a. Write step number
     *      b. Write swap count
     *      c. Extract cell array from snapshot
     *      d. For each cell: write its comparable value
     *      e. Separate values with commas
     *      f. End row with newline
     *   6. Flush and close file writer
     *   7. Handle IOExceptions gracefully
     * 
     * OUTPUTS: void (side effect: creates CSV file)
     * 
     * THROWS: IOException if file operations fail
     * 
     * CSV FORMAT EXAMPLE:
     *   step_number,swap_count,cell_0,cell_1,cell_2,cell_3,cell_4
     *   0,0,5,2,8,1,3
     *   1,1,2,5,8,1,3
     *   2,2,2,5,1,8,3
     *   ...
     * 
     * USAGE:
     *   - Export complete execution history for reproducibility
     *   - Analyze cell-level behavior over time
     *   - Debug sorting algorithms by examining state transitions
     * 
     * DESIGN RATIONALE:
     *   - Captures complete state evolution (not just metrics)
     *   - Enables reconstruction of execution for visualization
     *   - swap_count column tracks algorithm activity
     *   - Each row is a complete snapshot for easy analysis
     * 
     * NOTE: Cell values are converted to strings using toString().
     *       For numeric cells, this should produce the underlying value.
     * 
     * IMPLEMENTATION:
     *   This section exports raw snapshot data to CSV format.
     *   It integrates with the export system by following the established pattern.
     *   Writes complete cell state at each step for detailed analysis.
     */
    public static <T extends Cell<T>> void exportSnapshotsToCSV(String filepath, Probe<T> probe) 
            throws IOException {
        // Step 1: Validate filepath
        if (filepath == null || filepath.trim().isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
        // Step 2: Validate probe
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 3: Ensure parent directories exist
        ensureDirectoryExists(filepath);
        
        // Step 4: Get snapshots and determine array size from first snapshot\n        List<StepSnapshot<T>> snapshots = probe.getSnapshots();\n        \n        // Validate chronological order and consistent array size\n        if (snapshots.size() > 1) {\n            for (int i = 1; i < snapshots.size(); i++) {\n                StepSnapshot<T> previous = snapshots.get(i - 1);\n                StepSnapshot<T> current = snapshots.get(i);\n                if (current.getTimestamp() < previous.getTimestamp()) {\n                    throw new IllegalArgumentException(\n                        \"Snapshots are not in chronological order: \" +\n                        \"snapshot index \" + (i - 1) + \" (timestamp=\" + previous.getTimestamp() + \") \" +\n                        \"comes after snapshot index \" + i + \" (timestamp=\" + current.getTimestamp() + \")\"\n                    );\n                }\n            }\n        }\n        if (!snapshots.isEmpty()) {\n            int expectedSize = snapshots.get(0).getCellStates().length;\n            for (int idx = 0; idx < snapshots.size(); idx++) {\n                StepSnapshot<T> s = snapshots.get(idx);\n                int actualSize = s.getCellStates().length;\n                if (actualSize != expectedSize) {\n                    throw new IllegalArgumentException(\n                        \"Inconsistent array size across snapshots: \" +\n                        \"snapshotIndex=\" + idx + \", \" +\n                        \"stepNumber=\" + s.getStepNumber() + \", \" +\n                        \"expectedLength=\" + expectedSize + \", \" +\n                        \"actualLength=\" + actualSize\n                    );\n                }\n            }\n            int arraySize = expectedSize;\n        } else {\n            int arraySize = 0;\n        }\n        
        // Step 5: Open file writer and write CSV
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filepath), 
                    java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Step 6: Write header row
            writer.write("step_number,swap_count");
            for (int i = 0; i < arraySize; i++) {
                writer.write(",cell_");
                writer.write(String.valueOf(i));
            }
            writer.newLine();
            
            // Step 7: Write data rows
            for (StepSnapshot<T> snapshot : snapshots) {
                // Write step number and swap count
                writer.write(String.valueOf(snapshot.getStepNumber()));
                writer.write(",");
                writer.write(String.valueOf(snapshot.getSwapCount()));
                
                // Write cell values
                T[] cells = snapshot.getCellStates();
                for (T cell : cells) {
                    writer.write(",");
                    writer.write(escapeCsvValue(cell.toString()));
                }
                
                writer.newLine();
            }
        }
    }
    
    /**
     * PURPOSE: Export raw probe snapshots to JSON file with full metadata.
     * 
     * INPUTS:
     *   - filepath: Path where JSON file should be written
     *   - probe: Probe containing snapshot history
     * 
     * PROCESS:
     *   1. Validate inputs (filepath not null, probe not null/empty)
     *   2. Create parent directories if they don't exist
     *   3. Open file writer with UTF-8 encoding
     *   4. Write JSON opening: {"snapshots": [
     *   5. For each snapshot in probe:
     *      a. Write snapshot object with fields:
     *         - stepNumber: int
     *         - swapCount: int
     *         - timestamp: long (nanoseconds)
     *         - cellValues: array of cell values
     *         - cellTypeDistribution: map (if available)
     *      b. Add comma separator between snapshots (except last)
     *   6. Write JSON closing: ]}
     *   7. Flush and close file writer
     *   8. Handle IOExceptions gracefully
     * 
     * OUTPUTS: void (side effect: creates JSON file)
     * 
     * THROWS: IOException if file operations fail
     * 
     * JSON FORMAT EXAMPLE:
     *   {
     *     "snapshots": [
     *       {
     *         "stepNumber": 0,
     *         "swapCount": 0,
     *         "timestamp": 1234567890123456,
     *         "cellValues": [5, 2, 8, 1, 3],
     *         "cellTypeDistribution": {"BUBBLE": 3, "SELECTION": 2}
     *       },
     *       ...
     *     ]
     *   }
     * 
     * USAGE:
     *   - Export with full metadata for archival
     *   - Preserve timing information for performance analysis
     *   - Include cell type distribution for chimeric analysis
     * 
     * DESIGN RATIONALE:
     *   - JSON preserves structured metadata better than CSV
     *   - Timestamp enables temporal analysis (steps/second)
     *   - cellTypeDistribution crucial for chimeric experiments
     *   - Self-documenting format with field names
     * 
     * IMPLEMENTATION:
     *   This section exports raw snapshot data with metadata to JSON format.
     *   It integrates with the export system by following the established pattern.
     *   Includes all available metadata for comprehensive data archival.
     */
    public static <T extends Cell<T>> void exportSnapshotsToJSON(String filepath, Probe<T> probe) 
            throws IOException {
        // Step 1: Validate filepath
        if (filepath == null || filepath.trim().isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be null or empty");
        }
        
        // Step 2: Validate probe
        if (probe == null) {
            throw new IllegalArgumentException("Probe cannot be null");
        }
        if (probe.getSnapshotCount() == 0) {
            throw new IllegalArgumentException("Probe contains no snapshots");
        }
        
        // Step 3: Ensure parent directories exist
        ensureDirectoryExists(filepath);
        
        // Step 4: Get snapshots\n        List<StepSnapshot<T>> snapshots = probe.getSnapshots();\n        \n        // Validate chronological order and consistent array size\n        if (snapshots.size() > 1) {\n            for (int i = 1; i < snapshots.size(); i++) {\n                StepSnapshot<T> previous = snapshots.get(i - 1);\n                StepSnapshot<T> current = snapshots.get(i);\n                if (current.getTimestamp() < previous.getTimestamp()) {\n                    throw new IllegalArgumentException(\n                        \"Snapshots are not in chronological order: \" +\n                        \"snapshot index \" + (i - 1) + \" (timestamp=\" + previous.getTimestamp() + \") \" +\n                        \"comes after snapshot index \" + i + \" (timestamp=\" + current.getTimestamp() + \")\"\n                    );\n                }\n            }\n        }\n        if (!snapshots.isEmpty()) {\n            int expectedSize = snapshots.get(0).getCellStates().length;\n            for (int idx = 0; idx < snapshots.size(); idx++) {\n                StepSnapshot<T> s = snapshots.get(idx);\n                int actualSize = s.getCellStates().length;\n                if (actualSize != expectedSize) {\n                    throw new IllegalArgumentException(\n                        \"Inconsistent array size across snapshots: \" +\n                        \"snapshotIndex=\" + idx + \", \" +\n                        \"stepNumber=\" + s.getStepNumber() + \", \" +\n                        \"expectedLength=\" + expectedSize + \", \" +\n                        \"actualLength=\" + actualSize\n                    );\n                }\n            }\n        }\n        
        // Step 5: Open file writer and write JSON
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(filepath), 
                    java.nio.charset.StandardCharsets.UTF_8))) {
            
            // Step 6: Write JSON opening
            writer.write("{\n");
            writer.write("  \"snapshots\": [\n");
            
            // Step 7: Write each snapshot
            for (int i = 0; i < snapshots.size(); i++) {
                StepSnapshot<T> snapshot = snapshots.get(i);
                
                writer.write("    {\n");
                
                // Write stepNumber
                writer.write("      \"stepNumber\": ");
                writer.write(String.valueOf(snapshot.getStepNumber()));
                writer.write(",\n");
                
                // Write swapCount
                writer.write("      \"swapCount\": ");
                writer.write(String.valueOf(snapshot.getSwapCount()));
                writer.write(",\n");
                
                // Write timestamp
                writer.write("      \"timestamp\": ");
                writer.write(String.valueOf(snapshot.getTimestamp()));
                writer.write(",\n");
                
                // Write cellValues array
                writer.write("      \"cellValues\": [");
                T[] cells = snapshot.getCellStates();
                for (int j = 0; j < cells.length; j++) {
                    writer.write("\"");
                    String valStr = cells[j].toString();\n                    try {\n                        Double.parseDouble(valStr);\n                        writer.write(valStr);\n                    } catch (NumberFormatException e) {\n                        writer.write(\"\\\"\" + valStr.replace(\"\\\"\", \"\\\\\\\"\") + \"\\\"\");\n                    }
                    writer.write("\"");
                    if (j < cells.length - 1) {
                        writer.write(", ");
                    }
                }
                writer.write("]");
                
                // Write cellTypeDistribution if available
                if (snapshot.hasCellTypeDistribution()) {
                    writer.write(",\n");
                    writer.write("      \"cellTypeDistribution\": {");
                    
                    Map<com.emergent.doom.cell.Algotype, Integer> distribution = 
                        snapshot.getCellTypeDistribution();
                    int entryCount = 0;
                    for (Map.Entry<com.emergent.doom.cell.Algotype, Integer> entry : 
                         distribution.entrySet()) {
                        if (entryCount > 0) {
                            writer.write(", ");
                        }
                        writer.write("\"");
                        writer.write(entry.getKey().toString());
                        writer.write("\": ");
                        writer.write(String.valueOf(entry.getValue()));
                        entryCount++;
                    }
                    writer.write("}\n");
                } else {
                    writer.write("\n");
                }
                
                writer.write("    }");
                
                // Add comma unless last snapshot
                if (i < snapshots.size() - 1) {
                    writer.write(",");
                }
                writer.write("\n");
            }
            
            // Step 8: Write JSON closing
            writer.write("  ]\n");
            writer.write("}\n");
        }
    }
    
    /**
     * PURPOSE: Create parent directories for a file path if they don't exist.
     * 
     * INPUTS:
     *   - filepath: File path (may include directory components)
     * 
     * PROCESS:
     *   1. Extract directory path from filepath
     *   2. Check if directory exists
     *   3. If not, create all necessary parent directories
     *   4. Return success status
     * 
     * OUTPUTS: boolean - true if directories exist or were created
     * 
     * THROWS: IOException if directory creation fails
     * 
     * USAGE:
     *   - Called by all export methods before writing files
     *   - Ensures export doesn't fail due to missing directories
     * 
     * DESIGN RATIONALE:
     *   - Improves user experience (no need to pre-create directories)
     *   - Centralizes directory handling logic
     *   - Prevents common I/O errors
     * 
     * IMPLEMENTATION:
     *   This section ensures parent directories exist for a file path.
     *   It integrates with all export methods by being called before file writing.
     *   Uses java.nio.file.Files and Paths for modern, robust directory creation.
     */
    private static void ensureDirectoryExists(String filepath) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(filepath);
        java.nio.file.Path parentDir = path.getParent();
        
        if (parentDir != null && !java.nio.file.Files.exists(parentDir)) {
            java.nio.file.Files.createDirectories(parentDir);
        }
    }
    
    /**
     * PURPOSE: Validate that all trajectories in a map have the same length.
     * 
     * INPUTS:
     *   - trajectories: Map of metric names to value lists
     * 
     * PROCESS:
     *   1. Check if map is null or empty
     *   2. Get length of first trajectory
     *   3. Iterate through remaining trajectories
     *   4. Check if each has same length
     *   5. Return validation result
     * 
     * OUTPUTS: boolean - true if all trajectories have same length
     * 
     * THROWS: IllegalArgumentException if validation fails
     * 
     * USAGE:
     *   - Called by CSV/JSON export methods to validate inputs
     *   - Ensures data consistency for multi-metric exports
     * 
     * DESIGN RATIONALE:
     *   - Prevents corrupted output files from mismatched data
     *   - Centralizes validation logic
     *   - Provides clear error messages for debugging
     * 
     * IMPLEMENTATION:
     *   This section validates that all trajectories have the same length.
     *   It integrates with export methods by being called during input validation.
     *   Throws clear exceptions to help users identify data inconsistencies.
     */
    private static void validateTrajectoriesHaveSameLength(Map<String, List<Double>> trajectories) {
        if (trajectories == null || trajectories.isEmpty()) {
            return; // Already handled by calling methods
        }
        
        // Get length from first trajectory
        Integer expectedLength = null;
        String firstMetric = null;
        
        for (Map.Entry<String, List<Double>> entry : trajectories.entrySet()) {
            String metricName = entry.getKey();
            List<Double> trajectory = entry.getValue();
            
            if (trajectory == null) {
                throw new IllegalArgumentException(
                    "Trajectory for metric '" + metricName + "' is null");
            }
            
            if (expectedLength == null) {
                expectedLength = trajectory.size();
                firstMetric = metricName;
            } else {
                if (trajectory.size() != expectedLength) {
                    throw new IllegalArgumentException(
                        "Trajectory length mismatch: '" + firstMetric + "' has " + 
                        expectedLength + " values, but '" + metricName + "' has " + 
                        trajectory.size() + " values");
                }
            }
        }
    }
    
    /**
     * PURPOSE: Escape a string value for safe use in CSV format.
     * 
     * INPUTS:
     *   - value: String to escape
     * 
     * PROCESS:
     *   1. Check if value contains special CSV characters (comma, quote, newline)
     *   2. If yes:
     *      a. Wrap value in double quotes
     *      b. Escape any internal double quotes by doubling them
     *   3. Return escaped value
     * 
     * OUTPUTS: CSV-safe string
     * 
     * USAGE:
     *   - Called when writing string values to CSV
     *   - Ensures CSV parsing works correctly
     * 
     * DESIGN RATIONALE:
     *   - Follows CSV RFC 4180 standard
     *   - Prevents CSV injection and parsing errors
     *   - Handles edge cases like metric names with commas
     * 
     * IMPLEMENTATION:
     *   This section escapes strings for CSV format per RFC 4180.
     *   It integrates with CSV export methods by being called on all string values.
     *   Handles special characters: comma, quote, newline, carriage return.
     */
    private static String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        
        // Check if value needs escaping
        boolean needsEscaping = value.contains(",") || 
                                value.contains("\"") || 
                                value.contains("\n") || 
                                value.contains("\r");
        
        if (!needsEscaping) {
            return value;
        }
        
        // Escape internal quotes by doubling them
        String escaped = value.replace("\"", "\"\"");
        
        // Wrap in quotes
        return "\"" + escaped + "\"";
    }
}
