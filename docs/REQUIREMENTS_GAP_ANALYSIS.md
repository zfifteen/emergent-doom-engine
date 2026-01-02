# Requirements Gap Analysis: EDE Implementation vs REQUIREMENTS.md

**Version:** 1.0  
**Date:** 2026-01-02  
**Status:** Draft  
**Goal Supported:** EDE architecture formalization; identify blockers for clustering-primitive experiments

---

## 1. Summary

This document audits the current [emergent-doom-engine](https://github.com/zfifteen/emergent-doom-engine) Java implementation against the REQUIREMENTS.md specification (located at `docs/requirements/REQUIREMENTS.md`). The analysis identifies gaps that may block the next phase: using clustering as a computational primitive for non-sorting applications.

**Overall Assessment:** The EDE implementation is **substantially complete** for sorting-based experiments. Key gaps are in **abstraction for non-sorting domains** and **clustering-specific analysis tools**.

---

## 2. Gap Analysis Table

### 2.1 Core Data Structures (REQUIREMENTS §2)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Cell class with value, position, algotype | §2.1 | ✅ Complete | `cell/` package | - |
| FrozenState enum | §2.1 | ✅ Complete | Implemented | - |
| Probe class | §2.2 | ✅ Complete | `probe/` package | - |
| StepRecord structure | §2.2 | ✅ Complete | In probe package | - |
| ExperimentConfig | §2.3 | ✅ Complete | `experiment/` package | - |
| BatchExperimentConfig | §2.3 | ✅ Complete | Implemented | - |
| **Generic Goal Function interface** | Not in REQ | ❌ Missing | Sorting hardcoded; need abstraction for factorization | **HIGH** |

### 2.2 Cell-View Sorting Algorithms (REQUIREMENTS §3)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Cell-View Bubble Sort | §3.1 | ✅ Complete | Implemented | - |
| Cell-View Insertion Sort | §3.2 | ✅ Complete | Implemented | - |
| Cell-View Selection Sort | §3.3 | ✅ Complete | Implemented | - |
| Traditional baselines | §3 | ✅ Complete | `traditional/` package | - |

### 2.3 Evaluation Metrics (REQUIREMENTS §4)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Monotonicity Error | §4.1 | ✅ Complete | `metrics/MonotonicityError.java` | - |
| Sortedness Value | §4.2 | ✅ Complete | `metrics/SortednessValue.java` | - |
| Delayed Gratification | §4.3 | ✅ Complete | `metrics/DelayedGratificationCalculator.java` | - |
| Aggregation Value | §4.4 | ✅ Complete | `metrics/AggregationValue.java` | - |
| **Cluster Boundary Extraction** | Not in REQ | ❌ Missing | Needed for clustering-as-primitive | **HIGH** |
| **Aggregation Trajectory Analysis** | Not in REQ | ⚠️ Partial | Recording exists; analysis tools missing | **MEDIUM** |

### 2.4 Frozen Cell Implementation (REQUIREMENTS §5)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Frozen cell types | §5.1 | ✅ Complete | MOVABLE_FROZEN, IMMOVABLE_FROZEN | - |
| Swap logic with frozen | §5.2 | ✅ Complete | Implemented | - |
| Frozen cell placement | §5.3 | ✅ Complete | Implemented | - |

### 2.5 Chimeric Array Implementation (REQUIREMENTS §6)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Mixed algotype arrays | §6.1 | ✅ Complete | `chimeric/` package | - |
| Conflicting goals | §6.2 | ✅ Complete | Directional conflicts | - |
| Duplicate values | §6.3 | ✅ Complete | For persistent aggregation | - |
| **Non-sorting chimeric support** | Not in REQ | ❌ Missing | Chimeric assumes sorting goal | **HIGH** |

### 2.6 Threading and Synchronization (REQUIREMENTS §7)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Parallel cell execution | §7.1 | ✅ Complete | `execution/` package | - |
| Cell worker function | §7.2 | ✅ Complete | Implemented | - |
| Swap resolution | §7.3 | ✅ Complete | `swap/` package | - |
| Termination detection | §7.4 | ✅ Complete | Implemented | - |

### 2.7 Experiment Execution (REQUIREMENTS §8)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Single experiment runner | §8.1 | ✅ Complete | Implemented | - |
| Batch experiments | §8.2 | ✅ Complete | Implemented | - |
| Statistical analysis | §8.3 | ✅ Complete | `statistics/` package | - |

### 2.8 Data Persistence and Analysis (REQUIREMENTS §9)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Probe data format | §9.1 | ✅ Complete | `export/` package | - |
| Evaluation pipeline | §9.2 | ✅ Complete | `analysis/` package | - |
| **Clustering-specific analysis** | Not in REQ | ❌ Missing | Need ClusteringAnalyzer class | **MEDIUM** |

### 2.9 Visualization (REQUIREMENTS §10)

| Feature | REQ Section | Implementation Status | Gap Description | Priority |
|---------|-------------|----------------------|-----------------|----------|
| Sortedness trajectory plots | §10.1 | ✅ Complete | `visualization/` package | - |
| Aggregation curves | §10.2 | ✅ Complete | Implemented | - |
| Efficiency comparison | §10.3 | ✅ Complete | Implemented | - |
| **Cluster composition heatmaps** | Not in REQ | ❌ Missing | Needed for non-sorting analysis | **LOW** |

---

## 3. Top 3 Gaps Blocking Clustering-Primitive Experiments

### Gap #1: Generic Goal Function Interface

**Current State:** Goal is hardcoded as "sorting" throughout the codebase. `SortednessValue` assumes integer comparison ordering.

**Needed:** Abstract `GoalFunction<T>` interface that defines:
- What "progress" means in any domain
- How to measure distance to goal state
- What "sorted" or "solved" means

**Impact:** Cannot run factorization or other non-sorting experiments without this.

**Proposed Solution:**
```java
public interface GoalFunction<T> {
    double measureProgress(List<Cell<T>> cells);
    boolean isGoalReached(List<Cell<T>> cells);
    int compare(T a, T b);  // Ordering for "sorted" state
}
```

**Effort Estimate:** 3-5 days to refactor

---

### Gap #2: Cluster Boundary Extraction

**Current State:** `AggregationValue` computes overall clustering percentage but does not identify where clusters begin/end.

**Needed:** Method to extract:
- Cluster boundaries (indices where algotype changes)
- Cluster sizes and compositions
- Boundary stability across runs

**Impact:** Cannot use clustering as a partitioning primitive without knowing where partitions are.

**Proposed Solution:**
```java
public class ClusterBoundaryExtractor {
    public List<ClusterRegion> extractClusters(List<Cell<?>> cells);
    public List<Integer> getBoundaryIndices(List<Cell<?>> cells);
    public Map<Algotype, Double> getClusterPurity(ClusterRegion region);
}
```

**Effort Estimate:** 2-3 days

---

### Gap #3: Non-Sorting Chimeric Support

**Current State:** `chimeric/` package assumes cells are being sorted by integer value.

**Needed:** Generalize chimeric array construction to work with any `GoalFunction<T>`.

**Impact:** Cannot run factorization chimeric experiments.

**Proposed Solution:** Parameterize chimeric classes with goal function:
```java
public class ChimericArray<T> {
    private final GoalFunction<T> goal;
    private final List<Cell<T>> cells;
    // ...
}
```

**Effort Estimate:** 2-3 days (depends on Gap #1)

---

## 4. Implementation Priority Matrix

| Gap | Blocks | Effort | Priority Score | Recommended Order |
|-----|--------|--------|----------------|------------------|
| Generic GoalFunction | Everything non-sorting | High | **Critical** | 1st |
| Cluster Boundary Extraction | Clustering-as-primitive | Medium | **High** | 2nd |
| Non-Sorting Chimeric | Factorization experiment | Medium | **High** | 3rd (after #1) |
| Aggregation Trajectory Analysis | Deep clustering insights | Low | Medium | 4th |
| Cluster Composition Heatmaps | Visualization only | Low | Low | 5th |

---

## 5. Recommended Action Plan

### Week 1: Foundation
1. Design and implement `GoalFunction<T>` interface
2. Refactor `SortednessValue` to implement `GoalFunction<Integer>`
3. Update Cell class to be generic `Cell<T>`
4. Verify all existing sorting tests still pass

### Week 2: Clustering Tools
1. Implement `ClusterBoundaryExtractor`
2. Add cluster region data structures
3. Create `ClusteringAnalyzer` for trajectory analysis
4. Write unit tests for boundary detection

### Week 3: Factorization Experiment
1. Implement `FactorFitnessGoal` using new interface
2. Generalize chimeric support
3. Run first factorization experiment
4. Document results

---

## 6. Validation Checklist

Before proceeding to non-sorting experiments, verify:

- [ ] All existing sorting tests pass after refactoring
- [ ] `GoalFunction<Integer>` produces identical results to hardcoded sorting
- [ ] Chimeric Bubble-Selection still shows 72% peak aggregation
- [ ] Cluster boundaries match visual inspection of aggregation plots
- [ ] New code has >80% test coverage

---

## 7. References

- REQUIREMENTS.md: `docs/requirements/REQUIREMENTS.md`
- CLUSTERING_PRIMITIVE_SPEC.md: `docs/CLUSTERING_PRIMITIVE_SPEC.md`
- FIRST_NON_SORTING_EXPERIMENT.md: `docs/FIRST_NON_SORTING_EXPERIMENT.md`
- EDE Java source: `src/main/java/com/emergent/doom/`
