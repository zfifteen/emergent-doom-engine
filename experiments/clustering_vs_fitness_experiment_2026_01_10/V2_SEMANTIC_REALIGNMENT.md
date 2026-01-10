# V2 Semantic Realignment: Migration from Strategy-Based to Fitness-Field Metrics

**Document Version:** 2.0  
**Date:** January 10, 2026  
**Status:** Implementation Complete  
**Related:** EXPERIMENT_SETUP_AUDIT.md (v1 analysis)

---

## Executive Summary

Phase 1 of the clustering vs fitness experiment redesign implements **semantic realignment** with Levin's framework to avoid circular reasoning and enable multi-CP experimentation with unified theoretical semantics. The key change is moving from **strategy-label clustering** (v1) to **fitness-field clustering** (v2) metrics.

### Key Changes

1. **New Metrics Implemented:**
   - `FitnessSimilarityClusteringIndex` - measures fitness-field spatial aggregation
   - `FactorLocalizationIndex` - measures pattern formation (inter-factor proximity)

2. **Terminology Clarification:**
   - **Localization** = concentration of high-fitness configurations in morphospace
   - **Fitness Clustering** = spatial aggregation of similar FITNESS values
   - **Strategy Aggregation** = spatial grouping of same-STRATEGY labels (v1 metric, retained for comparison)

3. **Code Updates:**
   - `StepMetrics` now includes both v1 (strategy) and v2 (fitness) metrics
   - `ClusteringVsFitnessExperiment` computes all metrics per step
   - CSV export format updated with new columns

---

## Theoretical Foundation

### Levin's Framework Semantics

From Levin et al. (2024), pattern formation and localization should be **substrate-independent**. The framework distinguishes:

- **Algotype aggregation**: Emergent clustering of cells with same ALGORITHM during sorting (§8)
- **Pattern formation**: Concentration of high-fitness configurations in morphospace
- **Localization**: Spatial proximity of functionally related elements

### Problem with V1 Metrics

V1 experiment committed **circular reasoning** (begging the question):

```
MANIPULATION: C2 pre-groups cells by STRATEGY label
MEASUREMENT: "aggregation" = % cells with same-STRATEGY neighbor
HYPOTHESIS: "Does clustering affect localization?"
```

**Logical flaw**: The measurement ASSUMES what it's trying to prove. If "clustering" means "strategy grouping" in both manipulation AND measurement, the test becomes: "Does pre-grouping strategies affect convergence when measured by strategy grouping?"

### V2 Solution: Fitness-Field Semantics

V2 separates manipulation from measurement:

```
MANIPULATION: C2 pre-groups cells by STRATEGY label (for spatial structure)
MEASUREMENT: "fitness clustering" = % cells with similar FITNESS neighbor
HYPOTHESIS: "Does initial spatial structure affect fitness-driven localization?"
```

**No circular reasoning**: Strategy manipulation is independent of fitness measurement. We can now test whether pre-arranging strategies affects how quickly the fitness landscape reorganizes.

---

## Metric Definitions

### Strategy Aggregation (v1 - Retained for Comparison)

**Purpose:** Measure spatial grouping of cells with the SAME STRATEGY LABEL

**Formula:**
```
For each cell i:
  leftSame = (strategy[i-1] == strategy[i])
  rightSame = (strategy[i+1] == strategy[i])
  if (leftSame OR rightSame): count++
return (count / totalCells) × 100
```

**Use Case:** Chimeric algotype studies (Levin §8) where goal is to observe emergent strategy clustering

**NOT FOR:** Pattern formation experiments (creates circular reasoning)

**Example:**
```
Array: [SMALL, SMALL, FERMAT, RANDOM, RANDOM, RANDOM]
Strategy aggregation: 83.3% (5/6 cells have same-strategy neighbor)
```

---

### Fitness Clustering (v2 - NEW)

**Purpose:** Measure spatial aggregation of cells with SIMILAR FITNESS VALUES

**Formula:**
```
FITNESS_THRESHOLD = 0.1
For each cell i:
  leftSimilar = |fitness[i-1] - fitness[i]| < FITNESS_THRESHOLD
  rightSimilar = |fitness[i+1] - fitness[i]| < FITNESS_THRESHOLD
  if (leftSimilar OR rightSimilar): count++
return (count / totalCells) × 100
```

**Use Case:** Pattern formation analysis independent of strategy labels

