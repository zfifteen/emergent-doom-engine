# Chapter 4: Analysis Tools

Analysis tools transform raw execution data into insights about emergent behavior. They provide trajectory visualization, metric computation, and pattern detection to support research and debugging.

## Purpose

Tests in this package validate that analysis tools:
- Compute trajectory-level metrics (Monotonicity, Sortedness over time)
- Detect delayed gratification events
- Identify clustering patterns in chimeric populations
- Generate visualization-ready data structures
- Support post-hoc analysis of recorded experiments

## Concepts Covered

### Trajectory Analysis

A **trajectory** is the complete sequence of array states during execution:
- **Temporal dimension**: Steps from 0 to convergence
- **State dimension**: Cell values at each step
- **Metric dimension**: Computed measures over time

### Pattern Detection

Analysis tools identify emergent patterns:
- **Convergence dynamics**: How quickly system approaches goal
- **Oscillations**: Cyclic behavior indicating instability
- **Plateaus**: Periods of no progress
- **Phase transitions**: Sudden changes in sorting rate

### Visualization Support

Tools prepare data for graphing and rendering:
- **Time series**: Metric values over steps
- **Heatmaps**: Cell position changes over time
- **Clustering diagrams**: Algotype segregation visualization
- **Comparison plots**: Multiple trajectories overlaid

## Prerequisites

**Required:**
- [Chapter 2: Probe Recording](../probe/README.md) - Snapshot capture
- [Chapter 2: Metrics](../metrics/README.md) - Metric computation

**Helpful:**
- Data visualization concepts
- Time series analysis basics
- [Chapter 4: Chimeric Populations](../chimeric/README.md) - For clustering analysis

## Test Files

### TrajectoryAnalyzerTest.java **[Advanced]**

Tests for trajectory-level analysis utilities.

**Test Categories:**

1. **Metric Trajectory Computation**
   - Monotonicity over time
   - Sortedness over time
   - Delayed gratification detection

2. **Pattern Detection**
   - Convergence rate estimation
   - Oscillation detection
   - Plateau identification

3. **Visualization Data Preparation**
   - Time series generation
   - Heatmap data formatting
   - Clustering visualization data

**Link to source:** [TrajectoryAnalyzer.java](../../../../../../main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java)

## Usage Examples

### Computing Metric Trajectories

Extract time series from snapshots:

```java
Probe<IntCell> probe = getProbeFromExperiment();

// Create analyzer
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);

// Compute metric trajectories
List<Double> monotonicity = analyzer.computeMonotonicityTrajectory();
List<Double> sortedness = analyzer.computeSortednessTrajectory();

// Print values
for (int step = 0; step < monotonicity.size(); step++) {
    System.out.printf("Step %d: Mono=%.1f%%, Sort=%.1f%%%n",
                      step, monotonicity.get(step), sortedness.get(step));
}
```

**Key points:**
- One value per snapshot
- Useful for plotting convergence curves
- Can compute any metric that works on cell arrays

### Detecting Delayed Gratification

Find temporary setbacks in trajectory:

```java
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
List<Double> sortedness = analyzer.computeSortednessTrajectory();

DelayedGratificationCalculator dgCalc = new DelayedGratificationCalculator();
double dg = dgCalc.calculate(sortedness);
int dgEvents = dgCalc.countDGEvents(sortedness);

System.out.printf("DG ratio: %.2f%n", dg);
System.out.printf("DG events: %d%n", dgEvents);

// Find specific DG events
for (int step = 2; step < sortedness.size(); step++) {
    if (isDGEvent(sortedness, step)) {
        System.out.printf("DG event at step %d: %.1f%% → %.1f%% → %.1f%%%n",
                          step - 1,
                          sortedness.get(step - 2),
                          sortedness.get(step - 1),
                          sortedness.get(step));
    }
}
```

**DG event detection helper:**
```java
private boolean isDGEvent(List<Double> trajectory, int step) {
    if (step < 2) return false;
    
    double prev = trajectory.get(step - 2);
    double nadir = trajectory.get(step - 1);
    double curr = trajectory.get(step);
    
    // Decrease then increase
    return prev > nadir && curr > nadir && curr > prev;
}
```

### Visualizing Convergence

Prepare data for plotting:

