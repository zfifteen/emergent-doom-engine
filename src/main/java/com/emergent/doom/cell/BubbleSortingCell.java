package com.emergent.doom.cell;

import java.util.Optional;
import java.util.Random;

/**
 * Bubble sort cell implementation with random bidirectional movement.
 *
 * <p><strong>PURPOSE:</strong> Implement BUBBLE algotype behavior where cells randomly
 * pick one adjacent neighbor (left or right, 50/50 probability) and swap if value
 * ordering is wrong for the chosen direction.</p>
 *
 * <p><strong>KEY BEHAVIORAL CHARACTERISTICS:</strong></p>
 * <ul>
 *   <li>Visibility: Only sees immediate left and right neighbors</li>
 *   <li>Movement: Random direction choice (50% left, 50% right) per step</li>
 *   <li>Swap rule: Swap if moving toward sorted position for chosen direction</li>
 *   <li>Strategy: Opportunistic - makes greedy local swaps</li>
 * </ul>
 *
 * <p><strong>LEVIN REFERENCE:</strong> "Bubble sort cells compare with immediate
 * neighbors and swap if out of order" (Levin et al., 2024, p. 4).</p>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> Algotype is immutable (BUBBLE), travels with
 * cell during swaps, enabling Levin-style spatial clustering.</p>
 *
 * <p><strong>EXPECTED INPUTS:</strong> value (int), initialPosition (int), Random (for direction choice)</p>
 * <p><strong>EXPECTED OUTPUTS:</strong> Movement decisions based on random neighbor selection</p>
 * <p><strong>DATA FLOW:</strong> Engine provides NeighborhoodView → Cell randomly picks neighbor → Swap if wrong order</p>
 */
public class BubbleSortingCell extends AbstractSortingCell {
    
    // PURPOSE: Random number generator for direction choice (50/50 left/right)
    // INPUTS: Provided during construction, can be seeded for deterministic testing
    // PROCESS: Used in calculateTargetPositionGiven() to randomly select neighbor
    // OUTPUTS: Random direction selection (0 = left, 1 = right if both exist)
    // DEPENDENCIES: Must be non-null
    // ARCHITECTURE NOTE: Matches Levin paper behavior - each BUBBLE cell randomly
    //                    picks ONE direction per step, not both
    private final Random random;
    
    /**
     * Create a BubbleSortingCell with specified value, position, and Random instance.
     *
     * <p><strong>PURPOSE:</strong> Initialize BUBBLE cell with immutable BUBBLE algotype
     * and provided Random instance (for deterministic testing or seeded randomness).</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>initialPosition - Starting position in array</li>
     *   <li>random - Random instance for direction choice (required, non-null)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Call super constructor with value, SortingAlgotype.BUBBLE, initialPosition</li>
     *   <li>Validate random is not null</li>
     *   <li>Store random instance</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized BubbleSortingCell</p>
     *
     * <p><strong>DEPENDENCIES:</strong> None</p>
     *
     * @param value the sort key value
     * @param initialPosition the starting position in array
     * @param random the Random instance for direction choice (never null)
     * @throws NullPointerException if random is null
     */
    public BubbleSortingCell(int value, int initialPosition, Random random) {
        // PURPOSE: Initialize BUBBLE cell with immutable algotype and Random instance
        // PROCESS:
        //   1. Call super constructor with BUBBLE algotype (immutable)
        //   2. Validate random is not null
        //   3. Store random instance for direction choice
        
        super(value, SortingAlgotype.BUBBLE, initialPosition);
        
        if (random == null) {
            throw new NullPointerException("random cannot be null");
        }
        
        this.random = random;
    }
    
    /**
     * Create a BubbleSortingCell with specified value and position, using default Random.
     *
     * <p><strong>PURPOSE:</strong> Convenience constructor for non-deterministic random direction.
     * Creates unseeded Random instance.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>value - The sort key (immutable)</li>
     *   <li>initialPosition - Starting position in array</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong> Delegate to primary constructor with new Random()</p>
     *
     * <p><strong>OUTPUTS:</strong> Fully initialized BubbleSortingCell with unseeded Random</p>
     *
     * @param value the sort key value
     * @param initialPosition the starting position in array
     */
    public BubbleSortingCell(int value, int initialPosition) {
        this(value, initialPosition, new Random());
    }
    
