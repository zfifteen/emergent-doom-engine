# Clustering vs Fitness Experiment

## Overview

This experiment tests the central hypothesis that factor localization in the Emergent Doom Engine factorization domain is driven by **FITNESS-BASED SORTING**, not by algotype clustering.

## Scientific Question

**Does initial strategy aggregation (clustering) affect the speed of factor localization?**

Two competing hypotheses:

1. **Clustering Hypothesis**: High initial aggregation accelerates factor localization
2. **Fitness Hypothesis**: Fitness-driven sorting is the sole driver; aggregation is irrelevant

## Experimental Design

### Five Conditions

| Condition | Aggregation | Description | Purpose |
|-----------|-------------|-------------|---------|
| C1: Baseline | ~50-60% | Natural chimeric distribution (33% SMALL_PRIMES, 34% FERMAT_NEAR_SQRT, 33% RANDOM_SAMPLE) | Reference condition |
| C2: High Aggregation | ~75% | Pre-clustered by strategy (blocks of each strategy) | Test if clustering accelerates localization |
| C3: Zero Aggregation | ~0-10% | Maximally mixed strategies (alternating pattern) | Test if lack of clustering impedes localization |
| C4: Fitness Control | ~50-60% | Same as C1 but NO true factors (11 and 13 excluded) | Test if fitness gradient is necessary |
| C5: Homogeneous | 100% | Single strategy (100% FERMAT_NEAR_SQRT) | Test if perfect aggregation alone is sufficient |

### Prediction Matrix

| Hypothesis | C1 | C2 | C3 | C4 | C5 |
|------------|----|----|----|----|-----|
| **Clustering Causes Localization** | Localizes | **Fastest** | **No localization** | ? | Fastest |
| **Fitness Causes Localization** | Localizes | Same speed | Same speed | **No localization** | Same speed |

## Experimental Parameters

- **Target semiprime**: N = 143 (11 × 13)
- **Array size**: 50 cells
- **Max steps**: 100 per run
- **Repetitions**: 30 per condition (150 total runs)
- **Snapshot interval**: Every 5 steps
- **Convergence criteria**: Both factors in positions 0-4 OR swaps = 0

## Metrics Recorded Per Step

1. **Aggregation value**: % cells with same-strategy neighbor (clustering measure)
2. **Factor positions**: Positions of candidates 11 and 13
3. **Mean factor distance from front**: Average distance of factors from position 0
4. **Fitness gradient mean**: Mean |fitness[i] - fitness[i+1]|
5. **Fitness gradient std**: Standard deviation of fitness gradient
6. **Strategy entropy (global)**: Shannon entropy across entire array
7. **Strategy entropy (front)**: Shannon entropy in positions 0-9
8. **Swap count**: Number of swaps executed

## Running the Experiment

### Build the Project

```bash
mvn clean package -DskipTests
```

### Run the Full Experiment

```bash
java -cp target/emergent-doom-engine-0.4.0-alpha.jar \
  com.emergent.doom.factorization.ClusteringVsFitnessExperiment
```

This will:
- Run all 5 conditions
- Execute 30 repetitions per condition (150 total runs)
- Write results to `experiments/clustering_vs_fitness_experiment_2026_01_10/`

### Expected Runtime

- Approximately 5-10 minutes for full experiment (150 runs × 100 steps max)
- Progress is displayed per condition

## Output Structure

```
experiments/clustering_vs_fitness_experiment_2026_01_10/
├── results/
│   ├── C1_baseline_rep_001.csv
│   ├── C1_baseline_rep_002.csv
│   ├── ...
│   ├── C2_high_aggregation_rep_001.csv
│   ├── ...
│   ├── C3_zero_aggregation_rep_001.csv
│   ├── ...
│   ├── C4_fitness_control_rep_001.csv
│   ├── ...
│   └── C5_homogeneous_rep_030.csv
└── snapshots/
    ├── C1_baseline_rep_001_step_000.json
    ├── C1_baseline_rep_001_step_005.json
    ├── C1_baseline_rep_001_step_010.json
    └── ...
```

