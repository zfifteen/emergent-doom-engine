package com.emergent.doom.execution;

/**
 * Execution mode for the sorting engine.
 *
 * <p>Determines whether cells are evaluated sequentially (original behavior)
 * or in parallel (per Levin paper specification).</p>
 */
public enum ExecutionMode {

    /**
     * Sequential execution mode (original behavior).
     *
     * <p>Cells are evaluated one at a time in iteration order.
     * Simpler and deterministic, but does not match the paper's
     * specification of parallel cell evaluation.</p>
     */
    SEQUENTIAL,

    /**
     * Parallel execution mode (paper-faithful).
     *
     * <p>Each cell runs in its own thread, with barrier synchronization
     * between evaluation and swap resolution phases. Matches the Levin
     * paper specification: "each cell represented by a single thread".</p>
     *
     * <p><strong>Thread Model:</strong></p>
     * <ul>
     *   <li>One thread per cell</li>
     *   <li>CyclicBarrier for phase synchronization</li>
     *   <li>Main thread coordinates and resolves conflicts</li>
     * </ul>
     */
    PARALLEL
}
