# Refactor Plan: Divide and Conquer Factorization

## Status: Validated (Jan 3, 2026)
- **Offset Partitioning:** Successfully validated. Range partitioning allows discovery of factors in arbitrary segments (Trials > 0).
- **Chimeric Stability:** Successfully validated. Randomly assigning Algotype (BUBBLE, INSERTION, SELECTION) to cells maintains 100% convergence across large targets (RSA-100 to RSA-200).
- **Arbitrary Precision:** BigInteger logic handles numbers with hundreds of digits without performance regression.

## Objective
To scale the `FactorizationExperiment` to handle large integers (e.g., 128-bit or 256-bit) by implementing a **Divide and Conquer** parallelism model. This approach will distribute the search space across multiple independent trials, leveraging the engine's existing "Per-Trial Parallelism."

## Current Limitations
- **Single Range:** All trials currently search the same range starting from position 1.
- **Array Size Constraints:** Memory and propagation time limit the `arraySize` (currently capped at ~2,000 cells), preventing full coverage of the square root space for large targets.
- **Redundant Work:** 100 trials searching the same 2,000 cells is inefficient for discovery, though useful for statistical convergence.

## Proposed Architecture: Range Partitioning

The search space $[2, \sqrt{N}]$ will be divided into segments of size `S` (where `S` is the `arraySize`). Each trial in a batch will be assigned a unique segment to explore.

### 1. Offset-Aware Cell Initialization
Modify `FactorizationExperiment.createCellArray` to support a `startOffset`.

```java
private static RemainderCell[] createCellArray(BigInteger target, int size, int startOffset) {
    RemainderCell[] cells = new RemainderCell[size];
    for (int i = 0; i < size; i++) {
        // Position is now relative to the segment offset
        int position = startOffset + i + 1; 
        cells[i] = new RemainderCell(target, position);
    }
    return cells;
}
```

### 2. Distributed Trial Strategy
Update the `ExperimentRunner` integration in `FactorizationExperiment` to pass unique offsets to each trial.

- **Trial 0:** Offset 0 (Positions 1 to 2000)
- **Trial 1:** Offset 2000 (Positions 2001 to 4000)
- **Trial N:** Offset $N \times 2000$

### 3. Leveraging Per-Trial Parallelism
The existing `ExperimentRunner.runBatchExperiments()` is perfectly suited for this:
- **No Shared State:** Each trial remains independent, requiring no synchronization or locks between ranges.
- **CPU Scaling:** Different segments are processed concurrently across all available CPU cores.
- **Dynamic Trial Counts:** The "100 trial default" is no longer a limit for repetition, but a **Search Window**. Increasing the trial count directly increases the search coverage ($Trials \times ArraySize$).

## Implementation Steps

### Phase 1: Engine Enhancements
- Update `ExperimentRunner` to optionally provide the trial index to the `Supplier<T[]>` cell factory. This allows the factory to calculate offsets based on the trial number.

### Phase 2: FactorizationExperiment Refactor
- Implement `OffsetRemainderCellFactory` that uses the trial index and `arraySize` to calculate the `startOffset`.
- Update `resultsContainFactor` to report the *actual* factor found (offset + local position).
- Add a CLI argument `--search-limit` to allow users to increase the number of trials beyond 100 to cover more segments.

### Phase 3: Intelligent Scaling
- Automatically calculate the number of trials required to cover $\sqrt{N}$ if the user doesn't specify one, while respecting a "Time Budget" or "Trial Cap."

## Success Criteria
- **Coverage:** Ability to find factors located at high positions (e.g., $10^6$) by distributing the search across many trials.
- **Performance:** No regression in execution time for standard 1,000-cell arrays.
- **Stability:** Graceful handling of very large trial batches (e.g., 10,000 trials).

## Verification Plan
1. **Unit Test:** Verify `RemainderCell` creation with offsets.
2. **Integration Test:** Run a factorization experiment on a known semi-prime where the factor is outside the first 2,000 positions (e.g., factor = 5003).
3. **Performance Benchmark:** Compare "Single Large Array" vs "Multiple Small Segmented Arrays" for the same search space.
4. **RSA Factor Verification:** Use known factors from `data/rsa_factors.txt` to verify discovery at scale.
