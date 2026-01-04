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

        @Test
        @DisplayName("compareAndSwapCount starts at zero")
        void compareAndSwapCountStartsAtZero() {
            assertEquals(0, probe.getCompareAndSwapCount());
        }

        @Test
        @DisplayName("recordCompareAndSwap increments counter")
        void recordCompareAndSwapIncrementsCounter() {
            probe.recordCompareAndSwap();
            probe.recordCompareAndSwap();
            probe.recordCompareAndSwap();

            assertEquals(3, probe.getCompareAndSwapCount());
        }

        @Test
        @DisplayName("frozenSwapAttempts starts at zero")
        void frozenSwapAttemptsStartsAtZero() {
            assertEquals(0, probe.getFrozenSwapAttempts());
        }

        @Test
        @DisplayName("countFrozenSwapAttempt increments counter")
        void countFrozenSwapAttemptIncrementsCounter() {
            probe.countFrozenSwapAttempt();
            probe.countFrozenSwapAttempt();

            assertEquals(2, probe.getFrozenSwapAttempts());
        }

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
    @DisplayName("Cell Type Distribution Tracking")

    @Nested
    @DisplayName("Basic Snapshot Functionality")
    class BasicSnapshotTests {

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
