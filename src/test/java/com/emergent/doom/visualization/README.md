# Chapter 5: Visualization Tools

Visualization tools transform abstract execution data into **visual representations** that reveal patterns, aid debugging, and support research communication. They bridge quantitative metrics and human intuition.

## Purpose

Tests in this package validate that visualization infrastructure:
- Exports trajectory data in plottable formats (CSV, JSON)
- Generates plot-ready data structures
- Integrates with analysis tools (TrajectoryAnalyzer)
- Supports multiple visualization types (line plots, heatmaps, clustering diagrams)
- Produces correct and consistent output

## Concepts Covered

### Visualization Pipeline

Data flows through stages:

1. **Capture**: Probe records snapshots during execution
2. **Analyze**: TrajectoryAnalyzer computes metrics over time
3. **Export**: TrajectoryDataExporter writes to files
4. **Render**: External tools (matplotlib, gnuplot, D3.js) create visuals

EDE provides stages 1-3; users choose rendering tools.

### Visualization Types

Different views reveal different insights:

- **Line plots**: Metric trajectories (Monotonicity, Sortedness over time)
- **Heatmaps**: Cell position evolution across steps
- **Scatter plots**: Convergence vs array size (scaling analysis)
- **Bar charts**: Algotype performance comparison
- **Clustering diagrams**: Chimeric population segregation

### Export Formats

Tools support multiple formats for flexibility:

- **CSV**: Universal, works with spreadsheets and plotting libraries
- **JSON**: Structured data for web visualization
- **Custom**: Domain-specific formats for specialized tools

## Prerequisites

**Required:**
- [Chapter 2: Probe Recording](../probe/README.md) - Snapshot capture
- [Chapter 4: Analysis Tools](../analysis/README.md) - Trajectory analysis

**Helpful:**
- Plotting library experience (matplotlib, ggplot, D3.js, etc.)
- Data visualization principles

## Test Files

### VisualizationIntegrationTest.java

Integration tests for complete visualization workflow.

**Test Workflow:**

1. **Create probe with snapshots**
   - Simulate sorting process
   - Record snapshots at each step

2. **Analyze trajectories**
   - Compute sortedness trajectory
   - Compute swap count trajectory

3. **Generate plot data** (if applicable)
   - Prepare data structures for plotting

4. **Export data**
   - Write to CSV/JSON files
   - Validate file contents

**Key Tests:**
- Complete workflow from probe to export
- Trajectory analysis produces expected values
- Exported files are valid and readable
- Sortedness improves over time (validation)

**Link to source:** [TrajectoryDataExporter.java](../../../../../../main/java/com/emergent/doom/export/TrajectoryDataExporter.java)

## Usage Examples

### Exporting Sortedness Trajectory

Create CSV for line plot:

```java
Probe<IntCell> probe = getProbeFromExperiment();
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
List<Double> sortedness = analyzer.computeSortednessTrajectory();

// Export to CSV
Path outputPath = Paths.get("sortedness.csv");
try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
    writer.println("step,sortedness");
    for (int step = 0; step < sortedness.size(); step++) {
        writer.printf("%d,%.2f%n", step, sortedness.get(step));
    }
}

System.out.println("Exported to " + outputPath);
```

**Then plot with Python/matplotlib:**
```python
import matplotlib.pyplot as plt
import pandas as pd

data = pd.read_csv('sortedness.csv')
plt.plot(data['step'], data['sortedness'])
plt.xlabel('Step')
plt.ylabel('Sortedness (%)')
plt.title('Convergence Dynamics')
plt.savefig('sortedness.png')
```

### Comparing Multiple Trajectories

Overlay different algotypes:

