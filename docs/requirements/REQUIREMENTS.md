# Technical Requirements: Emergence Engine Implementation
## Based on "Classical Sorting Algorithms as a Model of Morphogenesis" (Levin et al., 2024)

**Target Audience**: Software engineers implementing cell-view sorting algorithms as emergence engines
**Paper Reference**: Zhang, T., Goldstein, A., Levin, M. (2024). arXiv:2401.05375v1
**Code Reference**: https://github.com/Zhangtaining/cell_research (Python 3.0 implementation)

> **IMPORTANT**: This document reflects the **actual implementation** from the `cell_research` Python codebase as the authoritative ground truth. Where the paper description differs from the code, the code behavior is documented as canonical.

---

## Document Conventions

Throughout this document:
- **[Paper p.X]** — Reference to the Levin et al. paper page number
- **[Code: filename.py:line]** — Reference to cell_research Python source

---

## 1. System Overview

### 1.1 Architecture Philosophy

The emergence engine implements a **distributed, agent-based sorting system** where each cell is an autonomous agent making local decisions based on its perspective of the environment.

**[Paper p.5-6]** Key Principles:
- **Bottom-up control**: No global controller; each cell acts independently
- **Local knowledge**: Cells only see their immediate neighbors or specific positions
- **Parallel execution**: All cells execute in their own threads
- **Emergent behavior**: System-level sorting emerges from local cell interactions
- **Unreliable substrate**: Support for "frozen" cells that fail to execute

### 1.2 Threading Architecture

**[Paper p.7]** describes two thread types. **[Code: MultiThreadCell.py, CellGroup.py]** implements:

```
┌─────────────────────────────────────────────────────────────────┐
│                        THREADING MODEL                          │
├─────────────────────────────────────────────────────────────────┤
│  Main Thread                                                    │
│    └── Starts all cell threads                                  │
│    └── Starts all group threads                                 │
│    └── Monitors for completion                                  │
│                                                                 │
│  Cell Threads (N threads, one per cell)                         │
│    └── Each extends threading.Thread                            │
│    └── Acquires shared lock → evaluates → swaps → releases      │
│    └── Runs until status == INACTIVE                            │
│                                                                 │
│  Group Threads (M threads, one per CellGroup)                   │
│    └── Manages sleep/wake cycles for cells                      │
│    └── Detects when group is sorted                             │
│    └── Merges with adjacent sorted groups                       │
└─────────────────────────────────────────────────────────────────┘
```

**CRITICAL**: The actual synchronization uses a **single global `threading.Lock()`**, NOT a barrier-based phase system. Each cell:
1. Acquires the lock
2. Evaluates its move condition
3. Executes swap if appropriate
4. Releases the lock

**[Code: BubbleSortCell.py:58-74]**:
```python
def move(self):
    self.lock.acquire()
    self.with_lock = True
    # ... evaluation and swap logic ...
    self.lock.release()
    self.with_lock = False
```

---

## 2. Core Data Structures

### 2.1 Position Format

**[Code: MultiThreadCell.py:38-39]** — Positions are **tuples**, not integers:

```python
self.current_position = current_position  # (x, y) tuple
self.target_position = current_position   # (x, y) tuple
```

For 1D sorting, `y` is always 1: `(index, 1)`

### 2.2 Cell Status Enumeration

**[Code: MultiThreadCell.py:7-14]**:

```python
class CellStatus(Enum):
    ACTIVE = 1      # Normal, can move
    SLEEP = 2       # Group is sleeping, cannot move
    MERGE = 3       # Group is merging (unused in current impl)
    MOVING = 4      # Animation in progress
    INACTIVE = 5    # Thread should terminate
    ERROR = 6       # Error state (unused)
    FREEZE = 7      # Frozen cell, cannot initiate swaps
```

### 2.3 MultiThreadCell Base Class

**[Code: MultiThreadCell.py:17-113]**:

