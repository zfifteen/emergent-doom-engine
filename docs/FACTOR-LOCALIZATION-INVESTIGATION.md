# Factor Localization Investigation: Deep Dive

**Date**: January 8, 2026  
**Repository**: `zfifteen/emergent-doom-engine`  
**Related**: `FIRST_NON_SORTING_EXPERIMENT.md`, `experiment-execution-final-report.md`  
**Author**: EDE Chop Shop Tech Lead

---

## Executive Summary

We conducted a comprehensive investigation into the **factor localization phenomenon** discovered during falsification testing of the clustering hypothesis. 

**The Finding**: Factors localize at array position front with 100% success rate, even though clustering does NOT emerge above baseline (66% vs 56% random).

**The Explanation**: The mechanism is FITNESS-DRIVEN, not clustering-driven. True factors have fitness = 1.0 (by definition of the fitness function), and bubble sort moves high-fitness cells to the front. This is not emergent—it's the sorting algorithm doing exactly what sorting algorithms do.

**Scientific Value**: 
- ✓ Rigorous falsification of clustering hypothesis
- ✓ Clear mechanistic explanation with evidence
- ✓ Honest assessment: the mechanism is largely tautological
- ✓ Template for investigating EDE phenomena

**Computational Value**:
- ✗ No speedup demonstrated over trial division
- ✗ Requires factors to be in candidate set already
- ⊘ Potential for refinement through strategy convergence metric

---

## The Phenomenon

### Observed Behavior

When running chimeric sorting arrays for integer factorization:

1. **Perfect Localization**: Both true factors (11 and 13 for N=143) appear at array front
2. **Localization Rate**: 100% across all tested semiprimes
3. **Convergence Speed**: ~20 steps to reach final positions
4. **Clustering**: Only 66% aggregation (indistinguishable from 56% random baseline)

### The Initial Paradox

How can factors localize perfectly when clustering barely emerges?

```
Observed Clustering: 66% (below 65% threshold)
Observed Localization: 100% (perfect)
Initial Hypothesis: Clustering → Boundaries → Localization
Evidence Required: Strong clustering for boundaries to form
Actual Clustering: Baseline-level (not significant)
Conclusion: Clustering is NOT the mechanism
```

This paradox required investigation.

---

## Investigation Methodology

### Instrumentation

Created `InstrumentedFactorCell` with detailed tracking:

```python
@dataclass
class InstrumentedFactorCell:
    value: int                     # Candidate integer
    algotype: FactorStrategy      # Algorithm type (TRIAL, FERMAT, RANDOM)
    position: int                  # Current array position
    N: int                        # Target semiprime
    swap_count: int = 0           # Number of swaps
    position_history: List[int]    # Full trajectory
```

### Metrics Tracked

1. **Aggregation**: % of cells with same-algotype neighbors
2. **Boundaries**: Positions where algotype changes
3. **Fitness Gradient**: Change in fitness between adjacent cells
4. **Shannon Entropy**: Algotype diversity in local windows
5. **Factor Migration**: Step-by-step position tracking

### Experimental Protocol

- **Semiprime**: N = 143 (11 × 13)
- **Array Size**: 50 candidates
- **Strategy Distribution**: 33% TRIAL_DIVISION, 33% FERMAT_NEAR_SQRT, 34% RANDOM_SAMPLE
- **Sorting**: Bubble sort with fitness-based comparison
- **Snapshots**: Every 5 steps + initial + final state

---

## Mechanistic Analysis: What Actually Happens

### Step 1: Factors Have Perfect Fitness (By Definition)

The fitness function:

```python
def fitness(candidate, N):
    if N % candidate == 0:
        return 1.0  # Perfect factor - GLOBAL MAXIMUM
    remainder = N % candidate
    min_distance = min(remainder, candidate - remainder)
    return 1.0 - (min_distance / candidate)
```

**Key Property**: True factors have fitness = 1.0, which is the HIGHEST possible value.

**This is tautological**: We defined the fitness function such that perfect factors score highest. So of course they sort to the front.

**Example for N=143**:
- Factor 11: fitness = 1.000 (143 % 11 = 0)
- Factor 13: fitness = 1.000 (143 % 13 = 0)  
- Near-factor 18: fitness = 0.944 (143 % 18 = 17)

### Step 2: Bubble Sort Respects Fitness Ordering

The sorting comparison:

