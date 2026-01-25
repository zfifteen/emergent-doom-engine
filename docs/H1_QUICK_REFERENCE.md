# H1 Batch Runner - Quick Reference

## One-Liner Commands

### Quick Test (10 seconds)
```bash
./scripts/run_h1_experiment.sh test
```

### Small Batch (2 minutes)
```bash
./scripts/run_h1_experiment.sh small
```

### Full Production Run (60 minutes)
```bash
./scripts/run_h1_experiment.sh full
```

### Custom Run
```bash
mvn exec:java -Dexec.mainClass="com.emergent.doom.domains.graphcoloring.H1BatchRunner" \
  -Dexec.args="--outDir output/my_run --trials 50 --popSizes 50,100 --maxSteps 3000"
```

## Analysis

### Install Dependencies
```bash
pip install -r scripts/requirements.txt
```

### Run Analysis
```bash
python scripts/analyze_h1_results.py experiments/h1_full_<timestamp>
```

### Quick Stats
```bash
# Success rates
for config in */; do
  echo -n "$config: "
  awk -F, 'NR>1 && $7=="true" {count++} END {print count+0}' "$config/trial_summary.csv"
done
```

## Output Files

Each configuration produces:
- `manifest.json` - Experiment metadata
- `trajectories.csv` - Step-by-step metrics
- `trial_summary.csv` - Per-trial aggregates

## Key Parameters

| Parameter | Default | Description |
|-----------|---------|-------------|
| --trials | 100 | Trials per popSize |
| --popSizes | 50,100 | Population sizes |
| --graphN | 20 | Graph vertices |
| --edgeP | 0.25 | Edge probability |
| --maxSteps | 5000 | Max steps per trial |
| --plateauWindowW | 75 | Plateau window |
| --bucketSizeB | 2 | Bucket size |
| --masterSeed | current time | Seed for determinism |

## Configurations

1. **BASELINE_CHIMERIC_NO_RECOMB** - Reference
2. **NEG_CONTROL_LABEL_ONLY** - Negative control
3. **CONTROL_RANDOM_CUT_RECOMB** - Random cut
4. **CONTROL_RANDOM_BOUNDARY_RECOMB** - Random boundary
5. **TEST_BOUNDARY_GUIDED_RECOMB** - H1 test

## Success Criteria

**H1 validated** if TEST significantly outperforms ALL controls:
- p-value < 0.05
- Cohen's d > 0.5

## Documentation

- Full guide: `docs/H1_BATCH_RUNNER.md`
- Overview: `docs/H1_EXPERIMENT_README.md`
- Summary: `docs/H1_IMPLEMENTATION_SUMMARY.md`
