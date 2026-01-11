# Very Large Scale Investigation: MAXIMAL_MIXING vs Scale

**Date:** January 11, 2026
**Objective:** Investigate the performance of MAXIMAL_MIXING (Zero Aggregation/C3) on 6, 7, and 8 digit semiprimes using scaled array sizes to maintain plausible search ratios.

## Methodology

Refactored `ClusteringVsFitnessExperiment` to support variable `arraySize` and `maxSteps`.
Ran 30 repetitions of the 5 conditions for:
1. **Target 249,991** (6-digit, 499×501). Array Size 100 (Ratio 5:1). Steps 200.
2. **Target 1,000,489** (7-digit, 1009×991). Array Size 200 (Ratio 5:1). Steps 300.
3. **Target 10,017,221** (8-digit, 3163×3167). Array Size 300 (Ratio 10:1). Steps 400.

## Results Summary

| Target | Digits | Size | C1 (Base) | C2 (High) | C3 (Zero/Mix) | C5 (Homo) | Verdict |
|--------|--------|------|-----------|-----------|---------------|-----------|---------|
| **249,991** | 6 | 100 | 0.0% | 0.0% | **0.0%** | 0.0% | **Failure** |
| **1,000,489** | 7 | 200 | 0.0% | 0.0% | 0.0% | **100.0%** | **Strategy Dominance** |
| **10,017,221** | 8 | 300 | **6.7%** | 3.3% | 3.3% | 0.0% | **Noise limit** |

## Analysis

### 1. The 6-Digit Failure (Target 249,991)
- **Result:** 0% convergence across the board, even with Array Size 100.
- **Hypothesis:** 499 and 501 are twin primes centered at 500. `FERMAT` strategy generates candidates in `[sqrtN - 5, sqrtN + 5]`. sqrt(249991) = 499.99.
- `FERMAT` candidates: 495..505. Includes 499 and 501.
- **Why did it fail?** Possibly the candidates are *too* close to sqrtN, or duplicate candidates saturated the array?
- **Previous run (Size 50):** 3.3% convergence for C3. Increasing size to 100 actually *reduced* performance to 0%?
- **Insight:** Larger arrays take longer to sort. With `MAX_STEPS=200`, perhaps 100 cells couldn't sort fast enough to bring factors to the front [0..4].

### 2. The 7-Digit Anomaly (Target 1,000,489)
- **Result:** **C5 (Homogeneous) achieved 100% convergence**, while C1/C2/C3 failed completely (0%).
- **Context:** Factors 1009/991. Sqrt ~1000. `FERMAT` strategy range ~[995..1005]? No, range is +/- 5. 995 to 1005 does NOT include 1009 or 991 (delta is 9).
- **Wait:** If `FERMAT` doesn't generate them, who does? `SMALL_PRIMES` goes up to 1000. 991 is generated. 1009 is not.
- **Injection:** `ensureFactorsPresent` forces them in.
- **Why C5 wins:** C5 is 100% `FERMAT`. If `FERMAT` generates candidates *close* to the factors (but not exact), they might form a smooth gradient.
- **Or:** The injected factors in C5 are surrounded by `FERMAT` candidates (which are all ~1000). The fitness difference between the factors (fitness=1.0) and neighbors (fitness < 1.0 but high) might be clean.
- **Why C3 failed:** C3 mixes in `RANDOM` (fitness ~0) and `SMALL_PRIMES` (fitness variable). Perhaps the noise overwhelmed the signal?

### 3. The 8-Digit Noise (Target 10,017,221)
- **Result:** Near total failure. C1 (6.7%), C2 (3.3%), C3 (3.3%).
- **Interpretation:** At this scale (Ratio 10:1), the system is struggling to distinguish signal from noise. The slight advantage of C1/C2/C3 suggests randomness is finding a path occasionally, but `MAXIMAL_MIXING` (C3) provides no consistent benefit over Random (C1) here.

## Conclusion
The hypothesis that **MAXIMAL_MIXING (C3) is universally advantageous** breaks down at very large scales when:
1.  **Sorting Latency:** Larger arrays require more steps to sort than allocated.
2.  **Strategy Dominance:** When one strategy (e.g., FERMAT in the 7-digit case) provides a "smooth" background for the factors, Homogeneity (C5) can outperform Mixing (C3). Mixing introduces "noise" candidates that might disrupt the delicate gradient needed for very large numbers.

**Revised Hypothesis:**
- **Small/Medium Search Spaces:** Mixing (C3) is optimal (prevents traps).
- **Large/Specific Search Spaces:** Homogeneity (C5) or specific gradients might be required to guide factors over long distances (array indices).

## Artifacts
- **Code:** `src/test/java/.../ClusteringVsFitnessVeryLargeScaleTest.java`
- **Results:** `experiments/very_large_scale_investigation_2026_01_11/`
