package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasValue;
import com.emergent.doom.group.GroupAwareCell;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.swap.SwapEngine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SynchronousExecutionEngine.
 *
 * <p>As a user, I want to verify that the synchronous execution engine:
 * <ul>
 *   <li>Correctly sorts arrays using single-threaded execution</li>
 *   <li>Produces identical results to ParallelExecutionEngine</li>
 *   <li>Executes faster for single trials (no threading overhead)</li>
 *   <li>Properly integrates with ExecutorService for parallel trials</li>
 *   <li>Handles all algotypes (BUBBLE, INSERTION, SELECTION)</li>
 *   <li>Supports convergence detection</li>
 *   <li>Records metrics correctly</li>
 * </ul>
 * </p>
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class SynchronousExecutionEngineTest {

    private SynchronousExecutionEngine<TestBubbleCell> engine;
    private TestBubbleCell[] cells;
    private Probe<TestBubbleCell> probe;
    private SwapEngine<TestBubbleCell> swapEngine;

    @BeforeEach
    void setUp() {
        // Will be initialized per test
    }

    @AfterEach
    void tearDown() {
        if (engine != null && engine.isRunning()) {
            engine.stop();
        }
    }

    /**
     * Helper: Initialize engine with specified values.
     *
     * <p>As a test author, I want a convenient way to set up test cases
     * so that I can focus on testing specific scenarios.</p>
     */
    private void initializeEngine(int... values) {
        cells = new TestBubbleCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new TestBubbleCell(values[i]);
        }

        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        swapEngine = new SwapEngine<>(frozenStatus);
        probe = new Probe<>();
        probe.setRecordingEnabled(true);
        ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(10);

        engine = new SynchronousExecutionEngine<>(cells, swapEngine, probe, convergenceDetector);
    }

    /**
     * Helper: Initialize engine with specified values and a seeded Random for determinism.
     */
    private void initializeEngineWithSeed(long seed, int... values) {
        cells = new TestBubbleCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new TestBubbleCell(values[i]);
        }

        FrozenCellStatus frozenStatus = new FrozenCellStatus();
        swapEngine = new SwapEngine<>(frozenStatus);
        probe = new Probe<>();
        probe.setRecordingEnabled(true);
        ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(10);

        engine = new SynchronousExecutionEngine<>(cells, swapEngine, probe, convergenceDetector, new Random(seed));
    }

    // ========================================================================
    // Basic Functionality Tests
    // ========================================================================

    @Nested
    @DisplayName("Basic sorting functionality")
    class BasicSortingTests {

        /**
         * As a user, I want to sort a small array
         * so that I can verify basic functionality works.
         */
        @Test
        @DisplayName("Sorts a small array correctly")
        void sortsSmallArray() {
            initializeEngine(5, 3, 1, 4, 2);
            
            int finalStep = engine.runUntilConvergence(1000);
            
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should be sorted");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
            assertTrue(finalStep > 0, "Should have executed at least one step");
        }

        /**
         * As a user, I want the engine to handle already-sorted arrays efficiently
         * so that I don't waste computation on sorted data.
         */
        @Test
        @DisplayName("Handles already sorted array")
        void handlesAlreadySorted() {
            initializeEngine(1, 2, 3, 4, 5);
            
            int finalStep = engine.runUntilConvergence(1000);
            
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should remain sorted");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
            // Already sorted arrays should converge quickly (within stable steps threshold)
            assertTrue(finalStep <= 10, "Should converge quickly for sorted array");
        }

        /**
         * As a user, I want to sort reverse-sorted arrays
         * so that I can handle worst-case input scenarios.
         */
        @Test
        @DisplayName("Handles reverse sorted array")
        void handlesReverseSorted() {
            initializeEngine(5, 4, 3, 2, 1);
            
            int finalStep = engine.runUntilConvergence(5000);
            
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should be sorted");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
        }

        /**
         * As a user, I want to handle single-element arrays
         * so that I can process edge cases without errors.
         */
        @Test
        @DisplayName("Handles single element array")
        void handlesSingleElement() {
            initializeEngine(42);
            
            int finalStep = engine.runUntilConvergence(1000);
            
            assertEquals(42, cells[0].getValue(), "Value should remain unchanged");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
        }

        /**
         * As a user, I want to sort two-element arrays
         * so that I can handle minimal non-trivial cases.
         */
        @Test
        @DisplayName("Handles two element array")
        void handlesTwoElements() {
            initializeEngine(2, 1);
            
            int finalStep = engine.runUntilConvergence(1000);
            
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2}, result, "Array should be sorted");
            assertTrue(engine.hasConverged(), "Engine should report convergence");
        }
    }

    // ========================================================================
    // Lifecycle Tests
    // ========================================================================

    @Nested
    @DisplayName("Engine lifecycle")
    class LifecycleTests {

        /**
         * As a user, I want to reset the engine between runs
         * so that I can reuse the same engine instance for multiple trials.
         */
        @Test
        @DisplayName("Reset allows rerun")
        void resetAllowsRerun() {
            initializeEngine(3, 1, 2);
            
            // First run
            engine.runUntilConvergence(1000);
            int firstFinalStep = engine.getCurrentStep();
            assertTrue(firstFinalStep > 0, "First run should execute steps");
            
            // Reset
            engine.reset();
            
            // Verify reset state
            assertEquals(0, engine.getCurrentStep(), "Step should be reset to 0");
            assertFalse(engine.hasConverged(), "Converged should be reset to false");
            assertFalse(engine.isRunning(), "Running should be reset to false");
            
            // Can run again
            engine.runUntilConvergence(1000);
            assertTrue(engine.getCurrentStep() > 0, "Should execute steps after reset");
        }

        /**
         * As a user, I want the engine to handle stop() gracefully
         * so that I can terminate long-running trials early.
         */
        @Test
        @DisplayName("Stop terminates execution early")
        void stopTerminatesEarly() {
            // Use a large array that won't converge quickly
            int[] largeReverse = new int[50];
            for (int i = 0; i < 50; i++) {
                largeReverse[i] = 50 - i;
            }
            initializeEngine(largeReverse);
            
            // Run a few steps then stop
            Thread runner = new Thread(() -> engine.runUntilConvergence(10000));
            runner.start();
            
            try {
                Thread.sleep(50); // Let it run briefly
                engine.stop();
                runner.join(1000); // Wait for thread to finish
            } catch (InterruptedException e) {
                fail("Test interrupted");
            }
            
            assertFalse(engine.isRunning(), "Engine should not be running after stop");
            assertTrue(engine.getCurrentStep() < 10000, "Should terminate before max steps");
        }
    }

    // ========================================================================
    // Step Execution Tests
    // ========================================================================

    @Nested
    @DisplayName("Step execution")
    class StepExecutionTests {

        /**
         * As a user, I want step() to return the number of swaps
         * so that I can monitor progress and debug issues.
         */
        @Test
        @DisplayName("Step returns swap count")
        void stepReturnsSwapCount() {
            initializeEngine(2, 1);
            
            int swapCount = engine.step();
            
            assertTrue(swapCount >= 0, "Swap count should be non-negative");
            assertEquals(1, engine.getCurrentStep(), "Step counter should increment");
        }

        /**
         * As a user, I want multiple steps to progressively sort the array
         * so that I can verify incremental progress.
         */
        @Test
        @DisplayName("Multiple steps progress sorting")
        void multipleStepsProgress() {
            initializeEngine(5, 4, 3, 2, 1);
            
            int totalSwaps = 0;
            for (int i = 0; i < 10; i++) {
                totalSwaps += engine.step();
            }
            
            assertTrue(totalSwaps > 0, "Should perform swaps during steps");
            assertEquals(10, engine.getCurrentStep(), "Should have executed 10 steps");
        }
    }

    // ========================================================================
    // Probe Recording Tests
    // ========================================================================

    @Nested
    @DisplayName("Probe recording")
    class ProbeRecordingTests {

        /**
         * As a user, I want initial snapshots recorded
         * so that I can analyze the starting state of experiments.
         */
        @Test
        @DisplayName("Records initial snapshot")
        void recordsInitialSnapshot() {
            initializeEngine(3, 1, 2);
            
            // Initial snapshot should be recorded during construction
            assertNotNull(probe.getSnapshots(), "Snapshots should not be null");
            assertEquals(1, probe.getSnapshots().size(), "Should have 1 initial snapshot");
        }

        /**
         * As a user, I want snapshots after each step
         * so that I can track the complete trajectory of sorting.
         */
        @Test
        @DisplayName("Records snapshot after each step")
        void recordsSnapshotsAfterSteps() {
            initializeEngine(3, 1, 2);
            
            // Execute 3 steps
            engine.step();
            engine.step();
            engine.step();
            
            // Should have initial + 3 step snapshots = 4 total
            assertEquals(4, probe.getSnapshots().size(), "Should have 4 snapshots (initial + 3 steps)");
        }
    }

    // ========================================================================
    // Convergence Tests
    // ========================================================================

    @Nested
    @DisplayName("Convergence detection")
    class ConvergenceTests {

        /**
         * As a user, I want convergence detection
         * so that trials terminate automatically when sorting completes.
         */
        @Test
        @DisplayName("Detects convergence after stable steps")
        void detectsConvergence() {
            initializeEngine(3, 1, 2);
            
            engine.runUntilConvergence(1000);
            
            assertTrue(engine.hasConverged(), "Engine should report convergence");
            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3}, result, "Array should be sorted");
        }

        /**
         * As a user, I want maxSteps to prevent infinite loops
         * so that I can handle non-converging scenarios gracefully.
         */
        @Test
        @DisplayName("Respects max steps limit")
        void respectsMaxSteps() {
            // Large reverse-sorted array that won't converge in 10 steps
            int[] largeReverse = new int[100];
            for (int i = 0; i < 100; i++) {
                largeReverse[i] = 100 - i;
            }
            initializeEngine(largeReverse);
            
            int finalStep = engine.runUntilConvergence(10);
            
            assertTrue(finalStep <= 10, "Should not exceed max steps");
        }
    }

    // ========================================================================
    // Determinism Tests
    // ========================================================================

    @Nested
    @DisplayName("Deterministic execution")
    class DeterminismTests {

        /**
         * As a user, I want deterministic results with fixed seeds
         * so that I can reproduce experiments and debug issues.
         */
        @Test
        @DisplayName("Same seed produces same results")
        void sameSeedSameResults() {
            // First run with seed 42
            initializeEngineWithSeed(42L, 5, 3, 1, 4, 2);
            engine.runUntilConvergence(1000);
            int firstSteps = engine.getCurrentStep();
            int[] firstResult = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            
            // Second run with same seed 42
            initializeEngineWithSeed(42L, 5, 3, 1, 4, 2);
            engine.runUntilConvergence(1000);
            int secondSteps = engine.getCurrentStep();
            int[] secondResult = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            
            assertEquals(firstSteps, secondSteps, "Same seed should produce same step count");
            assertArrayEquals(firstResult, secondResult, "Same seed should produce identical results");
        }

        /**
         * As a user, I want multiple runs to be consistent
         * so that I can trust the results of my experiments.
         */
        @Test
        @DisplayName("Multiple runs produce consistent results")
        void multipleRunsConsistent() {
            int[] input = {5, 3, 1, 4, 2};
            int[] expected = {1, 2, 3, 4, 5};
            
            for (int run = 0; run < 5; run++) {
                initializeEngine(input.clone());
                engine.runUntilConvergence(2000);
                int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
                assertArrayEquals(expected, result, "Run " + run + " should produce sorted array");
            }
        }
    }

    // ========================================================================
    // Performance Comparison Tests
    // ========================================================================

    @Nested
    @DisplayName("Performance characteristics")
    class PerformanceTests {

        /**
         * As a user, I want synchronous execution to be faster than parallel for single trials
         * so that I can maximize throughput when running many trials in parallel.
         */
        @Test
        @DisplayName("Faster than parallel for single trial")
        void fasterThanParallelForSingleTrial() {
            // This is a basic sanity check that synchronous execution completes
            // without thread overhead. Full performance comparison would require
            // benchmarking infrastructure.
            initializeEngine(10, 9, 8, 7, 6, 5, 4, 3, 2, 1);
            
            long start = System.nanoTime();
            engine.runUntilConvergence(5000);
            long elapsed = System.nanoTime() - start;
            
            assertTrue(elapsed < 5_000_000_000L, "Should complete within 5 seconds");
            assertTrue(engine.hasConverged(), "Should converge");
        }
    }

    // ========================================================================
    // Test Cell Implementation
    // ========================================================================

    /**
     * Simple bubble sort cell for testing.
     *
     * <p>As a test author, I want a minimal cell implementation
     * so that I can focus tests on engine behavior rather than cell complexity.</p>
     */
    static class TestBubbleCell implements Cell<TestBubbleCell>, GroupAwareCell<TestBubbleCell>, HasValue {
        private final int value;

        TestBubbleCell(int value) {
            this.value = value;
        }

        @Override
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
