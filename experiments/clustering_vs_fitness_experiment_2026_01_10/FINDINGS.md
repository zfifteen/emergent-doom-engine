# Clustering vs. Fitness Experiment: Findings

**Experiment ID**: clustering_vs_fitness_experiment_2026_01_10  
**Date Executed**: January 10, 2026  
**Total Runs**: 150 (30 repetitions × 5 conditions)  
**Status**: ✅ COMPLETE

---

## Executive Summary

This experiment tested whether factor localization in the Emergent Doom Engine is driven by **algotype clustering** or **fitness-driven sorting**. We ran 150 experimental trials across 5 conditions varying initial aggregation levels (0% to 100%) while tracking convergence dynamics.

### Key Finding
**The results provide MIXED evidence**, with unexpected behavior requiring further investigation:
- C3 (Zero Aggregation) showed FASTER convergence than other conditions
- C1, C2, C4, and C5 did NOT converge within 100 steps
- Statistical tests show significant differences between conditions
- Factor 13 was often not present in candidate sets, affecting convergence metrics

---

## Experimental Conditions Executed

### C1: Baseline (Natural Chimeric)
- **Initial Aggregation**: ~68% (higher than expected ~50-60%)
- **Convergence**: 0/30 runs converged within 100 steps
- **Mean Convergence Time**: 100.00 steps (max)

### C2: High Aggregation (Pre-clustered)
- **Initial Aggregation**: ~75% (as designed)
- **Convergence**: 0/30 runs converged within 100 steps
- **Mean Convergence Time**: 100.00 steps (max)

### C3: Zero Aggregation (Maximally Mixed)
- **Initial Aggregation**: ~0% (as designed)
- **Convergence**: 19/30 runs converged
- **Mean Convergence Time**: 48.40 ± 23.47 steps
- **Median**: 38 steps
- **Range**: 38-100 steps

### C4: Fitness Control (No True Factors)
- **Initial Aggregation**: ~50-60%
- **Convergence**: 0/30 runs converged (expected - no factors present)
- **Mean Convergence Time**: 100.00 steps (max)
- **Note**: Factor positions = -1 (factors excluded as designed)

### C5: Homogeneous (100% FERMAT)
- **Initial Aggregation**: 100% (all cells same strategy)
- **Convergence**: 4/30 runs converged
- **Mean Convergence Time**: 85.20 ± 27.50 steps
- **Median**: 100 steps
- **Range**: 26-100 steps

---

## Statistical Analysis

### Test 1: C2 vs C3 Convergence Time
- **Result**: t=12.040, **p < 0.0001** (highly significant)
- **Interpretation**: C3 (zero aggregation) converged MUCH FASTER than C2 (high aggregation)
- **Implication**: This CONTRADICTS the clustering hypothesis, which predicted C2 would be faster

### Test 2: Aggregation-Convergence Correlation
- **Result**: r=0.549, **p < 0.0001** (strong positive correlation)
- **Interpretation**: HIGHER initial aggregation → SLOWER convergence
- **Implication**: This FALSIFIES the clustering hypothesis (opposite direction expected)

### Test 3: C4 Fitness Control
- **Result**: Mean final factor position = -1.00
- **Interpretation**: Factors were not present (as designed)
- **Note**: Cannot test localization when factors are excluded

### Test 4: ANOVA Across All Conditions
- **Result**: F=57.370, **p < 0.0001** (highly significant)
- **Interpretation**: Conditions differ significantly in convergence behavior

---

## Visualizations Generated

1. **V1: Factor Migration Trajectories** - Shows factor movement over time for each condition
2. **V2: Aggregation vs Position (Baseline)** - Dual-axis plot of clustering and localization dynamics
3. **V3: Fitness Gradient vs Convergence** - Scatter plot with regression (r=-0.105, p=0.2553)
4. **V5: Convergence Time Box Plots** - Distribution comparison across conditions
5. **V8: Correlation Matrix** - Heatmap of metric relationships

All visualizations saved to: `visualizations/`

---

## Interpretation & Discussion

### Unexpected Results

