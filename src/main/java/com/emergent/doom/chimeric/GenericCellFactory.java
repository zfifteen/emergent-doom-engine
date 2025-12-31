package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;

import java.util.Random;

/**
 * Factory for creating GenericCell instances with specified algotypes.
 *
 * <p>Supports multiple value assignment strategies:
 * <ul>
 *   <li>Sequential: values 1 to N based on position</li>
 *   <li>Random: random values within a range</li>
 *   <li>Shuffled: values 1 to N in random order</li>
 * </ul></p>
 */
public class GenericCellFactory implements CellFactory<GenericCell> {

    private final ValueStrategy valueStrategy;
    private final int[] shuffledValues;
    private final Random random;

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
     * Create a factory with sequential value assignment (1 to N).
     */
    public GenericCellFactory() {
        this(ValueStrategy.SEQUENTIAL, 0, 0);
    }

    /**
     * Create a factory with specified value strategy.
     *
     * @param strategy the value assignment strategy
     * @param arraySize the total array size (for SHUFFLED strategy)
     * @param seed random seed (for RANDOM and SHUFFLED strategies)
     */
    public GenericCellFactory(ValueStrategy strategy, int arraySize, long seed) {
        this.valueStrategy = strategy;
        this.random = new Random(seed);

        if (strategy == ValueStrategy.SHUFFLED && arraySize > 0) {
            // Pre-generate shuffled values
            shuffledValues = new int[arraySize];
            for (int i = 0; i < arraySize; i++) {
                shuffledValues[i] = i + 1;
            }
            // Fisher-Yates shuffle
            for (int i = arraySize - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
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
        Algotype algotype = Algotype.valueOf(algotypeStr.toUpperCase());
        int value = getValue(position);
        return new GenericCell(value, algotype);
    }

    private int getValue(int position) {
        switch (valueStrategy) {
            case SEQUENTIAL:
                return position + 1;
            case RANDOM:
                return random.nextInt(1000) + 1;
            case SHUFFLED:
                if (shuffledValues != null && position < shuffledValues.length) {
                    return shuffledValues[position];
                }
                return position + 1;
            default:
                return position + 1;
        }
    }
}
