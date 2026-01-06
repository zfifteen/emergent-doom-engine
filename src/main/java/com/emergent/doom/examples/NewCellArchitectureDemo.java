package com.emergent.doom.examples;

import com.emergent.doom.cell.*;

import java.util.*;

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
        System.out.println("=== New Cell Architecture Demo ===\n");
        
        // Create array of 10 cells with mixed algotypes
        List<AbstractSortingCell> cells = createMixedAlgotypeCells(10);
        
        System.out.println("Initial State:");
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Perform a few swap steps manually to demonstrate
        System.out.println("\n--- Performing Manual Swaps ---\n");
        
        // Step 1: Swap cells at positions 0 and 1
        System.out.println("Step 1: Swap positions 0 and 1");
        swapCells(cells, 0, 1);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Step 2: Swap cells at positions 2 and 3
        System.out.println("\nStep 2: Swap positions 2 and 3");
        swapCells(cells, 2, 3);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        // Step 3: Swap cells at positions 1 and 2
        System.out.println("\nStep 3: Swap positions 1 and 2");
        swapCells(cells, 1, 2);
        printCellArray(cells);
        printAlgotypeAggregation(cells);
        
        System.out.println("\n=== Key Observation ===");
        System.out.println("Notice: When cells swap, their algotypes move WITH them.");
        System.out.println("This is the Levin-aligned semantics that enables clustering!");
        System.out.println("\nContrast with old architecture:");
        System.out.println("  OLD: Swap values only, algotypes frozen at positions");
        System.out.println("  NEW: Swap entire cell objects, algotypes travel with cells");
    }
    
    /**
     * Create a mixed array of cells with different algotypes.
     *
     * @param size the number of cells to create
     * @return list of cells with mixed algotypes and random values
     */
    private static List<AbstractSortingCell> createMixedAlgotypeCells(int size) {
        List<AbstractSortingCell> cells = new ArrayList<>();
        Random random = new Random(42); // Seeded for reproducibility
        
        for (int i = 0; i < size; i++) {
            int value = random.nextInt(100);
            
            // Distribute algotypes: 40% BUBBLE, 30% SELECTION, 30% INSERTION
            double rand = random.nextDouble();
            AbstractSortingCell cell;
            
            if (rand < 0.4) {
                cell = new BubbleSortingCell(value, i, random);
            } else if (rand < 0.7) {
                cell = new SelectionSortingCell(value, i, 0);
            } else {
                cell = new InsertionSortingCell(value, i);
            }
            
            cells.add(cell);
        }
        
        return cells;
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
        System.out.print("Positions:  [");
        for (int i = 0; i < cells.size(); i++) {
            System.out.print(String.format("%2d", i));
            if (i < cells.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        
        System.out.print("Values:     [");
        for (int i = 0; i < cells.size(); i++) {
            System.out.print(String.format("%2d", cells.get(i).readValue()));
            if (i < cells.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        
        System.out.print("Algotypes:  [");
        for (int i = 0; i < cells.size(); i++) {
            SortingAlgotype algotype = cells.get(i).readAlgotype();
            String abbrev = algotype == SortingAlgotype.BUBBLE ? "B " : 
                           algotype == SortingAlgotype.SELECTION ? "S " : "I ";
            System.out.print(abbrev);
            if (i < cells.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
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
        
        System.out.printf("Algotypes:  BUBBLE=%d, SELECTION=%d, INSERTION=%d\n", 
            bubbleCount, selectionCount, insertionCount);
        System.out.printf("Aggregation: %.1f%% (consecutive same-algotype pairs)\n", aggregation);
    }
}
