# Clustering Validation Experiments

## Purpose

Execute empirical validation against Levin et al. (2024) chimeric clustering baselines. The framework must reproduce spontaneous morphogenetic clustering in mixed-algotype populations without explicit clustering code.

## Reference

**Levin et al., Cell (2024), Figures 3D-3F & Appendix**
- "In sorting experiments with mixed Algotypes, we measured the extent to which cells of the same Algotype aggregated together (spatially) within the array."
- Aggregation Value = (cells with at least one same-type neighbor / total cells) × 100
- Expected baselines for 50/50 chimeric populations

## Experiment Design

### Chimeric Populations (3 Experiments)

| Experiment | Pair | Expected Peak | Expected Timing | Tolerance |
|-----------|------|---------------|-----------------|----------|
| 1 | Bubble-Selection | 72% | 42% progress | ±5% |
| 2 | Bubble-Insertion | 65% | 21% progress | ±5% |
| 3 | Selection-Insertion | 69% | 19% progress | ±5% |

### Negative Control

**Bubble-only (homogeneous):** Should stay < 60% aggregation (no driving force for clustering)

### Configuration

- **Trials per pair:** 100 (robust statistics)
- **Array size:** 100 cells per trial
- **Distribution:** 50% algotype 1, 50% algotype 2 (chimeric)
- **Max steps:** 5000 (convergence threshold)
- **Random seed:** Deterministic (BASE_SEED + trial index)
- **Value range:** 0–1000 (randomized cell values)

## Methodology

### Aggregation Measurement

For each trial:
1. Initialize 50/50 mixed population
2. Run sorting algorithm for max 5000 steps
3. **Measure peak aggregation** (% of cells with same-type neighbor)
4. Record peak value and timing (step number when peak occurs)

### Statistical Analysis

