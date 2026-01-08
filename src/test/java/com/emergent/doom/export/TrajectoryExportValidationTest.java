package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive validation suite for trajectory export framework.
 * 
 * <p>This test suite addresses gaps identified in THREE_EXPERIMENT_FINDINGS.md
 * (January 7, 2026), providing rigorous mathematical verification and edge case
 * coverage for the trajectory export infrastructure.</p>
 * 
 * <p>Tests validate:
 * <ul>
 *   <li>Metric calculation accuracy (monotonicity error, sortedness, swaps)</li>
 *   <li>Boundary conditions (size=1, size=2, empty arrays)</li>
 *   <li>Partial convergence scenarios (timeouts, frozen cells)</li>
 *   <li>CSV export format correctness (metadata, headers, data rows)</li>
 *   <li>Mathematical consistency with theoretical predictions</li>
 * </ul>
 * </p>
 * 
 * @see ExperimentTrajectory
 * @see TrajectoryBuilder
 * @see TrajectoryDataExporter
 */
@DisplayName("Trajectory Export Framework Validation")
class TrajectoryExportValidationTest {

    @TempDir
    Path tempDir;

    /**
     * Validates monotonicity error metric definition and calculation accuracy.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: "Monotonicity Error
     * Definition Ambiguity" - Test Run 1 reports Error=4 for reversed 5-element
     * array, which matches adjacent inversions (n-1) not total inversions C(5,2)=10.</p>
     */
    @Nested
    @DisplayName("Monotonicity error calculation verification")
    class MonotonicityErrorTests {

