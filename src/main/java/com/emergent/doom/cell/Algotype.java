package com.emergent.doom.cell;

/**
 * Enum for cell-view sorting algotypes.
 * Each represents a distinct behavioral policy (views, swaps, decisions).
 * 
 * <p>Note: This implementation includes the three algotypes studied in the
 * Levin et al. (2024) research (BUBBLE, INSERTION, SELECTION), plus the
 * novel FIBONACCI algotype which uses logarithmic neighbor coverage via
 * Fibonacci-distance viewing. Merge Sort is intentionally not included
 * as it was not part of the original research framework, which focused on
 * comparison-based sorting algorithms with distinct local interaction patterns.</p>
 */
public enum Algotype {
    BUBBLE("Local adjacent bidirectional value-based sorting"),
    INSERTION("Prefix left view with conservative left-only swaps"),
    SELECTION("Ideal target position chasing with incremental convergence"),
    FIBONACCI("Fibonacci-distance viewing with logarithmic neighbor coverage");

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