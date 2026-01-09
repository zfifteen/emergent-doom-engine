package com.emergent.doom.examples;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.AbstractSortingCell;
import com.emergent.doom.cell.SortingAlgotype;
import com.emergent.doom.factory.SortingCellFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstration of new cell-based architecture with Levin-aligned semantics.
 *
 * <p><strong>PURPOSE:</strong> Show how the new AbstractCell architecture enables
 * algotypes to travel with cells during swaps, producing Levin-style clustering
 * with dynamic spatial aggregation (18.30% variance signature).</p>
 *
 * <p><strong>KEY DEMONSTRATION:</strong> When cells swap, entire cell objects relocate
 * (value + algotype together), NOT just values. This produces the characteristic
 * mid-sorting aggregation peak that Levin observed.</p>
 */
public class NewCellArchitectureDemo {
    private static final Logger logger = LoggerFactory.getLogger(NewCellArchitectureDemo.class);
    
    /**
     * Run a simple sorting demonstration with the new cell architecture.
     *
     * <p><strong>PURPOSE:</strong> Demonstrate that:
     * <ol>
     *   <li>Cells carry their algotype as intrinsic property</li>
     *   <li>When cells swap, algotypes relocate WITH them</li>
     *   <li>This produces dynamic spatial aggregation patterns</li>
     * </ol>
     * </p>
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        logger.info("=== New Cell Architecture Demo ===\n");
        
        // Create array of 10 cells with mixed algotypes
        List<AbstractSortingCell> cells = createMixedAlgotypeCells(10);
        
        logger.info("Initial State:");
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Perform a few swap steps manually to demonstrate
        logger.info("\n--- Performing Manual Swaps ---\n");
        
        // Step 1: Swap cells at positions 0 and 1
        logger.info("Step 1: Swap positions 0 and 1");
        swapCells(cells, 0, 1);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Step 2: Swap cells at positions 2 and 3
        logger.info("\nStep 2: Swap positions 2 and 3");
        swapCells(cells, 2, 3);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Step 3: Swap cells at positions 1 and 2
        logger.info("\nStep 3: Swap positions 1 and 2");
        swapCells(cells, 1, 2);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        logger.info("\n=== Key Observation ===");
        logger.info("Notice: When cells swap, their algotypes move WITH them.");
        logger.info("This is the Levin-aligned semantics that enables clustering!");
        logger.info("\nContrast with old architecture:");
        logger.info("  OLD: Swap values only, algotypes frozen at positions");
        logger.info("  NEW: Swap entire cell objects, algotypes travel with cells");
    }
    
    /**
     * Create a mixed array of cells with different algotypes.
     *
     * @param size the number of cells to create
     * @return list of cells with mixed algotypes and random values
     */
    private static List<AbstractSortingCell> createMixedAlgotypeCells(int size) {
        // Use the new SortingCellFactory with Levin-aligned semantics
        SortingCellFactory factory = new SortingCellFactory(42); // Seeded for reproducibility
        
        // Distribute algotypes: 40% BUBBLE, 30% SELECTION, 30% INSERTION
        Map<SortingAlgotype, Double> distribution = new HashMap<>();
        distribution.put(SortingAlgotype.BUBBLE, 0.4);
        distribution.put(SortingAlgotype.SELECTION, 0.3);
        distribution.put(SortingAlgotype.INSERTION, 0.3);
        
        return factory.createRandomCells(distribution, size, 100);
    }
    
    /**
     * Swap two cells in the array (Levin-aligned: entire objects swap).
     *
     * <p><strong>CRITICAL:</strong> This is where algotypes travel with cells!
     * We swap entire cell objects, not just values.</p>
     *
     * @param cells the cell array
     * @param i first position
     * @param j second position
     */
    private static void swapCells(List<AbstractSortingCell> cells, int i, int j) {
        // Swap entire cell objects (value + algotype together)
        AbstractSortingCell temp = cells.get(i);
        cells.set(i, cells.get(j));
        cells.set(j, temp);
        
        // Update cell positions
        cells.get(i).updatePositionTo(i);
        cells.get(j).updatePositionTo(j);
    }
    
    /**
     * Print the current state of the cell array.
     *
     * @param cells the cell array
     */
    private static void printCellArray(List<AbstractSortingCell> cells) {
        logger.info("Positions:  [");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.size(); i++) {
            sb.append(String.format("%2d", i));
            if (i < cells.size() - 1) sb.append(", ");
        }
        logger.info(sb.toString() + "]");
        
        sb = new StringBuilder();
        sb.append("Values:     [");
        for (int i = 0; i < cells.size(); i++) {
            sb.append(String.format("%2d", cells.get(i).readValue()));
            if (i < cells.size() - 1) sb.append(", ");
        }
        logger.info(sb.toString() + "]");
        
        sb = new StringBuilder();
        sb.append("Algotypes:  [");
        for (int i = 0; i < cells.size(); i++) {
            SortingAlgotype algotype = cells.get(i).readAlgotype();
            String abbrev = algotype == SortingAlgotype.BUBBLE ? "B " : 
                           algotype == SortingAlgotype.SELECTION ? "S " : "I ";
            sb.append(abbrev);
            if (i < cells.size() - 1) sb.append(", ");
        }
        logger.info(sb.toString() + "]");
    }
    
    /**
     * Calculate and print algotype spatial aggregation.
     *
     * <p><strong>PURPOSE:</strong> Show how same-algotype cells cluster together
     * during sorting, producing the characteristic aggregation variance.</p>
     *
     * @param cells the cell array
     */
    private static void printAlgotypeAggregation(List<AbstractSortingCell> cells) {
        int bubbleCount = 0;
        int selectionCount = 0;
        int insertionCount = 0;
        
        for (AbstractSortingCell cell : cells) {
            SortingAlgotype algotype = cell.readAlgotype();
            if (algotype == SortingAlgotype.BUBBLE) bubbleCount++;
            else if (algotype == SortingAlgotype.SELECTION) selectionCount++;
            else if (algotype == SortingAlgotype.INSERTION) insertionCount++;
        }
        
        // Simple aggregation metric: count consecutive same-algotype pairs
        int consecutivePairs = 0;
        for (int i = 0; i < cells.size() - 1; i++) {
            if (cells.get(i).readAlgotype() == cells.get(i + 1).readAlgotype()) {
                consecutivePairs++;
            }
        }
        
        double aggregation = (double) consecutivePairs / (cells.size() - 1) * 100;
        
        logger.info("Algotypes:  BUBBLE={}, SELECTION={}, INSERTION={}", 
            bubbleCount, selectionCount, insertionCount);
        logger.info("Aggregation: {:.1f}% (consecutive same-algotype pairs)", aggregation);
    }
}