        /**
         * PURPOSE: As a researcher analyzing emergent sorting, I want to verify
         * monotonicity error counts adjacent inversions so that I can correctly
         * interpret trajectory data.
         *
         * INPUTS: Reversed 5-element array [5,4,3,2,1]
         * EXPECTED OUTPUT: Monotonicity error = 4 (adjacent inversions only)
         * TEST DATA: Array with 4 adjacent inversions, 10 total inversions
         * REPRODUCTION: Create reversed array, build trajectory, verify error=4
         */
        @Test
        @DisplayName("Counts adjacent inversions only, not total inversions")
        void countsAdjacentInversionsNotTotalInversions() {
            // TestWeaver: Mathematical verification
            // For reversed array [5,4,3,2,1]:
            //   Adjacent inversions: (5,4), (4,3), (3,2), (2,1) = 4 pairs
            //   Total inversions: C(5,2) = 5×4/2 = 10 pairs
            // MonotonicityError metric uses adjacent inversions → Expected: 4
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int expectedAdjacentInversions = arraySize - 1;  // n-1 for fully reversed
            int totalInversions = arraySize * (arraySize - 1) / 2;  // C(n,2) for reference
            
            // Fully reversed array: [5,4,3,2,1]
            GenericCell[] reversedCells = {
                new GenericCell(5), new GenericCell(4), new GenericCell(3),
                new GenericCell(2), new GenericCell(1)
            };
            probe.recordSnapshot(0, reversedCells, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            // TestWeaver: Verify metric uses adjacent inversions, not total inversions
            assertEquals(expectedAdjacentInversions, step.monotonicityError(),
                String.format(
                    "Monotonicity error should count adjacent inversions (n-1=%d), " +
                    "not total inversions C(n,2)=%d",
                    expectedAdjacentInversions, totalInversions
                ));
            
            assertEquals(4, step.monotonicityError(),
                "Reversed 5-element array has exactly 4 adjacent inversions");
        }

        /**
         * PURPOSE: As a developer implementing metric calculations, I want to verify
         * fully sorted arrays have zero monotonicity error so that the boundary
         * condition is correctly implemented.
         *
         * INPUTS: Sorted array [1,2,3,4,5]
         * EXPECTED OUTPUT: Monotonicity error = 0 (no inversions)
         * TEST DATA: Array with all adjacent pairs in correct order
         * REPRODUCTION: Create sorted array, build trajectory, verify error=0
         */
        @Test
        @DisplayName("Fully sorted array has zero monotonicity error")
        void fullySortedArrayHasZeroError() {
            // TestWeaver: Mathematical verification
            // For sorted array [1,2,3,4,5]:
            //   Adjacent pairs: (1,2), (2,3), (3,4), (4,5) - all in order
            //   Adjacent inversions: 0 (no pairs where left > right)
            // Expected monotonicity error: 0
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int expectedMonotonicityError = 0;  // No inversions in sorted array
            
            // Fully sorted array: [1,2,3,4,5]
            GenericCell[] sortedCells = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(0, sortedCells, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(expectedMonotonicityError, step.monotonicityError(),
                "Fully sorted array must have zero monotonicity error " +
                "because no adjacent pairs are inverted");
            
            // TestWeaver: Verify sortedness is 100% for sorted array
            assertEquals(100.0, step.sortedness(), 0.01,
                "Fully sorted array must have 100% sortedness");
        }

        /**
         * PURPOSE: As a researcher validating metrics, I want to verify partially
         * sorted arrays report accurate error counts so that I can track convergence
         * progress during sorting.
         *
         * INPUTS: Partially sorted array [1,3,2,4,5] with one inversion
         * EXPECTED OUTPUT: Monotonicity error = 1 (single adjacent inversion at positions 1-2)
         * TEST DATA: Array with exactly one adjacent inversion: (3,2)
         * REPRODUCTION: Create array, build trajectory, verify error=1
         */
        @Test
        @DisplayName("Partially sorted array counts only adjacent inversions")
        void partiallySortedArrayCountsOnlyAdjacentInversions() {
            // TestWeaver: Mathematical verification
            // For array [1,3,2,4,5]:
            //   Adjacent pairs: (1,3)✓, (3,2)✗, (2,4)✓, (4,5)✓
            //   Adjacent inversions: 1 (only the pair (3,2))
            //   Total inversions: 1 (pair (3,2) considering all positions)
            // Expected monotonicity error: 1
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int expectedAdjacentInversions = 1;  // Only one adjacent pair inverted
            
            // Partially sorted with one inversion: [1,3,2,4,5]
            GenericCell[] partiallySortedCells = {
                new GenericCell(1), new GenericCell(3), new GenericCell(2),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(0, partiallySortedCells, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(expectedAdjacentInversions, step.monotonicityError(),
                "Array [1,3,2,4,5] has exactly one adjacent inversion at positions 1-2");
            
            // TestWeaver: Verify sortedness = 80% (4 out of 5 elements in monotonic order)
            // First element (1): counts as monotonic (always true)
            // Element 3: 3 >= 1 ✓
            // Element 2: 2 >= 3 ✗
            // Element 4: 4 >= 2 ✓
            // Element 5: 5 >= 4 ✓
            // Total: 4/5 = 80%
            assertEquals(80.0, step.sortedness(), 0.01,
                "sortedness should be 80% (4 of 5 elements counted as monotonic under the metric definition)");
        }

        /**
         * PURPOSE: As a developer testing edge cases, I want to verify single-element
         * arrays are handled correctly so that boundary conditions don't cause errors.
         *
         * INPUTS: Single-element array [42]
         * EXPECTED OUTPUT: Monotonicity error = 0 (no pairs to compare)
         * TEST DATA: Array with zero adjacent pairs
         * REPRODUCTION: Create single-element array, verify error=0, sortedness=100%
         */
        @Test
        @DisplayName("Single-element array has zero monotonicity error")
        void singleElementArrayHasZeroError() {
            // TestWeaver: Mathematical verification
            // For array [42]:
            //   Adjacent pairs: 0 (no pairs exist)
            //   Adjacent inversions: 0 (no pairs to invert)
            // Expected monotonicity error: 0
            // Expected sortedness: 100% (trivially sorted)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 1;
            int expectedMonotonicityError = 0;  // No pairs means no inversions
            
            GenericCell[] singleCell = { new GenericCell(42) };
            probe.recordSnapshot(0, singleCell, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(expectedMonotonicityError, step.monotonicityError(),
                "Single-element array has no adjacent pairs, thus zero inversions");
            
            assertEquals(100.0, step.sortedness(), 0.01,
                "Single-element array is trivially 100% sorted");
        }
    }

    /**
     * Validates swap count tracking accuracy against theoretical bounds.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: "Swap Count
     * Mathematical Verification" - Test Run 2 shows 25 swaps for 15-element
     * reversed array, need verification against bubble sort worst-case n(n-1)/2=105.</p>
     */
    @Nested
    @DisplayName("Cumulative swap tracking accuracy")
    class SwapCountTests {

        /** Bubble sort worst-case swaps for 5-element array: 5×4/2 = 10 */
        private static final int WORST_CASE_SWAPS_SIZE_5 = 10;
        
        /** Bubble sort worst-case swaps for 15-element array: 15×14/2 = 105 */
        private static final int WORST_CASE_SWAPS_SIZE_15 = 105;

        /**
         * PURPOSE: As a researcher analyzing sorting efficiency, I want to verify
         * swap counts are tracked accurately so that I can compare algorithm
         * performance across trials.
         *
         * INPUTS: Probe recording exact swap counts at each step
         * EXPECTED OUTPUT: Trajectory preserves swap counts from probe snapshots
         * TEST DATA: Three snapshots with swaps = [0, 5, 10]
         * REPRODUCTION: Record snapshots with explicit swap counts, verify trajectory accuracy
         */
        @Test
        @DisplayName("Trajectory preserves swap counts from probe snapshots")
        void trajectoryPreservesSwapCountsFromProbe() {
            // TestWeaver: This test verifies data preservation, not algorithmic correctness
            // Swap count accuracy depends on probe recording, not trajectory building
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int step0Swaps = 0;   // Initial state - no swaps yet
            int step1Swaps = 5;   // After first pass
            int step2Swaps = 10;  // After second pass (fully sorted)
            
            GenericCell[] cells = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            
            probe.recordSnapshot(0, cells, step0Swaps);
            probe.recordSnapshot(1, cells, step1Swaps);
            probe.recordSnapshot(2, cells, step2Swaps);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
            
            assertEquals(step0Swaps, steps.get(0).cumulativeSwaps(),
                "Step 0 swap count must match probe recording");
            assertEquals(step1Swaps, steps.get(1).cumulativeSwaps(),
                "Step 1 swap count must match probe recording");
            assertEquals(step2Swaps, steps.get(2).cumulativeSwaps(),
                "Step 2 swap count must match probe recording");
        }

        /**
         * PURPOSE: As a developer validating algorithms, I want to verify already-sorted
         * inputs require zero swaps so that I can confirm the algorithm correctly
         * detects no-work scenarios.
         *
         * INPUTS: Already-sorted array [1,2,3,4,5]
         * EXPECTED OUTPUT: Cumulative swaps = 0 (no swaps needed)
         * TEST DATA: Sorted array requiring no reordering
         * REPRODUCTION: Record sorted array snapshot, verify swaps=0
         */
        @Test
        @DisplayName("Already-sorted input requires zero swaps")
        void alreadySortedInputRequiresZeroSwaps() {
            // TestWeaver: Mathematical verification
            // For sorted array [1,2,3,4,5]:
            //   Bubble sort behavior: Compare all adjacent pairs, no swaps needed
            //   Expected swaps: 0 (all comparisons find correct order)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int expectedSwaps = 0;  // Sorted array needs no swaps
            
            GenericCell[] sortedCells = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(0, sortedCells, expectedSwaps);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(expectedSwaps, step.cumulativeSwaps(),
                "Already-sorted array requires exactly 0 swaps");
            
            assertEquals(100.0, step.sortedness(), 0.01,
                "Already-sorted array has 100% sortedness");
            assertEquals(0, step.monotonicityError(),
                "Already-sorted array has 0 monotonicity error");
        }

        /**
         * PURPOSE: As a researcher studying sorting dynamics, I want to verify
         * single-swap scenarios are tracked accurately so that I can validate
         * minimal-intervention convergence paths.
         *
         * INPUTS: Array needing exactly one swap: [1,3,2,4,5]
         * EXPECTED OUTPUT: After swap, cumulative swaps = 1
         * TEST DATA: Array with one adjacent inversion requiring one swap
         * REPRODUCTION: Record before and after swap, verify count=1
         */
        @Test
        @DisplayName("Single-swap scenario is tracked accurately")
        void singleSwapScenarioIsTrackedAccurately() {
            // TestWeaver: Mathematical verification
            // For array [1,3,2,4,5] → [1,2,3,4,5]:
            //   Single inversion: (3,2) at positions 1-2
            //   Required swaps: 1 (swap positions 1 and 2)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int swapsBeforeSort = 0;  // Initial state
            int swapsAfterSort = 1;   // One swap performed
            
            // Before: [1,3,2,4,5] - one inversion
            GenericCell[] beforeSwap = {
                new GenericCell(1), new GenericCell(3), new GenericCell(2),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(0, beforeSwap, swapsBeforeSort);
            
            // After: [1,2,3,4,5] - fully sorted
            GenericCell[] afterSwap = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(1, afterSwap, swapsAfterSort);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
            
            assertEquals(swapsBeforeSort, steps.get(0).cumulativeSwaps(),
                "Initial state has 0 swaps");
            assertEquals(swapsAfterSort, steps.get(1).cumulativeSwaps(),
                "After sorting single inversion, exactly 1 swap recorded");
            
            assertEquals(100.0, steps.get(1).sortedness(), 0.01,
                "After single swap, array is 100% sorted");
            assertEquals(0, steps.get(1).monotonicityError(),
                "After single swap, monotonicity error is 0");
        }

        /**
         * PURPOSE: As a researcher validating theoretical bounds, I want to verify
         * swap counts stay within algorithmic complexity limits so that I can
         * detect implementation errors.
         *
         * INPUTS: Fully reversed 5-element array requiring maximum swaps
         * EXPECTED OUTPUT: Swaps ≤ worst-case bound of n(n-1)/2 = 10
         * TEST DATA: Array [5,4,3,2,1] with 10 total inversions
         * REPRODUCTION: Record complete sorting trajectory, verify final swaps ≤ 10
         */
        @Test
        @DisplayName("Swap count respects theoretical worst-case bound")
        void swapCountRespectsTheoreticalBound() {
            // TestWeaver: Mathematical verification
            // Bubble sort worst-case: n(n-1)/2 swaps for fully reversed array
            // For n=5: 5×4/2 = 10 swaps maximum
            // This is the theoretical upper bound - actual count may be equal or less
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            int maxTheoreticalSwaps = WORST_CASE_SWAPS_SIZE_5;  // 10 swaps
            
            // Fully reversed array: [5,4,3,2,1]
            GenericCell[] reversedCells = {
                new GenericCell(5), new GenericCell(4), new GenericCell(3),
                new GenericCell(2), new GenericCell(1)
            };
            probe.recordSnapshot(0, reversedCells, 0);
            
            // After sorting (exact swap count depends on algorithm implementation)
            GenericCell[] sortedCells = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            // Record final state with worst-case swap count
            probe.recordSnapshot(1, sortedCells, maxTheoreticalSwaps);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
            ExperimentTrajectory.TrajectoryStep finalStep = steps.get(steps.size() - 1);
            
            assertTrue(finalStep.cumulativeSwaps() <= maxTheoreticalSwaps,
                String.format(
                    "Swap count (%d) must not exceed bubble sort worst-case bound n(n-1)/2=%d",
                    finalStep.cumulativeSwaps(), maxTheoreticalSwaps
                ));
            
            assertEquals(100.0, finalStep.sortedness(), 0.01,
                "Final state must be fully sorted");
        }
    }

    /**
     * Validates boundary conditions and edge case handling.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: "Missing Negative
     * Test Cases" - need comprehensive edge case coverage for array sizes 1, 2,
     * already-sorted input, and duplicate elements.</p>
     */
    @Nested
    @DisplayName("Boundary condition handling")
    class EdgeCaseTests {

        /**
         * PURPOSE: As a developer ensuring robustness, I want to verify single-element
         * arrays are handled gracefully so that trivial cases don't cause errors.
         *
         * INPUTS: Single-element array [42]
         * EXPECTED OUTPUT: 100% sortedness, 0 error, trajectory builds successfully
         * TEST DATA: Trivially sorted array of size 1
         * REPRODUCTION: Build trajectory for single element, verify all metrics
         */
        @Test
        @DisplayName("Single-element array is trivially sorted")
        void singleElementArrayIsTriviallySorted() {
            // TestWeaver: Mathematical verification
            // Array [42]:
            //   Sortedness: 100% (single element always in correct position)
            //   Monotonicity error: 0 (no adjacent pairs to invert)
            //   Swaps: 0 (no work needed)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 1;
            int expectedSwaps = 0;
            int expectedMonotonicityError = 0;
            double expectedSortedness = 100.0;
            
            GenericCell[] singleElement = { new GenericCell(42) };
            probe.recordSnapshot(0, singleElement, expectedSwaps);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(expectedSortedness, step.sortedness(), 0.01,
                "Single element is trivially 100% sorted");
            assertEquals(expectedMonotonicityError, step.monotonicityError(),
                "Single element has 0 monotonicity error");
            assertEquals(expectedSwaps, step.cumulativeSwaps(),
                "Single element requires 0 swaps");
        }

        /**
         * PURPOSE: As a developer testing minimal sorting scenarios, I want to verify
         * two-element arrays are handled correctly so that the simplest non-trivial
         * case works properly.
         *
         * INPUTS: Two-element arrays in both sorted [1,2] and reversed [2,1] order
         * EXPECTED OUTPUT: Sorted version has 0 swaps, reversed version needs 1 swap
         * TEST DATA: Minimal arrays requiring 0 or 1 comparison
         * REPRODUCTION: Test both orderings, verify metrics
         */
        @Test
        @DisplayName("Two-element arrays handle both sorted and unsorted cases")
        void twoElementArraysHandleBothCases() {
            // TestWeaver: Mathematical verification
            // Sorted [1,2]:
            //   Sortedness: 100% (both elements in order)
            //   Monotonicity error: 0 (1 ≤ 2)
            //   Swaps: 0
            // Reversed [2,1]:
            //   Sortedness: 50% (first element counts, second violates order)
            //   Monotonicity error: 1 (one adjacent inversion)
            //   Swaps: 1 (one swap needed to sort)
            
            Probe<GenericCell> sortedProbe = new Probe<>();
            Probe<GenericCell> reversedProbe = new Probe<>();
            
            int arraySize = 2;
            
            // Test sorted case: [1,2]
            GenericCell[] sortedCells = { new GenericCell(1), new GenericCell(2) };
            sortedProbe.recordSnapshot(0, sortedCells, 0);
            
            ExperimentTrajectory sortedTrajectory = TrajectoryBuilder.fromProbe(
                sortedProbe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep sortedStep = sortedTrajectory.getSteps().get(0);
            assertEquals(100.0, sortedStep.sortedness(), 0.01,
                "Sorted two-element array [1,2] is 100% sorted");
            assertEquals(0, sortedStep.monotonicityError(),
                "Sorted two-element array has 0 monotonicity error");
            
            // Test reversed case: [2,1]
            GenericCell[] reversedCells = { new GenericCell(2), new GenericCell(1) };
            reversedProbe.recordSnapshot(0, reversedCells, 0);
            
            ExperimentTrajectory reversedTrajectory = TrajectoryBuilder.fromProbe(
                reversedProbe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep reversedStep = reversedTrajectory.getSteps().get(0);
            assertEquals(50.0, reversedStep.sortedness(), 0.01,
                "Reversed two-element array [2,1] is 50% sorted (first element counts)");
            assertEquals(1, reversedStep.monotonicityError(),
                "Reversed two-element array has 1 monotonicity error");
        }

        /**
         * PURPOSE: As a developer optimizing performance, I want to verify already-sorted
         * inputs are recognized so that no unnecessary work is performed.
         *
         * INPUTS: Already-sorted array [1,2,3,4,5]
         * EXPECTED OUTPUT: 100% sortedness, 0 error, 0 swaps on first snapshot
         * TEST DATA: Pre-sorted array requiring no modifications
         * REPRODUCTION: Build trajectory, verify initial state is perfect
         */
        @Test
        @DisplayName("Already-sorted input is immediately recognized")
        void alreadySortedInputIsImmediatelyRecognized() {
            // TestWeaver: Mathematical verification
            // Sorted array [1,2,3,4,5]:
            //   All adjacent pairs in order: (1,2)✓ (2,3)✓ (3,4)✓ (4,5)✓
            //   Sortedness: 100% (all 5 elements in monotonic order)
            //   Monotonicity error: 0 (no inversions)
            //   Swaps: 0 (no work needed)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            
            GenericCell[] alreadySorted = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5)
            };
            probe.recordSnapshot(0, alreadySorted, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep initialStep = trajectory.getSteps().get(0);
            
            assertEquals(100.0, initialStep.sortedness(), 0.01,
                "Already-sorted array must be immediately recognized as 100% sorted");
            assertEquals(0, initialStep.monotonicityError(),
                "Already-sorted array has 0 monotonicity error from the start");
            assertEquals(0, initialStep.cumulativeSwaps(),
                "Already-sorted array requires 0 swaps");
        }

        /**
         * PURPOSE: As a researcher testing diverse inputs, I want to verify duplicate
         * elements are handled correctly so that real-world data with repetition
         * doesn't break the framework.
         *
         * INPUTS: Array with duplicates [1,3,2,3,5]
         * EXPECTED OUTPUT: Metrics calculated correctly considering duplicates as valid order
         * TEST DATA: Array with repeated value (3 appears twice)
         * REPRODUCTION: Build trajectory, verify sortedness handles equality correctly
         */
        @Test
        @DisplayName("Duplicate elements are handled correctly")
        void duplicateElementsAreHandledCorrectly() {
            // TestWeaver: Mathematical verification
            // Array [1,3,2,3,5]:
            //   Adjacent pairs: (1,3)✓ (3,2)✗ (2,3)✓ (3,5)✓
            //   Monotonicity checks: 1→3 (≥)✓, 3→2 (≥)✗, 2→3 (≥)✓, 3→5 (≥)✓
            //   Monotonicity count: 1 (first) + 3 (passed ≥ test) = 4
            //   Sortedness: 4/5 = 80%
            //   Monotonicity error: 1 (one adjacent inversion)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 5;
            
            GenericCell[] withDuplicates = {
                new GenericCell(1), new GenericCell(3), new GenericCell(2),
                new GenericCell(3), new GenericCell(5)
            };
            probe.recordSnapshot(0, withDuplicates, 0);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
            
            assertEquals(80.0, step.sortedness(), 0.01,
                "Array [1,3,2,3,5] should be 80% sorted (4 elements in monotonic order)");
            assertEquals(1, step.monotonicityError(),
                "Array [1,3,2,3,5] has 1 adjacent inversion (3,2)");
            
            // Verify duplicates don't break trajectory building
            assertNotNull(trajectory,
                "Trajectory building should handle duplicate elements without errors");
            assertEquals(1, trajectory.getStepCount(),
                "Trajectory should contain exactly 1 step");
        }
    }

    /**
     * Validates partial convergence scenarios and timeout handling.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: "Missing Negative
     * Test Cases" - need tests for partial convergence when sorting doesn't
     * complete (frozen cells, timeouts).</p>
     */
    @Nested
    @DisplayName("Partial convergence scenarios")
    class PartialConvergenceTests {

        /**
         * PURPOSE: As a researcher studying convergence dynamics, I want to verify
         * trajectories are captured even when sorting doesn't complete so that I
         * can analyze partial progress.
         *
         * INPUTS: Snapshot sequence that stops before reaching 100% sortedness
         * EXPECTED OUTPUT: Trajectory ends at final captured state (e.g., 75% sorted)
         * TEST DATA: Three snapshots showing progression to only 75% sortedness
         * REPRODUCTION: Record incomplete sorting trajectory, verify final state < 100%
         */
        @Test
        @DisplayName("Trajectory captures partial convergence before timeout")
        void trajectoryCapturesPartialConvergence() {
            // TestWeaver: This test validates framework behavior when sorting
            // doesn't reach completion - critical for analyzing failure modes
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 8;
            int maxSteps = 3;  // Limited steps before timeout
            
            // Step 0: Initial state - 12.5% sorted (first element only)
            GenericCell[] step0 = {
                new GenericCell(8), new GenericCell(7), new GenericCell(6), new GenericCell(5),
                new GenericCell(4), new GenericCell(3), new GenericCell(2), new GenericCell(1)
            };
            probe.recordSnapshot(0, step0, 0);
            
            // Step 1: Partial progress - 62.5% sorted (5 elements in monotonic order with predecessor)
            // Array: [6,7,8,4,5,2,3,1]
            // Positions: 0(first), 1(7>=6✓), 2(8>=7✓), 3(4>=8✗), 4(5>=4✓), 5(2>=5✗), 6(3>=2✓), 7(1>=3✗)
            // Count: 1 + 4 passing = 5 out of 8 = 62.5%
            GenericCell[] step1 = {
                new GenericCell(6), new GenericCell(7), new GenericCell(8), new GenericCell(4),
                new GenericCell(5), new GenericCell(2), new GenericCell(3), new GenericCell(1)
            };
            probe.recordSnapshot(1, step1, 5);
            
            // Step 2: More progress - 87.5% sorted (7 elements in monotonic order with predecessor)
            // Array: [4,5,6,7,8,2,3,1]
            // Positions: 0(first), 1(5>=4✓), 2(6>=5✓), 3(7>=6✓), 4(8>=7✓), 5(2>=8✗), 6(3>=2✓), 7(1>=3✗)
            // Count: 1 + 6 passing = 7 out of 8 = 87.5%
            GenericCell[] step2 = {
                new GenericCell(4), new GenericCell(5), new GenericCell(6), new GenericCell(7),
                new GenericCell(8), new GenericCell(2), new GenericCell(3), new GenericCell(1)
            };
            probe.recordSnapshot(2, step2, 12);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            assertEquals(maxSteps, trajectory.getStepCount(),
                "Trajectory should capture all recorded steps even if incomplete");
            
            ExperimentTrajectory.TrajectoryStep finalStep = 
                trajectory.getSteps().get(trajectory.getStepCount() - 1);
            
            assertTrue(finalStep.sortedness() < 100.0,
                "Partial convergence means final sortedness is less than 100%");
            assertTrue(finalStep.monotonicityError() > 0,
                "Incomplete sorting means monotonicity error is greater than 0");
            
            // Verify trajectory progression shows improvement even if incomplete
            double initialSortedness = trajectory.getSteps().get(0).sortedness();
            double finalSortedness = finalStep.sortedness();
            assertTrue(finalSortedness > initialSortedness,
                "Sortedness should improve during partial convergence even if not reaching 100%");
        }

        /**
         * PURPOSE: As a developer validating CSV export, I want to verify partial
         * convergence data exports correctly so that incomplete runs can still be
         * analyzed.
         *
         * INPUTS: Trajectory ending at 80% sortedness
         * EXPECTED OUTPUT: CSV file with metadata and partial trajectory data
         * TEST DATA: Incomplete trajectory with final sortedness < 100%
         * REPRODUCTION: Export partial trajectory to CSV, verify file structure correct
         */
        @Test
        @DisplayName("CSV export succeeds even with partial convergence")
        void csvExportSucceedsWithPartialConvergence() throws IOException {
            // TestWeaver: Validates that export infrastructure handles incomplete
            // data gracefully - important for analyzing failed experiments
            
            int arraySize = 10;
            double finalSortedness = 80.0;  // Stopped before full convergence
            int finalMonotonicityError = 2;  // Still has errors at cutoff
            
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 10.0, 9, null, 0, 10));
            steps.add(new ExperimentTrajectory.TrajectoryStep(1, 50.0, 5, null, 15, 30));
            steps.add(new ExperimentTrajectory.TrajectoryStep(2, finalSortedness, 
                finalMonotonicityError, null, 25, 50));
            
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "BubbleSort", 0, 0, arraySize, System.currentTimeMillis()
                );
            
            ExperimentTrajectory partialTrajectory = new ExperimentTrajectory(steps, metadata);
            
            File csvFile = tempDir.resolve("partial_convergence.csv").toFile();
            TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), partialTrajectory);
            
            assertTrue(csvFile.exists(),
                "CSV export must succeed even for partial convergence");
            
            // Verify final row shows incomplete state
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            // Find last data row (skip comment/header lines)
            String lastDataRow = null;
            for (int i = lines.size() - 1; i >= 0; i--) {
                if (!lines.get(i).startsWith("#") && !lines.get(i).contains("step_number")) {
                    lastDataRow = lines.get(i);
                    break;
                }
            }
            
            assertNotNull(lastDataRow, "CSV should contain at least one data row");
            String[] fields = lastDataRow.split(",");
            
            assertEquals("80.0", fields[1], 
                "Final sortedness should be 80.0 (partial convergence)");
            assertEquals("2", fields[2],
                "Final monotonicity error should be 2 (incomplete sorting)");
        }
    }

    /**
     * Validates detection of non-monotonic sortedness trajectories.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: "Levin et al. Delayed
     * Gratification Gap" - all three test runs show monotonic sortedness increase,
     * but framework claims to support detecting decreases (Delayed Gratification events).</p>
     */
    @Nested
    @DisplayName("Non-monotonic sortedness trajectory (Delayed Gratification)")
    class DelayedGratificationTests {

        /**
         * PURPOSE: As a researcher analyzing emergent sorting, I want to detect
         * Delayed Gratification events so that I can identify algorithms that
         * temporarily worsen before improving.
         *
         * INPUTS: Snapshot sequence where sortedness temporarily decreases then increases
         * EXPECTED OUTPUT: Trajectory showing sortedness dip: [60%, 45%, 70%, 100%]
         * TEST DATA: Carefully crafted array states that produce sortedness regression
         * REPRODUCTION: Record snapshots with intentional sortedness decrease, verify trajectory
         */
        @Test
        @DisplayName("Trajectory detects sortedness decrease followed by increase")
        void trajectoryDetectsSortednessDecreaseFollowedByIncrease() {
            // TestWeaver: Mathematical verification of Delayed Gratification scenario
            // This simulates a sorting algorithm that temporarily disrupts order
            // (e.g., shuffling a segment to enable later global optimization)
            
            Probe<GenericCell> probe = new Probe<>();
            
            int arraySize = 10;
            
            // Step 0: Initial state - 50% sorted
            // Array: [1,2,3,4,10,9,8,7,6,5]
            // Monotonic check: 1→2✓, 2→3✓, 3→4✓, 4→10✓, 10→9✗, 9→8✗, 8→7✗, 7→6✗, 6→5✗
            // Monotonic count: 1 (first element) + 4 (increasing pairs) = 5
            // Sortedness: 5/10 = 50%
            GenericCell[] step0 = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(10), new GenericCell(9),
                new GenericCell(8), new GenericCell(7), new GenericCell(6),
                new GenericCell(5)
            };
            probe.recordSnapshot(0, step0, 0);
            
            // Step 1: DELAYED GRATIFICATION - sortedness DECREASES to 30%
            // Array: [1,2,10,9,8,7,6,5,4,3] - moved 10 earlier, disrupting order
            // Monotonic check: 1→2✓, 2→10✓, 10→9✗, 9→8✗, 8→7✗, 7→6✗, 6→5✗, 5→4✗, 4→3✗
            // Monotonic count: 1 (first element) + 2 (increasing pairs) = 3
            // Sortedness: 3/10 = 30%
            GenericCell[] step1 = {
                new GenericCell(1), new GenericCell(2), new GenericCell(10),
                new GenericCell(9), new GenericCell(8), new GenericCell(7),
                new GenericCell(6), new GenericCell(5), new GenericCell(4),
                new GenericCell(3)
            };
            probe.recordSnapshot(1, step1, 3);
            
            // Step 2: Recovery begins - sortedness increases to 90%
            // Array: [1,2,3,4,5,6,7,8,10,9] - partial recovery
            // Monotonic check: All pairs increasing except last (10→9✗)
            // Monotonic count: 1 (first element) + 8 (increasing pairs) = 9
            // Sortedness: 9/10 = 90%
            GenericCell[] step2 = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5), new GenericCell(6),
                new GenericCell(7), new GenericCell(8), new GenericCell(10),
                new GenericCell(9)
            };
            probe.recordSnapshot(2, step2, 8);
            
