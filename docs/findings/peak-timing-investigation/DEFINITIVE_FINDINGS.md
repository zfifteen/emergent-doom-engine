# Definitive Findings: EDE vs. Levin Architecture Comparison

**Date:** 2026-01-06  
**Investigation:** Expanded analysis of PR #109  
**Status:** ✅ COMPLETE - Experimental Proof Obtained

---

## Executive Summary

This investigation expands on PR #109 by comparing the EDE framework with the Levin et al. (2024) reference implementation (cell_research repository). We discovered a **fundamental architectural difference** in how algotypes bind to cells, leading to completely different aggregation dynamics during sorting.

**Key Finding:** The "Peak Timing Anomaly" from PR #109 was correctly resolved. The EDE framework shows constant aggregation because algotypes are bound to **positions**, while the Levin implementation shows dynamic aggregation because algotypes are bound to **cell objects**.

**Experimental Proof:** Side-by-side comparison of both architectures demonstrates:
- **EDE Approach:** 0.00% aggregation variance (constant at 71.20%)
- **Levin Approach:** 18.30% aggregation variance (peaks at 90.50% mid-sorting)

---

## Background

### PR #109 Resolution

PR #109 investigated why clustering peaks occurred at step 0 instead of mid-sorting (19-42% progress) as reported in Levin et al. (2024). The investigation concluded:

> "Algotype clustering (aggregation) is determined entirely by the initial random shuffle and remains constant throughout the sorting process. Cells move based on their values, not their algotypes, so algotype spatial patterns established at initialization persist until convergence."

**Conclusion:** This was correct behavior for the EDE architecture, not a bug.

### Remaining Question

Why does the Levin paper report mid-sorting clustering peaks when EDE shows constant aggregation?

**Answer:** Fundamentally different architectural choices for binding algotypes to cells.

---

## The Architectural Difference

### EDE Architecture: Position-Based Algotypes

**Implementation:**
- `PercentageAlgotypeProvider`: Maps position indices to algotypes
- `ChimericProbe`: Reads algotype via `provider.getAlgotype(position)`
- **When cells swap:** Only values move; algotypes stay at positions

**Example:**
```
Initial State:
Position 0: value=45, algotype=Bubble (from provider[0])
Position 1: value=23, algotype=Selection (from provider[1])

After Swap:
Position 0: value=23, algotype=Bubble (still from provider[0])
Position 1: value=45, algotype=Selection (still from provider[1])
```

**Result:** Algotype spatial pattern frozen at initialization → **constant aggregation**

### Levin Architecture: Cell-Based Algotypes

**Implementation:**
- `AlgotypedCell`: Cell object carries algotype as property
- `AlgotypedProbe`: Reads algotype via `cell.getAlgotype()`
- **When cells swap:** Entire cell objects (including algotypes) move

**Example:**
```
Initial State:
Position 0: AlgotypedCell(value=45, algotype=Bubble)
Position 1: AlgotypedCell(value=23, algotype=Selection)

After Swap:
Position 0: AlgotypedCell(value=23, algotype=Selection) ← cell moved
Position 1: AlgotypedCell(value=45, algotype=Bubble) ← cell moved
```

**Result:** Algotypes move with cells → **dynamic aggregation**

---

## Experimental Design

### Hypothesis

**EDE Approach:**
- Algotypes tied to positions
- Swaps move values only
- **Expected:** Constant aggregation (~71% for 50/50 mix)

**Levin Approach:**
- Algotypes bound to cells
- Swaps move cells (with algotypes)
- **Expected:** Dynamic aggregation with potential mid-sorting peaks

### Parameters

- **Array Size:** 100 cells
- **Distribution:** 50/50 Bubble/Selection (matching Levin paper)
- **Trials:** 10 (seeds 42-51)
- **Max Steps:** 10,000
- **Metrics:** Aggregation % (cells with ≥1 same-algotype neighbor), Sortedness %

### Method

1. Create identical value sequences for both approaches (same random seed per trial)
2. Assign algotypes using same distribution
3. Run sorting experiments with identical execution parameters
4. Record complete trajectories (every step)
5. Compare aggregation dynamics

