package com.emergent.doom.probe;

import com.emergent.doom.cell.Cell;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe implementation of the Probe for parallel execution.
 *
 * <p>Uses {@link CopyOnWriteArrayList} for thread-safe snapshot storage.
 * This provides safe concurrent reads and writes at the cost of copy-on-write
 * overhead for modifications.</p>
 *
 * <p>This class extends {@link Probe} to provide a drop-in replacement
 * with thread safety guarantees for parallel execution mode.</p>
 *
 * <p><strong>Thread Safety:</strong> All methods are safe for concurrent
 * access. The {@link #recordSnapshot} method can be called from the main
 * thread while other threads read from the probe.</p>
 *
 * @param <T> the type of cell
 */
public class ThreadSafeProbe<T extends Cell<T>> extends Probe<T> {

    private final CopyOnWriteArrayList<StepSnapshot<T>> concurrentSnapshots;
    private volatile boolean recordingEnabled = true;

    public ThreadSafeProbe() {
        super();
        this.concurrentSnapshots = new CopyOnWriteArrayList<>();
    }

    /**
     * Record a snapshot of the current cell state.
     * Thread-safe for concurrent access.
     */
    @Override
    public void recordSnapshot(int stepNumber, T[] cells, int swapCount) {
        if (recordingEnabled) {
            concurrentSnapshots.add(new StepSnapshot<>(stepNumber, cells, swapCount));
        }
    }

    /**
     * Get all recorded snapshots.
     * Returns an unmodifiable view that is safe for concurrent iteration.
     */
    @Override
    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(concurrentSnapshots);
    }

    /**
     * Get snapshot at a specific step number.
     * Thread-safe.
     */
    @Override
    public StepSnapshot<T> getSnapshot(int stepNumber) {
        for (StepSnapshot<T> snapshot : concurrentSnapshots) {
            if (snapshot.getStepNumber() == stepNumber) {
                return snapshot;
            }
        }
        return null;
    }

    /**
     * Get the most recent snapshot.
     * Thread-safe.
     */
    @Override
    public StepSnapshot<T> getLastSnapshot() {
        int size = concurrentSnapshots.size();
        if (size == 0) {
            return null;
        }
        return concurrentSnapshots.get(size - 1);
    }

    /**
     * Get the total number of recorded snapshots.
     * Thread-safe.
     */
    @Override
    public int getSnapshotCount() {
        return concurrentSnapshots.size();
    }

    /**
     * Clear all recorded snapshots.
     * Thread-safe.
     */
    @Override
    public void clear() {
        concurrentSnapshots.clear();
    }

    /**
     * Enable or disable snapshot recording.
     * Thread-safe via volatile.
     */
    @Override
    public void setRecordingEnabled(boolean enabled) {
        this.recordingEnabled = enabled;
    }

    /**
     * Check if recording is currently enabled.
     * Thread-safe via volatile.
     */
    @Override
    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }
}
