# Factor Reporting Fix - Implementation Summary
**Date:** January 1, 2026  
**Fix Type:** Option A - Store Final Cell Array  
**Status:** ✅ COMPLETED AND VERIFIED

---

## Problem Statement

The `FactorizationExperiment` was incorrectly reporting factor positions because it tried to extract positions from the snapshot after sorting. After sorting, the array indices no longer correspond to the original candidate factor positions.

**Example of the bug:**
- Target: 100,039 = 71 × 1,409
- After sorting, position 71 (the true factor) was at array index 1
- Code incorrectly reported "factor at position 2" (index 1 + 1)
- Should have reported "factor at position 71"

---

## Solution Implemented

**Option A:** Store the final cell array in `TrialResult` to enable direct access to domain-specific cell properties (like `position`).

### Architecture

```
Before Fix:
TrialResult → StepSnapshot → values (remainder only)
                          ❌ Position information lost!

After Fix:
TrialResult → finalCells[] → RemainderCell.getPosition() ✅
           → StepSnapshot → values (for metrics)
```

---

## Code Changes

### 1. Modified `TrialResult.java`

Added field to store final cell array:
```java
private final T[] finalCells;
```

Added overloaded constructor:
```java
public TrialResult(..., T[] finalCells) {
    // Store reference to final cell array
    this.finalCells = finalCells;
}
```

Added getter:
```java
public T[] getFinalCells() {
    return finalCells;
}
```

**Design decision:** Store reference (not copy) for memory efficiency. The cell array is immutable after experiment completion.

### 2. Modified `ExperimentRunner.java`

Pass final cell array to `TrialResult`:
```java
return new TrialResult<>(
    trialNumber,
    finalStep,
    converged,
    trajectory,
    metricValues,
    endTime - startTime,
    cells  // ← Store final cell array
);
```

### 3. Fixed `FactorizationExperiment.java`

#### Fixed `displayFactors()`:
```java
RemainderCell[] finalCells = lastTrial.getFinalCells();

for (int idx = 0; idx < finalCells.length; idx++) {
    RemainderCell cell = finalCells[idx];
    int position = cell.getPosition();  // ✅ Correct!
    BigInteger remainder = cell.getRemainder();
    
    if (position > 1 && remainder.equals(BigInteger.ZERO)) {
        // Report actual factor position
        factorPositions.add(position);
    }
}
```

#### Fixed `resultsContainFactor()`:
```java
for (TrialResult<RemainderCell> trial : results.getTrials()) {
    RemainderCell[] finalCells = trial.getFinalCells();
    for (RemainderCell cell : finalCells) {
        if (cell.getPosition() > 1 && 
            cell.getRemainder().equals(BigInteger.ZERO)) {
            return true;  // Found non-trivial factor
        }
    }
}
```

---

## Verification Results

### Before Fix
```
Factors found (remainder = 0), excluding trivial position 1:
----------------------------------------
  Found 1 factor positions: [2]  ❌ WRONG!
```

### After Fix
```
Factors found (remainder = 0), excluding trivial position 1:
------------------------------------------------------------
  Found 1 non-trivial factor(s): [71]  ✅ CORRECT!

  Factor details:
  Position 71 (array index 1): 71 × 1409 = 100039

  First 10 cells in sorted array:
    Index 0: position=1, remainder=0   ← Trivial factor
    Index 1: position=71, remainder=0  ← Non-trivial factor ✅
    Index 2: position=2, remainder=1
    ...
```

### Independent Verification
```
============================================================
VERIFICATION: Checking all positions 1-1000 for target=100039
============================================================
Actual factors in range [1, 1000]:
  Position 1: 1 × 100039 = 100039
  Position 71: 71 × 1409 = 100039

Non-trivial factors: [71]  ✅ MATCHES!
```

---

## Performance Impact

**Minimal impact:**
- Memory: One additional array reference per `TrialResult` (~8 bytes)
- CPU: No additional computation (just storing existing reference)
- Backward compatibility: Maintained via constructor overloading

**Metrics after fix:**
```
Trials:              5
Convergence Rate:    100.0%
Mean Steps:          1,151.60
Sortedness:          99.70% ± 0.67%
Monotonicity Error:  0.20 ± 0.45
```

(Metrics actually improved slightly due to random variation, showing no negative impact)

---

## Lessons Learned

### 1. Snapshots vs. Domain Objects

**Snapshots** (lightweight, generic):
- ✅ Good for metrics (sortedness, monotonicity)
- ✅ Good for visualization (trajectory plots)
- ❌ Lose domain-specific information after sorting

**Domain Objects** (full state):
- ✅ Preserve all domain-specific properties
- ✅ Enable post-hoc analysis
- ⚠️ Slightly higher memory cost

**Pattern:** Use both! Snapshots for metrics, domain objects for detailed analysis.

### 2. Importance of Verification

The bug was discovered through **independent verification**:
```java
verifyFactorsByBruteForce(target, arraySize);
```

This brute-force check:
- Exposed the discrepancy between reported and actual factors
- Provided ground truth for validation
- Should be standard practice for emergent systems

### 3. Index vs. Position Confusion

**Critical distinction:**
- **Index:** Current location in sorted array (0, 1, 2, ...)
- **Position:** Original candidate factor value (1, 71, 2, ...)

After sorting, these are NOT the same! Always trace back to domain objects when extracting results.

---

## Applicability to Other Domains

This fix establishes a **general pattern** for domain-specific experiments:

### When to Store Final Domain Objects

✅ **Yes, store final cells when:**
- Domain objects have properties not captured in snapshots
- Post-experiment analysis needs full object state
- Extraction logic would be complex/error-prone from snapshots alone

❌ **No, snapshots alone suffice when:**
- Only generic metrics needed (sortedness, swaps, convergence)
- Trajectory visualization is the primary goal
- Memory is extremely constrained

### Examples

**Other experiments that should use this pattern:**
1. **Clustering:** Need to extract `cluster_id` from cells
2. **Pathfinding:** Need to extract `path_coordinates` 
3. **Scheduling:** Need to extract `task_id` and `time_slot`
4. **Graph coloring:** Need to extract `node_id` and `color`

---

## Files Modified

```
src/main/java/com/emergent/doom/experiment/TrialResult.java
src/main/java/com/emergent/doom/experiment/ExperimentRunner.java
src/main/java/com/emergent/doom/examples/FactorizationExperiment.java
docs/findings/factorization_experiment_2026-01-01.md
```

---

## Testing Checklist

- [x] Code compiles without errors
- [x] Backward compatibility maintained (old constructor still works)
- [x] Factor reporting shows correct position (71, not 2)
- [x] Independent verification matches reported factors
- [x] Performance metrics unchanged or improved
- [x] Documentation updated

---

## Follow-Up Actions

1. ✅ Update documentation - DONE
2. ⏭️ Apply pattern to other domain experiments as needed
3. ⏭️ Consider adding `getFinalCells()` to standard experiment template
4. ⏭️ Add javadoc explaining when to store final cells vs. rely on snapshots

---

**Conclusion:** The fix is complete, verified, and establishes a reusable pattern for domain-specific result extraction in the Emergent Doom Engine.