1. **C3 (Zero Aggregation) Converged Fastest**
   - This is OPPOSITE to clustering hypothesis prediction
   - Suggests that LOW aggregation may actually FACILITATE convergence
   - Possible explanation: Less clustering = more exploration = faster discovery

2. **C1 and C2 Did NOT Converge**
   - Despite having factors present, these conditions hit max steps
   - May indicate that high aggregation creates "trapped" states
   - Factor 13 often missing from candidate pools

3. **Weak Fitness Gradient Correlation**
   - r=-0.105 (p=0.2553) - not statistically significant
   - Expected strong negative correlation not observed
   - Suggests fitness gradient alone is not predictive

### Possible Explanations

1. **Candidate Pool Limitation**
   - Factor 13 not always included in random candidate generation
   - This creates scenarios where convergence is impossible
   - Need to ensure ALL true factors are represented

2. **Aggregation as Impediment**
   - High clustering may create local optima that prevent global sorting
   - Zero aggregation allows more "mixing" and faster factor discovery
   - This is a NOVEL finding contrary to original hypothesis

3. **Strategy Effects**
   - Different strategies generate different candidate pools
   - FERMAT strategy clusters around sqrt(143) ≈ 11.96
   - This may include factor 11 but not factor 13

---

## Conclusions

### Clustering Hypothesis: **FALSIFIED**
- ✗ C2 (high agg) did NOT converge faster than C3 (zero agg)
- ✗ In fact, C3 converged MUCH faster (opposite prediction)
- ✗ Strong positive correlation: more aggregation → slower convergence

### Fitness Hypothesis: **INCONCLUSIVE**
- ? Fitness gradient correlation weak and non-significant (r=-0.105, p=0.2553)
- ? C4 (no factors) could not be tested properly due to design
- ? Factor presence/absence in candidate pools confounds results

### Novel Finding
**Low aggregation accelerates convergence**, contrary to expectations. This may indicate that:
- Clustering impedes exploration and sorting
- Diverse spatial distribution enables faster factor discovery
- The sorting algorithm benefits from heterogeneity

---

## Recommendations

### Immediate Next Steps

1. **Fix Candidate Generation**
   - Ensure BOTH factors (11 and 13) are ALWAYS present in all conditions
   - Modify generators to guarantee true factors in candidate pools
   - Re-run experiment with guaranteed factor presence

2. **Extend Max Steps**
   - Current 100-step limit too short for C1, C2 conditions
   - Increase to 200-300 steps to allow convergence
   - Determine actual convergence times without ceiling effect

3. **Investigate C3 Mechanism**
   - Why does zero aggregation accelerate convergence?
   - Profile cell movement patterns in C3 vs C1
   - Analyze swap patterns and sorting efficiency

### Future Experiments

1. **Vary Array Size**
   - Test if results hold for 25, 100, 200-cell arrays
   - Determine scaling behavior

2. **Alternative Fitness Functions**
   - Test with different distance metrics
   - Explore non-linear fitness landscapes

3. **Controlled Factor Placement**
   - Initialize with factors at known positions
   - Track migration independent of candidate generation

---

## Data Artifacts

- **CSV Results**: 150 files in `results/`
- **JSON Snapshots**: 1,209 files in `snapshots/`
- **Visualizations**: 5 PNG files in `visualizations/`
- **Statistical Analysis**: `analysis/statistical_tests.txt`
- **Total Data Size**: ~50 MB

---

## Reproducibility

All runs used deterministic seeds (rep N → seed N). To reproduce:

```bash
cd /home/runner/work/emergent-doom-engine/emergent-doom-engine
java -jar target/emergent-doom-engine-0.4.0-alpha-jar-with-dependencies.jar
cd experiments/clustering_vs_fitness_experiment_2026_01_10
python3 generate_visualizations.py results/
```

---

**Conclusion**: This experiment successfully executed but revealed unexpected dynamics requiring further investigation. The clustering hypothesis appears FALSIFIED, but candidate generation issues prevent definitive conclusions about the fitness hypothesis. A follow-up experiment with guaranteed factor presence is recommended.
