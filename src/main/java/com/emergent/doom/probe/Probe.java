package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records execution trajectory by capturing snapshots at each step.
 *
 * <p>The probe maintains a complete history of cell states throughout
 * execution, enabling post-hoc analysis, visualization, and metric
 * computation.</p>
 *
 * <p><strong>StatusProbe Fields (per cell_research):</strong></p>
 * <ul>
 *   <li>{@code compareAndSwapCount} - Comparisons that led to swap decisions</li>
 *   <li>{@code frozenSwapAttempts} - Attempts to swap with frozen cells</li>
 *   <li>{@code cellTypes} - Cell type distribution tracked in snapshots</li>
 * </ul>
 *
 * @param <T> the type of cell
 */
public class Probe<T extends Cell<T>> {

    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;

    // StatusProbe fields matching cell_research Python implementation
    private final AtomicInteger compareAndSwapCount;
    private final AtomicInteger frozenSwapAttempts;
    
    /**
     * IMPLEMENTED: Initialize an empty probe with all StatusProbe counters
     */
    public Probe() {
        this.snapshots = new ArrayList<>();
        this.recordingEnabled = true;
        this.compareAndSwapCount = new AtomicInteger(0);
        this.frozenSwapAttempts = new AtomicInteger(0);
    }
    
    /**
     * IMPLEMENTED: Record a snapshot of the current cell state
     */
    public void recordSnapshot(int stepNumber, T[] cells, int swapCount) {
        if (recordingEnabled) {
            snapshots.add(new StepSnapshot<>(stepNumber, cells, swapCount));
        }
    }
    
    /**
     * IMPLEMENTED: Get all recorded snapshots
     */
    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }
    
    /**
     * IMPLEMENTED: Get snapshot at a specific step number
     */
    public StepSnapshot<T> getSnapshot(int stepNumber) {
        for (StepSnapshot<T> snapshot : snapshots) {
            if (snapshot.getStepNumber() == stepNumber) {
                return snapshot;
            }
        }
        return null;
    }
    
    /**
     * IMPLEMENTED: Get the most recent snapshot
     */
    public StepSnapshot<T> getLastSnapshot() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return snapshots.get(snapshots.size() - 1);
    }
    
    /**
     * IMPLEMENTED: Get the total number of recorded snapshots
     */
    public int getSnapshotCount() {
        return snapshots.size();
    }
    
    /**
     * IMPLEMENTED: Clear all recorded snapshots and reset counters
     */
    public void clear() {
        snapshots.clear();
        resetCounters();
    }
    
    /**
     * IMPLEMENTED: Enable or disable snapshot recording
     * Useful for performance when trajectory not needed
     */
    public void setRecordingEnabled(boolean enabled) {
        this.recordingEnabled = enabled;
    }
    
    /**
     * IMPLEMENTED: Check if recording is currently enabled
     */
    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    // ========== StatusProbe Methods (matching cell_research Python) ==========

    /**
     * Record a comparison that led to a swap decision.
     * Called when should_move() returns true in Python cell_research.
     * Thread-safe.
     */
    public void recordCompareAndSwap() {
        compareAndSwapCount.incrementAndGet();
    }

    /**
     * Count an attempt to swap with a frozen cell.
     * Called when a cell tries to initiate a swap but is frozen.
     * Thread-safe.
     */
    public void countFrozenSwapAttempt() {
        frozenSwapAttempts.incrementAndGet();
    }

    /**
     * Get the total number of comparisons that led to swap decisions.
     * Thread-safe read.
     */
    public int getCompareAndSwapCount() {
        return compareAndSwapCount.get();
    }

    /**
     * Get the total number of frozen swap attempts.
     * Thread-safe read.
     */
    public int getFrozenSwapAttempts() {
        return frozenSwapAttempts.get();
    }

    /**
     * Record a snapshot with cell type distribution.
     * This extended version captures algotype distribution for each step.
     *
     * @param stepNumber the current step number
     * @param cells the cell array
     * @param swapCount swaps in this step
     */
    public void recordSnapshotWithTypes(int stepNumber, T[] cells, int swapCount) {
        if (recordingEnabled) {
            Map<Algotype, Integer> typeDistribution = new HashMap<>();
            for (T cell : cells) {
                Algotype type = cell.getAlgotype();
                typeDistribution.merge(type, 1, Integer::sum);
            }
            snapshots.add(new StepSnapshot<>(stepNumber, cells, swapCount, typeDistribution));
        }
    }

    /**
     * Get the cell type distribution at a specific step.
     *
     * @param stepNumber the step to query
     * @return map of algotype to count, or null if step not found
     */
    public Map<Algotype, Integer> getCellTypeDistribution(int stepNumber) {
        StepSnapshot<T> snapshot = getSnapshot(stepNumber);
        if (snapshot != null) {
            return snapshot.getCellTypeDistribution();
        }
        return null;
    }

    /**
     * Reset all StatusProbe counters (called on engine reset).
     */
    public void resetCounters() {
        compareAndSwapCount.set(0);
        frozenSwapAttempts.set(0);
    }
}
