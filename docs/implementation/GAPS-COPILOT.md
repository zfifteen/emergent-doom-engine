# GAPS.md - Deviations from Levin Paper Methods

## Overview

This document systematically identifies all deviations between the Java implementation of the Emergent Doom Engine (EDE) and the methods described in the Levin et al. paper "Classical Sorting Algorithms as a Model of Morphogenesis: self-sorting arrays reveal unexpected competencies in a minimal model of basal intelligence" (arXiv:2401.05375v1).

**Status**: Partial implementation with functional core but several stub components.

---

## 1. IMPLEMENTATION LANGUAGE & CONCURRENCY

### Paper Methods
- **Language**: Python 3.0
- **Concurrency**: Multi-threaded with "cell threads" (one per cell) and a main thread
- **Execution Model**: Parallel cell execution where "each cell is a competent agent implementing local policies"
- **Repository**: https://github.com/Zhangtaining/cell_research

### Java Implementation  
- **Language**: Java 11+
- **Concurrency**: **NONE** - Sequential iteration through cells
- **Execution Model**: Single-threaded loop in `ExecutionEngine.step()`
- **Repository**: This repository (emergent-doom-engine)

### Impact
**CRITICAL DEVIATION**: The paper's core premise is "distributed architecture where each cell is a competent agent." The Java implementation uses traditional sequential iteration, fundamentally changing the execution model from concurrent/parallel to sequential deterministic. This affects:
- **Timing**: No race conditions, no emergent timing effects
- **Behavioral Dynamics**: Cells act in strict order, not simultaneously
- **Emergent Properties**: Some paper findings may not reproduce without parallelism

---

## 2. CELL-VIEW ALGORITHMS

### 2.1 Bubble Sort

#### Paper Definition
> "Cell-view Bubble Sort:
> 1. Each cell is able to view and swap with either its left or right neighbor.
> 2. Active cell moves to the left if its value is smaller than that of its left neighbor, or active cell moves to the right if its value is bigger than that of its right neighbor."

#### Java Implementation
```java
// ExecutionEngine.shouldSwapForAlgotype() - BUBBLE case
if (j == i - 1 && cells[i].compareTo(cells[j]) < 0) { 
    return true; // left neighbor, smaller value
} else if (j == i + 1 && cells[i].compareTo(cells[j]) > 0) { 
    return true; // right neighbor, bigger value
}
```

**Status**: ✅ **CORRECT** - Matches paper specification exactly.

---

### 2.2 Insertion Sort

#### Paper Definition
> "Cell-view Insertion Sort:
> 1. Each cell is able to view all cells to its left, and can swap only with its left neighbor.
> 2. Active cell moves to the left if cells to the left have been sorted, and if the value of the active cell is smaller than that of its left neighbor."

#### Java Implementation
```java
// ExecutionEngine.shouldSwapForAlgotype() - INSERTION case
if (j == i - 1 && isLeftSorted(i) && cells[i].compareTo(cells[j]) < 0) {
    return true;
}
// Note: neighbors include all left, but only swap with immediate left
```

**Status**: ✅ **CORRECT** - Implements both conditions:
1. Checks if left subarray is sorted (`isLeftSorted(i)`)
2. Only swaps with immediate left neighbor if value is smaller

**Minor Gap**: The paper says "Each cell is able to view **all cells to its left**" - the Java implementation returns all left indices in `InsertionTopology.getNeighbors()` but only evaluates swap with immediate left (`j == i - 1`). This is functionally equivalent to the paper's description.

---

### 2.3 Selection Sort

#### Paper Definition
> "Cell-view Selection Sort:
> 1. Each cell has an ideal target position to which it wants to move. The initial value of the ideal position for all the cells is the most left position.
> 2. Each cell can view and swap with the cell that currently occupies its ideal position.
> 3. If the value of the active cell is smaller than the value of the cell occupying the active cell's ideal target position, the active cell swaps places with that occupying cell."

#### Java Implementation
```java
// SelectionCell.java
private int idealPos = 0; // Initial value = 0 (leftmost)

// ExecutionEngine.shouldSwapForAlgotype() - SELECTION case
if (i == j) return false; // Guard: skip self-targeting

if (cells[i].compareTo(cells[j]) < 0) { 
    return true; // smaller than target
} else {
    // Swap denied: increment ideal position
    if (selCell.getIdealPos() < cells.length - 1) {
        selCell.incrementIdealPos();
    }
    return false;
}
```

**Status**: ⚠️ **PARTIAL DEVIATION**

