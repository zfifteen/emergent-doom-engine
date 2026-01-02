package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasIdealPosition;
import com.emergent.doom.cell.HasSortDirection;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.cell.SortDirection;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.swap.SwapProposal;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.InsertionTopology;
import com.emergent.doom.topology.SelectionTopology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Synchronous execution engine implementing single-threaded trial execution.
 *
 * <p>PURPOSE: Replace per-cell threading model with synchronous loop execution.
 * Each trial runs in a single thread, evaluating all cells sequentially within
 * each step. This eliminates barrier synchronization overhead and thread creation
 * costs, enabling efficient per-trial parallelism at the experiment level.</p>
 *
 * <p>ARCHITECTURE: This engine is designed for embarrassingly parallel trial
 * execution where each trial is completely independent:
 * <ul>
 *   <li>No shared state between trials</li>
 *   <li>No synchronization needed between trials</li>
 *   <li>Trials can run in parallel across CPU cores via ExecutorService</li>
 *   <li>Each trial runs in single thread with synchronous cell evaluation</li>
 * </ul>
 * </p>
 *
 * <p>EXECUTION FLOW per Step:
 * <ol>
 *   <li>Sequentially evaluate all cells and collect proposed swaps</li>
 *   <li>Resolve conflicts using priority order (leftmost first)</li>
 *   <li>Execute non-conflicting swaps</li>
 *   <li>Record metrics and check convergence</li>
 * </ol>
 * </p>
 *
 * <p>PERFORMANCE CHARACTERISTICS:
 * <ul>
 *   <li>For N cells, M trials: Creates M threads (not N*M)</li>
 *   <li>No barrier synchronization within trial execution</li>
 *   <li>No lock contention on swap proposals</li>
 *   <li>Minimal context switching overhead</li>
 * </ul>
 * </p>
 *
 * <p>COMPARISON TO ParallelExecutionEngine:
 * <pre>
 * ParallelExecutionEngine (Current):
 *   - 100 trials × 1000 cells = 100,000 thread creations
 *   - Barrier sync every step (~2,500+ steps per trial)
 *   - Lock contention on swap collector
 *
 * SynchronousExecutionEngine (New):
 *   - 100 trials = 100 thread creations (via ExecutorService)
 *   - No barrier sync (single-threaded per trial)
 *   - No lock contention (no shared state per trial)
 * </pre>
 * </p>
 *
 * @param <T> the type of cell
 */
public class SynchronousExecutionEngine<T extends Cell<T>> {

    /**
     * PURPOSE: Cell array being sorted in this trial.
     * 
     * <p>ARCHITECTURE: This is the primary data structure modified during execution.
     * Cells are evaluated sequentially and swapped based on their algotype rules.</p>
     * 
     * <p>THREAD SAFETY: Not thread-safe. Each engine instance runs in a single thread.</p>
     */
    private final T[] cells;

    /**
     * PURPOSE: Topology helper for BUBBLE algotype neighbor evaluation.
     * 
     * <p>ARCHITECTURE: Provides neighbor indices based on bubble sort topology
     * (left and right adjacent cells). Used during cell evaluation phase.</p>
     */
    private final BubbleTopology<T> bubbleTopology;

    /**
     * PURPOSE: Topology helper for INSERTION algotype neighbor evaluation.
     * 
     * <p>ARCHITECTURE: Provides neighbor indices based on insertion sort topology
     * (all cells to the left). Used during cell evaluation phase.</p>
     */
    private final InsertionTopology<T> insertionTopology;

    /**
     * PURPOSE: Topology helper for SELECTION algotype neighbor evaluation.
     * 
     * <p>ARCHITECTURE: Provides target position based on cell's ideal position.
     * Selection cells maintain internal state for target tracking.</p>
     */
    private final SelectionTopology<T> selectionTopology;

    /**
     * PURPOSE: Swap engine for executing approved swaps and tracking frozen cells.
     * 
     * <p>ARCHITECTURE: After conflict resolution, this engine executes the
     * non-conflicting swaps and maintains frozen cell status.</p>
     */
    private final SwapEngine<T> swapEngine;

    /**
     * PURPOSE: Probe for recording snapshots and metrics at each step.
     * 
     * <p>ARCHITECTURE: Records trajectory data for analysis. Can be disabled
     * for performance when trajectory data is not needed.</p>
     */
    private final Probe<T> probe;

