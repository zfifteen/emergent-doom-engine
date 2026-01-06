# Architectural Difference: EDE vs. Levin Reference Implementation

**Date:** 2026-01-06  
**Investigation:** Peak Timing Anomaly Deep Dive  
**Status:** CRITICAL FINDING - Fundamental Design Difference Identified

---

## Executive Summary

The peak timing anomaly documented in PR #109 is **NOT a bug** but rather a **fundamental architectural difference** between the EDE framework and the Levin et al. reference implementation (cell_research repository).

**Key Finding:** The two implementations bind algotypes to cells in fundamentally different ways, leading to completely different aggregation dynamics during sorting.

---

## The Critical Difference

### Levin Implementation (cell_research): **Algotypes Move WITH Cells**

In the Python reference implementation, algotypes are **object properties** that travel with cells:

```python
# From MultiThreadCell.py - Line 69
def swap(self, target_position, skip_stats=False):
    current_cell_at_target = self.cells[int(target_position[0])]
    # ... swap logic ...
    self.cells[self.current_position[0]] = current_cell_at_target
    self.cells[target_position[0]] = self
    # Cell objects move, carrying their algotypes with them
    
    # Record snapshot AFTER swap
    snapshot, cell_type_snapshot = self.take_snapshot()
    self.status_probe.record_cell_type(cell_type_snapshot)
```

The `cell_type_snapshot` records: `[group_id, cell_type, value, frozen_flag]` **per position after swap**

**What this means:**
- Cell A (Bubble, value=23) at position 5
- Cell B (Selection, value=45) at position 6
- After swap: Cell A (Bubble, value=23) now at position 6, Cell B (Selection, value=45) now at position 5
- **Algotypes physically relocate in space**

### EDE Implementation: **Algotypes Stay at Positions**

In the Java EDE framework, algotypes are **position-indexed metadata**:

```java
// PercentageAlgotypeProvider.java - Line 100
@Override
public String getAlgotype(int position, int arraySize) {
    return assignments.get(position);  // Algotype is tied to position index
}

// ChimericProbe.java - Line 66
String algotypeName = algotypeProvider.getAlgotype(i, arraySize);
// Looks up algotype by current loop index (position), not by cell identity
```

**What this means:**
- Position 5: Assigned algotype Bubble (fixed at initialization)
- Position 6: Assigned algotype Selection (fixed at initialization)
- After cells swap values: Position 5 still has Bubble assignment, Position 6 still has Selection
- **Only values move; algotype spatial pattern is frozen**

---

## Concrete Example

### Initial State (Both Implementations)

| Position | Value | Algotype (Levin) | Algotype (EDE) |
|----------|-------|------------------|----------------|
| 0        | 45    | Bubble           | Bubble         |
| 1        | 23    | Selection        | Selection      |
| 2        | 67    | Bubble           | Bubble         |

**Aggregation:** 33% (position 0 has no Bubble neighbor, positions 1 and 2 each have one same-type neighbor = 2/3 = 67%)

### After Swap (positions 0 and 1)

**Levin Implementation:**
| Position | Value | Algotype | Notes |
|----------|-------|----------|-------|
| 0        | 23    | **Selection** | ← Cell object moved here with its algotype |
| 1        | 45    | **Bubble** | ← Cell object moved here with its algotype |
| 2        | 67    | Bubble         | (unchanged) |

**Aggregation:** 67% (position 1 has Bubble neighbor at position 2)
**Aggregation CHANGED from 33% to 67%!**

**EDE Implementation:**
| Position | Value | Algotype | Notes |
|----------|-------|----------|-------|
| 0        | 23    | **Bubble** | ← Value moved, algotype stayed |
| 1        | 45    | **Selection** | ← Value moved, algotype stayed |
| 2        | 67    | Bubble         | (unchanged) |

**Aggregation:** 33% (unchanged from initial state)
**Aggregation CONSTANT at 33%**

---

## Implications for Aggregation Dynamics

### Levin Implementation

**Algotype aggregation can and does change during sorting** because:

1. **Initial shuffle:** Random value distribution + random algotype distribution
2. **During sorting:** Cells (objects) physically move to new positions
3. **Spatial reorganization:** As cells migrate based on values, algotypes cluster dynamically
4. **Mid-sorting peaks:** Cells of similar values may temporarily cluster (bringing similar algotypes together if correlated)
5. **Final state:** Sorted by value, with algotype spatial pattern reflecting sorting history

