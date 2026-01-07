# Clustering Validation Experiments

> **⚠️ Status:** This directory contains archived experimental code (*.old files). Active clustering tests have not yet been implemented. The README documents the experimental design for future implementation.

This directory contains experiments to validate that the Emergent Doom Engine reproduces the clustering baselines from the Levin et al. (2024) paper.

## Purpose

Empirical verification of chimeric clustering behavior establishes the scientific foundation for treating clustering as a computational primitive. Before extracting `ClusteringPrimitive<T>` as a reusable interface, experimental results must validate that EDE reproduces expected clustering dynamics.

## Concepts Validated

The clustering experiments validate morphogenetic clustering, chimeric population dynamics, and aggregation patterns from Levin et al. (2024):

- **Morphogenetic Clustering** - Emergent spatial aggregation of same-type algotypes
- **Chimeric Populations** - Mixed algotype populations (50/50 Bubble/Selection, etc.)
- **Aggregation Dynamics** - Temporal progression of clustering from random to organized
- **Statistical Validation** - Comparing experimental results to theoretical baselines with t-tests

## Experimental Design

### Test Files (Archived)

All test files carry *.old extensions pending re-implementation:
- `ClusteringValidationExperiment.java.old` - Main experiment class
- `ClusteringValidationExperimentTest.java.old` - Test suite
- `ValidationStatistics.java.old` - Statistical analysis utilities
- `ClusteringValidationRunner.java.old` - Command-line runner
- `ChimericProbe.java.old` - Custom probe for chimeric experiments

### Validation Targets

Three algotype pairs tested against paper expectations:

| Algotype Pair | Expected Peak | Expected Timing |
|---------------|---------------|-----------------|
| Bubble-Selection | 72% ± 5% | 42% ± 5% progress |
| Bubble-Insertion | 65% ± 5% | 21% ± 5% progress |
| Selection-Insertion | 69% ± 5% | 19% ± 5% progress |

**Negative Control:** Bubble-Bubble (homogeneous) should show < 60% aggregation (random baseline)

### Statistical Methods

- **One-sample t-test** - Compare observed peaks to paper expectations
- **Two-sample t-test** - Compare experimental peaks to control baseline
- **95% Confidence Intervals** - Computed using t-distribution
- **Bessel's Correction** - Applied to sample standard deviation

## Usage (When Re-Implemented)

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

**Success Criteria:**
- ✓ All algotype pairs match paper expectations (p >= 0.05)  
- ✓ All algotype pairs differ from control (p < 0.05)  
- ✓ Control stays below random baseline (< 60%)  
- ✓ Statistical significance confirmed

## References

- Zhang, T., Goldstein, A., Levin, M. (2024). "Classical Sorting Algorithms as a Model of Morphogenesis." arXiv:2401.05375v1
- `docs/requirements/CLUSTERING_PRIMITIVE_SPEC.md` - Primitive specification
- `docs/requirements/FIRST_NON_SORTING_EXPERIMENT.md` - Blocked experiment

---

**Last Updated:** 2026-01-06