#### Deviations:
1. **Missing Paper Specification**: The paper does not explicitly state what happens when a cell's value is **NOT** smaller than the target. The Java implementation adds a **"competition mechanism"** where the ideal position increments on failed swaps. This is an **emergent feature inspired by Levin's work but NOT in the paper's algorithm description.**

2. **Self-Targeting Guard**: The Java code includes `if (i == j) return false;` to prevent cells from swapping with themselves when `idealPos` equals their current position. The paper does not mention this edge case.

#### Impact:
- The "competition" mechanism (incrementing `idealPos` on failure) is a **novel addition** that may change behavior compared to paper.
- This could be considered an **enhancement** rather than a bug, as it prevents Selection cells from getting "stuck" targeting occupied positions.

---

## 3. FROZEN CELL MECHANICS

### Paper Definition
> "Frozen Cell: A cell that does not always move, even though the algorithm tells it to move (representing a damaged cell). There are two types:
> - A 'movable' Frozen Cell will not move on its own (will not initiate a move), but other cells are able to move it.
> - An immovable Frozen Cell can neither proactively move itself nor can it be moved by another cell."

### Java Implementation
```java
public enum FrozenType {
    NONE,       // Fully mobile
    MOVABLE,    // Can move but cannot be displaced
    IMMOVABLE   // Completely frozen
}
```

**Status**: ⚠️ **SEMANTIC INVERSION**

#### Critical Deviation:
The Java implementation **inverts** the semantics:
- Paper "movable" = **cannot initiate, CAN be displaced**
- Java `MOVABLE` = **CAN initiate, cannot be displaced**

The Java frozen mechanics:
```java
// FrozenCellStatus.java
public boolean canMove(int position) {
    FrozenType type = frozenTypes[position];
    return type == FrozenType.NONE || type == FrozenType.MOVABLE;
}

public boolean canBeDisplaced(int position) {
    FrozenType type = frozenTypes[position];
    return type == FrozenType.NONE; // Only NONE can be displaced
}
```

#### Impact:
**CRITICAL**: The frozen cell behavior is **backwards** from the paper. This will fundamentally change experimental results involving frozen cells. To match the paper:
- `MOVABLE` should mean: `canMove() = false`, `canBeDisplaced() = true`
- `IMMOVABLE` should mean: `canMove() = false`, `canBeDisplaced() = false`

---

## 4. METRICS

### 4.1 Monotonicity Error

#### Paper Definition
> "Monotonicity Error is the number of cells that violate the monotonic order and break the monotonicity of the cell array."
> 
> Formula: `ME = Σ (i=0 to n-2) [cells[i] ≥ cells[i+1] → 0] ∧ [cells[i] < cells[i+1] → 1]`

#### Java Implementation
```java
// MonotonicityError.java
public double compute(T[] cells) {
    int inversions = 0;
    for (int i = 0; i < cells.length - 1; i++) {
        if (cells[i].compareTo(cells[i + 1]) > 0) {
            inversions++;
        }
    }
    return inversions;
}
```

**Status**: ✅ **CORRECT** - Counts violations of increasing order.

---

### 4.2 Sortedness Value

#### Paper Definition
> "Sortedness Value is defined as the percentage of cells that strictly follow the designated sort order."
> 
> Formula: `Sortedness = (1 - ME / (n-1)) × 100%`

#### Java Implementation
**Status**: ❌ **MISSING** - Not implemented.

#### Impact:
The paper uses Sortedness as a primary metric for analyzing sorting trajectories (see Figures 3, 4, 9). This metric is absent from the Java implementation, making direct comparison of results impossible.

---

### 4.3 Delayed Gratification Index (DGI)

#### Paper Discussion
The paper describes **Delayed Gratification** as an **emergent behavior** where Sortedness temporarily decreases to navigate around frozen cells. The paper does **not provide an explicit formula** for quantifying DGI.

#### Java Implementation
```java
// DelayedGratificationIndex.java
public double compute(T[] cells) {
    return 0.0; // STUB - not implemented
}
```

**Status**: ⚠️ **STUB** - Returns placeholder value.

#### Impact:
The paper analyzes delayed gratification **qualitatively** by observing Sortedness trajectories (Figure 5). The Java implementation would need to:
1. Define a quantitative DGI formula (not in paper)
2. Analyze trajectory data from `Probe` to detect backtracking

**Missing Specification**: The paper does not provide a mathematical definition of how to compute DGI from trajectory data.

---

### 4.4 Aggregation Value

