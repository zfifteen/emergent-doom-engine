# Chapter 2: Metrics

Metrics quantify emergent computation by measuring **problem-space traversal**. They transform raw execution trajectories into interpretable measures of progress, disorder, and adaptive behavior inspired by the Levin et al. (2024) research.

## Purpose

Tests in this package validate that metrics correctly:
- Compute **Monotonicity** (disorder remaining in array)
- Compute **Sortedness** (progress toward goal state)
- Detect **Delayed Gratification** (temporary setbacks for long-term gain)
- Calculate **Spearman Distance** (rank correlation with ideal state)
- Match reference implementations from `cell_research` Python codebase

## Concepts Covered

### Problem-Space Navigation

Sorting is viewed as **traversal through a problem space** from initial disorder to final order:
- **Current state**: Array configuration at step N
- **Goal state**: Fully sorted array
- **Trajectory**: Sequence of states over time
- **Metrics**: Quantify position and movement in this space

### Emergent Competencies

Metrics reveal cognitive-like behaviors in minimal substrates:
- **Goal-directed behavior**: Progress toward sorted state (Sortedness)
- **Error tolerance**: System tolerates disorder (Monotonicity)
- **Adaptive navigation**: Temporary disorder increases to reach goals (Delayed Gratification)
- **Memory-like persistence**: Maintained progress despite perturbations

### Metric Types

1. **State metrics** (single snapshot): Monotonicity, Sortedness
2. **Trajectory metrics** (sequence of snapshots): Delayed Gratification
3. **Comparative metrics** (actual vs ideal): Spearman Distance

## Prerequisites

**Required:**
- [Chapter 1: Cell Foundations](../cell/README.md) - Understanding of Cell comparison
- [Chapter 2: Probe Recording](../probe/README.md) - Snapshot capture for trajectories

**Helpful:**
- Levin et al. (2024) Section 2.2 - "Problem Space Characterization"
- Basic statistics (percentages, ratios, correlation)

## Test Files

### MonotonicityTest.java

Tests the Monotonicity metric: percentage of correctly ordered adjacent pairs.

**Formula:** `Monotonicity = (correctly ordered pairs / total pairs) × 100`

**Key Tests:**
- Edge cases: null, empty, single element → 100%
- Sorted array [1,2,3,4,5] → 100%
- Reverse sorted [5,4,3,2,1] → 20% (first element always "correct")
- Partially monotonic [1,3,2,4,5] → 80%
- Reference implementation parity with Python `get_monotonicity()`

**Link to source:** [Monotonicity.java](../../../../../../main/java/com/emergent/doom/metrics/Monotonicity.java)

### SortednessValueTest.java

Tests the Sortedness metric: percentage of elements in ideal positions.

**Formula:** `Sortedness = (elements in final position / total elements) × 100`

**Key Tests:**
- Edge cases: null, empty, single element → 100%
- Fully sorted array → 100%
- Reverse sorted array → 0% or minimal (depends on duplicates)
- Partially sorted states
- Reference implementation parity with Python `get_sortedness()`

**Link to source:** [SortednessValue.java](../../../../../../main/java/com/emergent/doom/metrics/SortednessValue.java)

### DelayedGratificationCalculatorTest.java

Tests the Delayed Gratification (DG) metric: ratio of progress after temporary setbacks.

**Formula:** `DG = ΔS_increasing / ΔS_decreasing`

Where:
- `ΔS_increasing`: Total magnitude of Sortedness increases after decreases
- `ΔS_decreasing`: Total magnitude of Sortedness decreases

**Key Tests:**
- Edge cases: null, empty, < 3 values → 0.0
- Monotonic trajectories (always increasing) → 0.0
- Single dip-and-recovery pattern
- Multiple DG events in one trajectory
- Asymmetric recovery (partial vs complete)
- Reference implementation parity with Levin et al. definition

**Why it matters:** Reveals adaptive navigation - system accepts temporary disorder to reach better states.

**Link to source:** [DelayedGratificationCalculator.java](../../../../../../main/java/com/emergent/doom/metrics/DelayedGratificationCalculator.java)

### SpearmanDistanceTest.java

Tests Spearman Distance: rank correlation between current and ideal ordering.