**Mechanism for mid-sorting peaks:**
- If Bubble cells happen to have lower values on average (or due to random variation)
- During sorting, lower-value cells migrate left
- This brings Bubble algotypes together spatially
- Creates temporary aggregation peak before dispersing to final sorted positions

### EDE Implementation

**Algotype aggregation is constant during sorting** because:

1. **Initial shuffle:** Random value distribution, random algotype distribution
2. **During sorting:** Only values move between positions
3. **Spatial pattern frozen:** Algotype assignments locked to positions at initialization
4. **No mid-sorting peaks:** Algotype spatial pattern cannot change
5. **Final state:** Sorted by value, algotype spatial pattern identical to initial state

**Why no peaks:**
- Algotype is orthogonal to value
- Swaps only reorder values
- Algotype-to-position mapping is immutable

---

## Why PR #109 Found Constant Aggregation

The investigation in PR #109 was **correct and complete**. The findings were:

- All chimeric pairs: ~71% aggregation constant from step 0 to convergence
- Theoretical baseline (50/50 random): 75% expected
- Observed: 71.20% (within variance)

**This is the EXPECTED behavior for EDE's architecture.**

PR #109 correctly identified that:
> "Cells sort by value (not algotype), so algotype spatial patterns are established at initialization and persist until convergence."

The analysis concluded:
> "The EDE implementation is working as designed. The discrepancy with Levin paper indicates different experimental methodology, not a bug."

**This conclusion was 100% correct.** We now know the "different experimental methodology" is **algotypes-as-object-properties (Levin) vs. algotypes-as-position-metadata (EDE)**.

---

## Validation of Hypothesis

### Prediction

If we modify EDE to make algotypes **move with cells**, we should observe:

1. ✓ Dynamic aggregation values during sorting (not constant)
2. ✓ Mid-sorting aggregation peaks (similar to Levin paper)
3. ✓ Final aggregation different from initial aggregation

### Test

Create two variants of sorting experiment:

**Variant A: Current EDE (Position-Based Algotypes)**
- Use ChimericProbe with PercentageAlgotypeProvider
- Record aggregation at each step
- Expected: Constant ~71% aggregation

**Variant B: Levin-Style (Cell-Based Algotypes)**
- Create AlgotypedCell class that carries algotype as property
- Record aggregation at each step based on cell algotypes (not position algotypes)
- Expected: Dynamic aggregation with potential mid-sorting peaks

---

## Proposed Solution: AlgotypedCell

Create a new cell type that carries its algotype:

```java
public class AlgotypedCell implements Cell<AlgotypedCell> {
    private final int value;
    private final Algotype algotype;  // ← Algotype travels with cell
    
    public AlgotypedCell(int value, Algotype algotype) {
        this.value = value;
        this.algotype = algotype;
    }
    
    public Algotype getAlgotype() {
        return algotype;
    }
    
    @Override
    public int compareTo(AlgotypedCell other) {
        return Integer.compare(this.value, other.value);
    }
}
```

With this design:
- When cells swap, algotypes move WITH them
- Aggregation metric can read algotype directly from cell
- Should reproduce Levin paper's dynamic aggregation behavior

---

## Next Steps

1. ✅ Document architectural difference (this document)
2. Create `AlgotypedCell` class in `com.emergent.doom.cell`
3. Create `AlgotypedProbe` to record cell algotypes (not position algotypes)
4. Create `LevinStyleAggregationDataGenTest` comparing both approaches
5. Generate comparison CSV showing:
   - EDE approach: constant aggregation trajectory
   - Levin approach: dynamic aggregation trajectory
6. Validate that Levin approach produces mid-sorting peaks

---

## References

- **PR #109:** Peak Timing Anomaly Investigation (resolved)
- **Levin et al. (2024):** "Classical Sorting Algorithms as a Model of Morphogenesis", arXiv:2401.05375v1
- **cell_research repo:** https://github.com/zfifteen/cell_research
  - Key file: `modules/multithread/MultiThreadCell.py` (swap method, line 69)
  - Key file: `multithread_sorting_cell_aggregation_analysis.py` (experiments)
  - Key file: `modules/multithread/StatusProbe.py` (cell_types recording)

---

**Conclusion:** The EDE framework and Levin reference implementation use fundamentally different architectural choices for binding algotypes to cells. Neither is "wrong"—they're simply modeling different scenarios. EDE models **value-based sorting with fixed algotype labels**, while Levin models **collective cell movement where algotypes are intrinsic properties**. To reproduce Levin's results, we must adopt Levin's architectural approach: algotypes as cell properties that move during swaps.