### CSV Format

Each CSV contains per-step metrics:

```csv
step,aggregation,factor_11_pos,factor_13_pos,mean_factor_dist,fitness_grad_mean,fitness_grad_std,entropy_global,entropy_front,swaps
0,68.00,46,12,29.00,0.0234,0.0156,1.5234,1.4821,0
1,66.00,44,11,27.50,0.0221,0.0148,1.5123,1.4654,8
...
```

### JSON Snapshot Format

Each JSON contains array state at a specific step:

```json
{
  "step": 5,
  "condition": "C1_baseline",
  "rep": 1,
  "cells": [
    {
      "position": 0,
      "candidate": 11,
      "fitness": 1.0,
      "strategy": "FERMAT_NEAR_SQRT"
    },
    ...
  ]
}
```

## Analyzing Results

### Key Questions to Answer

1. **Does C2 (high aggregation) localize factors faster than C1 and C3?**
   - If YES → Clustering hypothesis supported
   - If NO → Fitness hypothesis supported

2. **Does C3 (zero aggregation) still localize factors?**
   - If YES → Aggregation not necessary
   - If NO → Aggregation is necessary

3. **Does C4 (no true factors) show localization?**
   - If YES → Clustering alone can cause localization-like patterns
   - If NO → Fitness gradient is necessary

4. **Does C5 (100% aggregation) localize faster than C1?**
   - If YES → Perfect aggregation provides advantage
   - If NO → Aggregation level doesn't affect localization speed

### Recommended Analysis

1. **Convergence speed**: Plot mean_factor_dist over steps for each condition
2. **Aggregation dynamics**: Plot aggregation over time for each condition
3. **Fitness landscape evolution**: Plot fitness_grad_mean over time
4. **Entropy dynamics**: Compare entropy_global and entropy_front over time
5. **Statistical comparison**: Compare final convergence steps across conditions (ANOVA/Kruskal-Wallis)

## Implementation Details

### Classes

- **`StepMetrics.java`**: Data record for per-step metrics
- **`ClusteringVsFitnessExperiment.java`**: Main experiment orchestrator

### Key Methods

- `generateC1Baseline(seed)`: Create baseline condition
- `generateC2HighAggregation(seed)`: Create pre-clustered condition
- `generateC3ZeroAggregation(seed)`: Create maximally mixed condition
- `generateC4FitnessControl(seed)`: Create no-factors control
- `generateC5Homogeneous(seed)`: Create single-strategy condition
- `computeMetrics(step, cells, swaps)`: Compute all metrics for current state
- `executeExperimentRun(cells)`: Run single trial and collect metrics

### Reproducibility

- Each repetition uses its repetition number as the random seed (rep 1 → seed 1, rep 2 → seed 2, etc.)
- All random number generation uses seeded `java.util.Random`
- Candidate generation is deterministic given a seed

## Expected Results (Hypothesis: Fitness Drives Localization)

If fitness-driven sorting is the sole mechanism:

- **C1, C2, C3, C5**: All should localize at approximately the same speed
- **C4**: Should NOT localize (no fitness peak)
- **Aggregation**: Should be uncorrelated with localization speed

If clustering is causal:

- **C2**: Should localize significantly faster than C1 and C3
- **C3**: Should localize slowly or not at all
- **C5**: Should localize fastest (perfect clustering)
- **Aggregation**: Should be strongly correlated with localization speed

## References

- `src/main/java/com/emergent/doom/factorization/FactorCell.java` - Factor cell implementation with fitness-based comparison
- `docs/FACTOR-LOCALIZATION-INVESTIGATION.md` - Mechanistic analysis of factor localization (Jan 8, 2026)
- `docs/FIRST_NON_SORTING_EXPERIMENT.md` - Original experimental design for factorization domain