**Formula:** `ρ = 1 - (6 Σd² / n(n²-1))` where `d` is rank difference

**Key Tests:**
- Perfect correlation (sorted) → 1.0
- Perfect anti-correlation (reverse sorted) → -1.0
- No correlation (random) → ≈0.0
- Handling of ties
- Edge cases: null, empty, single element

**Why it matters:** Domain-agnostic measure that doesn't require `getValue()` - works with any `Comparable`.

**Link to source:** [SpearmanDistance.java](../../../../../../main/java/com/emergent/doom/metrics/SpearmanDistance.java)

## Usage Examples

### Computing Monotonicity

Measure disorder remaining in an array:

```java
IntCell[] cells = { new IntCell(1), new IntCell(3), new IntCell(2), 
                    new IntCell(4), new IntCell(5) };
Monotonicity<IntCell> metric = new Monotonicity<>();

double monotonicity = metric.compute(cells);
System.out.println("Monotonicity: " + monotonicity + "%"); // 80% (4/5 pairs correct)
```

**Interpretation:**
- 100% = perfectly sorted
- 20% = reverse sorted (first element always "passes")
- Higher values = less disorder

### Computing Sortedness

Measure progress toward goal state:

```java
IntCell[] cells = { new IntCell(1), new IntCell(2), new IntCell(5), 
                    new IntCell(3), new IntCell(4) };
SortednessValue<IntCell> metric = new SortednessValue<>();

double sortedness = metric.compute(cells);
System.out.println("Sortedness: " + sortedness + "%"); // 40% (2/5 in final position)
```

**Interpretation:**
- 100% = fully sorted
- 0% = no elements in correct position
- Tracks actual convergence toward goal

### Detecting Delayed Gratification

Identify adaptive navigation in trajectories:

```java
// Sortedness trajectory: starts at 50%, dips to 40%, recovers to 70%
List<Double> sortednessTrajectory = Arrays.asList(50.0, 40.0, 70.0);

DelayedGratificationCalculator dgCalc = new DelayedGratificationCalculator();
double dg = dgCalc.calculate(sortednessTrajectory);
int events = dgCalc.countDGEvents(sortednessTrajectory);

System.out.println("DG ratio: " + dg);      // 3.0 (30 increase / 10 decrease)
System.out.println("DG events: " + events); // 1 event
```

**Interpretation:**
- DG = 0.0: No temporary setbacks (monotonic progress)
- DG > 0.0: System tolerates disorder for better outcomes
- Higher DG: More aggressive acceptance of temporary disorder

### Computing Spearman Correlation

Domain-agnostic ordering measure:

```java
// Works with ANY Comparable type - no getValue() needed
List<String> words = Arrays.asList("banana", "apple", "cherry");
SpearmanDistance metric = new SpearmanDistance();

double correlation = metric.compute(words);
System.out.println("Spearman ρ: " + correlation); // -0.5 (partial disorder)
```

**Interpretation:**
- ρ = 1.0: Perfect correlation (sorted)
- ρ = 0.0: No correlation (random)
- ρ = -1.0: Perfect anti-correlation (reverse sorted)

### Trajectory Analysis Pipeline

Combine metrics for comprehensive analysis:

```java
Probe<IntCell> probe = getProbeFromExperiment();
List<Double> monotonicityTraj = new ArrayList<>();
List<Double> sortednessTraj = new ArrayList<>();

Monotonicity<IntCell> monoMetric = new Monotonicity<>();
SortednessValue<IntCell> sortMetric = new SortednessValue<>();

// Compute metrics at each step
for (int step = 0; step < probe.getSnapshotCount(); step++) {
    IntCell[] cells = probe.getSnapshot(step).getCellsCopy();
    monotonicityTraj.add(monoMetric.compute(cells));
    sortednessTraj.add(sortMetric.compute(cells));
}

// Analyze trajectories
DelayedGratificationCalculator dgCalc = new DelayedGratificationCalculator();
double dg = dgCalc.calculate(sortednessTraj);

System.out.printf("Final Monotonicity: %.2f%%%n", 
                  monotonicityTraj.get(monotonicityTraj.size() - 1));
System.out.printf("Final Sortedness: %.2f%%%n", 
                  sortednessTraj.get(sortednessTraj.size() - 1));
System.out.printf("Delayed Gratification: %.2f%n", dg);
System.out.printf("DG Events: %d%n", dgCalc.countDGEvents(sortednessTraj));
```