```java
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
List<Double> sortedness = analyzer.computeSortednessTrajectory();

// Export for plotting tool (matplotlib, gnuplot, etc.)
try (PrintWriter writer = new PrintWriter("sortedness.csv")) {
    writer.println("step,sortedness");
    for (int step = 0; step < sortedness.size(); step++) {
        writer.printf("%d,%.2f%n", step, sortedness.get(step));
    }
}

// Or use in-memory charting library
Chart chart = new LineChart();
chart.addSeries("Sortedness", IntStream.range(0, sortedness.size()).toArray(),
                sortedness.stream().mapToDouble(d -> d).toArray());
chart.setXLabel("Step");
chart.setYLabel("Sortedness (%)");
chart.display();
```

### Comparing Multiple Trajectories

Overlay different algotypes:

```java
Map<Algotype, List<Double>> trajectories = new HashMap<>();

for (Algotype algotype : List.of(Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION)) {
    // Run experiment with this algotype
    Probe<IntCell> probe = runExperiment(algotype);
    TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);
    trajectories.put(algotype, analyzer.computeSortednessTrajectory());
}

// Find which converges fastest
int bubbleConvergence = findConvergenceStep(trajectories.get(Algotype.BUBBLE));
int insertionConvergence = findConvergenceStep(trajectories.get(Algotype.INSERTION));
int selectionConvergence = findConvergenceStep(trajectories.get(Algotype.SELECTION));

System.out.println("Convergence steps:");
System.out.printf("  BUBBLE: %d%n", bubbleConvergence);
System.out.printf("  INSERTION: %d%n", insertionConvergence);
System.out.printf("  SELECTION: %d%n", selectionConvergence);
```

**Convergence detection helper:**
```java
private int findConvergenceStep(List<Double> trajectory) {
    double threshold = 99.0; // 99% sorted
    for (int step = 0; step < trajectory.size(); step++) {
        if (trajectory.get(step) >= threshold) {
            return step;
        }
    }
    return trajectory.size(); // Never converged
}
```

### Analyzing Clustering Dynamics

Track algotype segregation over time:

```java
Probe<IntCell> probe = getChimericExperimentProbe();
IntFunction<CellMetadata> metadata = getChimericMetadata();

List<Integer> clusterCounts = new ArrayList<>();

for (int step = 0; step < probe.getSnapshotCount(); step++) {
    IntCell[] cells = probe.getSnapshot(step).getCellsCopy();
    int clusters = countAlgotypeClusters(cells, metadata);
    clusterCounts.add(clusters);
}

// Plot cluster count over time
System.out.println("Clustering dynamics:");
for (int step = 0; step < clusterCounts.size(); step += 10) {
    System.out.printf("Step %d: %d clusters%n", step, clusterCounts.get(step));
}
```

**Cluster counting helper:**
```java
private int countAlgotypeClusters(IntCell[] cells, IntFunction<CellMetadata> metadata) {
    if (cells.length == 0) return 0;
    
    int clusters = 1;
    Algotype prevAlgotype = metadata.apply(0).getAlgotype();
    
    for (int i = 1; i < cells.length; i++) {
        Algotype currAlgotype = metadata.apply(i).getAlgotype();
        if (currAlgotype != prevAlgotype) {
            clusters++;
            prevAlgotype = currAlgotype;
        }
    }
    
    return clusters;
}
```

### Generating Heatmap Data

Visualize cell movement over time:

```java
TrajectoryAnalyzer<IntCell> analyzer = new TrajectoryAnalyzer<>(probe);

// Build 2D matrix: [step][position] = value
int[][] heatmapData = new int[probe.getSnapshotCount()][arraySize];

for (int step = 0; step < probe.getSnapshotCount(); step++) {
    IntCell[] cells = probe.getSnapshot(step).getCellsCopy();
    for (int pos = 0; pos < cells.length; pos++) {
        heatmapData[step][pos] = cells[pos].getValue();
    }
}

// Export for heatmap tool
exportHeatmap(heatmapData, "trajectory_heatmap.csv");
```

**Heatmap export helper:**
```java
private void exportHeatmap(int[][] data, String filename) throws IOException {
    try (PrintWriter writer = new PrintWriter(filename)) {
        // Header row
        writer.print("step");
        for (int pos = 0; pos < data[0].length; pos++) {
            writer.printf(",pos_%d", pos);
        }
        writer.println();
        
        // Data rows
        for (int step = 0; step < data.length; step++) {
            writer.print(step);
            for (int pos = 0; pos < data[step].length; pos++) {
                writer.printf(",%d", data[step][pos]);
            }
            writer.println();
        }
    }
}
```

## Architecture Insights

### Lazy Computation

Analyzer computes metrics on-demand:

```java
public List<Double> computeSortednessTrajectory() {
    return probe.getSnapshots().stream()
        .map(snapshot -> sortednessMetric.compute(snapshot.getCellsCopy()))
        .collect(Collectors.toList());
}
```

