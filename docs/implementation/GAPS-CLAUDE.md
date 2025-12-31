# Implementation Gap Analysis: Emergent Doom Engine vs. cell_research

**Ground Truth Reference:** `cell_research` Python implementation (Levin et al.)
**Paper Reference:** Zhang, T., Goldstein, A., & Levin, M. (2024). *Classical Sorting Algorithms as a Model of Morphogenesis*
**Implementation:** Java Emergent Doom Engine (this repository)
**Analysis Date:** 2025-12-31

> **IMPORTANT**: This gap analysis uses the `cell_research` Python codebase as the authoritative ground truth, NOT the paper alone. The paper descriptions sometimes differ from the actual code behavior.

---

## Executive Summary

| Rating | Description |
|--------|-------------|
| **MISSING** | Feature in cell_research has no EDE implementation |
| **PARTIAL** | Feature partially implemented, missing key aspects |
| **DEVIATION** | EDE implementation differs from cell_research behavior |
| **IMPLEMENTED** | Feature correctly matches cell_research |

### Gap Count by Category

| Category | MISSING | PARTIAL | DEVIATION | IMPLEMENTED | Total Gaps |
|----------|---------|---------|-----------|-------------|------------|
| Threading Model | 0 | 0 | 1 | 0 | 1 |
| Cell Algorithms | 0 | 1 | 1 | 1 | 2 |
| CellGroup System | 4 | 0 | 0 | 0 | 4 |
| Frozen Cells | 0 | 1 | 1 | 0 | 2 |
| Metrics/Probe | 0 | 1 | 0 | 4 | 1 |
| Chimeric Features | 1 | 0 | 0 | 1 | 1 |
| Statistical Analysis | 2 | 0 | 0 | 0 | 2 |
| Traditional Algorithms | 2 | 0 | 0 | 0 | 2 |
| Visualization | 3 | 0 | 0 | 0 | 3 |
| **TOTAL** | **12** | **3** | **3** | **6** | **18** |

---

## Category 1: Threading Model

### 1.1 Synchronization Mechanism [DEVIATION]

**cell_research behavior** (`MultiThreadCell.py:58-74`):
```python
def move(self):
    self.lock.acquire()    # Simple mutex
    # ... evaluation and swap ...
    self.lock.release()
```
- Uses single global `threading.Lock()`
- Each cell: acquire → evaluate → swap → release
- No explicit phase synchronization

**EDE implementation** (`ParallelExecutionEngine.java:50-175`):
```java
// Uses CyclicBarrier with N+1 parties
this.barrier = new CyclicBarrier(cells.length + 1);

public int step() {
    barrier.await();  // Release cells to evaluate
    barrier.await();  // Wait for all cells
    // Resolve conflicts
    // Execute swaps
    barrier.await();  // Release for next step
}
```

**Impact**: EDE has stricter synchronization (all cells complete evaluation before any swaps occur). cell_research allows cells to swap asynchronously as they acquire the lock.

**Recommendation**: Consider adding a `ExecutionMode.LOCK_BASED` option that uses ReentrantLock instead of CyclicBarrier to match cell_research exactly.

---

## Category 2: Cell Algorithms

### 2.1 BubbleSortCell Direction Selection [DEVIATION]

**cell_research behavior** (`BubbleSortCell.py:66-72`):
```python
# Random direction choice - NOT both!
check_right = random.random() < 0.5
if check_right:
    target_position = (pos[0] + vision, pos[1])
else:
    target_position = (pos[0] - vision, pos[1])
```
Each iteration, cell randomly picks ONE direction (50% chance).

**EDE implementation** (`ExecutionEngine.java:162-171`):
```java
case BUBBLE:
    // Checks BOTH directions
    if (j == i - 1 && cells[i].compareTo(cells[j]) < 0) {
        return true;  // left
    } else if (j == i + 1 && cells[i].compareTo(cells[j]) > 0) {
        return true;  // right
    }
```
EDE checks both neighbors and may swap with either.

**Impact**: Different swap patterns and potentially different convergence behavior.

**Fix Required**: Add randomization to BubbleTopology or ExecutionEngine to match cell_research.

