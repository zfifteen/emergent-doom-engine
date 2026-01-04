package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Probe class, including StatusProbe fields (Gap 5.1).
 */
class ProbeTest {

    private Probe<GenericCell> probe;

    @BeforeEach
    void setUp() {
        probe = new Probe<>();
    }

    @Nested
    @DisplayName("Gap 5.1: StatusProbe Fields")
    class StatusProbeFieldsTests {

        /**
         * PURPOSE: As a developer, I want compareAndSwapCount to start at zero
         * so that I can verify the initial state of a new Probe instance.
         *
         * INPUTS: New Probe instance
         * EXPECTED OUTPUT: getCompareAndSwapCount() returns 0
         * TEST DATA: probe = new Probe<>()
         * REPRODUCTION: System.out.println("probe.getCompareAndSwapCount() = " + probe.getCompareAndSwapCount())
         */
        @Test
        @DisplayName("compareAndSwapCount starts at zero")
        void compareAndSwapCountStartsAtZero() {
            assertEquals(0, probe.getCompareAndSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want recordCompareAndSwap to increment the counter
         * so that I can track the number of swap attempts during execution.
         *
         * INPUTS: Probe instance with recordCompareAndSwap() called 3 times
         * EXPECTED OUTPUT: getCompareAndSwapCount() returns 3
         * TEST DATA: 3 recordCompareAndSwap() calls
         * REPRODUCTION: Call probe.recordCompareAndSwap() three times then print counter
         */
        @Test
        @DisplayName("recordCompareAndSwap increments counter")
        void recordCompareAndSwapIncrementsCounter() {
            probe.recordCompareAndSwap();
            probe.recordCompareAndSwap();
            probe.recordCompareAndSwap();

            assertEquals(3, probe.getCompareAndSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want frozenSwapAttempts to start at zero
         * so that I can verify the initial state of frozen swap tracking.
         *
         * INPUTS: New Probe instance
         * EXPECTED OUTPUT: getFrozenSwapAttempts() returns 0
         * TEST DATA: probe = new Probe<>()
         * REPRODUCTION: System.out.println("probe.getFrozenSwapAttempts() = " + probe.getFrozenSwapAttempts())
         */
        @Test
        @DisplayName("frozenSwapAttempts starts at zero")
        void frozenSwapAttemptsStartsAtZero() {
            assertEquals(0, probe.getFrozenSwapAttempts());
        }

        /**
         * PURPOSE: As a developer, I want countFrozenSwapAttempt to increment the counter
         * so that I can track how many swaps were blocked by frozen cells.
         *
         * INPUTS: Probe instance with countFrozenSwapAttempt() called 2 times
         * EXPECTED OUTPUT: getFrozenSwapAttempts() returns 2
         * TEST DATA: 2 countFrozenSwapAttempt() calls
         * REPRODUCTION: Call probe.countFrozenSwapAttempt() twice then print counter
         */
        @Test
        @DisplayName("countFrozenSwapAttempt increments counter")
        void countFrozenSwapAttemptIncrementsCounter() {
            probe.countFrozenSwapAttempt();
            probe.countFrozenSwapAttempt();

            assertEquals(2, probe.getFrozenSwapAttempts());
        }

        /**
         * PURPOSE: As a developer, I want clear() to reset all counters
         * so that I can reuse the same Probe instance for multiple trials.
         *
         * INPUTS: Probe with compareAndSwapCount=2 and frozenSwapAttempts=1
         * EXPECTED OUTPUT: Both counters reset to 0 after clear()
         * TEST DATA: 2 recordCompareAndSwap() calls, 1 countFrozenSwapAttempt() call
         * REPRODUCTION: Increment counters, call clear(), then verify both are 0
         */
        @Test
        @DisplayName("clear() resets all counters")
        void clearResetsAllCounters() {
            probe.recordCompareAndSwap();
            probe.recordCompareAndSwap();
            probe.countFrozenSwapAttempt();

            probe.clear();

            assertEquals(0, probe.getCompareAndSwapCount());
            assertEquals(0, probe.getFrozenSwapAttempts());
        }

        /**
         * PURPOSE: As a developer, I want resetCounters() to reset counters without clearing snapshots
         * so that I can reset metrics between runs while preserving trajectory data.
         *
         * INPUTS: Probe with 1 snapshot, compareAndSwapCount=1, frozenSwapAttempts=1
         * EXPECTED OUTPUT: Counters reset to 0, snapshot count remains 1
         * TEST DATA: cells=[1,2], 1 snapshot, 1 compareAndSwap, 1 frozenSwapAttempt
         * REPRODUCTION: Record snapshot and increment counters, call resetCounters(), verify snapshot preserved
         */
        @Test
        @DisplayName("resetCounters() resets counters without clearing snapshots")
        void resetCountersWithoutClearingSnapshots() {
            GenericCell[] cells = {
                new GenericCell(1),
                new GenericCell(2)
            };
            probe.recordSnapshot(0, cells, 0);
            probe.recordCompareAndSwap();
            probe.countFrozenSwapAttempt();

            probe.resetCounters();

            assertEquals(0, probe.getCompareAndSwapCount());
            assertEquals(0, probe.getFrozenSwapAttempts());
            assertEquals(1, probe.getSnapshotCount()); // Snapshots preserved
        }

        /**
         * PURPOSE: As a developer, I want StatusProbe counters to be thread-safe
         * so that parallel execution engines can safely record metrics from multiple threads.
         *
         * INPUTS: 10 threads each incrementing counters 1000 times
         * EXPECTED OUTPUT: Final counters equal 10,000 (no lost updates)
         * TEST DATA: threadCount=10, incrementsPerThread=1000
         * REPRODUCTION: Start 10 threads incrementing counters, join all, verify totals
         */
        @Test
        @DisplayName("StatusProbe counters are thread-safe")
        void statusProbeCountersAreThreadSafe() throws InterruptedException {
            int threadCount = 10;
            int incrementsPerThread = 1000;
            Thread[] threads = new Thread[threadCount];

            for (int i = 0; i < threadCount; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        probe.recordCompareAndSwap();
                        probe.countFrozenSwapAttempt();
                    }
                });
                threads[i].start();
            }

            for (Thread t : threads) {
                t.join();
            }

            assertEquals(threadCount * incrementsPerThread, probe.getCompareAndSwapCount());
            assertEquals(threadCount * incrementsPerThread, probe.getFrozenSwapAttempts());
        }
    }

