package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.cell.SortDirection;

import java.util.Random;

/**
 * Factory for creating GenericCell instances with specified algotypes and sort directions.
 *
 * <p>Supports multiple value assignment strategies:
 * <ul>
 *   <li>Sequential: values 1 to N based on position</li>
 *   <li>Random: random values within a range</li>
 *   <li>Shuffled: values 1 to N in random order</li>
 * </ul></p>
 * 
 * <p>Supports configurable sort direction strategies for cross-purpose sorting experiments
 * (Levin et al. 2024, p.14).</p>
 */
public class GenericCellFactory implements CellFactory<GenericCell> {

    private final ValueStrategy valueStrategy;
    private final DirectionStrategy directionStrategy;
    private final int[] shuffledValues;
    private final Random valueRandom;
    private final Random directionRandom;

    /**
     * Value assignment strategy.
     */
    public enum ValueStrategy {
        /** Values 1 to N based on position (position + 1) */
        SEQUENTIAL,
        /** Random values within configured range */
        RANDOM,
        /** Pre-shuffled values 1 to N */
        SHUFFLED
    }

    /**
     * Sort direction assignment strategy for cross-purpose sorting.
     */
    public enum DirectionStrategy {
        /** All cells sort ascending (default, backward compatible) */
        ALL_ASCENDING,
        /** All cells sort descending */
        ALL_DESCENDING,
        /** Alternating: even positions ascending, odd descending */
        ALTERNATING,
        /** Random 50/50 distribution */
        RANDOM
    }

    /**
     * Create a factory with sequential value assignment (1 to N) and ascending direction.
     */
    public GenericCellFactory() {
        this(ValueStrategy.SEQUENTIAL, DirectionStrategy.ALL_ASCENDING, 0, 0);
    }

    /**
     * Create a factory with specified value strategy and default ascending direction.
     *
     * @param strategy the value assignment strategy
     * @param arraySize the total array size (for SHUFFLED strategy)
     * @param seed random seed (for RANDOM and SHUFFLED strategies)
     */
    public GenericCellFactory(ValueStrategy strategy, int arraySize, long seed) {
        this(strategy, DirectionStrategy.ALL_ASCENDING, arraySize, seed);
    }

    /**
     * Create a factory with specified value and direction strategies.
     *
     * <p>Uses separate Random instances for value and direction generation to ensure
     * reproducible behavior when using RANDOM strategies for both.</p>
     *
     * @param valueStrategy the value assignment strategy
     * @param directionStrategy the sort direction assignment strategy
     * @param arraySize the total array size (for SHUFFLED strategy)
     * @param seed random seed (for RANDOM and SHUFFLED strategies)
     */
    public GenericCellFactory(ValueStrategy valueStrategy, DirectionStrategy directionStrategy, int arraySize, long seed) {
        this.valueStrategy = valueStrategy;
        this.directionStrategy = directionStrategy;
        // Use separate Random instances to avoid coupling between value and direction generation
        this.valueRandom = new Random(seed);
        this.directionRandom = new Random(seed + 1);  // Different seed for independent stream

        if (valueStrategy == ValueStrategy.SHUFFLED && arraySize > 0) {
            // Pre-generate shuffled values
            shuffledValues = new int[arraySize];
            for (int i = 0; i < arraySize; i++) {
                shuffledValues[i] = i + 1;
            }
            // Fisher-Yates shuffle
            for (int i = arraySize - 1; i > 0; i--) {
                int j = valueRandom.nextInt(i + 1);
                int temp = shuffledValues[i];
                shuffledValues[i] = shuffledValues[j];
                shuffledValues[j] = temp;
            }
        } else {
            shuffledValues = null;
        }
    }

    /**
     * Create a factory with shuffled values.
     *
     * @param arraySize the total array size
     * @param seed random seed for shuffling
     * @return factory with SHUFFLED strategy
     */
    public static GenericCellFactory shuffled(int arraySize, long seed) {
        return new GenericCellFactory(ValueStrategy.SHUFFLED, arraySize, seed);
    }

    /**
     * Create a factory with random values.
     *
     * @param seed random seed
     * @return factory with RANDOM strategy
     */
    public static GenericCellFactory random(long seed) {
        return new GenericCellFactory(ValueStrategy.RANDOM, 0, seed);
    }

    /**
     * Create a factory with sequential values.
     *
     * @return factory with SEQUENTIAL strategy
     */
    public static GenericCellFactory sequential() {
        return new GenericCellFactory();
    }

    @Override
    public GenericCell createCell(int position, String algotypeStr) {
        if (algotypeStr == null) {
            throw new IllegalArgumentException("Algotype string cannot be null");
        }
        Algotype algotype = Algotype.valueOf(algotypeStr.toUpperCase());
        int value = getValue(position);
        SortDirection direction = getDirection(position);
        return new GenericCell(value, algotype, direction);
    }

    private int getValue(int position) {
        switch (valueStrategy) {
            case SEQUENTIAL:
                return position + 1;
            case RANDOM:
                return valueRandom.nextInt(1000) + 1;
            case SHUFFLED:
                if (shuffledValues != null && position < shuffledValues.length) {
                    return shuffledValues[position];
                }
                return position + 1;
            default:
                return position + 1;
        }
    }

    private SortDirection getDirection(int position) {
        switch (directionStrategy) {
            case ALL_ASCENDING:
                return SortDirection.ASCENDING;
            case ALL_DESCENDING:
                return SortDirection.DESCENDING;
            case ALTERNATING:
                return (position % 2 == 0) ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            case RANDOM:
                return directionRandom.nextBoolean() ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            default:
                return SortDirection.ASCENDING;
        }
    }
}
