# Trajectory Data Export

This package provides functionality for exporting experiment trajectories to CSV format for analysis and validation.

## Overview

The trajectory export system enables exporting step-by-step execution data from experiments, providing the detailed metrics required for metric dashboard validation as specified in the Levin et al. (2024) paper.

## Core Components

### ExperimentTrajectory

Immutable data structure that encapsulates complete trajectory data for a single experiment run.

**Components:**
- `TrajectoryStep`: Per-step metrics including:
  - `stepNumber`: Execution step (0-based)
  - `sortedness`: Percentage of cells in correct relative order (0-100%)
  - `monotonicityError`: Count of adjacent inversions
  - `aggregation`: Percentage of cells with same-type neighbors (null for non-chimeric)
  - `cumulativeSwaps`: Total swaps executed up to this step
  - `cumulativeComparisons`: Total comparisons executed up to this step

- `ExperimentMetadata`: Context information including:
  - `algotype`: Algorithm type (e.g., "Bubble", "Fib")
  - `frozenCells`: Number of frozen (immovable) cells
  - `trialNumber`: Trial number within experiment batch
  - `arraySize`: Size of the cell array
  - `timestamp`: Unix timestamp (milliseconds) when experiment started

### TrajectoryBuilder

Utility class for building `ExperimentTrajectory` from `Probe` snapshots.

**Features:**
- Automatically computes metrics from snapshots
- Detects chimeric experiments and includes aggregation data
- Validates all inputs

### TrajectoryDataExporter

Exports trajectories to CSV format with metadata and per-step data.

**Export Format:**
```csv
# Metadata
algotype,Bubble
frozen_cells,2
trial_number,0
array_size,50
timestamp,1704672000000
# Trajectory Data
step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons
0,45.2,15,0,50
1,52.1,12,3,100
2,68.7,8,7,150
```

For chimeric experiments, an `aggregation` column is included between `monotonicity_error` and `cumulative_swaps`.

## Usage Example

```java
// Step 1: Record snapshots during experiment
Probe<GenericCell> probe = new Probe<>();

// Initial state
GenericCell[] step0 = { /* ... */ };
probe.recordSnapshot(0, step0, 0);

// After some swaps
GenericCell[] step1 = { /* ... */ };
probe.recordSnapshot(1, step1, 3);

// ... more steps ...

// Step 2: Build trajectory from probe
ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
    probe,
    "Bubble",          // algotype
    0,                 // frozenCells
    0,                 // trialNumber
    50,                // arraySize
    System.currentTimeMillis()
);

// Step 3: Export to CSV
TrajectoryDataExporter.exportTrajectoryToCSV(
    "experiments/data/bubble_trial_000_trajectory.csv",
    trajectory
);
```

## Use Cases

### Metric Dashboard Validation

Export trajectories to validate metrics from the Levin et al. (2024) paper:

- **Delayed Gratification (DG):** Requires `sortedness` history to detect temporary decreases that enable later gains
- **Aggregation Peaks:** Requires `aggregation` history to identify maximum clustering and timing
- **Monotonicity Error:** Requires full trajectory to track error tolerance dynamics
- **Error Tolerance Analysis:** Compare trajectories across frozen cell counts (0, 1, 2, 3)

### Data Analysis Pipelines

CSV files can be imported into:
- Python (pandas, numpy, matplotlib)
- R (tidyverse, ggplot2)
- Excel/Google Sheets
- Jupyter notebooks
- Web-based visualization tools

### Reproducibility

Complete trajectory data enables:
- Experiment replay
- Detailed debugging
- Publication-quality figures
- Peer review validation

## File Naming Convention

Recommended naming pattern for trajectory files:
```
{algotype}_{frozen}frozen_trial{trial:03d}_trajectory.csv
```

Examples:
- `Bubble_0frozen_trial000_trajectory.csv`
- `Fib_2frozen_trial005_trajectory.csv`
- `Chimeric_1frozen_trial012_trajectory.csv`

## Testing

See `src/test/java/com/emergent/doom/export/`:
- `ExperimentTrajectoryTest`: 15 tests validating data structures
- `TrajectoryExportTest`: 6 tests validating export functionality
- `TrajectoryExportExampleTest`: Complete pipeline demonstration

## Implementation Notes

### Comparison Count Heuristic

⚠️ **WARNING:** The comparison count is a rough heuristic and may be inaccurate.

Since `Probe` doesn't track per-snapshot comparisons, `TrajectoryBuilder` uses a heuristic:
```
cumulativeComparisons = cumulativeSwaps + (stepNumber + 1) * arraySize
```

This assumes `arraySize` comparisons per step, which is **not accurate** for most sorting algorithms:
- Bubble sort performs fewer comparisons as the array becomes sorted
- Selection sort has a specific comparison pattern that doesn't scale linearly with steps
- The estimate becomes increasingly incorrect as steps increase

**Use comparison count data with caution for metric validation.** For accurate tracking, enhance `Probe` to record actual comparison counts per snapshot.

### Chimeric Detection

⚠️ **IMPORTANT:** Chimeric detection requires `AlgotypedProbe` or snapshot metadata with valid algotype labels.

The standard `Probe` class always sets `algotypeLabel` to `-1` in its snapshots (see `Probe.java:59`), which means:
- Chimeric detection will always return `false` when using standard `Probe`
- Aggregation metrics will **not** be computed or exported
- The aggregation column will be omitted from the CSV

To export aggregation data for chimeric experiments:
1. Use `AlgotypedProbe` instead of `Probe` for recording snapshots
2. `AlgotypedProbe` reads algotypes from cell objects via `cell.getAlgotype()`
3. This populates valid algotype labels (0, 1, 2) in snapshot metadata
4. `TrajectoryBuilder` will detect multiple algotypes and include aggregation data

**Example for chimeric experiments:**
```java
AlgotypedProbe probe = new AlgotypedProbe();
// ... record snapshots with algotyped cells ...
ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
    probe, "Chimeric", 0, 0, 50, System.currentTimeMillis()
);
// Aggregation column will be included in CSV export
```

## Related Classes

- `com.emergent.doom.probe.Probe`: Records execution snapshots
- `com.emergent.doom.probe.StepSnapshot`: Immutable snapshot of cell states
- `com.emergent.doom.metrics.Monotonicity`: Computes sortedness
- `com.emergent.doom.metrics.MonotonicityError`: Computes inversion count
- `com.emergent.doom.metrics.AlgotypeAggregationIndex`: Computes clustering

## References

- Levin et al. (2024), "Collective Intelligence of Morphogenesis..."
- Issue: "Implement Full Trajectory Data Export for Metric Dashboard Validation"
