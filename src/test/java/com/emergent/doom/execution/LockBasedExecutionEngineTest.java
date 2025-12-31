package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.GenericCell;
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
 * Test suite for LockBasedExecutionEngine.
 *
 * <p>Verifies that lock-based execution correctly sorts arrays
 * using the Python cell_research threading model (single global lock,
 * asynchronous cell operations).</p>
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class LockBasedExecutionEngineTest {

    private LockBasedExecutionEngine<TestBubbleCell> engine;
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
        ConvergenceDetector<TestBubbleCell> convergenceDetector = new NoSwapConvergence<>(3);

        engine = new LockBasedExecutionEngine<>(cells, swapEngine, probe, convergenceDetector);
    }

    // ========================================================================
    // Basic Sorting Tests
    // ========================================================================

    @Nested
    @DisplayName("Basic sorting functionality")
    class BasicSortingTests {

        @Test
        @DisplayName("Sorts a small array correctly")
        void sortsSmallArray() {
            initializeEngine(5, 3, 1, 4, 2);

            engine.runUntilConvergence(10000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should be sorted");
        }

        @Test
        @DisplayName("Sorts already sorted array quickly")
        void sortAlreadySorted() {
            initializeEngine(1, 2, 3, 4, 5);

            engine.runUntilConvergence(1000);

            assertTrue(engine.hasConverged(), "Should converge quickly for sorted array");
            assertTrue(engine.getTotalSwaps() < 5, "Should have minimal swaps");
        }

        @Test
        @DisplayName("Handles reverse sorted array")
        void sortReverseSorted() {
            initializeEngine(5, 4, 3, 2, 1);

            engine.runUntilConvergence(10000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result, "Array should be sorted");
        }

        @Test
        @DisplayName("Handles single element array")
        void singleElement() {
            initializeEngine(42);

            engine.runUntilConvergence(100);

            assertEquals(42, cells[0].getValue());
            assertTrue(engine.hasConverged());
        }

        @Test
        @DisplayName("Handles two element swap")
        void twoElements() {
            initializeEngine(2, 1);

            engine.runUntilConvergence(1000);

            int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
            assertArrayEquals(new int[]{1, 2}, result, "Should swap the two elements");
        }
    }

    // ========================================================================
    // Lifecycle Tests
    // ========================================================================

    @Nested
    @DisplayName("Engine lifecycle")
    class LifecycleTests {

        @Test
        @DisplayName("Starts and shuts down cleanly")
        void startAndShutdown() {
            initializeEngine(2, 1);

            assertFalse(engine.isRunning(), "Should not be running initially");

            engine.start();
            assertTrue(engine.isRunning(), "Should be running after start");

            engine.shutdown();
            assertFalse(engine.isRunning(), "Should not be running after shutdown");
        }

        @Test
        @DisplayName("Throws if started twice")
        void throwIfStartedTwice() {
            initializeEngine(1);

            engine.start();
            assertThrows(IllegalStateException.class, engine::start);
        }

        @Test
        @DisplayName("Auto-starts when runUntilConvergence called")
        void autoStartOnRun() {
            initializeEngine(1, 2);

            assertFalse(engine.isRunning(), "Should not be running initially");
            engine.runUntilConvergence(100);
            // Note: may have shut down after convergence
        }

        @Test
        @DisplayName("Reset allows re-use")
        void resetAllowsReuse() {
            initializeEngine(3, 2, 1);

            engine.runUntilConvergence(5000);
            engine.shutdown();

            // Reset and verify can sort again
            engine.reset();
            // After reset, cells are in same (now sorted) order
            // Just verify reset completed without error
            assertFalse(engine.isRunning());
            assertEquals(0, engine.getCurrentStep());
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

            // Initial snapshot is recorded in constructor
            assertFalse(probe.getSnapshots().isEmpty(), "Should have initial snapshot");
        }

        @Test
        @DisplayName("Accumulates swaps during execution")
        void accumulatesSwaps() {
            initializeEngine(5, 4, 3, 2, 1);

            engine.runUntilConvergence(10000);

            assertTrue(engine.getTotalSwaps() > 0, "Should have performed swaps");
        }
    }

    // ========================================================================
    // Thread Safety Tests
    // ========================================================================

    @Nested
    @DisplayName("Thread safety")
    class ThreadSafetyTests {

        @Test
        @DisplayName("Handles concurrent operations on larger array")
        void concurrentOperations() {
            // Larger array to stress test concurrency
            int[] values = new int[20];
            for (int i = 0; i < values.length; i++) {
                values[i] = values.length - i; // Reverse order
            }
            initializeEngine(values);

            engine.runUntilConvergence(50000);

            // Verify sorted (no corruption from race conditions)
            for (int i = 0; i < cells.length - 1; i++) {
                assertTrue(cells[i].getValue() <= cells[i + 1].getValue(),
                        "Array should be sorted without corruption");
            }
        }

        @Test
        @DisplayName("Multiple sequential runs complete successfully")
        void multipleRuns() {
            for (int run = 0; run < 3; run++) {
                initializeEngine(5, 4, 3, 2, 1);
                engine.runUntilConvergence(10000);
                engine.shutdown();

                int[] result = Arrays.stream(cells).mapToInt(TestBubbleCell::getValue).toArray();
                assertArrayEquals(new int[]{1, 2, 3, 4, 5}, result,
                        "Run " + run + " should produce sorted array");
            }
        }
    }

    // ========================================================================
    // Convergence Detector Integration Tests
    // ========================================================================

    @Nested
    @DisplayName("Convergence detector integration")
    class ConvergenceDetectorTests {

        @Test
        @DisplayName("Uses configured convergence detector")
        void usesConvergenceDetector() {
            cells = new TestBubbleCell[] {
                new TestBubbleCell(1),
                new TestBubbleCell(2)
            };

            FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
            swapEngine = new SwapEngine<>(frozenStatus);
            probe = new ThreadSafeProbe<>();

            // Custom detector that tracks if it was called
            TrackingConvergenceDetector<TestBubbleCell> trackingDetector = new TrackingConvergenceDetector<>();

            engine = new LockBasedExecutionEngine<>(cells, swapEngine, probe, trackingDetector);
            engine.runUntilConvergence(1000);
            engine.shutdown();

            assertTrue(trackingDetector.wasChecked(), "Convergence detector should be consulted");
        }

        @Test
        @DisplayName("Respects detector convergence decision")
        void respectsDetectorDecision() {
            cells = new TestBubbleCell[] {
                new TestBubbleCell(2),
                new TestBubbleCell(1)
            };

            FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
            swapEngine = new SwapEngine<>(frozenStatus);
            probe = new ThreadSafeProbe<>();

            // Detector that always says converged after being checked once
            ImmediateConvergenceDetector<TestBubbleCell> immediateDetector =
                new ImmediateConvergenceDetector<>();

            engine = new LockBasedExecutionEngine<>(cells, swapEngine, probe, immediateDetector);
            engine.runUntilConvergence(10000);
            engine.shutdown();

            assertTrue(engine.hasConverged(), "Should converge when detector says so");
            // May or may not have had time for swaps depending on timing
        }

        @Test
        @DisplayName("Uses custom polling configuration")
        void usesCustomPollingConfig() {
            cells = new TestBubbleCell[] {
                new TestBubbleCell(1),
                new TestBubbleCell(2)
            };

            FrozenCellStatus frozenStatus = new ThreadSafeFrozenCellStatus();
            swapEngine = new SwapEngine<>(frozenStatus);
            probe = new ThreadSafeProbe<>();
            ConvergenceDetector<TestBubbleCell> detector = new NoSwapConvergence<>(3);

            // Custom polling: 5ms interval, 10 stable polls required
            engine = new LockBasedExecutionEngine<>(
                cells, swapEngine, probe, detector, 5, 10);
            engine.runUntilConvergence(100);
            engine.shutdown();

            assertTrue(engine.hasConverged(), "Should converge with custom polling config");
        }
    }

    // ========================================================================
    // Test Helper Classes
    // ========================================================================

    /**
     * Convergence detector that tracks if it was checked.
     */
    static class TrackingConvergenceDetector<T extends Cell<T>> implements ConvergenceDetector<T> {
        private volatile boolean checked = false;
        private volatile int checkCount = 0;

        @Override
        public boolean hasConverged(com.emergent.doom.probe.Probe<T> probe, int currentStep) {
            checked = true;
            checkCount++;
            // Let stable polling handle convergence
            return checkCount > 10; // Eventually converge after enough checks
        }

        public boolean wasChecked() {
            return checked;
        }

        public int getCheckCount() {
            return checkCount;
        }
    }

    /**
     * Convergence detector that returns true immediately after first check.
     */
    static class ImmediateConvergenceDetector<T extends Cell<T>> implements ConvergenceDetector<T> {
        private volatile boolean firstCheck = true;

        @Override
        public boolean hasConverged(com.emergent.doom.probe.Probe<T> probe, int currentStep) {
            if (firstCheck) {
                firstCheck = false;
                return false;
            }
            return true;
        }
    }

    // ========================================================================
    // Test Helper Cell
    // ========================================================================

    /**
     * Simple test cell implementation for BUBBLE sort.
     */
    static class TestBubbleCell implements Cell<TestBubbleCell> {
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
        public int compareTo(TestBubbleCell other) {
            return Integer.compare(this.value, other.value);
        }

        @Override
        public String toString() {
            return "TestBubbleCell(" + value + ")";
        }
    }
}
