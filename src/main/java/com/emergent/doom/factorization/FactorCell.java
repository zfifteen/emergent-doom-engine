package com.emergent.doom.factorization;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;

import java.util.Optional;

/**
 * Cell implementation for integer factorization domain.
 *
 * <p><strong>PURPOSE:</strong> Represent a factor candidate with an associated
 * factor-finding strategy, enabling emergent clustering based on candidate
 * fitness and strategy compatibility.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Value semantics: candidate is the integer being evaluated as a potential factor</li>
 *   <li>Fitness-based comparison: cells sort by factor fitness (N mod candidate proximity)</li>
 *   <li>Strategy as algotype: different factor-finding approaches encoded as strategies</li>
 *   <li>Levin-aligned: strategy travels WITH cell during swaps, enabling clustering</li>
 * </ul>
 *
 * <p><strong>COMPARISON LOGIC:</strong> Cells compare based on factor fitness score,
 * NOT raw candidate value. This allows true factors (remainder=0) to naturally
 * migrate to array front.</p>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> Extends AbstractCell&lt;Integer, FactorStrategy&gt;
 * to reuse EDE cell infrastructure while providing factorization-specific behavior.</p>
 *
 * <p><strong>REFERENCE:</strong> See FIRST_NON_SORTING_EXPERIMENT.md for experimental
 * design and fitness function definition.</p>
 */
public class FactorCell extends AbstractCell<Integer, FactorStrategy> {
    
    // ==================== IMMUTABLE PROPERTIES ====================
    
    /** The candidate integer being evaluated as a potential factor */
    private final int candidate;
    
    /** The semiprime being factored (target N) */
    private final int target;
    
    /** The factor-finding strategy (algotype) */
    private final FactorStrategy strategy;
    
    /** Pre-computed factor fitness score (cached for performance) */
    private final double fitness;
    
    // ==================== MUTABLE STATE ====================
    
    /** Current position in array (updated during swaps) */
    private int currentPosition;
    
    /** Current execution status (ACTIVE, FREEZE, etc.) */
    private CellStatus status;
    
    /**
     * Create a new FactorCell.
     *
     * <p><strong>PURPOSE:</strong> Initialize a factor candidate cell with strategy
     * and pre-computed fitness score.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>candidate - integer to evaluate as potential factor</li>
     *   <li>target - the semiprime N being factored</li>
     *   <li>strategy - the factor-finding strategy (algotype)</li>
     *   <li>initialPosition - starting position in array</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate candidate > 1 (0 and 1 are not valid factors)</li>
     *   <li>Validate target > candidate (factor must be smaller than target)</li>
     *   <li>Compute factor fitness score</li>
     *   <li>Initialize position and status</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Initialized FactorCell ready for use</p>
     *
     * @param candidate the candidate integer
     * @param target the semiprime N being factored
     * @param strategy the factor-finding strategy
     * @param initialPosition starting position in array
     * @throws IllegalArgumentException if candidate <= 1 or candidate >= target
     */
    public FactorCell(int candidate, int target, FactorStrategy strategy, int initialPosition) {
        if (candidate <= 1) {
            throw new IllegalArgumentException("Candidate must be > 1, got: " + candidate);
        }
        if (candidate >= target) {
            throw new IllegalArgumentException(
                String.format("Candidate (%d) must be < target (%d)", candidate, target)
            );
        }
        
        this.candidate = candidate;
        this.target = target;
        this.strategy = strategy;
        this.currentPosition = initialPosition;
        this.status = CellStatus.ACTIVE;
        
        // Pre-compute fitness for performance
        this.fitness = computeFactorFitness(candidate, target);
    }
    
    /**
     * Compute factor fitness score for a candidate.
     *
     * <p><strong>PURPOSE:</strong> Measure how "close" a candidate is to being
     * a true factor of N. Perfect factors (N mod candidate = 0) score 1.0.</p>
     *
     * <p><strong>FORMULA:</strong></p>
     * <pre>
     * if (N % candidate == 0) return 1.0;  // Perfect factor
     * 
     * distanceToMultiple = min(remainder, candidate - remainder)
     * fitness = 1.0 - (distanceToMultiple / candidate)
     * </pre>
     *
     * <p><strong>INTUITION:</strong> Candidates that leave small remainders are
     * "almost" factors. We measure distance to nearest multiple and normalize.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>candidate - integer to evaluate</li>
     *   <li>N - the semiprime being factored</li>
     * </ul>
     *
     * <p><strong>OUTPUTS:</strong> Fitness score in [0.0, 1.0], where 1.0 = perfect factor</p>
     *
     * <p><strong>REFERENCE:</strong> FIRST_NON_SORTING_EXPERIMENT.md Section 3.3</p>
     *
     * @param candidate the candidate integer
     * @param N the semiprime being factored
     * @return fitness score in [0.0, 1.0]
     */
    private static double computeFactorFitness(int candidate, int N) {
        int remainder = N % candidate;
        if (remainder == 0) {
            return 1.0;  // Perfect factor
        }
        
        // Distance to nearest multiple (either up or down)
        int distanceToMultiple = Math.min(remainder, candidate - remainder);
        
        // Normalize to [0.0, 1.0] range
        // Smaller distance = higher fitness
        return 1.0 - ((double) distanceToMultiple / candidate);
    }
    
    // ==================== INTRINSIC PROPERTIES ====================
    
    @Override
    public FactorStrategy readAlgotype() {
        return strategy;
    }
    
