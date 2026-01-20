package com.emergent.doom.sat;

/**
 * Algotype enumeration for Boolean satisfiability domain (SCAFFOLD).
 *
 * <p><strong>PURPOSE:</strong> Define assignment strategies enabling emergent
 * clustering based on strategy compatibility in constraint landscape.</p>
 *
 * <p><strong>PHASE ONE NOTES:</strong> Stub only; parameters and logic in Phase 3.</p>
 *
 * <p><strong>REFERENCE:</strong> TECH_SPEC.md §3.2.1 (Levin et al. 2024).</p>
 */
/**
 * Algotype enumeration for Boolean satisfiability domain (PHASE THREE ITER 1: Full enum with parameters).
 *
 * <p><strong>PURPOSE:</strong> Define assignment strategies enabling emergent
 * clustering based on strategy compatibility in constraint landscape.</p>
 *
 * <p><strong>PARAMETERS:</strong> Each strategy has configurable parameters
 * defined in {@link SATStrategyConfig}. Defaults are empirically justified.</p>
 *
 * <p><strong>REFERENCE:</strong> TECH_SPEC.md §3.2.1 (Levin et al. 2024).</p>
 */
public enum SATStrategy {
    
    /**
     * DPLL systematic search strategy.
     *
     * <p><strong>INTUITION:</strong> Unit propagation and pure literal elimination.
     * Systematic exploration with logical inference.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Extended Fibonacci-style viewing
     * (positions 1, 2, 3, 5, 8 away) for branching awareness.</p>
     *
     * <p><strong>SWAP THRESHOLD:</strong> Conservative, only moves for >threshold%
     * improvement (default 5%, configurable via {@link SATStrategyConfig#dpllSwapThreshold}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Clusters in constraint-dense regions
     * where unit propagation provides strong guidance.</p>
     */
    DPLL("DPLL systematic search with unit propagation", 5, true),
    
    /**
     * Greedy most-constrained-variable heuristic.
     *
     * <p><strong>INTUITION:</strong> Assign variable appearing in most unsatisfied
     * clauses. Greedy local optimization.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Immediate neighbors only (positions ±1).</p>
     *
     * <p><strong>SWAP THRESHOLD:</strong> Any improvement triggers swap (threshold 0%).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Clusters in regions with clear
     * variable dominance (skewed constraint distribution).</p>
     */
    GREEDY_MCV("Greedy most-constrained-variable heuristic", 0, false),
    
    /**
     * WalkSAT-inspired random walk with noise.
     *
     * <p><strong>INTUITION:</strong> Random variable flipping with bias toward
     * unsatisfied clauses. Escapes local optima through noise.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Immediate neighbors only.</p>
     *
     * <p><strong>NOISE PARAMETER:</strong> p=0.5 probability of random flip vs
     * greedy flip (configurable via {@link SATStrategyConfig#walksatNoise}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Baseline clustering due to random
     * component; should show ~50-55% aggregation.</p>
     */
    WALKSAT("WalkSAT random walk with noise p=0.5", 0, false),
    
    /**
     * Hybrid adaptive strategy switching.
     *
     * <p><strong>INTUITION:</strong> Switch between DPLL and WALKSAT based on
     * progress rate.</p>
     *
     * <p><strong>SWITCHING LOGIC:</strong></p>
     * <ul>
     *   <li>Start with DPLL behavior</li>
     *   <li>Switch to WALKSAT if no improvement for 5 consecutive steps</li>
     *   <li>Revert to DPLL when improvement resumes</li>
     * </ul>
     *
     * <p><strong>STAGNATION THRESHOLD:</strong> 5 steps (configurable via
     * {@link SATStrategyConfig#hybridStagnationThreshold}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Dynamic clustering patterns
     * as strategy switches align with landscape structure.</p>
     */
    HYBRID("Hybrid adaptive DPLL/WalkSAT, stagnation=5", 0, true);

    private final String description;
    private final int defaultSwapThreshold;
    private final boolean usesExtendedNeighborhood;
    
    SATStrategy(String description, int defaultSwapThreshold, boolean usesExtendedNeighborhood) {
        this.description = description;
        this.defaultSwapThreshold = defaultSwapThreshold;
        this.usesExtendedNeighborhood = usesExtendedNeighborhood;
    }
    
    public String getDescription() { 
        return description; 
    }
    
    public int getDefaultSwapThreshold() { 
        return defaultSwapThreshold; 
    }
    
    public boolean usesExtendedNeighborhood() { 
        return usesExtendedNeighborhood; 
    }
    
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}