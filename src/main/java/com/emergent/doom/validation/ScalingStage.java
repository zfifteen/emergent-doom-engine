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
     * <p><strong>Implementation:</strong> Returns the base-10 magnitude exponent
     * (e.g., 6 for 10^6). This determines the difficulty level of the semiprime.</p>
     * 
     * <p><strong>Reasoning:</strong> Each stage tests a specific magnitude to map
     * the transition from linear scaling (easy targets) to super-linear (hard targets).
     * The magnitude directly correlates with cryptographic hardness.</p>
     * 
     * @return The target magnitude exponent
     */
    public int getTargetMagnitude() {
        switch (this) {
            case STAGE_1_E6:  return 6;
            case STAGE_2_E9:  return 9;
            case STAGE_3_E12: return 12;
            case STAGE_4_E18: return 18;
            default: throw new IllegalStateException("Unknown stage: " + this);
        }
    }
    
    /**
     * Get the array sizes to test for this stage.
     * 
     * <p><strong>Implementation:</strong> Returns [10^4, 10^5, 10^6] for all stages.
     * Testing across 2 orders of magnitude allows measurement of B = ∂steps/∂array_size.</p>
     * 
     * <p><strong>Reasoning:</strong> Consistent array sizes across stages enable
     * direct comparison of B coefficients. If B stays ~0, array size doesn't matter
     * (linear scaling). If B grows, we've found the failure boundary.</p>
     * 
     * @return Array of array sizes to test
     */
    public int[] getArraySizes() {
        // All stages use the same array size progression per protocol
        return new int[]{10_000, 100_000, 1_000_000};
    }
    
    /**
     * Get the maximum steps allowed before declaring non-convergence.
     * 
     * <p><strong>Implementation:</strong> Returns stage-specific limits based on
     * expected convergence difficulty. Harder targets get more steps to avoid
     * false negatives (declaring non-convergence when it's just slow).</p>
     * 
     * <p><strong>Reasoning:</strong> From LINEAR_SCALING_ANALYSIS.md, easy targets
     * converge in ~135 steps. We allow 100x margin for stages 1-2, 1000x for stage 3,
     * and 10000x for stage 4 to account for potential slowdown.</p>
     * 
     * @return Maximum steps for convergence attempt
     */
    public int getMaxSteps() {
        switch (this) {
            case STAGE_1_E6:  return 10_000;
            case STAGE_2_E9:  return 10_000;
            case STAGE_3_E12: return 100_000;
            case STAGE_4_E18: return 1_000_000;
            default: throw new IllegalStateException("Unknown stage: " + this);
        }
    }
    
    /**
     * Get the base prime magnitude for semiprime generation.
     * 
     * <p><strong>Implementation:</strong> Returns the magnitude of prime factors.
     * For target N = p × q ≈ 10^m, we use p, q ≈ 10^(m/2).</p>
     * 
     * <p><strong>Reasoning:</strong> Balanced semiprimes (p ≈ q) are hardest to factor,
     * representing the worst case. We use (m/2) to ensure p × q reaches target magnitude.</p>
     * 
     * @return Base prime magnitude (exponent for 10^x)
     */
    public int getPrimeMagnitude() {
        // For semiprime N = p × q ≈ 10^m, use p, q ≈ 10^(m/2)
        switch (this) {
            case STAGE_1_E6:  return 3;   // 10^3 × 10^3 ≈ 10^6
            case STAGE_2_E9:  return 4;   // ~10^4.5 × 10^4.5 ≈ 10^9 (we'll use 10^4 for simplicity)
            case STAGE_3_E12: return 6;   // 10^6 × 10^6 = 10^12
            case STAGE_4_E18: return 9;   // 10^9 × 10^9 = 10^18
            default: throw new IllegalStateException("Unknown stage: " + this);
        }
    }
    
    /**
     * Get the gap size for selecting second prime factor.
     * 
     * <p><strong>Implementation:</strong> Returns the gap parameter used to
     * select q = nextprime(p + gap) for target = p × q.</p>
     * 
     * <p><strong>Reasoning:</strong> Small gaps ensure p and q are close in magnitude
     * (balanced semiprime). Larger gaps for harder stages introduce more randomness
     * while maintaining balance. Gap of 1000 for stage 4 per protocol specification.</p>
     * 
     * @return Gap size for prime selection
     */
    public int getPrimeGap() {
        switch (this) {
            case STAGE_1_E6:  return 10;
            case STAGE_2_E9:  return 100;
            case STAGE_3_E12: return 100;
            case STAGE_4_E18: return 1000;
            default: throw new IllegalStateException("Unknown stage: " + this);
        }
    }
}
