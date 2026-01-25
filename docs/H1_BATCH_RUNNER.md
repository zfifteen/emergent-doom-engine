# H1 Plateau Boundary Study - Batch Runner Documentation

## Overview

The H1 Batch Runner executes systematic experiments to validate hypothesis H1: **"Boundary interfaces between algotypes are computational primitives that carry exploitable structure."**

This infrastructure enables reproducible multi-trial experiments across five configurations, generating complete datasets for statistical analysis.

## Quick Start

### Minimal Example

```bash
# Run small test batch (2 trials, 20 cells, 100 steps)
mvn exec:java -Dexec.mainClass="com.emergent.doom.domains.graphcoloring.H1BatchRunner" \
  -Dexec.args="--outDir output/h1_test --trials 2 --popSizes 20 --maxSteps 100"
```

### Production Run (Recommended)

```bash
# Run full H1 experiment (100 trials per popSize, 5000 steps)
mvn exec:java -Dexec.mainClass="com.emergent.doom.domains.graphcoloring.H1BatchRunner" \
  -Dexec.args="--outDir output/h1_full \
               --trials 100 \
               --popSizes 50,100 \
               --graphN 20 \
               --edgeP 0.25 \
               --maxSteps 5000 \
               --masterSeed 42"
```

## Experiment Configurations

The runner executes all five configurations automatically:

### 1. BASELINE_CHIMERIC_NO_RECOMB
**Purpose:** Baseline performance without recombination.  
**Behavior:** Chimeric population (4 algotypes, 25% each) with no offline recombination.  
**Use:** Reference for measuring improvement from recombination.

### 2. NEG_CONTROL_LABEL_ONLY  
**Purpose:** Negative control to verify algotype labels matter.  
**Behavior:** Same as baseline (labels present but not used for boundary selection).  
**Expected:** Should perform identically to baseline if labels are irrelevant.

### 3. CONTROL_RANDOM_CUT_RECOMB
**Purpose:** Control for random crossover position.  
**Behavior:** Offline recombination at random cut points (not at boundaries).  
**Expected:** Minimal improvement over baseline (random cuts lack structure).

### 4. CONTROL_RANDOM_BOUNDARY_RECOMB
**Purpose:** Control for random boundary selection.  
**Behavior:** Offline recombination at randomly chosen boundaries.  
**Expected:** Some improvement, but less than guided selection.

### 5. TEST_BOUNDARY_GUIDED_RECOMB
**Purpose:** Test H1 hypothesis.  
**Behavior:** Offline recombination at top boundary by mobility gradient with low fitness gradient constraint.  
**Expected:** **Must dominate all controls** or H1 is falsified.

## Command-Line Arguments

