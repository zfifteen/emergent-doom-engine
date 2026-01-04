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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

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
     * PURPOSE: Metadata array storing execution behavior for each cell position.
     * 
     * <p>ARCHITECTURE: Parallel array indexed by cell position. When metadata provider
     * is used, this array stores algotype, sort direction, and ideal position state
     * that would otherwise be queried from cell objects. This enables lightweight cells
     * that are pure Comparable data carriers.</p>
     * 
     * <p>INPUTS: Initialized from IntFunction&lt;CellMetadata&gt; provider in constructor</p>
     * 
     * <p>PROCESS: May be swapped alongside cells during step() to keep metadata
     * attached to logical agent identity (implementation depends on swap strategy)</p>
     * 
     * <p>OUTPUTS: metadata[i] provides CellMetadata for cell at position i</p>
     * 
     * <p>DEPENDENCIES: May be null for legacy mode (backward compatibility)</p>
     */
    private CellMetadata[] metadata;

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
     * Initialize the synchronous execution engine with default random seed.
     *
     * <p>PURPOSE: Set up all required components for single-threaded trial execution
     * using an unseeded Random instance (non-deterministic behavior).</p>
     *
     * <p><strong>DETERMINISM NOTE:</strong> This constructor uses {@code new Random()}
     * which is NOT seeded, resulting in non-deterministic execution. For reproducible
     * experiments and testing, use the constructor that accepts a {@link Random} parameter:
     * {@link #SynchronousExecutionEngine(Cell[], SwapEngine, Probe, ConvergenceDetector, Random)}
     * with a pre-seeded Random instance, e.g.:</p>
     * <pre>
     * // For deterministic, reproducible execution:
     * Random seededRandom = new Random(12345L);
     * new SynchronousExecutionEngine<>(cells, swapEngine, probe, detector, seededRandom);
     * </pre>
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
     * @see #SynchronousExecutionEngine(Cell[], SwapEngine, Probe, ConvergenceDetector, Random)
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {
        this(cells, swapEngine, probe, convergenceDetector, new Random());
    }

    /**
     * Initialize with explicit Random instance for deterministic execution.
     *
     * <p>PURPOSE: Allow deterministic execution for testing, validation, and
     * reproducible experiments by accepting a pre-seeded Random instance.</p>
     *
     * <p><strong>USAGE FOR DETERMINISM:</strong> Pass a seeded Random instance
     * to ensure reproducible execution across runs:</p>
     * <pre>
     * // Deterministic execution with seed 42
     * Random seededRandom = new Random(42L);
     * SynchronousExecutionEngine<MyCell> engine = new SynchronousExecutionEngine<>(
     *     cells, swapEngine, probe, detector, seededRandom);
     * </pre>
     *
     * <p>INPUTS: Same as primary constructor, plus Random instance</p>
     *
     * <p>PROCESS: Same as primary constructor, but uses provided Random instance</p>
     *
     * <p>OUTPUTS: Fully initialized engine with deterministic random behavior</p>
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     * @param random the Random instance for direction selection (use seeded instance for determinism)
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector,
            Random random) {
        this.cells = cells;
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.random = random;
        
        // Initialize topology helpers
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();
        this.selectionTopology = new SelectionTopology<>();
        
        // No metadata provider - legacy mode (backward compatibility)
        this.metadata = null;
        
        // Initialize state
        this.currentStep = 0;
        this.converged = false;
        this.running = false;
        this.reverseDirection = false;  // Default to ascending sort
        
        // Wire up probe to swap engine for frozen swap attempt tracking
        swapEngine.setProbe(probe);
        
        // Record initial state
        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Initialize with metadata provider for lightweight cells.
     *
     * <p>PURPOSE: Create engine with externally-managed metadata array, enabling
     * cells to be pure Comparable wrappers without execution metadata.</p>
     *
     * <p>INPUTS:
     * <ul>
     *   <li>cells - Array of cells to sort (may be pure Comparable wrappers)</li>
     *   <li>swapEngine - Swap execution and frozen cell tracking</li>
     *   <li>probe - Metrics and trajectory recording</li>
     *   <li>convergenceDetector - Determines when execution completes</li>
     *   <li>metadataProvider - Function mapping index → CellMetadata</li>
     *   <li>random - Random instance for BUBBLE direction choice</li>
     * </ul>
     * </p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>Store all component references</li>
     *   <li>Initialize topology helpers</li>
     *   <li>Create metadata array from provider: metadata[i] = metadataProvider.apply(i)</li>
     *   <li>Initialize state variables</li>
     *   <li>Wire probe to swap engine</li>
     *   <li>Record initial snapshot</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Fully initialized engine using metadata provider pattern</p>
     *
     * <p>DEPENDENCIES: metadataProvider must return non-null CellMetadata for all valid indices</p>
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     * @param metadataProvider function providing metadata for each cell index
     * @param random the Random instance for direction selection
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector,
            java.util.function.IntFunction<CellMetadata> metadataProvider,
            Random random) {

        // PURPOSE: Initialize engine with metadata provider pattern
        // PROCESS:
        //   1. Store component references
        //   2. Initialize topology helpers
        //   3. Create metadata array from provider
        //   4. Initialize state variables
        //   5. Wire probe and record initial state

        this.cells = cells;
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.random = random;
        
        // Initialize topology helpers
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();
        this.selectionTopology = new SelectionTopology<>();
        
        // PHASE TWO: Initialize metadata from provider
        // PURPOSE: Populate metadata array by calling provider function for each index
        // PROCESS:
        //   1. Create metadata array with same length as cells array
        //   2. For each index i, call metadataProvider.apply(i) to get metadata
        //   3. Store result in metadata[i]
        // OUTPUTS: Fully populated metadata array
        // DATA FLOW: metadataProvider(index) -> CellMetadata -> metadata[index]
        this.metadata = new CellMetadata[cells.length];
        for (int i = 0; i < cells.length; i++) {
            this.metadata[i] = metadataProvider.apply(i);
        }
        
        // Initialize state
        this.currentStep = 0;
        this.converged = false;
        this.running = false;
        this.reverseDirection = false;  // Default to ascending sort
        
        // Wire up probe to swap engine for frozen swap attempt tracking
        swapEngine.setProbe(probe);
        
        // Record initial state
        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Convenience constructor with metadata provider and default Random.
     *
     * <p>PURPOSE: Create engine with metadata provider using unseeded Random
     * (non-deterministic execution). For deterministic execution, use the constructor
     * that accepts both metadataProvider and seeded Random instance.</p>
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     * @param metadataProvider function providing metadata for each cell index
     */
    public SynchronousExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector,
            java.util.function.IntFunction<CellMetadata> metadataProvider) {
        this(cells, swapEngine, probe, convergenceDetector, metadataProvider, new Random());
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
        // PHASE TWO: Implement synchronous step logic
        // Get iteration order (use bubble topology as default, all are sequential)
        List<Integer> iterationOrder = bubbleTopology.getIterationOrder(cells.length);

        // Reset swap counter for this step
        swapEngine.resetSwapCount();

        // Collect all swap proposals from cell evaluations
        List<SwapProposal> proposedSwaps = new ArrayList<>();
        
        // For each cell in iteration order, try swapping with neighbors based on algotype
        for (int i : iterationOrder) {
            Algotype algotype = getCellAlgotype(i);
            SortDirection direction = getCellDirection(cells[i]);

            if (algotype == Algotype.BUBBLE) {
                // Random 50/50 direction choice - matches cell_research Python behavior
                // Each iteration, cell randomly picks ONE direction (left or right), not both
                List<Integer> allNeighbors = getNeighborsForAlgotype(i, algotype);
                if (!allNeighbors.isEmpty()) {
                    // Pick ONE random neighbor (50% left, 50% right if both exist)
                    int randomIndex = random.nextInt(allNeighbors.size());
                    int j = allNeighbors.get(randomIndex);
                    // Record comparison before checking shouldSwap
                    probe.recordCompareAndSwap();
                    boolean shouldSwap = shouldSwapWithDirection(i, j, algotype, direction);
                    if (shouldSwap) {
                        proposedSwaps.add(new SwapProposal(i, j));
                    }
                }
            } else {
                // Other algotypes: iterate all neighbors
                List<Integer> neighbors = getNeighborsForAlgotype(i, algotype);
                for (int j : neighbors) {
                    // Record comparison before checking shouldSwap
                    probe.recordCompareAndSwap();
                    boolean shouldSwap = shouldSwapWithDirection(i, j, algotype, direction);
                    if (shouldSwap) {
                        proposedSwaps.add(new SwapProposal(i, j));
                    }
                }
            }
        }

        // Resolve conflicts (leftmost priority)
        List<SwapProposal> resolvedSwaps = resolveConflicts(proposedSwaps);

        // Execute approved swaps
        for (SwapProposal proposal : resolvedSwaps) {
            int i = proposal.getInitiatorIndex();
            int j = proposal.getTargetIndex();

            swapEngine.attemptSwap(cells, i, j);

            // PHASE THREE: Swap metadata alongside cells
            // PURPOSE: Keep metadata attached to logical agent identity as cells move
            // PROCESS:
            //   1. Check if metadata provider mode (metadata != null)
            //   2. If yes, swap metadata[i] and metadata[j] using temp variable
            //   3. This ensures metadata[i] always describes behavior of cell at position i
            // RATIONALE: When cells swap positions, their metadata must follow them
            //   to maintain the invariant that metadata[i] describes cells[i]
            if (metadata != null) {
                CellMetadata tempMetadata = metadata[i];
                metadata[i] = metadata[j];
                metadata[j] = tempMetadata;
            }
        }

        // Get swap count for this step
        int swaps = swapEngine.getSwapCount();

        // Increment step counter
        currentStep++;

        // Record snapshot
        probe.recordSnapshot(currentStep, cells, swaps);

        // Check convergence
        converged = convergenceDetector.hasConverged(probe, currentStep);

        return swaps;
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
        // PHASE TWO: Implement main entry point logic
        running = true;
        
        while (!converged && currentStep < maxSteps && running) {
            step();
        }
        
        running = false;
        return currentStep;
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
        // Sort by priority (initiator index - leftmost first)
        Collections.sort(proposals);
        
        List<SwapProposal> resolved = new ArrayList<>();
        Set<Integer> involvedCells = new HashSet<>();
        
        for (SwapProposal proposal : proposals) {
            int initiator = proposal.getInitiatorIndex();
            int target = proposal.getTargetIndex();
            
            // Skip if either cell is already involved in a swap this step
            if (involvedCells.contains(initiator) || involvedCells.contains(target)) {
                continue;
            }
            
            // Accept this proposal
            involvedCells.add(initiator);
            involvedCells.add(target);
            resolved.add(proposal);
        }
        
        return resolved;
    }

    // ========== Helper Methods for Metadata/Cell Access ==========

    /**
     * Get algotype from metadata provider (if available) or cell (legacy fallback).
     *
     * <p>PURPOSE: Support both metadata provider pattern and legacy cell introspection
     * for backward compatibility during migration.</p>
     *
     * <p>INPUTS: cellIndex - position of cell to query</p>
     *
     * <p>PROCESS:
     * <ol>
     *   <li>If metadata != null: return metadata[cellIndex].getAlgotype()</li>
     *   <li>Otherwise: cast cell to HasAlgotype and call getAlgotype()</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: Algotype for this cell position</p>
     *
     * <p>DEPENDENCIES: In legacy mode, cell must implement HasAlgotype</p>
     */
    private Algotype getCellAlgotype(int cellIndex) {
        // PHASE TWO: Use metadata if available (metadata provider mode)
        // PURPOSE: Query algotype from metadata array instead of cell
        // PROCESS: Check if metadata array exists, if yes return metadata[cellIndex].getAlgotype()
        // BENEFITS: Enables lightweight cells without embedded algotype field
        if (metadata != null) {
            return metadata[cellIndex].getAlgotype();
        }

        // Legacy mode: query cell directly (requires HasAlgotype)
        // PURPOSE: Maintain backward compatibility with existing cell implementations
        // PROCESS: Cast cell to HasAlgotype interface and call getAlgotype()
        // FALLBACK: Used when no metadata provider was given to constructor
        if (cells[cellIndex] instanceof com.emergent.doom.cell.HasAlgotype) {
            return ((com.emergent.doom.cell.HasAlgotype) cells[cellIndex]).getAlgotype();
        }

        throw new IllegalStateException(
            "Cell at index " + cellIndex + " does not implement HasAlgotype and no metadata provider was given");
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
        switch (algotype) {
            case BUBBLE:
                return bubbleTopology.getNeighbors(i, cells.length, algotype);
            case INSERTION:
                return insertionTopology.getNeighbors(i, cells.length, algotype);
            case SELECTION:
                // Get dynamic ideal target from cell state
                int idealPos = getIdealPosition(cells[i]);
                int target = Math.min(idealPos, cells.length - 1);
                return Arrays.asList(target);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
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
        // Check if cell implements HasSortDirection interface
        if (cell instanceof HasSortDirection) {
            // Cell supports direction - return its preference
            return ((HasSortDirection) cell).getSortDirection();
        }
        // Cell doesn't support direction - default to ascending
        return SortDirection.ASCENDING;
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
        // Get comparison result: negative if cells[i] < cells[j], positive if cells[i] > cells[j]
        int cmp = cells[i].compareTo(cells[j]);
        boolean isAscending = direction.isAscending();
        
        switch (algotype) {
            case BUBBLE:
                // BUBBLE: Move based on value comparison and direction
                // For ascending: move left if smaller, right if larger
                // For descending: move left if larger, right if smaller
                
                if (j == i - 1) { // Left neighbor
                    // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                    return isAscending ? (cmp < 0) : (cmp > 0);
                } else if (j == i + 1) { // Right neighbor
                    // Ascending: swap if i > j (cmp > 0), Descending: swap if i < j (cmp < 0)
                    return isAscending ? (cmp > 0) : (cmp < 0);
                }
                return false;
                
            case INSERTION:
                // INSERTION: Only move left, and only if left side is sorted
                if (j == i - 1 && isLeftSorted(i, !isAscending)) {
                    // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                    return isAscending ? (cmp < 0) : (cmp > 0);
                }
                return false;
                
            case SELECTION:
                // Guard: Skip if targeting self
                if (i == j) {
                    return false;
                }
                
                // SELECTION: Swap with ideal target if in correct order
                // Ascending: swap if i < j (cmp < 0), Descending: swap if i > j (cmp > 0)
                boolean shouldSwap = isAscending ? (cmp < 0) : (cmp > 0);
                
                if (shouldSwap) {
                    return true;
                } else {
                    // Swap denied: increment ideal position if not at end
                    int currentIdealPos = getIdealPosition(cells[i]);
                    if (currentIdealPos < cells.length - 1) {
                        incrementIdealPosition(cells[i]);
                    }
                    return false;
                }
                
            default:
                return false;
        }
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
     *   <li>For k = 0 to i-2:
     *     <ul>
     *       <li>If cell k or k+1 is frozen: skip pair (matches Python)</li>
     *       <li>Compare cells[k] and cells[k+1]</li>
     *       <li>If out of order: return false</li>
     *     </ul>
     *   </li>
     *   <li>If all in order: return true</li>
     * </ol>
     * </p>
     *
     * <p>OUTPUTS: true if sorted, false otherwise</p>
     *
     * @param i position to check (checks 0 to i-1)
     * @param reverseDirection true for descending sort
     * @return true if sorted in specified direction
     */
    private boolean isLeftSorted(int i, boolean reverseDirection) {
        for (int k = 0; k < i - 1; k++) {
            // Skip frozen cells - reset comparison chain (matches Python)
            if (swapEngine.isFrozen(k) || swapEngine.isFrozen(k + 1)) {
                continue;
            }

            int cmp = cells[k].compareTo(cells[k + 1]);
            
            // Check if out of order based on direction
            boolean outOfOrder = reverseDirection 
                ? (cmp < 0)  // Descending: should be cells[k] >= cells[k+1] (cmp >= 0)
                : (cmp > 0); // Ascending: should be cells[k] <= cells[k+1] (cmp <= 0)
            
            if (outOfOrder) {
                return false; // Out of order
            }
        }
        return true;
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
        if (cell instanceof com.emergent.doom.cell.HasValue) {
            return ((com.emergent.doom.cell.HasValue) cell).getValue();
        }
        if (cell instanceof com.emergent.doom.cell.SelectionCell) {
            return ((com.emergent.doom.cell.SelectionCell<?>) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.InsertionCell) {
            return ((com.emergent.doom.cell.InsertionCell<?>) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.BubbleCell) {
            return ((com.emergent.doom.cell.BubbleCell<?>) cell).getValue();
        }
        // Fail-fast: throw exception for unsupported cell types
        // (hashCode is unreliable for sorting - doesn't maintain ordering relationships)
        throw new UnsupportedOperationException(
            "Cell type " + cell.getClass().getName() + " does not support getValue(). " +
            "All Cell implementations must extend SelectionCell, GenericCell, InsertionCell, or BubbleCell, or implement HasValue."
        );
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
        if (cell instanceof com.emergent.doom.cell.HasIdealPosition) {
            return ((com.emergent.doom.cell.HasIdealPosition) cell).getIdealPos();
        }
        if (cell instanceof SelectionCell) {
            return ((SelectionCell<?>) cell).getIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getIdealPos();
        }
        return 0;  // Default for other cell types
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
        if (cell instanceof com.emergent.doom.cell.HasIdealPosition) {
            ((com.emergent.doom.cell.HasIdealPosition) cell).incrementIdealPos();
            return;
        }
        if (cell instanceof SelectionCell) {
            ((SelectionCell<?>) cell).incrementIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            ((com.emergent.doom.cell.GenericCell) cell).incrementIdealPos();
        }
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
        currentStep = 0;
        converged = false;
        running = false;
        this.reverseDirection = reverseDirection;  // Store for isLeftSorted
        probe.clear();
        swapEngine.resetSwapCount();
        bubbleTopology.reset();
        insertionTopology.reset();
        selectionTopology.reset();
        convergenceDetector.reset();

        // Reset SELECTION cell ideal positions to boundary (matches Python cell_research)
        resetSelectionCellIdealPositions(reverseDirection);

        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Reset with default ascending sort direction.
     */
    public void reset() {
        reset(false);
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
        int leftBoundary = 0;
        int rightBoundary = cells.length - 1;

        for (int i = 0; i < cells.length; i++) {
            Algotype algotype = getCellAlgotype(i);
            if (algotype == Algotype.SELECTION) {
                if (cells[i] instanceof HasIdealPosition) {
                    ((HasIdealPosition) cells[i]).updateForBoundary(leftBoundary, rightBoundary, reverseDirection);
                }
            }
        }
    }

    // ========== Accessors ==========

    /**
     * Get the cell array (returns reference, not copy, for performance).
     */
    public T[] getCells() {
        return cells;
    }

    /**
     * Get the current step number.
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Check if execution has converged.
     */
    public boolean hasConverged() {
        return converged;
    }

    /**
     * Check if execution is currently running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get the probe for trajectory analysis.
     */
    public Probe<T> getProbe() {
        return probe;
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
        running = false;
    }
}
