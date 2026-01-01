package com.emergent.doom.cell;

/**
 * Enum for Levin paper's cell-view sorting algotypes.
 * Each represents a distinct behavioral policy (views, swaps, decisions).
 * 
 * <p>Note: This implementation provides the three algotypes studied in the
 * Levin et al. (2024) research. Merge Sort is intentionally not included
 * as it was not part of the original research framework, which focused on
 * comparison-based sorting algorithms with distinct local interaction patterns:
 * bubble (bidirectional local), insertion (unidirectional prefix), and 
 * selection (global target seeking).</p>
 */
public enum Algotype {
    BUBBLE("Local adjacent bidirectional value-based sorting"),
    INSERTION("Prefix left view with conservative left-only swaps"),
    SELECTION("Ideal target position chasing with incremental convergence");

    private final String description;

    Algotype(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name() + ": " + description;
    }
}