---

### 2.2 SelectionCell `idealPos` Reset on Merge [PARTIAL]

**cell_research behavior** (`SelectionSortCell.py:77-81`):
```python
def update(self):
    # Called when group merges - reset ideal position
    if self.reverse_direction:
        self.ideal_position = self.right_boundary
    else:
        self.ideal_position = self.left_boundary
```

**EDE implementation**:
- `SelectionCell.java` has `setIdealPos()` method ✓
- But no `update()` method called on group merge because CellGroup doesn't exist
- `ParallelExecutionEngine.reset()` resets to 0, not to boundary

**Impact**: Without CellGroup merging, this is currently not triggered. Will need implementation when CellGroup is added.

---

### 2.3 InsertionCell Left-Sorted Check [IMPLEMENTED ✓]

**cell_research behavior** (`InsertionSortCell.py:68-83`):
```python
def is_enable_to_move(self):
    for i in range(left_boundary, current_position):
        if cells[i].status == FREEZE:
            prev = -1  # Skip frozen
            continue
        if cells[i].value < prev:
            return False
```

**EDE implementation** (`ExecutionEngine.java:108-115`):
```java
private boolean isLeftSorted(int i) {
    for (int k = 0; k < i - 1; k++) {
        if (cells[k].compareTo(cells[k + 1]) > 0) {
            return false;
        }
    }
    return true;
}
```

**Status**: Core logic implemented. Note: EDE doesn't skip frozen cells in the check (see Frozen Cells section).

---

## Category 3: CellGroup System [MAJOR MISSING]

The entire CellGroup hierarchical management system from cell_research is not implemented in EDE.

### 3.1 CellGroup Class [MISSING]

**cell_research** (`CellGroup.py:13-122`):
```python
class CellGroup(threading.Thread):
    group_id: int
    cells_in_group: list
    left_boundary_position: tuple
    right_boundary_position: tuple
    status: GroupStatus  # ACTIVE, MERGING, SLEEP, MERGED
    phase_period: int
    count_down: int
```

**EDE**: No equivalent class.

---

### 3.2 Group Sleep/Wake Cycles [MISSING]

**cell_research** (`CellGroup.py:81-101`):
```python
def change_status(self):
    count_down = phase_period
    if status == ACTIVE:
        status = SLEEP
        put_cells_to_sleep()
    elif status == SLEEP:
        status = ACTIVE
        awake_cells()
```

Cells have periodic inactive phases controlled by their group.

**EDE**: No sleep/wake mechanism.

---

### 3.3 Group Merging [MISSING]

**cell_research** (`CellGroup.py:55-73`):
```python
def merge_with_group(self, next_group):
    next_group.status = MERGED
    self.right_boundary_position = next_group.right_boundary_position
    self.cells_in_group.extend(next_group.cells_in_group)
    for cell in self.cells_in_group:
        cell.group = self
        cell.left_boundary = self.left_boundary_position
        cell.right_boundary = self.right_boundary_position
        cell.update()  # Reset idealPos for Selection cells
```

When adjacent groups are both sorted, they merge into one larger group.

**EDE**: No group merging.

---

### 3.4 Group Sorted Detection [MISSING]

**cell_research** (`CellGroup.py:37-44`):
```python
def is_group_sorted(self):
    prev_cell = cells[left_boundary[0]]
    for i in range(left_boundary[0], right_boundary[0] + 1):
        cell = cells[i]
        if cell.status in [SLEEP, MOVING] or cell.value < prev_cell.value:
            return False
        prev_cell = cell
    return True
```

**EDE**: No per-group sorted detection.

---

## Category 4: Frozen Cells

### 4.1 Frozen Cell Semantics [DEVIATION]

**cell_research** (`MultiThreadCell.py:7-14, 71-78`):
```python
class CellStatus(Enum):
    FREEZE = 7  # Single frozen state

def swap(self, target_position):
    if self.status == CellStatus.FREEZE:
        # Frozen cell cannot INITIATE swap
        status_probe.count_frozen_cell_attempt()
        return
    # But frozen cells CAN BE MOVED by active cells
```

