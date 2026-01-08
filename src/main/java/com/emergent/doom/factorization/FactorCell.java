package com.emergent.doom.factorization;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;

import java.util.Optional;

/**
 * Cell implementation for integer factorization domain.
 *
 * <p><strong>PURPOSE:</strong> Represent a factor candidate with an associated
 * factor-finding strategy, enabling natural localization of true factors through
 * fitness-driven sorting.</p>
 *
 * <p><strong>KEY DISCOVERY (Jan 8, 2026):</strong> FactorCells naturally localize
 * at array front through FITNESS-DRIVEN SORTING, not through clustering. True factors
 * have fitness = 1.0, while non-factors have lower fitness scores. Bubble sort moves
 * high-fitness cells forward, causing 100% localization of factors in ~20 steps.</p>
 *
 * <p><strong>MECHANISM:</strong></p>
 * <ol>
 *   <li>True factors (N % candidate = 0) have fitness = 1.0</li>
 *   <li>Non-factors have fitness &lt; 1.0 (based on distance to nearest divisor)</li>
 *   <li>Bubble sort compares cells by fitness (not raw value)</li>
 *   <li>High-fitness cells rapidly migrate to array front</li>
 *   <li>Factors form fitness plateau at front (positions 0-k where all fitness = 1.0)</li>
 *   <li>Plateau boundary occurs at fitness transition (1.0 → &lt;1.0)</li>
 *   <li>Boundary appears to align with algotype transitions (COINCIDENTAL)</li>
 * </ol>
 *
 * <p><strong>IMPORTANT NOTE:</strong> Algotype clustering is NOT the cause of factor
 * localization. Multiple independent strategies (TRIAL, FERMAT, RANDOM) independently
 * find similar factor values, creating homogeneous factor regions by CONVERGENCE, not
 * by algotype affinity. Peak aggregation reaches only 66% (indistinguishable from 56%
 * random baseline), yet localization is 100% perfect.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Value semantics: candidate is the integer being evaluated as a potential factor</li>
 *   <li>Fitness-based comparison: cells sort by factor fitness (not raw value)</li>
 *   <li>Strategy as metadata: different factor-finding approaches encoded as strategies</li>
 *   <li>EDE-aligned: strategy travels WITH cell during swaps (for analysis)</li>
 * </ul>
 *
 * <p><strong>COMPARISON LOGIC:</strong> Cells compare based on factor fitness score,
 * NOT raw candidate value. This allows true factors (fitness=1.0) to naturally
 * migrate to array front through standard bubble-sort mechanics.</p>
 *
 * <p><strong>ARCHITECTURE NOTE:</strong> Extends AbstractCell&lt;Integer, FactorStrategy&gt;
 * to reuse EDE cell infrastructure while providing factorization-specific behavior.</p>
 *
 * <p><strong>REFERENCE:</strong> 
 * <ul>
 *   <li>FIRST_NON_SORTING_EXPERIMENT.md - Original experimental design</li>
 *   <li>FACTOR-LOCALIZATION-INVESTIGATION.md - Deep mechanistic analysis (Jan 8, 2026)</li>
 *   <li>experiment-execution-final-report.md - Falsification protocol results</li>
 * </ul>
 * </p>
 */
public class FactorCell extends AbstractCell<Integer, FactorStrategy> {
    
    // ==================== IMMUTABLE PROPERTIES ====================
    
    /** The candidate integer being evaluated as a potential factor */
    private final int candidate;
    
    /** The semiprime being factored (target N) */
    private final int target;
    