    /**
     * PURPOSE: Convergence detector to determine when sorting has completed.
     * 
     * <p>ARCHITECTURE: Typically checks for N consecutive steps with zero swaps
     * (NoSwapConvergence). Signals when execution can terminate early.</p>
     */
    private final ConvergenceDetector<T> convergenceDetector;

    /**
     * PURPOSE: Random number generator for BUBBLE algotype direction choice.
     * 
     * <p>ARCHITECTURE: Each BUBBLE cell randomly picks left or right neighbor
     * (50/50 probability) per step, matching Levin paper behavior.</p>
     */
    private final Random random;

    /**
     * PURPOSE: Current step counter for this trial.
     * 
     * <p>ARCHITECTURE: Incremented after each step. Used for convergence detection
     * and trajectory recording.</p>
     */
    private int currentStep;

    /**
     * PURPOSE: Convergence flag indicating if execution has completed.
     * 
     * <p>ARCHITECTURE: Set by convergence detector. When true, runUntilConvergence
     * will terminate early.</p>
     */
    private boolean converged;

    /**
     * PURPOSE: Running flag to control execution lifecycle.
     * 
     * <p>ARCHITECTURE: Set to true during run(), allows early termination via stop().</p>
     */
    private volatile boolean running;

    /**
     * PURPOSE: Sort direction for INSERTION algotype's isLeftSorted check.
     * 
     * <p>ARCHITECTURE: Used to support cross-purpose sorting where cells can
     * sort in ascending or descending order.</p>
     */
    private boolean reverseDirection;

