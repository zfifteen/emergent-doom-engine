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
                int target = Math.min(getIdealPosition(cells[i]), cells.length - 1);
                return Arrays.asList(target);
            default:
                throw new IllegalStateException("Unknown algotype: " + algotype);
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
                // TODO: Phase Two - Use HasIdealPosition for polymorphic access
                // Guard: Skip if targeting self
                if (i == j) {
                    return false;
                }

                // Swap if smaller than target
                if (cells[i].compareTo(cells[j]) < 0) {
                    return true;
                } else {
                    // Denied: Increment idealPos using interface
                    // if (!(cells[i] instanceof HasIdealPosition)) throw UnsupportedOperationException(...);
                    // HasIdealPosition sel = (HasIdealPosition) cells[i];
                    // if (getIdealPosition(cells[i]) < cells.length - 1) incrementIdealPosition(cells[i]);
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
    // Helper methods for HasIdealPosition (scaffold; implement in phase two)
    private int getIdealPosition(T cell) {
        if (!(cell instanceof HasIdealPosition)) {
            throw new UnsupportedOperationException(
                "Cell type " + cell.getClass().getSimpleName() + 
                " does not support idealPos for SELECTION algotype. Ensure cell implements HasIdealPosition."
            );
        }
        HasIdealPosition sel = (HasIdealPosition) cell;
        return sel.getIdealPos();
    }

    private void incrementIdealPosition(T cell) {
        if (!(cell instanceof HasIdealPosition)) {
            throw new UnsupportedOperationException(
                "Cell type " + cell.getClass().getSimpleName() + 
                " does not support idealPos for SELECTION algotype. Ensure cell implements HasIdealPosition."
            );
        }
        HasIdealPosition sel = (HasIdealPosition) cell;
        sel.incrementIdealPos();
    }

    private void setIdealPosition(T cell, int pos) {
        if (!(cell instanceof HasIdealPosition)) {
            throw new UnsupportedOperationException(
                "Cell type " + cell.getClass().getSimpleName() + 
                " does not support idealPos for SELECTION algotype. Ensure cell implements HasIdealPosition."
            );
        }
        HasIdealPosition sel = (HasIdealPosition) cell;
        sel.setIdealPos(pos);
    }

    public void reset() {
        currentStep = 0;
        converged = false;
        probe.clear();
        swapEngine.resetSwapCount();
        bubbleTopology.reset();
        insertionTopology.reset();
        selectionTopology.reset();
        convergenceDetector.reset();
        // Reset idealPos for SELECTION cells only
        for (T cell : cells) {
            if (cell.getAlgotype() == Algotype.SELECTION) {
                setIdealPosition(cell, 0);
            }
        }
        probe.recordSnapshot(0, cells, 0);
    }
}