### Required
- `--outDir <path>` - Output directory (created if doesn't exist)

### Optional (with defaults)
- `--trials <n>` - Trials per population size (default: 100)
- `--popSizes <list>` - Comma-separated population sizes (default: 50,100)
- `--graphN <n>` - Graph vertices (default: 20)
- `--edgeP <p>` - Edge probability for Erdős-Rényi graphs (default: 0.25)
- `--maxSteps <n>` - Maximum steps per trial (default: 5000)
- `--plateauWindowW <w>` - Plateau detection window (default: 75)
- `--bucketSizeB <b>` - Bucket size for plateau engineering (default: 2)
- `--masterSeed <seed>` - Master seed for deterministic runs (default: current time)
- `--graphSeeds <list>` - Explicit graph seeds (optional, overrides masterSeed)
- `--runSeeds <list>` - Explicit run seeds (optional, overrides masterSeed)

## Output Structure

Each configuration produces its own directory:

```
output/
├── BASELINE_CHIMERIC_NO_RECOMB/
│   ├── manifest.json          # Experiment metadata
│   ├── trajectories.csv       # Step-by-step metrics for all trials
│   └── trial_summary.csv      # Aggregate metrics per trial
├── NEG_CONTROL_LABEL_ONLY/
│   ├── manifest.json
│   ├── trajectories.csv
│   └── trial_summary.csv
├── CONTROL_RANDOM_CUT_RECOMB/
│   └── ...
├── CONTROL_RANDOM_BOUNDARY_RECOMB/
│   └── ...
└── TEST_BOUNDARY_GUIDED_RECOMB/
    └── ...
```

### manifest.json

Contains experiment configuration and metadata:

```json
{
  "experiment": "H1_Plateau_Boundary_Study",
  "configuration": "TEST_BOUNDARY_GUIDED_RECOMB",
  "description": "Test: boundary-guided recombination",
  "parameters": {
    "trials": 100,
    "popSizes": [50, 100],
    "graphN": 20,
    "edgeP": 0.25,
    "maxSteps": 5000,
    "plateauWindowW": 75,
    "bucketSizeB": 2,
    "algotypeMix": {
      "GREEDY_REPAIR": 0.25,
      "MIN_CONFLICT": 0.25,
      "RANDOM_WALK": 0.25,
      "BACKTRACK_LIGHT": 0.25
    }
  },
  "seeds": {
    "graphSeeds": [...],
    "runSeeds": [...]
  },
  "timestamp": 1737819383719
}
```

### trajectories.csv

Step-by-step metrics for each trial:

**Columns:**
- `trial_id` - Unique trial identifier
- `popSize` - Population size for this trial
- `step` - Execution step (0-indexed)
- `best_violations` - Minimum violations in population
- `median_violations` - Median violations
- `worst_violations` - Maximum violations
- `aggregation` - Algotype aggregation index (0-1)
- `on_plateau` - Boolean: currently on fitness plateau
- `plateau_duration` - Consecutive plateau ticks
- `recomb_events` - Recombination events this step (offline analysis)

**Example:**
```csv
trial_id,popSize,step,best_violations,median_violations,worst_violations,aggregation,on_plateau,plateau_duration,recomb_events
0,50,0,12,16,28,0.1633,false,0,0
0,50,1,9,14,28,0.1633,false,0,0
0,50,75,5,8,15,0.2449,true,1,0
0,50,85,5,8,14,0.2449,true,11,1
```

### trial_summary.csv

Aggregate metrics per trial:

**Columns:**
- `trial_id` - Unique trial identifier
- `popSize` - Population size
- `graphSeed` - Seed used for graph generation
- `runSeed` - Seed used for execution
- `final_best` - Best violations at end
- `final_median` - Median violations at end
- `solved` - Boolean: found valid coloring (0 violations)
- `steps_to_solution` - Steps to first valid coloring (-1 if unsolved)
- `total_recomb_events` - Total offline recombination events
- `max_aggregation` - Peak algotype aggregation
- `final_aggregation` - Aggregation at end

**Example:**
```csv
trial_id,popSize,graphSeed,runSeed,final_best,final_median,solved,steps_to_solution,total_recomb_events,max_aggregation,final_aggregation
0,50,123456,789012,2,5,false,-1,12,0.3571,0.2857
1,50,234567,890123,0,3,true,342,8,0.4082,0.3673
```

## Determinism Guarantees

### Reproducibility
With the same `--masterSeed`, runs produce **identical results**:
- Same graph structures (vertices, edges)
- Same initial populations (states, positions)
- Same execution sequences (random walks, repairs)
- Same recombination events (parent selection, crossover)

### Verification
```bash
# Run 1
mvn exec:java -Dexec.mainClass="..." \
  -Dexec.args="--outDir run1 --masterSeed 42 --trials 10"

# Run 2
mvn exec:java -Dexec.mainClass="..." \
  -Dexec.args="--outDir run2 --masterSeed 42 --trials 10"

# Verify
diff run1/*/trial_summary.csv run2/*/trial_summary.csv
# Should output: Files are identical
```

## Data Analysis Workflow

### 1. Load Data
```python
import pandas as pd

# Load all configurations
configs = [
    'BASELINE_CHIMERIC_NO_RECOMB',
    'NEG_CONTROL_LABEL_ONLY',
    'CONTROL_RANDOM_CUT_RECOMB',
    'CONTROL_RANDOM_BOUNDARY_RECOMB',
    'TEST_BOUNDARY_GUIDED_RECOMB'
]

summaries = {}
for config in configs:
    summaries[config] = pd.read_csv(f'output/{config}/trial_summary.csv')
```

### 2. Key Metrics

**Success Rate:**
```python
for config, df in summaries.items():
    solved_pct = (df['solved'].sum() / len(df)) * 100
    print(f"{config}: {solved_pct:.1f}% solved")
```

**Mean Final Fitness:**
```python
for config, df in summaries.items():
    mean_best = df['final_best'].mean()
    print(f"{config}: {mean_best:.2f} mean violations")
```

**Aggregation Peaks:**
```python
for config, df in summaries.items():
    mean_peak = df['max_aggregation'].mean()
    print(f"{config}: {mean_peak:.3f} peak aggregation")
```

### 3. Statistical Tests

**H1 Validation:**
```python
from scipy import stats

baseline = summaries['BASELINE_CHIMERIC_NO_RECOMB']['final_best']
guided = summaries['TEST_BOUNDARY_GUIDED_RECOMB']['final_best']

# Two-sample t-test
t_stat, p_value = stats.ttest_ind(baseline, guided)
print(f"t-statistic: {t_stat:.3f}, p-value: {p_value:.4f}")

# Effect size (Cohen's d)
mean_diff = baseline.mean() - guided.mean()
pooled_std = np.sqrt((baseline.std()**2 + guided.std()**2) / 2)
cohens_d = mean_diff / pooled_std
print(f"Cohen's d: {cohens_d:.3f}")
```

**H1 passes only if:**
- Guided significantly better than **all** controls (p < 0.05)
- Effect sizes are substantial (Cohen's d > 0.5)

## Performance Notes

### Runtime Estimates
- **Small test** (2 trials, popSize=20, 100 steps): ~10 seconds
- **Medium batch** (10 trials, popSize=50, 1000 steps): ~2 minutes
- **Full run** (100 trials, popSizes=[50,100], 5000 steps): ~45-60 minutes

### Memory Requirements
- **Small test:** <512 MB
- **Full run:** ~2-4 GB (Java heap)

Set Java heap if needed:
```bash
export MAVEN_OPTS="-Xmx4g"
mvn exec:java -Dexec.mainClass="..."
```

### Parallelization
Current implementation is **single-threaded** by design (for determinism).

For faster runs, split trials across multiple processes:
```bash
# Process 1: trials 0-49
--graphSeeds <seeds_0_49> --runSeeds <seeds_0_49>

# Process 2: trials 50-99
--graphSeeds <seeds_50_99> --runSeeds <seeds_50_99>
```

## Troubleshooting

### OutOfMemoryError
```bash
export MAVEN_OPTS="-Xmx8g"
```

### Files Not Created
Check permissions on `--outDir` path.

### Non-Deterministic Results
Verify:
1. Same `--masterSeed` used
2. Same `--trials` count
3. Same `--popSizes` order
4. Same JVM version (OpenJDK 11+)

## Citation

If using this runner for research, cite:

```bibtex
@software{h1_batch_runner_2026,
  title={H1 Plateau Boundary Study Batch Runner},
  author={Emergent Doom Engine},
  year={2026},
  url={https://github.com/zfifteen/emergent-doom-engine}
}
```

## Related Documentation

- [H1 Validation Demo](../src/main/java/com/emergent/doom/domains/graphcoloring/H1ValidationDemo.java)
- [Plateau Detection](../src/main/java/com/emergent/doom/domains/graphcoloring/PlateauDetector.java)
- [Boundary Analysis](../src/main/java/com/emergent/doom/domains/graphcoloring/BoundaryAnalyzer.java)
- [Guided Recombination](../src/main/java/com/emergent/doom/domains/graphcoloring/BoundaryGuidedRecombination.java)
