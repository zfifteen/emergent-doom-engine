# Export Package Tests

---
**Documentation Version:** Draft  
**Last Updated:** 2026-01-08 (TestWeaver automated maintenance)

**Recent Changes:**
- Added comprehensive TrajectoryExportValidationTest.java with 19 test methods
- Created package README with test catalog and usage examples
- Documented mathematical verification patterns for metric calculations
---

## Purpose

This package validates the trajectory export framework that enables experimental data collection, CSV export, and dashboard validation for the Emergent Doom Engine. Tests ensure accurate metric calculation (sortedness, monotonicity error, swap counts), robust edge case handling, and correct CSV format for analysis tool compatibility.

## Concepts Covered

- **Trajectory Building**: Converting probe snapshots into structured experiment trajectories
- **Metric Calculation**: Monotonicity error (adjacent inversions), sortedness percentage, cumulative operations
- **CSV Export**: Pandas/R-compatible format with metadata comments and data rows
- **Mathematical Verification**: Theoretical bounds validation (e.g., bubble sort worst-case swaps)
- **Delayed Gratification Detection**: Non-monotonic sortedness trajectories (temporary worsening before improvement)
- **Partial Convergence**: Handling incomplete sorting scenarios (timeouts, frozen cells)

## Prerequisites

Before studying these tests, you should understand:
- **[probe/](../probe/README.md)** - Snapshot recording and trajectory capture
- **[metrics/](../metrics/README.md)** - Sortedness, monotonicity, and quality measures
- **[experiment/](../experiment/README.md)** - Multi-trial experiment execution

## Test Files

### Core Integration Tests

#### [TrajectoryExportTest.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/TrajectoryExportTest.java)
Basic integration tests for trajectory building and CSV export pipeline. Validates happy-path functionality: building trajectories from probe snapshots, correct metric calculation, CSV file creation, and chimeric trajectory support.

**Key Tests:**
- `buildTrajectoryFromProbeSnapshots()` - Constructs trajectory from recorded snapshots
- `trajectoryStepsContainCorrectMetrics()` - Validates sortedness and error calculations
- `exportTrajectoryToCsvFile()` - Creates CSV with metadata and data rows
- `exportChimericTrajectoryIncludesAggregation()` - Handles mixed-algotype trajectories

### Comprehensive Validation Suite

#### [TrajectoryExportValidationTest.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/TrajectoryExportValidationTest.java)
**NEW: Comprehensive validation suite** addressing gaps identified in THREE_EXPERIMENT_FINDINGS.md (January 7, 2026). Provides rigorous mathematical verification, edge case coverage, and format validation across 6 nested test categories.

**Category 1: Monotonicity Error Calculation** (4 tests)
- `countsAdjacentInversionsNotTotalInversions()` - Verifies metric uses n-1 adjacent inversions, not C(n,2) total inversions
- `fullySortedArrayHasZeroError()` - Boundary condition: sorted array → error=0
- `partiallySortedArrayCountsOnlyAdjacentInversions()` - Mixed order: [1,3,2,4,5] → error=1
- `singleElementArrayHasZeroError()` - Edge case: size=1 → error=0

**Category 2: Swap Count Verification** (4 tests)
- `trajectoryPreservesSwapCountsFromProbe()` - Data preservation from probe to trajectory
- `alreadySortedInputRequiresZeroSwaps()` - Sorted input → swaps=0
- `singleSwapScenarioIsTrackedAccurately()` - One inversion → swaps=1
- `swapCountRespectsTheoreticalBound()` - Worst-case ≤ n(n-1)/2 for bubble sort

**Category 3: Boundary Conditions** (4 tests)
- `singleElementArrayIsTriviallySorted()` - Size=1 → 100% sorted, 0 error
- `twoElementArraysHandleBothCases()` - Size=2 in sorted and reversed order
- `alreadySortedInputIsImmediatelyRecognized()` - Pre-sorted → 100% sortedness from start
- `duplicateElementsAreHandledCorrectly()` - Arrays with repeated values

