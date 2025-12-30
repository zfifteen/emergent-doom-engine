package com.emergent.doom.analysis;

import com.emergent.doom.metrics.Metric;
import com.emergent.doom.metrics.MonotonicityError;
import com.emergent.doom.metrics.SortednessValue;
import com.emergent.doom.probe.StepSnapshot;
import com.emergent.doom.swap.IntCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link TrajectoryAnalyzer}.
 */
class TrajectoryAnalyzerTest {

    private TrajectoryAnalyzer<IntCell> analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TrajectoryAnalyzer<>();
    }

    private IntCell[] createCells(int... values) {
        IntCell[] cells = new IntCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new IntCell(values[i]);
        }
        return cells;
    }

    private List<StepSnapshot<IntCell>> createTrajectory(int[][] cellValuesByStep, int[] swapCounts) {
        List<StepSnapshot<IntCell>> snapshots = new ArrayList<>();
        for (int i = 0; i < cellValuesByStep.length; i++) {
            IntCell[] cells = createCells(cellValuesByStep[i]);
            snapshots.add(new StepSnapshot<>(i, cells, swapCounts[i]));
        }
        return snapshots;
    }

    @Nested
    @DisplayName("computeMetricTrajectory")
    class ComputeMetricTrajectory {

        @Test
        @DisplayName("null snapshots returns empty list")
        void nullSnapshots() {
            List<Double> result = analyzer.computeMetricTrajectory(null, new SortednessValue<>());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("empty snapshots returns empty list")
        void emptySnapshots() {
            List<Double> result = analyzer.computeMetricTrajectory(Collections.emptyList(), new SortednessValue<>());
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("computes sortedness for each step")
        void computesSortednessTrajectory() {
            // SortednessValue counts cells in their CORRECT FINAL sorted position
            // For INCREASING sort, sorted array would be [1, 2, 3]
            // Step 0: [3,1,2] - pos0 has 3 (should be 1), pos1 has 1 (should be 2), pos2 has 2 (should be 3) = 0% correct
            // Step 1: [1,3,2] - pos0 has 1 (correct!), pos1 has 3 (should be 2), pos2 has 2 (should be 3) = 33.3% correct
            // Step 2: [1,2,3] - all correct = 100%
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}},
                    new int[]{0, 1, 1}
            );

            List<Double> sortedness = analyzer.computeMetricTrajectory(trajectory, new SortednessValue<>());

            assertEquals(3, sortedness.size());
            assertEquals(0.0, sortedness.get(0), 0.01);    // [3,1,2] - nothing in final position
            assertEquals(33.33, sortedness.get(1), 0.5);   // [1,3,2] - only "1" is correct
            assertEquals(100.0, sortedness.get(2), 0.01);  // [1,2,3] - all correct
        }

        @Test
        @DisplayName("computes monotonicity error for each step")
        void computesMonotonicityTrajectory() {
            // Step 0: [3,1,2] - 2 inversions (3>1, 3>2)
            // Step 1: [1,3,2] - 1 inversion (3>2)
            // Step 2: [1,2,3] - 0 inversions
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}},
                    new int[]{0, 1, 1}
            );

            List<Double> errors = analyzer.computeMetricTrajectory(trajectory, new MonotonicityError<>());

            assertEquals(3, errors.size());
            assertEquals(2.0, errors.get(0), 0.01);
            assertEquals(1.0, errors.get(1), 0.01);
            assertEquals(0.0, errors.get(2), 0.01);
        }
    }

    @Nested
    @DisplayName("extractSwapCounts")
    class ExtractSwapCounts {

        @Test
        @DisplayName("null snapshots returns empty list")
        void nullSnapshots() {
            assertTrue(analyzer.extractSwapCounts(null).isEmpty());
        }

        @Test
        @DisplayName("empty snapshots returns empty list")
        void emptySnapshots() {
            assertTrue(analyzer.extractSwapCounts(Collections.emptyList()).isEmpty());
        }

        @Test
        @DisplayName("extracts swap counts from trajectory")
        void extractsSwapCounts() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}, {1, 2, 3}},
                    new int[]{0, 2, 1, 0}
            );

            List<Integer> counts = analyzer.extractSwapCounts(trajectory);

            assertEquals(Arrays.asList(0, 2, 1, 0), counts);
        }
    }

    @Nested
    @DisplayName("findConvergenceStep")
    class FindConvergenceStep {

        @Test
        @DisplayName("null snapshots returns -1")
        void nullSnapshots() {
            assertEquals(-1, analyzer.findConvergenceStep(null, 3));
        }

        @Test
        @DisplayName("empty snapshots returns -1")
        void emptySnapshots() {
            assertEquals(-1, analyzer.findConvergenceStep(Collections.emptyList(), 3));
        }

        @Test
        @DisplayName("zero consecutive requirement returns -1")
        void zeroConsecutive() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{1, 2, 3}},
                    new int[]{0}
            );
            assertEquals(-1, analyzer.findConvergenceStep(trajectory, 0));
        }

        @Test
        @DisplayName("finds convergence with 3 consecutive zero swaps")
        void findsConvergenceAt3() {
            // Swaps: 2, 1, 0, 0, 0 -> converges at step 2 (first of 3 zeros)
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}, {1, 2, 3}, {1, 2, 3}},
                    new int[]{2, 1, 0, 0, 0}
            );

            assertEquals(2, analyzer.findConvergenceStep(trajectory, 3));
        }

        @Test
        @DisplayName("finds convergence requiring only 1 zero swap")
        void findsConvergenceAt1() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 2, 3}},
                    new int[]{1, 0}
            );

            assertEquals(1, analyzer.findConvergenceStep(trajectory, 1));
        }

        @Test
        @DisplayName("returns -1 when never converges")
        void neverConverges() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}},
                    new int[]{2, 1, 1}  // Never hits 3 consecutive zeros
            );

            assertEquals(-1, analyzer.findConvergenceStep(trajectory, 3));
        }

        @Test
        @DisplayName("resets counter when non-zero swap interrupts")
        void resetsOnInterrupt() {
            // Swaps: 0, 0, 1, 0, 0, 0 -> converges at step 3
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{1, 2, 3}, {1, 2, 3}, {2, 1, 3}, {1, 2, 3}, {1, 2, 3}, {1, 2, 3}},
                    new int[]{0, 0, 1, 0, 0, 0}
            );

            assertEquals(3, analyzer.findConvergenceStep(trajectory, 3));
        }
    }

    @Nested
    @DisplayName("visualizeTrajectory")
    class VisualizeTrajectory {

        @Test
        @DisplayName("null snapshots returns informative message")
        void nullSnapshots() {
            assertEquals("No trajectory data available.", analyzer.visualizeTrajectory(null, 10));
        }

        @Test
        @DisplayName("empty snapshots returns informative message")
        void emptySnapshots() {
            assertEquals("No trajectory data available.", analyzer.visualizeTrajectory(Collections.emptyList(), 10));
        }

        @Test
        @DisplayName("visualizes trajectory with header and data")
        void visualizesData() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 2, 3}},
                    new int[]{1, 0}
            );

            String result = analyzer.visualizeTrajectory(trajectory, 10);

            assertTrue(result.contains("Step"));
            assertTrue(result.contains("Swaps"));
            assertTrue(result.contains("Array Size"));
            assertTrue(result.contains("0"));  // Step 0
            assertTrue(result.contains("1"));  // Step 1 or swap count
            assertTrue(result.contains("3"));  // Array size
        }

        @Test
        @DisplayName("limits output to maxSnapshotsToShow")
        void limitsOutput() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{3, 1, 2}, {1, 3, 2}, {1, 2, 3}, {1, 2, 3}, {1, 2, 3}},
                    new int[]{0, 1, 1, 0, 0}
            );

            String result = analyzer.visualizeTrajectory(trajectory, 2);

            assertTrue(result.contains("... and 3 more steps"));
        }
    }

    @Nested
    @DisplayName("getTotalExecutionTime")
    class GetTotalExecutionTime {

        @Test
        @DisplayName("null snapshots returns 0")
        void nullSnapshots() {
            assertEquals(0L, analyzer.getTotalExecutionTime(null));
        }

        @Test
        @DisplayName("empty snapshots returns 0")
        void emptySnapshots() {
            assertEquals(0L, analyzer.getTotalExecutionTime(Collections.emptyList()));
        }

        @Test
        @DisplayName("single snapshot returns 0")
        void singleSnapshot() {
            List<StepSnapshot<IntCell>> trajectory = createTrajectory(
                    new int[][]{{1, 2, 3}},
                    new int[]{0}
            );
            assertEquals(0L, analyzer.getTotalExecutionTime(trajectory));
        }

        @Test
        @DisplayName("multiple snapshots returns non-negative duration")
        void multipleSnapshots() {
            // Create snapshots in quick succession; System.nanoTime() should yield different timestamps
            IntCell[] cells = createCells(1, 2, 3);
            List<StepSnapshot<IntCell>> trajectory = new ArrayList<>();
            trajectory.add(new StepSnapshot<>(0, cells, 0));
            trajectory.add(new StepSnapshot<>(1, cells, 0));

            long duration = analyzer.getTotalExecutionTime(trajectory);
            // Duration should be non-negative (timestamps are monotonically increasing)
            assertTrue(duration >= 0, "Expected non-negative duration, got " + duration);
        }
    }
}
