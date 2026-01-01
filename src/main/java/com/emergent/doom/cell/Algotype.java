package com.emergent.doom.cell;

/**
 * Enum for Levin paper's cell-view sorting algotypes.
 * Each represents a distinct behavioral policy (views, swaps, decisions).
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