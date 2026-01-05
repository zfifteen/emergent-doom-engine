# Migration Guide: v1.x → v2.0

## Breaking Changes: Per-Cell Threading Removal

### What Changed
Removed deprecated per-cell threading execution modes:
- `ExecutionMode.PARALLEL` (barrier-synchronized per-cell threads)
- `ExecutionMode.LOCK_BASED` (lock-synchronized per-cell threads)
- `ParallelExecutionEngine`, `LockBasedExecutionEngine`, `CellThread` classes

### Why This Change?
Per-trial parallelism (running multiple independent trials concurrently) is **5-10× more efficient** than per-cell threading because it eliminates:
- **Barrier synchronization overhead**: ~250,000 barriers for 100 trials × 2,500 steps
- **Lock contention**: Shared swap collectors no longer bottleneck
- **Thread creation overhead**: 100 trials × 1,000 cells = 100,000 threads → 100 threads

See [`ExperimentRunner.java` JavaDoc](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/main/java/com/emergent/doom/experiment/ExperimentRunner.java) for detailed performance comparison.

### How to Migrate

#### Before (v1.x):
```java
ExperimentConfig config = new ExperimentConfig(
    100,                        // arraySize
    10000,                      // maxSteps
    10,                         // requiredStableSteps
    false,                      // recordTrajectory
    ExecutionMode.PARALLEL,     // ← REMOVED
    100                         // numRepetitions
);
ExperimentResults results = runner.runExperiment(config, 100);
```

#### After (v2.0):
```java
ExperimentConfig config = new ExperimentConfig(
    100,                        // arraySize
    10000,                      // maxSteps
    10,                         // requiredStableSteps
    false,                      // recordTrajectory
    ExecutionMode.SEQUENTIAL,   // ← Use this (only mode)
    100                         // numRepetitions for batch execution
);
// Batch execution parallelizes across trials (not within trials)
ExperimentResults results = runner.runBatchExperiments(config);
```

### Performance Impact

**You will see FASTER execution** because:

- **Per-trial parallelism** (`runBatchExperiments()`): Embarrassingly parallel, no synchronization
- **Per-cell threading** (removed): Heavy synchronization overhead, lock contention

### Topology Support

v2.0 retains full topology support (`RingTopology`, `GridTopology`, etc.). `SynchronousExecutionEngine` respects topology iteration order unchanged.

### Clustering & Aggregation Metrics

Chimeric experiments (`AlgotypeAggregationIndex`, `AggregationValue`) work identically in v2.0. Expected aggregation peaks remain:

- Bubble-Selection: ~72% (paper baseline)
- Bubble-Insertion: ~65%
- Selection-Insertion: ~69%

### Serialized Experiment Configs

If you have serialized `ExperimentConfig` objects with `ExecutionMode.PARALLEL` or
`LOCK_BASED`, you must:

1. Migrate serialized data before upgrading
2. Replace enum values with `SEQUENTIAL` in persisted storage
3. Re-serialize with updated schema

**Example (JSON):**
```json
{
  "executionMode": "SEQUENTIAL"  // Replace PARALLEL/LOCK_BASED
}
```

### ExecutionMode status

`ExecutionMode` remains temporarily for backward compatibility but is `@Deprecated(since = "2.0", forRemoval = true)` with only `SEQUENTIAL` supported. Configure batch-level parallelism through `ExperimentRunner#runBatchExperiments()` rather than selecting execution modes. Plan to remove the enum entirely in a future release once migrations are complete.

### Questions?

See [Issue #92](https://github.com/zfifteen/emergent-doom-engine/issues/92) for architectural rationale.
