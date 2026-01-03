package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.SortDirection;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Metadata associated with a cell, managed by the execution engine.
 *
 * <p>This class centralizes all sorting-specific state and behavioral metadata
 * that was previously stored within the cell objects themselves. Moving this
 * state to the engine enables true domain-generality for the Cell interface.</p>
 */
public final class CellMetadata {
    private final Algotype algotype;
    private final SortDirection direction;
    private final AtomicInteger idealPos;
    private final int leftBoundary;
    private final int rightBoundary;

    public CellMetadata(
        Algotype algotype,
        SortDirection direction,
        AtomicInteger idealPos,
        int leftBoundary,
        int rightBoundary
    ) {
        this.algotype = algotype;
        this.direction = direction;
        this.idealPos = idealPos;
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
    }

    public Algotype algotype() {
        return algotype;
    }

    public SortDirection direction() {
        return direction;
    }

    public AtomicInteger idealPos() {
        return idealPos;
    }

    public int leftBoundary() {
        return leftBoundary;
    }

    public int rightBoundary() {
        return rightBoundary;
    }

    /**
     * Create a deep copy of the metadata.
     *
     * @return a new CellMetadata instance with the same values
     */
    public CellMetadata copy() {
        return new CellMetadata(
            algotype,
            direction,
            new AtomicInteger(idealPos.get()),
            leftBoundary,
            rightBoundary
        );
    }
}
