package com.emergent.doom.factorization;

/**
 * Algotype enumeration for integer factorization domain.
 *
 * <p><strong>PURPOSE:</strong> Define behavioral strategies for factor-finding cells.
 * Each strategy represents a distinct approach to generating and evaluating factor
 * candidates, enabling emergent clustering based on strategy compatibility.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Domain-specific: Factorization strategies separate from sorting algotypes</li>
 *   <li>Type-safe: Prevents mixing incompatible strategies across domains</li>
 *   <li>Hypothesis-driven: Each strategy encodes a different factorization intuition</li>
 *   <li>Cluster-compatible: Strategies cluster when they find similar candidates</li>
 * </ul>
 *
 * <p><strong>EXPERIMENT MAPPING:</strong></p>
 * <table>
 *   <tr><th>EDE Concept</th><th>Factorization Mapping</th></tr>
 *   <tr><td>Cell</td><td>Factor candidate</td></tr>
 *   <tr><td>Cell.value</td><td>Candidate integer</td></tr>
 *   <tr><td>Cell.algotype</td><td>Factor-finding strategy</td></tr>
 *   <tr><td>"Sorted" state</td><td>Candidates ordered by factor-fitness score</td></tr>
 *   <tr><td>Aggregation</td><td>Clustering of strategies around similar candidates</td></tr>
 * </table>
 *
 * <p><strong>REFERENCE:</strong> See FIRST_NON_SORTING_EXPERIMENT.md for full
 * experimental design and hypothesis.</p>
 */
public enum FactorStrategy {
    
    /**
     * Small prime trial division strategy.
     *
     * <p><strong>INTUITION:</strong> Many semiprimes have small prime factors.
     * Generate candidates from the first N primes.</p>
     *
     * <p><strong>CANDIDATE GENERATION:</strong> First 'count' primes up to sqrt(N).</p>
     *
     * <p><strong>EXPECTED BEHAVIOR:</strong> For N=143 (11×13), will include both
     * true factors since 11 and 13 are small primes.</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Will cluster with other small primes,
     * potentially revealing prime-rich regions in candidate space.</p>
     */
    SMALL_PRIMES("Small prime trial division strategy"),
    
    /**
     * Fermat-method-inspired near-sqrt candidates.
     *
     * <p><strong>INTUITION:</strong> Fermat's factorization method searches for factors
     * near sqrt(N). For N = a*b close to each other, both a and b are near sqrt(N).</p>
     *
     * <p><strong>CANDIDATE GENERATION:</strong> Candidates clustered around sqrt(N),
     * clamped to range [2, sqrt(N)].</p>
     *
     * <p><strong>EXPECTED BEHAVIOR:</strong> For N=143, sqrt(143) ≈ 11.96, so candidates
     * will cluster around 11-12, including true factor 11.</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Will cluster near sqrt(N), potentially
     * separating from small prime strategy.</p>
     */
    FERMAT_NEAR_SQRT("Fermat-method-inspired near-sqrt candidates"),
    
    /**
     * Random sampling baseline strategy.
     *
     * <p><strong>INTUITION:</strong> Random exploration of candidate space as control.
     * No structure, pure probabilistic coverage.</p>
     *
     * <p><strong>CANDIDATE GENERATION:</strong> Uniform random integers in [2, sqrt(N)].</p>
     *
     * <p><strong>EXPECTED BEHAVIOR:</strong> For N=143, random integers in [2, 11].
     * May or may not include true factors by chance.</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Should show minimal clustering signal,
     * serving as baseline for measuring structure in other strategies.</p>
     */
    RANDOM_SAMPLE("Random sampling baseline strategy");
    
    // PURPOSE: Human-readable description of strategy behavior
    // INPUTS: Set during enum construction
    // OUTPUTS: Available via getDescription()
    private final String description;
    
    /**
     * Create a FactorStrategy with description.
     *
     * <p><strong>PURPOSE:</strong> Initialize enum constant with description.</p>
     *
     * @param description human-readable description of strategy behavior
     */
    FactorStrategy(String description) {
        this.description = description;
    }
    
    /**
     * Get the description of this strategy.
     *
     * <p><strong>PURPOSE:</strong> Provide human-readable explanation of strategy behavior
     * for logging, documentation, and analysis.</p>
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
     * Convert strategy to string with description.
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