```python
class MultiThreadCell(threading.Thread):
    # Core Properties
    threadID: int                    # Thread identifier
    value: int/float                 # Sortable value (immutable)
    current_position: tuple          # (x, y) current location
    target_position: tuple           # (x, y) destination during swap
    status: CellStatus               # Current operational status
    previous_status: CellStatus      # Status before MOVING

    # Threading
    lock: threading.Lock             # Shared global lock
    with_lock: bool                  # Currently holding lock

    # Boundaries (from CellGroup)
    left_boundary: tuple             # (x, y) leftmost position in group
    right_boundary: tuple            # (x, y) rightmost position in group
    group: CellGroup                 # Parent group reference

    # Algorithm-specific
    cell_vision: int = 1             # How far cell can see (always 1)
    ideal_position: tuple            # For SelectionSortCell only
    cell_type: str                   # 'Bubble', 'Selection', 'Insertion'
    label: int                       # 0=Bubble, 1=Selection, 2=Insertion
    reverse_direction: bool          # True = sort descending

    # Metrics tracking
    tried_to_swap_with_frozen: bool  # Tracks frozen interaction
    swapping_count: list             # [count] - shared mutable reference
    export_steps: list               # Snapshot history
    status_probe: StatusProbe        # Metrics collector

    # Global array reference
    cells: list                      # Reference to entire cell array
```

### 2.4 Cell Type Implementations

#### 2.4.1 BubbleSortCell

**[Paper p.7]** describes bidirectional neighbor comparison.
**[Code: BubbleSortCell.py]** — Actually uses **random direction selection**:

```python
class BubbleSortCell(MultiThreadCell):
    cell_vision = 1
    cell_type = 'Bubble'
```

**Key Behavior** — Random direction choice per iteration:
```python
# [Code: BubbleSortCell.py:66]
check_right = random.random() < 0.5  # 50% chance left, 50% right
```

#### 2.4.2 SelectionSortCell

**[Paper p.7-8]** describes ideal position targeting.
**[Code: SelectionSortCell.py]**:

```python
class SelectionSortCell(MultiThreadCell):
    cell_type = 'Selection'

    def __init__(self, ...):
        # Initial ideal position is leftmost (or rightmost if reverse)
        if self.reverse_direction:
            self.ideal_position = right_boundary
        else:
            self.ideal_position = left_boundary
```

#### 2.4.3 InsertionSortCell

**[Paper p.7]** describes left-prefix sorted checking.
**[Code: InsertionSortCell.py]**:

```python
class InsertionSortCell(MultiThreadCell):
    cell_vision = 1
    cell_type = 'Insertion'
    # Only moves LEFT, never right
```

### 2.5 CellGroup Class

**[Code: CellGroup.py]** — Hierarchical group management (not fully detailed in paper):

```python
class CellGroup(threading.Thread):
    group_id: int                      # Unique identifier
    cells_in_group: list               # Cells belonging to this group
    global_cells: list                 # Reference to all cells
    left_boundary_position: tuple      # (x, y) group start
    right_boundary_position: tuple     # (x, y) group end
    status: GroupStatus                # ACTIVE, MERGING, SLEEP, MERGED
    lock: threading.Lock               # Shared global lock
    phase_period: int                  # Sleep/wake cycle duration
    count_down: int                    # Current countdown to phase change
```

**GroupStatus Enumeration** **[Code: CellGroup.py:6-10]**:
```python
class GroupStatus(Enum):
    ACTIVE = 1      # Cells can move
    MERGING = 2     # Merging in progress
    SLEEP = 3       # All cells asleep
    MERGED = 4      # Group absorbed into another (terminal)
```

### 2.6 StatusProbe Class

**[Code: StatusProbe.py:1-22]** — Metrics collection:

```python
class StatusProbe:
    sorting_steps: list = []           # Array snapshots at each swap
    swap_count: int = 0                # Total swap operations
    compare_and_swap_count: int = 0    # Comparisons that led to swap decision
    cell_types: list = []              # Cell type distribution per step
    frozen_swap_attempts: int = 0      # Attempts to swap with frozen cells
```

**Recording Methods**:
- `record_swap()` — Increment swap_count
- `record_compare_and_swap()` — Increment comparison count
- `record_sorting_step(snapshot)` — Save array state
- `record_cell_type(snapshot)` — Save type distribution
- `count_frozen_cell_attempt()` — Track frozen interactions

---

## 3. Cell-View Sorting Algorithms

### 3.1 Cell-View Bubble Sort

**[Paper p.7]**: "Bubble sort... can swap with the cell next to it (either left or right)"

**[Code: BubbleSortCell.py:24-75]** — Actual implementation:

```
ALGORITHM: BubbleSortCell.move()

1. ACQUIRE global lock

2. UPDATE status if group is sleeping:
   IF group.status == SLEEP AND self.status != MOVING:
       self.status = SLEEP

3. RECORD comparison if should_move() returns True:
   IF should_move():
       status_probe.record_compare_and_swap()

4. RANDOMLY choose direction (NOT both!):
   check_right = random.random() < 0.5

   IF check_right:
       target = (current_position[0] + cell_vision, current_position[1])
   ELSE:
       target = (current_position[0] - cell_vision, current_position[1])

5. ATTEMPT swap if conditions met:
   IF should_move_to(target, check_right):
       swap(target)

6. RELEASE lock
```

**should_move()** **[Code: BubbleSortCell.py:24-42]**:
```
# For ascending sort (reverse_direction=False):
smaller_than_left = value < left_neighbor.value AND left_neighbor.status == ACTIVE
bigger_than_right = value > right_neighbor.value AND right_neighbor.status == ACTIVE
RETURN smaller_than_left OR bigger_than_right

# For descending sort (reverse_direction=True):
bigger_than_left = value > left_neighbor.value AND left_neighbor.status == ACTIVE
smaller_than_right = value < right_neighbor.value AND right_neighbor.status == ACTIVE
RETURN bigger_than_left OR smaller_than_right
```

**should_move_to(target, check_right)** **[Code: BubbleSortCell.py:44-56]**:
```
IF status != ACTIVE:
    RETURN False
IF NOT within_boundary(target):
    RETURN False
IF target_cell.status NOT IN [ACTIVE, FREEZE]:
    RETURN False

# Comparison logic (ascending):
IF check_right:
    RETURN value > target_cell.value  # Move right if bigger
ELSE:
    RETURN value < target_cell.value  # Move left if smaller
```

### 3.2 Cell-View Selection Sort

**[Paper p.7-8]**: Each cell has ideal target position, starts at leftmost.

**[Code: SelectionSortCell.py:31-98]**:

```
ALGORITHM: SelectionSortCell.move()

1. ACQUIRE global lock

2. UPDATE status if group sleeping

3. RECORD comparison if should_move()

4. CHECK should_move_to(ideal_position):

   # Handle frozen cell at ideal position:
   IF target is FREEZE:
       SHIFT ideal_position by 1
       IF value < frozen_cell.value:
           swap(target)  # Just to count frozen attempt
       RETURN False

   # Normal movement logic:
   IF status == ACTIVE AND within_boundary AND target is ACTIVE:
       IF value >= target_cell.value:
           # Swap denied: I'm not smaller, shift target
           ideal_position += 1
           RETURN False
       ELSE:
           RETURN True  # I'm smaller, can swap

5. IF should_move_to() returned True:
   IF cell_at_ideal.status == ACTIVE:
       swap(ideal_position)

6. RELEASE lock
```

**should_move()** **[Code: SelectionSortCell.py:31-32]**:
```
RETURN current_position != ideal_position AND within_boundary(ideal_position)
```

**update()** — Called after group merge **[Code: SelectionSortCell.py:77-81]**:
```
# Reset ideal position to group boundary
IF reverse_direction:
    ideal_position = right_boundary
ELSE:
    ideal_position = left_boundary
```

### 3.3 Cell-View Insertion Sort

**[Paper p.7]**: Check if left portion is sorted before moving left.

**[Code: InsertionSortCell.py:24-101]**:

```
ALGORITHM: InsertionSortCell.move()

1. ACQUIRE global lock

2. CHECK is_enable_to_move():
   IF NOT is_enable_to_move():
       RELEASE lock
       RETURN  # Wait for left side to sort

3. RECORD comparison if should_move()

4. UPDATE status if group sleeping

5. CALCULATE target (always LEFT):
   target = (current_position[0] - cell_vision, current_position[1])

6. IF should_move_to(target):
   swap(target)

7. RELEASE lock
```

**is_enable_to_move()** **[Code: InsertionSortCell.py:68-83]**:
```
# Check if all cells to the left are sorted
prev = -1  # (or 100000 if reverse_direction)

FOR i FROM left_boundary TO current_position - 1:
    IF cells[i].status == FREEZE:
        prev = -1  # Reset on frozen (skip it)
        CONTINUE

    IF cells[i].value < prev:  # (or > if reverse)
        RETURN False  # Left side not sorted yet

    prev = cells[i].value

RETURN True  # Left side is sorted, can move
```