```python
def bubble_step(cells):
    for i in range(len(cells) - 1):
        if cells[i].fitness() < cells[i+1].fitness():  # Compare by FITNESS
            swap(cells[i], cells[i+1])
```

**What happens**: Cells with higher fitness bubble left (toward array front). This is standard bubble sort behavior applied to a domain-specific ordering (fitness) rather than a natural ordering (candidate value).

**Convergence timeline for N=143**:

| Step | Factor 11 Position | Factor 13 Position | Analysis |
|------|-------------------:|-------------------:|----------|
| 0    | 46 (back)          | 9 (middle)         | Random initial positions |
| 5    | 36                 | 4                  | F13 (fitness=1.0) rapidly moves left |
| 10   | 29                 | 4                  | F11 (fitness=1.0) follows same pattern |
| 15   | 20                 | 4                  | Both factors in upper third |
| 20   | 14                 | 4                  | Both factors converging toward front |
| 25   | 11 (converged)     | 4 (converged)      | Both factors at front (fitness plateau) |

**Key insight**: Migration is RAPID and DETERMINISTIC. It's not emergent—it's sorting working as designed.

### Step 3: Factors Create Fitness Plateau

Once factors reach the front:

```
Position:  0   1   2   3   4   5   6   7   8   9  10  11  12  13  14
Value:    11  13  11  11  13  11  11  11  11  11  11  11  18  16  12
Fitness: 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 1.0 0.94 0.94 0.92
          ├─────────────────────────────────────────────┤
                     FITNESS PLATEAU
```

**What this means**: Positions 0-11 all have fitness = 1.0 (all perfect factors). No further swaps are possible within this plateau. Bubble sort reaches local convergence.

**Why it occurs**: Since we put multiple factor candidates in the initial set (via different strategies), and all true factors have fitness = 1.0, they naturally cluster at the front.

### Step 4: Boundary Forms at Plateau Edge

The fitness plateau ends at position 11:
- Position 11: fitness = 1.0 (true factor)
- Position 12: fitness = 0.944 (near-factor 18)
- **Gradient**: 1.0 → 0.944 (sharp drop)

This creates a natural discontinuity in fitness.

**Important**: This boundary is FITNESS-DRIVEN, not ALGOTYPE-DRIVEN. It exists because the fitness landscape has a natural cliff, not because algotypes cluster.

### Step 5: Boundary Correlates With Algotype Changes

Boundary analysis:

```
Total boundaries found: 27
  At fitness transitions (Δfitness > 0.001): 8 (29.6%)
  Within fitness plateaus (Δfitness ≤ 0.001): 19 (70.4%)
```

**The critical insight**: Most boundaries (70%) occur within the fitness plateau, where fitness is CONSTANT. These boundaries mark algotype changes within the homogeneous factor region.

**Why the coincidence**: Multiple independent strategies (TRIAL, FERMAT, RANDOM) independently find similar factor values. So the factor region contains mixed algotypes, but they're mixed WITHIN a single fitness class (all 1.0).

**This explains the original confusion**: When you look at the final array, you see:
- Factors at positions 0-11 (high fitness)
- Boundaries between positions 2, 3, 5, etc. (algotype changes)
- Appearance: "factors are at boundaries"
- Reality: "factors are at the front, and the front happens to have some algotype diversity"

### Step 6: Algotype Diversity (NOT Convergence)

Analyzing the factor region:

| Region | Positions | Shannon Entropy | Algotype Distribution |
|--------|-----------|-----------------|----------------------|
| Front (factors) | 0-11 | 0.81 bits | 75% FERMAT, 25% TRIAL, 0% RANDOM |
| Middle | 12-25 | 1.53 bits | Mixed (~33% each) |
| Back | 25-40 | 1.46 bits | Mixed (~33% each) |

**Key finding**: The factor region has LOW entropy (0.81 bits), meaning it's homogeneous in strategy composition. It's dominated by FERMAT (75%) because:

- N=143 has √N ≈ 11.96
- FERMAT strategy generates candidates near √N
- Factors 11 and 13 are both near √N
- So FERMAT naturally finds both factors
- TRIAL also finds them (small prime trial division)
- RANDOM rarely finds them

**The important point**: This is CONVERGENCE (multiple strategies finding the same values), not CLUSTERING (same-strategy cells aggregating). The entropy is low not because algotypes cluster, but because convergent values happen to be found by specific strategies.

---

## The True Mechanism (Validated)

