package com.emergent.doom.chimeric;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;

import java.lang.reflect.Array;

/**
 * Manages populations with multiple algotypes (chimeric populations).
 *
 * <p>Chimeric populations allow mixing different cell behaviors in a
 * single array, enabling study of cooperative and competitive dynamics.</p>
 *
 * <p>From Levin et al. (2024), p.11-12:
 * "At the beginning of these experiments, we randomly assigned one of the three
 * different Algotypes to each of the cells, and began the sort as previously,
 * allowing all the cells to move based on their Algotype."</p>
 *
 * @param <T> the type of cell
 */
public class ChimericPopulation<T extends Cell<T>> {

    private final CellFactory<T> cellFactory;
    private final AlgotypeProvider algotypeProvider;

    /**
     * Create a chimeric population manager.
     *
     * @param cellFactory creates cells with specified position and algotype
     * @param algotypeProvider determines algotype assignment strategy
     */
    public ChimericPopulation(CellFactory<T> cellFactory, AlgotypeProvider algotypeProvider) {
        if (cellFactory == null) {
            throw new IllegalArgumentException("CellFactory cannot be null");
        }
        if (algotypeProvider == null) {
            throw new IllegalArgumentException("AlgotypeProvider cannot be null");
        }
        this.cellFactory = cellFactory;
        this.algotypeProvider = algotypeProvider;
    }

    /**
     * Create a cell array with mixed algotypes.
     *
     * <p>For each position, gets the algotype from the provider and
     * creates a cell using the factory with that algotype.</p>
     *
     * @param size the number of cells to create
     * @param cellClass the cell class for array creation
     * @return array of cells with assigned algotypes
     * @throws IllegalArgumentException if size <= 0 or cellClass is null
     */
    @SuppressWarnings("unchecked")
    public T[] createPopulation(int size, Class<T> cellClass) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        if (cellClass == null) {
            throw new IllegalArgumentException("Cell class cannot be null");
        }

        T[] cells = (T[]) Array.newInstance(cellClass, size);
        for (int i = 0; i < size; i++) {
            String algotype = algotypeProvider.getAlgotype(i, size);
            cells[i] = cellFactory.createCell(i, algotype);
        }
        return cells;
    }

    /**
     * Count cells of a specific algotype in an array.
     *
     * <p>Iterates through the array and counts cells whose algotype
     * matches the specified name (case-insensitive).</p>
     *
     * @param cells the cell array to analyze
     * @param algotype the algotype name to count (e.g., "BUBBLE", "SELECTION")
     * @return number of cells with the specified algotype
     * @throws IllegalArgumentException if cells or algotype is null
     */
    public int countAlgotype(T[] cells, String algotype) {
        if (cells == null) {
            throw new IllegalArgumentException("Cells array cannot be null");
        }
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }

        int count = 0;
        for (T cell : cells) {
            if (cell != null) {
                Algotype cellAlgotype = null;
                if (cell instanceof com.emergent.doom.cell.HasAlgotype) {
                    cellAlgotype = ((com.emergent.doom.cell.HasAlgotype) cell).getAlgotype();
                }

                if (cellAlgotype != null && cellAlgotype.name().equalsIgnoreCase(algotype)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Count cells of a specific algotype enum in an array.
     *
     * @param cells the cell array to analyze
     * @param algotype the algotype enum to count
     * @return number of cells with the specified algotype
     */
    public int countAlgotype(T[] cells, Algotype algotype) {
        if (cells == null) {
            throw new IllegalArgumentException("Cells array cannot be null");
        }
        if (algotype == null) {
            throw new IllegalArgumentException("Algotype cannot be null");
        }

        int count = 0;
        for (T cell : cells) {
            if (cell != null) {
                Algotype cellAlgotype = null;
                if (cell instanceof com.emergent.doom.cell.HasAlgotype) {
                    cellAlgotype = ((com.emergent.doom.cell.HasAlgotype) cell).getAlgotype();
                }

                if (cellAlgotype == algotype) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Get the algotype provider used by this population.
     *
     * @return the algotype provider
     */
    public AlgotypeProvider getAlgotypeProvider() {
        return algotypeProvider;
    }

    /**
     * Get the cell factory used by this population.
     *
     * @return the cell factory
     */
    public CellFactory<T> getCellFactory() {
        return cellFactory;
    }
}
