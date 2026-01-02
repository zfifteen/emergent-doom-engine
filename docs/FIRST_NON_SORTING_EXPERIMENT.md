# First Non-Sorting Experiment: Factor Candidate Partitioning

**Version:** 1.0  
**Date:** 2026-01-02  
**Status:** Draft  
**Goal Supported:** Demonstrate EDE on a non-sorting application using clustering as computational primitive

---

## 1. Problem Statement

**Domain:** Integer factorization (toy scale)  
**Challenge:** Given a semiprime N = p × q, can emergent clustering help identify factor candidates?

**Why this problem:**
- Clear success criteria (we know the factors)
- Maps naturally to sorting (candidates can be ordered)
- Multiple strategies exist (trial division, Fermat, Pollard-rho concepts)
- Clustering might reveal structure before explicit discovery

---

## 2. Hypothesis

> When cells encode factor candidates and algotypes encode factor-finding strategies, emergent clustering will group candidates by their "closeness" to true factors, providing a partitioning that narrows the search space.

**Null hypothesis:** Clustering provides no better partitioning than random assignment.

---

## 3. Experimental Design

### 3.1 Problem Encoding

| EDE Concept | Factorization Mapping |
|-------------|----------------------|
| Cell | Factor candidate |
| Cell.value | Candidate integer |
| Cell.algotype | Factor-finding strategy class |
| "Sorted" state | Candidates ordered by factor-fitness score |
| Aggregation | Clustering of strategies around similar candidates |

### 3.2 Algotypes (Strategies)

```java
public enum FactorStrategy {
    SMALL_PRIMES,      // Candidates from small prime trial division
    FERMAT_NEAR_SQRT,  // Candidates near sqrt(N) (Fermat's method intuition)
    RANDOM_SAMPLE      // Random candidates in range [2, sqrt(N)]
}
```

### 3.3 Factor Fitness Score

Define a fitness function that measures "closeness" to being a factor:

```java
public double factorFitness(int candidate, int N) {
    int remainder = N % candidate;
    if (remainder == 0) return 1.0;  // Perfect factor
    
    // Score based on how close N is to the nearest multiple of candidate
    // Smaller modular distance = closer to being a factor
    int distanceToMultiple = Math.min(remainder, candidate - remainder);
    return 1.0 - ((double) distanceToMultiple / candidate);
}
```

**"Sorted" state:** Cells ordered by descending factor fitness.

### 3.4 Experiment Parameters

| Parameter | Value | Rationale |
|-----------|-------|----------|
| N (semiprime) | 143 (11 × 13) | Small enough to verify, large enough to be non-trivial |
| Array size | 50 candidates | Manageable size, enough for clustering signal |
| Candidates | Mix from each strategy | ~17 per strategy |
| Algotype distribution | 33% / 33% / 34% | Equal representation |
| Runs per experiment | 100 | Statistical significance |

### 3.5 Candidate Generation

```java
/**
 * Generate factor candidates using specified strategy.
 * @param N The semiprime to factor
 * @param strategy The factor-finding strategy to use
 * @param count Number of candidates to generate
 * @param rand Random instance for reproducibility (use fixed seed for experiments)
 */
public List<Integer> generateCandidates(int N, FactorStrategy strategy, int count, Random rand) {
    int sqrtN = (int) Math.sqrt(N);
    List<Integer> candidates = new ArrayList<>();
    
    switch (strategy) {
        case SMALL_PRIMES:
            // First 'count' primes <= sqrtN
            candidates = primesUpTo(sqrtN).stream().limit(count).collect(toList());
            break;
            
        case FERMAT_NEAR_SQRT:
            // Candidates clustered around sqrt(N), clamped to [2, sqrtN]
            for (int i = 0; i < count; i++) {
                int candidate = sqrtN - count / 2 + i;
                if (candidate < 2) {
                    candidate = 2;
                } else if (candidate > sqrtN) {
                    candidate = sqrtN;
                }
                candidates.add(candidate);
            }
            break;
            
        case RANDOM_SAMPLE:
            // Random integers in [2, sqrtN]
            for (int i = 0; i < count; i++) {
                candidates.add(2 + rand.nextInt(sqrtN - 1));
            }
            break;
    }
    return candidates;
}
```

---

## 4. Measurements

### 4.1 Primary Metrics

| Metric | Definition | Success Criterion |
|--------|------------|------------------|
| Peak Aggregation | Max clustering during sorting | > 60% (above random baseline) |
| Factor Proximity in Clusters | Do true factors (11, 13) end up in same/adjacent clusters? | Significantly more often than random |
| Strategy Dominance near Factors | Which algotype dominates the cluster containing true factors? | Consistent pattern across runs |

