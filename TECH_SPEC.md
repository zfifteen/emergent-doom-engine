# SAT Clustering Primitive: Technical Specification v1.1

**Version:** 1.1 (Revised per Peer Review)
**Date:** 2026-01-13
**Status:** Implementation Ready
**Repository:** [emergent-doom-engine](https://github.com/zfifteen/emergent-doom-engine)
**Goal:** Demonstrate emergent search-space partitioning for Boolean satisfiability through strategy clustering

***

## Revision Summary (Response to Peer Review)

| Review Issue | Resolution | Section |
| :-- | :-- | :-- |
| TODOs in strategy logic unquantified | Added explicit parameters: WALKSAT p=0.5, HYBRID stagnation=5 steps | §3.2.2 |
| `calculateTargetPositionGiven()` limited to neighbors | Added strategy-specific neighborhood views (FIBONACCI-style for DPLL) | §3.2.2, §3.3 |
| Integer division truncates satisfaction score | Changed to `Math.round((satisfied * 100.0) / totalClauses)` | §3.2.2 |
| DPLL 5% threshold unjustified | Made configurable via `SATStrategyConfig`, default justified | §3.4 |
| Instance generation unspecified | Added DIMACS benchmark + WalkSAT-biased generator | §4.1, Appendix B |
| "Constraint-dense regions" undefined | Defined as variable degree in clause graph, formula provided | §4.3.2 |
| Partial assignments not handled | Clarified: all assignments complete, validation enforced | §3.2.2 |
| Unsatisfiable instances not addressed | Added timeout/failure handling section | §4.5 |
| Missing sample instance | Added complete 3-SAT example in Appendix B | Appendix B |
| MiniSat dependency not documented | Added to dependencies and validation checklist | §5, Appendix A |
| Scalability concerns for 100+ vars | Added smaller pilot instance (20 vars, 80 clauses) | §4.1.2 |
| Cluster boundary visualization missing | Added ASCII visualization example | §4.3.3 |


***

## 1. Executive Summary

This specification defines a **SAT constraint satisfaction domain** for the Emergent Doom Engine, implementing Hypothesis 1 from the clustering primitive brainstorm. The domain maps Boolean satisfiability problems to the EDE cell-based architecture, enabling empirical measurement of emergent search-space partitioning through algotype clustering.[^1][^2]

**Core Hypothesis:** Different variable assignment strategies (DPLL, greedy, random walk) exhibit varying effectiveness across constraint landscape regions. Algotype clustering will emergently reveal these strategy-compatibility zones, partitioning the search space without explicit constraint analysis.

**Expected Signature:** Peak aggregation 65-72% at mid-search (analogous to Bubble-Selection 72% at 42% progress in sorting), with cluster boundaries localizing at constraint-dense decision points.[^3]

**Computational Primitive:** Clustering boundaries identify where to fork parallel search threads or switch strategies, converting homogeneous search into adaptively partitioned, heterogeneous search with minimal overhead.

***

## 2. Domain Mapping

### 2.1 SAT to EDE Translation

| EDE Concept | SAT Mapping | Implementation |
| :-- | :-- | :-- |
| **Cell** | Variable assignment candidate | `SATCell` extends `AbstractCell<Integer, SATStrategy>` |
| **Cell.value** | Satisfaction score (% clauses satisfied) | Integer in |
| **Cell.algotype** | Assignment strategy class | `SATStrategy` enum |
| **"Sorted" state** | Candidates ordered by satisfaction (descending) | Score 100 = solution found |
| **Aggregation** | Spatial clustering of strategy classes | `AlgotypeAggregationIndex` from EDE[^4] |
| **Convergence** | Solution found OR timeout | Satisfaction = 100% or max steps exceeded |

### 2.2 Fitness Function: Satisfaction Score

**Purpose:** Measure closeness to satisfying the CNF formula. Perfect assignments (all clauses true) score 100.

**Formula:**

```
satisfactionScore = round((clausesSatisfied / totalClauses) × 100)

where clausesSatisfied = Σ evaluate(clause, assignment)
```

**Implementation Note (per review):** Use floating-point division with rounding to avoid truncation:

```java
return (int) Math.round((satisfied * 100.0) / totalClauses);
```

**Comparison Semantics:** Cells compare by satisfaction score (higher = better). Unlike factorization where `fitness=1.0` is globally maximal, SAT can have **multiple cells at 100%** (different satisfying assignments), enabling study of solution-dense vs solution-sparse regions.

### 2.3 Assignment Completeness Invariant

**Invariant:** All `SATCell` instances MUST have complete assignments (all formula variables assigned).

**Enforcement:**

```java
// In SATCell constructor
if (!assignment.keySet().equals(formula.getVariables())) {
    Set<String> missing = new HashSet<>(formula.getVariables());
    missing.removeAll(assignment.keySet());
    throw new IllegalArgumentException("Incomplete assignment, missing: " + missing);
}
```

**Rationale (per review):** Partial assignments complicate satisfaction scoring and comparison semantics. Strategy-specific candidate generation MUST produce complete assignments.

***

## 3. Implementation Architecture

### 3.1 Package Structure

```
src/main/java/com/emergent/doom/sat/
├── SATStrategy.java              # Algotype enum with strategy parameters
├── SATStrategyConfig.java        # Configurable parameters (NEW)
├── SATCell.java                  # Cell implementation
├── SATCellFactory.java           # Factory with strategy distribution
├── CNFFormula.java               # SAT instance representation
├── CNFClause.java                # Single clause (extracted class)
├── AssignmentGenerator.java      # Strategy-specific candidate generation
├── ConstraintDensityAnalyzer.java # Variable degree analysis (NEW)
├── SATExperiment.java            # Experiment runner
├── SATMetrics.java               # Domain-specific metrics
├── SATInstanceGenerator.java     # 3-SAT instance generation (NEW)
└── package-info.java             # Documentation
```


### 3.2 Core Components

#### 3.2.1 SATStrategy Enum

```java
package com.emergent.doom.sat;

/**
 * Algotype enumeration for Boolean satisfiability domain.
 *
 * <p><strong>PURPOSE:</strong> Define assignment strategies enabling emergent
 * clustering based on strategy compatibility in constraint landscape.</p>
 *
 * <p><strong>PARAMETERS:</strong> Each strategy has configurable parameters
 * defined in {@link SATStrategyConfig}. Defaults are empirically justified.</p>
 *
 * <p><strong>REFERENCE:</strong> Levin et al. 2024 (arXiv:2401.05375v1)</p>
 */
public enum SATStrategy {
    
    /**
     * DPLL systematic search strategy.
     *
     * <p><strong>INTUITION:</strong> Unit propagation and pure literal elimination.
     * Systematic exploration with logical inference.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Extended Fibonacci-style viewing
     * (positions 1, 2, 3, 5, 8 away) for branching awareness.</p>
     *
     * <p><strong>SWAP THRESHOLD:</strong> Conservative, only moves for >threshold%
     * improvement (default 5%, configurable via {@link SATStrategyConfig#DPLL_SWAP_THRESHOLD}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Clusters in constraint-dense regions
     * where unit propagation provides strong guidance.</p>
     */
    DPLL("DPLL systematic search with unit propagation", 5, true),
    
    /**
     * Greedy most-constrained-variable heuristic.
     *
     * <p><strong>INTUITION:</strong> Assign variable appearing in most unsatisfied
     * clauses. Greedy local optimization.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Immediate neighbors only (positions ±1).</p>
     *
     * <p><strong>SWAP THRESHOLD:</strong> Any improvement triggers swap (threshold 0%).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Clusters in regions with clear
     * variable dominance (skewed constraint distribution).</p>
     */
    GREEDY_MCV("Greedy most-constrained-variable heuristic", 0, false),
    
    /**
     * WalkSAT-inspired random walk with noise.
     *
     * <p><strong>INTUITION:</strong> Random variable flipping with bias toward
     * unsatisfied clauses. Escapes local optima through noise.</p>
     *
     * <p><strong>NEIGHBORHOOD VIEW:</strong> Immediate neighbors only.</p>
     *
     * <p><strong>NOISE PARAMETER:</strong> p=0.5 probability of random flip vs
     * greedy flip (configurable via {@link SATStrategyConfig#WALKSAT_NOISE}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Baseline clustering due to random
     * component; should show ~50-55% aggregation.</p>
     */
    WALKSAT("WalkSAT random walk with noise p=0.5", 0, false),
    
    /**
     * Hybrid adaptive strategy switching.
     *
     * <p><strong>INTUITION:</strong> Switch between DPLL and WALKSAT based on
     * progress rate.</p>
     *
     * <p><strong>SWITCHING LOGIC:</strong></p>
     * <ul>
     *   <li>Start with DPLL behavior</li>
     *   <li>Switch to WALKSAT if no improvement for 5 consecutive steps</li>
     *   <li>Revert to DPLL when improvement resumes</li>
     * </ul>
     *
     * <p><strong>STAGNATION THRESHOLD:</strong> 5 steps (configurable via
     * {@link SATStrategyConfig#HYBRID_STAGNATION_THRESHOLD}).</p>
     *
     * <p><strong>CLUSTERING HYPOTHESIS:</strong> Dynamic clustering patterns
     * as strategy switches align with landscape structure.</p>
     */
    HYBRID("Hybrid adaptive DPLL/WalkSAT, stagnation=5", 0, true);
    
    private final String description;
    private final int defaultSwapThreshold;
    private final boolean usesExtendedNeighborhood;
    
    SATStrategy(String description, int defaultSwapThreshold, boolean usesExtendedNeighborhood) {
        this.description = description;
        this.defaultSwapThreshold = defaultSwapThreshold;
        this.usesExtendedNeighborhood = usesExtendedNeighborhood;
    }
    
    public String getDescription() { return description; }
    public int getDefaultSwapThreshold() { return defaultSwapThreshold; }
    public boolean usesExtendedNeighborhood() { return usesExtendedNeighborhood; }
    
    @Override
    public String toString() {
        return name() + ": " + description;
    }
}
```


#### 3.2.2 SATCell Implementation (Complete)

```java
package com.emergent.doom.sat;

import com.emergent.doom.cell.AbstractCell;
import com.emergent.doom.cell.NeighborhoodView;
import com.emergent.doom.group.CellStatus;

import java.util.*;

/**
 * Cell implementation for Boolean satisfiability domain.
 *
 * <p><strong>PURPOSE:</strong> Represent a variable assignment candidate with
 * associated search strategy, enabling emergent partitioning of constraint
 * landscape through strategy clustering.</p>
 *
 * <p><strong>INVARIANT:</strong> All assignments are COMPLETE (all formula
 * variables assigned). Enforced at construction time.</p>
 *
 * <p><strong>COMPARISON SEMANTICS:</strong> Cells compare by satisfaction score
 * (higher = better). Uses floating-point division with rounding to avoid
 * truncation issues (per peer review).</p>
 */
public class SATCell extends AbstractCell<Integer, SATStrategy> {
    
    // ==================== IMMUTABLE PROPERTIES ====================
    
    /** Variable assignment (Map from variable name to boolean value) */
    private final Map<String, Boolean> assignment;
    
    /** The CNF formula being satisfied */
    private final CNFFormula formula;
    
    /** The assignment strategy */
    private final SATStrategy strategy;
    
    /** Strategy configuration parameters */
    private final SATStrategyConfig config;
    
    /** Pre-computed satisfaction score (0-100) */
    private final int satisfactionScore;
    
    // ==================== MUTABLE STATE ====================
    
    /** Current position in array */
    private int currentPosition;
    
    /** Execution status */
    private CellStatus status;
    
    /** Steps since last improvement (for HYBRID stagnation detection) */
    private int stepsSinceImprovement;
    
    /** Last recorded satisfaction score (for HYBRID) */
    private int lastSatisfactionScore;
    
    /**
     * Create a new SATCell with default configuration.
     */
    public SATCell(Map<String, Boolean> assignment, CNFFormula formula,
                   SATStrategy strategy, int initialPosition) {
        this(assignment, formula, strategy, initialPosition, SATStrategyConfig.defaults());
    }
    
    /**
     * Create a new SATCell with custom configuration.
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>assignment - complete variable assignments (immutable map)</li>
     *   <li>formula - CNF formula being satisfied</li>
     *   <li>strategy - assignment strategy</li>
     *   <li>initialPosition - starting array position</li>
     *   <li>config - strategy configuration parameters</li>
     * </ul>
     *
     * <p><strong>VALIDATION:</strong></p>
     * <ol>
     *   <li>Assignment must cover ALL formula variables (completeness invariant)</li>
     *   <li>Assignment must not contain extra variables</li>
     *   <li>Position must be non-negative</li>
     * </ol>
     *
     * @throws IllegalArgumentException if assignment incomplete or invalid
     */
    public SATCell(Map<String, Boolean> assignment, CNFFormula formula,
                   SATStrategy strategy, int initialPosition, SATStrategyConfig config) {
        // Validate assignment completeness (per review: strict enforcement)
        if (!assignment.keySet().equals(formula.getVariables())) {
            Set<String> missing = new HashSet<>(formula.getVariables());
            missing.removeAll(assignment.keySet());
            Set<String> extra = new HashSet<>(assignment.keySet());
            extra.removeAll(formula.getVariables());
            throw new IllegalArgumentException(String.format(
                "Assignment mismatch: missing=%s, extra=%s", missing, extra));
        }
        
        if (initialPosition < 0) {
            throw new IllegalArgumentException("Position cannot be negative: " + initialPosition);
        }
        
        this.assignment = Map.copyOf(assignment); // Immutable copy
        this.formula = formula;
        this.strategy = strategy;
        this.config = config;
        this.currentPosition = initialPosition;
        this.status = CellStatus.ACTIVE;
        this.stepsSinceImprovement = 0;
        
        // Pre-compute satisfaction score with proper rounding (per review)
        this.satisfactionScore = computeSatisfactionScore(assignment, formula);
        this.lastSatisfactionScore = this.satisfactionScore;
    }
    
    /**
     * Compute satisfaction score with proper rounding.
     *
     * <p><strong>FORMULA:</strong></p>
     * <pre>
     * satisfactionScore = round((clausesSatisfied / totalClauses) × 100)
     * </pre>
     *
     * <p><strong>IMPLEMENTATION NOTE (per review):</strong> Uses floating-point
     * division with Math.round() to avoid truncation (e.g., 1/3 = 33% not 0%).</p>
     *
     * @param assignment variable assignments
     * @param formula CNF formula to evaluate
     * @return Integer percentage in [0, 100]
     */
    private static int computeSatisfactionScore(Map<String, Boolean> assignment,
                                                 CNFFormula formula) {
        int totalClauses = formula.getClauseCount();
        if (totalClauses == 0) {
            return 100; // Empty formula is trivially satisfied
        }
        
        int satisfied = 0;
        for (CNFClause clause : formula.getClauses()) {
            if (clause.evaluate(assignment)) {
                satisfied++;
            }
        }
        
        // Floating-point division with rounding (per review fix)
        return (int) Math.round((satisfied * 100.0) / totalClauses);
    }
    
    // ==================== INTRINSIC PROPERTIES ====================
    
    @Override
    public SATStrategy readAlgotype() {
        return strategy;
    }
    
    @Override
    public Integer readValue() {
        return satisfactionScore;
    }
    
    public Map<String, Boolean> getAssignment() {
        return assignment;
    }
    
    public boolean isSolution() {
        return satisfactionScore == 100;
    }
    
    public SATStrategyConfig getConfig() {
        return config;
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
    
    /**
     * Update stagnation tracking (called by execution engine after each step).
     *
     * <p><strong>PURPOSE:</strong> Track steps without improvement for HYBRID
     * strategy switching.</p>
     */
    public void updateStagnationTracking(int currentScore) {
        if (currentScore > lastSatisfactionScore) {
            stepsSinceImprovement = 0;
        } else {
            stepsSinceImprovement++;
        }
        lastSatisfactionScore = currentScore;
    }
    
    public int getStepsSinceImprovement() {
        return stepsSinceImprovement;
    }
    
    // ==================== BEHAVIORAL POLICY ====================
    
    /**
     * Strategy-specific movement policy with quantified parameters.
     *
     * <p><strong>PARAMETERS (per review resolution):</strong></p>
     * <ul>
     *   <li>DPLL: threshold = config.getDpllSwapThreshold() (default 5%)</li>
     *   <li>GREEDY_MCV: threshold = 0 (any improvement)</li>
     *   <li>WALKSAT: noise p = config.getWalksatNoise() (default 0.5)</li>
     *   <li>HYBRID: stagnation = config.getHybridStagnationThreshold() (default 5)</li>
     * </ul>
     */
    @Override
    public boolean shouldMoveGiven(NeighborhoodView<Integer, SATStrategy> neighbors) {
        if (status != CellStatus.ACTIVE) {
            return false;
        }
        
        return switch(strategy) {
            case DPLL -> dpllShouldMove(neighbors);
            case GREEDY_MCV -> greedyShouldMove(neighbors);
            case WALKSAT -> walkSatShouldMove(neighbors);
            case HYBRID -> hybridShouldMove(neighbors);
        };
    }
    
    /**
     * Calculate target position with strategy-specific neighborhood awareness.
     *
     * <p><strong>NEIGHBORHOOD VIEWS (per review enhancement):</strong></p>
     * <ul>
     *   <li>DPLL/HYBRID: Extended Fibonacci-style (1, 2, 3, 5, 8 positions)</li>
     *   <li>GREEDY_MCV/WALKSAT: Immediate neighbors only (±1)</li>
     * </ul>
     */
    @Override
    public Optional<Integer> calculateTargetPositionGiven(
            NeighborhoodView<Integer, SATStrategy> neighbors) {
        
        if (strategy.usesExtendedNeighborhood()) {
            return calculateExtendedTarget(neighbors);
        } else {
            return calculateImmediateTarget(neighbors);
        }
    }
    
    /**
     * Extended target calculation for DPLL/HYBRID.
     * Uses Fibonacci-style lookahead for branching awareness.
     */
    private Optional<Integer> calculateExtendedTarget(
            NeighborhoodView<Integer, SATStrategy> neighbors) {
        
        // Check extended neighbors in Fibonacci sequence: 1, 2, 3, 5, 8
        int[] fibDistances = {1, 2, 3, 5, 8};
        int bestTarget = -1;
        int bestImprovement = 0;
        
        for (int distance : fibDistances) {
            // Check left at distance
            Optional<AbstractCell<Integer, SATStrategy>> leftOpt = 
                neighbors.getNeighborAtDistance(-distance);
            if (leftOpt.isPresent()) {
                SATCell left = (SATCell) leftOpt.get();
                int improvement = this.satisfactionScore - left.satisfactionScore;
                if (improvement > bestImprovement) {
                    bestImprovement = improvement;
                    bestTarget = left.readCurrentPosition();
                }
            }
        }
        
        // Only return if improvement exceeds strategy threshold
        int threshold = config.getDpllSwapThreshold();
        if (bestImprovement > threshold) {
            return Optional.of(bestTarget);
        }
        
        // Fall back to immediate neighbor check
        return calculateImmediateTarget(neighbors);
    }
    
    /**
     * Immediate target calculation for GREEDY/WALKSAT.
     * Standard bubble-sort neighbor comparison.
     */
    private Optional<Integer> calculateImmediateTarget(
            NeighborhoodView<Integer, SATStrategy> neighbors) {
        
        // Check left neighbor: move left if this cell has higher score
        Optional<AbstractCell<Integer, SATStrategy>> leftOpt = neighbors.getLeftNeighbor();
        if (leftOpt.isPresent()) {
            SATCell left = (SATCell) leftOpt.get();
            if (this.satisfactionScore > left.satisfactionScore) {
                return Optional.of(left.readCurrentPosition());
            }
        }
        
        // Check right neighbor: move right if right has higher score
        Optional<AbstractCell<Integer, SATStrategy>> rightOpt = neighbors.getRightNeighbor();
        if (rightOpt.isPresent()) {
            SATCell right = (SATCell) rightOpt.get();
            if (this.satisfactionScore < right.satisfactionScore) {
                return Optional.of(right.readCurrentPosition());
            }
        }
        
        return Optional.empty();
    }
    
    // ==================== STRATEGY-SPECIFIC LOGIC (Quantified per Review) ====================
    
    /**
     * DPLL movement policy: conservative, requires significant improvement.
     *
     * <p><strong>THRESHOLD JUSTIFICATION (per review):</strong> 5% threshold
     * derived from sorting experiments where conservative strategies showed
     * stronger clustering. Configurable for sensitivity analysis.</p>
     */
    private boolean dpllShouldMove(NeighborhoodView<Integer, SATStrategy> neighbors) {
        int threshold = config.getDpllSwapThreshold();
        return neighbors.getLeftNeighbor()
            .map(left -> this.satisfactionScore - ((SATCell)left).satisfactionScore > threshold)
            .orElse(false);
    }
    
    /**
     * GREEDY movement policy: any improvement triggers movement.
     */
    private boolean greedyShouldMove(NeighborhoodView<Integer, SATStrategy> neighbors) {
        return neighbors.getLeftNeighbor()
            .map(left -> this.satisfactionScore > ((SATCell)left).satisfactionScore)
            .orElse(false);
    }
    
    /**
     * WALKSAT movement policy: probabilistic with noise parameter.
     *
     * <p><strong>NOISE PARAMETER (per review):</strong> p = 0.5 (default).
     * With probability p, move randomly; with probability 1-p, move greedily.</p>
     */
    private boolean walkSatShouldMove(NeighborhoodView<Integer, SATStrategy> neighbors) {
        double noise = config.getWalksatNoise();
        
        if (Math.random() < noise) {
            // Random component: move with 50% probability regardless of improvement
            return Math.random() > 0.5;
        } else {
            // Greedy component: move only if improvement exists
            return greedyShouldMove(neighbors);
        }
    }
    
    /**
     * HYBRID movement policy: adaptive switching based on stagnation.
     *
     * <p><strong>SWITCHING LOGIC (per review):</strong></p>
     * <ul>
     *   <li>If stagnation &lt; threshold: use DPLL logic</li>
     *   <li>If stagnation &gt;= threshold: use WALKSAT logic</li>
     * </ul>
     */
    private boolean hybridShouldMove(NeighborhoodView<Integer, SATStrategy> neighbors) {
        int stagnationThreshold = config.getHybridStagnationThreshold();
        
        if (stepsSinceImprovement >= stagnationThreshold) {
            // Stagnating: switch to WALKSAT exploratory behavior
            return walkSatShouldMove(neighbors);
        } else {
            // Making progress: use conservative DPLL behavior
            return dpllShouldMove(neighbors);
        }
    }
    
    // ==================== SWAP ELIGIBILITY ====================
    
    @Override
    public boolean canInitiateSwap() {
        return status == CellStatus.ACTIVE;
    }
    
    @Override
    public boolean canAcceptSwapFrom(AbstractCell<Integer, SATStrategy> initiator) {
        return status == CellStatus.ACTIVE || status == CellStatus.FREEZE;
    }
    
    // ==================== VALUE COMPARISON ====================
    
    @Override
    public boolean hasGreaterValueThan(AbstractCell<Integer, SATStrategy> other) {
        if (other instanceof SATCell otherSAT) {
            return this.satisfactionScore > otherSAT.satisfactionScore;
        }
        return false;
    }
    
    @Override
    public int compareTo(AbstractCell<Integer, SATStrategy> other) {
        if (other instanceof SATCell otherSAT) {
            // Descending satisfaction order (higher score sorts first)
            return Integer.compare(otherSAT.satisfactionScore, this.satisfactionScore);
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return String.format("SATCell[score=%d%%, strategy=%s, pos=%d, stagnation=%d]",
            satisfactionScore, strategy, currentPosition, stepsSinceImprovement);
    }
}
```


### 3.3 Extended Neighborhood View

**Purpose:** Support strategy-specific neighborhood sizes (per review: DPLL needs lookahead).

**Addition to NeighborhoodView interface:**

```java
/**
 * Get neighbor at specified signed distance.
 *
 * <p><strong>PURPOSE:</strong> Support extended neighborhood views for strategies
 * like DPLL that benefit from Fibonacci-style lookahead.</p>
 *
 * @param distance signed distance (negative = left, positive = right)
 * @return Optional containing neighbor cell, or empty if out of bounds
 */
default Optional<AbstractCell<V, A>> getNeighborAtDistance(int distance) {
    int targetPosition = getCurrentPosition() + distance;
    if (targetPosition < 0 || targetPosition >= getArraySize()) {
        return Optional.empty();
    }
    return getCellAtPosition(targetPosition);
}
```


### 3.4 Strategy Configuration

**Purpose:** Make strategy parameters configurable (per review).

```java
package com.emergent.doom.sat;

/**
 * Configuration parameters for SAT strategy behaviors.
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
public record SATStrategyConfig(
    int dpllSwapThreshold,      // Minimum improvement % for DPLL swap
    double walksatNoise,        // Probability of random vs greedy move
    int hybridStagnationThreshold, // Steps before HYBRID switches to WALKSAT
    long randomSeed             // For reproducibility
) {
    /**
     * Default configuration with empirically-justified values.
     */
    public static SATStrategyConfig defaults() {
        return new SATStrategyConfig(5, 0.5, 5, 42L);
    }
    
    /**
     * Builder for custom configurations.
     */
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
            return new SATStrategyConfig(
                dpllSwapThreshold, walksatNoise, hybridStagnationThreshold, randomSeed);
        }
    }
}
```


***

## 4. Experimental Design

### 4.1 Test Instances

#### 4.1.1 Primary Instance: 3-SAT at Phase Transition

**Parameters:**

- Variables: 50
- Clauses: 200
- Clause size: 3 literals per clause
- Clause/variable ratio: 4.0 (near phase transition 4.27)
- Satisfiability: Verified with MiniSat

**Generation Method (per review):**

```java
/**
 * Generate satisfiable 3-SAT instance using WalkSAT-biased construction.
 *
 * <p><strong>METHOD:</strong></p>
 * <ol>
 *   <li>Generate random assignment (the "planted" solution)</li>
 *   <li>Generate random clauses</li>
 *   <li>For each clause, ensure at least one literal matches planted solution</li>
 *   <li>This guarantees satisfiability while maintaining random structure</li>
 * </ol>
 */
public static CNFFormula generateSatisfiable3SAT(int numVars, int numClauses, long seed) {
    Random rng = new Random(seed);
    
    // Generate planted solution
    Map<String, Boolean> plantedSolution = new HashMap<>();
    for (int i = 1; i <= numVars; i++) {
        plantedSolution.put("x" + i, rng.nextBoolean());
    }
    
    List<CNFClause> clauses = new ArrayList<>();
    List<String> varNames = new ArrayList<>(plantedSolution.keySet());
    
    for (int c = 0; c < numClauses; c++) {
        // Pick 3 distinct variables
        Collections.shuffle(varNames, rng);
        Map<String, Boolean> literals = new HashMap<>();
        
        for (int i = 0; i < 3; i++) {
            String var = varNames.get(i);
            boolean polarity = rng.nextBoolean();
            literals.put(var, polarity);
        }
        
        // Ensure at least one literal satisfies planted solution
        boolean satisfied = literals.entrySet().stream()
            .anyMatch(e -> e.getValue() == plantedSolution.get(e.getKey()));
        
        if (!satisfied) {
            // Flip one literal to match planted solution
            String firstVar = varNames.get(0);
            literals.put(firstVar, plantedSolution.get(firstVar));
        }
        
        clauses.add(new CNFClause(literals));
    }
    
    return new CNFFormula(clauses);
}
```


#### 4.1.2 Pilot Instance: Smaller Scale (per review)

**Purpose:** Verify infrastructure before full-scale experiments; test scalability.

**Parameters:**

- Variables: 20
- Clauses: 80
- Ratio: 4.0
- Array size: 40 candidates

**Validation:** Run 10 trials to confirm metrics work before scaling up.

### 4.2 Chimeric Configuration

**Array Setup:**

- **Array size:** 100 assignment candidates
- **Strategy distribution:**
    - 30% `DPLL` cells (30 cells)
    - 30% `GREEDY_MCV` cells (30 cells)
    - 40% `WALKSAT` cells (40 cells)

**Control Configurations:**


| Control | Configuration | Expected Aggregation | Purpose |
| :-- | :-- | :-- | :-- |
| **Negative** | 100% DPLL (same strategy) | 50-60% baseline | Establish non-emergent baseline |
| **Positive** | Bubble-Selection sorting (from EDE) | 72% at 42% progress | Verify measurement infrastructure |
| **Homogeneous** | 100% GREEDY_MCV | Traditional solver baseline | Measure clustering overhead |

**Positive Control Specification (per review):**

```java
// Exact configuration matching Levin et al. results
Map<SortingAlgotype, Double> controlDistribution = Map.of(
    SortingAlgotype.BUBBLE, 0.5,
    SortingAlgotype.SELECTION, 0.5
);
// Array size: 100, max value: 1000
// Expected: 72% peak aggregation at 42% progress (steps 840 of 2000)
```


### 4.3 Metrics

#### 4.3.1 Primary Metrics

| Metric | Implementation | Success Criterion |
| :-- | :-- | :-- |
| **Peak Aggregation** | `AlgotypeAggregationIndex.compute()` | > 60% (p < 0.05 vs control) |
| **Aggregation Trajectory** | Record at each step | Clear rise-peak-fall pattern |
| **Peak Timing** | Step number at max aggregation | 30-50% of total steps (analogous to sorting) |

#### 4.3.2 Constraint Density Measurement (per review)

**Definition:** Constraint density for a variable = number of clauses containing that variable.

**Formula:**

```
constraintDensity(var) = |{clause ∈ formula : var ∈ clause}|
normalizedDensity(var) = constraintDensity(var) / totalClauses
```

**Implementation:**

```java
package com.emergent.doom.sat;

import java.util.*;

/**
 * Analyze constraint density in CNF formula.
 *
 * <p><strong>PURPOSE:</strong> Measure variable degree in clause graph for
 * cluster boundary analysis.</p>
 */
public class ConstraintDensityAnalyzer {
    
    private final CNFFormula formula;
    private final Map<String, Integer> variableDegrees;
    
    public ConstraintDensityAnalyzer(CNFFormula formula) {
        this.formula = formula;
        this.variableDegrees = computeVariableDegrees(formula);
    }
    
    private static Map<String, Integer> computeVariableDegrees(CNFFormula formula) {
        Map<String, Integer> degrees = new HashMap<>();
        for (String var : formula.getVariables()) {
            degrees.put(var, 0);
        }
        
        for (CNFClause clause : formula.getClauses()) {
            for (String var : clause.getVariables()) {
                degrees.merge(var, 1, Integer::sum);
            }
        }
        
        return degrees;
    }
    
    /**
     * Get normalized density for variable (0.0 to 1.0).
     */
    public double getNormalizedDensity(String variable) {
        int degree = variableDegrees.getOrDefault(variable, 0);
        return (double) degree / formula.getClauseCount();
    }
    
    /**
     * Get top N most constrained variables.
     */
    public List<String> getMostConstrainedVariables(int n) {
        return variableDegrees.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(n)
            .map(Map.Entry::getKey)
            .toList();
    }
    
    /**
     * Check if position is at constraint-dense boundary.
     *
     * <p><strong>DEFINITION:</strong> Position is at boundary if the dominant
     * variables in cells at that position are in top 30% most constrained.</p>
     */
    public boolean isAtConstraintDenseBoundary(List<SATCell> cells, int position,
                                                List<Integer> clusterBoundaries) {
        if (!clusterBoundaries.contains(position)) {
            return false;
        }
        
        // Get variables most relevant at this position
        SATCell cell = cells.get(position);
        Set<String> relevantVars = getUnsatisfiedVariables(cell);
        
        // Check if these are highly constrained
        List<String> topConstrained = getMostConstrainedVariables(
            (int) (formula.getVariableCount() * 0.3));
        
        long overlap = relevantVars.stream()
            .filter(topConstrained::contains)
            .count();
        
        return overlap > relevantVars.size() * 0.5;
    }
    
    private Set<String> getUnsatisfiedVariables(SATCell cell) {
        Set<String> unsatisfied = new HashSet<>();
        for (CNFClause clause : formula.getClauses()) {
            if (!clause.evaluate(cell.getAssignment())) {
                unsatisfied.addAll(clause.getVariables());
            }
        }
        return unsatisfied;
    }
}
```


#### 4.3.3 Cluster Boundary Visualization (per review)

**ASCII Visualization Example:**

```
Step 500 (50% progress), Aggregation: 68%

Position:  0    10   20   30   40   50   60   70   80   90   100
           |----|----|----|----|----|----|----|----|----|----|
Strategy:  DDDDDDDDDDGGGGGGGGGGGGGWWWWWWWWWWWWWWWWWWWWWWWWWDDDDDD
Boundary:           ^           ^                            ^
Score:     [98%----][75%-------][62%------------------------][91%]

Legend: D=DPLL, G=GREEDY_MCV, W=WALKSAT
Boundaries at positions: 10, 22, 86
Constraint density at boundaries: 0.72, 0.68, 0.81 (top 30% threshold: 0.65)
```

**Programmatic Visualization:**

```java
public static String visualizeClustering(List<SATCell> cells, 
                                          List<Integer> boundaries) {
    StringBuilder sb = new StringBuilder();
    sb.append("Position: ");
    for (int i = 0; i < cells.size(); i += 10) {
        sb.append(String.format("%-10d", i));
    }
    sb.append("\nStrategy: ");
    
    for (SATCell cell : cells) {
        char symbol = switch(cell.readAlgotype()) {
            case DPLL -> 'D';
            case GREEDY_MCV -> 'G';
            case WALKSAT -> 'W';
            case HYBRID -> 'H';
        };
        sb.append(symbol);
    }
    
    sb.append("\nBoundary: ");
    for (int i = 0; i < cells.size(); i++) {
        sb.append(boundaries.contains(i) ? '^' : ' ');
    }
    
    return sb.toString();
}
```


### 4.4 Experimental Protocol

**Procedure:**

1. **Setup Phase:**
    - Generate 3-SAT instance using `SATInstanceGenerator.generateSatisfiable3SAT(50, 200, seed)`
    - Verify satisfiability with MiniSat (must return SAT)
    - Create 100 initial candidates via `AssignmentGenerator`
    - Distribute strategies: 30/30/40 DPLL/GREEDY/WALKSAT
2. **Execution Phase:**
    - Initialize `CellBasedExecutionEngine`
    - Run until convergence (satisfaction = 100%) OR timeout (10,000 steps)
    - Record at each step:
        - Aggregation index
        - Satisfaction score distribution (min, max, mean)
        - Cluster boundary positions
        - Stagnation counts (for HYBRID cells)
3. **Analysis Phase:**
    - Compute peak aggregation and timing
    - Extract cluster boundaries using algorithm from[^2]
    - Map strategy dominance regions
    - Measure constraint density at boundaries
    - Generate visualization
4. **Statistical Validation:**
    - Run 100 trials per configuration
    - Compute means and standard deviations
    - Perform Welch's t-test against control (p < 0.05)
    - Compute Cohen's d effect size (target: d > 0.5, medium effect)

### 4.5 Timeout and Failure Handling (per review)

**Timeout Conditions:**

- **Hard timeout:** 10,000 steps without solution
- **Stagnation timeout:** 500 consecutive steps with no improvement in best satisfaction score

**Failure Handling:**

```java
public enum ExperimentOutcome {
    SOLUTION_FOUND,      // Satisfaction reached 100%
    TIMEOUT_STEPS,       // Hard timeout (10,000 steps)
    TIMEOUT_STAGNATION,  // No improvement for 500 steps
    ERROR                // Unexpected error (log and continue)
}

public class ExperimentResult {
    private final ExperimentOutcome outcome;
    private final int stepsCompleted;
    private final int finalBestScore;
    private final double peakAggregation;
    private final int peakAggregationStep;
    private final List<Integer> finalBoundaries;
    
    // ... accessors
}
```

**Unsatisfiable Instance Protection:**

- All generated instances use planted-solution method (guaranteed satisfiable)
- If external DIMACS instance used, validate with MiniSat before experiment
- Log and skip unsatisfiable instances (should not occur with proper generation)

***

## 5. Dependencies and Integration

### 5.1 External Dependencies (per review)

| Dependency | Version | Purpose | Integration |
| :-- | :-- | :-- | :-- |
| **MiniSat** | 2.2.0+ | Instance validation | Command-line invocation for verification |
| **JUnit 5** | 5.9+ | Unit testing | Already in EDE |
| **Maven** | 3.6+ | Build | Already in EDE |

**MiniSat Integration:**

```java
public class MiniSatValidator {
    
    private static final String MINISAT_PATH = System.getenv("MINISAT_PATH");
    
    /**
     * Validate formula satisfiability using MiniSat.
     *
     * @param formula CNF formula to validate
     * @return true if satisfiable, false otherwise
     * @throws IOException if MiniSat invocation fails
     */
    public static boolean isSatisfiable(CNFFormula formula) throws IOException {
        // Write formula to temp file in DIMACS format
        Path tempFile = Files.createTempFile("sat_", ".cnf");
        writeDIMACS(formula, tempFile);
        
        // Invoke MiniSat
        ProcessBuilder pb = new ProcessBuilder(MINISAT_PATH, tempFile.toString());
        Process process = pb.start();
        
        try {
            int exitCode = process.waitFor();
            // MiniSat exit codes: 10 = SAT, 20 = UNSAT
            return exitCode == 10;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("MiniSat interrupted", e);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    private static void writeDIMACS(CNFFormula formula, Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(String.format("p cnf %d %d%n", 
                formula.getVariableCount(), formula.getClauseCount()));
            
            // Map variable names to integers
            Map<String, Integer> varMap = new HashMap<>();
            int idx = 1;
            for (String var : formula.getVariables()) {
                varMap.put(var, idx++);
            }
            
            for (CNFClause clause : formula.getClauses()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Boolean> lit : clause.getLiterals().entrySet()) {
                    int varNum = varMap.get(lit.getKey());
                    if (!lit.getValue()) varNum = -varNum;
                    sb.append(varNum).append(" ");
                }
                sb.append("0");
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }
}
```


### 5.2 EDE Integration

**No modifications required to existing components:**

- `AbstractCell` interface
- `CellBasedExecutionEngine`
- `AlgotypeAggregationIndex`
- `NeighborhoodView` (minor extension for `getNeighborAtDistance`)

**New components isolated in `sat` package:**

- Follows existing `factorization` package pattern
- Compatible with batch execution framework
- Reuses probe system for trajectory recording

***

## 6. Success Criteria

### 6.1 Primary Success (Hypothesis Supported)

- [ ] **Peak aggregation significantly exceeds control** (p < 0.05, Cohen's d > 0.5)
    - Chimeric: 65-72% peak aggregation
    - Control: 50-60% baseline
- [ ] **Cluster boundaries localize at constraint-dense regions**
    - >50% of boundaries at top 30% most-constrained variables
    - Measured via `ConstraintDensityAnalyzer`
- [ ] **Strategy dominance patterns reproducible**
    - >80% agreement on dominant strategy per region across runs
    - DPLL dominates high-constraint regions, WALKSAT in sparse regions
- [ ] **Computational gain from clustering** (optional success)
    - Using cluster boundaries to guide strategy switching reduces steps by >15%


### 6.2 Partial Success

- Aggregation exceeds control but boundaries not localized → clustering captures strategy compatibility but not constraint structure
- Boundaries localized but no computational gain → clustering reveals structure without improving efficiency


### 6.3 Null Result

- Aggregation indistinguishable from control (p > 0.05) → SAT constraint structure not captured by this encoding
- **Next steps:** Try clause-centric encoding (alternative from v1.0 spec)

***

## 7. Implementation Roadmap

### Phase 1: Core Infrastructure (3 days)

- [ ] Create `src/main/java/com/emergent/doom/sat/` package
- [ ] Implement `SATStrategy.java` enum
- [ ] Implement `SATStrategyConfig.java` record
- [ ] Implement `CNFFormula.java` and `CNFClause.java`
- [ ] Implement `SATCell.java` with all strategy logic
- [ ] Write unit tests for formula evaluation
- [ ] **Validation:** `CNFFormula.evaluate()` matches MiniSat on 10 test cases


### Phase 2: Candidate Generation (2 days)

- [ ] Implement `SATInstanceGenerator.java` with planted-solution method
- [ ] Implement `AssignmentGenerator.java` with strategy-specific logic
- [ ] Implement `ConstraintDensityAnalyzer.java`
- [ ] **Validation:** Generated instances verified satisfiable by MiniSat


### Phase 3: Factory and Experiment Runner (2 days)

- [ ] Implement `SATCellFactory.java`
- [ ] Implement `SATExperiment.java`
- [ ] Implement `MiniSatValidator.java`
- [ ] Wire into existing `CellBasedExecutionEngine`
- [ ] **Validation:** Single experiment runs without errors


### Phase 4: Experimental Validation (3 days)

- [ ] Run pilot experiment (20 vars, 10 trials)
- [ ] Run full experiment (50 vars, 100 trials per configuration)
- [ ] Run control experiments
- [ ] Compute statistical summaries
- [ ] Generate visualizations
- [ ] **Validation:** Results statistically significant (p < 0.05)


### Phase 5: Analysis and Documentation (2 days)

- [ ] Extract and analyze cluster boundaries
- [ ] Map strategy dominance regions
- [ ] Document findings in `docs/findings/sat_clustering_experiment_001.md`
- [ ] Update `CLUSTERING_PRIMITIVE_SPEC.md` with SAT results[^2]
- [ ] Create PR with findings per Grand Marshal guidelines[^5]

**Total estimated time:** 12 days

***

## Appendix A: Implementation Checklist (Expanded per Review)

### Pre-Implementation

- [ ] Install MiniSat and set `MINISAT_PATH` environment variable
- [ ] Review factorization package structure
- [ ] Understand `AbstractCell` contract
- [ ] Study `AlgotypeAggregationIndex` implementation


### Development

- [ ] Create `sat` package with `package-info.java`
- [ ] Implement core classes (see Phase 1-3)
- [ ] Use Conventional Commits (e.g., `feat(sat): add SATCell implementation`)
- [ ] Commit after each phase completion


### Validation

- [ ] Run single experiment (10 trials) to verify infrastructure
- [ ] Check aggregation metric produces values in[^6]
- [ ] Verify satisfaction score computation matches MiniSat
- [ ] Run full batch (100 trials) for statistical validation
- [ ] Generate comparison table: clustered vs non-clustered


### Documentation

- [ ] Update `CLUSTERING_PRIMITIVE_SPEC.md` with SAT findings
- [ ] Create experiment report in `docs/findings/`
- [ ] Add README to `sat` package

***

## Appendix B: Sample 3-SAT Instance (per Review)

**Instance:** 10 variables, 40 clauses (ratio 4.0), satisfiable

**DIMACS Format:**

```
c Sample 3-SAT instance for testing
c Variables: x1-x10, Clauses: 40
c Planted solution: x1=T, x2=F, x3=T, x4=T, x5=F, x6=T, x7=F, x8=T, x9=F, x10=T
p cnf 10 40
1 -2 3 0
-1 4 5 0
2 -3 6 0
-4 5 -7 0
1 6 8 0
-2 -5 9 0
3 7 -10 0
4 -6 1 0
-8 9 10 0
5 -7 -9 0
1 2 -4 0
-3 6 7 0
8 -9 -10 0
-1 -2 5 0
3 4 -8 0
6 7 9 0
-5 8 10 0
1 -6 -7 0
2 3 -9 0
4 5 10 0
-1 -3 8 0
2 6 -10 0
-4 7 9 0
5 -8 1 0
-2 3 -6 0
4 -7 10 0
-5 6 -9 0
1 8 -10 0
-3 -4 7 0
2 5 9 0
6 -8 -1 0
-7 9 10 0
3 -5 -6 0
4 8 -9 0
-2 7 1 0
5 -10 -3 0
6 9 4 0
-8 10 -7 0
1 -4 -5 0
2 3 8 0
```

**Java Representation:**

```java
// Programmatic creation of sample instance
CNFFormula sampleInstance = new CNFFormula(List.of(
    new CNFClause(Map.of("x1", true, "x2", false, "x3", true)),
    new CNFClause(Map.of("x1", false, "x4", true, "x5", true)),
    // ... remaining 38 clauses
));

// Planted solution for validation
Map<String, Boolean> plantedSolution = Map.of(
    "x1", true, "x2", false, "x3", true, "x4", true, "x5", false,
    "x6", true, "x7", false, "x8", true, "x9", false, "x10", true
);

assert sampleInstance.evaluate(plantedSolution); // Must be true
```


***

## Appendix C: References

1. **Levin et al. 2024** - Zhang, T., Goldstein, A., Levin, M. "Classical Sorting Algorithms as a Model of Morphogenesis." arXiv:2401.05375v1. [PDF](https://github.com/zfifteen/emergent-doom-engine/blob/main/docs/theory/2401.05375v1.pdf)[^3]
2. **EDE Repository** - https://github.com/zfifteen/emergent-doom-engine
3. **Clustering Primitive Spec** - `CLUSTERING_PRIMITIVE_SPEC.md`[^2]
4. **First Non-Sorting Experiment** - `FIRST_NON_SORTING_EXPERIMENT.md`[^1]
5. **EDE Requirements** - `REQUIREMENTS.md`[^4]
6. **MiniSat Solver** - http://minisat.se/ (MIT License)
7. **Phase Transition in 3-SAT** - Kirkpatrick, S., Selman, B. "Critical Behavior in the Satisfiability of Random Boolean Expressions." Science 264(5163):1297-1301, 1994.

***

**Status:** Implementation ready. All peer review issues addressed. Proceed to Phase 1.
<span style="display:none">[^7][^8][^9]</span>

<div align="center">⁂</div>

[^1]: FIRST_NON_SORTING_EXPERIMENT.md

[^2]: CLUSTERING_PRIMITIVE_SPEC.md

[^3]: 2401.05375v1.pdf

[^4]: REQUIREMENTS.md

[^5]: EDE_PM_INSTRUCTIONS.md

[^6]: turing.pdf

[^7]: PEER_REVIEW.MD

[^8]: references.md

[^9]: README.md