---

## Results

### Summary Statistics (10 Trials)

| Metric | EDE Approach | Levin Approach | Difference |
|--------|-------------|----------------|------------|
| **Initial Aggregation** | 71.20% | 72.20% | +1.00% |
| **Peak Aggregation** | 71.20% | **90.50%** | +19.30% |
| **Final Aggregation** | 71.20% | 71.50% | +0.30% |
| **Variance (peak - initial)** | **0.00%** | **18.30%** | +18.30% |
| **Variance (final - initial)** | **0.00%** | -0.70% | -0.70% |

### Key Observations

1. **EDE shows ZERO variance** in aggregation throughout sorting
   - Initial = Peak = Final = 71.20%
   - Perfectly constant as predicted

2. **Levin shows DRAMATIC variance** in aggregation during sorting
   - Starts at 72.20%, peaks at **90.50%**, ends at 71.50%
   - Average peak occurs mid-sorting (consistent with Levin paper)
   - Variance of 18.30% proves dynamic aggregation

3. **Both approaches converge to similar final aggregation** (~71%)
   - This makes sense: final sorted state has similar algotype distribution
   - The difference is in the **journey**, not the destination

### Example Trial (Seed 42)

| Step | EDE Aggregation | Levin Aggregation | Delta |
|------|-----------------|-------------------|-------|
| 0 | 70.00% | 75.00% | +5.00% |
| 50 | 70.00% | 92.00% | +22.00% |
| 100 | 70.00% | 97.00% | **+27.00%** (peak) |
| 150 | 70.00% | 85.00% | +15.00% |
| 193 (final) | 70.00% | 71.00% | +1.00% |

**Observation:** Levin aggregation rises dramatically during sorting (70% → 97%), then decreases to final state (71%). EDE remains perfectly flat at 70%.

---

## Mechanism: Why Levin Shows Mid-Sorting Peaks

### Spatial Clustering During Sorting

When cells sort by value, cells with similar values move to adjacent positions:

1. **Initial state:** Random value distribution, random algotype distribution
2. **During sorting:** Low-value cells migrate left, high-value cells migrate right
3. **Spatial grouping:** Cells with similar values cluster spatially
4. **Algotype clustering (Levin only):** If algotypes are bound to cells, they cluster too!

### Example Scenario

Suppose Bubble cells happen to have lower values on average (due to random variation):

