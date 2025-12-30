# Implementation Gap Analysis: Emergent Doom Engine vs. Levin Paper

**Paper:** Zhang, T., Goldstein, A., & Levin, M. (2024). *Classical Sorting Algorithms as a Model of Morphogenesis: self-sorting arrays reveal unexpected competencies in a minimal model of basal intelligence*

**Implementation:** Java Emergent Doom Engine (this repository)

**Analysis Date:** 2025-12-30

---

## Executive Summary

This document catalogs deviations between the Java implementation and the methods described in the Levin paper. Each gap is rated by severity:

| Rating | Description |
|--------|-------------|
| **MISSING** | Feature described in paper has no implementation |
| **STUB** | Code structure exists but returns placeholder values |
| **PARTIAL** | Feature partially implemented, missing key aspects |
| **DEVIATION** | Implementation differs from paper's methodology |

### Gap Count by Category

| Category | MISSING | STUB | PARTIAL | DEVIATION | Total |
|----------|---------|------|---------|-----------|-------|
| Metrics | 0 | 1 | 0 | 0 | 1 |
| Chimeric Populations | 2 | 2 | 0 | 0 | 4 |
| Concurrency Model | 0 | 0 | 0 | 0 | 0 |
| Traditional Algorithms | 2 | 0 | 0 | 0 | 2 |
| Trajectory Analysis | 0 | 0 | 0 | 0 | 0 |
| Statistical Analysis | 2 | 0 | 1 | 0 | 3 |
| Algorithm Specifics | 0 | 0 | 0 | 1 | 1 |
| Visualization/Output | 3 | 0 | 0 | 0 | 3 |
| **TOTAL** | **9** | **3** | **1** | **1** | **14** |

---

## Category 1: Metrics

### 1.1 Sortedness Value [IMPLEMENTED ✓]

**Paper Definition (p.8):**
> "Sortedness Value is defined as the percentage of cells that strictly follow the designated sort order (either increasing or decreasing). For example, if the array were completely sorted, the Sortedness Value would be 100%."

**Implementation Status:** Correctly implemented as percentage of cells in correct sorted position.

**Code Location:** `src/main/java/com/emergent/doom/metrics/SortednessValue.java`

**Verification:** Implementation matches paper definition. Creates sorted reference array and counts cells in correct final position. Formula: (cells in correct position / total cells) × 100.

---

### 1.2 Delayed Gratification Index [IMPLEMENTED ✓]

**Paper Definition (p.8):**
> "Delayed Gratification is used to evaluate the ability of each algorithm undertake actions that temporarily increase Monotonicity Error in order to achieve gains later on. Delayed Gratification is defined as the improvement in Sortedness made by a temporarily error increasing action. The total Sortedness change after a consecutive Sortedness value's increasing is ΔS_increasing. The total Sortedness change after the consecutive Sortedness value decreasing starting from last peak is ΔS_decreasing."

**Paper Formula:**
```
DG = ΔS_increasing / ΔS_decreasing
```

**Implementation Status:** Implemented via trajectory-based calculation in `DelayedGratificationCalculator`.

**Code Location:** `src/main/java/com/emergent/doom/metrics/DelayedGratificationCalculator.java`

**Verification:** Implementation correctly:
1. Analyzes sequence of Sortedness values from trajectory
2. Identifies peaks (start of consecutive decreases) and troughs (end of decreases)
3. Calculates ratio of recovery gains (ΔS_increasing) to setbacks (ΔS_decreasing)
4. Sums DG values across all events in a trajectory

**Note:** The original `DelayedGratificationIndex.java` (snapshot-based) remains as a stub. Use `DelayedGratificationCalculator` with `TrajectoryAnalyzer.computeMetricTrajectory()` for trajectory-based DG calculation per the paper.

---

### 1.3 Aggregation Value [STUB]

**Paper Definition (p.8-9):**
> "In sorting experiments with mixed Algotypes, we measured the extent to which cells of the same Algotype aggregated together (spatially) within the array. We defined Aggregation Value as the percentage of cells with directly adjacent neighboring cells that were all the same Algotype."

**Implementation Status:** Returns `0.0` unconditionally.

**Code Location:** `src/main/java/com/emergent/doom/metrics/AggregationValue.java:48`
```java
@Override
public double compute(T[] cells) {
    // Implementation will go here
    return 0.0;  // <-- STUB
}
```