#### Paper Definition
> "Cell Aggregation: A metric of the degree to which the same type of cells cluster together (spatial proximity) during the sorting process when different Algotypes are mixed."

The paper uses this metric extensively in chimeric experiments (Figure 8) but does **not provide the calculation formula**.

#### Java Implementation
```java
// AggregationValue.java
public double compute(T[] cells) {
    return 0.0; // STUB - not implemented
}
```

**Status**: ⚠️ **STUB** - Returns placeholder value.

#### Impact:
The paper's **most surprising finding** is emergent algotype clustering (Section "Chimeric Arrays"). Without the aggregation metric formula from the original Python code, this phenomenon **cannot be replicated** in Java.

**Missing Specification**: The paper does not define how aggregation is calculated. The Python repository may contain the formula.

---

## 5. CONVERGENCE DETECTION

### Paper Methods
The paper describes experiments running "until it meets the stop condition" but does not explicitly define convergence criteria.

### Java Implementation
```java
// NoSwapConvergence.java
public boolean hasConverged(Probe<T> probe, int currentStep) {
    if (currentStep < stableSteps) return false;
    
    // Check if swaps = 0 for last 'stableSteps' steps
    for (int i = 0; i < stableSteps; i++) {
        StepSnapshot<T> snapshot = probe.getSnapshot(currentStep - i);
        if (snapshot.getSwapsPerformed() > 0) {
            return false;
        }
    }
    return true;
}
```

**Status**: ✅ **REASONABLE** - Assumes convergence when no swaps occur for N consecutive steps.

**Gap**: The paper does not specify convergence detection, so this is an **assumed implementation**.

---

## 6. TOPOLOGY / NEIGHBORHOODS

### Paper Methods
The paper describes neighborhoods implicitly through algotype rules:
- **Bubble**: "left or right neighbor"
- **Insertion**: "all cells to its left"
- **Selection**: "the cell that currently occupies its ideal position"

### Java Implementation
- **BubbleTopology**: Returns `[i-1, i+1]` (adjacent neighbors)
- **InsertionTopology**: Returns `[0, 1, ..., i-1]` (all left cells)
- **SelectionTopology**: Returns `[idealPos]` (dynamic target from `SelectionCell` state)

**Status**: ✅ **CORRECT** - Matches paper descriptions.

**Note**: The paper uses the term "topology" informally. The Java implementation formalizes this as `Topology` interface with `getNeighbors()` and `getIterationOrder()` methods.

---

## 7. CHIMERIC POPULATIONS

### Paper Methods
> "We performed experiments using two mixed Algotypes... We ensured that all 3 combinations start from similar Sortedness, ~50%."

The paper mixes cells with different algotypes (Bubble + Selection, etc.) and studies emergent aggregation.

### Java Implementation
```java
// ChimericPopulation.java
public static <T extends Cell<T>> T[] createPopulation(...) {
    return null; // STUB - not implemented
}

public static <T extends Cell<T>> int countAlgotype(...) {
    return 0; // STUB - not implemented
}
```

**Status**: ❌ **NOT IMPLEMENTED** - All methods are stubs.

#### Impact:
**CRITICAL**: The paper's most novel findings (emergent algotype clustering, cross-purpose chimeras, delayed gratification around frozen cells) involve chimeric populations. Without this, the Java implementation **cannot reproduce** these results.

---

## 8. TRAJECTORY ANALYSIS

### Paper Methods
The paper shows extensive trajectory visualizations:
- **Figure 3**: Sortedness vs. steps for different algorithms
- **Figure 4**: Comparison of traditional vs. cell-view algorithms
- **Figure 5**: Delayed gratification trajectories with frozen cells
- **Figure 8**: Aggregation values over time in chimeric arrays

### Java Implementation
```java
// TrajectoryAnalyzer.java - ALL STUBS
public List<Double> extractMetricOverTime(Probe<T> probe, Metric<T> metric) {
    return new ArrayList<>(); // STUB
}

public Map<String, List<Double>> compareAlgotypes(...) {
    return new HashMap<>(); // STUB
}
```

**Status**: ❌ **NOT IMPLEMENTED** - All analysis methods are stubs.

#### Impact:
The paper's conclusions are based on **trajectory analysis** of metrics over time. Without this, the Java implementation cannot:
- Generate figures equivalent to the paper
- Detect delayed gratification patterns
- Analyze convergence rates
- Compare algorithm efficiency

---

## 9. EXPERIMENT FRAMEWORK

### 9.1 Probe Recording