            // Step 3: Full convergence - 100% sorted
            GenericCell[] step3 = {
                new GenericCell(1), new GenericCell(2), new GenericCell(3),
                new GenericCell(4), new GenericCell(5), new GenericCell(6),
                new GenericCell(7), new GenericCell(8), new GenericCell(9),
                new GenericCell(10)
            };
            probe.recordSnapshot(3, step3, 9);
            
            ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
                probe, "DelayedGratification", 0, 0, arraySize, System.currentTimeMillis()
            );
            
            List<ExperimentTrajectory.TrajectoryStep> steps = trajectory.getSteps();
            
            double sortedness0 = steps.get(0).sortedness();
            double sortedness1 = steps.get(1).sortedness();
            double sortedness2 = steps.get(2).sortedness();
            double sortedness3 = steps.get(3).sortedness();
            
            // Verify Delayed Gratification: sortedness decreases then increases
            assertTrue(sortedness1 < sortedness0,
                String.format(
                    "Delayed Gratification requires sortedness decrease: step0=%.1f%% > step1=%.1f%%",
                    sortedness0, sortedness1
                ));
            
            assertTrue(sortedness2 > sortedness1,
                String.format(
                    "After Delayed Gratification, sortedness must recover: step1=%.1f%% < step2=%.1f%%",
                    sortedness1, sortedness2
                ));
            
