# Clustering as a Computational Primitive

**Version:** 1.0  
**Date:** 2026-01-02  
**Status:** Draft  
**Goal Supported:** Turn algotype clustering into a reusable computational primitive applicable beyond sorting

---

## 1. Executive Summary

This document formalizes how the emergent clustering behavior observed in chimeric sorting arrays can be extracted and reused as a **general-purpose computational primitive**. Clustering is not merely a side effect—it is "free compute" that partitions problem spaces, groups compatible strategies, and reveals structure without explicit programming.

---

## 2. What Clustering Measures

### 2.1 Aggregation Value Definition

**Formula:**
```
Aggregation = (Cells with at least one same-algotype neighbor) / (Total cells) × 100
```

**Implemented in:** `com.emergent.doom.metrics.AggregationValue.java`

### 2.2 Computational Interpretation

| Metric | What It Represents Computationally | Analogy |
|--------|-----------------------------------|--------|
| **Aggregation Value** | Degree of local consensus among agents with similar strategies | Clustering coefficient in social networks |
| **Peak Aggregation** | Maximum emergent grouping during problem-solving | Phase transition / crystallization point |
| **Peak Timing (% progress)** | When strategy-grouping is most pronounced relative to goal progress | Critical point in annealing |
| **Final Aggregation** | Residual structure after goal achievement | Persistent memory / learned partitioning |
| **Aggregation Trajectory Shape** | Dynamics of consensus formation and dissolution | Energy landscape traversal |

### 2.3 Key Observations from Levin Paper

| Chimeric Pair | Peak Aggregation | Peak Timing | Interpretation |
|--------------|------------------|-------------|----------------|
| Bubble-Selection | 72% | 42% progress | Strong mid-process grouping, dissolves as sorting completes |
| Bubble-Insertion | 65% | 21% progress | Early grouping, weaker signal |
| Selection-Insertion | 69% | 19% progress | Very early grouping |
| Control (same algo, different labels) | ~61% | Random | No meaningful clustering = baseline noise |

**Insight:** Different algorithm pairings produce distinct clustering signatures. This is exploitable.

---

## 3. What Clustering Encodes

### 3.1 Strategy Compatibility

When cells with algotype A cluster together and separate from algotype B, the system has **discovered** that:
- A-type strategies are locally more compatible with each other
- A and B strategies create friction when adjacent
- The problem space has regions where A works better than B (and vice versa)

### 3.2 Problem Space Partitioning

Clustering effectively **partitions the problem space** without explicit instruction:
- Clusters = regions where a particular strategy dominates
- Cluster boundaries = transition zones / conflict regions
- Cluster sizes = relative "market share" of each strategy in the current configuration

### 3.3 Information Content

The clustering trajectory contains information about:
1. **Compatibility matrix** between strategies (which pairs cluster more strongly)
2. **Problem difficulty** (harder regions may show more clustering as strategies specialize)
3. **Solution structure** (final clustering with duplicates reveals stable partitions)

---

## 4. Clustering as Primitive: API Sketch

### 4.1 Type Definitions

```java
/**
 * Strategy represents a problem-solving approach or algorithm variant.
 * In sorting, this maps to algotypes (Bubble, Selection, Insertion).
 * In other domains, this could be any categorical classification of agents.
 * 
 * Implementations could be:
 * - An enum (e.g., FactorStrategy, SortingAlgotype)
 * - An interface for custom strategy implementations
 * - A string identifier for dynamic strategy assignment
 */
public interface Strategy {
    String getName();
}

/**
 * IntRange represents a contiguous range of indices [start, end).
 * Used to define cluster boundaries and regional dominance.
 * 
 * Example: IntRange(5, 12) represents indices 5, 6, 7, 8, 9, 10, 11.
 */
public record IntRange(int start, int end) {
    public int length() { return end - start; }
    public boolean contains(int index) { return index >= start && index < end; }
}
```

### 4.2 Core Interface

