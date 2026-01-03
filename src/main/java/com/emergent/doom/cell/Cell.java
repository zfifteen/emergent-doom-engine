package com.emergent.doom.cell;

/**
 * Minimal contract for cells in the Emergent Doom Engine.
 * 
 * <p>This interface enforces a pure comparison-based design where cells
 * can only be compared to each other. All domain-specific logic is hidden
 * within the implementation, allowing the engine to remain domain-agnostic.</p>
 * 
 * <p><strong>Design Principle:</strong> The engine treats cells as opaque
 * entities that can only be ordered. This minimal contract enables emergence
 * through simple swap mechanics without any knowledge of the underlying domain.</p>
 * 
 * @param <T> the type of cell (must be comparable to itself)
 */
public interface Cell<T extends Cell<T>> extends Comparable<T> {
    /**
     * Returns the sortable value of the cell.
     * 
     * <p>This method is required for metrics and logging, but is not used
     * by the engine for swap decisions (which use compareTo).</p>
     * 
     * @return the integer value of the cell
     */
    int getValue();
}
