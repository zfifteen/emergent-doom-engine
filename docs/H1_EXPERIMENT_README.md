# H1 Plateau Boundary Study

## Overview

This directory contains the complete experimental infrastructure for validating **Hypothesis H1**:

> **"Boundary interfaces between algotypes are computational primitives that carry exploitable structure."**

The H1 study uses graph 3-coloring on Erdős-Rényi graphs as the test domain, with chimeric populations containing four distinct algotypes (GREEDY_REPAIR, MIN_CONFLICT, RANDOM_WALK, BACKTRACK_LIGHT).

## Quick Start

### 1. Run Quick Test (10 seconds)

```bash
# Validate infrastructure with minimal run
./scripts/run_h1_experiment.sh test
```

### 2. Run Small Batch (2 minutes)

```bash
# 10 trials, 50 cells, 1000 steps
./scripts/run_h1_experiment.sh small
```

### 3. Run Full Experiment (~60 minutes)

```bash
# Production run: 100 trials per popSize
./scripts/run_h1_experiment.sh full
```

### 4. Analyze Results

```bash
# Install analysis dependencies
pip install -r scripts/requirements.txt

# Analyze results
python scripts/analyze_h1_results.py experiments/h1_full_<timestamp>
```

## Experiment Design

### Five Configurations

All experiments run automatically across five configurations:

1. **BASELINE_CHIMERIC_NO_RECOMB** - Reference performance without recombination
2. **NEG_CONTROL_LABEL_ONLY** - Negative control (labels present but unused)
3. **CONTROL_RANDOM_CUT_RECOMB** - Random crossover position control
4. **CONTROL_RANDOM_BOUNDARY_RECOMB** - Random boundary selection control
5. **TEST_BOUNDARY_GUIDED_RECOMB** - Hypothesis test (boundary-guided selection)

### Success Criteria