**EDE** (`FrozenCellStatus.java:30-39`):
```java
public enum FrozenType {
    NONE,      // Fully mobile
    MOVABLE,   // Can move, cannot be displaced
    IMMOVABLE  // Cannot participate in swaps
}
```

**Differences**:
| Aspect | cell_research FREEZE | EDE MOVABLE | EDE IMMOVABLE |
|--------|---------------------|-------------|---------------|
| Can initiate swap | No | Yes | No |
| Can be moved by others | Yes | No | No |

**Impact**: EDE's MOVABLE is opposite of cell_research FREEZE semantics.

**Recommendation**: Align EDE FREEZE = cannot initiate but CAN be moved.

---

### 4.2 Frozen Cell Skip in Insertion isLeftSorted [PARTIAL]

**cell_research** (`InsertionSortCell.py:74-76`):
```python
if cells[i].status == FREEZE:
    prev = -1  # Reset comparison, skip frozen
    continue
```

**EDE** (`ExecutionEngine.java:108-115`):
Does not skip frozen cells in `isLeftSorted()` check.

**Impact**: Insertion cells may incorrectly wait if frozen cell breaks left-side order.

---

## Category 5: Metrics and Probe

### 5.1 StatusProbe Fields [PARTIAL]

**cell_research** (`StatusProbe.py:1-22`):
```python
class StatusProbe:
    sorting_steps = []           # Array snapshots
    swap_count = 0               # Total swaps
    compare_and_swap_count = 0   # Comparisons leading to swap decision
    cell_types = []              # Type distribution per step
    frozen_swap_attempts = 0     # Attempts to swap with frozen
```

**EDE** (`Probe.java`):
- ✓ `snapshots` (includes swapCount per step)
- ✗ `compare_and_swap_count` — NOT tracked
- ✗ `cell_types` — NOT tracked
- ✗ `frozen_swap_attempts` — NOT tracked

**Recommendation**: Add counters to Probe or create separate StatusProbe class.

---

### 5.2 SortednessValue [IMPLEMENTED ✓]

**Location**: `SortednessValue.java`
**Status**: Correctly implemented per paper and cell_research.

---

### 5.3 MonotonicityError [IMPLEMENTED ✓]

**Location**: `MonotonicityError.java`
**Status**: Correctly implemented.

---

### 5.4 AlgotypeAggregationIndex [IMPLEMENTED ✓]

**Location**: `AlgotypeAggregationIndex.java`
**Status**: Correctly implemented per cell_research.

---

### 5.5 DelayedGratificationCalculator [IMPLEMENTED ✓]

**Location**: `DelayedGratificationCalculator.java`
**Status**: Correctly implemented using trajectory analysis.

---

## Category 6: Chimeric Features

### 6.1 ChimericPopulation [IMPLEMENTED ✓]

**Location**: `ChimericPopulation.java`, `PercentageAlgotypeProvider.java`, `GenericCellFactory.java`
**Status**: Correctly implemented with percentage-based distribution.

---

### 6.2 Cross-Purpose Sorting (Conflicting Directions) [MISSING]

**cell_research**: Supports cells with different `reverse_direction` flags.

**Paper** (p.14):
> "we performed experiments using two mixed Algotypes, where one was made to sort in *decreasing* order while the other sorted in *increasing* order."

**EDE**: No per-cell sort direction. All cells sort in same direction.

**Recommendation**: Add `SortDirection` field to Cell interface or GenericCell.

---

## Category 7: Statistical Analysis

### 7.1 Z-Test / T-Test [MISSING]

**Paper** (p.9): Uses Z-test and T-test for significance analysis.

**EDE**: No statistical testing utilities.

**Recommendation**: Add `StatisticalTests` utility or integrate Apache Commons Math.

---

### 7.2 Batch Experiment Statistics [MISSING]

**cell_research**: Runs 100 experiments, computes mean, std dev, Z-scores, p-values.

**EDE** (`ExperimentResults.java`): Has `getMeanMetric()`, `getStdDevMetric()` but no Z/T-test.

---

## Category 8: Traditional Algorithms

### 8.1 Traditional Sorting Implementations [MISSING]

