# Phase 3 Clean Run Findings
**Date:** January 11, 2026
**Executor:** Gemini CLI Agent

## Executive Summary
A clean execution of the Clustering vs Fitness Experiment (v2 metrics) was performed. The results strongly support the falsification of the Clustering Hypothesis. Condition C3 (Zero Aggregation) demonstrated the highest convergence rate (50%), significantly outperforming C2 (High Aggregation, 0%) and C1 (Baseline, 0%).

## Experiment Execution Details
- **Test Suite:** `ClusteringVsFitnessExperimentPhase3Test`
- **Runs:** 150 total (30 per condition)
- **Max Steps:** 100
- **Target:** 143 (Factors 11, 13)
- **Convergence Criteria:** Both factors in positions [0, 4]
- **Stagnation Criteria:** 20 consecutive steps with 0 swaps

## Results by Condition

| Condition | Description | Convergence Rate (N=30) | Status |
|-----------|-------------|-------------------------|--------|
| **C1** | Baseline (Random) | 0/30 (0.0%) | Stagnated |
| **C2** | High Aggregation | 0/30 (0.0%) | Stagnated |
| **C3** | Zero Aggregation | 15/30 (50.0%) | **Highest Convergence** |
| **C4** | Fitness Control | 0/30 (0.0%) | Expected (No factors) |
| **C5** | Homogeneous | 4/30 (13.3%) | Low Convergence |

## Key Findings

### 1. Falsification of Clustering Hypothesis
The "Clustering Hypothesis" posits that spatial aggregation of same-strategy cells accelerates or enables factor localization. The results directly contradict this:
- **C3 (Zero Aggregation)** had the highest success rate (50%), despite having ~0% initial strategy aggregation.
- **C2 (High Aggregation)** completely failed to converge (0%), suggesting that pre-clustering strategies might actually *inhibit* or *delay* localization in this domain (possibly by creating stable local attractors that prevent factors from moving to the front).

### 2. Validation of Phase 2 Fixes
- **Factor Presence:** Validated that C1, C2, C3, and C5 contained factors 11 and 13.
- **Control Integrity:** Validated that C4 (Fitness Control) correctly excluded factors 11 and 13.
- **Stagnation Detection:** The stagnation mechanism (20 steps with 0 swaps) functioned as expected, terminating runs that reached stable non-optimal states.

### 3. Comparison with Previous Data
- **C3 Convergence:** Observed 50% vs PR reported 63.3%. This variance is expected in stochastic experiments but directionally consistent (C3 >> C2).
- **C1/C2 Failure:** Consistent with PR reports (0% convergence).

## Conclusion
The clean run confirms that **strategy aggregation is NOT a prerequisite for factor localization**. In fact, minimal aggregation (C3) appears advantageous. This supports the "Fitness-Driven Sorting" hypothesis, where global fitness gradients drive localization regardless of local strategy clustering.

## Artifacts
- **Results:** `experiments/clustering_vs_fitness_experiment_2026_01_10/results/*.csv`
- **Snapshots:** `experiments/clustering_vs_fitness_experiment_2026_01_10/snapshots/*.json`