**Benefits:**
- No pre-computation overhead
- Compute only what's needed
- Easy to add new analyses

**Trade-off:** Re-computation if called multiple times. Cache if needed.

### Stream-Based Processing

Analysis uses Java Streams for concise operations:

```java
// Find max sortedness value
double maxSortedness = analyzer.computeSortednessTrajectory().stream()
    .mapToDouble(d -> d)
    .max()
    .orElse(0.0);

// Count steps below threshold
long belowThreshold = analyzer.computeSortednessTrajectory().stream()
    .filter(s -> s < 50.0)
    .count();
```

**Benefits:**
- Readable data transformations
- Parallel processing available (`.parallelStream()`)
- Composable operations

### Immutable Snapshots Guarantee Consistency

Analysis always works on immutable copies:

```java
IntCell[] cells = snapshot.getCellsCopy(); // Defensive copy
double metric = compute(cells);
// Original snapshot unchanged
```

**Why this matters:**
- No accidental mutations
- Thread-safe analysis
- Reproducible results

## Common Patterns

### Convergence Rate Estimation

Measure how quickly sortedness increases:

```java
List<Double> sortedness = analyzer.computeSortednessTrajectory();

// Estimate rate over first 100 steps
if (sortedness.size() >= 100) {
    double initialSortedness = sortedness.get(0);
    double sortednessAt100 = sortedness.get(100);
    double rate = (sortednessAt100 - initialSortedness) / 100.0;
    
    System.out.printf("Convergence rate: %.2f%% per step%n", rate);
}
```

### Oscillation Detection

Identify unstable sorting:

```java
List<Double> sortedness = analyzer.computeSortednessTrajectory();
int oscillations = 0;

for (int i = 2; i < sortedness.size(); i++) {
    double prev = sortedness.get(i - 2);
    double curr = sortedness.get(i - 1);
    double next = sortedness.get(i);
    
    // Peak or valley
    if ((curr > prev && curr > next) || (curr < prev && curr < next)) {
        oscillations++;
    }
}

System.out.println("Oscillations detected: " + oscillations);
```

### Statistical Summary

Compute trajectory statistics:

```java
List<Double> sortedness = analyzer.computeSortednessTrajectory();

DoubleSummaryStatistics stats = sortedness.stream()
    .mapToDouble(d -> d)
    .summaryStatistics();

System.out.printf("Min: %.1f%%%n", stats.getMin());
System.out.printf("Max: %.1f%%%n", stats.getMax());
System.out.printf("Average: %.1f%%%n", stats.getAverage());
```

## Troubleshooting

### "Trajectory is empty"

**Problem:** No snapshots recorded.

**Check:** Was recording enabled?
```java
probe.setRecordingEnabled(true); // Before running engine
```

### "Metrics return NaN"

**Problem:** Invalid cell state or empty arrays.

**Debug:**
```java
for (int step = 0; step < probe.getSnapshotCount(); step++) {
    IntCell[] cells = probe.getSnapshot(step).getCellsCopy();
    System.out.printf("Step %d: array size %d%n", step, cells.length);
    if (cells.length == 0) {
        System.err.println("Empty array at step " + step);
    }
}
```

### "Analysis takes too long"

**Problem:** Computing metrics on large trajectories.

**Solutions:**
```java
// 1. Sample trajectory (every Nth step)
List<Double> sampled = IntStream.range(0, probe.getSnapshotCount())
    .filter(i -> i % 10 == 0) // Every 10th step
    .mapToObj(i -> probe.getSnapshot(i))
    .map(s -> metric.compute(s.getCellsCopy()))
    .collect(Collectors.toList());

// 2. Parallel computation
List<Double> parallel = probe.getSnapshots().parallelStream()
    .map(s -> metric.compute(s.getCellsCopy()))
    .collect(Collectors.toList());

// 3. Cache results
List<Double> cached = analyzer.computeSortednessTrajectory();
// Reuse 'cached' instead of recomputing
```

## Next Steps

Now that you understand trajectory analysis, proceed to:

**[Chapter 5: Traditional Algorithms](../traditional/README.md)** - Compare EDE behavior with classical sorting implementations.

**Also see:**
- [Chapter 5: Visualization](../visualization/README.md) - Rendering and display tools
- [Chapter 4: Experiment Framework](../experiment/README.md) - Generating analysis data

---

**[← Back: Experiment Framework](../experiment/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Traditional Algorithms →](../traditional/README.md)**
