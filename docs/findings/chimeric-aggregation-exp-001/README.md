# Chimeric Aggregation Experiment 001
**Experiment ID:** chimeric-aggregation-exp-001  
**Date:** 2026-01-06  
**Status:** ✅ Complete  
**Researcher:** DataGenAgent

---

## Experiment Overview

### Research Question
**"How do mixed-algotype populations spontaneously self-organize, and does this clustering impact sorting convergence?"**

### Hypothesis
Mixed-algotype cell populations will exhibit:
1. **Spontaneous clustering:** Cells of the same algotype will aggregate over time, creating local neighborhoods
2. **Emergent organization:** Aggregation percentage will increase from random baseline (~50% for binary mix) toward higher values
3. **Performance impact:** Chimeric populations may converge faster or slower than homogeneous populations depending on algotype synergies

### Scientific Context
This experiment directly tests the Levin et al. (2024) hypothesis that collective intelligence emerges from diverse strategy populations navigating problem spaces. In the EDE framework, "algotypes" represent different local problem-solving strategies (Bubble, Selection, Insertion), and their spatial organization may reveal principles of multi-agent coordination.

---

## Experimental Design

### Independent Variables (Parameter Space)

| Parameter | Values | Rationale |
|:----------|:-------|:----------|
| **Array Size** | 30, 50, 100 | Test scalability of aggregation patterns |
| **Algotype Mix** | 100% Bubble (control)<br>50/50 Bubble/Selection<br>33/33/33 Bubble/Selection/Insertion | Compare homogeneous baseline with binary and ternary mixtures |
| **Frozen Cells** | 0, 1, 3 | Test robustness of clustering under substrate constraints |
| **Random Seeds** | 42, 123, 789 | Ensure reproducibility and statistical validity |
| **Max Steps** | 5000 | Allow sufficient time for clustering emergence |

**Total Configurations:** 3 sizes × 3 mixes × 3 frozen × 3 seeds = **81 experimental runs**

### Dependent Variables (Measured Per Step)

1. **Aggregation Percentage** (`aggregation_pct`)
   - **Definition:** Percentage of cells with at least one same-algotype neighbor
   - **Range:** 0-100%
   - **Expected Behavior:** Increase over time as cells cluster

2. **Sortedness Percentage** (`sortedness_pct`)
   - **Definition:** Percentage of cells in correct final sorted position
   - **Range:** 0-100%
   - **Expected Behavior:** Monotonic increase (with possible DG dips)

3. **Monotonicity Percentage** (`monotonicity_pct`)
   - **Definition:** Percentage of cells >= their predecessor
   - **Range:** 0-100%
   - **Expected Behavior:** Increase toward 100% as array sorts

4. **Swap Count** (`swap_count`)
   - **Definition:** Cumulative swaps executed
   - **Range:** 0-∞
   - **Expected Behavior:** Linear or sublinear growth

5. **Step Number** (`step_number`)
   - **Definition:** Iteration count
   - **Range:** 0-5000
   - **Unit:** Discrete steps

### Control Conditions
- **Baseline:** 100% Bubble algotype (homogeneous population, no clustering possible)
- **Comparison Metric:** Does chimeric aggregation correlate with convergence speed?

---

## Methodology

### Test Implementation Strategy

1. **Augment ChimericPopulationTest** with data generation variant:
   - Create `ChimericAggregationDataGenTest.java` in `com.emergent.doom.datagen` package
   - Reuse existing `ChimericPopulation`, `PercentageAlgotypeProvider`, `GenericCellFactory`
   - Instrument `ExperimentRunner` to capture per-step metrics

2. **Metric Calculation:**
   - **Aggregation:** Iterate cells, count neighbors with same algotype
   - **Sortedness:** Use existing `SortednessValue` metric
   - **Monotonicity:** Use existing `Monotonicity` metric
   - **Swaps:** Track from probe data

3. **Data Export:**
   - CSV format: One row per step per configuration
   - Columns: `step_number,aggregation_pct,sortedness_pct,monotonicity_pct,swap_count,array_size,algotype_mix,frozen_count,seed`
   - Metadata JSON: Parameter grid, metric definitions, generation timestamp

### Expected Execution Time
- ~1-2 seconds per configuration × 81 configs = **2-3 minutes total**
- CSV generation: <30 seconds
- **Total experiment runtime:** ~5 minutes

---

## Expected Outcomes

### Predicted Patterns

1. **Aggregation Trajectory:**
   - Initial: ~33-50% (random distribution baseline)
   - Mid-experiment: Gradual increase as swaps create local clusters
   - Final: 70-90% (stable clustered state)

2. **Convergence Comparison:**
   - **Homogeneous (control):** Standard convergence curve
   - **Chimeric (50/50):** Potentially faster due to strategy diversity OR slower due to coordination overhead
   - **Chimeric (33/33/33):** More complex dynamics, possibly intermediate performance

3. **Frozen Cell Impact:**
   - Frozen cells disrupt clustering by "pinning" cells in place
   - Expected: Lower final aggregation % with more frozen cells

### Discovery Opportunities

**Unexpected patterns to watch for:**
- Non-monotonic aggregation (clustering then dispersal)
- Phase transitions at specific algotype ratios
- Correlation between aggregation peaks and delayed gratification events
- Emergent "fault lines" between algotype clusters

---

## Data Artifacts

### Primary Outputs
- `chimeric_aggregation_timeseries.csv` - Full time-series data (~400k rows)
- `chimeric_aggregation_metadata.json` - Experiment metadata and metric definitions
- `ChimericAggregationDataGenTest.java` - Executable test code

### Supplementary Outputs
- This README documenting methodology
- `FINDINGS.md` with analysis and conclusions (created post-experiment)
- Visualization plots (if generated)

---

## Reproducibility

### To Reproduce This Experiment:
```bash
# Run the data generation test
mvn test -Dtest=ChimericAggregationDataGenTest

# Output files written to:
# docs/findings/chimeric-aggregation-exp-001/chimeric_aggregation_timeseries.csv
# docs/findings/chimeric-aggregation-exp-001/chimeric_aggregation_metadata.json
```

### Environment:
- Java 11
- Maven 3.x
- JUnit 5.9.2
- EDE Framework v0.3.0-alpha
- Platform: Linux x86_64

---

## Experiment Log

### 2026-01-06 02:58 UTC - Experiment Initialization
- Created experiment directory structure
- Documented methodology and expected outcomes
- Status: Ready to implement test code

### 2026-01-06 03:05 UTC - Experiment Execution Complete
- Implemented `ChimericAggregationDataGenTest.java`
- Executed 81 experimental configurations
- Generated 6,636 time-series data points in 0.40 seconds
- Exported CSV and metadata JSON successfully

### 2026-01-06 03:06 UTC - Analysis Complete
- Analyzed experimental results
- Documented key findings in FINDINGS.md
- **Key Discovery:** Persistent high aggregation (100%) across all chimeric mixes
- All configurations achieved >99.5% sortedness convergence

### Next Steps:
1. ✅ Create experiment directory and README
2. ✅ Implement `ChimericAggregationDataGenTest.java`
3. ✅ Execute experiment
4. ✅ Generate CSV and metadata
5. ✅ Analyze results and document findings

**Status:** ✅ COMPLETE

---

**Last Updated:** 2026-01-06 03:06 UTC  
**Git Branch:** copilot/document-experimental-findings
