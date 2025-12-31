package com.emergent.doom.experiment;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.execution.ExecutionMode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for chimeric population experiments.
 *
 * <p>Extends ExperimentConfig with chimeric-specific settings including
 * algotype distribution and frozen cell configuration.</p>
 *
 * <p>From Levin et al. (2024), chimeric experiments used:
 * <ul>
 *   <li>50/50 Bubble/Selection mix</li>
 *   <li>50/50 Bubble/Insertion mix</li>
 *   <li>33/33/34 three-way mix</li>
 *   <li>0-3 frozen cells for robustness testing</li>
 * </ul></p>
 *
 * <p>Usage:
 * <pre>{@code
 * ChimericExperimentConfig config = ChimericExperimentConfig.builder()
 *     .arraySize(100)
 *     .maxSteps(5000)
 *     .requiredStableSteps(3)
 *     .recordTrajectory(true)
 *     .algotypeMix(Map.of(Algotype.BUBBLE, 0.5, Algotype.SELECTION, 0.5))
 *     .frozenCellCount(0)
 *     .seed(42L)
 *     .build();
 * }</pre></p>
 */
public class ChimericExperimentConfig extends ExperimentConfig {

    /** Key for chimeric mix in custom parameters */
    public static final String PARAM_CHIMERIC_MIX = "chimericMix";
    /** Key for frozen cell count in custom parameters */
    public static final String PARAM_FROZEN_CELL_COUNT = "frozenCellCount";
    /** Key for random seed in custom parameters */
    public static final String PARAM_SEED = "seed";
    /** Key for sort direction in custom parameters */
    public static final String PARAM_SORT_DIRECTION = "sortDirection";

    private final Map<Algotype, Double> chimericMix;
    private final int frozenCellCount;
    private final long seed;
    private final SortDirection sortDirection;

    private ChimericExperimentConfig(Builder builder) {
        super(builder.arraySize, builder.maxSteps, builder.requiredStableSteps,
              builder.recordTrajectory, builder.executionMode);

        this.chimericMix = Collections.unmodifiableMap(new HashMap<>(builder.chimericMix));
        this.frozenCellCount = builder.frozenCellCount;
        this.seed = builder.seed;
        this.sortDirection = builder.sortDirection;

        // Store in custom parameters for serialization compatibility
        setCustomParameter(PARAM_CHIMERIC_MIX, this.chimericMix);
        setCustomParameter(PARAM_FROZEN_CELL_COUNT, this.frozenCellCount);
        setCustomParameter(PARAM_SEED, this.seed);
        setCustomParameter(PARAM_SORT_DIRECTION, this.sortDirection);
    }

    /**
     * Get the algotype distribution for this chimeric experiment.
     *
     * @return unmodifiable map of algotype to percentage (0.0 to 1.0)
     */
    public Map<Algotype, Double> getChimericMix() {
        return chimericMix;
    }

    /**
     * Check if this is a chimeric (multi-algotype) experiment.
     *
     * @return true if more than one algotype is configured
     */
    public boolean isChimeric() {
        return chimericMix.size() > 1;
    }

    /**
     * Get the number of frozen (immovable) cells.
     *
     * @return frozen cell count (0 for none)
     */
    public int getFrozenCellCount() {
        return frozenCellCount;
    }

    /**
     * Get the random seed for reproducibility.
     *
     * @return random seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Get the target sort direction.
     *
     * @return INCREASING or DECREASING
     */
    public SortDirection getSortDirection() {
        return sortDirection;
    }

    /**
     * Create a new builder for ChimericExperimentConfig.
     *
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ChimericExperimentConfig.
     */
    public static class Builder {
        private int arraySize = 100;
        private int maxSteps = 5000;
        private int requiredStableSteps = 3;
        private boolean recordTrajectory = true;
        private ExecutionMode executionMode = ExecutionMode.SEQUENTIAL;
        private Map<Algotype, Double> chimericMix = new HashMap<>();
        private int frozenCellCount = 0;
        private long seed = System.nanoTime();
        private SortDirection sortDirection = SortDirection.INCREASING;

        /**
         * Set the array size.
         */
        public Builder arraySize(int arraySize) {
            this.arraySize = arraySize;
            return this;
        }

        /**
         * Set maximum steps before timeout.
         */
        public Builder maxSteps(int maxSteps) {
            this.maxSteps = maxSteps;
            return this;
        }

        /**
         * Set required stable steps for convergence.
         */
        public Builder requiredStableSteps(int requiredStableSteps) {
            this.requiredStableSteps = requiredStableSteps;
            return this;
        }

        /**
         * Set whether to record full trajectory.
         */
        public Builder recordTrajectory(boolean recordTrajectory) {
            this.recordTrajectory = recordTrajectory;
            return this;
        }

        /**
         * Set execution mode (SEQUENTIAL or PARALLEL).
         */
        public Builder executionMode(ExecutionMode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        /**
         * Set the algotype distribution.
         *
         * @param chimericMix map of algotype to percentage (must sum to 1.0)
         */
        public Builder algotypeMix(Map<Algotype, Double> chimericMix) {
            this.chimericMix = new HashMap<>(chimericMix);
            return this;
        }

        /**
         * Configure a 50/50 two-algotype mix.
         */
        public Builder twoWayMix(Algotype first, Algotype second) {
            this.chimericMix = Map.of(first, 0.5, second, 0.5);
            return this;
        }

        /**
         * Configure a 33/33/34 three-algotype mix.
         */
        public Builder threeWayMix() {
            this.chimericMix = Map.of(
                Algotype.BUBBLE, 0.33,
                Algotype.INSERTION, 0.33,
                Algotype.SELECTION, 0.34
            );
            return this;
        }

        /**
         * Configure a single algotype (non-chimeric).
         */
        public Builder singleAlgotype(Algotype algotype) {
            this.chimericMix = Map.of(algotype, 1.0);
            return this;
        }

        /**
         * Set the number of frozen cells.
         */
        public Builder frozenCellCount(int frozenCellCount) {
            this.frozenCellCount = frozenCellCount;
            return this;
        }

        /**
         * Set the random seed for reproducibility.
         */
        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        /**
         * Set the target sort direction.
         */
        public Builder sortDirection(SortDirection sortDirection) {
            this.sortDirection = sortDirection;
            return this;
        }

        /**
         * Build the configuration.
         *
         * @return new ChimericExperimentConfig
         * @throws IllegalArgumentException if configuration is invalid
         */
        public ChimericExperimentConfig build() {
            // Validate chimeric mix
            if (chimericMix.isEmpty()) {
                throw new IllegalArgumentException("At least one algotype must be configured");
            }
            double sum = chimericMix.values().stream().mapToDouble(Double::doubleValue).sum();
            if (Math.abs(sum - 1.0) > 0.01) {
                throw new IllegalArgumentException("Algotype percentages must sum to 1.0, got: " + sum);
            }

            return new ChimericExperimentConfig(this);
        }
    }
}
