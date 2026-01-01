# Experimental Findings

This directory contains detailed reports of experiments conducted with the Emergent Doom Engine.

## Directory Structure

```
docs/findings/
├── README.md                    (this file - experiment index)
└── factorization-exp-001/       (Factorization experiment artifacts)
    ├── factorization_experiment_2026-01-01.md
    ├── factor_reporting_fix_2026-01-01.md
    ├── OPTION_A_COMPLETE.md
    └── final_run_output.txt
```

Each experiment gets its own subfolder containing all related documentation and artifacts.

---

## Index of Experiments

### 2026

#### Factorization Experiment (FACT-EXP-001)
**Folder:** `factorization-exp-001/`  
**Date:** January 1, 2026  
**Status:** ✅ SUCCESSFUL - Reporting bug FIXED

**Documents:**
- **[Main Report](./factorization-exp-001/factorization_experiment_2026-01-01.md)** - Complete experimental report
  - Target: Single semiprime (100,039 = 71 × 1,409)
  - Key Result: 100% convergence, factors emerged at array positions 0-1
  - Discovery: Critical bug in factor reporting (position vs. index confusion)
  - Fix: Implemented Option A - store final cell array in TrialResult
  - Theoretical Validation: Confirmed distributed Euclidean algorithm
  - Metrics: 99.70% sortedness, 0.20 monotonicity error, mean 1,156 steps

- **[Factor Reporting Fix](./factorization-exp-001/factor_reporting_fix_2026-01-01.md)** - Detailed fix documentation
  - Problem: Snapshot loses position information after sorting
  - Solution: Store final cell array in TrialResult
  - Impact: Zero performance degradation, correctly reports factors
  - Pattern: Established general approach for domain-specific result extraction

- **[Implementation Summary](./factorization-exp-001/OPTION_A_COMPLETE.md)** - Complete checklist and deliverables
  - Verification results
  - Files modified
  - Next steps

- **[Final Run Output](./factorization-exp-001/final_run_output.txt)** - Raw output from verification run
  - Shows correct factor reporting (position 71)
  - Demonstrates 100% convergence

#### Factorization Experiment (FACT-EXP-002)
**Folder:** `factorization-exp-002/`  
**Date:** January 1, 2026  
**Status:** ✅ SUCCESSFUL - Large Number Scaling

**Documents:**
- **[Main Report](./factorization-exp-002/README.md)** - Experiment overview and results
  - Target: 1e18 (1,000,000,000,000,000,000 = 2^18 × 5^18)
  - Key Result: 100% convergence, 28 factors discovered
  - Performance: 98.45% sortedness, 0.57 monotonicity error, mean 1,279 steps
  - Scalability: Only 10.6% increase in steps despite 10^13× larger target!
  - New Feature: Command-line argument support added
  - Trials: 30 (increased from 5 for better statistics)

- **[Raw Output](./factorization-exp-002/exp002_run_output.txt)** - Console output from run
  - Shows all 28 factors discovered
  - Demonstrates 100% convergence across 30 trials

---

## Document Standards

Each experimental report should include:

1. **Executive Summary:** High-level results and status
2. **Experimental Setup:** Configuration parameters and code modifications
3. **Results:** Quantitative metrics and observations
4. **Analysis:** Interpretation and theoretical validation
5. **Lessons Learned:** Insights for future work
6. **Recommendations:** Next steps and follow-up experiments
7. **Appendices:** Raw data, code changes, verification scripts

### Folder Organization

Each experiment should have its own subfolder named with a descriptive identifier:
- Format: `{experiment-name}-{id}/` (e.g., `factorization-exp-001/`)
- All related files go in the subfolder (reports, fixes, output, data, etc.)
- Main experiment report should be the primary document

---

## Related Documentation

- **Theory:** `docs/theory/distributed_euclidean_remaindercell.md`
- **Requirements:** `docs/requirements/REQUIREMENTS.md`
- **Implementation History:** `docs/history/`
- **Gap Analysis:** `docs/implementation/GAPS-CLAUDE.md`

---

**Maintained by:** Emergent Doom Engine Research Team  
**Last updated:** January 1, 2026