```java
Map<Algotype, List<Double>> trajectories = new HashMap<>();

// Run experiments for each algotype
for (Algotype algotype : List.of(Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION)) {
    Probe<IntCell> probe = runExperiment(algotype);
    TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
    trajectories.put(algotype, analyzer.computeSortednessTrajectory());
}

// Export combined data
try (PrintWriter writer = new PrintWriter("algotype_comparison.csv")) {
    writer.println("step,bubble,insertion,selection");
    
    int maxSteps = trajectories.values().stream()
        .mapToInt(List::size)
        .max()
        .orElse(0);
    
    for (int step = 0; step < maxSteps; step++) {
        writer.printf("%d", step);
        for (Algotype algotype : List.of(Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION)) {
            List<Double> traj = trajectories.get(algotype);
            double value = (step < traj.size()) ? traj.get(step) : 100.0;
            writer.printf(",%.2f", value);
        }
        writer.println();
    }
}
```

### Generating Heatmap Data

Visualize cell movement over time:

```java
Probe<IntCell> probe = getProbeFromExperiment();

// Build 2D matrix: heatmap[step][position] = value
int steps = probe.getSnapshotCount();
int arraySize = probe.getSnapshot(0).getCellsCopy().length;
int[][] heatmap = new int[steps][arraySize];

for (int step = 0; step < steps; step++) {
    IntCell[] cells = probe.getSnapshot(step).getCellsCopy();
    for (int pos = 0; pos < arraySize; pos++) {
        heatmap[step][pos] = cells[pos].getValue();
    }
}

// Export to CSV
try (PrintWriter writer = new PrintWriter("heatmap.csv")) {
    // Header
    writer.print("step");
    for (int pos = 0; pos < arraySize; pos++) {
        writer.printf(",pos_%d", pos);
    }
    writer.println();
    
    // Data
    for (int step = 0; step < steps; step++) {
        writer.print(step);
        for (int pos = 0; pos < arraySize; pos++) {
            writer.printf(",%d", heatmap[step][pos]);
        }
        writer.println();
    }
}
```

**Then create heatmap with Python/seaborn:**
```python
import seaborn as sns
import pandas as pd

data = pd.read_csv('heatmap.csv', index_col=0)
sns.heatmap(data, cmap='viridis')
plt.xlabel('Array Position')
plt.ylabel('Step')
plt.title('Cell Value Evolution')
plt.savefig('heatmap.png')
```

### Exporting Experiment Results

Summarize multi-trial data:

```java
ExperimentResults<IntCell> results = getExperimentResults();

// Export summary statistics
try (PrintWriter writer = new PrintWriter("experiment_summary.csv")) {
    writer.println("metric,value");
    writer.printf("average_steps,%.2f%n", results.getAverageSteps());
    writer.printf("median_steps,%.2f%n", results.getMedianSteps());
    writer.printf("std_dev,%.2f%n", results.getStdDevSteps());
    writer.printf("convergence_rate,%.2f%n", results.getConvergenceRate() * 100);
    writer.printf("trials,%d%n", results.getTrials().size());
}

// Export per-trial data
try (PrintWriter writer = new PrintWriter("trial_details.csv")) {
    writer.println("trial,steps,converged,final_sortedness");
    for (int i = 0; i < results.getTrials().size(); i++) {
        TrialResult<IntCell> trial = results.getTrials().get(i);
        writer.printf("%d,%d,%b,%.2f%n",
                      i,
                      trial.getFinalStep(),
                      trial.isConverged(),
                      trial.getFinalSortedness());
    }
}
```

### Creating Scaling Plot Data

Visualize linear scaling:

```java
Map<Integer, Double> sizeToAvgSteps = new HashMap<>();

// Run experiments at different array sizes
for (int n : new int[]{1000, 2000, 4000, 8000, 16000}) {
    double avgSteps = runMultipleTrials(n, 50); // 50 trials per size
    sizeToAvgSteps.put(n, avgSteps);
}

// Export for scatter plot
try (PrintWriter writer = new PrintWriter("scaling.csv")) {
    writer.println("array_size,average_steps");
    sizeToAvgSteps.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(entry -> writer.printf("%d,%.2f%n", 
                                         entry.getKey(), entry.getValue()));
}
```

**Plot to verify O(n) scaling:**
```python
data = pd.read_csv('scaling.csv')
plt.scatter(data['array_size'], data['average_steps'])
plt.xlabel('Array Size (n)')
plt.ylabel('Average Steps to Convergence')
plt.title('Linear Time Scaling Validation')

# Add linear regression line
from scipy import stats
slope, intercept, r, p, se = stats.linregress(data['array_size'], data['average_steps'])
plt.plot(data['array_size'], slope * data['array_size'] + intercept, 'r--',
         label=f'Linear fit (slope={slope:.6f})')
plt.legend()
plt.savefig('scaling.png')
```

