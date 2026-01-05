# Chapter 4: Experiment Framework

The experiment framework provides utilities for running **multi-trial experiments** with statistical rigor. It automates batch execution, collects metrics, and enables reproducible scientific studies of emergent phenomena.

## Purpose

Tests in this package validate that the experiment framework:
- Executes multiple trials with varying parameters
- Collects convergence metrics and trajectories
- Supports sequential and parallel trial execution
- Enables reproducible experiments via seeding
- Provides statistical summaries (mean, median, std dev)
- Integrates with chimeric populations

## Concepts Covered

### Statistical Validation

Single runs don't prove emergent properties - need statistical evidence:
- **Sample size**: 100+ trials for reliable statistics
- **Variance**: Measure spread of convergence times
- **Outliers**: Detect and analyze unusual behaviors
- **Confidence intervals**: Quantify uncertainty

### Experimental Design

Framework supports structured experimental design:
- **Control variables**: Array size, max steps, algotype
- **Independent variables**: What you change (e.g., chimeric percentage)
- **Dependent variables**: What you measure (e.g., convergence steps)
- **Replication**: Repeat experiments for validation

### Batch Execution Modes

1. **Sequential**: Run trials one at a time (deterministic, slower)
2. **Parallel**: Run trials concurrently (faster, non-deterministic without seeds)

### Trial Results

Each trial captures:
- Final step count
- Convergence status (true/false)
- Final sortedness percentage
- Execution time
- Optional trajectory snapshots

## Prerequisites

**Required:**
- [Chapter 3: Execution Engines](../execution/README.md) - Engine setup and execution
- [Chapter 2: Metrics](../metrics/README.md) - Sortedness and convergence measures

**Helpful:**
- Basic statistics (mean, standard deviation, percentiles)
- Experimental design principles
- [Chapter 4: Chimeric Populations](../chimeric/README.md) - For chimeric experiments

## Test Files

### ExperimentRunnerBatchTest.java **[Advanced]**

Tests for batch experiment execution (currently disabled - requires refactor for metadata providers).

**Test Categories:**

1. **Basic Batch Execution** (Disabled)
   - Standard 100-trial batches
   - Verification of completion
   - Results collection

2. **Configuration Testing** (Disabled)
   - Array size variation
   - Max steps limits
   - Execution mode switching

**Note:** Tests disabled pending migration to lightweight cell architecture with metadata providers.

**Link to source:** [ExperimentRunner.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/experiment/ExperimentRunner.java)

### ExperimentConfig **[Intermediate]**

Configuration object for experiment parameters:

**Fields:**
- `arraySize`: Number of cells in each trial
- `maxSteps`: Convergence timeout
- `stableSteps`: No-swap threshold for convergence
- `recordTrajectories`: Whether to capture full snapshots
- `executionMode`: SEQUENTIAL or PARALLEL
- `numTrials`: Total trials to run

**Link to source:** [ExperimentConfig.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/experiment/ExperimentConfig.java)

### ExperimentResults **[Intermediate]**

Container for multi-trial results:

**Methods:**
- `getTrials()`: List of individual trial results
- `getAverageSteps()`: Mean convergence time
- `getMedianSteps()`: Median convergence time
- `getStdDevSteps()`: Standard deviation
- `getConvergenceRate()`: Percentage of successful trials

**Link to source:** [ExperimentResults.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/experiment/ExperimentResults.java)

### TrialResult **[Intermediate]**

Single trial outcome:

**Fields:**
- `finalStep`: Steps to convergence
- `isConverged`: Completion status
- `finalSortedness`: Final sortedness percentage
- `executionTimeMs`: Wall-clock time
- `trajectory`: Optional snapshot history