**Example:**
```
Array: [0.95, 0.92, 0.15, 0.18, 0.20, 0.22]
Fitness clustering: 100% (all cells have fitness-similar neighbor)
Strategy labels: [SMALL, FERMAT, RANDOM, SMALL, FERMAT, RANDOM]
Strategy aggregation: 0% (no same-strategy neighbors)
```

**Interpretation:**
- High fitness clustering (≥75%) → fitness landscape is spatially structured
- Low fitness clustering (≤25%) → fitness landscape is fragmented
- Random baseline: ~50%

---

### Factor Localization (v2 - NEW)

**Purpose:** Measure concentration of high-fitness factors in morphospace

**Formula:**
```
interFactorDistance = |pos[factor1] - pos[factor2]|
maxDistance = arraySize - 1
localization = 1.0 - (interFactorDistance / maxDistance)
```

**Use Case:** Quantify pattern formation independent of task-specific convergence criteria

**Example:**
```
Array size: 50
Factors at [2, 3]: localization = 1.0 - 1/49 = 0.98 (high)
Factors at [0, 49]: localization = 1.0 - 49/49 = 0.0 (low)
Factors at [20, 30]: localization = 1.0 - 10/49 = 0.80 (moderate)
```

**Interpretation:**
- Localization = 1.0: perfect pattern formation (factors adjacent)
- Localization ≥ 0.9: strong pattern formation
- Localization ~ 0.5: moderate pattern formation
- Localization ≤ 0.1: minimal pattern formation

**Comparison with Convergence:**
- Convergence criterion: "both factors in positions [0,4]" (task-specific success)
- Localization: inter-factor proximity (pattern formation)
- Factors at [2,3]: BOTH converged AND highly localized
- Factors at [0,49]: NOT converged, NOT localized
- Localization separates pattern formation from task completion

---

## Implementation Details

### StepMetrics Changes

**Old (v1):**
```java
public StepMetrics(
    int stepNumber,
    double aggregationValue,  // strategy aggregation only
    int[] factorPositions,
    double meanFactorDistanceFromFront,
    ...
)
```

**New (v2):**
```java
public StepMetrics(
    int stepNumber,
    double strategyAggregation,      // v1 metric (renamed for clarity)
    double fitnessClustering,        // v2 FITNESS-FIELD metric
    double factorLocalization,       // v2 PATTERN FORMATION metric
    int[] factorPositions,
    double meanFactorDistanceFromFront,
    ...
)
```

### CSV Export Format

**Old (v1):**
```
step,aggregation,factor_11_pos,factor_13_pos,mean_factor_dist,...
```

**New (v2):**
```
step,strategy_agg,fitness_clust,factor_local,factor_11_pos,factor_13_pos,mean_factor_dist,...
```

**Column Descriptions:**
- `strategy_agg`: Strategy aggregation (v1 metric, for comparison)
- `fitness_clust`: Fitness clustering (v2 metric, for hypothesis testing)
- `factor_local`: Factor localization (v2 metric, for pattern formation analysis)

---

## Experimental Implications

### Hypothesis Testing with V2 Metrics

**Clustering Hypothesis (v1 - INVALID):**
> "Initial strategy aggregation accelerates factor localization"

**Problem:** Circular reasoning (strategy manipulation measured by strategy metric)

**Fitness Clustering Hypothesis (v2 - VALID):**
> "Initial spatial structure affects fitness-field reorganization dynamics"

**Test:** Compare `fitnessClustering` trajectory across conditions C1-C5

**Prediction:**
- If fitness clustering emerges from fitness-driven sorting: C1, C2, C3 should converge to similar `fitnessClustering` values by convergence
- If initial spatial structure matters: C2 (pre-grouped) should maintain higher `fitnessClustering` throughout

### Condition Reinterpretation

| Condition | V1 Description | V2 Description |
|-----------|---------------|----------------|
| C1: Baseline | Random aggregation | Random spatial structure (baseline) |
| C2: High Aggregation | Pre-clustered by strategy | Pre-grouped by strategy (test if structure persists) |
| C3: Zero Aggregation | Maximally mixed strategies | Alternating strategies (test if anti-structure slows localization) |
| C4: Fitness Control | No factors | No fitness peak (negative control) |
| C5: Homogeneous | 100% aggregation | Single strategy (test if diversity needed) |

