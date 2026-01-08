# Factor Localization: The Simple Truth

**Quick Reference for Developers**

---

## What Happens

When you run factorization sorting with FactorCell, true factors (like 11 and 13 for N=143) end up at the array front with 100% reliability.

## Why It Happens

Three simple facts:

1. **True factors have fitness = 1.0**
   ```java
   if (N % candidate == 0) return 1.0;  // Perfect factor
   ```
   This is the definition of the fitness function. True factors are mathematically defined to have maximum fitness.

2. **FactorCell compares by fitness, not value**
   ```java
   return Double.compare(otherFactor.fitness, this.fitness);  // Compare FITNESS
   ```
   When bubble sort runs, it respects fitness ordering.

3. **Bubble sort bubbles maximal elements to front**
   ```
   while not converged:
       if left.fitness < right.fitness:
           swap(left, right)
   ```
   This is what bubble sort does. High values move left.

## The Result

```
Before sorting:  [18, 11, 22, 13, 8, 19, 15, 11, ...]
After sorting:   [11, 13, 11, 11, 18, 22, 19, 15, ...]
                  ^^^^^^^^^^^^^^^
                  All fitness = 1.0 (true factors)
```

## Why This Is NOT Emergent

**Emergent behavior**: Small local rules create unexpectedly complex global patterns.

**What we have**: A direct application of sorting theory to a domain-specific metric (fitness rather than natural value ordering).

**The global behavior** (factors at front) directly follows from:
- The fitness definition
- The comparison function
- The sorting algorithm

No surprise. No emergence. Just math.

## Why Clustering Does NOT Work Here

**Key observation**: Peak aggregation reaches only 66%, yet localization is 100% perfect.

This proves clustering is not the mechanism:
- If clustering drove localization, we'd expect high clustering for high localization
- Instead, we have baseline clustering but perfect localization
- Therefore, some other mechanism must be at work

**The mechanism**: Fitness-driven sorting, not clustering.

## What Does Cluster (Coincidentally)

Multiple strategies independently find similar factor values:
- **FERMAT**: Generates candidates near √N
- **TRIAL**: Tests small primes
- **RANDOM**: Generates random candidates

For N=143:
- √N ≈ 11.96
- True factors: 11, 13 (both near √N)
- FERMAT finds: 11, 13 (naturally near sqrt)
- TRIAL finds: 11, 13 (small primes)
- RANDOM finds: 11, 13 (with low probability)

**Result**: Multiple strategies independently find the same factors. This is CONVERGENCE, not CLUSTERING.

**The coincidence**: When multiple strategies find the same values, those values concentrate in the factor region. This looks like clustering, but it's really convergence of independent heuristics.

## Key Numbers

| Metric | Value | Interpretation |
|--------|-------|----------------|
| Localization Rate | 100% | Perfect (both factors at front) |
| Aggregation Peak | 66% | Only ~66% of cells have same-algotype neighbors |
| Random Baseline | 56% | Expected aggregation if algotypes were random |
| Difference | 10% | Statistically insignificant |

**Conclusion**: Clustering (only 66% vs 56% baseline) cannot explain perfect localization (100%).

## For Code Developers

### When You See This

You might wonder: "Why does FactorCell compare by fitness instead of candidate value?"

### The Answer

Because the factorization domain has a problem-specific ordering:
- **Natural order**: 11 < 13 < 18 < 22 (by numeric value)
- **Fitness order**: 1.0 (factor) > 0.944 (near-factor) > 0.5 (non-factor)

We want to sort by fitness order, not natural order. So we override compareTo().

### Why This Matters

```java
// If we compared by value:
if (11 < 18)  // TRUE - would keep them in order by value
    // 11 stays left of 18

// But we compare by fitness:
if (1.0 > 0.944)  // TRUE - moves high-fitness left
    // 11 bubbles to front (correct)
```

Comparing by fitness ensures the algorithm optimizes for the problem (factorization) rather than for natural numeric ordering.

## What's NOT Explained By This

- Why other domains might use clustering
- How to extend this to larger semiprimes
- Whether fitness-driven sorting generalizes
- How to generate candidates if factors aren't in the initial set

Those are separate questions requiring separate investigation.

## Further Reading

- `FACTOR-LOCALIZATION-INVESTIGATION.md` - Full technical analysis
- `FactorCell.java` - Implementation with detailed JavaDoc
- `FIRST_NON_SORTING_EXPERIMENT.md` - Original experimental design

---

**TL;DR**: True factors have maximum fitness by definition. Sorting by fitness puts maximum-fitness elements at the front. This is not emergent behavior—it's the sorting algorithm doing exactly what it's designed to do.

**Key Insight**: Confusing the symptom (algotype changes at factor boundary) with the cause (fitness-driven sorting). The boundary exists because the fitness landscape has a discontinuity, not because algotypes cluster.

**Scientific Value**: Falsifies clustering hypothesis, proves fitness-driven sorting explains observed behavior, prevents pursuing wrong directions in future research.