# Chapter 2: Probe Recording

Probes provide **observability** into emergent computation by capturing execution trajectories. They record snapshots of cell states at each step, enabling post-hoc analysis, visualization, and debugging of emergent dynamics.

## Purpose

Tests in this package validate that the `Probe` correctly:
- Captures cell state snapshots at each execution step
- Tracks swap counts and frozen swap attempts
- Supports enabling/disabling recording for performance
- Maintains thread safety for parallel execution
- Preserves immutability of captured states

## Concepts Covered

### Execution Trajectory Recording
- **Snapshot**: Immutable copy of cell array state at a specific step
- **Step number**: Sequential identifier for temporal ordering
- **Swap count**: Number of successful swaps up to that step
- **Frozen attempts**: Number of blocked swaps due to frozen cells

### Observability Without Intrusion
- Probes observe but don't affect computation
- Recording can be disabled for production runs
- Captured data enables reproducibility and debugging

### Post-Hoc Analysis
- Complete trajectory history for visualization
- Metrics computed retroactively from snapshots
- Pattern detection in emergent behavior

### Thread Safety for Parallel Execution
- `ThreadSafeProbe` variant for concurrent engines
- Atomic counters prevent race conditions
- Snapshot immutability guarantees consistency

## Prerequisites

**Required:**
- [Chapter 1: Cell Foundations](../cell/README.md) - Cell interface and immutability
- [Chapter 2: Swap Mechanics](../swap/README.md) - Understanding of swap operations

**Helpful:**
- Concept of immutable data structures
- Observer pattern in software design

## Test Files

### ProbeTest.java

Comprehensive test suite for probe functionality.

**Test Categories:**

1. **StatusProbe Fields (Gap 5.1)**
   - Compare-and-swap counter tracking
   - Frozen swap attempt counting
   - Counter reset without clearing snapshots
   - Thread safety for concurrent increments

2. **Basic Snapshot Functionality**
   - Recording snapshots at each step
   - Retrieving snapshots by step number
   - Enabling/disabling recording
   - Snapshot count tracking

**Key Tests:**
- `compareAndSwapCount` starts at zero and increments correctly
- `frozenSwapAttempts` tracks blocked swaps
- `clear()` resets all counters and snapshots
- `resetCounters()` preserves snapshots while clearing counters
- `setRecordingEnabled(false)` prevents snapshot capture
- Thread safety under concurrent access

**Link to source:** [Probe.java](../../../../../main/java/com/emergent/doom/probe/Probe.java)

### StepSnapshot Structure

Tests the immutable snapshot representation:

**Key Properties:**
- Step number (temporal identifier)
- Cell state copy (immutable)
- Swap count at that step
- Metadata preservation

**Link to source:** [StepSnapshot.java](../../../../../main/java/com/emergent/doom/probe/StepSnapshot.java)

### ThreadSafeProbe

Tests thread-safe variant for parallel execution:

**Key Tests:**
- Concurrent snapshot recording
- Atomic counter updates
- Snapshot ordering guarantees

**Link to source:** [ThreadSafeProbe.java](../../../../../main/java/com/emergent/doom/probe/ThreadSafeProbe.java)

## Usage Examples

### Basic Snapshot Recording

Capture execution trajectory during sorting:

```java
// Create cells and probe
IntCell[] cells = { new IntCell(5), new IntCell(3), new IntCell(1) };
Probe<IntCell> probe = new Probe<>();
probe.setRecordingEnabled(true);

// Record initial state (step 0)
probe.recordSnapshot(0, cells, 0);

// Execute a swap and record
cells = swap(cells, 0, 1); // Swap positions 0 and 1
probe.recordSnapshot(1, cells, 1); // Step 1, 1 total swap

// Retrieve snapshots
StepSnapshot<IntCell> snapshot0 = probe.getSnapshot(0);
StepSnapshot<IntCell> snapshot1 = probe.getSnapshot(1);

System.out.println("Step 0 state: " + snapshot0.getCellsCopy());
System.out.println("Step 1 state: " + snapshot1.getCellsCopy());
System.out.println("Total snapshots: " + probe.getSnapshotCount()); // 2
```

**Key points:**
- Snapshots are immutable copies of cell state
- Step numbers enable temporal ordering
- Swap counts accumulate over trajectory

### Tracking Compare-and-Swap Operations

Monitor the number of comparison-based swaps:

```java
Probe<IntCell> probe = new Probe<>();

// During execution, record each compare-and-swap
for (int i = 0; i < cells.length - 1; i++) {
    if (cells[i].compareTo(cells[i + 1]) > 0) {
        swap(cells, i, i + 1);
        probe.recordCompareAndSwap();
    }
}

System.out.println("Total compare-and-swaps: " + 
                   probe.getCompareAndSwapCount());
```

**Key points:**
- Separate counter from snapshot recording
- Useful for algorithm analysis
- Tracks only successful swaps, not attempts

### Tracking Frozen Swap Attempts

Measure robustness by counting blocked swaps:

```java
Probe<IntCell> probe = new Probe<>();
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);

// Freeze a cell
frozenStatus.setFrozen(0, FrozenType.IMMUTABLE);

// Attempt swap involving frozen cell
boolean swapped = swapEngine.attemptSwap(cells, 0, 1);
if (!swapped) {
    probe.countFrozenSwapAttempt();
}

System.out.println("Frozen swap attempts: " + 
                   probe.getFrozenSwapAttempts());
```

**Key points:**
- Quantifies impact of frozen constraints
- Distinguishes blocked swaps from successful ones
- Informs robustness analysis

### Disabling Recording for Performance

Skip snapshot capture in production:

```java
Probe<IntCell> probe = new Probe<>();

// Disable recording for fast execution
probe.setRecordingEnabled(false);

// Run engine (no snapshots recorded)
engine.runUntilConvergence(10000);

// Counters still work
System.out.println("Swaps executed: " + probe.getCompareAndSwapCount());
System.out.println("Snapshots captured: " + probe.getSnapshotCount()); // 0
```

**Key points:**
- Reduces memory overhead
- Counters remain functional
- Re-enable recording for debugging

### Resetting Between Trials

Clear state for multi-trial experiments:

```java
Probe<IntCell> probe = new Probe<>();

// Run first trial
runTrial(probe);
int trial1Swaps = probe.getCompareAndSwapCount();
int trial1Snapshots = probe.getSnapshotCount();

// Clear everything for next trial
probe.clear();

// Run second trial (clean state)
runTrial(probe);
int trial2Swaps = probe.getCompareAndSwapCount();
```

**Key points:**
- `clear()` resets counters AND snapshots
- `resetCounters()` preserves snapshots for later analysis
- Choose based on whether you need historical data

## Architecture Insights

### Immutable Snapshots

Snapshots use **defensive copying** to prevent accidental mutation:

```java
public void recordSnapshot(int stepNumber, T[] cells, int swapCount) {
    // Create defensive copy - changes to original array don't affect snapshot
    T[] cellsCopy = Arrays.copyOf(cells, cells.length);
    snapshots.add(new StepSnapshot<>(stepNumber, cellsCopy, swapCount));
}
```

**Why this matters:**
- Captured state is frozen in time
- Original array can continue mutating
- Analysis sees consistent historical states

### Thread Safety Design

`ThreadSafeProbe` uses `AtomicInteger` for lock-free concurrency:

```java
private final AtomicInteger compareAndSwapCount = new AtomicInteger(0);

public void recordCompareAndSwap() {
    compareAndSwapCount.incrementAndGet(); // Atomic operation
}
```

**Why this matters:**
- Parallel engines can record without locks
- Eliminates contention bottlenecks
- Guarantees correct counts under concurrency

### Separation of Concerns

Probes follow the **Observer pattern**:
- **Subject**: Execution engine performs computation
- **Observer**: Probe records events
- **No coupling**: Engine doesn't know about probe internals

This enables:
- Multiple probes observing same execution
- Hot-swapping probes at runtime
- Testing engines without probes attached

## Common Patterns

### Trajectory Analysis

Compute metrics from captured snapshots:

```java
Probe<IntCell> probe = getProbeFromExperiment();

for (int step = 0; step < probe.getSnapshotCount(); step++) {
    StepSnapshot<IntCell> snapshot = probe.getSnapshot(step);
    IntCell[] cells = snapshot.getCellsCopy();
    
    double monotonicity = computeMonotonicity(cells);
    double sortedness = computeSortedness(cells);
    
    System.out.printf("Step %d: Mono=%.2f, Sort=%.2f%n",
                      step, monotonicity, sortedness);
}
```

**Use case:** Post-hoc analysis of convergence dynamics.

### Comparative Visualization

Compare trajectories from different algotypes:

```java
Probe<IntCell> bubbleProbe = runBubbleSort(cells);
Probe<IntCell> insertionProbe = runInsertionSort(cells);

// Compare step-by-step
for (int step = 0; step < Math.min(bubbleProbe.getSnapshotCount(), 
                                    insertionProbe.getSnapshotCount()); step++) {
    visualizeComparison(bubbleProbe.getSnapshot(step),
                        insertionProbe.getSnapshot(step));
}
```

**Use case:** Algorithm comparison and educational visualization.

### Debugging Non-Convergence

Identify where sorting gets stuck:

```java
Probe<IntCell> probe = getNonConvergingTrial();

for (int step = probe.getSnapshotCount() - 10; 
     step < probe.getSnapshotCount(); step++) {
    StepSnapshot<IntCell> snapshot = probe.getSnapshot(step);
    System.out.println("Step " + step + ": " + 
                       Arrays.toString(snapshot.getCellsCopy()));
}

// Look for repeating patterns or oscillations
```

**Use case:** Diagnosing convergence failures.

## Troubleshooting

### "Snapshot count is zero but swaps occurred"

**Problem:** Recording is disabled.

**Solution:** Enable recording before execution:
```java
probe.setRecordingEnabled(true);
```

### "Snapshot shows wrong values"

**Problem:** Snapshot captured reference, not copy.

**Check:** Ensure you're using `getCellsCopy()`, not direct array access:
```java
// ✅ Correct - returns immutable copy
IntCell[] state = snapshot.getCellsCopy();

// ❌ Wrong - might expose internal reference
IntCell[] state = snapshot.cells; // Don't do this
```

### "Counter values are inconsistent"

**Problem:** Using non-thread-safe probe with parallel execution.

**Solution:** Use `ThreadSafeProbe` for concurrent engines:
```java
// ❌ Wrong - race conditions
Probe<IntCell> probe = new Probe<>();

// ✅ Correct - thread-safe
Probe<IntCell> probe = new ThreadSafeProbe<>();
```

## Next Steps

Now that you understand trajectory recording, proceed to:

**[Chapter 2: Metrics](../metrics/README.md)** - Learn how to quantify emergent behavior using snapshots (Monotonicity, Sortedness, Delayed Gratification).

**Also see:**
- [Chapter 4: Analysis Tools](../analysis/README.md) - Advanced trajectory analysis and visualization
- [Chapter 3: Execution Engines](../execution/README.md) - How engines integrate with probes

---

**[← Back: Swap Mechanics](../swap/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Metrics →](../metrics/README.md)**
