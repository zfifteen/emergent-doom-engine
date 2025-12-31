package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasIdealPosition;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.topology.BubbleTopology;
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
     * IMPLEMENTED: Execute a single step of the algorithm
     * @return number of swaps performed in this step
     */
    public int step() {
        // Get iteration order (use bubble topology as default, all are sequential)
        List<Integer> iterationOrder = bubbleTopology.getIterationOrder(cells.length);

        // Reset swap counter for this step
        swapEngine.resetSwapCount();

        // For each cell in iteration order, try swapping with neighbors based on algotype
        for (int i : iterationOrder) {
            Algotype algotype = cells[i].getAlgotype();

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
                    boolean shouldSwap = shouldSwapForAlgotype(i, j, algotype);
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
                    boolean shouldSwap = shouldSwapForAlgotype(i, j, algotype);
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

    /**
     * Helper: Extract comparable value from cell for isLeftSorted comparison.
     */
    private int getCellValue(T cell) {
        if (cell instanceof com.emergent.doom.cell.SelectionCell) {
            return ((com.emergent.doom.cell.SelectionCell<?>) cell).getValue();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getValue();
        }
        // Fallback: use hashCode as proxy (not ideal but allows compilation)
        return cell.hashCode();
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
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Helper: Get ideal position from a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private int getIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            return ((SelectionCell<?>) cell).getIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            return ((com.emergent.doom.cell.GenericCell) cell).getIdealPos();
        }
        return 0;  // Default for other cell types
    }

    /**
     * Helper: Increment ideal position for a SELECTION cell (supports both SelectionCell and GenericCell)
     */
    private void incrementIdealPosition(T cell) {
        if (cell instanceof SelectionCell) {
            ((SelectionCell<?>) cell).incrementIdealPos();
        } else if (cell instanceof com.emergent.doom.cell.GenericCell) {
            ((com.emergent.doom.cell.GenericCell) cell).incrementIdealPos();
        }
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
        int leftBoundary = 0;
        int rightBoundary = cells.length - 1;

        for (T cell : cells) {
            if (cell.getAlgotype() == Algotype.SELECTION) {
                if (cell instanceof HasIdealPosition) {
                    ((HasIdealPosition) cell).updateForBoundary(leftBoundary, rightBoundary, reverseDirection);
                }
            }
        }
    }
}
