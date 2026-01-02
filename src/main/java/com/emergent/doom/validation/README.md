# Linear Scaling Validation System

## Overview

This package implements the experimental validation system described in GitHub issue "Experimental Validation: Test Linear Scaling on Progressively Harder Semiprimes". The system systematically tests whether the emergent factorization algorithm maintains **O(n) time complexity** (constant convergence steps regardless of array size) as problem difficulty increases.

## Background

Recent analysis in `docs/findings/LINEAR_SCALING_ANALYSIS.md` demonstrated that the algorithm exhibits linear scaling for easy semiprimes (N=100043), with convergence time invariant to array size expansion (B ≈ 0, where B = ∂steps/∂array_size). This system tests whether this property generalizes to cryptographically hard semiprimes.

## Key Metric: The B Coefficient

**B = ∂(mean_steps)/∂(array_size)**

- **B ≈ 0**: Steps constant regardless of array size → **O(n) scaling confirmed**
- **B > 0.5**: Steps grow with array size → **Failure boundary found**

## Experimental Ladder

The system tests progressively harder semiprimes:

| Stage | Magnitude | Prime Factors | Array Sizes | Max Steps | Purpose |
|-------|-----------|---------------|-------------|-----------|---------|
| Stage 1 | 10^6 | ~10^3 × 10^3 | [10^4, 10^5, 10^6] | 10,000 | Baseline validation |
| Stage 2 | 10^9 | ~10^4.5 × 10^4.5 | [10^4, 10^5, 10^6] | 10,000 | Moderate difficulty |
| Stage 3 | 10^12 | ~10^6 × 10^6 | [10^4, 10^5, 10^6] | 100,000 | High difficulty |
| Stage 4 | 10^18 | ~10^9 × 10^9 | [10^4, 10^5, 10^6] | 1,000,000 | Cryptographic scale |

**Early Termination**: The system automatically stops if B > 0.5 or success rate < 70%, avoiding wasted compute on doomed experiments.

## Quick Start

### Build
```bash
mvn clean package -DskipTests
```

### Run Default Experiment (Stage 1)
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.validation.LinearScalingValidator
```

### Run Progressive Ladder (All Stages)
```bash
./scripts/run_linear_scaling_validation.sh
```

This script:
1. Runs all stages sequentially
2. Logs output to `validation_results/experiment_log.txt`
3. Exports CSV data for each stage
4. Terminates early if failure boundary detected

## Output

### Console Report
Each stage produces a detailed console report:
```
======================================================================
SCALING ANALYSIS REPORT
======================================================================

Stage: STAGE_1_E6
Total trials: 90

KEY METRICS:
  B coefficient (∂steps/∂array_size): 0.000023
  R² (goodness of fit): 0.9874
  Z-normalization (CV): 0.0541
  Success rate: 100.0%

ASSESSMENT: LINEAR SCALING CONFIRMED (B ≈ 0, high R², high success rate)

PER-ARRAY-SIZE BREAKDOWN:
  Array size 10000: mean=134.2, std=7.1, successes=30/30 (100%)
  Array size 100000: mean=135.8, std=6.9, successes=30/30 (100%)
  Array size 1000000: mean=136.1, std=7.3, successes=30/30 (100%)
```

### CSV Export
Results are exported to CSV files compatible with `scripts/analyze_scaling.py`:

```csv
stage,target,arraySize,steps,converged,foundFactor,factor,timeMs,remainderMean,remainderVariance,remainderAutocorr
STAGE_1_E6,1006009,10000,135,true,true,1003,,45.2,892.4,0.87
STAGE_1_E6,1006009,10000,138,true,true,1003,,44.8,901.2,0.86
...
```

## Architecture

### Core Classes

- **`LinearScalingValidator`**: Main orchestrator, entry point
- **`ScalingStage`**: Enum defining experimental stages and their parameters
- **`ScalingTrialConfig`**: Immutable configuration for a single trial
- **`ScalingTrialResult`**: Results from one trial execution
- **`RemainderStatistics`**: Analyzes remainder landscape (mean, variance, autocorrelation)
- **`ScalingReport`**: Aggregates trials, computes B coefficient, generates reports

### Integration

The system integrates with existing infrastructure:
- Uses `ExperimentRunner` for trial execution
- Uses `RemainderCell` for factorization domain
- Uses Apache Commons Math for statistical analysis (linear regression)

### Data Flow

```
Command-line args
    ↓
Stage selection → Generate semiprime (nextPrime-based)
    ↓
For each array size:
    Run 30 trials → executeSingleTrial()
        ↓
    Collect: steps, convergence, factors, remainder stats
    ↓
Aggregate results → ScalingReport
    ↓
Compute B coefficient (linear regression)
    ↓
Generate console report + CSV export
    ↓
Decision: proceed or terminate?
```

## Advanced Usage

### Custom Target
Test a specific semiprime (bypasses stage defaults):
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.validation.LinearScalingValidator \
  --target 1000000007 --trials 30
```

### Specific Stage
Run only Stage 2:
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.validation.LinearScalingValidator \
  --stage STAGE_2_E9
```

### Custom Output Path
```bash
java -cp target/emergent-doom-engine-0.1.0-alpha.jar \
  com.emergent.doom.validation.LinearScalingValidator \
  --output results/stage2_custom.csv
```

## Expected Outcomes

### If B stays ~0 through 10^18
- **Significance**: Genuinely extraordinary finding
- **Implication**: Remainder landscape exploitation works even for cryptographically hard semiprimes
- **Action**: Publish immediately, document extensively

### If B > 0.5 at some stage
- **Significance**: Found the failure boundary
- **Implication**: Linear scaling limited to certain difficulty range
- **Action**: Characterize what changed (remainder variance, gradient strength)

### If non-convergence within step limits
- **Significance**: Remainder landscape too flat
- **Implication**: Expected outcome for truly cryptographic targets
- **Action**: Validates theoretical predictions

## Analysis Tools

### Python Analysis
Use existing `scripts/analyze_scaling.py` for detailed statistical analysis:
```bash
python3 scripts/analyze_scaling.py validation_results/scaling_validation_results_STAGE_1_E6.csv
```

This produces:
- Linear regression analysis
- R² goodness of fit
- Coefficient of variation
- Residual analysis

## Performance Notes

- **Memory**: Array size 10^6 requires ~8GB RAM for cell storage
- **Execution Time**: 
  - Stage 1: ~5-10 minutes (30 trials × 3 array sizes)
  - Stage 2: ~10-20 minutes
  - Stage 3: ~30-60 minutes (higher step limit)
  - Stage 4: ~2-4 hours (if it runs - likely to terminate early)

## Testing

Test suite available in `src/test/java/com/emergent/doom/validation/LinearScalingValidatorTest.java`:
- Unit tests for all components
- User-story framed test comments
- Covers configuration, prime generation, statistics, B calculation

Run tests:
```bash
mvn test -Dtest=LinearScalingValidatorTest
```

## References

- Original finding: `docs/findings/LINEAR_SCALING_ANALYSIS.md`
- Issue: "Experimental Validation: Test Linear Scaling on Progressively Harder Semiprimes"
- Z-framework analysis: Claude chat 91e7054f-c7e2-4599-ad71-c93f411e2e25
- Experimental protocol agreed upon: 2026-01-01

## Contributing

When extending this system:
1. Follow the Incremental Coder v2 pattern (scaffold → main entry → iterative implementation)
2. Maintain immutability in data structures
3. Add verbose comments explaining reasoning and integration
4. Update this README with new features
5. Ensure compatibility with existing CSV schema

## License

Part of the Emergent Doom Engine project. See LICENSE file in repository root.