## Architecture Insights

### Metric Interface

All metrics implement a common `Metric<T>` interface:

```java
public interface Metric<T extends Cell<T>> {
    double compute(T[] cells);
}
```

**Why this matters:**
- Uniform API for all state metrics
- Easy to add custom metrics
- Compatible with generic analysis pipelines

### Reference Implementation Validation

Tests validate against Python reference implementation from `cell_research`:

```java
// Python: get_monotonicity([1,2,3,4,5]) → 100.0
@Test
void sortedArray() {
    IntCell[] cells = createCells(1, 2, 3, 4, 5);
    assertEquals(100.0, metric.compute(cells), 0.01);
}
```

**Why this matters:**
- Ensures cross-language consistency
- Validates against peer-reviewed research implementation
- Catches numerical precision issues

### Delayed Gratification Detection Algorithm

The DG calculator uses a **three-point window** to detect dip-and-recovery:

```
Step:     0    1    2    3
Sortedness: 50 → 40 → 70 → 65
            ↓    ↓    ↑    ↓
Pattern:    -    dip  recover  (DG event!)
```

A DG event requires:
1. **Decrease**: Sortedness drops (step 0→1)
2. **Nadir**: Local minimum (step 1)
3. **Recovery**: Sortedness exceeds nadir (step 2)

**Why this matters:** Distinguishes intentional navigation from random fluctuation.

## Common Patterns

### Convergence Detection

Use Monotonicity to detect sorting completion:

```java
if (monoMetric.compute(cells) >= 99.0) {
    System.out.println("Nearly sorted - consider converged");
}
```

### Performance Comparison

Compare algotypes using trajectory metrics:

```java
double bubbleDG = getDG(bubbleProbe);
double insertionDG = getDG(insertionProbe);

System.out.printf("Bubble DG: %.2f%n", bubbleDG);
System.out.printf("Insertion DG: %.2f%n", insertionDG);

if (insertionDG > bubbleDG) {
    System.out.println("Insertion shows more adaptive navigation");
}
```

### Experimental Validation

Verify emergent competencies quantitatively:

```java
// Hypothesis: Chimeric arrays exhibit DG behavior
List<Double> chimericDG = measureChimericExperiments();
double avgDG = chimericDG.stream().mapToDouble(d -> d).average().orElse(0.0);

if (avgDG > 0.1) {
    System.out.println("Confirmed: Chimeric systems show delayed gratification");
}
```

## Troubleshooting

### "Monotonicity is always 100% or 20%"

**Problem:** Array is either fully sorted or fully reversed.

**Check:** Are you testing on intermediate states?
```java
// Run a few steps to get partial sorting
engine.step();
engine.step();
double mono = metric.compute(cells); // Should be between 20-100%
```

### "Sortedness never reaches 100%"

**Problem:** Convergence detection may be terminating early.

**Check:** Final state of array:
```java
System.out.println("Final state: " + Arrays.toString(cells));
// Verify it's actually sorted
```

### "DG is always 0.0"

**Problem:** Trajectory is monotonically increasing.

**This is expected** for some algorithms/inputs - not all executions exhibit DG.

**To see DG:**
- Use harder inputs (reverse sorted, random)
- Try chimeric populations
- Use larger arrays (more opportunities for navigation)

### "Spearman correlation is NaN"

**Problem:** Array has < 2 elements or all elements are identical.

**Solution:** Check array before computing:
```java
if (cells.length < 2) {
    return 1.0; // Degenerate case - assume perfect correlation
}
```

## Next Steps

Now that you understand how to quantify emergent behavior, proceed to:

**[Chapter 3: Execution Engines](../execution/README.md)** - Learn how engines orchestrate swaps and use metrics for convergence detection.

**Also see:**
- [Chapter 4: Analysis Tools](../analysis/README.md) - Advanced trajectory visualization using metrics
- [Chapter 4: Chimeric Populations](../chimeric/README.md) - Metrics for multi-algotype systems

---

**[← Back: Probe Recording](../probe/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Execution Engines →](../execution/README.md)**
