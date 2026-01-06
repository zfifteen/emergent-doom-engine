package com.emergent.doom.cell;

/**
 * Algotype enumeration for sorting domain.
 *
 * <p><strong>PURPOSE:</strong> Define behavioral policies for sorting cells based on
 * Levin et al. (2024) research framework. Each algotype represents distinct local
 * interaction patterns (views, swaps, decisions).</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Domain-specific: Separates sorting algotypes from future domains (factorization, etc.)</li>
 *   <li>Type-safe: Prevents mixing incompatible algotypes across domains</li>
 *   <li>Levin-aligned: Matches the three core algotypes from research paper</li>
 *   <li>Extensible: Can add novel algotypes (e.g., FIBONACCI) within sorting domain</li>
 * </ul>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> This enum is the "A" type parameter in
 * AbstractCell&lt;V, A&gt; for sorting domain. Future domains will define their own
 * algotype enums (e.g., FactorizationAlgotype).</p>
 *
 * <p><strong>GROUND TRUTH REFERENCE:</strong> Levin et al. (2024) studies BUBBLE,
 * INSERTION, and SELECTION as core comparison-based sorting algotypes with distinct
 * local interaction patterns.</p>
 */
public enum SortingAlgotype {
    
    /**
     * Local adjacent bidirectional value-based sorting.
     *
     * <p><strong>VISIBILITY:</strong> Sees only left and right adjacent neighbors.</p>
     *
     * <p><strong>MOVEMENT:</strong> Randomly picks one direction (50/50 left/right),
     * swaps if value ordering is wrong for that direction.</p>
     *
     * <p><strong>STRATEGY:</strong> Opportunistic - makes greedy local swaps based on
     * immediate neighbors only.</p>
     *
     * <p><strong>LEVIN REFERENCE:</strong> "Bubble sort cells compare with immediate
     * neighbors and swap if out of order" (Levin et al., 2024, p. 4).</p>
     */
    BUBBLE("Local adjacent bidirectional value-based sorting"),
    
    /**
     * Prefix left view with conservative left-only swaps.
     *
     * <p><strong>VISIBILITY:</strong> Sees all cells to the left (prefix view).</p>
     *
     * <p><strong>MOVEMENT:</strong> Only moves left, and only if left side is already sorted.
     * Conservative insertion into sorted region.</p>
     *
     * <p><strong>STRATEGY:</strong> Defensive - waits for left side to sort before inserting,
     * preventing disruption of sorted prefix.</p>
     *
     * <p><strong>LEVIN REFERENCE:</strong> "Insertion sort cells check if the left segment
     * is sorted before attempting to insert" (Levin et al., 2024, p. 5).</p>
     */
    INSERTION("Prefix left view with conservative left-only swaps"),
    
    /**
     * Ideal target position chasing with incremental convergence.
     *
     * <p><strong>VISIBILITY:</strong> Sees target position (initially leftmost/rightmost
     * depending on sort direction).</p>
     *
     * <p><strong>MOVEMENT:</strong> Tries to swap with cell at ideal position. If denied,
     * increments ideal position rightward (ascending) or leftward (descending).</p>
     *
     * <p><strong>STRATEGY:</strong> Targeted - converges toward final position through
     * iterative adjustments.</p>
     *
     * <p><strong>LEVIN REFERENCE:</strong> "Selection sort cells target specific positions
     * and adjust when blocked" (Levin et al., 2024, p. 6).</p>
     */
    SELECTION("Ideal target position chasing with incremental convergence"),
    
    /**
     * Fibonacci-distance viewing with logarithmic neighbor coverage.
     *
     * <p><strong>VISIBILITY:</strong> Sees neighbors at Fibonacci distances (1, 2, 3, 5, 8, ...).
     * Logarithmic coverage provides long-range awareness while maintaining local focus.</p>
     *
     * <p><strong>MOVEMENT:</strong> Checks Fibonacci distances for beneficial swaps.
     * Prioritizes left larger values, then right smaller values.</p>
     *
     * <p><strong>STRATEGY:</strong> Exploratory - balances local and distant awareness
     * for efficient long-range movement.</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Novel algotype extending Levin framework
     * with logarithmic viewing pattern. Not in original research but follows same
     * cell-based interaction model.</p>
     */
    FIBONACCI("Fibonacci-distance viewing with logarithmic neighbor coverage");
    
    // PURPOSE: Human-readable description of algotype behavior
    // INPUTS: Set during enum construction
    // OUTPUTS: Available via getDescription()
    private final String description;
    
    /**
     * Create a SortingAlgotype with description.
     *
     * <p><strong>PURPOSE:</strong> Initialize enum constant with description.</p>
     *
     * @param description human-readable description of algotype behavior
     */
    SortingAlgotype(String description) {
        this.description = description;
    }
    
    /**
     * Get the description of this algotype.
     *
     * <p><strong>PURPOSE:</strong> Provide human-readable explanation of algotype behavior
     * for logging, documentation, and debugging.</p>
     *
     * <p><strong>INPUTS:</strong> None (getter method)</p>
     *
     * <p><strong>OUTPUTS:</strong> Description string (never null)</p>
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Convert algotype to string with description.
     *
     * <p><strong>PURPOSE:</strong> Provide formatted string for logging and display.</p>
     *
     * <p><strong>INPUTS:</strong> None (toString override)</p>
     *
     * <p><strong>OUTPUTS:</strong> Formatted string "NAME: description"</p>
     *
     * @return formatted string representation
     */
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
