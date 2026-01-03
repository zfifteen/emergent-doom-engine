package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasIdealPosition;
import com.emergent.doom.cell.HasSortDirection;
import com.emergent.doom.cell.SortDirection;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.InsertionTopology;
import com.emergent.doom.topology.SelectionTopology;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main execution engine that orchestrates cell dynamics.
 * 
 * <p>The ExecutionEngine is the heart of the EDE system, coordinating:
 * <ul>
 *   <li>Cell array management</li>
 *   <li>Swap attempts based on topology</li>
 *   <li>Probe recording</li>
 *   <li>Convergence detection</li>
 *   <li>Step-by-step iteration</li>
 * </ul>
 * </p>
 * 
 * @param <T> the type of cell
 */
public class ExecutionEngine<T extends Cell<T>> {
    
    private final T[] cells;
    private final BubbleTopology<T> bubbleTopology;
    private final InsertionTopology<T> insertionTopology;
    private final SelectionTopology<T> selectionTopology;
    private final SwapEngine<T> swapEngine;
    private final Probe<T> probe;
    private final ConvergenceDetector<T> convergenceDetector;
    private final CellMetadata[] metadata;
    private final Random random;

    private int currentStep;
    private boolean converged;
    private boolean reverseDirection;  // Track sort direction for isLeftSorted
    
    /**
     * IMPLEMENTED: Initialize the execution engine with algotype-based topology dispatch
     */
    public ExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {
        this(cells, swapEngine, probe, convergenceDetector, new Random());
    }

    private void initializeMetadata(T[] cells) {
        for (int i = 0; i < cells.length; i++) {
            Algotype algotype = Algotype.BUBBLE;
            SortDirection direction = SortDirection.ASCENDING;
            int idealPos = 0;
            int left = 0;
            int right = cells.length - 1;

            T cell = cells[i];
            if (cell instanceof com.emergent.doom.cell.HasAlgotype) {
                algotype = ((com.emergent.doom.cell.HasAlgotype) cell).getAlgotype();
            }

            if (cell instanceof com.emergent.doom.cell.HasSortDirection) {
                direction = ((com.emergent.doom.cell.HasSortDirection) cell).getSortDirection();
            }

            if (cell instanceof com.emergent.doom.cell.HasIdealPosition) {
                idealPos = ((com.emergent.doom.cell.HasIdealPosition) cell).getIdealPos();
            }

            if (cell instanceof com.emergent.doom.group.GroupAwareCell) {
                left = ((com.emergent.doom.group.GroupAwareCell<?>) cell).getLeftBoundary();
                right = ((com.emergent.doom.group.GroupAwareCell<?>) cell).getRightBoundary();
            }

            this.metadata[i] = new CellMetadata(algotype, direction, new AtomicInteger(idealPos), left, right);
        }
    }

