# Clustering Validation Experiments

> **⚠️ Status:** This directory contains archived experimental code (*.old files). Active clustering tests have not yet been implemented. The README documents the experimental design for future implementation.

This directory contains experiments to validate that the Emergent Doom Engine reproduces the clustering baselines from the Levin et al. (2024) paper.

## Purpose

Before extracting `ClusteringPrimitive<T>` as a reusable computational primitive, we must empirically verify that chimeric clustering behaves as expected. This validation establishes the scientific foundation for treating clustering as a computational primitive.

## Concepts Covered

The clustering experiments validate these key EDE framework concepts:

- **Morphogenetic Clustering** - Emergent spatial aggregation of same-type algotypes
- **Chimeric Populations** - Mixed algotype populations (50/50 Bubble/Selection, etc.)
- **Aggregation Dynamics** - Temporal progression of clustering from random to organized
- **Statistical Validation** - Comparing experimental results to theoretical baselines with t-tests
- **Levin Framework Alignment** - Reproducing clustering signatures from Levin et al. (2024)

## Prerequisites

**Required Understanding:**
- [Chapter 1: Cell Foundations](../../cell/README.md) - Cell interface and algotypes
- [Chapter 2: Swap Mechanics](../../swap/README.md) - How cells interact via swaps
- [Chapter 2: Metrics](../../metrics/README.md) - AlgotypeAggregationIndex metric
- [Chapter 4: Experiments](../../experiment/README.md) - Experimental framework

**Mathematical Background:**
- Basic statistics (mean, standard deviation, confidence intervals)
- T-tests for hypothesis testing (one-sample and two-sample)

## Test Files

**Current Status:** All test files are archived (*.old extension). Implementation pending.

**Archived Components:**
- `ClusteringValidationExperiment.java.old` - Main experiment class
- `ClusteringValidationExperimentTest.java.old` - Test suite
- `ValidationStatistics.java.old` - Statistical analysis utilities
- `ClusteringValidationRunner.java.old` - Command-line runner
- `ChimericProbe.java.old` - Custom probe for chimeric experiments

**Planned Test Structure:**
When re-implemented, tests will cover:
1. Aggregation peak detection (72%, 65%, 69% for different pairs)
2. Peak timing verification (fraction of sorting progress)
3. Negative control validation (Bubble-Bubble < 60%)
4. Statistical significance testing (p-values)

## Usage Examples

**Note:** Examples refer to archived code structure. Update when re-implementing.

### Quick Structural Test
```bash
mvn test -Dtest="ClusteringValidationExperimentTest#testExpectedResultRecord"
```

### Full Validation Suite
```bash
mvn test-compile exec:java \
  -Dexec.mainClass="com.emergent.doom.experiments.clustering.ClusteringValidationRunner" \
  -Dexec.classpathScope=test
```

### Expected Output Pattern
```
Hardware: [CPU info for reproducibility]
Running: Bubble-Selection (100 trials)...
  Peak: 72.3% ± 4.1% at 42.1% ± 3.8% progress
  vs Control: p < 0.001 ✓
  vs Paper: p = 0.23 ✓
```

## Experiments

### ClusteringValidationExperiment

Validates three algotype pairs against paper expectations:

| Algotype Pair | Expected Peak | Expected Timing |
|---------------|---------------|-----------------|
| Bubble-Selection | 72% ± 5% | 42% ± 5% progress |
| Bubble-Insertion | 65% ± 5% | 21% ± 5% progress |
| Selection-Insertion | 69% ± 5% | 19% ± 5% progress |

**Negative Control:** Bubble-Bubble (homogeneous) should show < 60% aggregation (random baseline)

## Running the Validation

### Quick Test (verify structure)

```bash
mvn test -Dtest="ClusteringValidationExperimentTest#testExpectedResultRecord,testAlgotypePairRecord"
```

### Full Validation Suite (400 trials, ~30-60 seconds)

```bash
cd /home/runner/work/emergent-doom-engine/emergent-doom-engine
mvn test-compile exec:java -Dexec.mainClass="com.emergent.doom.experiments.clustering.ClusteringValidationRunner" -Dexec.classpathScope=test
```

## Expected Output

The runner prints:
1. Hardware configuration (for reproducibility)
2. Progress for each algotype pair
3. Statistical results (mean, std dev, p-values)
4. Validation status (✓ or ✗ for each criterion)
5. Overall validation summary

## Success Criteria

✓ All algotype pairs match paper expectations (p >= 0.05)  
✓ All algotype pairs differ from control (p < 0.05)  
✓ Control stays below random baseline (< 60%)  
✓ Statistical significance confirmed

## Implementation Details

### Components

- **ClusteringValidationExperiment**: Main experiment class
- **ValidationStatistics**: Statistical analysis (t-tests, CI)
- **ClusteringValidationRunner**: Command-line runner
- **ClusteringValidationExperimentTest**: Test suite

### Statistical Methods

- **One-sample t-test**: Compare observed peaks to paper expectations
- **Two-sample t-test**: Compare experimental peaks to control baseline
- **95% Confidence Intervals**: Computed using t-distribution
- **Bessel's Correction**: Applied to sample standard deviation

### Data Collection

For each trial:
1. Run chimeric experiment (50/50 algotype mix)
2. Extract aggregation trajectory from snapshots
3. Identify peak aggregation value
4. Identify timing of peak (as fraction of sorting progress)
5. Store for statistical analysis

## References

- Zhang, T., Goldstein, A., Levin, M. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis." arXiv:2401.05375v1
- `docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md` - Primitive specification
- `docs/requirements/FIRST_NON_SORTING_EXPERIMENT.md` - Blocked experiment

## Next Steps

After successful validation:
1. Document results in `docs/findings/clustering_validation_001.md`
2. Extract `ClusteringPrimitive<T>` interface
3. Apply to factorization experiment
4. Unblock Wave-CRISPR integration

---

**Status**: Implementation complete, ready for validation run  
**Last Updated**: 2026-01-05
