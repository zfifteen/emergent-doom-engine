# Chapter 2: Swap Mechanics

Swaps are the fundamental local interaction mechanism in the EDE. This package tests how cells exchange positions through **conditional swapping**, enabling emergent computation through decentralized interactions.

## Purpose

Tests in this package validate that the `SwapEngine` correctly implements:
- Conditional swap logic based on cell comparison
- Frozen cell constraints (immutable, movable, none)
- Swap count tracking for metrics
- Robustness on "unreliable substrates" (damaged/frozen cells)

## Concepts Covered

### Local Agent Interactions
- Cells interact only with neighbors through pairwise swaps
- No global orchestration - each swap is an autonomous decision
- Comparison determines swap necessity: `if (cell[i] > cell[j]) swap(i, j)`

### Unreliable Substrate Simulation
- **Frozen cells** model damaged/disabled agents
- System must route around failures without catastrophic breakdown
- Demonstrates robustness through redundancy

### Frozen Cell States
1. **NONE**: Fully active cell (can initiate and participate in swaps)
2. **MOVABLE**: Passive cell (can be moved by others but cannot initiate swaps)
3. **IMMUTABLE**: Completely frozen (cannot move or be moved)

### Swap Engine Responsibilities
- Enforce frozen state constraints before attempting comparisons
- Track successful swaps for convergence detection
- Provide deterministic swap semantics for reproducibility

## Prerequisites

**Required:**
- [Chapter 1: Cell Foundations](../cell/README.md) - Understanding of Cell interface and `compareTo()`

**Helpful:**
- Levin et al. (2024) Section 2.1 - "Autonomous Array Elements"
- EDE principle: Robustness through decentralized control

## Test Files

### SwapEngineTest.java **[Beginner]**

Comprehensive test suite covering all swap scenarios based on `SwapEngineTestSpec.md`.

**Test Categories:**

1. **NONE/NONE State (T01-T02)**: Both cells active
   - Swaps occur unconditionally (even if already ordered)
   - Demonstrates "dumb executor" - engine doesn't decide, just executes

2. **MOVABLE/NONE State (T04-T05)**: First cell passive
   - MOVABLE cells cannot initiate swaps
   - Blocked regardless of value comparison

3. **NONE/MOVABLE State (T06-T07)**: Second cell passive
   - Active cell can move passive cell
   - Swap succeeds if values warrant it

4. **IMMUTABLE Constraints (T08-T12)**: At least one cell frozen
   - Any IMMUTABLE involvement blocks swap
   - Models permanent damage/critical constraints

5. **Swap Parity (T13-T14)**: `wouldSwap()` vs `attemptSwap()`
   - Prediction matches actual behavior
   - Enables lookahead without side effects

6. **Swap Count Tracking (T15-T16)**: Metrics and convergence
   - Counter increments only on successful swaps
   - Resets with engine lifecycle

**Link to source:** [SwapEngine.java](../../../../../../main/java/com/emergent/doom/swap/SwapEngine.java)

### IntCellTest.java **[Beginner]**

Tests the `IntCell` test helper implementation.

**Key Tests:**
- Value wrapping and extraction
- Comparison behavior
- Immutability guarantees
- String representation

**Why it matters:** Provides a reference implementation for other tests.

**Link to source:** [IntCell.java](IntCell.java) (test utility, not production code)

### FrozenCellStatus Integration **[Intermediate]**

Tests interaction between `SwapEngine` and `FrozenCellStatus`:

**Key Tests:**
- State transitions (NONE → MOVABLE → IMMUTABLE)
- Persistence across swaps
- Thread safety (via `ThreadSafeFrozenCellStatus`)

**Link to source:** [FrozenCellStatus.java](../../../../../../main/java/com/emergent/doom/swap/FrozenCellStatus.java)

## Usage Examples

### Basic Swap Operation

The simplest swap between two cells:

```java
// Create cells and swap engine
IntCell[] cells = { new IntCell(10), new IntCell(5) };
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);

// Attempt swap
boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

System.out.println("Swapped: " + swapped);        // true
System.out.println("cells[0]: " + cells[0].getValue()); // 5
System.out.println("cells[1]: " + cells[1].getValue()); // 10
System.out.println("Total swaps: " + swapEngine.getSwapCount()); // 1
```

**Key points:**
- Swap occurs because `cells[0] > cells[1]` (10 > 5)
- Array is modified in-place
- Swap count tracks successful swaps

### Frozen Cell Constraints

Model damaged agents that cannot participate:

```java
IntCell[] cells = { new IntCell(10), new IntCell(5) };
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);

// Freeze first cell as IMMUTABLE
frozenStatus.setFrozen(0, FrozenType.IMMUTABLE);

// Attempt swap
boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

System.out.println("Swapped: " + swapped);        // false
System.out.println("cells[0]: " + cells[0].getValue()); // 10 (unchanged)
System.out.println("cells[1]: " + cells[1].getValue()); // 5 (unchanged)
```

**Key points:**
- IMMUTABLE cells block all swaps involving them
- Models permanent constraints or critical values
- System must route around frozen positions

### MOVABLE Cells (Passive Agents)

Allow passive participation without agency:

```java
IntCell[] cells = { new IntCell(5), new IntCell(10) };
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);

// Cell 1 is MOVABLE (can be moved but can't initiate)
frozenStatus.setFrozen(1, FrozenType.MOVABLE);

// Swap with cell 0 as initiator
boolean swapped = swapEngine.attemptSwap(cells, 0, 1);

System.out.println("Swapped: " + swapped);        // true
System.out.println("cells[0]: " + cells[0].getValue()); // 10
System.out.println("cells[1]: " + cells[1].getValue()); // 5

// But MOVABLE cell cannot initiate
swapped = swapEngine.attemptSwap(cells, 1, 0);
System.out.println("Swapped: " + swapped);        // false
```

**Key points:**
- MOVABLE state allows passive movement
- Useful for "inert" values that can be relocated
- Asymmetric swap constraints

### Predicting Swaps Without Side Effects

Use `wouldSwap()` for lookahead:

```java
IntCell[] cells = { new IntCell(10), new IntCell(5) };
FrozenCellStatus frozenStatus = new FrozenCellStatus();
SwapEngine<IntCell> swapEngine = new SwapEngine<>(frozenStatus);

// Check if swap would occur without executing it
boolean willSwap = swapEngine.wouldSwap(cells, 0, 1);
System.out.println("Would swap: " + willSwap); // true

// Array unchanged
System.out.println("cells[0]: " + cells[0].getValue()); // 10
System.out.println("Swap count: " + swapEngine.getSwapCount()); // 0

// Now actually swap
swapEngine.attemptSwap(cells, 0, 1);
System.out.println("Swap count: " + swapEngine.getSwapCount()); // 1
```

**Key points:**
- `wouldSwap()` predicts without mutating state
- Useful for convergence detection algorithms
- Guarantees parity with `attemptSwap()` behavior

## Architecture Insights

### Dumb Executor Pattern

The SwapEngine is intentionally "dumb" - it swaps **unconditionally** if cells are not frozen, even if they're already in correct order:

```java
// Already sorted, but still swaps
IntCell[] cells = { new IntCell(5), new IntCell(10) };
swapEngine.attemptSwap(cells, 0, 1); // true - swaps anyway!
```

**Why this matters:**
- Engine doesn't "know" what correct order is
- Comparison logic lives in cells (`compareTo()`), not engine
- Emergent behavior arises from repeated dumb swaps, not smart decisions

### Frozen State as Robustness Mechanism

Frozen cells model the "unreliable substrate" concept from Levin et al.:

- **Biological analogy**: Cells die or become unresponsive
- **Hardware analogy**: Memory corruption or hardware failures
- **Abstract computation**: Constraints that cannot be violated

The system demonstrates **robustness** by continuing to sort around frozen positions, proving that:
- Global function emerges from local redundancy
- Single-point failures don't cascade
- Decentralized systems are inherently fault-tolerant

### Swap Count as Convergence Signal

The swap counter provides a simple heuristic for convergence detection:

```java
int previousSwapCount = swapEngine.getSwapCount();
engine.step(); // Execute one sorting step
int newSwapCount = swapEngine.getSwapCount();

if (newSwapCount == previousSwapCount) {
    System.out.println("No swaps occurred - possibly converged");
}
```

**Convergence detectors** use this signal to identify when sorting completes (see [Chapter 3: Execution Engines](../execution/README.md)).

## Common Patterns

### Progressive Crystallization

Freeze cells as they reach final positions:

```java
for (int i = 0; i < cells.length; i++) {
    if (isInFinalPosition(cells[i], i)) {
        frozenStatus.setFrozen(i, FrozenType.IMMUTABLE);
    }
}
```

**Use case:** Optimize convergence by preventing re-sorting of solved regions.

### Partial Constraint Satisfaction

Mix frozen and active cells to model constraints:

```java
// First and last positions are fixed constraints
frozenStatus.setFrozen(0, FrozenType.IMMUTABLE);
frozenStatus.setFrozen(cells.length - 1, FrozenType.IMMUTABLE);

// Middle positions are free to sort
```

**Use case:** Boundary conditions or anchor points in problem space.

## Troubleshooting

### "Expected swap but none occurred"

**Problem:** `attemptSwap()` returned `false` unexpectedly.

**Check:**
1. Are either cells frozen? (Use `frozenStatus.getFrozenType(i)`)
2. Is comparison logic correct? (Test `cell[i].compareTo(cell[j])`)
3. Are you swapping the same cell with itself? (Invalid: `i == j`)

### "Swap count not incrementing"

**Problem:** Swaps succeed but counter stays at zero.

**Solution:** Verify you're using the same `SwapEngine` instance:
```java
// ❌ Wrong - creates new engine
swapEngine = new SwapEngine<>(frozenStatus);

// ✅ Correct - reuse existing engine
swapEngine.attemptSwap(cells, i, j);
```

### "wouldSwap() and attemptSwap() disagree"

**Problem:** Prediction doesn't match actual behavior.

**This is a bug** - please file an issue with:
- Cell values
- Frozen states
- Code to reproduce

## Next Steps

Now that you understand local swapping interactions, proceed to:

**[Chapter 2: Probe Recording](../probe/README.md)** - Learn how to capture and analyze execution trajectories for debugging and research.

**Also see:**
- [Chapter 3: Execution Engines](../execution/README.md) - How engines orchestrate swaps across entire arrays
- [Chapter 4: Chimeric Populations](../chimeric/README.md) - Robustness with mixed algotypes

---

**[← Back: Cell Foundations](../cell/README.md)** | **[↑ Test Suite Home](../README.md)** | **[Next: Probe Recording →](../probe/README.md)**