**Paper** (p.6-7, p.10): Compares cell-view vs traditional (top-down) algorithms.

**EDE**: Only has cell-view implementations.

**Recommendation**: Create `TraditionalSortEngine` for comparison studies.

---

### 8.2 Dual Cost Model (Swaps + Comparisons) [MISSING]

**Paper** (p.10): Analyzes efficiency counting "both reading (comparison) and writing (swapping)".

**EDE**: Only tracks swap count.

**Recommendation**: Add comparison counter to execution engine.

---

## Category 9: Visualization / Output

### 9.1 Sortedness Trajectory Plots [MISSING]

**Paper**: Figures 3A, 3B, 3C show sortedness vs steps.

**EDE**: No plotting capability.

---

### 9.2 Aggregation Timeline Plots [MISSING]

**Paper**: Figure 8 shows aggregation value over time.

---

### 9.3 File Export (.npy format) [MISSING]

**Paper** (p.6): "information collected by the Probe is stored as a .npy file"

**EDE**: No file export.

**Recommendation**: Add JSON or CSV export (or jnumpy for .npy compatibility).

---

## Implementation Priority Matrix

### Critical (Blocks Core Functionality)
| Gap | Impact | Effort |
|-----|--------|--------|
| 2.1 Bubble random direction | Different convergence behavior | Low |
| 4.1 Frozen cell semantics | Incorrect frozen behavior | Medium |

### High (Major Feature Gaps)
| Gap | Impact | Effort |
|-----|--------|--------|
| 3.1-3.4 CellGroup system | No hierarchical organization | High |
| 5.1 StatusProbe fields | Missing metrics | Low |
| 6.2 Cross-purpose sorting | Can't replicate paper experiments | Low |

### Medium (Enhanced Analysis)
| Gap | Impact | Effort |
|-----|--------|--------|
| 1.1 Lock-based threading option | Match cell_research exactly | Medium |
| 7.1-7.2 Statistical tests | No significance analysis | Low |
| 8.1-8.2 Traditional algorithms | No comparison baseline | Medium |

### Lower (Extensions)
| Gap | Impact | Effort |
|-----|--------|--------|
| 9.1-9.3 Visualization | No visual output | Medium |
| 4.2 Frozen skip in insertion | Edge case | Low |

---

## Verification Checklist

After implementing fixes, verify against cell_research:

### Algorithms
- [ ] Bubble: Random 50% left/right direction choice
- [ ] Selection: idealPos starts at left_boundary, resets on merge
- [ ] Insertion: isLeftSorted skips FREEZE cells

### Threading
- [ ] Optional lock-based mode matching cell_research
- [ ] Barrier mode for deterministic parallel execution

### CellGroup
- [ ] GroupStatus enum (ACTIVE, MERGING, SLEEP, MERGED)
- [ ] Sleep/wake cycles with phase_period
- [ ] Group merge when adjacent groups sorted
- [ ] Cell boundary updates on merge

### Frozen Cells
- [ ] FREEZE = cannot initiate, CAN be moved
- [ ] frozen_swap_attempts counter
- [ ] tried_to_swap_with_frozen flag per cell

### Metrics
- [ ] compare_and_swap_count
- [ ] cell_types[] distribution tracking
- [ ] frozen_swap_attempts

### Chimeric
- [ ] Per-cell sort direction (reverse_direction)

---

## Reference: cell_research File Map

| Component | File | Lines |
|-----------|------|-------|
| CellStatus enum | `MultiThreadCell.py` | 7-14 |
| MultiThreadCell | `MultiThreadCell.py` | 17-113 |
| swap() | `MultiThreadCell.py` | 71-98 |
| BubbleSortCell | `BubbleSortCell.py` | 8-75 |
| SelectionSortCell | `SelectionSortCell.py` | 8-100 |
| InsertionSortCell | `InsertionSortCell.py` | 8-101 |
| GroupStatus enum | `CellGroup.py` | 6-10 |
| CellGroup | `CellGroup.py` | 13-122 |
| StatusProbe | `StatusProbe.py` | 1-22 |

---

*Generated by comparing EDE Java implementation against cell_research Python ground truth*
