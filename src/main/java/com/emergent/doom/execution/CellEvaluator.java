package com.emergent.doom.execution;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.swap.SwapProposal;

import java.util.Optional;

/**
 * Functional interface for cell swap evaluation.
 *
 * <p>Evaluates whether a cell at a given position wants to initiate a swap
 * and returns the proposal if so. Used by parallel execution to decouple
 * evaluation from swap execution.</p>
 *
 * <p><strong>Thread Safety:</strong> Implementations must be thread-safe
 * as they may be invoked concurrently from multiple cell threads.</p>
 *
 * @param <T> the type of cell
 */
@FunctionalInterface
public interface CellEvaluator<T extends Cell<T>> {

    /**
     * Evaluate a cell and determine if it wants to swap with a neighbor.
     *
     * <p>This method is called during the parallel evaluation phase.
     * It should only read from the cell array, not modify it.</p>
     *
     * @param cellIndex the index of the cell to evaluate
     * @param cells the current cell array (read-only access)
     * @return a swap proposal if the cell wants to swap, empty otherwise
     */
    Optional<SwapProposal> evaluate(int cellIndex, T[] cells);
}