### 4.2 Secondary Metrics

| Metric | Definition | Purpose |
|--------|------------|--------|
| Aggregation Trajectory | Shape of clustering over time | Compare to sorting experiments |
| Cluster Boundary Stability | Variance in boundary positions across runs | Assess reliability |
| Time to Peak | When does max clustering occur? | Compare to sorting (42% for Bubble-Selection) |

### 4.3 Control Experiments

1. **Negative Control:** Same candidates, all same algotype (different labels only)
   - Expected: ~50-60% aggregation (random baseline)
   
2. **Positive Control:** Standard Bubble-Selection chimeric sort
   - Expected: ~72% peak aggregation at ~42% progress
   
3. **Random Fitness:** Replace factor fitness with random scores
   - Expected: No meaningful clustering pattern

---

## 5. Success / Failure Criteria

### 5.1 Success (Hypothesis Supported)

- [ ] Peak aggregation significantly exceeds negative control (p < 0.05)
- [ ] True factors appear in same or adjacent clusters > 70% of runs
- [ ] At least one algotype shows consistent dominance near factors
- [ ] Clustering narrows search space by at least 50% (top cluster contains factors)

### 5.2 Partial Success

- [ ] Aggregation exceeds control but factors not consistently clustered
- [ ] Insight: Clustering captures something, but not factor proximity

### 5.3 Failure (Null Hypothesis Not Rejected)

- [ ] Aggregation indistinguishable from negative control
- [ ] Factor positions random relative to clusters
- [ ] Conclusion: Factorization structure not captured by this encoding

---

## 6. Implementation Plan

### 6.1 Phase 1: Infrastructure (Est. 2-3 days)

1. Create `com.emergent.doom.factorization` package
2. Implement `FactorStrategy` enum
3. Implement `FactorFitnessGoal` (GoalFunction for factorization)
4. Create `FactorCell` extending base Cell with candidate value

### 6.2 Phase 2: Experiment Runner (Est. 2 days)

1. Create `FactorizationExperiment` class
2. Implement candidate generation for each strategy
3. Wire up to existing experiment infrastructure
4. Add factor-specific probe recording

### 6.3 Phase 3: Analysis (Est. 2 days)

1. Run 100 experiments for each condition (main + 3 controls)
2. Compute statistics and p-values
3. Generate visualizations:
   - Aggregation trajectory plots
   - Cluster composition heatmaps
   - Factor position distributions

### 6.4 Phase 4: Documentation (Est. 1 day)

1. Record results in `docs/findings/`
2. Update CLUSTERING_PRIMITIVE_SPEC.md with findings
3. Identify next experiment based on results

---

## 7. Required Code Changes

### 7.1 New Files

```
src/main/java/com/emergent/doom/factorization/
├── FactorStrategy.java           # Algotype enum
├── FactorFitnessGoal.java        # Goal function
├── FactorCell.java               # Cell with candidate value
├── FactorizationExperiment.java  # Experiment runner
└── FactorAnalysis.java           # Result analysis
```

### 7.2 Modified Files

- `experiment/ExperimentRunner.java` - Add factorization experiment type
- `export/` - Add factorization-specific export format

### 7.3 Test Files

```
src/test/java/com/emergent/doom/factorization/
├── FactorFitnessGoalTest.java
├── FactorizationExperimentTest.java
└── FactorAnalysisTest.java
```

---

## 8. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|------------|
| Encoding doesn't capture factor structure | Medium | High | Start with toy size, iterate on encoding |
| Clustering too weak to detect | Medium | Medium | Increase array size, try different strategies |
| Results not reproducible | Low | High | Fix random seeds, run 100+ trials |
| Takes too long to run | Low | Low | Use smaller arrays for debugging |

---

## 9. Definition of Done

- [ ] All code implemented and tested
- [ ] 100 runs completed for main experiment
- [ ] 100 runs completed for each control
- [ ] Statistical analysis complete with p-values
- [ ] Results documented in `docs/findings/factorization_experiment_001.md`
- [ ] CLUSTERING_PRIMITIVE_SPEC.md updated with findings
- [ ] Next steps identified based on results

---

## 10. Links to Goals

| This Document | Supports |
|---------------|----------|
| Factor-fitness encoding | Sub-goal: Design non-sorting goal function |
| Experiment design | Sub-goal: Validate clustering as computational primitive |
| Success criteria | High-level: Demonstrate EDE beyond sorting |
| Implementation plan | High-level: Advance EDE as domain-general framework |