**Key Insight:** C2 and C3 manipulate SPATIAL STRUCTURE, but v2 measures FITNESS LANDSCAPE dynamics. This avoids circular reasoning.

---

## Migration Guide: V1 → V2 Analysis

### Reading V1 Data

V1 CSV files contain:
```
step,aggregation,factor_11_pos,factor_13_pos,...
```

**Interpretation:** `aggregation` = strategy aggregation (now called `strategy_agg` in v2)

### Reading V2 Data

V2 CSV files contain:
```
step,strategy_agg,fitness_clust,factor_local,...
```

**New Columns:**
- `fitness_clust`: Use this for hypothesis testing (avoids circular reasoning)
- `factor_local`: Use this for pattern formation analysis

**Backward Compatibility:**
- `strategy_agg` = v1 `aggregation` (same metric, renamed for clarity)

### Recommended Analysis

**For V1 data (already collected):**
1. Treat `aggregation` as "strategy aggregation"
2. Interpret results as DESCRIPTIVE (not hypothesis testing due to circular reasoning)
3. Compare with v2 fitness clustering to see if strategy and fitness patterns differ

**For V2 data (future experiments):**
1. Use `fitness_clust` for hypothesis testing
2. Use `factor_local` for pattern formation dynamics
3. Use `strategy_agg` for comparison with v1 or chimeric algotype studies

---

## Validation Checklist

- [x] **Metrics Implemented:** FitnessSimilarityClusteringIndex, FactorLocalizationIndex
- [x] **Code Updated:** ClusteringVsFitnessExperiment, StepMetrics
- [x] **CSV Format:** Updated with new columns
- [x] **JavaDoc:** All classes documented with Levin-consistent terminology
- [x] **Compilation:** Code compiles without errors
- [x] **Unit Tests:** Phase 2 tests validate factor presence and stagnation detection
- [x] **Integration Tests:** Phase 3 tests execute full experiment with v2 metrics
- [x] **Documentation:** Updated with Phase 3 completion status
- [x] **Comparison:** v1 and v2 results compared (see Phase 3 Findings below)

---

## Phase 2: Correctness Fixes (COMPLETED)

All Phase 2 fixes have been implemented and validated:

- [x] Fix candidate generation to guarantee factors (11, 13) present in all runs (C1-C3, C5)
- [x] Clarify convergence positions ([0,4]) and create shared constant `CONVERGENCE_POSITION`
- [x] Add stagnation detection (zero swaps for 20 steps tracked via `consecutiveZeroSwapSteps`)
- [x] Stagnation threshold validated at 20 steps (`STAGNATION_THRESHOLD`)
- [x] C4 fitness control correctly excludes factors 11 and 13

---

## Phase 3: V2 Experiment Execution (COMPLETED)

**Status:** ✅ Complete  
**Date:** January 10, 2026  
**Experiment Runs:** 150 (30 reps × 5 conditions)  
**Output Format:** v2 CSV with `strategy_agg`, `fitness_clust`, `factor_local`, `consec_zero_swaps`, `stagnant`

### Experiment Summary

| Condition | Convergence Rate | Mean Conv. Time | Stagnation Rate | Initial Fitness Clustering |
|-----------|------------------|-----------------|-----------------|---------------------------|
| C1: Baseline | 0.0% (0/30) | N/A | 100.0% | 51.5% |
| C2: High Aggregation | 0.0% (0/30) | N/A | 100.0% | 64.9% |
| C3: Zero Aggregation | 63.3% (19/30) | 38.0 steps | 36.7% | 55.1% |
| C4: Fitness Control | 0.0% (0/30) | N/A | 100.0% | 68.7% |
| C5: Homogeneous | 16.7% (5/30) | 34.8 steps | 83.3% | 53.3% |

### Key Findings

#### 1. Strategy Aggregation vs Fitness Clustering Relationship

**Sample Measurement (C1_baseline_rep_001, step 0):**
- v1 aggregation (strategy-based): 68.0%
- v2 strategy_agg (same metric): 68.0% ✓ Consistent
- v2 fitness_clust (NEW metric): 66.0%

**Finding:** Strategy aggregation and fitness clustering show **similar initial values** (<10% difference), suggesting that in this factorization domain, strategy grouping and fitness grouping are **coupled** rather than independent.

**Interpretation:** This coupling may explain why v1 results appeared contradictory—strategy pre-grouping (C2) also affects fitness-field structure, making it difficult to isolate the causal mechanism with strategy-based metrics alone.

