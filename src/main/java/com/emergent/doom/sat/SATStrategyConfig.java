package com.emergent.doom.sat;

/**
 * Configuration parameters for SAT strategy behaviors (SCAFFOLD).
 *
 * &lt;p&gt;&lt;strong&gt;PURPOSE:&lt;/strong&gt; Encapsulate tunable parameters for sensitivity analysis.&lt;/p&gt;
 *
 * &lt;p&gt;&lt;strong&gt;PHASE ONE NOTES:&lt;/strong&gt; Stub defaults; full builder in Phase 2.&lt;/p&gt;
 */
/**
 * Configuration parameters for SAT strategy behaviors (PHASE THREE ITER 1: Full class with builder).
 *
 * <p><strong>PURPOSE:</strong> Encapsulate tunable parameters to enable
 * sensitivity analysis and reproducibility.</p>
 *
 * <p><strong>DEFAULTS (empirically justified):</strong></p>
 * <ul>
 *   <li>DPLL swap threshold: 5% (from sorting conservative strategy patterns)</li>
 *   <li>WALKSAT noise: 0.5 (standard WalkSAT balance)</li>
 *   <li>HYBRID stagnation: 5 steps (balance responsiveness vs stability)</li>
 * </ul>
 */
public class SATStrategyConfig {
    private final int dpllSwapThreshold;
    private final double walksatNoise;
    private final int hybridStagnationThreshold;
    private final long randomSeed;

    public SATStrategyConfig(int dpllSwapThreshold, double walksatNoise, int hybridStagnationThreshold, long randomSeed) {
        this.dpllSwapThreshold = dpllSwapThreshold;
        this.walksatNoise = walksatNoise;
        this.hybridStagnationThreshold = hybridStagnationThreshold;
        this.randomSeed = randomSeed;
    }

    public static SATStrategyConfig defaults() {
        return new SATStrategyConfig(5, 0.5, 5, 42L);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getDpllSwapThreshold() { return dpllSwapThreshold; }
    public double getWalksatNoise() { return walksatNoise; }
    public int getHybridStagnationThreshold() { return hybridStagnationThreshold; }
    public long getRandomSeed() { return randomSeed; }

    public static class Builder {
        private int dpllSwapThreshold = 5;
        private double walksatNoise = 0.5;
        private int hybridStagnationThreshold = 5;
        private long randomSeed = 42L;

        public Builder dpllSwapThreshold(int threshold) {
            this.dpllSwapThreshold = threshold;
            return this;
        }

        public Builder walksatNoise(double noise) {
            this.walksatNoise = noise;
            return this;
        }

        public Builder hybridStagnationThreshold(int threshold) {
            this.hybridStagnationThreshold = threshold;
            return this;
        }

        public Builder randomSeed(long seed) {
            this.randomSeed = seed;
            return this;
        }

        public SATStrategyConfig build() {
            return new SATStrategyConfig(dpllSwapThreshold, walksatNoise, hybridStagnationThreshold, randomSeed);
        }
    }
}