**Recommendation:** Implement as:
```java
int matchingNeighbors = 0;
int totalNeighborPairs = 0;
for (int i = 0; i < cells.length - 1; i++) {
    if (cells[i].getAlgotype() == cells[i + 1].getAlgotype()) {
        matchingNeighbors++;
    }
    totalNeighborPairs++;
}
return (double) matchingNeighbors / totalNeighborPairs;
```

---

### 1.4 Monotonicity Error [IMPLEMENTED ✓]

**Paper Definition (p.8):**
> "Monotonicity is the measurement of how well the cells followed monotonic order (either increasing or decreasing). The monotonicity error is the number of cells that violate the monotonic order and break the monotonicity of the cell array."

**Implementation Status:** Correctly implemented as inversion counting.

**Code Location:** `src/main/java/com/emergent/doom/metrics/MonotonicityError.java:20-33`

**Verification:** Implementation matches paper definition.

---

## Category 2: Chimeric Population Support

### 2.1 Population Creation [STUB]

**Paper Context (p.11-12):**
> "At the beginning of these experiments, we randomly assigned one of the three different Algotypes to each of the cells, and began the sort as previously, allowing all the cells to move based on their Algotype."

**Implementation Status:** Returns `null`.

**Code Location:** `src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java:51-54`
```java
public T[] createPopulation(int size, Class<T> cellClass) {
    // Implementation will go here
    return null;  // <-- STUB
}
```

**Recommendation:** Implement using provided `CellFactory<T>` and `AlgotypeProvider` interfaces.

---

### 2.2 Algotype Counting [STUB]

**Implementation Status:** Returns `0`.

**Code Location:** `src/main/java/com/emergent/doom/chimeric/ChimericPopulation.java:69-72`
```java
public int countAlgotype(T[] cells, String algotype) {
    // Implementation will go here
    return 0;  // <-- STUB
}
```

---

### 2.3 Cross-Purpose Sorting [MISSING]

**Paper Description (p.14):**
> "what happens when the two different Algotypes are at cross-purposes—that is, they do not have the same goal? ... we performed experiments using two mixed Algotypes, where one was made to sort in *decreasing* order while the other sorted in *increasing* order."

**Implementation Status:** No support for cells with different sort directions.

**Code Location:** `Cell` interface has no sort direction property.

**Recommendation:** Add `getSortDirection()` method to `Cell` interface or create `DirectedCell` extension.

---

### 2.4 Duplicate Values Experiments [MISSING]

**Paper Description (p.13):**
> "we performed similar experiments as above but allowed assignment of duplicated values to cells (100 cells with values ranging from 1 to 10, guaranteeing duplicated occurrences of 10 cells for each value randomly distributed in the initial string)."

**Implementation Status:** Not explicitly tested or documented.

**Recommendation:** Add experiment configuration and tests for duplicate-value scenarios.

---

## Category 3: Concurrency Model

### 3.1 Multi-Threaded Cell Execution [IMPLEMENTED]

**Paper Description (p.7):**
> "We used multi-thread programming to implement the cell-view sorting algorithms. 2 types of threads were involved during the sorting process: cell threads are used to represent all cells, with each cell represented by a single thread; a main thread is used to activate all the threads and monitor the sorting process."

**Implementation Status:** IMPLEMENTED - One thread per cell with CyclicBarrier synchronization.

**Implementation Files:**
- `src/main/java/com/emergent/doom/execution/ParallelExecutionEngine.java` - Main parallel engine
- `src/main/java/com/emergent/doom/execution/CellThread.java` - Per-cell thread runnable
- `src/main/java/com/emergent/doom/execution/CellEvaluator.java` - Cell evaluation interface
- `src/main/java/com/emergent/doom/execution/ExecutionMode.java` - SEQUENTIAL/PARALLEL enum
- `src/main/java/com/emergent/doom/swap/ConcurrentSwapCollector.java` - Thread-safe proposal collector
- `src/main/java/com/emergent/doom/swap/SwapProposal.java` - Immutable swap proposal
- `src/main/java/com/emergent/doom/swap/ThreadSafeFrozenCellStatus.java` - Thread-safe frozen state
- `src/main/java/com/emergent/doom/probe/ThreadSafeProbe.java` - Thread-safe probe

**Architecture:**
```
Main Thread                    Cell Threads (N threads)
    |                               |
    +-- barrier.await() -----> All cells evaluate in parallel
    |                               |
    +-- barrier.await() <----- All cells submit SwapProposals
    |
    +-- Resolve conflicts (leftmost priority)
    +-- Execute non-conflicting swaps
    |
    +-- barrier.await() -----> Release for next step
```

