## [2.0.0] - 2026-01-05

### BREAKING CHANGES
- **Removed per-cell threading execution models** ([#92](https://github.com/zfifteen/emergent-doom-engine/issues/92))
  - Deleted `ExecutionMode.PARALLEL` and `ExecutionMode.LOCK_BASED`
  - Deleted `ParallelExecutionEngine`, `LockBasedExecutionEngine`, `CellThread` classes
  - **Migration**: Use `ExecutionMode.SEQUENTIAL` with `runBatchExperiments()`
  - See [`docs/MIGRATION_v2.0.md`](docs/MIGRATION_v2.0.md) for details

### Performance Improvements
- Per-trial parallelism (`runBatchExperiments()`) provides **5-10× speedup** over removed per-cell threading
- Eliminated barrier synchronization overhead (~250,000 barriers per 100-trial batch)
- Eliminated lock contention on shared swap collectors