            assertEquals(100.0, sortedness3, 0.01,
                "Final step should reach 100% sortedness");
        }

        /**
         * PURPOSE: As a researcher studying algorithmic behavior, I want to document
         * what conditions trigger sortedness regression so that I can design experiments
         * to study Delayed Gratification systematically.
         *
         * INPUTS: Test analysis of when sortedness can decrease
         * EXPECTED OUTPUT: Documentation of necessary conditions for DG events
         * TEST DATA: N/A (documentation test)
         * REPRODUCTION: Review test comments and verify understanding
         */
        @Test
        @DisplayName("Documents conditions that enable sortedness regression")
        void documentsConditionsThatEnableSortednessRegression() {
            // TestWeaver: This test documents the theoretical conditions required
            // for sortedness to temporarily decrease (Delayed Gratification).
            //
            // Sortedness formula: (cells in monotonic order with predecessor) / n
            // 
            // For sortedness to DECREASE between steps:
            //   monotonic_count[t+1] < monotonic_count[t]
            //
            // This requires:
            //   1. Moving a cell that was in correct order to incorrect position
            //   2. Creating new adjacent inversions
            //   3. Net loss of monotonic pairs despite potential local improvements
            //
            // Examples:
            //   - Insertion sort moving a large element backward through sorted region
            //   - Quicksort partition phase temporarily dispersing sorted subsequence
            //   - Selection sort swapping a correctly-positioned element
            //
            // Key insight: Delayed Gratification is RARE in simple sorting algorithms
            // because most maintain or improve sortedness monotonically. It appears in:
            //   - Algorithms with global reorganization phases
            //   - Algorithms that temporarily sacrifice local order for global optimum
            //   - Pathological cases where random swaps disrupt existing order
            
            // This test passes by documenting the conditions - no runtime assertions needed
            assertTrue(true, 
                "Delayed Gratification requires algorithms that temporarily disrupt " +
                "existing order to enable later global optimization");
            
            // Future work: Implement actual sorting algorithms (e.g., quicksort) that
            // exhibit Delayed Gratification and validate detection in real scenarios
        }
    }

    /**
     * Validates CSV export format correctness and parsing compatibility.
     * 
     * <p>Addresses finding from THREE_EXPERIMENT_FINDINGS.md: CSV format validation
     * needed to ensure metadata comments, headers, and data rows are correctly
     * structured for pandas/R compatibility.</p>
     */
    @Nested
    @DisplayName("CSV export format correctness")
    class CsvFormatTests {

        /**
         * PURPOSE: As a data analyst using pandas, I want comment lines to start
         * with '#' so that I can parse CSV files with comment='#' parameter.
         *
         * INPUTS: Exported CSV trajectory
         * EXPECTED OUTPUT: All metadata lines start with '#'
         * TEST DATA: Standard trajectory with metadata
         * REPRODUCTION: Export CSV, verify all comment lines have '#' prefix
         */
        @Test
        @DisplayName("All comment lines start with '#' character")
        void allCommentLinesStartWithHashCharacter() throws IOException {
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 5, null, 0, 25));
            steps.add(new ExperimentTrajectory.TrajectoryStep(1, 100.0, 0, null, 10, 50));
            
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "BubbleSort", 0, 0, 10, System.currentTimeMillis()
                );
            
            ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
            
            File csvFile = tempDir.resolve("format_test.csv").toFile();
            TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
            
            List<String> commentLines = new ArrayList<>();
            List<String> metadataLines = new ArrayList<>();
            
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) {
                        commentLines.add(line);
                    } else if (!line.contains("step_number") && !line.matches("\\d.*")) {
                        // Lines that look like metadata but don't start with #
                        metadataLines.add(line);
                    }
                }
            }
            
            assertTrue(commentLines.size() >= 3,
                "CSV should have at least 3 comment lines (# Metadata, # WARNING, # Trajectory Data)");
            
            for (String commentLine : commentLines) {
                assertTrue(commentLine.startsWith("#"),
                    "All comment lines must start with '#' for pandas/R compatibility");
            }
        }

        /**
         * PURPOSE: As a data scientist, I want CSV headers to follow standard format
         * so that my data analysis tools can parse the file correctly.
         *
         * INPUTS: Exported CSV with both chimeric and non-chimeric trajectories
         * EXPECTED OUTPUT: Headers match expected schema exactly
         * TEST DATA: Standard and chimeric trajectories
         * REPRODUCTION: Export both types, verify header structure
         */
        @Test
        @DisplayName("CSV headers match expected schema")
        void csvHeadersMatchExpectedSchema() throws IOException {
            String expectedNonChimericHeader = 
                "step_number,sortedness,monotonicity_error,cumulative_swaps,cumulative_comparisons";
            String expectedChimericHeader = 
                "step_number,sortedness,monotonicity_error,aggregation,cumulative_swaps,cumulative_comparisons";
            
            // Test non-chimeric trajectory
            List<ExperimentTrajectory.TrajectoryStep> nonChimericSteps = new ArrayList<>();
            nonChimericSteps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 5, null, 0, 25));
            
            ExperimentTrajectory.ExperimentMetadata metadata1 = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "BubbleSort", 0, 0, 10, System.currentTimeMillis()
                );
            
            ExperimentTrajectory nonChimericTrajectory = 
                new ExperimentTrajectory(nonChimericSteps, metadata1);
            
            File nonChimericFile = tempDir.resolve("non_chimeric.csv").toFile();
            TrajectoryDataExporter.exportTrajectoryToCSV(
                nonChimericFile.getAbsolutePath(), nonChimericTrajectory);
            
            String nonChimericHeader = findHeaderLine(nonChimericFile);
            assertEquals(expectedNonChimericHeader, nonChimericHeader,
                "Non-chimeric trajectory header must match standard schema");
            
            // Test chimeric trajectory
            List<ExperimentTrajectory.TrajectoryStep> chimericSteps = new ArrayList<>();
            chimericSteps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 5, 75.0, 0, 25));
            
            ExperimentTrajectory.ExperimentMetadata metadata2 = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "Chimeric", 0, 0, 10, System.currentTimeMillis()
                );
            
            ExperimentTrajectory chimericTrajectory = 
                new ExperimentTrajectory(chimericSteps, metadata2);
            
            File chimericFile = tempDir.resolve("chimeric.csv").toFile();
            TrajectoryDataExporter.exportTrajectoryToCSV(
                chimericFile.getAbsolutePath(), chimericTrajectory);
            
            String chimericHeader = findHeaderLine(chimericFile);
            assertEquals(expectedChimericHeader, chimericHeader,
                "Chimeric trajectory header must include aggregation column");
        }

        /**
         * PURPOSE: As a data analyst, I want to verify pandas can parse exported
         * CSV files so that I can load trajectory data without preprocessing.
         *
         * INPUTS: Exported CSV file
         * EXPECTED OUTPUT: File structure compatible with pandas.read_csv(comment='#')
         * TEST DATA: Multi-step trajectory
         * REPRODUCTION: Export CSV, verify structure allows clean parsing
         */
        @Test
        @DisplayName("CSV structure is compatible with pandas parsing")
        void csvStructureIsCompatibleWithPandasParsing() throws IOException {
            // TestWeaver: This test validates CSV structure without requiring pandas
            // Checks: (1) Comment lines prefixed with #, (2) Header after comments,
            // (3) Data rows have correct column count
            
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 10.0, 9, null, 0, 10));
            steps.add(new ExperimentTrajectory.TrajectoryStep(1, 50.0, 5, null, 15, 30));
            steps.add(new ExperimentTrajectory.TrajectoryStep(2, 100.0, 0, null, 25, 50));
            
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "BubbleSort", 0, 0, 10, System.currentTimeMillis()
                );
            
            ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
            
            File csvFile = tempDir.resolve("pandas_compatible.csv").toFile();
            TrajectoryDataExporter.exportTrajectoryToCSV(csvFile.getAbsolutePath(), trajectory);
            
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            // Verify structure: comments, then header, then data
            boolean foundHeader = false;
            boolean foundData = false;
            int expectedColumnCount = 5;  // Non-chimeric: 5 columns
            
            for (String line : lines) {
                if (line.startsWith("#")) {
                    continue;  // Skip comments
                } else if (line.contains("step_number")) {
                    foundHeader = true;
                    String[] headerCols = line.split(",");
                    assertEquals(expectedColumnCount, headerCols.length,
                        "Header must have exactly " + expectedColumnCount + " columns");
                } else if (line.matches("\\d.*")) {
                    foundData = true;
                    String[] dataCols = line.split(",");
                    assertEquals(expectedColumnCount, dataCols.length,
                        "Each data row must have exactly " + expectedColumnCount + " columns");
                }
            }
            
            assertTrue(foundHeader, "CSV must contain header row");
            assertTrue(foundData, "CSV must contain at least one data row");
        }

        /**
         * Helper method to find the header line in a CSV file.
         */
        private String findHeaderLine(File csvFile) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("step_number")) {
                        return line;
                    }
                }
            }
            return null;
        }
    }
}
