package com.emergent.doom.cell;

/**
 * Minimal cell contract for domain-agnostic sorting.
 * 
 * <p>Cells are pure data carriers implementing only the Comparable contract.
 * All sorting-specific metadata (algotype, sort direction, ideal position) 
 * is managed externally by execution engines via CellMetadata.</p>
 * 
 * <p>Implementations must provide:</p>
 * <ul>
 *   <li>compareTo() - domain-specific comparison logic</li>
 *   <li>getValue() - optional, for metrics/logging</li>
 * </ul>
 * 
 * <p>Implementations should NOT implement deprecated Has* interfaces 
 * (HasAlgotype, HasSortDirection, HasIdealPosition). These are maintained 
 * only for backward compatibility during migration.</p>
 * 
 * @param <T> the concrete cell type
 * @see com.emergent.doom.execution.CellMetadata
 */
public interface Cell<T extends Cell<T>> extends Comparable<T> {
    // PURPOSE: Compare this cell to another cell for ordering
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
    // ARCHITECTURE NOTE:
    //   - Comparison is the ONLY method required by the engine
    //   - All domain logic is encapsulated in the implementation
    //   - Execution metadata (algotype, sort direction, ideal position) is managed
    //     externally by execution engines via CellMetadata arrays
    //   - This achieves true domain-agnostic sorting where cells are pure value carriers
}
