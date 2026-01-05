package com.emergent.doom.execution;

/**
 * Execution mode for single-trial execution.
 *
 * <p>As of v2.0, only SEQUENTIAL mode is supported. Per-cell threading modes
 * (PARALLEL, LOCK_BASED) were removed because per-trial parallelism via
 * {@link com.emergent.doom.experiment.ExperimentRunner#runBatchExperiments(com.emergent.doom.experiment.ExperimentConfig)}
 * provides 5-10× better performance by eliminating barrier synchronization overhead.</p>
 *
 * <p><strong>Historical Note:</strong> Pre-v2.0 versions supported PARALLEL and LOCK_BASED
 * modes for parallelizing cell evaluation within a single trial. These were deprecated
 * in favor of batch-level parallelism (multiple independent trials running concurrently).</p>
 *
 * <p><strong>Future Extensibility:</strong> This enum remains as a single-value type to
 * preserve architectural flexibility for potential future modes (e.g., DISTRIBUTED,
 * GPU_ACCELERATED) without breaking existing APIs.</p>
 */
public enum ExecutionMode {

    /**
     * Sequential execution mode.
     *
     * <p>Cells are evaluated one at a time in iteration order using
     * {@link SynchronousExecutionEngine}. This is the only supported mode as of v2.0.</p>
     *
     * <p><strong>For Parallel Execution:</strong> Run multiple trials concurrently via
     * {@link com.emergent.doom.experiment.ExperimentRunner#runBatchExperiments(com.emergent.doom.experiment.ExperimentConfig)}
     * rather than threading individual cells within a trial.</p>
     */
    SEQUENTIAL
}
