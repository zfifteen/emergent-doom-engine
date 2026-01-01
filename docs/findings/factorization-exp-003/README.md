# Factorization Experiment (FACT-EXP-003)

**Experiment ID:** FACT-EXP-003  
**Date:** January 1, 2026  
**Status:** 🔄 RUNNING - Unbalanced vs Balanced Semiprimes  
**Principal Investigator:** GitHub Copilot

---

## Quick Summary

Testing the RemainderCell distributed factorization approach with two contrasting semiprimes at 1e18 scale:
1. **Unbalanced semiprime** with one small factor (71) - should be discoverable
2. **Balanced semiprime** with both factors ~1e9 - no factors in array range

**Key Goal:** Compare performance and convergence behavior between discoverable and non-discoverable factor scenarios.

**Trials:** 100 per target (increased from 30 for robust statistics)

---

## Experimental Design

### Target 1: Unbalanced Semiprime (Discoverable)

**Value:** 1,000,000,000,000,000,091  
**Factorization:** 71 × 14,084,507,042,253,521  
**Factors in range [1, 1000]:**
- Position 1 (trivial)
- **Position 71 (discoverable prime factor)** ✅

**Hypothesis:** System should:
- ✅ Find factor at position 71
- ✅ Converge to highly sorted state
- ✅ Show similar metrics to FACT-EXP-001

### Target 2: Balanced Semiprime (Not Discoverable)

**Value:** 999,999,944,006,315,359  
**Factorization:** 1,000,000,007 × 999,999,937  
**Factors in range [1, 1000]:**
- Position 1 (trivial only)
- **No non-trivial factors** ❌

**Hypothesis:** System should:
- ❌ Find no non-trivial factors
- ⚠️ Still converge (sort by remainder values)
- ⚠️ Possibly lower sortedness (no clear attractor)

---

## Configuration

### Common Parameters
- **Array size:** 1,000 positions
- **Trials:** 100 (increased for statistical robustness)
- **Max steps:** 10,000
- **Convergence criterion:** 3 stable steps
- **Execution mode:** SEQUENTIAL

### Command Lines

**Unbalanced:**
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.examples.FactorizationExperiment \
  1000000000000000091 100
```

**Balanced:**
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.examples.FactorizationExperiment \
  999999944006315359 100
```

---

## Expected Outcomes

### Unbalanced Semiprime Predictions

| Metric | Expected Range |
|--------|---------------|
| Convergence Rate | 95-100% |
| Mean Steps | 1,150-1,300 |
| Sortedness | 98-100% |
| Monotonicity Error | 0.2-0.6 |
| Factor Discovery | Position 71 found |

### Balanced Semiprime Predictions

| Metric | Expected Range |
|--------|---------------|
| Convergence Rate | 80-100% |
| Mean Steps | 1,000-1,500 |
| Sortedness | 90-98% (lower) |
| Monotonicity Error | 0.5-1.5 (higher) |
| Factor Discovery | None (only position 1) |

---

## Research Questions

1. **Does factor discoverability affect convergence rate?**
   - Compare convergence % between unbalanced and balanced

2. **How does the system behave without factors?**
   - Does it still sort by remainder values?
   - Is sortedness lower without clear attractors?

3. **Are there performance differences?**
   - Steps to convergence
   - Execution time
   - Metric distributions

4. **Statistical robustness with 100 trials?**
   - Standard deviations
   - Outlier detection
   - Confidence intervals

---

## Files in This Directory

1. **README.md** (this file) - Experiment overview
2. **unbalanced_exp003_output.txt** - Unbalanced semiprime results
3. **balanced_exp003_output.txt** - Balanced semiprime results
4. **comparison_analysis.md** - Side-by-side comparison (to be created)
5. **EXPERIMENT_COMPLETE.md** - Final summary (to be created)

---

## Comparison with Previous Experiments

| Experiment | Target Type | Target | Factors in Range | Trials |
|-----------|-------------|--------|-----------------|--------|
| FACT-EXP-001 | Unbalanced semiprime | ~1e5 | 1 (position 71) | 5 → 30 |
| FACT-EXP-002 | Highly composite | 1e18 | 28 (powers of 2, 5) | 30 |
| FACT-EXP-003a | **Unbalanced semiprime** | **1e18** | **1 (position 71)** | **100** |
| FACT-EXP-003b | **Balanced semiprime** | **~1e18** | **0 (none)** | **100** |

---

## Code Enhancement

Added support for custom trial count via command line:

```bash
java FactorizationExperiment <target> <trials>
```

This allows flexible statistical analysis with varying sample sizes.

---

## Status

⏳ **Running:** Both experiments executing in background  
📊 **Progress:** Check terminal outputs for real-time status  
⏱️ **Estimated time:** ~30-40 minutes total (100 trials each)

---

## How to Reproduce

```bash
# Navigate to project root
cd /Users/velocityworks/IdeaProjects/emergent-doom-engine

# Build project
mvn clean package -DskipTests

# Run unbalanced semiprime (discoverable factor)
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.examples.FactorizationExperiment \
  1000000000000000091 100

# Run balanced semiprime (no discoverable factors)
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.examples.FactorizationExperiment \
  999999944006315359 100
```

---

## Theoretical Significance

This experiment tests the **factorability hypothesis:**
- Does the system's performance depend on factor existence?
- Or is it purely a sorting algorithm robust to any remainder distribution?

**If unbalanced == balanced performance:**
- System is domain-agnostic (just sorting)
- Factor discovery is emergent property, not optimization target

**If unbalanced >> balanced performance:**
- System optimizes toward factor discovery
- Presence of factors creates stronger attractors
- Morphogenetic organization is goal-directed

---

**Experiment will provide critical insights into the nature of emergent factorization!**

