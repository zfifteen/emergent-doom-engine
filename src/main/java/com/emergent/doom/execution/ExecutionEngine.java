package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.topology.BubbleTopology;
import com.emergent.doom.topology.InsertionTopology;
import com.emergent.doom.topology.SelectionTopology;

import java.util.Arrays;

import java.util.List;

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
    
    private int currentStep;
    private boolean converged;
    
    /**
     * IMPLEMENTED: Initialize the execution engine with algotype-based topology dispatch
     */
    public ExecutionEngine(
            T[] cells,
            SwapEngine<T> swapEngine,
            Probe<T> probe,
            ConvergenceDetector<T> convergenceDetector) {
        this.cells = cells;
        this.bubbleTopology = new BubbleTopology<>();
        this.insertionTopology = new InsertionTopology<>();
        this.selectionTopology = new SelectionTopology<>();
        this.swapEngine = swapEngine;
        this.probe = probe;
        this.convergenceDetector = convergenceDetector;
        this.currentStep = 0;
        this.converged = false;

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
            List<Integer> neighbors = getNeighborsForAlgotype(i, algotype);
            for (int j : neighbors) {
                if (shouldSwapForAlgotype(i, j, algotype)) {
                    swapEngine.attemptSwap(cells, i, j);
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
     * Helper: Check if cells 0 to i-1 are sorted in ascending order
     */
    private boolean isLeftSorted(int i) {
        for (int k = 0; k < i - 1; k++) {
            if (cells[k].compareTo(cells[k + 1]) > 0) {
                return false;
            }
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
                if (j == i - 1 && isLeftSorted(i) && cells[i].compareTo(cells[j]) < 0) {
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
        probe.recordSnapshot(0, cells, 0);
    }
}