#### Paper Methods
> "The execution subsystem passes a Probe object to each experiment run, and the Probe is designed to record each step of the sorting process."

#### Java Implementation
```java
// Probe.java
public void recordSnapshot(int step, T[] cells, int swapsPerformed) {
    if (recordTrajectory) {
        T[] cellsCopy = Arrays.copyOf(cells, cells.length);
        snapshots.add(new StepSnapshot<>(step, cellsCopy, swapsPerformed));
    }
}
```

**Status**: ✅ **CORRECT** - Records complete state at each step.

---

### 9.2 Multi-Trial Experiments

#### Paper Methods
The paper reports statistics across multiple trials:
> "repeated 100 times"
> "average and standard deviation of the total steps"

#### Java Implementation
```java
// ExperimentRunner.java
public ExperimentResults<T> runExperiment(ExperimentConfig config, int numTrials) {
    List<TrialResult<T>> trialResults = new ArrayList<>();
    for (int i = 0; i < numTrials; i++) {
        // Run trial, collect results
    }
    return new ExperimentResults<>(trialResults);
}

// ExperimentResults.java
public String getSummaryReport() {
    // Calculates mean, std dev, min, max
}
```

**Status**: ✅ **CORRECT** - Implements multi-trial framework with statistics.

---

## 10. DOMAIN-SPECIFIC IMPLEMENTATIONS

### 10.1 Factorization Example

#### Paper Domain
The paper focuses on **generic integer sorting** with random initial arrays:
> "Each sorting process starts from a randomized array of cells"

#### Java Domain
The Java implementation includes a **factorization-specific** example (`RemainderCell`) that sorts by remainder values:
```java
public class RemainderCell implements Cell<RemainderCell> {
    private final BigInteger n;           // Target number
    private final BigInteger position;    // Potential factor
    
    public int compareTo(RemainderCell other) {
        // Sort by remainder: n mod position
    }
}
```

**Status**: ✅ **DOMAIN EXTENSION** - Not a gap, but an **additional application domain** beyond the paper.

**Note**: This demonstrates the "domain-agnostic substrate" design principle but is not part of the paper's methods.

---

## 11. MISSING PAPER ELEMENTS

The following paper features have **no Java equivalent**:

### 11.1 Traditional (Top-Down) Algorithms
The paper compares cell-view algorithms to traditional centralized implementations:
> "We evaluated the Sortedness of the input array... two of the three cell-view algorithms are actually significantly more efficient as distributed agents than as classical top-down algorithms."

**Java Implementation**: Does **not** include traditional algorithm implementations for comparison.

**Impact**: Cannot reproduce efficiency comparisons (Figure 4).

---

### 11.2 Duplicate Values Experiments
The paper tests chimeric arrays with repeated cell values:
> "we performed similar experiments... but allowed assignment of duplicated values to cells (100 cells with values ranging from 1 to 10)"

**Java Implementation**: No explicit support for duplicate value experiments.

**Impact**: Cannot reproduce Figure 8D/E (persistent aggregation with duplicates).

---

### 11.3 Cross-Purpose Chimeras
The paper tests algotypes with **conflicting goals** (increasing vs. decreasing):
> "we performed experiments using two mixed Algotypes, where one was made to sort in *decreasing* order while the other sorted in *increasing* order"

**Java Implementation**: No support for cells with opposite sort directions.

**Impact**: Cannot reproduce Figure 9 (conflicting goal dynamics).

---

### 11.4 File I/O for Trajectory Data
The paper mentions:
> "After the sorting process ends, the information collected by the Probe is stored as a .npy file."

**Java Implementation**: No file output for trajectories (only in-memory `Probe` storage).

**Impact**: Cannot persist/reload experiment data for post-hoc analysis.

---

## 12. ADDITIONAL JAVA FEATURES NOT IN PAPER

The Java implementation includes several **enhancements** beyond the paper:

### 12.1 Generic Type System
```java
public interface Cell<T extends Cell<T>> extends Comparable<T>
```
**Benefit**: Type-safe implementation, prevents runtime type errors.

### 12.2 Formal Topology Abstraction
```java
public interface Topology<T extends Cell<T>> {
    List<Integer> getNeighbors(int position, int arraySize);
    List<Integer> getIterationOrder(int arraySize);
}
```
**Benefit**: Extensible to new neighborhood structures (grid, graph, etc.).

### 12.3 Immutable Snapshots
```java
public class StepSnapshot<T extends Cell<T>> {
    private final int step;
    private final T[] cellState;
    private final int swapsPerformed;
    // No setters - immutable
}
```
**Benefit**: Thread-safe trajectory data (if concurrency added later).

