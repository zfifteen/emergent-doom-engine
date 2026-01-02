package com.emergent.doom.validation;

/**
 * Defines progressive difficulty stages for linear scaling validation experiments.
 * 
 * <p><strong>Purpose:</strong> This enum represents the experimental ladder as defined in
 * the research protocol. Each stage tests progressively harder semiprimes to identify
 * the failure boundary where linear scaling (B ≈ 0) transitions to super-linear growth (B > 0.5).</p>
 * 
 * <p><strong>Architecture Role:</strong> Provides type-safe configuration for experiment stages,
 * ensuring systematic testing from easy (N=10^6) to cryptographically hard (N=10^18) targets.</p>
 * 
 * <p><strong>Data Flow:</strong>
 * <ul>
 *   <li>Input: Stage selection from command-line or programmatic configuration</li>
 *   <li>Output: Target magnitude, array size ranges, max step limits for that stage</li>
 * </ul>
 * </p>
 */
public enum ScalingStage {
    /**
     * Stage 1: N = 10^6 magnitude semiprimes.
     * Target: nextprime(10^3) × nextprime(10^3 + small_gap)
     * Array sizes: [10^4, 10^5, 10^6]
     * Max steps: 10,000
     */
    STAGE_1_E6,
    
    /**
     * Stage 2: N = 10^9 magnitude semiprimes.
     * Target: nextprime(10^4.5) × nextprime(10^4.5 + small_gap)
     * Array sizes: [10^4, 10^5, 10^6]
     * Max steps: 10,000
     */
    STAGE_2_E9,
    
    /**
     * Stage 3: N = 10^12 magnitude semiprimes.
     * Target: nextprime(10^6) × nextprime(10^6 + small_gap)
     * Array sizes: [10^4, 10^5, 10^6]
     * Max steps: 100,000
     */
    STAGE_3_E12,
    
    /**
     * Stage 4: N = 10^18 magnitude semiprimes (only if Stage 3 succeeds).
     * Target: nextprime(10^9) × nextprime(10^9 + 1000)
     * Array sizes: [10^4, 10^5, 10^6]
     * Max steps: 1,000,000
     */
    STAGE_4_E18;
    
    /**
     * Get the target magnitude for this stage.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the base-10 magnitude (e.g., 6 for 10^6).</p>
     * 
     * @return The target magnitude exponent
     */
    public int getTargetMagnitude() {
        // TODO: Phase 3 - implement magnitude lookup
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the array sizes to test for this stage.
     * 
     * <p><strong>Not yet implemented.</strong> Will return [10^4, 10^5, 10^6] for all current stages.</p>
     * 
     * @return Array of array sizes to test
     */
    public int[] getArraySizes() {
        // TODO: Phase 3 - implement array size configuration
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the maximum steps allowed before declaring non-convergence.
     * 
     * <p><strong>Not yet implemented.</strong> Will return stage-specific limits
     * (10k for stages 1-2, 100k for stage 3, 1M for stage 4).</p>
     * 
     * @return Maximum steps for convergence attempt
     */
    public int getMaxSteps() {
        // TODO: Phase 3 - implement max steps lookup
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the base prime magnitude for semiprime generation.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the magnitude of the prime factors
     * (e.g., 3 for Stage 1 means primes near 10^3).</p>
     * 
     * @return Base prime magnitude
     */
    public int getPrimeMagnitude() {
        // TODO: Phase 3 - implement prime magnitude lookup
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    /**
     * Get the gap size for selecting second prime factor.
     * 
     * <p><strong>Not yet implemented.</strong> Will return the gap parameter used to
     * select q = nextprime(p + gap) for target = p × q.</p>
     * 
     * @return Gap size for prime selection
     */
    public int getPrimeGap() {
        // TODO: Phase 3 - implement gap configuration
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
