package com.emergent.doom.execution;

/**
 * Execution mode for the sorting engine.
 *
 * <p>Determines whether cells are evaluated sequentially (original behavior),
 * in parallel with barriers (Levin paper specification), or with lock-based
 * synchronization (cell_research Python behavior).</p>
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
    PARALLEL,

    /**
     * Lock-based execution mode (cell_research Python behavior).
     *
     * <p>Each cell runs in its own thread with a single global lock.
     * Cells acquire the lock, evaluate, swap (if appropriate), and release.
     * This matches the Python cell_research implementation exactly.</p>
     *
     * <p><strong>Thread Model:</strong></p>
     * <ul>
     *   <li>One thread per cell</li>
     *   <li>Single ReentrantLock for mutual exclusion</li>
     *   <li>No phase synchronization - cells swap asynchronously</li>
     *   <li>Non-deterministic execution order</li>
     * </ul>
     *
     * <p><strong>Python Reference:</strong> (BubbleSortCell.py:58-74)</p>
     * <pre>
     * def move(self):
     *     self.lock.acquire()    # Single global lock
     *     # ... evaluation and swap ...
     *     self.lock.release()
     * </pre>
     */
    LOCK_BASED
}