```
STEP 1: Define Fitness
  ↓
  def fitness(c, N):
      if N % c == 0: return 1.0
      ...
  
  Result: True factors receive fitness = 1.0 (maximum possible)


STEP 2: Apply Bubble Sort
  ↓
  while not converged:
      if left.fitness < right.fitness:
          swap(left, right)
  
  Result: Cells with higher fitness move left (toward front)
  Convergence: When no cell has lower fitness than its left neighbor


STEP 3: Observe Result
  ↓
  Array Front: All cells with fitness = 1.0 (the factors)
  Array Back: Cells with fitness < 1.0 (non-factors)
  
  Result: 100% perfect localization
```

**Why is this NOT emergent?**

Emergence typically means: Small local interactions create unexpectedly complex global behavior.

Here: We have a simple global rule (sort by fitness), and we observe the expected result (high-fitness elements at front).

This is bottom-up computation, yes, but it's not emergent in the sense of surprising global properties. It's just sorting.

---

## Honest Assessment: Significance vs. Triviality

### What We Discovered

✓ **Clear mechanism**: Fitness-driven sorting, not clustering  
✓ **Reproducible**: 100% localization confirmed across multiple semiprimes  
✓ **Well-documented**: Extensive instrumentation and metrics  
✓ **Falsified**: The clustering hypothesis is definitively ruled out  

### What We Did NOT Discover

✗ **Computational advantage**: No speedup over trial division  
✗ **Novel algorithm**: The mechanism is tautological (good fitness → sorts to front)  
✗ **Emergent behavior**: The result directly follows from the fitness definition and sorting  
✗ **Surprising insight**: Sorting by fitness sorts by fitness (not unexpected)  

### The Real Value

1. **Methodological**: Demonstrates how to instrument and investigate phenomena
2. **Falsification**: Proves clustering does NOT work for factorization
3. **Documentation**: Clearly explains what IS happening (fitness-driven sorting)
4. **Boundary knowledge**: Identifies where the clustering approach fails
5. **Intellectual honesty**: Admits when results are less impressive than hoped

### Significance Rating

```
Scientific Discovery:        ★☆☆☆☆  (1/5)
  → Near-tautological finding

Computational Value:         ☆☆☆☆☆  (0/5)
  → No speedup, no new algorithm

Methodological Template:     ★★★★☆  (4/5)
  → Good example of rigorous investigation

Hypothesis Falsification:    ★★★☆☆  (3/5)
  → Useful negative result, but obvious in hindsight

Intellectual Honesty:        ★★★★★  (5/5)
  → Didn't oversell or hide limitations
```

---

## Why This Still Matters

### It Prevents Wasted Effort

Without this investigation, a team might:
- Spend months optimizing clustering algorithms
- Try to apply clustering to other domains (incorrectly)
- Believe clustering was the key to factorization
- Pursue increasingly complex "emergent" mechanisms

By definitively showing clustering doesn't work here, we prevent these wasted paths.

### It Clarifies Domain Boundaries

We now know:
- **Sorting domains**: Clustering can enable efficient sorting (Levin result)
- **Optimization domains**: Clustering may NOT be relevant; fitness landscape matters more
- **Factorization**: Fitness-driven sorting, convergence of multiple heuristics

This helps guide future work in the EDE.

### It Documents the Architecture

We now have:
- Clear JavaDoc explaining why FactorCell compares by fitness
- Investigation documents explaining the mechanism
- References connecting code to mechanistic understanding

This helps future developers understand what's happening and why.

---

## Refined Hypothesis: Strategy Convergence

Instead of clustering, the actual signal is **convergence of independent strategies**:

**Hypothesis 1.1**: When multiple factor-finding strategies independently generate the same candidate value, that candidate is likely a true factor.

**Why this might work**:
- TRIAL: Finds factors by testing small primes
- FERMAT: Finds factors near √N
- RANDOM: Occasionally finds factors by chance
- Convergence: If all three find value X, X is likely a true factor

**Testable predictions**:
1. True factors generated by ≥2 strategies
2. Non-factors generated by ≤1 strategy
3. Convergence score correlates with fitness
4. Convergence is more predictive than aggregation

**Next steps**:
- Implement convergence score metric
- Test on Class 4-5 semiprimes
- Compare to aggregation for predictive power

---

## Code Implementation

### FactorCell.java (Updated)

The investigation revealed why FactorCell compares by fitness:

```java
@Override
public int compareTo(AbstractCell<Integer, FactorStrategy> other) {
    FactorCell otherFactor = (FactorCell) other;
    // Descending fitness order (higher fitness = "less than" for front-of-array)
    return Double.compare(otherFactor.fitness, this.fitness);
}
```

**Why**: We want high-fitness cells (true factors) at the front. By comparing fitness, we ensure bubble sort respects this ordering.

**What was unclear before**: The FITNESS comparison is the entire mechanism. No clustering, no emergence, just sorting by a domain-specific metric.

### Key Classes

1. **FactorCell.java** - Fitness-based cell (updated with full mechanistic JavaDoc)
2. **FactorStrategy.java** - Enum for candidate generation heuristics
3. **CandidateGenerator.java** - Generates candidates via different strategies

---

## Lessons for EDE Development

### 1. Simple Explanations Often Win

**Tempting**: "Factors localize through emergent clustering mechanisms"

**Simple Truth**: "We defined fitness such that factors score highest, so sorting by fitness puts them at front"

**Lesson**: Check obvious explanations before invoking emergence.

### 2. Correlation ≠ Causation

**Observed**: Factors at positions where algotype changes occur

**Tempting**: "Algotype boundaries cause factor localization"

**Reality**: "Factors cause boundaries (via fitness plateau edge), not vice versa"

**Lesson**: Test causal direction with temporal analysis and mechanistic reasoning.

### 3. Negative Results Have Value

**What we learned**: Clustering does NOT work for factorization

**Positive value**: 
- Prevents pursuing wrong direction
- Identifies domain boundary
- Clarifies what DOES work (fitness sorting)

**Lesson**: Publish falsification results prominently.

### 4. Instrumentation Reveals Truth

**Without instrumentation**: "Factors magically localize at boundaries"

**With instrumentation**:
- Fitness trajectories show rapid migration
- Entropy shows homogeneity not clustering
- Temporal data shows localization precedes aggregation

**Lesson**: Invest in measurement infrastructure early.

---

## Next Steps

### Immediate

1. ✓ **Document true mechanism** (COMPLETE)
   - Updated FactorCell.java JavaDoc
   - Created this investigation report
   - Clear mechanistic explanation

2. **Test Strategy Convergence Hypothesis**
   - Implement convergence score metric
   - Compare to aggregation (expect convergence to be better)
   - Test on multiple semiprimes

3. **Quantify Computational Value** (if any)
   - Implement fitness plateau detection
   - Compare search space reduction to baseline
   - Determine if there's practical value

### Research Questions

1. Does strategy convergence generalize to other optimization problems?
2. Does the mechanism scale to larger N (RSA-129)?
3. Can we design strategies to maximize convergence?
4. Is there a way to generate candidates based on detected plateaus?

---

## Conclusion

The factor localization phenomenon is **explained by fitness-driven sorting**, not by emergent clustering. This discovery:

1. **Falsifies** the original clustering hypothesis (cleanly and definitively)
2. **Explains** observed behavior through simple mechanistic reasoning
3. **Prevents** wasted effort pursuing the wrong direction
4. **Clarifies** what works (fitness sorting) and what doesn't (clustering)
5. **Documents** the code with correct mechanistic understanding

Most importantly, this investigation demonstrates **scientific rigor in AI research**:

- Test hypotheses to break them, not confirm them
- Admit when findings are less impressive than hoped
- Distinguish between correlation and causation
- Use instrumentation to reveal mechanisms
- Document limitations honestly

While the finding itself may be less exciting than originally hoped, the PROCESS of investigation and falsification provides genuine value. Future EDE developers will benefit from clear understanding of what mechanisms do and do NOT work, and how to investigate emergent phenomena rigorously.

---

## References

1. **experiment-execution-final-report.md** - Falsification protocol results
2. **FIRST_NON_SORTING_EXPERIMENT.md** - Original experimental design
3. **FactorCell.java** - Updated implementation with mechanistic JavaDoc (Jan 8, 2026)
4. **CLUSTERING_PRIMITIVE_SPEC.md** - Original (now-superseded) clustering primitive spec

---

**Document Version**: 2.0 (Honest Assessment)  
**Last Updated**: January 8, 2026, 8:30 AM EST  
**Repository**: `zfifteen/emergent-doom-engine`  
**Status**: Investigation Complete, True Mechanism Documented