**should_move_to(target)** **[Code: InsertionSortCell.py:40-61]**:
```
IF (status IN [ACTIVE, FREEZE]) AND within_boundary(target)
   AND target_cell.status IN [ACTIVE, FREEZE]:

    # Skip past consecutive frozen cells
    next_cell = target[0]
    WHILE next_cell < len(cells) AND cells[next_cell].status == FREEZE:
        next_cell += 1

    # Compare with target
    IF reverse_direction:
        RETURN value > target_cell.value
    ELSE:
        RETURN value < target_cell.value
```

---

## 4. Swap Operation

### 4.1 Atomic Swap

**[Code: MultiThreadCell.py:71-98]**:

```
ALGORITHM: swap(target_position, skip_stats=False)

1. GET cell at target position:
   target_cell = cells[target_position[0]]

2. CHECK if self is frozen:
   IF self.status == FREEZE:
       IF NOT tried_to_swap_with_frozen:
           status_probe.count_frozen_cell_attempt()
           tried_to_swap_with_frozen = True
       RETURN  # Cannot initiate swap

3. RESET frozen attempt flags:
   self.tried_to_swap_with_frozen = False
   target_cell.tried_to_swap_with_frozen = False

4. SET both cells to MOVING status:
   self.status = MOVING
   target_cell.status = MOVING

5. SWAP positions in array:
   cells[self.current_position[0]] = target_cell
   cells[target_position[0]] = self

6. UPDATE target positions:
   target_cell.target_position = self.current_position
   self.target_position = target_position

7. IF visualization disabled (instant mode):
   self.current_position = self.target_position
   target_cell.current_position = target_cell.target_position
   self.status = self.previous_status
   target_cell.status = target_cell.previous_status

8. RECORD metrics (unless skip_stats):
   swapping_count[0] += 1
   status_probe.record_swap()
   snapshot, cell_type_snapshot = take_snapshot()
   status_probe.record_sorting_step(snapshot)
   export_steps.append(snapshot)
   status_probe.record_cell_type(cell_type_snapshot)
```

### 4.2 Snapshot Format

**[Code: MultiThreadCell.py:61-62]**:

```python
def take_snapshot(self):
    # Array values snapshot
    values = [c.value for c in self.cells]

    # Cell type snapshot: [group_id, cell_type, value, is_frozen]
    types = [[c.group.group_id,
              cell_type_dict[c.cell_type] if c.label == 0 else c.label,
              c.value,
              1 if c.status == CellStatus.FREEZE else 0]
             for c in self.cells]

    return values, types
```

---

## 5. CellGroup Management

### 5.1 Group Lifecycle

**[Code: CellGroup.py:104-122]**:

```
ALGORITHM: CellGroup.run()

WHILE status != MERGED AND NOT all_cells_inactive():

    IF count_down == 0:
        change_status()  # Toggle ACTIVE <-> SLEEP

    IF status == SLEEP:
        put_cells_to_sleep()
        count_down -= 1
        sleep(0.05)

    IF status == ACTIVE:
        ACQUIRE lock

        IF is_group_sorted():
            next_group = find_next_group()
            IF next_group AND next_group.status == ACTIVE AND next_group.is_group_sorted():
                merge_with_group(next_group)

        RELEASE lock
        sleep(0.05)
        count_down -= 1
```

### 5.2 Group Sorted Check

**[Code: CellGroup.py:37-44]**:

```
ALGORITHM: is_group_sorted()

prev_cell = cells[left_boundary[0]]

FOR i FROM left_boundary[0] TO right_boundary[0]:
    cell = cells[i]

    IF cell.status IN [SLEEP, MOVING]:
        RETURN False  # Can't determine if cell is moving

    IF cell.value < prev_cell.value:
        RETURN False  # Out of order

    prev_cell = cell

RETURN True
```

### 5.3 Group Merging

**[Code: CellGroup.py:55-73]**:

```
ALGORITHM: merge_with_group(next_group)

1. MARK next group as merged:
   next_group.status = MERGED

2. TAKE minimum timing parameters:
   self.count_down = min(self.count_down, next_group.count_down)
   self.phase_period = min(self.phase_period, next_group.phase_period)

3. EXTEND boundary:
   self.right_boundary_position = next_group.right_boundary_position

4. ABSORB cells:
   self.cells_in_group.extend(next_group.cells_in_group)

5. UPDATE all cells in merged group:
   FOR cell IN self.cells_in_group:
       cell.group = self
       cell.left_boundary = self.left_boundary_position
       cell.right_boundary = self.right_boundary_position
       cell.update()  # Reset ideal_position for Selection cells

       IF cell.cell_type == 'Insertion':
           cell.enable_to_move = False

6. ENABLE one insertion cell:
   FOR cell IN self.cells_in_group:
       IF cell.cell_type == 'Insertion':
           cell.enable_to_move = False  # Only first one enabled
           BREAK
```

### 5.4 Sleep/Wake Cycle

**[Code: CellGroup.py:81-101]**:

```
ALGORITHM: change_status()

1. RESET countdown:
   count_down = phase_period

2. TOGGLE status:
   IF status == ACTIVE:
       status = SLEEP
       put_cells_to_sleep()

   ELSE IF status == SLEEP:
       status = ACTIVE
       awake_cells()

---

put_cells_to_sleep():
    FOR cell IN cells_in_group:
        IF cell.status NOT IN [MOVING, INACTIVE]:
            cell.status = SLEEP

awake_cells():
    FOR cell IN cells_in_group:
        IF cell.status != INACTIVE:
            cell.status = cell.previous_status
```

---

## 6. Frozen Cell Implementation

### 6.1 Frozen Cell Semantics

**[Paper p.8]** describes "movable frozen" vs "immovable frozen".
**[Code: MultiThreadCell.py]** — Simpler single FREEZE status:

| Aspect | Behavior |
|--------|----------|
| **Initiate swap** | Cannot (swap() returns early) |
| **Accept swap** | Can be swapped with by ACTIVE cells |
| **Position** | Moves when swapped with |
| **Tracking** | `frozen_swap_attempts` counter |

### 6.2 Setting Frozen Status

**[Code: MultiThreadCell.py:67-69]**:

```python
def set_cell_to_freeze(self):
    self.status = CellStatus.FREEZE
    self.previous_status = CellStatus.FREEZE
```

### 6.3 Frozen Cell Interactions by Algorithm

**BubbleSortCell** **[Code: BubbleSortCell.py:49]**:
- Can attempt swap with FREEZE cells (included in valid targets)
- Swap will succeed, moving frozen cell

**SelectionSortCell** **[Code: SelectionSortCell.py:35-42]**:
- When ideal_position has frozen cell: shifts ideal_position
- Calls swap() anyway if value < frozen.value (to count attempt)

**InsertionSortCell** **[Code: InsertionSortCell.py:74-76]**:
- Skips frozen cells in `is_enable_to_move()` check
- Can swap with frozen cells

---

## 7. Evaluation Metrics

### 7.1 Monotonicity (Sortedness by Neighbors)

**[Paper p.8]**: "Monotonicity is the measurement of how well the cells followed monotonic order"

**[Code: analysis/utils.py]** — `get_monotonicity()`:

```python
def get_monotonicity(arr):
    monotonicity_value = 1  # Start counting from first element
    prev = arr[0]
    for i in range(1, len(arr)):
        if arr[i] >= prev:
            monotonicity_value += 1
        prev = arr[i]
    return (monotonicity_value / len(arr)) * 100
```

**Formula**: (cells in correct relative order with predecessor / total cells) × 100

### 7.2 Monotonicity Error

**[Paper p.8]**: "number of cells that violate the monotonic order"

**Formula**: Count of adjacent pairs where `arr[i] > arr[i+1]` (for ascending)

### 7.3 Sortedness Value

**[Paper p.8]**: "percentage of cells that strictly follow the designated sort order"

**Formula**: (cells in correct final sorted position / total cells) × 100

### 7.4 Spearman Distance

**[Code: analysis/utils.py]** — `get_spearman_distance()`:

```python
def get_spearman_distance(arr):
    res = 0
    for i in range(len(arr)):
        res += abs(arr[i] - i)  # Distance from correct position
    return res
```

**Formula**: Σ |actual_position - expected_position| for all cells

### 7.5 Delayed Gratification

**[Paper p.8]**: "ability to undertake actions that temporarily increase Monotonicity Error in order to achieve gains later on"