**Category 4: Partial Convergence** (2 tests)
- `trajectoryCapturesPartialConvergence()` - Incomplete sorting (sortedness < 100%)
- `csvExportSucceedsWithPartialConvergence()` - CSV export handles incomplete data

**Category 5: Delayed Gratification Detection** (2 tests)
- `trajectoryDetectsSortednessDecreaseFollowedByIncrease()` - Non-monotonic sortedness trajectory
- `documentsConditionsThatEnableSortednessRegression()` - Theoretical conditions for DG events

**Category 6: CSV Format Validation** (3 tests)
- `allCommentLinesStartWithHashCharacter()` - Pandas/R compatibility: comments start with '#'
- `csvHeadersMatchExpectedSchema()` - Correct column headers for chimeric and non-chimeric
- `csvStructureIsCompatibleWithPandasParsing()` - File structure supports clean parsing

### Example Tests

#### [TrajectoryExportExampleTest.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/TrajectoryExportExampleTest.java)
Demonstrates trajectory export workflow for documentation purposes. Shows complete pipeline from experiment execution through CSV export.

#### [ExperimentTrajectoryTest.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/ExperimentTrajectoryTest.java)
Unit tests for ExperimentTrajectory data structure. Validates immutability, input validation, and metadata handling.

### Test Runners

#### [ThreeExperimentTestRunner.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/ThreeExperimentTestRunner.java)
Initial validation test runner that produced THREE_EXPERIMENT_FINDINGS.md. Executes three distinct experiments (small, medium, large arrays) to validate framework functionality.

#### [ManualIntegrationTest.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/ManualIntegrationTest.java)
Manual integration tests for development workflows. Disabled in CI, used for local validation.

#### [FalsificationTestRunner.java](https://github.com/zfifteen/emergent-doom-engine/blob/main/src/test/java/com/emergent/doom/export/FalsificationTestRunner.java)
Experimental test runner for falsification-based validation approaches.

## Usage Examples

### Building Trajectory from Probe

```java
// Create probe and record snapshots during experiment execution
Probe<GenericCell> probe = new Probe<>();

GenericCell[] initialState = {
    new GenericCell(5), new GenericCell(4), new GenericCell(3),
    new GenericCell(2), new GenericCell(1)
};
probe.recordSnapshot(0, initialState, 0);

GenericCell[] finalState = {
    new GenericCell(1), new GenericCell(2), new GenericCell(3),
    new GenericCell(4), new GenericCell(5)
};
probe.recordSnapshot(10, finalState, 10);

// Build trajectory with metadata
ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
    probe,
    "BubbleSort",           // algotype
    2,                       // frozenCells
    0,                       // trialNumber
    5,                       // arraySize
    System.currentTimeMillis()
);

// Access trajectory data
assertEquals(2, trajectory.getStepCount());
assertEquals("BubbleSort", trajectory.getMetadata().algotype());
```

### Exporting to CSV

```java
// Create trajectory (see above)
ExperimentTrajectory trajectory = ...;

// Export to CSV file
String outputPath = "experiments/data/trajectory.csv";
TrajectoryDataExporter.exportTrajectoryToCSV(outputPath, trajectory);

// Resulting CSV structure:
// # Metadata
// algotype,BubbleSort
// frozen_cells,2
// trial_number,0
// array_size,5
// timestamp,1704672000000
// # WARNING: cumulative_comparisons is a rough heuristic - use with caution
// # Trajectory Data
// step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons
// 0,20.0,4,0,5
// 10,100.0,0,10,55
```

### Mathematical Verification Pattern

TestWeaver enforces mathematical verification comments on all metric assertions:

```java
@Test
void verifyMonotonicityErrorCalculation() {
    // TestWeaver: Mathematical verification
    // For reversed array [5,4,3,2,1]:
    //   Adjacent inversions: (5,4), (4,3), (3,2), (2,1) = 4 pairs
    //   Total inversions: C(5,2) = 5×4/2 = 10 pairs
    // MonotonicityError metric uses adjacent inversions → Expected: 4
    
    Probe<GenericCell> probe = new Probe<>();
    
    int arraySize = 5;
    int expectedAdjacentInversions = arraySize - 1;  // n-1 for fully reversed
    
    GenericCell[] reversedCells = {
        new GenericCell(5), new GenericCell(4), new GenericCell(3),
        new GenericCell(2), new GenericCell(1)
    };
    probe.recordSnapshot(0, reversedCells, 0);
    
    ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
        probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
    );
    
    assertEquals(expectedAdjacentInversions, 
        trajectory.getSteps().get(0).monotonicityError(),
        "Monotonicity error should count adjacent inversions (n-1), " +
        "not total inversions C(n,2)");
}
```

### Named Constants for Magic Numbers

All numeric literals are extracted to named constants with explanatory comments:

```java
/** Bubble sort worst-case swaps for 5-element array: 5×4/2 = 10 */
private static final int WORST_CASE_SWAPS_SIZE_5 = 10;

/** Bubble sort worst-case swaps for 15-element array: 15×14/2 = 105 */
private static final int WORST_CASE_SWAPS_SIZE_15 = 105;

@Test
void swapCountRespectsTheoreticalBound() {
    int arraySize = 5;
    int maxTheoreticalSwaps = WORST_CASE_SWAPS_SIZE_5;  // Use constant
    
    // Test implementation...
}
```

## Key Formulas Verified

| Metric | Formula | Reference |
|--------|---------|----------|
| **Sortedness** | `(cells in monotonic order with predecessor / n) × 100` | Levin et al. (2024), p.8 |
| **Monotonicity Error** | `count of adjacent inversions` | MonotonicityError.java |
| **Adjacent Inversions** | `pairs (i, i+1) where cells[i] > cells[i+1]` | Range: [0, n-1] |
| **Worst-Case Swaps (Bubble)** | `n(n-1)/2` for fully reversed array | Algorithmic complexity theory |
| **Cumulative Comparisons** | `cumulativeSwaps + (stepNumber + 1) × arraySize` | TrajectoryBuilder.java (HEURISTIC) |

**Note on Cumulative Comparisons:** This is a rough heuristic documented in TrajectoryBuilder.java. The formula does NOT accurately reflect actual sorting behavior. See TrajectoryBuilder.java:100-117 for detailed explanation of limitations.

## Gaps Addressed from THREE_EXPERIMENT_FINDINGS.md

The TrajectoryExportValidationTest suite addresses these findings from the January 7, 2026 report:

1. ✅ **Monotonicity Error Definition Ambiguity**: Test explicitly verifies adjacent inversions (n-1) vs total inversions C(n,2)
2. ✅ **Swap Count Mathematical Verification**: Tests validate against theoretical worst-case bounds
3. ✅ **Missing Negative Test Cases**: Added edge cases (size=1, size=2, partial convergence)
4. ✅ **Delayed Gratification Gap**: Tests demonstrate non-monotonic sortedness trajectories
5. ✅ **CSV Format Validation**: Tests verify pandas/R parsing compatibility

## Next Steps

After mastering trajectory export, proceed to:
- **[analysis/](../analysis/README.md)** - Post-experiment trajectory analysis and pattern detection
- **[visualization/](../visualization/README.md)** - Graphical representation of trajectory data
- **[validation/](../validation/README.md)** - End-to-end system validation and scaling behavior

---

**Package Purpose:** Trajectory export framework validation  
**Test Count:** 25+ tests across 7 files  
**Coverage:** Metric calculation, CSV export, edge cases, format compatibility  
**Reference:** THREE_EXPERIMENT_FINDINGS.md (January 7, 2026)
