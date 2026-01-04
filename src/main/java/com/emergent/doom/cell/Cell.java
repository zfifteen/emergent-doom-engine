package com.emergent.doom.cell;

import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.group.CellGroup;
import com.emergent.doom.group.CellStatus;
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
 * <p><strong>REFACTORING NOTE (Issue #TBD):</strong> Cell interface has been stripped
 * of HasAlgotype dependency. Execution metadata (algotype, sort direction, ideal position)
 * is now managed by engines via CellMetadata and metadata provider pattern, achieving
 * true domain-agnostic cells as pure Comparable data carriers.</p>
 * 
 * @param <T> the type of cell (must be comparable to itself)
 */
public interface Cell<T extends Cell<T>> extends Comparable<T>, HasValue, HasGroup, HasStatus {
    // PURPOSE: Compare this cell to another cell
    // INPUTS: other (T) - the cell to compare against
    // PROCESS:
    //   1. Implement domain-specific comparison logic
    //   2. Return negative if this < other
    //   3. Return zero if this == other
    //   4. Return positive if this > other
    //   5. Must be consistent with equals() and hashCode()
    //   6. Must be transitive: if a < b and b < c, then a < c
    // OUTPUTS: int - negative, zero, or positive integer
    // DEPENDENCIES: None
    // NOTE: Comparison is the ONLY method required by the engine.
    //       All domain logic is encapsulated in the implementation.
    //       Execution metadata is managed separately via CellMetadata.
}