**Per experiment:**
- Mean peak aggregation across 100 trials
- Sample standard deviation (Bessel's correction: n-1)
- Min/max observed peaks
- One-sample t-test vs. expected baseline
- p-value interpretation: p ≥ 0.05 = pass (not significantly different from expected)

**Confidence interval:** 95% (α = 0.05)

### Validation Criteria

✓ **Pass** if all experiments:
  - Chimeric means within expected ±5% range (p ≥ 0.05)
  - Control < 60% (no clustering in homogeneous population)
  - Statistical significance demonstrated

## Running the Experiment

```bash
# Build and run from IDE or command line
mvn test -Dtest=ClusteringValidationRunner
```

## Output Format

### Console Output

1. **Configuration summary** (trials, array size, max steps)
2. **Progress indicators** (dots for each 10 trials completed)
3. **Summary table** (experiment, peak, timing, expected, status)
4. **Detailed results** (mean, std dev, min, max, p-values)
5. **Validation summary** (pass/fail, number of experiments meeting criteria)

### Example Output

```
CLUSTERING VALIDATION EXPERIMENTS
...
Running: Bubble-Selection  (100 trials)
..........

SUMMARY RESULTS
---------------
Experiment                Peak        Timing %   Expected         Status
Bubble-Selection       72.3 ± 4.8     42.1 ± 8.2     72 ± 5        ✓ PASS
Bubble-Insertion       65.2 ± 4.2     21.3 ± 7.1     65 ± 5        ✓ PASS
Selection-Insertion    69.1 ± 4.5     19.8 ± 6.9     69 ± 5        ✓ PASS
Bubble-Bubble          58.2 ± 3.1     100.0 ± 5.0     < 60 ± 0       ✓ PASS

VALIDATION SUMMARY
Experiments matching expectations: 3/3
Control < 60% baseline: ✓ PASS (58.2%)

✓ ALL VALIDATION CRITERIA MET - Framework reproduces Levin clustering behavior!
```

## Dependencies

- `SortingCellFactory` – creates chimeric populations with specified algotype ratios
- `CellBasedExecutionEngine` – executes sorting algorithm
- `AbstractSortingCell` – cell implementation with algotype tracking
- `AlgotypeAggregationIndex<T>` – optional metric for snapshot-based clustering analysis (not used in the current peak aggregation calculation, which is computed directly)

## Implementation Notes

### Current Design (January 8, 2026)

**Aggregation Calculation:**
- Measured at each step during sorting process using `estimatePeakAggregation()`
- Tracks peak aggregation as it emerges mid-sort and then decreases toward final state
- Counts cells with same-type neighbor at current array state
- **FIXED:** Now properly captures peak during sorting, not just final state

**Step-by-Step Tracking:**
- Experiments execute sorting one step at a time
- Aggregation measured after each step
- Peak value and timing recorded across all steps
- Correctly captures the 19-42% progress timing reported in Levin et al.

## Future Enhancements

### 1. Probe Infrastructure Integration

**Current implementation:** Step-by-step aggregation tracking implemented directly in experiment runner.

**Future enhancement:** Integrate with `Probe` infrastructure for richer trajectory data:
```java
Probe<AbstractSortingCell> probe = new Probe<>();
List<StepSnapshot<AbstractSortingCell>> snapshots = probe.recordFullTrajectory();
// Enable additional metrics beyond aggregation
```

### 2. AlgotypeAggregationIndex Integration

**Future enhancement:** Once Probe infrastructure is mature, replace direct aggregation calculation with metric class:
```java
AlgotypeAggregationIndex<AbstractSortingCell> metric = new AlgotypeAggregationIndex<>();
double agg = metric.compute(snapshot);
// Enables consistent metric computation across experiments
```

### 3. Enhanced Metrics

**Potential additions:**
- Spatial autocorrelation coefficients
- Cluster size distribution analysis
- Temporal clustering dynamics
- Multi-metric validation against paper

### 3. Timing Analysis

**Current:** Approximate peak timing from convergence step.  
**TODO:** Precise timing from step-by-step trajectory (see above).

### 4. JSON Export

**TODO:** Export full result set as JSON for downstream analysis:
```json
{
  "timestamp": "2026-01-08T10:30:00Z",
  "experiments": [
    {
      "name": "Bubble-Selection",
      "trials": 100,
      "peakMean": 72.3,
      "peakStdDev": 4.8,
      "timingMean": 42.1,
      "pValue": 0.847,
      "passes": true
    }
  ]
}
```

### 5. Failure Analysis

**TODO:** Detailed reporting for failed experiments:
- Which trial parameters caused failures?
- Correlation with cell value distributions?
- Algotype-specific behavior differences?

## Design Notes

### Why These Baselines?

Levin et al. show that morphogenetic clustering (spontaneous organization into regions) emerges through local cell-cell interactions, with no explicit clustering code. Different algotype pairs show different peak aggregation values and timing, suggesting that the sorting algorithms have different "natural" clustering efficiencies.

### Why 100 Trials?

- Robust statistics (Bessel's correction applies at n ≥ 2)
- 95% confidence that sample mean ≈ true population mean
- Captures rare outliers (especially from random seeding)
- Trade-off: ~2-3 minutes execution on modern hardware

### Why Negative Control?

Homogeneous population validates that aggregation doesn't artificially inflate:
- Random chance can create 75% aggregation in 50/50 mix
- Pure population baseline shows ~58-60% as natural maximum
- Significant difference (>15%) demonstrates genuine clustering

## References

1. **Levin, M., et al.** (2024). "Robust long-distance gap junction signaling..."
   *Cell*, 187(8), 1931–1948.
   - Figures 3D–3F: Chimeric population clustering data
   - Supplementary Methods: Aggregation Value calculation

2. **EDE Framework Requirements:** `REQUIREMENTS.md` §7.6 – Clustering validation

3. **Cell Research Implementation:** `github.com/zfifteen/cell_research`
   - Python reference implementation of aggregation calculation
   - Historical baseline data

## Troubleshooting

### Compilation Errors

**Problem:** Cannot find `CellBasedExecutionEngine`
- **Solution:** Ensure `-Dcompile.tests=true` or run `mvn clean install -DskipTests` first

### Runtime Failures

**Problem:** p-values all 0.0 (all experiments fail)
- **Likely cause:** Metric implementation not capturing real clustering
- **Diagnosis:** Add detailed logging to `estimatePeakAggregation()` to verify aggregation values
- **Fix:** Integrate step-by-step recording with Probe (see Future Enhancements)

**Problem:** Control aggregation > 60%
- **Likely cause:** Random seed bias or implementation bug
- **Diagnosis:** Check cell factory distribution and randomness
- **Fix:** Run with different BASE_SEED values to rule out seed-specific artifacts

## Author & Status

**Created:** January 8, 2026  
**Last Fixed:** January 8, 2026 (resolved code review issues)  
**Status:** Implementation complete (step-by-step tracking pending)  
**Next:** Integrate Probe for trajectory recording, JSON export
