package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.CellStatus;
import com.emergent.doom.group.CellGroup;
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
 *   <li>{@code swapCount} - Total successful swaps</li>
 *   <li>{@code compareAndSwapCount} - Comparisons that led to swap decisions</li>
 *   <li>{@code frozenSwapAttempts} - Attempts to swap with frozen cells</li>
 *   <li>{@code sortingSteps} - Value snapshots at each step</li>
 *   <li>{@code cellTypes} - Detailed per-cell type info [groupId, label, value, isFrozen]</li>
 * </ul>
 *
 * @param <T> the type of cell
 */
public class Probe<T extends Cell<T>> {

    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;

    // StatusProbe fields matching cell_research Python implementation
    private final AtomicInteger swapCount;
    private final AtomicInteger compareAndSwapCount;
    private final AtomicInteger frozenSwapAttempts;

    /**
     * Initialize an empty probe with all StatusProbe counters
     */
    public Probe() {
        this.snapshots = new ArrayList<>();
        this.recordingEnabled = true;
        this.swapCount = new AtomicInteger(0);
        this.compareAndSwapCount = new AtomicInteger(0);
        this.frozenSwapAttempts = new AtomicInteger(0);
    }

    /**
     * Record a snapshot with extracted values and detailed types to match Python take_snapshot().
     * Called after each swap in execution engine.
     *
     * @param stepNumber current global step
     * @param cells current cell array
     * @param localSwapCount swaps in this step (usually 1)
     */
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        if (recordingEnabled) {
            List<Comparable<?>> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();
            for (T cell : cells) {
                values.add(cell.getValue());
                int groupId = (cell.getGroup() != null) ? cell.getGroup().getGroupId() : -1;
                int algotypeLabel = cell.getAlgotype().ordinal(); // 0=Bubble, 1=Selection, 2=Insertion
                Comparable<?> value = cell.getValue();
                int isFrozen = (cell.getStatus() == CellStatus.FREEZE) ? 1 : 0;
                types.add(new Object[]{groupId, algotypeLabel, value, isFrozen});
            }
            swapCount.addAndGet(localSwapCount);
            snapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    /**
     * Deprecated: Use recordSnapshot for detailed fidelity to Python.
     */
    @Deprecated
    public void recordSnapshotWithTypes(int stepNumber, T[] cells, int swapCount) {
        recordSnapshot(stepNumber, cells, swapCount);
    }

    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    public StepSnapshot<T> getSnapshot(int stepNumber) {
        for (StepSnapshot<T> snapshot : snapshots) {
            if (snapshot.getStepNumber() == stepNumber) {
                return snapshot;
            }
        }
        return null;
    }

    public StepSnapshot<T> getLastSnapshot() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return snapshots.get(snapshots.size() - 1);
    }

    public int getSnapshotCount() {
        return snapshots.size();
    }

    public void clear() {
        snapshots.clear();
        resetCounters();
    }

    public void setRecordingEnabled(boolean enabled) {
        this.recordingEnabled = enabled;
    }

    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    // ========== StatusProbe Methods (matching cell_research Python) ==========

    /**
     * Record a successful swap. Called post-swap in cell logic.
     */
    public void recordSwap() {
        swapCount.incrementAndGet();
    }

    /**
     * Record a comparison that led to a swap decision.
     * Called when shouldMove() returns true.
     */
    public void recordCompareAndSwap() {
        compareAndSwapCount.incrementAndGet();
    }

    /**
     * Count an attempt to swap with a frozen cell.
     */
    public void countFrozenSwapAttempt() {
        frozenSwapAttempts.incrementAndGet();
    }

    public int getSwapCount() {
        return swapCount.get();
    }

    public int getCompareAndSwapCount() {
        return compareAndSwapCount.get();
    }

    public int getFrozenSwapAttempts() {
        return frozenSwapAttempts.get();
    }

    /**
     * Get detailed types snapshot for step (matches Python cell_types).
     */
    public List<Object[]> getTypesSnapshot(int stepNumber) {
        StepSnapshot<T> snapshot = getSnapshot(stepNumber);
        return (snapshot != null) ? snapshot.getTypes() : null;
    }

    /**
     * Get aggregate cell type distribution for compatibility.
     */
    public Map<Algotype, Integer> getCellTypeDistribution(int stepNumber) {
        List<Object[]> types = getTypesSnapshot(stepNumber);
        if (types != null) {
            Map<Algotype, Integer> dist = new HashMap<>();
            for (Object[] t : types) {
                int label = (Integer) t[1];
                Algotype type = Algotype.values()[label];
                dist.merge(type, 1, Integer::sum);
            }
            return dist;
        }
        return null;
    }

    public void resetCounters() {
        swapCount.set(0);
        compareAndSwapCount.set(0);
        frozenSwapAttempts.set(0);
    }
}