## Architecture Insights

### Separation of Concerns

Visualization infrastructure separates:

1. **Data generation**: Probe, TrajectoryAnalyzer (EDE core)
2. **Data export**: TrajectoryDataExporter (EDE utilities)
3. **Rendering**: User-chosen tools (matplotlib, D3.js, etc.)

**Why this matters:**
- EDE doesn't dictate visualization style
- Users integrate with existing workflows
- Export formats are language-agnostic

### Format Independence

CSV/JSON exports work across ecosystems:

- **Python**: pandas, matplotlib, seaborn
- **R**: ggplot2, dplyr
- **JavaScript**: D3.js, Chart.js
- **Spreadsheets**: Excel, Google Sheets

### Lazy Export

Data isn't exported until explicitly requested:

```java
// Probe captures data
probe.recordSnapshot(0, cells, 0);

// Analysis happens on-demand
List<Double> sortedness = analyzer.computeSortednessTrajectory();

// Export when ready
exportToCSV(sortedness);
```

**Benefits:**
- No wasted I/O
- Flexibility in what to export
- Can post-process before export

## Common Patterns

### Interactive Debugging Workflow

Visualize during development:

```java
// Run experiment
engine.runUntilConvergence(5000);

// Quick export
exportTrajectory(probe, "debug.csv");

// View in plotting tool
Runtime.getRuntime().exec("python plot_trajectory.py debug.csv");
```

### Publication-Quality Figures

Generate high-resolution plots:

```python
# High DPI for publication
plt.figure(figsize=(10, 6), dpi=300)
# ... plotting code ...
plt.savefig('figure.pdf', bbox_inches='tight')  # PDF for LaTeX
```

### Web-Based Visualization

Export JSON for interactive web apps:

```java
// Export trajectory as JSON
JSONObject json = new JSONObject();
json.put("sortedness", sortedness);
json.put("monotonicity", monotonicity);

Files.writeString(Paths.get("trajectory.json"), json.toString());
```

```javascript
// Load and visualize in D3.js
d3.json('trajectory.json').then(data => {
    const svg = d3.select('svg');
    // ... D3 visualization code ...
});
```

## Troubleshooting

### "Exported CSV has wrong number of rows"

**Problem:** Mismatch between expected and actual data points.

**Check:**
```java
System.out.println("Snapshot count: " + probe.getSnapshotCount());
System.out.println("Trajectory length: " + sortedness.size());
// Should be equal
```

### "Plot shows unexpected spike"

**Problem:** Outlier or data corruption.

**Debug:** Print suspicious values:
```java
for (int i = 0; i < sortedness.size(); i++) {
    if (sortedness.get(i) > 100.0 || sortedness.get(i) < 0.0) {
        System.err.printf("Invalid sortedness at step %d: %.2f%n",
                          i, sortedness.get(i));
    }
}
```

### "Exported file is empty"

**Problem:** Export failed silently.

**Solution:** Add error handling:
```java
try (PrintWriter writer = new PrintWriter(outputPath)) {
    // Export code
    writer.flush();
    if (writer.checkError()) {
        throw new IOException("Write error occurred");
    }
} catch (IOException e) {
    System.err.println("Export failed: " + e.getMessage());
}
```

## Next Steps

Congratulations! You've completed the test suite documentation tour.

**What's next?**
- **Run experiments**: Use patterns from this guide to explore emergent phenomena
- **Contribute**: Add new visualizations and share with the community
- **Research**: Investigate open questions (chimeric clustering, DG behavior, scaling limits)

**Return to:**
- **[Test Suite Home](../README.md)** - Navigate to other chapters
- **[Main README](../../../../../../../README.md)** - Project overview and quick start

---

**[← Back: Integration Validation](../validation/README.md)** | **[↑ Test Suite Home](../README.md)**
