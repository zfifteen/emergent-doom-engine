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
    // Metadata Provider Tests (Phase 2)
    // ========================================================================

    @Nested
    @DisplayName("Metadata Provider Pattern")
    class MetadataProviderTests {

        /**
         * As a User I want to create engines with metadata providers
         * so that I can use lightweight cells without embedded metadata.
         * 
         * PURPOSE: Test new constructor accepting IntFunction<CellMetadata>
         * INPUTS: Cell array, metadata provider function
         * EXPECTED: Engine initializes with metadata array
         */
        @Test
        @DisplayName("Engine accepts metadata provider constructor")
        void engineAcceptsMetadataProvider() {
            // TODO PHASE TWO: Implement test
            // 1. Create minimal cells with only values (no algotype)
            // 2. Create metadata provider: index -> new CellMetadata(BUBBLE, ASCENDING)
            // 3. Construct engine with metadata provider
            // 4. Verify engine initializes without error
        }

        /**
         * As a User I want metadata to swap with cells during execution
         * so that metadata stays attached to the logical agent identity.
         * 
         * PURPOSE: Verify metadata swaps alongside cells
         * INPUTS: Cells with identifiable metadata
         * EXPECTED: After swaps, metadata[i] corresponds to logical cell at position i
         */
        @Test
        @DisplayName("Metadata swaps with cells during execution")
        void metadataSwapsWithCells() {
            // TODO PHASE THREE: Implement test
            // 1. Create cells and metadata with identifiable markers
            // 2. Run engine for a few steps
            // 3. Verify metadata stayed attached to correct logical cell after swaps
        }

        /**
         * As a User I want engines to use metadata providers instead of cell interfaces
         * so that I can migrate to lightweight cells without breaking functionality.
         * 
         * PURPOSE: Test engine sorts using metadata provider instead of cell.getAlgotype()
         * INPUTS: Cells without getAlgotype(), metadata provider with BUBBLE algotype
         * EXPECTED: Engine sorts array correctly using metadata
         */
        @Test
        @DisplayName("Engine sorts using metadata provider instead of cell interfaces")
        void engineUsesMetadataProvider() {
            // TODO PHASE THREE: Implement test
            // 1. Create minimal cells (just Comparable, no HasAlgotype)
            // 2. Provide metadata externally via IntFunction<CellMetadata>
            // 3. Run engine to convergence
            // 4. Verify array is sorted (proving metadata was used, not cell.getAlgotype())
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

        @Override
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