**Formula**:
```
DG_event = ΔS_increasing / ΔS_decreasing

where:
  ΔS_decreasing = Sortedness drop from peak to trough
  ΔS_increasing = Sortedness gain from trough to next peak
```

**Calculation**: Sum all DG events across trajectory

### 7.6 Aggregation Value (Chimeric)

**[Paper p.8-9]**: "percentage of cells with directly adjacent neighboring cells that were all the same Algotype"

**[Code: analysis/utils.py style]**:

```python
def get_aggregation_value(cells):
    same_type_count = 0
    for i in range(len(cells)):
        has_left_same = (i > 0 and cells[i-1].algotype == cells[i].algotype)
        has_right_same = (i < len(cells)-1 and cells[i+1].algotype == cells[i].algotype)
        if has_left_same or has_right_same:
            same_type_count += 1
    return (same_type_count / len(cells)) * 100
```

### 7.7 Position Success Rate (Frozen Experiments)

**[Code: freezing_sorting_analysis.py]**:

```python
def get_pos_success_rate(cells, frozen_cell_num):
    current = [c.value for c in cells]
    expected = sorted(current)
    correct = sum(1 for a, b in zip(expected, current) if a == b)
    return (correct - frozen_cell_num) / (len(current) - frozen_cell_num)
```

---

## 8. Experiment Configuration

### 8.1 Standard Parameters

**[Code: Various experiment files]**:

| Parameter | Typical Value | Description |
|-----------|---------------|-------------|
| Array size | 100 | Number of cells |
| Value range | 1-100 (unique) or 1-10 (duplicates) | Cell values |
| Frozen cells | 0, 1, 2, 3 | Number of frozen obstacles |
| Experiment count | 100 | Repetitions for statistics |
| Timeout | 40 seconds | Max time before abort |
| Phase period | random(100, 200) | Group sleep/wake cycle |
| Start countdown | random(0, phase_period) | Initial delay |

### 8.2 Experiment Types

**Single Algorithm Sorting**:
- Homogeneous population (all Bubble, all Selection, or all Insertion)
- Single group or multi-group configuration
- With/without frozen cells

**Chimeric Population Sorting**:
- Mixed algotypes (e.g., 50% Bubble + 50% Selection)
- Random algotype assignment
- Aggregation tracking

**Cross-Purpose Sorting**:
- Different algotypes with different sort directions
- E.g., Bubble(ascending) + Selection(descending)
- Measures equilibrium sortedness

**Duplicate Values**:
- 100 cells with values 1-10 (10 copies each)
- Studies persistent aggregation after sorting

---

## 9. Expected Experimental Results

### 9.1 Efficiency (Steps to Sort)

**[Paper p.10, Table 1]**:

| Algorithm | Traditional (swaps) | Cell-View (swaps) | Z-score | p-value |
|-----------|--------------------|--------------------|---------|---------|
| Bubble | ~2500 | ~2500 | 0.73 | 0.47 |
| Insertion | ~2500 | ~2500 | 1.26 | 0.24 |
| Selection | ~100 | ~1100 | 120.43 | <0.01 |

### 9.2 Error Tolerance (Monotonicity Error)

**[Paper p.10-11, Tables 2-3]**:

**Movable Frozen**:
| Algorithm | 1 Frozen | 2 Frozen | 3 Frozen |
|-----------|----------|----------|----------|
| Bubble | 0.00 | 0.80 | 2.64 |
| Selection | 2.24 | 4.36 | 13.24 |

**Immovable Frozen**:
| Algorithm | 1 Frozen | 2 Frozen | 3 Frozen |
|-----------|----------|----------|----------|
| Bubble | 1.91 | 3.72 | 5.37 |
| Selection | 1.00 | 1.96 | 2.91 |

### 9.3 Delayed Gratification

**[Paper p.11]**:

| Algorithm | 0 Frozen | 1 Frozen | 2 Frozen | 3 Frozen |
|-----------|----------|----------|----------|----------|
| Bubble | 0.24 | 0.29 | 0.32 | 0.37 |
| Insertion | 1.10 | 1.13 | 1.15 | 1.19 |
| Selection | ~2.8 | (no clear trend) | | |

### 9.4 Chimeric Aggregation

**[Paper p.12-13]**:

| Algotype Pair | Peak Aggregation | At Progress % |
|---------------|------------------|---------------|
| Bubble + Selection | 72% | 42% |
| Bubble + Insertion | 65% | 21% |
| Selection + Insertion | 69% | 19% |
| Control (same algo) | 61% | (baseline) |

### 9.5 Cross-Purpose Equilibrium

**[Paper p.14]**:

| Configuration | Final Sortedness |
|---------------|------------------|
| Bubble↓ + Selection↑ | 42.5% |
| Bubble↑ + Insertion↓ | 73.73% |
| Selection↓ + Insertion↑ | 38.31% |

---

## 10. Implementation Checklist

### 10.1 Core Components

- [ ] `MultiThreadCell` base class with all properties
- [ ] `CellStatus` enumeration (7 states)
- [ ] `BubbleSortCell` with random direction selection
- [ ] `SelectionSortCell` with ideal_position shifting
- [ ] `InsertionSortCell` with left-sorted check
- [ ] `CellGroup` with sleep/wake and merge logic
- [ ] `GroupStatus` enumeration (4 states)
- [ ] `StatusProbe` metrics collector
- [ ] Position as (x, y) tuples

### 10.2 Threading

- [ ] Single global lock (not barrier-based)
- [ ] Cell threads extending Thread class
- [ ] Group threads for coordination
- [ ] Lock acquire/release pattern in move()

### 10.3 Algorithms

- [ ] Bubble: random 50% left/right direction
- [ ] Selection: ideal_position init, shift on denial, reset on merge
- [ ] Insertion: is_enable_to_move() left-sorted check

### 10.4 Frozen Cells

- [ ] FREEZE status prevents initiating swaps
- [ ] FREEZE cells can be moved by others
- [ ] frozen_swap_attempts counter
- [ ] tried_to_swap_with_frozen flag

### 10.5 Groups

- [ ] Sleep/wake cycle with phase_period
- [ ] is_group_sorted() check
- [ ] merge_with_group() logic
- [ ] Cell boundary and group updates on merge

### 10.6 Metrics

- [ ] swap_count
- [ ] compare_and_swap_count
- [ ] sorting_steps[] array snapshots
- [ ] cell_types[] type distribution
- [ ] Monotonicity calculation
- [ ] Sortedness calculation
- [ ] Delayed Gratification calculation
- [ ] Aggregation Value calculation

---

## Appendix A: File Reference Map

| Component | Python File | Line Numbers |
|-----------|-------------|--------------|
| CellStatus enum | MultiThreadCell.py | 7-14 |
| MultiThreadCell class | MultiThreadCell.py | 17-113 |
| swap() method | MultiThreadCell.py | 71-98 |
| BubbleSortCell | BubbleSortCell.py | 8-75 |
| SelectionSortCell | SelectionSortCell.py | 8-100 |
| InsertionSortCell | InsertionSortCell.py | 8-101 |
| GroupStatus enum | CellGroup.py | 6-10 |
| CellGroup class | CellGroup.py | 13-122 |
| is_group_sorted() | CellGroup.py | 37-44 |
| merge_with_group() | CellGroup.py | 55-73 |
| StatusProbe | StatusProbe.py | 1-22 |

---

## Appendix B: Glossary

**Active Cell**: Cell with `status == ACTIVE`, can initiate and accept swaps

**Algotype**: The sorting algorithm a cell follows (Bubble, Selection, Insertion)

**Aggregation Value**: Percentage of cells adjacent to same-algotype neighbors

**Cell Vision**: How many positions a cell can see (always 1 in current implementation)

**Chimeric Array**: Array with cells following different algotypes

**Delayed Gratification (DG)**: Ratio of sortedness recovery to temporary setback

**Frozen Cell**: Cell with `status == FREEZE`, cannot initiate swaps

**Group**: Collection of cells with shared boundaries managed by CellGroup thread

**Ideal Position**: Target position for SelectionSortCell (shifts on denial)

**Monotonicity Error**: Count of adjacent cell pairs in wrong order

**Phase Period**: Duration of sleep or wake phase for a CellGroup

**Sortedness Value**: Percentage of cells in correct final sorted position

**Spearman Distance**: Sum of positional displacements from sorted order

---

**Document Version**: 2.0
**Date**: December 31, 2025
**Status**: Revised to match cell_research ground truth
**Previous Version**: 1.0 (paper-only derivation)
