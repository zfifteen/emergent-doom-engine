package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.cell.SortDirection;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.FibonacciTopology;
import com.emergent.doom.topology.InsertionTopology;
import com.emergent.doom.topology.SelectionTopology;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

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
    private final FibonacciTopology<T> fibonacciTopology;
    private final SwapEngine<T> swapEngine;
    private final Probe<T> probe;
    private final ConvergenceDetector<T> convergenceDetector;
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
        this.fibonacciTopology = new FibonacciTopology<>();
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.random = random;
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
     * <p>This method processes all cells in the array during each iteration.
     * The iteration order includes every cell index from 0 to n-1, and each cell 
     * evaluates swap decisions with its topology-defined neighbors.</p>
     *
     * <p><strong>Convergence:</strong></p>
     * <p>Convergence depends on the problem structure (e.g., remainder landscape
     * for factorization), not the search space size. Once cells reach local
     * equilibrium where no beneficial swaps exist, the system converges. This is 
     * a key property of emergent optimization - the solution quality depends on 
     * the fitness landscape, not exhaustive enumeration.</p>
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
                    // CRITICAL FIX: Record comparison before checking shouldSwap
                    // Python tracks ALL comparisons, not just those leading to swaps
                    boolean shouldSwap = shouldSwapWithDirection(i, j, algotype, direction);
                    probe.recordCompareAndSwap(); // StatusProbe: comparison made
                    if (shouldSwap) {
                        swapEngine.attemptSwap(cells, i, j);
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
                        swapEngine.attemptSwap(cells, i, j);
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
            int currentValue = getCellValue(cells[k]);
            
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

    // ========== Helper Methods for Cell Access ==========

    /**
     * Get algotype from cell (legacy mode - infers from cell type).
     *
     * <p>PURPOSE: Support legacy cell introspection during Phase 4 migration.
     * ExecutionEngine doesn't support metadata providers, so this infers
     * algotype from the cell's concrete class type.</p>
     *
     * <p>INPUTS: cellIndex - position of cell to query</p>
     *
     * <p>PROCESS: Check cell type and return corresponding algotype</p>
     *
     * <p>OUTPUTS: Algotype for this cell position</p>
     *
     * <p>DEPENDENCIES: Cell must be BubbleCell, InsertionCell, SelectionCell, or GenericCell</p>
     */
    private Algotype getCellAlgotype(int cellIndex) {
        T cell = cells[cellIndex];
        
        // Infer algotype from cell type
        if (cell instanceof com.emergent.doom.cell.BubbleCell) {
            return Algotype.BUBBLE;
        } else if (cell instanceof com.emergent.doom.cell.InsertionCell) {
            return Algotype.INSERTION;
        } else if (cell instanceof com.emergent.doom.cell.SelectionCell) {
            return Algotype.SELECTION;
        } else if (cell instanceof com.emergent.doom.cell.RemainderCell) {
            // RemainderCell has getAlgotype() method
            return ((com.emergent.doom.cell.RemainderCell) cell).getAlgotype();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            // GenericCell doesn't store algotype - default to BUBBLE
            // In practice, GenericCell should be used with metadata providers, not ExecutionEngine
            return Algotype.BUBBLE;
        }

        throw new IllegalStateException(
            "Cell at index " + cellIndex + " is of unknown type: " + cell.getClass().getName() + ". " +
            "ExecutionEngine does not support metadata providers.");
    }

    /**
     * Helper: Extract comparable value from cell for isLeftSorted comparison.
     * 
     * <p>P1 FIX: All cell types now properly handled via getValue().
     * Previously fell back to hashCode() for InsertionCell/BubbleCell,
     * which broke insertion-mode runs using those types.</p>
     * 
     * <p>COPILOT REVIEW FIX: Throws UnsupportedOperationException instead of
     * using hashCode() fallback, since hashCode() is unreliable for sorting
     * comparisons (hash codes don't maintain ordering relationships).</p>
     */
    private int getCellValue(T cell) {
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
            "All Cell implementations must extend SelectionCell, GenericCell, InsertionCell, or BubbleCell."
        );
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
                int idealPos = getIdealPosition(cells[i]);
                int target = Math.min(idealPos, cells.length - 1);
                return Arrays.asList(target);
            case FIBONACCI:
                return fibonacciTopology.getNeighbors(i, cells.length, algotype);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Helper: Get ideal position from metadata (SELECTION algotype only).
     * 
     * <p>Note: ExecutionEngine doesn't support metadata providers yet.
     * This method cannot be implemented without metadata array support.</p>
     * 
     * @deprecated ExecutionEngine needs metadata provider support to track ideal positions
     */
    @Deprecated
    private int getIdealPosition(T cell) {
        throw new UnsupportedOperationException(
            "ExecutionEngine does not support ideal position tracking. " +
            "Use ParallelExecutionEngine, SynchronousExecutionEngine, or LockBasedExecutionEngine " +
            "with metadata providers instead.");
    }

    /**
     * Helper: Increment ideal position for SELECTION cells.
     * 
     * <p>Note: ExecutionEngine doesn't support metadata providers yet.
     * This method cannot be implemented without metadata array support.</p>
     * 
     * @deprecated ExecutionEngine needs metadata provider support to track ideal positions
     */
    @Deprecated
    private void incrementIdealPosition(T cell) {
        throw new UnsupportedOperationException(
            "ExecutionEngine does not support ideal position tracking. " +
            "Use ParallelExecutionEngine, SynchronousExecutionEngine, or LockBasedExecutionEngine " +
            "with metadata providers instead.");
    }

    /**
     * Helper: Determine if swap should occur based on Levin algotype rules
     */
    private boolean shouldSwapForAlgotype(int i, int j, Algotype algotype) {
        switch (algotype) {
            case BUBBLE:
                // Move left if value < left neighbor, right if value > right neighbor
                if (j == i - 1 && cells[i].compareTo(cells[j]) < 0) { // left neighbor, smaller value
                    return true;
                } else if (j == i + 1 && cells[i].compareTo(cells[j]) > 0) { // right neighbor, bigger value
                    return true;
                }
                return false;
            case INSERTION:
                // Move left only if left side sorted AND value < left neighbor
                if (j == i - 1 && isLeftSorted(i, reverseDirection) && cells[i].compareTo(cells[j]) < 0) {
                    return true;
                }
                // Note: neighbors include all left, but only swap with immediate left if conditions met
                return false;
            case SELECTION:
                // Guard: Skip if targeting self (prevents drift of correctly placed cells)
                if (i == j) {
                    return false;
                }

                // Swap with target if value < target value
                if (cells[i].compareTo(cells[j]) < 0) { // smaller than target
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
     * Helper: Get the sort direction (deprecated - ExecutionEngine doesn't support metadata).
     * 
     * <p>PURPOSE: This method is deprecated. ExecutionEngine is a legacy class that
     * doesn't support metadata providers. Use ParallelExecutionEngine or
     * SynchronousExecutionEngine for metadata-aware sorting.</p>
     * 
     * <p>INPUTS: cell (T) - the cell (ignored)</p>
     * 
     * <p>PROCESS: Returns ASCENDING by default (metadata not supported)</p>
     * 
     * <p>OUTPUTS: SortDirection.ASCENDING (always)</p>
     * 
     * <p>DEPENDENCIES: SortDirection enum</p>
     * 
     * <p>ARCHITECTURE NOTE: For direction-aware sorting, use modern execution engines
     * that accept metadata providers (ParallelExecutionEngine, SynchronousExecutionEngine).</p>
     * 
     * @param cell the cell (ignored)
     * @return SortDirection.ASCENDING (always)
     * @deprecated Use execution engines with metadata provider support
     */
    @Deprecated
    private SortDirection getCellDirection(T cell) {
        // ExecutionEngine is deprecated and doesn't support metadata providers
        // Default to ascending for all cells
        return SortDirection.ASCENDING;
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
        // ExecutionEngine is deprecated and doesn't support metadata providers
        // Selection cell ideal position tracking requires metadata providers
        // This method is a no-op for ExecutionEngine
        // Use ParallelExecutionEngine, SynchronousExecutionEngine, or LockBasedExecutionEngine instead
    }
}
