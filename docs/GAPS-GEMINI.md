# Gap Analysis: Emergent Doom Engine vs. Levin Paper (2401.05375v1)

This document details the deviations between the Java implementation of the "Emergent Doom Engine" and the methods described in the reference paper *Classical Sorting Algorithms as a Model of Morphogenesis* (Zhang et al.).

## 1. Execution Model & Architecture

*   **Paper Requirement:** The paper explicitly describes a distributed, parallel execution model.
    > "We used multi-thread programming to implement the cell-view sorting algorithms... each cell represented by a single thread... All cells have a chance to move at each time step (parallel)." (p. 8, 22)
*   **Current Implementation:** The `ExecutionEngine` utilizes a **single-threaded, sequential** execution model.
    *   It iterates through the cell array in a deterministic order (provided by `BubbleTopology.getIterationOrder`) within a single `step()`.
    *   **Impact:** This removes race conditions and simultaneous swap attempts, which are key characteristics of the "biological/distributed" analogy. Emergent behaviors reliant on race conditions (like specific clustering dynamics) may differ or disappear.

## 2. Frozen Cell Behavior (Logic Inversion)

*   **Paper Requirement:**
    *   **Movable Frozen Cell:** "...will not move on its own (will not initiate a move), but other cells are able to move it." (p. 6)
    *   **Immovable Frozen Cell:** "...can neither proactively move itself nor can it be moved by another cell." (p. 6)
*   **Current Implementation:** The logic in `FrozenCellStatus` and `SwapEngine` is **inverted** for `MOVABLE` cells.
    *   **Initiation:** `canMove(i)` returns `true` for `MOVABLE`. This allows `MOVABLE` cells to *initiate* swaps (Violation).
    *   **Displacement:** `canBeDisplaced(j)` returns `false` for `MOVABLE`. This prevents `MOVABLE` cells from being *moved by others* (Violation).
    *   **Impact:** `MOVABLE` cells behave like "Active but Heavy/Stubborn" cells (can push, can't be pushed), whereas the paper describes them as "Passive" (can't push, can be pushed).

## 3. Metrics Implementation

### 3.1 Aggregation Value
*   **Paper Definition:** "...the percentage of cells with directly adjacent neighboring cells that were all the same Algotype." (p. 9)
*   **Current Implementation:** `AggregationValue.java` is designed to sum numerical values extracted from cells (`extractValue`).
    *   **Status:** **Incorrect Concept & Unimplemented**. The class structure does not support the Algotype clustering metric described.

### 3.2 Delayed Gratification Index
*   **Paper Definition:** "The improvement in Sortedness made by a temporarily error increasing action." (p. 9)
*   **Current Implementation:** `DelayedGratificationIndex.java` exists but the `compute` method returns `0.0`.
    *   **Status:** **Unimplemented**.

### 3.3 Monotonicity Error
*   **Status:** **Implemented Correctly**. Calculates total inversions (pairs out of order).

## 4. Algorithm Logic

### 4.1 Selection Sort
*   **Paper Requirement:** "Each cell can view and swap with the cell that currently occupies its ideal position." (p. 8)
*   **Current Implementation:** `SelectionCell` includes an **unspecified mechanism** where `idealPos` is incremented upon a denied swap.
    *   **Observation:** While likely necessary for the algorithm to avoid deadlock (as implied by "swapped away" comments in Fig 3), this explicit state mutation on failure is not detailed in the primary algorithm description. It represents an implementation inference.

### 4.2 Bubble & Insertion Sort
*   **Status:** **Implemented Correctly**. Both adhere to the local neighbor and left-sorted-prefix rules respectively.

## Summary of Action Items

1.  **Refactor `FrozenCellStatus`**: Fix the boolean logic for `canMove` and `canBeDisplaced` to match the paper's "Passive/Active" definitions.
2.  **Implement Metrics**:
    *   Rewrite `AggregationValue` to calculate Algotype adjacency % instead of value sums.
    *   Implement `DelayedGratificationIndex` logic (requires history tracking in `Probe` or `ExecutionEngine` to detect "dips" and "recoveries").
3.  **Concurrency (Long-term)**: Consider refactoring `ExecutionEngine` to support a `parallelStream` or `ExecutorService` model to better simulate the distributed nature of the paper.