    /**
     * Initialize the synchronous execution engine.
     *
     * <p>PURPOSE: Set up all required components for single-threaded trial execution.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>cells - Array of cells to sort</li>
     *   <li>swapEngine - Engine for executing swaps</li>
     *   <li>probe - Metrics recorder</li>
     *   <li>convergenceDetector - Determines when to stop</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Store all component references</li>
     *   <li>Initialize topology helpers</li>
     *   <li>Create random instance with default seed</li>
     *   <li>Initialize state variables (step=0, converged=false)</li>
     *   <li>Wire probe to swap engine for frozen swap tracking</li>
     *   <li>Record initial snapshot (step 0)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Fully initialized engine ready for execution</p>
     *
     * <p>DEPENDENCIES: All constructor parameters must be non-null</p>
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {
        // SCAFFOLD: Constructor body not yet implemented
        // TODO: Store all parameters as fields
        // TODO: Initialize topology helpers (bubble, insertion, selection)
        // TODO: Create Random instance
        // TODO: Initialize state (currentStep=0, converged=false, running=false)
        // TODO: Wire probe to swapEngine
        // TODO: Record initial snapshot
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Initialize with explicit random seed for reproducibility.
     *
     * <p>PURPOSE: Allow deterministic execution for testing and validation.</p>
     *
     * <p>INPUTS: Same as primary constructor, plus random seed</p>
     *
     * <p>PROCESS: Same as primary constructor, but uses seeded Random instance</p>
     *
     * <p>OUTPUTS: Fully initialized engine with deterministic random behavior</p>
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     * @param random the random instance for direction selection
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector,
            Random random) {
        // SCAFFOLD: Constructor body not yet implemented
        // TODO: Same as primary constructor but use provided Random instance
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Execute a single step of synchronous sorting.
     *
     * <p>PURPOSE: Main iteration logic - evaluates all cells, resolves conflicts,
     * executes swaps, and records metrics. Runs synchronously in calling thread.</p>
     *
     * <p>INPUTS: None (operates on engine state)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Get iteration order (all cells 0 to N-1)</li>
     *   <li>Reset swap counter for this step</li>
     *   <li>For each cell in iteration order:
     *     <ul>
     *       <li>Get algotype and sort direction</li>
     *       <li>Get neighbors based on algotype</li>
     *       <li>For BUBBLE: randomly pick ONE neighbor (50/50 left/right)</li>
     *       <li>For each neighbor: check if swap should occur</li>
     *       <li>If yes: create SwapProposal and add to list</li>
     *       <li>Record comparison via probe</li>
     *     </ul>
     *   </li>
     *   <li>Resolve conflicts (priority = leftmost cell first)</li>
     *   <li>Execute non-conflicting swaps via swapEngine</li>
     *   <li>Record snapshot with step number and swap count</li>
     *   <li>Check convergence via convergenceDetector</li>
     *   <li>Increment currentStep</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Number of swaps executed in this step</p>
     *
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>evaluateCell() for individual cell evaluation</li>
     *   <li>resolveConflicts() for conflict resolution</li>
     *   <li>swapEngine.attemptSwap() for swap execution</li>
     *   <li>probe.recordSnapshot() for metrics</li>
     *   <li>convergenceDetector.hasConverged() for termination check</li>
     * </ul>
     * </p>
     *
     * <p>GROUND TRUTH REFERENCE: Matches ExecutionEngine.step() synchronous logic
     * but eliminates parallel barriers and CellThread overhead.</p>
     *
     * @return number of swaps performed in this step
     */
    public int step() {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement synchronous step logic as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Run execution until convergence or max steps reached.
     *
     * <p>PURPOSE: Main entry point for trial execution. Runs step() repeatedly
     * until convergence is detected or maximum steps are exceeded.</p>
     *
     * <p>INPUTS: maxSteps - maximum number of steps before timeout</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Set running flag to true</li>
     *   <li>While not converged AND currentStep < maxSteps:
     *     <ul>
     *       <li>Call step()</li>
     *       <li>Check if running flag is still true (allow early termination)</li>
     *     </ul>
     *   </li>
     *   <li>Set running flag to false</li>
     *   <li>Return final step count</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Total number of steps executed</p>
     *
     * <p>DEPENDENCIES: step() method for iteration logic</p>
     *
     * <p>ARCHITECTURE NOTE: This is the method called by ExperimentRunner when
     * executing a trial in a worker thread from ExecutorService pool.</p>
     *
     * @param maxSteps maximum number of steps to execute
     * @return total number of steps executed
     */
    public int runUntilConvergence(int maxSteps) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement run loop as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Evaluate a single cell and determine if it should propose a swap.
     *
     * <p>PURPOSE: Encapsulates cell evaluation logic for a specific index.
     * Determines neighbors based on algotype and checks if swap is beneficial.</p>
     *
     * <p>INPUTS: cellIndex - index of cell to evaluate</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Get cell reference from array</li>
     *   <li>Get algotype and sort direction from cell</li>
     *   <li>Get neighbors based on algotype:
     *     <ul>
     *       <li>BUBBLE: left and/or right adjacent cells</li>
     *       <li>INSERTION: all cells to the left</li>
     *       <li>SELECTION: ideal position target</li>
     *     </ul>
     *   </li>
     *   <li>For BUBBLE: randomly select ONE neighbor (50/50 probability)</li>
     *   <li>For each neighbor:
     *     <ul>
     *       <li>Record comparison via probe</li>
     *       <li>Check shouldSwapWithDirection()</li>
     *       <li>If true: return SwapProposal</li>
     *     </ul>
     *   </li>
     *   <li>If no beneficial swap found: return empty Optional</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Optional<SwapProposal> - proposal if swap is beneficial, empty otherwise</p>
     *
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>getNeighborsForAlgotype() for neighbor determination</li>
     *   <li>getCellDirection() for sort direction</li>
     *   <li>shouldSwapWithDirection() for swap decision</li>
     * </ul>
     * </p>
     *
     * @param cellIndex index of cell to evaluate
     * @return swap proposal if beneficial, empty otherwise
     */
    private Optional<SwapProposal> evaluateCell(int cellIndex) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement cell evaluation logic as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Resolve conflicts in swap proposals using leftmost-priority rule.
     *
     * <p>PURPOSE: Ensure no cell participates in more than one swap per step.
     * Resolves conflicts by prioritizing leftmost cells (lowest indices).</p>
     *
     * <p>INPUTS: proposals - list of all swap proposals from cell evaluation</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Sort proposals by initiator index (leftmost first)</li>
     *   <li>Create empty resolved list</li>
     *   <li>Create set to track cells already involved in swaps</li>
     *   <li>For each proposal in sorted order:
     *     <ul>
     *       <li>Get initiator and target indices</li>
     *       <li>If either is already in involved set: skip proposal</li>
     *       <li>Otherwise: add to resolved list, mark both as involved</li>
     *     </ul>
     *   </li>
     *   <li>Return resolved list</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: List of non-conflicting swap proposals</p>
     *
     * <p>DEPENDENCIES: SwapProposal.compareTo() for sorting by priority</p>
     *
     * <p>GROUND TRUTH REFERENCE: Matches ParallelExecutionEngine.resolveConflicts()
     * but operates on simple ArrayList instead of ConcurrentSwapCollector.</p>
     *
     * @param proposals list of all swap proposals
     * @return list of non-conflicting proposals to execute
     */
    private List<SwapProposal> resolveConflicts(List<SwapProposal> proposals) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement conflict resolution as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Get neighbors for a cell based on its algotype.
     *
     * <p>PURPOSE: Determine which cells a given cell should consider for swapping.</p>
     *
     * <p>INPUTS: i - cell index, algotype - algorithm type</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Switch on algotype:
     *     <ul>
     *       <li>BUBBLE: return bubbleTopology.getNeighbors()</li>
     *       <li>INSERTION: return insertionTopology.getNeighbors()</li>
     *       <li>SELECTION: get ideal position, return as singleton list</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: List of neighbor indices</p>
     *
     * @param i cell index
     * @param algotype algorithm type
     * @return list of neighbor indices
     */
    private List<Integer> getNeighborsForAlgotype(int i, Algotype algotype) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement neighbor determination as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Get the sort direction of a cell.
     *
     * <p>PURPOSE: Determine if cell prefers ascending or descending sort order.</p>
     *
     * <p>INPUTS: cell - the cell to query</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Check if cell implements HasSortDirection</li>
     *   <li>If yes: return cell.getSortDirection()</li>
     *   <li>If no: return SortDirection.ASCENDING (default)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: SortDirection (ASCENDING or DESCENDING)</p>
     *
     * @param cell the cell to query
     * @return sort direction preference
     */
    private SortDirection getCellDirection(T cell) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement direction check as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Determine if swap should occur based on algotype rules and direction.
     *
     * <p>PURPOSE: Core swap decision logic respecting algotype semantics and
     * individual cell sort direction preferences.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>i - initiator cell index</li>
     *   <li>j - target cell index</li>
     *   <li>algotype - algorithm type</li>
     *   <li>direction - sort direction preference</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS: Varies by algotype:
     * <ul>
     *   <li>BUBBLE: Compare values, swap if moving toward sorted position</li>
     *   <li>INSERTION: Check isLeftSorted, then compare values</li>
     *   <li>SELECTION: Compare with ideal target, increment if denied</li>
     * </ul>
     * Direction inverts comparison polarity (ascending vs descending).
     * </p>
     *
     * <p>OUTPUTS: true if swap should occur, false otherwise</p>
     *
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>isLeftSorted() for INSERTION algotype</li>
     *   <li>getIdealPosition()/incrementIdealPosition() for SELECTION</li>
     * </ul>
     * </p>
     *
     * @param i initiator index
     * @param j target index
     * @param algotype algorithm type
     * @param direction sort direction
     * @return true if swap should occur
     */
    private boolean shouldSwapWithDirection(int i, int j, Algotype algotype, SortDirection direction) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement swap decision logic for all algotypes with direction support
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Check if cells 0 to i-1 are sorted in the specified direction.
     *
     * <p>PURPOSE: Used by INSERTION algotype to verify left side is sorted
     * before attempting to insert current cell into sorted region.</p>
     *
     * <p>INPUTS: i - position to check, reverseDirection - true for descending</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Set sentinel value (MIN for ascending, MAX for descending)</li>
     *   <li>For k = 0 to i-1:
     *     <ul>
     *       <li>If cell k is frozen: reset sentinel (skip frozen cells)</li>
     *       <li>Otherwise: get value and check order</li>
     *       <li>If out of order: return false</li>
     *       <li>Update sentinel to current value</li>
     *     </ul>
     *   </li>
     *   <li>If all in order: return true</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: true if sorted, false otherwise</p>
     *
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>swapEngine.isFrozen() for frozen cell check</li>
     *   <li>getCellValue() for value extraction</li>
     * </ul>
     * </p>
     *
     * @param i position to check (checks 0 to i-1)
     * @param reverseDirection true for descending sort
     * @return true if sorted in specified direction
     */
    private boolean isLeftSorted(int i, boolean reverseDirection) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement sorted check with frozen cell handling
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Extract comparable value from a cell.
     *
     * <p>PURPOSE: Provide uniform value access across different cell types.</p>
     *
     * <p>INPUTS: cell - the cell to extract value from</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Check cell type via instanceof</li>
     *   <li>Cast and call appropriate getValue() method</li>
     *   <li>If unsupported type: throw UnsupportedOperationException</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: int value for comparison</p>
     *
     * @param cell the cell
     * @return comparable value
     */
    private int getCellValue(T cell) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement type-safe value extraction
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Get ideal position from a SELECTION algotype cell.
     *
     * <p>PURPOSE: Access ideal position field for SELECTION cells.</p>
     *
     * <p>INPUTS: cell - the cell (must be SELECTION algotype)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Check if cell is SelectionCell or GenericCell</li>
     *   <li>Cast and call getIdealPos()</li>
     *   <li>If not SELECTION type: return 0 (default)</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: ideal position index</p>
     *
     * @param cell the cell
     * @return ideal position
     */
    private int getIdealPosition(T cell) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement ideal position access
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Increment ideal position for a SELECTION algotype cell.
     *
     * <p>PURPOSE: Update target position when swap is denied.</p>
     *
     * <p>INPUTS: cell - the cell (must be SELECTION algotype)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Check if cell is SelectionCell or GenericCell</li>
     *   <li>Cast and call incrementIdealPos()</li>
     *   <li>If not SELECTION type: no-op</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: None (modifies cell state)</p>
     *
     * @param cell the cell
     */
    private void incrementIdealPosition(T cell) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement ideal position increment
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Reset execution state to initial conditions.
     *
     * <p>PURPOSE: Allow engine reuse for multiple trials with same cell array.</p>
     *
     * <p>INPUTS: None</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Reset currentStep to 0</li>
     *   <li>Reset converged to false</li>
     *   <li>Reset running to false</li>
     *   <li>Clear probe snapshots</li>
     *   <li>Reset swap engine state</li>
     *   <li>Reset topology helpers</li>
     *   <li>Reset convergence detector</li>
     *   <li>Reset SELECTION cell ideal positions</li>
     *   <li>Record initial snapshot</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: None (modifies engine state)</p>
     *
     * @param reverseDirection true for descending sort, false for ascending
     */
    public void reset(boolean reverseDirection) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement reset logic as described above
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Reset with default ascending sort direction.
     */
    public void reset() {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Call reset(false)
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Reset SELECTION cell ideal positions to boundary.
     *
     * <p>PURPOSE: Initialize SELECTION cells to correct starting position
     * based on sort direction.</p>
     *
     * <p>INPUTS: reverseDirection - true for descending (start at right boundary)</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Calculate left boundary = 0</li>
     *   <li>Calculate right boundary = cells.length - 1</li>
     *   <li>For each cell:
     *     <ul>
     *       <li>If algotype is SELECTION</li>
     *       <li>And cell implements HasIdealPosition</li>
     *       <li>Call updateForBoundary(left, right, reverseDirection)</li>
     *     </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: None (modifies cell state)</p>
     *
     * @param reverseDirection true for descending sort
     */
    private void resetSelectionCellIdealPositions(boolean reverseDirection) {
        // SCAFFOLD: Method body not yet implemented
        // TODO: Implement SELECTION cell boundary reset
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    // ========== Accessors ==========

    /**
     * Get the cell array (returns reference, not copy, for performance).
     */
    public T[] getCells() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Get the current step number.
     */
    public int getCurrentStep() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Check if execution has converged.
     */
    public boolean hasConverged() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Check if execution is currently running.
     */
    public boolean isRunning() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Get the probe for trajectory analysis.
     */
    public Probe<T> getProbe() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }

    /**
     * Stop execution early (sets running flag to false).
     * 
     * <p>PURPOSE: Allow graceful shutdown of long-running trials.</p>
     * 
     * <p>PROCESS: Set running = false, which will cause runUntilConvergence
     * to exit at the end of the current step.</p>
     */
    public void stop() {
        // SCAFFOLD: Method body not yet implemented
        throw new UnsupportedOperationException("Phase One: Scaffold only - not yet implemented");
    }
}