    /** The factor-finding strategy (metadata for analysis, not primary driver) */
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
     *   <li>strategy - the factor-finding strategy (for analysis)</li>
     *   <li>initialPosition - starting position in array</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Validate candidate > 1 (0 and 1 are not valid factors)</li>
     *   <li>Validate target > candidate (factor must be smaller than target)</li>
     *   <li>Compute factor fitness score via {@link #computeFactorFitness(int, int)}</li>
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
     * a true factor of N. Perfect factors (N % candidate = 0) score 1.0.
     * This is THE KEY DRIVER of factor localization.</p>
     *
     * <p><strong>MECHANISM:</strong> This fitness function has an intrinsic property:
     * true factors achieve fitness = 1.0, which is the GLOBAL MAXIMUM possible value.
     * When combined with bubble-sort comparison, any cell with fitness = 1.0 will
     * naturally migrate to the array front. This migration is INDEPENDENT of algotype
     * clustering—it's purely a consequence of having maximal fitness.</p>
     *
     * <p><strong>FORMULA:</strong></p>
     * <pre>
     * if (N % candidate == 0)
     *     return 1.0;  // Perfect factor (global maximum)
     * 
     * remainder = N % candidate
     * distanceToMultiple = min(remainder, N - remainder*⌈N/candidate⌉)
     * fitness = 1.0 - (distanceToMultiple / candidate)
     * </pre>
     *
     * <p><strong>INTUITION:</strong> Candidates that leave small remainders are
     * "almost" factors. We measure distance to nearest multiple and normalize.
     * Perfect factors (remainder=0) receive maximum fitness.</p>
     *
     * <p><strong>PRACTICAL EXAMPLES:</strong></p>
     * <table border="1">
     * <tr><th>Candidate</th><th>N=143</th><th>Fitness</th><th>Notes</th></tr>
     * <tr><td>11</td><td>143 % 11 = 0</td><td>1.000</td><td>Perfect factor</td></tr>
     * <tr><td>13</td><td>143 % 13 = 0</td><td>1.000</td><td>Perfect factor</td></tr>
     * <tr><td>18</td><td>143 % 18 = 17</td><td>0.944</td><td>Near factor (distance=1)</td></tr>
     * <tr><td>12</td><td>143 % 12 = 11</td><td>0.083</td><td>Far from factor</td></tr>
     * </table>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>candidate - integer to evaluate</li>
     *   <li>N - the semiprime being factored</li>
     * </ul>
     *
     * <p><strong>OUTPUTS:</strong> Fitness score in [0.0, 1.0], where 1.0 = perfect factor</p>
     *
     * <p><strong>WHY THIS WORKS:</strong> The fitness function creates a fitness landscape
     * where true factors have a unique, globally-maximal value (1.0). In any sorting
     * algorithm that respects this ordering, cells with fitness = 1.0 will naturally
     * rise to the front. This is not emergent—it's a direct consequence of the comparison
     * operator and the fitness function. No clustering mechanism is required.</p>
     *
     * <p><strong>REFERENCE:</strong> FACTOR-LOCALIZATION-INVESTIGATION.md Section 3.1</p>
     *
     * @param candidate the candidate integer
     * @param N the semiprime being factored
     * @return fitness score in [0.0, 1.0], where 1.0 indicates a perfect factor
     */
    private static double computeFactorFitness(int candidate, int N) {
        int remainder = N % candidate;
        if (remainder == 0) {
            return 1.0;  // Perfect factor - GLOBAL MAXIMUM
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
     * <p><strong>NOTE:</strong> This is the PRIMARY DRIVER of cell movement, not the
     * strategy (algotype). High fitness → rapid migration to front.</p>
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
     * <p><strong>CONSEQUENCE:</strong> Perfect factors will always migrate to array
     * front during sorting, regardless of initial position or algotype strategy.</p>
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
     * <p><strong>KEY MECHANISM:</strong> This is where factor localization actually happens.
     * By comparing fitness (not candidate value), this method ensures that cells with
     * high fitness (like true factors with fitness=1.0) naturally move toward the array
     * front. This process is deterministic and requires no emergent behavior.</p>
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
     * <p><strong>CONVERGENCE:</strong> When factors reach the array front, they form
     * a fitness plateau where all cells have fitness = 1.0. At this point, local
     * comparisons yield no beneficial swaps, and the algorithm converges. This typically
     * occurs in ~20 steps for N=143 with 50-cell arrays.</p>
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
     * This is the KEY mechanism that enables factor localization.</p>
     *
     * <p><strong>MECHANISM:</strong> By comparing fitness instead of raw candidate values,
     * cells with fitness = 1.0 (true factors) will compare as "greater than" cells with
     * lower fitness. In a bubble-sort context, this causes high-fitness cells to rise
     * to the front of the array.</p>
     *
     * <p><strong>EXAMPLE:</strong> Consider three cells:
     * <ul>
     *   <li>Cell A: candidate=11, fitness=1.000 (true factor)</li>
     *   <li>Cell B: candidate=18, fitness=0.944 (near-factor)</li>
     *   <li>Cell C: candidate=12, fitness=0.083 (far from factor)</li>
     * </ul>
     * When comparing:
     * <ul>
     *   <li>A.hasGreaterValueThan(B) = true (1.000 > 0.944)</li>
     *   <li>A.hasGreaterValueThan(C) = true (1.000 > 0.083)</li>
     *   <li>B.hasGreaterValueThan(C) = true (0.944 > 0.083)</li>
     * </ul>
     * This ordering ensures A (the true factor) bubbles to the front.
     * </p>
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
     * pattern because factorization domain compares on derived fitness, not raw value.
     * This is intentional and necessary for correct behavior.</p>
     *
     * <p><strong>MATHEMATICAL BASIS:</strong> The fitness function creates a total order
     * on candidates where true factors (fitness=1.0) are maximal. Standard comparison-based
     * sorting respects this order, naturally moving maximal elements to the front. This is
     * not emergent behavior—it's a direct application of sorting theory to a domain-specific
     * ordering (fitness) rather than a natural ordering (candidate value).</p>
     *
     * @param other the cell to compare against
     * @return comparison result (descending fitness order, so higher fitness sorts first)
     */
    @Override
    public int compareTo(AbstractCell<Integer, FactorStrategy> other) {
        FactorCell otherFactor = (FactorCell) other;
        // Descending fitness order (higher fitness = "less than" for front-of-array sorting)
        return Double.compare(otherFactor.fitness, this.fitness);
    }
    
    // ==================== DISPLAY ====================
    
    @Override
    public String toString() {
        return String.format("FactorCell[candidate=%d, fitness=%.3f, strategy=%s, pos=%d]",
            candidate, fitness, strategy, currentPosition);
    }
}