    @Override
    public Integer readValue() {
        return candidate;
    }
    
    /**
     * Get the factor fitness score.
     *
     * <p><strong>PURPOSE:</strong> Expose pre-computed fitness for metrics and analysis.</p>
     *
     * @return fitness score in [0.0, 1.0]
     */
    public double getFitness() {
        return fitness;
    }
    
    /**
     * Check if this candidate is a perfect factor.
     *
     * <p><strong>PURPOSE:</strong> Quick check for true factors (fitness = 1.0).</p>
     *
     * @return true if this candidate divides target evenly
     */
    public boolean isPerfectFactor() {
        return fitness == 1.0;
    }
    
    // ==================== MUTABLE STATE ====================
    
    @Override
    public int readCurrentPosition() {
        return currentPosition;
    }
    
    @Override
    public void updatePositionTo(int newPosition) {
        if (newPosition < 0) {
            throw new IllegalArgumentException("Position cannot be negative: " + newPosition);
        }
        this.currentPosition = newPosition;
    }
    
    @Override
    public CellStatus readStatus() {
        return status;
    }
    
    @Override
    public void updateStatusTo(CellStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = newStatus;
    }
    
    // ==================== BEHAVIORAL POLICY ====================
    
    /**
     * Simple movement policy: always willing to move if not at optimal position.
     *
     * <p><strong>PURPOSE:</strong> FactorCells use simple bubble-like movement.
     * No complex targeting like Selection sort.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - neighborhood view (currently unused)</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check if status allows movement</li>
     *   <li>Return true if ACTIVE (can move)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> true if cell wants to move</p>
     *
     * @param neighbors the neighborhood view
     * @return true if cell should attempt to move
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<Integer, FactorStrategy> neighbors) {
        // Simple policy: always willing to improve position if active
        return status == CellStatus.ACTIVE;
    }
    
    /**
     * Calculate target position based on neighbor fitness.
     *
     * <p><strong>PURPOSE:</strong> Implement bubble-like local swapping based on
     * fitness comparison.</p>
     *
     * <p><strong>INPUTS:</strong> neighbors - neighborhood view with left/right neighbors</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Check left neighbor: if it has lower fitness, propose swap left</li>
     *   <li>Check right neighbor: if it has higher fitness, propose swap right</li>
     *   <li>Return Optional.empty() if no beneficial swap exists</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> Optional containing target position, or empty</p>
     *
     * @param neighbors the neighborhood view
     * @return Optional containing target position, or empty if no valid target
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(NeighborhoodView<Integer, FactorStrategy> neighbors) {
        // Check left neighbor: move left if left has lower fitness
        Optional<AbstractCell<Integer, FactorStrategy>> leftOpt = neighbors.getLeftNeighbor();
        if (leftOpt.isPresent()) {
            FactorCell left = (FactorCell) leftOpt.get();
            if (this.fitness > left.fitness) {
                return Optional.of(left.readCurrentPosition());
            }
        }
        
        // Check right neighbor: move right if right has higher fitness
        Optional<AbstractCell<Integer, FactorStrategy>> rightOpt = neighbors.getRightNeighbor();
        if (rightOpt.isPresent()) {
            FactorCell right = (FactorCell) rightOpt.get();
            if (this.fitness < right.fitness) {
                return Optional.of(right.readCurrentPosition());
            }
        }
        
        // No beneficial swap
        return Optional.empty();
    }
    
    // ==================== SWAP ELIGIBILITY ====================
    
    @Override
    public boolean canInitiateSwap() {
        return status == CellStatus.ACTIVE;
    }
    
    @Override
    public boolean canAcceptSwapFrom(AbstractCell<Integer, FactorStrategy> initiator) {
        // Can accept if ACTIVE or FREEZE (frozen cells can be moved but not initiate)
        return status == CellStatus.ACTIVE || status == CellStatus.FREEZE;
    }
    
    // ==================== VALUE COMPARISON ====================
    
    /**
     * Compare fitness scores (NOT raw candidate values).
     *
     * <p><strong>CRITICAL:</strong> FactorCells compare by FITNESS, not by candidate value.
     * This allows true factors (high fitness) to bubble to the front.</p>
     *
     * @param other the cell to compare against
     * @return true if this cell has higher fitness
     */
    @Override
    public boolean hasGreaterValueThan(AbstractCell<Integer, FactorStrategy> other) {
        FactorCell otherFactor = (FactorCell) other;
        return this.fitness > otherFactor.fitness;
    }
    
    /**
     * Compare cells by fitness for sorting.
     *
     * <p><strong>CRITICAL:</strong> Comparison is by FITNESS, not candidate value.
     * Higher fitness = "greater" cell (sorts to front).</p>
     *
     * <p><strong>ARCHITECTURE NOTE:</strong> This deviates from readValue().compareTo()
     * pattern because factorization domain compares on derived fitness, not raw value.</p>
     *
     * @param other the cell to compare against
     * @return comparison result (descending fitness order)
     */
    @Override
    public int compareTo(AbstractCell<Integer, FactorStrategy> other) {
        FactorCell otherFactor = (FactorCell) other;
        // Descending fitness order (higher fitness = "less than" for front-of-array)
        return Double.compare(otherFactor.fitness, this.fitness);
    }
    
    // ==================== DISPLAY ====================
    
    @Override
    public String toString() {
        return String.format("FactorCell[candidate=%d, fitness=%.3f, strategy=%s, pos=%d]",
            candidate, fitness, strategy, currentPosition);
    }
}
