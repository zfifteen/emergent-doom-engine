package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.GroupAwareCell;
import com.emergent.doom.probe.ThreadSafeProbe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.swap.ThreadSafeFrozenCellStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ParallelExecutionEngine.
 *
 * Tests verify:
 * - Correct sorting behavior with parallel execution
 * - Thread safety of concurrent operations
 * - Convergence detection
 * - Proper startup and shutdown
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class ParallelExecutionEngineTest {

    private ParallelExecutionEngine<TestBubbleCell> engine;
    private TestBubbleCell[] cells;
    private ThreadSafeProbe<TestBubbleCell> probe;
    private SwapEngine<TestBubbleCell> swapEngine;

    @BeforeEach
    void setUp() {
        // Will be initialized per test
    }

    @AfterEach
    void tearDown() {
        if (engine != null && engine.isRunning()) {
            engine.shutdown();
        }
    }

    private void initializeEngine(int... values) {
        cells = new TestBubbleCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new TestBubbleCell(values[i]);
        }

        FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
        swapEngine = new SwapEngine<>(frozenStatus);
        probe = new ThreadSafeProbe<>();
        ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(10);

        engine = new ParallelExecutionEngine<>(cells, swapEngine, probe, convergenceDetector);
    }

    // ========================================================================
    // Basic Functionality Tests
    // ========================================================================

    @Nested
    @DisplayName("Basic sorting functionality")
    class BasicSortingTests {

        @Test
        @DisplayName("Sorts a small array correctly")
        void sortsSmallArray() {
            initializeEngine(5, 3, 1, 4, 2);

            engine.start();
            engine.runUntilConvergence(1000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should be sorted");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
        }

        @Test
        @DisplayName("Handles already sorted array")
        void handlesAlreadySorted() {
            initializeEngine(1, 2, 3, 4, 5);

            engine.start();
            int steps = engine.runUntilConvergence(100);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result);
            assertTrue(engine.hasConverged());
            assertTrue(steps < 10, "Should converge quickly for sorted array");
        }

        @Test
        @DisplayName("Handles reverse sorted array")
        void handlesReverseSorted() {
            initializeEngine(5, 4, 3, 2, 1);

            engine.start();
            engine.runUntilConvergence(1000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result);
            assertTrue(engine.hasConverged());
        }

        @Test
        @DisplayName("Handles single element array")
        void handlesSingleElement() {
            initializeEngine(42);

            engine.start();
            engine.runUntilConvergence(100);

            assertEquals(42, cells[0].getValue());
            assertTrue(engine.hasConverged());
        }

        @Test
        @DisplayName("Handles two element array")
        void handlesTwoElements() {
            initializeEngine(2, 1);

            engine.start();
            engine.runUntilConvergence(100);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2}, result);
            assertTrue(engine.hasConverged());
        }
    }

    // ========================================================================
    // Lifecycle Tests
    // ========================================================================

    @Nested
    @DisplayName("Engine lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Start and shutdown work correctly")
        void startAndShutdown() {
            initializeEngine(3, 1, 2);

            assertFalse(engine.isRunning(), "Should not be running before start");

            engine.start();
            assertTrue(engine.isRunning(), "Should be running after start");

            engine.shutdown();
            assertFalse(engine.isRunning(), "Should not be running after shutdown");
        }

        @Test
        @DisplayName("Cannot step before start")
        void cannotStepBeforeStart() {
            initializeEngine(3, 1, 2);

            assertThrows(IllegalStateException.class, () -> engine.step(),
                    "Should throw when stepping before start");
        }

        @Test
        @DisplayName("Cannot start twice")
        void cannotStartTwice() {
            initializeEngine(3, 1, 2);
            engine.start();

            assertThrows(IllegalStateException.class, () -> engine.start(),
                    "Should throw when starting twice");

            engine.shutdown();
        }

        @Test
        @DisplayName("Reset allows restart")
        void resetAllowsRestart() {
            initializeEngine(3, 1, 2);
            engine.start();
            engine.step();
            engine.shutdown();

            engine.reset();

            assertFalse(engine.isRunning());
            assertEquals(0, engine.getCurrentStep());
            assertFalse(engine.hasConverged());

            // Should be able to start again
            engine.start();
            assertTrue(engine.isRunning());
            engine.shutdown();
        }
    }

    // ========================================================================
    // Step Execution Tests
    // ========================================================================

    @Nested
    @DisplayName("Step execution")
    class StepExecutionTests {

        @Test
        @DisplayName("Step returns swap count")
        void stepReturnsSwapCount() {
            initializeEngine(2, 1);  // Will swap on first step
            engine.start();

            int swaps = engine.step();

            assertTrue(swaps >= 0, "Swap count should be non-negative");
            assertEquals(1, engine.getCurrentStep());
        }

        @Test
        @DisplayName("Multiple steps progress sorting")
        void multipleStepsProgress() {
            initializeEngine(5, 4, 3, 2, 1);
            engine.start();

            int totalSwaps = 0;
            for (int i = 0; i < 10; i++) {
                totalSwaps += engine.step();
            }

            assertTrue(totalSwaps > 0, "Should have made some swaps");
            assertEquals(10, engine.getCurrentStep());
        }
    }

    // ========================================================================
    // Probe Recording Tests
    // ========================================================================

    @Nested
    @DisplayName("Probe recording")
    class ProbeRecordingTests {

        @Test
        @DisplayName("Records initial snapshot")
        void recordsInitialSnapshot() {
            initializeEngine(3, 1, 2);

            assertEquals(1, probe.getSnapshotCount(), "Should have initial snapshot");
            assertNotNull(probe.getSnapshot(0), "Initial snapshot should exist");
        }

        @Test
        @DisplayName("Records snapshot after each step")
        void recordsSnapshotsAfterSteps() {
            initializeEngine(3, 1, 2);
            engine.start();

            engine.step();
            engine.step();
            engine.step();

            assertEquals(4, probe.getSnapshotCount(), "Should have initial + 3 step snapshots");
            engine.shutdown();
        }
    }

    // ========================================================================
    // Thread Safety Tests
    // ========================================================================

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Concurrent sorting completes without deadlock")
        @Timeout(value = 10, unit = TimeUnit.SECONDS)
        void concurrentSortingCompletes() {
            // Use larger array to stress test concurrency
            int[] values = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
            initializeEngine(values);

            engine.start();
            engine.runUntilConvergence(5000);

            // Verify sorted
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            int[] expected = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
            assertArrayEquals(expected, result, "Array should be sorted");
        }

        @Test
        @DisplayName("Multiple runs produce consistent results")
        void multipleRunsConsistent() {
            for (int run = 0; run < 5; run++) {
                initializeEngine(5, 3, 1, 4, 2);
                engine.start();
                engine.runUntilConvergence(1000);

                int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
                assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result,
                        "Run " + run + " should produce sorted array");

                engine.shutdown();
            }
        }
    }

    // ========================================================================
    // Lightweight Cell Refactoring Tests
    // ========================================================================

    /**
     * Tests demonstrating the lightweight cell refactoring where the engine
     * manages metadata independently from cell objects.
     */
    @Nested
    @DisplayName("Lightweight cell refactoring")
    class LightweightCellTests {

        @Test
        @DisplayName("Engine manages metadata independently via provider function")
        void engineManagesMetadataIndependently() {
            // Create minimal cells with only values (no metadata)
            int[] values = {5, 3, 1, 4, 2};
            cells = new TestBubbleCell[values.length];
            for (int i = 0; i < values.length; i++) {
                cells[i] = new TestBubbleCell(values[i]);
            }

            // Engine provides metadata via IntFunction
            java.util.function.IntFunction<CellMetadata> metadataProvider = index -> {
                // All cells get BUBBLE algotype and ASCENDING direction
                return new CellMetadata(
                    Algotype.BUBBLE,
                    com.emergent.doom.cell.SortDirection.ASCENDING,
                    new java.util.concurrent.atomic.AtomicInteger(0),
                    0,
                    cells.length - 1
                );
            };

            FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
            swapEngine = new SwapEngine<>(frozenStatus);
            probe = new ThreadSafeProbe<>();
            ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(10);

            // Use new constructor that accepts metadata provider
            engine = new ParallelExecutionEngine<>(cells, swapEngine, probe, convergenceDetector, metadataProvider);

            engine.start();
            engine.runUntilConvergence(1000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result,
                    "Array should be sorted using engine-managed metadata");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
        }

        @Test
        @DisplayName("Chimeric population with engine-controlled metadata")
        void chimericPopulationViaMetadata() {
            // Create minimal cells
            int[] values = {10, 9, 8, 7, 6, 5, 4, 3, 2, 1};
            cells = new TestBubbleCell[values.length];
            for (int i = 0; i < values.length; i++) {
                cells[i] = new TestBubbleCell(values[i]);
            }

            // Engine assigns alternating algotypes (50% BUBBLE, 50% INSERTION)
            java.util.function.IntFunction<CellMetadata> metadataProvider = index -> {
                Algotype algotype = (index % 2 == 0) ? Algotype.BUBBLE : Algotype.INSERTION;
                return new CellMetadata(
                    algotype,
                    com.emergent.doom.cell.SortDirection.ASCENDING,
                    new java.util.concurrent.atomic.AtomicInteger(0),
                    0,
                    cells.length - 1
                );
            };

            FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
            swapEngine = new SwapEngine<>(frozenStatus);
            probe = new ThreadSafeProbe<>();
            ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(10);

            engine = new ParallelExecutionEngine<>(cells, swapEngine, probe, convergenceDetector, metadataProvider);

            engine.start();
            engine.runUntilConvergence(2000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, result,
                    "Chimeric population should sort correctly with engine-managed metadata");
            assertTrue(engine.hasConverged(), "Engine should converge");
        }
    }

    // ========================================================================
    // Test Cell Implementation
    // ========================================================================

    /**
     * Simple bubble sort cell for testing.
     */
    static class TestBubbleCell implements Cell<TestBubbleCell>, GroupAwareCell<TestBubbleCell> {
        private final int value;

        TestBubbleCell(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public Algotype getAlgotype() {
            return Algotype.BUBBLE;
        }

        @Override
        public com.emergent.doom.group.CellGroup<TestBubbleCell> getGroup() { return null; }
        @Override
        public com.emergent.doom.group.CellStatus getStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
        @Override
        public com.emergent.doom.group.CellStatus getPreviousStatus() { return com.emergent.doom.group.CellStatus.ACTIVE; }
        @Override
        public void setStatus(com.emergent.doom.group.CellStatus status) {}
        @Override
        public void setPreviousStatus(com.emergent.doom.group.CellStatus status) {}
        @Override
        public void setGroup(com.emergent.doom.group.CellGroup<TestBubbleCell> group) {}
        @Override
        public int getLeftBoundary() { return 0; }
        @Override
        public void setLeftBoundary(int leftBoundary) {}
        @Override
        public int getRightBoundary() { return 0; }
        @Override
        public void setRightBoundary(int rightBoundary) {}
        @Override
        public void updateForGroupMerge() {}

        @Override
        public int compareTo(TestBubbleCell other) {
            return Integer.compare(this.value, other.value);
        }

        @Override
        public String toString() {
            return "TestBubbleCell(" + value + ")";
        }
    }
}