**H1 is validated** if and only if:
- TEST_BOUNDARY_GUIDED_RECOMB significantly outperforms **ALL** controls (p < 0.05)
- Effect sizes are substantial (Cohen's d > 0.5)

Otherwise, **H1 is falsified**.

## Directory Structure

```
.
├── src/main/java/com/emergent/doom/domains/graphcoloring/
│   ├── H1BatchRunner.java              # Main experiment runner
│   ├── H1ValidationDemo.java            # Interactive demo
│   ├── PlateauDetector.java             # Plateau detection
│   ├── BoundaryAnalyzer.java            # Boundary analysis
│   └── BoundaryGuidedRecombination.java # Recombination operator
│
├── scripts/
│   ├── run_h1_experiment.sh     # Helper script (presets: test, small, medium, full)
│   ├── analyze_h1_results.py    # Statistical analysis script
│   └── requirements.txt         # Python dependencies
│
├── docs/
│   └── H1_BATCH_RUNNER.md       # Comprehensive documentation
│
└── experiments/
    └── h1_*_<timestamp>/        # Experiment outputs (gitignored)
        ├── BASELINE_CHIMERIC_NO_RECOMB/
        │   ├── manifest.json
        │   ├── trajectories.csv
        │   └── trial_summary.csv
        ├── NEG_CONTROL_LABEL_ONLY/
        ├── CONTROL_RANDOM_CUT_RECOMB/
        ├── CONTROL_RANDOM_BOUNDARY_RECOMB/
        └── TEST_BOUNDARY_GUIDED_RECOMB/
```

## Output Files

Each configuration produces:

- **manifest.json** - Experiment metadata and parameters
- **trajectories.csv** - Step-by-step metrics (all trials)
- **trial_summary.csv** - Per-trial aggregates

### CSV Columns

**trajectories.csv:**
```
trial_id, popSize, step, best_violations, median_violations, 
worst_violations, aggregation, on_plateau, plateau_duration, recomb_events
```

**trial_summary.csv:**
```
trial_id, popSize, graphSeed, runSeed, final_best, final_median,
solved, steps_to_solution, total_recomb_events, max_aggregation, final_aggregation
```

## Key Features

### Deterministic Execution
All runs are **fully reproducible** from seeds:
- Same `--masterSeed` → identical results
- Graph generation, population initialization, execution sequence all deterministic
- Verified with differential testing (see docs/H1_BATCH_RUNNER.md)

### Offline Recombination
Recombination is **purely analytical** - never modifies running population:
- Preserves EDE substrate integrity
- Enables clean control vs. test comparisons
- Recombination events recorded for analysis

### Plateau Engineering
Uses **bucketed violations** (bucket size = 2) to create wide fitness plateaus:
- Forces sustained exploration
- Tests boundary utility during stagnation
- Plateau window: 75 ticks (default)

## Example Usage

### Custom Configuration

```bash
# 20 trials, dual population sizes, custom steps
mvn exec:java -Dexec.mainClass="com.emergent.doom.domains.graphcoloring.H1BatchRunner" \
  -Dexec.args="--outDir my_experiment \
               --trials 20 \
               --popSizes 30,60 \
               --maxSteps 3000 \
               --masterSeed 42"
```

### Parallel Execution

Split large runs across processes:

```bash
# Process 1: trials 0-49
--graphSeeds <seeds_0_49> --runSeeds <seeds_0_49>

# Process 2: trials 50-99
--graphSeeds <seeds_50_99> --runSeeds <seeds_50_99>
```

## Analysis Workflow

### Load and Compare

```python
import pandas as pd

# Load all configurations
baseline = pd.read_csv('experiments/h1_*/BASELINE_CHIMERIC_NO_RECOMB/trial_summary.csv')
guided = pd.read_csv('experiments/h1_*/TEST_BOUNDARY_GUIDED_RECOMB/trial_summary.csv')

# Compare success rates
print(f"Baseline solved: {baseline['solved'].mean()*100:.1f}%")
print(f"Guided solved: {guided['solved'].mean()*100:.1f}%")

# Mean final fitness
print(f"Baseline mean violations: {baseline['final_best'].mean():.2f}")
print(f"Guided mean violations: {guided['final_best'].mean():.2f}")
```

### Statistical Testing

```python
from scipy import stats

# Two-sample t-test (one-tailed)
t_stat, p_value = stats.ttest_ind(
    baseline['final_best'], 
    guided['final_best'],
    alternative='greater'
)

print(f"p-value: {p_value:.4f}")
if p_value < 0.05:
    print("✓ Significant improvement")
else:
    print("✗ Not significant")
```

## Performance Notes

| Preset | Trials | Steps | Time | Memory |
|--------|--------|-------|------|--------|
| test   | 2      | 100   | ~10s | <512MB |
| small  | 10     | 1000  | ~2m  | ~1GB   |
| medium | 50     | 2500  | ~15m | ~2GB   |
| full   | 200    | 5000  | ~60m | ~4GB   |

*Times are approximate for popSize=50. Double for dual sizes (50,100).*

## Documentation

- **[H1_BATCH_RUNNER.md](docs/H1_BATCH_RUNNER.md)** - Comprehensive guide
  - Full CLI reference
  - Output format specifications
  - Analysis examples
  - Troubleshooting

## Requirements

### Java
- JDK 11 or higher
- Maven 3.6+

### Python (for analysis)
```bash
pip install -r scripts/requirements.txt
```

Installs: pandas, numpy, scipy, matplotlib

## Citation

```bibtex
@software{h1_plateau_boundary_study_2026,
  title={H1 Plateau Boundary Study: Graph Coloring Experiments},
  author={Emergent Doom Engine},
  year={2026},
  url={https://github.com/zfifteen/emergent-doom-engine}
}
```

## Related Work

- **Levin et al. (2024)** - "Morphogenesis as Computation" - Theoretical foundation
- **PR #176** - Initial H1 validation infrastructure
- [Graph Coloring Domain](src/main/java/com/emergent/doom/domains/graphcoloring/README.md)

## License

MIT License - See [LICENSE](../LICENSE)

---

**Last Updated:** 2026-01-25  
**Status:** Production Ready  
**Maintainer:** @zfifteen