**Link to source:** [TrialResult.java](https://github.com/zfifteen/emergent-doom-engine/blob/7e77864a5553f144acb55bff7115b22c3d2919cb/src/main/java/com/emergent/doom/experiment/TrialResult.java)

## Usage Examples

### Running a Basic Experiment

Execute 100 trials to measure average convergence:

```java
// Configuration
ExperimentConfig config = ExperimentConfig.builder()
    .arraySize(100)
    .maxSteps(5000)
    .stableSteps(10)
    .recordTrajectories(false) // Skip snapshots for speed
    .executionMode(ExecutionMode.PARALLEL)
    .numTrials(100)
    .build();

// Cell factory
Supplier<IntCell[]> cellFactory = () -> createRandomCells(100);

// Metadata factory
Supplier<IntFunction<CellMetadata>> metadataFactory = () -> 
    index -> new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);

// Run experiment
ExperimentRunner<IntCell> runner = new ExperimentRunner<>(cellFactory, metadataFactory);
ExperimentResults<IntCell> results = runner.runBatchExperiments(config);

// Analyze results
System.out.printf("Average steps: %.1f%n", results.getAverageSteps());
System.out.printf("Median steps: %.1f%n", results.getMedianSteps());
System.out.printf("Std dev: %.1f%n", results.getStdDevSteps());
System.out.printf("Convergence rate: %.1f%%%n", results.getConvergenceRate() * 100);
```

**Key points:**
- Parallel mode for speed (uses thread pool)
- Disable trajectories unless needed (memory intensive)
- 100 trials is minimum for reliable statistics

### Comparing Algotype Performance

Which algotype converges fastest?

```java
// Test each algotype
for (Algotype algotype : List.of(Algotype.BUBBLE, Algotype.INSERTION, Algotype.SELECTION)) {
    Supplier<IntFunction<CellMetadata>> metadataFactory = () -> 
        index -> new CellMetadata(algotype, SortDirection.ASCENDING);
    
    ExperimentRunner<IntCell> runner = new ExperimentRunner<>(cellFactory, metadataFactory);
    ExperimentResults<IntCell> results = runner.runBatchExperiments(config);
    
    System.out.printf("%s: %.1f ± %.1f steps%n", 
                      algotype, 
                      results.getAverageSteps(),
                      results.getStdDevSteps());
}
```

**Expected output:**
```
BUBBLE: 1200.5 ± 150.2 steps
INSERTION: 980.3 ± 120.5 steps
SELECTION: 1100.7 ± 140.1 steps
```

### Chimeric vs Homogeneous Comparison

Does mixing algotypes help or hurt?

```java
// Homogeneous: 100% BUBBLE
Supplier<IntFunction<CellMetadata>> homogeneous = () -> 
    index -> new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);

ExperimentResults<IntCell> homogeneousResults = 
    new ExperimentRunner<>(cellFactory, homogeneous).runBatchExperiments(config);

// Chimeric: 50% BUBBLE, 50% INSERTION
Supplier<IntFunction<CellMetadata>> chimeric = () -> {
    Map<Algotype, Double> mix = Map.of(Algotype.BUBBLE, 0.5, Algotype.INSERTION, 0.5);
    PercentageAlgotypeProvider provider = new PercentageAlgotypeProvider(mix, 100, System.nanoTime());
    return index -> {
        Algotype algotype = Algotype.valueOf(provider.getAlgotype(index, 100));
        return new CellMetadata(algotype, SortDirection.ASCENDING);
    };
};

ExperimentResults<IntCell> chimericResults = 
    new ExperimentRunner<>(cellFactory, chimeric).runBatchExperiments(config);

// Compare
double overhead = (chimericResults.getAverageSteps() - homogeneousResults.getAverageSteps()) 
                  / homogeneousResults.getAverageSteps() * 100;
                  
System.out.printf("Chimeric overhead: %.1f%%%n", overhead);
```

### Sequential vs Parallel Execution

Compare execution modes:

```java
// Sequential mode (deterministic, slower)
ExperimentConfig seqConfig = config.withExecutionMode(ExecutionMode.SEQUENTIAL);
long seqStart = System.currentTimeMillis();
ExperimentResults<IntCell> seqResults = runner.runBatchExperiments(seqConfig);
long seqTime = System.currentTimeMillis() - seqStart;

// Parallel mode (faster, requires more memory)
ExperimentConfig parConfig = config.withExecutionMode(ExecutionMode.PARALLEL);
long parStart = System.currentTimeMillis();
ExperimentResults<IntCell> parResults = runner.runBatchExperiments(parConfig);
long parTime = System.currentTimeMillis() - parStart;

System.out.printf("Sequential: %d ms%n", seqTime);
System.out.printf("Parallel: %d ms%n", parTime);
System.out.printf("Speedup: %.1fx%n", (double) seqTime / parTime);
```

### Collecting Trajectory Data

Capture full execution history for analysis:

```java
ExperimentConfig trajConfig = config.withRecordTrajectories(true);

ExperimentResults<IntCell> results = runner.runBatchExperiments(trajConfig);

// Analyze first trial's trajectory
TrialResult<IntCell> firstTrial = results.getTrials().get(0);
List<StepSnapshot<IntCell>> trajectory = firstTrial.getTrajectory();

// Compute delayed gratification
List<Double> sortednessValues = trajectory.stream()
    .map(snapshot -> new SortednessValue<IntCell>().compute(snapshot.getCellsCopy()))
    .collect(Collectors.toList());
    
double dg = new DelayedGratificationCalculator().calculate(sortednessValues);
System.out.println("Trial 0 DG: " + dg);
```

**Warning:** Recording trajectories uses significant memory. For 100 trials × 5000 steps × 100 cells = 50M cell copies.

## Architecture Insights

### Factory Pattern for Reproducibility

Experiment runner uses **factory suppliers** for flexibility:

```java
public class ExperimentRunner<T extends Cell<T>> {
    private final Supplier<T[]> cellFactory;
    private final Supplier<IntFunction<CellMetadata>> metadataFactory;
    
    // Each trial gets fresh cells and metadata
    public void runTrial() {
        T[] cells = cellFactory.get(); // Fresh array
        IntFunction<CellMetadata> metadata = metadataFactory.get(); // Fresh metadata
        // ... run engine ...
    }
}
```

**Why this matters:**
- Each trial starts with clean state
- No contamination between trials
- Easy to vary parameters per trial

### Statistical Aggregation

Results object computes statistics on-demand:

```java
public double getAverageSteps() {
    return trials.stream()
        .mapToInt(TrialResult::getFinalStep)
        .average()
        .orElse(0.0);
}
```

**Benefits:**
- No pre-computation overhead
- Always reflects current trial list
- Easy to add new statistics

### Parallel Execution Architecture

Parallel mode uses `ExecutorService`:

```java
ExecutorService executor = Executors.newFixedThreadPool(numCores);
List<Future<TrialResult<T>>> futures = new ArrayList<>();

for (int i = 0; i < numTrials; i++) {
    futures.add(executor.submit(() -> runSingleTrial()));
}

for (Future<TrialResult<T>> future : futures) {
    results.add(future.get()); // Collect results
}

executor.shutdown();
```

**Trade-offs:**
- ✅ Much faster for large trial counts
- ❌ Higher memory usage (concurrent trials)
- ❌ Non-deterministic without careful seeding

## Common Patterns

### Parameter Sweep

Vary a parameter systematically:

```java
int[] arraySizes = {50, 100, 200, 400, 800};
Map<Integer, Double> sizeToSteps = new HashMap<>();

for (int size : arraySizes) {
    Supplier<IntCell[]> factory = () -> createRandomCells(size);
    ExperimentConfig config = baseConfig.withArraySize(size);
    
    ExperimentResults<IntCell> results = runner.runBatchExperiments(config);
    sizeToSteps.put(size, results.getAverageSteps());
}

// Analyze scaling behavior
sizeToSteps.forEach((size, steps) -> 
    System.out.printf("n=%d: %.1f steps%n", size, steps));
```

### Convergence Rate Analysis

What percentage of trials converge?

```java
ExperimentResults<IntCell> results = runner.runBatchExperiments(config);

int converged = (int) results.getTrials().stream()
    .filter(TrialResult::isConverged)
    .count();
    
System.out.printf("%d / %d trials converged (%.1f%%)%n",
                  converged, results.getTrials().size(),
                  results.getConvergenceRate() * 100);
```

### Outlier Detection

Identify unusual trials:

```java
double mean = results.getAverageSteps();
double stdDev = results.getStdDevSteps();
double threshold = mean + 2 * stdDev; // 2 sigma

List<TrialResult<IntCell>> outliers = results.getTrials().stream()
    .filter(trial -> trial.getFinalStep() > threshold)
    .collect(Collectors.toList());
    
System.out.println("Outlier trials: " + outliers.size());
outliers.forEach(trial -> 
    System.out.printf("  Trial %d: %d steps%n", 
                      outliers.indexOf(trial), trial.getFinalStep()));
```

## Troubleshooting

### "Experiment never completes"

**Problem:** Parallel trials hanging.

**Check:**
1. Is max steps reasonable? (Try lower value first)
2. Are trials deadlocking? (Check for infinite loops)
3. Is thread pool exhausted? (Monitor system resources)

**Debug:** Run with `ExecutionMode.SEQUENTIAL` to isolate issue.

### "Out of memory error"

**Problem:** Too many trajectories recorded.

**Solutions:**
```java
// 1. Disable trajectory recording
config = config.withRecordTrajectories(false);

// 2. Reduce trials or array size
config = config.withNumTrials(50).withArraySize(50);

// 3. Increase heap size
// java -Xmx4G -jar experiment.jar
```

### "Results have huge variance"

**Problem:** High standard deviation in convergence steps.

**This may be expected** for certain configurations:
- Random initial states naturally vary
- Chimeric populations show more variance
- Larger arrays have more variation

**Mitigation:**
- Increase trial count for better statistics
- Use median instead of mean (robust to outliers)
- Check for implementation bugs

### "Tests disabled in this package"

**Status:** ExperimentRunner requires refactoring for lightweight cell architecture.

**Workaround:** Use experiment framework manually in your own code following the usage examples above.

## Next Steps

Now that you understand experiment design, proceed to:

**[Chapter 4: Analysis Tools](../analysis/README.md)** - Learn how to visualize and analyze trajectory data from experiments.

**Also see:**
- [Chapter 5: Validation](../validation/README.md) - System-level integration testing
- [Chapter 2: Metrics](../metrics/README.md) - Metrics used in experiments

---

**[← Back: Chimeric Populations](../chimeric/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Analysis Tools →](../analysis/README.md)**