### 12.4 Builder-Style Configuration
```java
ExperimentConfig config = new ExperimentConfig(
    arraySize, maxSteps, stableSteps, recordTrajectory
);
```
**Benefit**: Clear, validated configuration (paper uses informal Python scripts).

---

## 13. SUMMARY OF CRITICAL GAPS

### High-Priority Gaps (Block Paper Replication)
1. ❌ **Concurrency Model**: Sequential execution vs. paper's multi-threaded parallelism
2. ❌ **Frozen Cell Semantics**: Inverted meaning of "movable" vs. paper
3. ❌ **Sortedness Metric**: Missing primary evaluation metric
4. ❌ **Aggregation Formula**: Missing calculation for chimeric clustering
5. ❌ **Chimeric Population Creation**: Stub implementation, cannot create mixed algotype arrays
6. ❌ **Trajectory Analysis**: Stub implementation, cannot generate paper-style figures
7. ❌ **Delayed Gratification Detection**: No quantitative measure (paper is qualitative)

### Medium-Priority Gaps (Limit Full Validation)
8. ⚠️ **Selection Sort Competition**: Added `incrementIdealPos()` mechanism not in paper
9. ⚠️ **Convergence Criterion**: Assumed "no swaps" rule (paper unspecified)
10. ⚠️ **Duplicate Values**: No explicit support for repeated cell values experiments
11. ⚠️ **Cross-Purpose Chimeras**: No support for conflicting sort directions

### Low-Priority Gaps (Minor Differences)
12. ✓ **Traditional Algorithms**: Absent, but not critical for core cell-view validation
13. ✓ **File I/O**: Absent, but in-memory `Probe` is functionally equivalent
14. ✓ **Language/Platform**: Python vs. Java (expected difference)

---

## 14. RECOMMENDATIONS FOR ALIGNMENT

To bring the Java implementation into full alignment with the paper:

### Immediate Actions
1. **Fix Frozen Cell Semantics**: Redefine `MOVABLE` to match paper (cannot initiate, can be displaced)
2. **Implement Sortedness Metric**: Add to `MonotonicityError` or create `SortednessValue` class
3. **Define Aggregation Formula**: Reverse-engineer from Python repo or derive from paper description
4. **Remove Selection Competition**: Make `idealPos` increment optional/configurable

### Short-Term Extensions
5. **Add Concurrency**: Implement multi-threaded cell execution (major architectural change)
6. **Implement ChimericPopulation**: Create mixed algotype arrays per paper specifications
7. **Implement TrajectoryAnalyzer**: Extract metric time-series from `Probe` data
8. **Add Delayed Gratification Detector**: Analyze Sortedness backtracking in trajectories

### Long-Term Research Extensions
9. **Add Traditional Algorithms**: Implement centralized Bubble/Insertion/Selection for comparison
10. **Support Duplicate Values**: Allow chimeric experiments with repeated cell values
11. **Add Cross-Purpose Mode**: Enable cells with opposite sort directions
12. **Output Visualization**: Generate CSV/JSON for plotting (equivalent to .npy files)

---

## 15. CONCLUSION

The Java implementation represents a **partial, architecturally divergent** realization of the Levin paper's methods. While the **core cell-view algorithm logic** (Bubble, Insertion, Selection) is **correctly implemented**, several **critical deviations** prevent full replication:

- **Sequential vs. Parallel Execution**: Fundamentally different computational model
- **Inverted Frozen Cell Semantics**: Will produce incorrect results
- **Missing Key Metrics**: Cannot measure Sortedness or Aggregation per paper
- **Stub Chimeric Support**: Cannot reproduce paper's most novel findings

**Overall Assessment**: The implementation demonstrates the **conceptual framework** of emergent cell-based sorting but requires significant work to match the paper's experimental capabilities.

**Functional Status**: 
- ✅ Core engine works (sorts arrays correctly)
- ✅ Single-algotype experiments functional
- ⚠️ Frozen cell experiments will behave incorrectly
- ❌ Chimeric experiments non-functional
- ❌ Trajectory analysis non-functional
- ❌ Paper-equivalent validation impossible without metric implementations

---

**Document Version**: 1.0  
**Date**: 2025-01-XX  
**Paper Reference**: Zhang, T., Goldstein, A., Levin, M. (2024). arXiv:2401.05375v1  
**Java Implementation**: emergent-doom-engine v1.0.0-SNAPSHOT