**Usage:**
```java
// Enable parallel execution via ExperimentConfig
ExperimentConfig config = new ExperimentConfig(
    100,     // arraySize
    10000,   // maxSteps
    3,       // requiredStableSteps
    true,    // recordTrajectory
    ExecutionMode.PARALLEL  // <-- Enable parallel mode
);
```

---

### 3.2 Parallel Cell Activation [IMPLEMENTED]

**Paper Description (Figure 2 caption):**
> "All cells have a chance to move at each time step (parallel)."

**Implementation Status:** IMPLEMENTED - Configurable execution mode (SEQUENTIAL or PARALLEL).

**Details:**
- `ExecutionMode.SEQUENTIAL` - Original behavior, cells evaluated one at a time
- `ExecutionMode.PARALLEL` - All cells evaluate simultaneously with barrier sync
- `ExperimentRunner` automatically uses thread-safe components for parallel mode

---

## Category 4: Traditional Algorithm Comparison

### 4.1 Traditional Sorting Algorithms [MISSING]

**Paper Description (p.6-7):**
> Traditional algorithms described:
> - Bubble Sort (top-down controller)
> - Insertion Sort (top-down controller)
> - Selection Sort (top-down controller)

**Paper Context (p.10):**
> "We used the total steps that each algorithm needed to complete the sorting process for 100 elements in each experiment to indicate the efficiency of the algorithm... By repeating the experiments and doing the Z-test over the average steps, we calculated the efficiency difference between traditional sorting and cell-view sorting algorithms."

**Implementation Status:** Only cell-view algorithms implemented.

**Recommendation:** Create `TraditionalSortEngine` class with implementations of standard algorithms for comparison studies.

---

### 4.2 Dual Cost Model [MISSING]

**Paper Description (p.10):**
> "When we counted only swapping steps..." and "The situation changed when we considered both reading (comparison) and writing (swapping) as costly steps, simulating the metabolic cost of both measurements and actions."

**Implementation Status:** Only swap count tracked.

**Code Location:** `SwapEngine.java` tracks swap count only.

**Recommendation:** Add comparison counter to track read operations for complete cost analysis.

---

## Category 5: Trajectory Analysis

### 5.1 Metric Trajectory Computation [IMPLEMENTED ✓]

**Code Location:** `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java`

**Implementation Status:** Fully implemented - computes any metric over trajectory snapshots.

```java
public List<Double> computeMetricTrajectory(List<StepSnapshot<T>> snapshots, Metric<T> metric)
```

---

### 5.2 Swap Count Extraction [IMPLEMENTED ✓]

**Code Location:** `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java`

**Implementation Status:** Fully implemented - extracts swap counts from trajectory.

```java
public List<Integer> extractSwapCounts(List<StepSnapshot<T>> snapshots)
```

---

### 5.3 Convergence Detection [IMPLEMENTED ✓]

**Code Location:** `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java`

**Implementation Status:** Fully implemented - finds step where N consecutive zero-swap steps occurred.

```java
public int findConvergenceStep(List<StepSnapshot<T>> snapshots, int consecutiveZeroSwaps)
```

---

### 5.4 Trajectory Visualization [IMPLEMENTED ✓]

**Code Location:** `src/main/java/com/emergent/doom/analysis/TrajectoryAnalyzer.java`

**Implementation Status:** Fully implemented - generates text-based trajectory visualization.

```java
public String visualizeTrajectory(List<StepSnapshot<T>> snapshots, int maxSnapshotsToShow)
```

---

### 5.5 Trajectory File Export [MISSING]

**Paper Description (p.6):**
> "After the sorting process ends, the information collected by the Probe is stored as a .npy file."

**Implementation Status:** No file export capability.

**Recommendation:** Add JSON or CSV export for Java compatibility (or use jnumpy for .npy support).

---

## Category 6: Statistical Analysis

### 6.1 Z-Test [MISSING]

**Paper Description (p.9):**
> "We apply standard statistical hypothesis methods, Z-test and T-test, to evaluate the significance of the differences we report."

**Paper Usage Example (p.10):**
> "the Z-test statistical values comparing the efficiencies of Bubble and Insertion sort were 0.73 and 1.26 (p-values were 0.47 and 0.24 respectively)"

**Implementation Status:** No statistical testing implemented.