    /**
     * Initialize the execution engine with a specific random seed for reproducibility.
     *
     * @param cells the cell array to sort
     * @param swapEngine the swap engine
     * @param probe the probe for recording
     * @param convergenceDetector the convergence detector
     * @param random the random instance for direction selection
     */
    public ExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector,
            Random random) {
        this.cells = cells;
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();
        this.selectionTopology = new SelectionTopology<>();
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.random = random;

        // Initialize metadata from cells
        this.metadata = new CellMetadata[cells.length];
        initializeMetadata(cells);

        this.currentStep = 0;
        this.converged = false;
        this.reverseDirection = false;  // Default to ascending sort

        // Wire up probe to swap engine for frozen swap attempt tracking
        swapEngine.setProbe(probe);

        // SelectionCell idealPos initialized to 0 in constructor (Levin competition)

        // Record initial state
        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Execute a single step of the emergent sorting algorithm.
     *
     * <p><strong>TIME COMPLEXITY: O(n) per step</strong></p>
     *
     * <p>This method processes ALL n cells in the array during each iteration,
     * making the per-step computational cost linear in array size. The iteration
     * order includes every cell index from 0 to n-1, and each cell evaluates
     * swap decisions with its topology-defined neighbors.</p>
     *
     * <p><strong>SCALING ANALYSIS:</strong></p>
     * <ul>
     *   <li><strong>Per-step work:</strong> O(n) - iterates through all n cells</li>
     *   <li><strong>Steps to convergence:</strong> Problem-dependent, often constant
     *       for a given problem class (e.g., ~130-140 steps for factorization
     *       experiments with array sizes 1000-4000)</li>
     *   <li><strong>Total complexity:</strong> O(steps × n)
     *       <ul>
     *         <li>If steps is constant: <strong>O(n) total time</strong></li>
     *         <li>If steps grows with n: O(f(n) × n) where f(n) is convergence rate</li>
     *       </ul>
     *   </li>
     * </ul>
     *
     * <p><strong>EXPERIMENTAL VALIDATION (Factorization Scaling Study):</strong></p>
     * <pre>
     * Array Size | Steps to Converge | Total Compare+Swap | Time (ms)
     * -----------|-------------------|--------------------|-----------
     *    1000    |       ~135        |      ~135,000     |    ~100
     *    2000    |       ~133        |      ~266,000     |    ~200
     *    3000    |       ~138        |      ~414,000     |    ~300
     *    4000    |       ~137        |      ~548,000     |    ~400
     *
     * Result: Steps constant (~130-140), time and operations scale O(n)
     * </pre>
     *
     * <p><strong>WHY STEPS CAN BE CONSTANT:</strong></p>
     * <p>Convergence depends on the problem structure (e.g., remainder landscape
     * for factorization), not the search space size. Once cells reach local
     * equilibrium where no beneficial swaps exist, the system converges regardless
     * of how many total candidate positions were available. This is a key property
     * of emergent optimization - the solution quality depends on the fitness
     * landscape, not exhaustive enumeration.</p>
     *
     * <p><strong>IMPLICATIONS:</strong></p>
     * <ul>
     *   <li>Larger arrays don't necessarily take more steps to converge</li>
     *   <li>Time complexity is dominated by per-step O(n) cell processing</li>
     *   <li>For problems where convergence is constant-time, this yields
     *       linear-time factorization candidate discovery</li>
     * </ul>
     *
     * @return number of swaps performed in this step (used for convergence detection)
     */
    public int step() {
        // Get iteration order (use bubble topology as default, all are sequential)
        List<Integer> iterationOrder = bubbleTopology.getIterationOrder(cells.length);

        // Reset swap counter for this step
        swapEngine.resetSwapCount();

        // For each cell in iteration order, try swapping with neighbors based on algotype
        for (int i : iterationOrder) {
            Algotype algotype = metadata[i].algotype();
            SortDirection direction = metadata[i].direction();

            if (algotype == Algotype.BUBBLE) {
                // Random 50/50 direction choice - matches cell_research Python behavior
                // Each iteration, cell randomly picks ONE direction (left or right), not both
                List<Integer> allNeighbors = getNeighborsForAlgotype(i, algotype);
                if (!allNeighbors.isEmpty()) {
                    // Pick ONE random neighbor (50% left, 50% right if both exist)
                    int randomIndex = random.nextInt(allNeighbors.size());
                    int j = allNeighbors.get(randomIndex);
                    // CRITICAL FIX: Record comparison before checking shouldSwap
                    // Python tracks ALL comparisons, not just those leading to swaps
                    boolean shouldSwap = shouldSwapWithDirection(i, j, algotype, direction);
                    probe.recordCompareAndSwap(); // StatusProbe: comparison made
                    if (shouldSwap) {
                        if (swapEngine.attemptSwap(cells, i, j)) {
                            // Synchronize metadata
                            CellMetadata temp = metadata[i];
                            metadata[i] = metadata[j];
                            metadata[j] = temp;
                        }
                    }
                }
            } else {
                // Other algotypes: iterate all neighbors as before
                List<Integer> neighbors = getNeighborsForAlgotype(i, algotype);
                for (int j : neighbors) {
                    // CRITICAL FIX: Record comparison before checking shouldSwap
                    boolean shouldSwap = shouldSwapWithDirection(i, j, algotype, direction);
                    probe.recordCompareAndSwap(); // StatusProbe: comparison made
                    if (shouldSwap) {
                        if (swapEngine.attemptSwap(cells, i, j)) {
                            // Synchronize metadata
                            CellMetadata temp = metadata[i];
                            metadata[i] = metadata[j];
                            metadata[j] = temp;
                        }
                    }
                }
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
     * Helper: Check if cells 0 to i-1 are sorted in correct order (ascending or descending).
     * Matches Python cell_research behavior: frozen cells are skipped and
     * reset the comparison chain.
     *
     * <p>CRITICAL FIX: Now supports both ascending and descending sort directions.
     * For descending sorts, the sentinel value is MAX_VALUE and comparison is inverted.</p>
     *
     * Python reference (InsertionSortCell.py:74-76):
     * <pre>
     * if cells[i].status == FREEZE:
     *     prev = -1  # Reset comparison, skip frozen (ascending: MIN_VALUE)
     *     continue
     * </pre>
     *
     * @param i the position to check (checks cells 0 to i-1)
     * @param reverseDirection true for descending sort, false for ascending
     * @return true if cells 0 to i-1 are sorted in the specified direction
     */
    private boolean isLeftSorted(int i, boolean reverseDirection) {
        // Start with sentinel: MIN for ascending (any value >= MIN), MAX for descending (any value <= MAX)
        int prevValue = reverseDirection ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        
        for (int k = 0; k < i; k++) {
            // Skip frozen cells - reset comparison chain (matches Python)
            if (swapEngine.isFrozen(k)) {
                // Reset sentinel after frozen cell
                prevValue = reverseDirection ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                continue;
            }

            // Get cell value for comparison
            int currentValue = cells[k].getValue();
            
            // Check if out of order based on direction
            boolean outOfOrder = reverseDirection 
                ? (currentValue > prevValue)  // Descending: next should be <= prev
                : (currentValue < prevValue); // Ascending: next should be >= prev
            
            if (outOfOrder) {
                return false; // Out of order
            }
            prevValue = currentValue;
        }
        return true;
    }

    /**
     * Helper: Get neighbors for the given position based on algotype
     */
    private List<Integer> getNeighborsForAlgotype(int i, Algotype algotype) {
        switch (algotype) {
            case BUBBLE:
                return bubbleTopology.getNeighbors(i, cells.length, algotype);
            case INSERTION:
                return insertionTopology.getNeighbors(i, cells.length, algotype);
            case SELECTION:
                // Get dynamic ideal target from cell state
                int idealPos = getIdealPosition(i);
                int target = Math.min(idealPos, cells.length - 1);
                return Arrays.asList(target);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Helper: Get ideal position from a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private int getIdealPosition(int index) {
        return metadata[index].idealPos().get();
    }

    /**
     * Helper: Increment ideal position for a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private void incrementIdealPosition(int index) {
        metadata[index].idealPos().incrementAndGet();
    }

    /**
     * Helper: Determine if swap should occur using direction-aware comparison.
     * 
     * <p>PURPOSE: Enables cross-purpose sorting by respecting each cell's individual
     * sort direction preference during swap evaluation.</p>
     * 
     * <p>INPUTS:
     * <ul>
     *   <li>i (int) - index of cell initiating swap</li>
     *   <li>j (int) - index of target neighbor cell</li>
     *   <li>algotype (Algotype) - algorithm policy of initiating cell</li>
     *   <li>direction (SortDirection) - sort direction of initiating cell</li>
     * </ul>
     * </p>
     * 
     * <p>PROCESS:
     * <ol>
     *   <li>Determine relative position (j < i means left neighbor, j > i means right)</li>
     *   <li>Get comparison result: cells[i].compareTo(cells[j])</li>
     *   <li>Apply algotype-specific swap rules</li>
     *   <li>Adjust comparison polarity based on direction:
     *       <ul>
     *         <li>ASCENDING: move left if smaller, right if larger</li>
     *         <li>DESCENDING: move left if larger, right if smaller</li>
     *       </ul>
     *   </li>
     *   <li>Return true if swap should proceed, false otherwise</li>
     * </ol>
     * </p>
     * 
     * <p>OUTPUTS: boolean - true if swap satisfies algotype and direction rules</p>
     * 
     * <p>DEPENDENCIES:
     * <ul>
     *   <li>Cell.compareTo() for value comparison</li>
     *   <li>isLeftSorted() for INSERTION algotype</li>
     *   <li>incrementIdealPosition() for SELECTION algotype</li>
     * </ul>
     * </p>
     * 
     * <p>ARCHITECTURE NOTE: This method is the heart of cross-purpose sorting.
     * It replaces hardcoded ascending logic with direction-aware decisions that
     * allow cells with different goals to compete and reach equilibrium.</p>
     * 
     * <p>GROUND TRUTH REFERENCE: cell_research Python checks reverse_direction
     * throughout swap logic in BubbleSortCell.py, SelectionSortCell.py, InsertionSortCell.py</p>
     * 
     * @param i index of initiating cell
     * @param j index of target neighbor
     * @param algotype algorithm policy
     * @param direction sort direction preference
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
                    int currentIdealPos = getIdealPosition(i);
                    if (currentIdealPos < cells.length - 1) {
                        incrementIdealPosition(i);
                    }
                    return false;
                }
                
            default:
                return false;
        }
    }



    /**
     * IMPLEMENTED: Run execution until convergence or max steps
     * @return total number of steps executed
     */
    public int runUntilConvergence(int maxSteps) {
        while (!converged && currentStep < maxSteps) {
            step();
        }
        return currentStep;
    }
    
    /**
     * IMPLEMENTED: Get the current cell array
     * Returns reference, not copy, for performance
     */
    public T[] getCells() {
        return cells;
    }
    
    /**
     * IMPLEMENTED: Get the current step number
     */
    public int getCurrentStep() {
        return currentStep;
    }
    
    /**
     * IMPLEMENTED: Check if execution has converged
     */
    public boolean hasConverged() {
        return converged;
    }
    
    /**
     * IMPLEMENTED: Get the probe for trajectory analysis
     */
    public Probe<T> getProbe() {
        return probe;
    }
    
    /**
     * IMPLEMENTED: Reset execution state to initial conditions
     */
    public void reset() {
        currentStep = 0;
        converged = false;
        probe.clear();
        swapEngine.resetSwapCount();
        bubbleTopology.reset();
        insertionTopology.reset();
        selectionTopology.reset();
        convergenceDetector.reset();

        // Reset SELECTION cell ideal positions to boundary (matches Python cell_research)
        resetSelectionCellIdealPositions(false); // ascending sort by default

        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Reset execution with explicit sort direction for SELECTION cells.
     *
     * @param reverseDirection true for descending sort, false for ascending
     */
    public void reset(boolean reverseDirection) {
        currentStep = 0;
        converged = false;
        this.reverseDirection = reverseDirection;  // CRITICAL FIX: Store for isLeftSorted
        probe.clear();
        swapEngine.resetSwapCount();
        bubbleTopology.reset();
        insertionTopology.reset();
        selectionTopology.reset();
        convergenceDetector.reset();

        // Reset SELECTION cell ideal positions to boundary
        resetSelectionCellIdealPositions(reverseDirection);

        probe.recordSnapshot(0, cells, 0);
    }

    /**
     * Reset ideal positions for SELECTION algotype cells.
     * Uses updateForBoundary matching Python cell_research SelectionSortCell.update() behavior.
     *
     * @param reverseDirection true for descending sort (ideal = right boundary),
     *                         false for ascending (ideal = left boundary)
     */
    private void resetSelectionCellIdealPositions(boolean reverseDirection) {
        for (int i = 0; i < cells.length; i++) {
            if (metadata[i].algotype() == Algotype.SELECTION) {
                int left = metadata[i].leftBoundary();
                int right = metadata[i].rightBoundary();
                int initialPos = reverseDirection ? right : left;

                // Update engine metadata (used for neighbor selection)
                metadata[i].idealPos().set(initialPos);

                // Also update the concrete cell state when it supports HasIdealPosition.
                // This keeps the Cell API minimal while ensuring external callers/tests
                // that look at the cell object observe the reset behavior.
                if (cells[i] instanceof HasIdealPosition) {
                    ((HasIdealPosition) cells[i]).setIdealPos(initialPos);
                }
            }
        }
    }
}