```java
public interface ClusteringPrimitive<T> {
    
    /**
     * Run clustering process and return partition.
     * @param items Elements to partition
     * @param strategies Strategy assignments for each element
     * @param goalFunction What "sorted" means in this domain
     * @return ClusteringResult with partition and trajectory
     */
    ClusteringResult<T> computePartition(
        List<T> items,
        Map<T, Strategy> strategies,
        GoalFunction<T> goalFunction
    );
    
    /**
     * Extract cluster boundaries as candidate cut points.
     */
    List<Integer> extractBoundaries(ClusteringResult<T> result);
    
    /**
     * Get strategy dominance by region.
     */
    Map<IntRange, Strategy> getRegionalDominance(ClusteringResult<T> result);
}
```

### 4.3 ClusteringResult Structure

```java
public class ClusteringResult<T> {
    private final List<T> finalArrangement;
    private final List<Double> aggregationTrajectory;
    private final double peakAggregation;
    private final double peakTimingPercent;
    private final Map<Strategy, List<IntRange>> clusterRanges;
    private final double finalAggregation;
    
    // Derived metrics
    public double getClusteringStrength();      // peak - baseline
    public double getClusteringPersistence();   // final - baseline  
    public List<Integer> getClusterBoundaries();
}
```

---

## 5. Non-Sorting Applications

### 5.1 Hypothesis Grouping

**Problem:** Given N hypotheses about a phenomenon, group compatible ones.

**Mapping:**
- Cell value = hypothesis ID
- Algotype = hypothesis class (e.g., "mechanism A" vs "mechanism B")
- "Sorted" = hypotheses ordered by some fitness/plausibility metric
- Clustering reveals which hypothesis classes are locally compatible

### 5.2 Factor Candidate Partitioning (Factorization)

**Problem:** Given a semiprime N, identify candidate factor pairs.

**Mapping:**
- Cell value = candidate factor
- Algotype = factor-finding strategy (e.g., "small primes" vs "Fermat-near-sqrt")
- "Sorted" = candidates ordered by proximity to true factors
- Clustering reveals which strategy classes find similar candidates

**Potential value:** Clusters might concentrate around true factors before explicit discovery.

### 5.3 Signal Routing / Wave-CRISPR

**Problem:** Route signals through a network with competing routing strategies.

**Mapping:**
- Cell value = signal packet ID
- Algotype = routing strategy
- "Sorted" = packets reaching destination in optimal order
- Clustering reveals which routing strategies are compatible on which network regions

---

## 6. Experimental Validation Requirements

### 6.1 Baseline Validation

Before using clustering as a primitive, validate that:
- [ ] Aggregation metric matches paper values (72% peak for Bubble-Selection)
- [ ] Control experiments show ~50-61% (random baseline)
- [ ] Peak timing correlates with algorithm pairing as expected

### 6.2 Primitive Extraction Tests

- [ ] Cluster boundaries are stable across runs (low variance)
- [ ] Extracted partitions are meaningful (not random)
- [ ] Information content exceeds what random partitioning would provide

### 6.3 Non-Sorting Application Tests

- [ ] Design toy problem with known structure
- [ ] Run clustering primitive
- [ ] Verify clustering discovers known structure
- [ ] Measure computational cost vs. direct methods

---

## 7. Implementation Status in EDE

| Component | Status | Location |
|-----------|--------|----------|
| AggregationValue metric | ✅ Implemented | `metrics/AggregationValue.java` |
| AlgotypeAggregationIndex | ✅ Implemented | `metrics/AlgotypeAggregationIndex.java` |
| Chimeric array support | ✅ Implemented | `chimeric/` package |
| Trajectory recording | ✅ Implemented | `probe/` package |
| ClusteringPrimitive interface | ❌ Not yet | Proposed above |
| Boundary extraction | ❌ Not yet | Proposed above |
| Non-sorting goal functions | ❌ Not yet | Needed for Step 2 |

---

## 8. Next Steps

1. **Validate baseline:** Run chimeric experiments and confirm paper-matching aggregation values
2. **Implement ClusteringPrimitive interface:** Extract pattern from existing chimeric code
3. **Design first non-sorting experiment:** See `FIRST_NON_SORTING_EXPERIMENT.md`
4. **Measure information content:** Quantify what clustering tells us vs. random baseline

---

## References

- Zhang, T., Goldstein, A., Levin, M. (2024). Classical Sorting Algorithms as a Model of Morphogenesis. arXiv:2401.05375v1
- EDE REQUIREMENTS.md: `docs/requirements/REQUIREMENTS.md`
- Levin paper (local): `docs/theory/2401.05375v1.md`