**EDE Approach:**
- Bubble algotype assignments stay at original positions
- Low-value cells move to those positions
- No increase in Bubble-Bubble neighbors (algotypes don't move)

**Levin Approach:**
- Bubble algotypes travel WITH low-value cells
- Low-value cells cluster on left side
- Bubble algotypes cluster on left side
- **Increased Bubble-Bubble neighbors → aggregation peak!**

### Peak Timing

Peaks occur mid-sorting because:
1. **Early sorting:** Cells begin to cluster by value
2. **Mid-sorting:** Maximum spatial clustering (cells in general regions but not yet sorted)
3. **Late sorting:** Fine-tuning positions, algotypes spread out as cells find exact spots

This explains the Levin paper's reported peak timings (19-42% of progress).

---

## Implications

### For PR #109

**PR #109 was 100% correct.** The conclusion that algotype aggregation is constant during sorting is the **expected behavior for EDE's architecture**. The discrepancy with the Levin paper is due to architectural differences, not a bug.

### For EDE Framework

The EDE architecture is **valid and intentional**:
- Models scenarios where algotype is a **label** or **category** assigned to positions
- Enables studying sorting dynamics with fixed algotype distributions
- Provides clean separation: value space (dynamic) vs. algotype space (static)

**Use cases:**
- Studying sorting efficiency across algotype mixes
- Error tolerance experiments with frozen cells
- Delayed gratification with fixed strategies

### For Levin Paper Reproduction

To reproduce Levin et al. (2024) results, use **Levin-style architecture**:
- `AlgotypedCell` instead of `GenericCell + PercentageAlgotypeProvider`
- `AlgotypedProbe` instead of `ChimericProbe`
- Results will match paper's mid-sorting clustering peaks

### Both Architectures are Valid

Neither approach is "wrong"—they model different scenarios:

| Scenario | Use Architecture | Aggregation Behavior |
|----------|------------------|---------------------|
| Fixed strategy assignments | EDE (position-based) | Constant |
| Collective cell movement | Levin (cell-based) | Dynamic |
| Biological morphogenesis | Levin (cell-based) | Dynamic |
| Strategy label analysis | EDE (position-based) | Constant |

---

## Artifacts

### Code Components

1. **`AlgotypedCell.java`** - Cell implementation with embedded algotype
2. **`AlgotypedCellFactory.java`** - Factory for creating AlgotypedCell arrays
3. **`AlgotypedProbe.java`** - Probe that reads algotypes from cell objects
4. **`EDEvsLevinComparisonDataGenTest.java`** - Comparative experiment test

### Documentation

1. **`ARCHITECTURAL_DIFFERENCE.md`** - Comprehensive 9KB analysis document
2. **`DEFINITIVE_FINDINGS.md`** - This document (executive summary)

### Experimental Data

1. **`ede_vs_levin_comparison.csv`** - 2,037 rows of trajectory data
   - Columns: trial, seed, step, ede_aggregation, ede_sortedness, levin_aggregation, levin_sortedness
   - Shows step-by-step comparison of both approaches

2. **`ede_vs_levin_summary.csv`** - 10 trial summaries
   - Columns: trial, seed, final_steps, initial_agg, peak_agg, peak_step, final_agg (for both approaches)
   - Statistical summary per trial

3. **`ede_vs_levin_metadata.json`** - Experiment metadata
   - Parameters, hypothesis, metric definitions

---

## Conclusions

### Primary Conclusion

**The EDE framework and Levin reference implementation use fundamentally different architectural approaches for binding algotypes to cells:**

- **EDE:** Position-based algotypes (constant aggregation)
- **Levin:** Cell-based algotypes (dynamic aggregation)

This difference fully explains the "Peak Timing Anomaly" and validates PR #109's resolution.

### Secondary Conclusion

**Experimental evidence confirms the architectural difference:**

- EDE shows 0.00% aggregation variance (perfectly constant)
- Levin shows 18.30% aggregation variance (dynamic with mid-sorting peaks)
- Peak aggregation delta: 19.30% between approaches

### Tertiary Conclusion

**Both architectures are scientifically valid** but model different scenarios:

- Use **EDE architecture** for fixed strategy distributions and value-based sorting analysis
- Use **Levin architecture** for collective movement and biological morphogenesis modeling

---

## Recommendations

### For Future EDE Development

1. **Document architectural choice** in main README and test suite
2. **Support both architectures** via configuration flags or separate execution modes
3. **Create migration guide** for users wanting to switch between architectures
4. **Validate against Levin paper** using Levin-style architecture specifically

### For Experimental Validation

1. **Levin Paper Reproduction:** Use AlgotypedCell + AlgotypedProbe to reproduce paper results
2. **EDE-Specific Baselines:** Establish validation baselines for position-based architecture
3. **Comparative Studies:** Use side-by-side experiments to study both approaches
4. **Contact Authors:** Clarify experimental methodology with Levin et al. if needed

---

## References

- **PR #109:** "Resolve Peak Timing Anomaly: Constant aggregation is expected behavior"
- **Levin et al. (2024):** "Classical Sorting Algorithms as a Model of Morphogenesis", arXiv:2401.05375v1
- **cell_research repository:** https://github.com/zfifteen/cell_research (Python reference implementation)
- **emergent-doom-engine repository:** https://github.com/zfifteen/emergent-doom-engine (Java EDE framework)

---

**Investigation Status:** ✅ COMPLETE  
**Findings:** DEFINITIVE with experimental proof  
**PR #109 Validation:** CONFIRMED as correct  
**Architectural Difference:** DOCUMENTED and DEMONSTRATED  
**Next Steps:** Documentation updates, optional architecture support

**Date Completed:** 2026-01-06