**Recommendation:** Add `StatisticalTests` utility class or integrate Apache Commons Math.

---

### 6.2 T-Test [MISSING]

Same as above.

---

### 6.3 Experiment Replication (N=100) [PARTIAL]

**Paper Description (p.10):**
> "By repeating the experiments" (consistently uses N=100 replicates)

**Implementation Status:** `ExperimentRunner` supports multi-trial execution but statistical analysis is incomplete.

**Code Location:** `src/main/java/com/emergent/doom/experiment/ExperimentResults.java`
- `getMeanMetric()` - implemented
- `getStdDevMetric()` - implemented
- Z-test, T-test, p-values - NOT implemented

---

## Category 7: Algorithm Specifics

### 7.1 Selection Sort `idealPos` Reset Behavior [DEVIATION]

**Paper Description (p.7-8):**
> "1. Each cell has an ideal target position to which it wants to move. The initial value of the ideal position for all the cells is the most left position.
> 2. Each cell can view and swap with the cell that currently occupies its ideal position.
> 3. If the value of the active cell is smaller than the value of the cell occupying the active cell's ideal target position, the active cell swaps places with that occupying cell."

**Paper Implication:** After a successful swap, behavior is not specified. Does `idealPos` reset?

**Implementation:** `idealPos` only increments on swap denial, never resets.

**Code Location:** `src/main/java/com/emergent/doom/execution/ExecutionEngine.java:166-173`
```java
} else {
    // Swap denied: increment ideal position if not at end
    if (cells[i] instanceof SelectionCell) {
        SelectionCell<?> selCell = (SelectionCell<?>) cells[i];
        if (selCell.getIdealPos() < cells.length - 1) {
            selCell.incrementIdealPos();
        }
    }
    return false;
}
```

**Impact:** Cells may "drift" to targeting far-right positions without mechanism to return to seeking position 0.

**Recommendation:** Clarify with paper authors or add configurable reset-on-swap behavior.

---

## Category 8: Visualization and Output

### 8.1 Sortedness Trajectory Plots [MISSING]

**Paper:** Figures 3A, 3B, 3C show Sortedness vs. Steps plots.

**Implementation Status:** No plotting capability.

---

### 8.2 Aggregation Timeline Plots [MISSING]

**Paper:** Figure 8 shows Aggregation Value over sorting process.

**Implementation Status:** No plotting capability.

---

### 8.3 Statistical Comparison Figures [MISSING]

**Paper:** Figures 4, 5, 7 show bar charts with error bars.

**Implementation Status:** No visualization output.

**Recommendation:** Consider JFreeChart integration or JSON export for external visualization.

---

## Implementation Priority Recommendations

### High Priority (Core Functionality)
1. ~~`SortednessValue` metric~~ - ✓ IMPLEMENTED
2. `AggregationValue.compute()` - Required for chimeric experiments
3. `ChimericPopulation.createPopulation()` - Required for chimeric experiments
4. ~~`TrajectoryAnalyzer` stub methods~~ - ✓ IMPLEMENTED (all 5 methods)

### Medium Priority (Enhanced Analysis)
5. ~~`DelayedGratificationCalculator`~~ - ✓ IMPLEMENTED (trajectory-based DG calculation)
6. Traditional algorithm implementations - Required for comparison studies
7. Statistical tests (Z-test, T-test) - Required for significance analysis

### Lower Priority (Extensions)
8. ~~Multi-threaded execution~~ - ✓ IMPLEMENTED (ParallelExecutionEngine)
9. Cross-purpose sorting support - Extended experiments
10. Visualization output - Presentation/publication support

---

## Verification Checklist

After implementing fixes, verify against paper:

- [ ] Bubble sort: bidirectional swapping with neighbors
- [ ] Insertion sort: left-prefix sorted check before swap
- [ ] Selection sort: ideal position targeting with increment on denial
- [ ] Frozen cells: MOVABLE vs IMMOVABLE behavior
- [x] Monotonicity Error: inversion count matches paper formula
- [x] Sortedness Value: percentage calculation correct
- [ ] Aggregation Value: neighbor-matching percentage
- [x] Delayed Gratification: trajectory-based calculation (DelayedGratificationCalculator)
- [x] Trajectory Analysis: all methods implemented (computeMetricTrajectory, extractSwapCounts, findConvergenceStep, visualizeTrajectory, getTotalExecutionTime)

---

*Generated by gap analysis comparing `docs/2401.05375v1.md` against Java implementation*