    /**
     * Determine if this cell should attempt to move given its neighbors.
     *
     * <p><strong>PURPOSE:</strong> Implement BUBBLE movement predicate - move if at least
     * one adjacent neighbor exists with wrong value ordering.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView with left/right adjacent cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if any neighbors are visible</li>
     *   <li>If yes, return true (will pick one randomly in calculateTargetPositionGiven)</li>
     *   <li>If no, return false (no movement possible)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if has neighbors, false otherwise</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView must provide adjacent neighbors</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> Separates "do I want to move?" from "where?"
     * BUBBLE always wants to move if neighbors exist - direction/swap decision happens later.</p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return true if cell should attempt to move
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Check if BUBBLE cell wants to move
        // PROCESS:
        //   1. BUBBLE cells are opportunistic - always want to move if neighbors exist
        //   2. Return true if neighbors available, false otherwise
        // ARCHITECTURE: Movement decision separated from direction selection
        
        return neighbors.hasNeighbors();
    }
    
    /**
     * Calculate the target position for this cell's move.
     *
     * <p><strong>PURPOSE:</strong> Implement BUBBLE target selection - randomly pick
     * ONE adjacent neighbor (50/50 left/right if both exist) and return its position
     * if swap would move toward sorted order.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - NeighborhoodView with left/right adjacent cells</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Get left and right neighbors from view</li>
     *   <li>If no neighbors exist, return Optional.empty()</li>
     *   <li>If both neighbors exist:
     *     <ul>
     *       <li>Randomly pick one (50/50 probability)</li>
     *       <li>Check if swap with chosen neighbor moves toward sorted order</li>
     *       <li>Return Optional.of(neighborPosition) if yes</li>
     *       <li>Return Optional.empty() if no</li>
     *     </ul>
     *   </li>
     *   <li>If only one neighbor exists:
     *     <ul>
     *       <li>Check if swap with that neighbor moves toward sorted order</li>
     *       <li>Return Optional.of(neighborPosition) if yes</li>
     *       <li>Return Optional.empty() if no</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>SWAP RULES:</strong></p>
     * <ul>
     *   <li>Ascending sort (default): Swap left if this.value < left.value, swap right if this.value > right.value</li>
     *   <li>Descending sort: Swap left if this.value > left.value, swap right if this.value < right.value</li>
     * </ul>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing target position, or empty if no valid swap</p>
     *
     * <p><strong>DEPENDENCIES:</strong> NeighborhoodView, Random for direction choice</p>
     *
     * <p><strong>GROUND TRUTH REFERENCE:</strong> cell_research/BubbleSortCell.py:54-56:
     * <pre>
     * if self.reverse_direction:
     *     return self.value < target if check_right else self.value > target
     * return self.value > target if check_right else self.value < target
     * </pre>
     * </p>
     *
     * @param neighbors the neighborhood view for this cell
     * @return Optional containing target position, or empty if no valid swap
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<Integer, SortingAlgotype> neighbors) {
        // PURPOSE: Calculate BUBBLE swap target via random neighbor selection
        // PROCESS:
        //   1. Get left/right neighbors from view
        //   2. If both exist, randomly pick one (50/50)
        //   3. If only one exists, use that one
        //   4. Check if swap with chosen neighbor moves toward sorted order
        //   5. Return position if yes, empty if no
        // GROUND TRUTH: Matches cell_research Python random direction selection
        
        Optional<AbstractCell<Integer, SortingAlgotype>> leftOpt = neighbors.getLeftNeighbor();
        Optional<AbstractCell<Integer, SortingAlgotype>> rightOpt = neighbors.getRightNeighbor();
        
        // No neighbors available
        if (leftOpt.isEmpty() && rightOpt.isEmpty()) {
            return Optional.empty();
        }
        
        // Pick random neighbor if both exist, otherwise use the one that exists
        AbstractCell<Integer, SortingAlgotype> chosenNeighbor;
        boolean isLeft;
        
        if (leftOpt.isPresent() && rightOpt.isPresent()) {
            // Both neighbors exist - randomly pick one (50/50)
            boolean pickLeft = random.nextBoolean();
            chosenNeighbor = pickLeft ? leftOpt.get() : rightOpt.get();
            isLeft = pickLeft;
        } else if (leftOpt.isPresent()) {
            // Only left neighbor exists
            chosenNeighbor = leftOpt.get();
            isLeft = true;
        } else {
            // Only right neighbor exists
            chosenNeighbor = rightOpt.get();
            isLeft = false;
        }
        
        // Check if swap with chosen neighbor moves toward sorted order
        // Ascending sort (default for this implementation):
        //   - Swap left if this.value < left.value (smaller value moves left)
        //   - Swap right if this.value > right.value (larger value moves right)
        
        boolean shouldSwap;
        if (isLeft) {
            // Swapping left: beneficial if this.value < chosenNeighbor.value
            shouldSwap = this.value < chosenNeighbor.readValue();
        } else {
            // Swapping right: beneficial if this.value > chosenNeighbor.value
            shouldSwap = this.value > chosenNeighbor.readValue();
        }
        
        if (shouldSwap) {
            return Optional.of(chosenNeighbor.readCurrentPosition());
        } else {
            return Optional.empty();
        }
    }
}