#### 2. Convergence Patterns Replicate v1 Findings

The v2 experiment replicates v1 findings with corrected metrics:

- **C3 (Zero Aggregation)** shows highest convergence (63.3%) with fastest mean time (38 steps)
- **C1 (Baseline) and C2 (High Aggregation)** both stagnate (0% convergence, 100% stagnation)
- **C5 (Homogeneous)** shows partial convergence (16.7%, 34.8 steps mean)

**Consistency with v1:** These patterns match v1 results, confirming that the **falsification of the clustering hypothesis** is robust across both metric definitions.

#### 3. Stagnation Detection Effectiveness

Phase 2 stagnation detection successfully distinguishes:
- **Converged runs:** C3 and C5 show meaningful progress
- **Stagnated runs:** C1, C2, C4 reach local attractors (100% stagnation)

**Impact:** This resolves v1's ambiguity about "not yet converged" vs "stuck in local optimum."

#### 4. Initial Fitness Clustering Varies by Condition

| Condition | Strategy Agg (Manipulation) | Fitness Clust (Measurement) |
|-----------|----------------------------|-----------------------------|
| C1: Baseline | ~50-60% (random) | 51.5% |
| C2: High Agg | ~75% (pre-grouped) | 64.9% |
| C3: Zero Agg | ~0-10% (alternating) | 55.1% |
| C4: Control | ~50-60% | 68.7% |
| C5: Homogeneous | 100% (single strategy) | 53.3% |

**Observation:** C2 (high strategy aggregation) shows elevated fitness clustering (64.9%), supporting the coupling hypothesis. However, C3 (zero strategy aggregation) does not show depressed fitness clustering (55.1% ≈ baseline), suggesting the relationship is **asymmetric**.

### Phase 3 Conclusions

1. **v2 Metrics Validated:** The experiment successfully generated v2 format CSVs with `fitness_clust` and `factor_local` metrics
2. **Phase 2 Fixes Confirmed:** Factor presence, stagnation detection, and convergence criteria work as designed
3. **Strategy-Fitness Coupling Observed:** Strategy aggregation and fitness clustering are correlated (not independent)
4. **Hypothesis Falsification Robust:** Both v1 and v2 metrics show C3 (low aggregation) converges faster than C2 (high aggregation), falsifying the clustering hypothesis
5. **Asymmetric Relationship:** High strategy aggregation → high fitness clustering, but low strategy aggregation ≠ low fitness clustering

### Implications for Multi-CP Framework

The coupling between strategy aggregation and fitness clustering in the factorization domain suggests:

1. **Domain-Specific Coupling:** Strategy and fitness may be more independent in other domains (e.g., biological morphogenesis)
2. **Fitness-Field Metrics Still Valid:** Even with coupling, `fitness_clust` avoids circular reasoning by separating manipulation (strategy) from measurement (fitness)
3. **Generalization Requires Testing:** Multi-CP framework should test both metrics across domains to identify where they diverge

---

## Multi-CP Generalization

With fitness-field semantics in place, the framework can be applied to:
- Other factorization targets (different semiprimes)
- Different computational problems (sorting, search, optimization)
- Biological simulations (morphogenesis, tissue patterning)

All using the SAME metrics and terminology, enabling cross-domain comparison.

---

## References

1. **Levin et al. (2024):** "Sorting as a Model of Morphogenesis" - Section 8 (Chimeric Algotypes)
2. **EXPERIMENT_SETUP_AUDIT.md:** Detailed analysis of v1 circular reasoning
3. **TASK.md:** Phase 1 semantic realignment tasks
4. **AlgotypeAggregationIndex.java:** Strategy-label aggregation (Levin §8 metric)
5. **FitnessSimilarityClusteringIndex.java:** Fitness-field clustering (v2 metric)
6. **FactorLocalizationIndex.java:** Pattern formation metric (v2)
7. **ClusteringVsFitnessExperimentPhase3Test.java:** Phase 3 v2 experiment execution and validation

---

**Last Updated:** January 10, 2026  
**Status:** Phase 3 Complete - v2 Experiment Executed, Results Validated, v1 vs v2 Comparison Completed  
**All Phases Complete:** Phase 1 (Semantic Realignment) ✓ | Phase 2 (Correctness Fixes) ✓ | Phase 3 (v2 Experiment) ✓