    @Nested
    @DisplayName("Basic Snapshot Functionality")
    class BasicSnapshotTests {

        /**
         * PURPOSE: As a developer, I want recordSnapshot to add snapshots to the list
         * so that I can track the execution trajectory over time.
         *
         * INPUTS: Two recordSnapshot() calls with step numbers 0 and 1
         * EXPECTED OUTPUT: getSnapshotCount() returns 2
         * TEST DATA: cells=[1], steps 0 and 1, swapCounts 0 and 1
         * REPRODUCTION: Record two snapshots then print snapshot count
         */
        @Test
        @DisplayName("recordSnapshot adds snapshot to list")
        void recordSnapshotAddsToList() {
            GenericCell[] cells = {
                new GenericCell(1)
            };

            probe.recordSnapshot(0, cells, 0);
            probe.recordSnapshot(1, cells, 1);

            assertEquals(2, probe.getSnapshotCount());
        }

        /**
         * PURPOSE: As a developer, I want getSnapshot to return the correct snapshot by step number
         * so that I can analyze specific points in the execution trajectory.
         *
         * INPUTS: Snapshot recorded at step 5 with swapCount 3
         * EXPECTED OUTPUT: getSnapshot(5) returns snapshot with stepNumber=5, swapCount=3
         * TEST DATA: cells=[42], stepNumber=5, swapCount=3
         * REPRODUCTION: Record snapshot at step 5, retrieve it, verify step number and swap count
         */
        @Test
        @DisplayName("getSnapshot returns correct snapshot by step number")
        void getSnapshotReturnsByStepNumber() {
            GenericCell[] cells = {
                new GenericCell(42)
            };

            probe.recordSnapshot(5, cells, 3);

            StepSnapshot<GenericCell> snapshot = probe.getSnapshot(5);
            assertNotNull(snapshot);
            assertEquals(5, snapshot.getStepNumber());
            assertEquals(3, snapshot.getSwapCount());
        }

        /**
         * PURPOSE: As a developer, I want setRecordingEnabled(false) to prevent snapshot recording
         * so that I can disable trajectory tracking for performance-critical experiments.
         *
         * INPUTS: Probe with recording disabled, recordSnapshot() called once
         * EXPECTED OUTPUT: getSnapshotCount() returns 0 (snapshot was not recorded)
         * TEST DATA: cells=[1], recording=false
         * REPRODUCTION: Disable recording, attempt to record snapshot, verify count is 0
         */
        @Test
        @DisplayName("setRecordingEnabled(false) prevents snapshot recording")
        void disabledRecordingPreventsSnapshots() {
            GenericCell[] cells = {
                new GenericCell(1)
            };

            probe.setRecordingEnabled(false);
            probe.recordSnapshot(0, cells, 0);

            assertEquals(0, probe.getSnapshotCount());
        }
    }
}
