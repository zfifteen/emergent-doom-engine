package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.CellStatus;
import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.cell.HasAlgotype;
import com.emergent.doom.group.CellGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic implementation of the Probe interface.
 *
 * <p>Records execution trajectory by capturing snapshots at each step.
 * This implementation matches the cell_research Python StatusProbe fields.</p>
 *
 * @param <T> the type of cell
 */
public class BasicProbe<T extends HasValue & HasGroup & HasStatus & HasAlgotype> implements Probe<T> {

    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;

    // StatusProbe fields matching cell_research Python implementation
    protected final AtomicInteger swapCount;
    private final AtomicInteger compareAndSwapCount;
    private final AtomicInteger frozenSwapAttempts;

    /**
     * Initialize an empty probe with all StatusProbe counters
     */
    public BasicProbe() {
        this.snapshots = new ArrayList<>();
        this.recordingEnabled = true;
        this.swapCount = new AtomicInteger(0);
        this.compareAndSwapCount = new AtomicInteger(0);
        this.frozenSwapAttempts = new AtomicInteger(0);
    }

    @Override
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        if (recordingEnabled) {
            List<Integer> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();
            for (T cell : cells) {
                values.add(cell.getValue());
                int groupId = (cell.getGroup() != null) ? cell.getGroup().getGroupId() : -1;
                int algotypeLabel = cell.getAlgotype().ordinal(); // 0=Bubble, 1=Selection, 2=Insertion
                int value = cell.getValue();
                int isFrozen = (cell.getStatus() == CellStatus.FREEZE) ? 1 : 0;
                types.add(new Object[]{groupId, algotypeLabel, value, isFrozen});
            }
            swapCount.addAndGet(localSwapCount);
            snapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    @Override
    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    @Override
    public StepSnapshot<T> getSnapshot(int stepNumber) {
        for (StepSnapshot<T> snapshot : snapshots) {
            if (snapshot.getStepNumber() == stepNumber) {
                return snapshot;
            }
        }
        return null;
    }

    @Override
    public StepSnapshot<T> getLastSnapshot() {
        if (snapshots.isEmpty()) {
            return null;
        }
        return snapshots.get(snapshots.size() - 1);
    }

    @Override
    public int getSnapshotCount() {
        return snapshots.size();
    }

    @Override
    public Algotype[] getTypesSnapshot(int step) {
        if (step < 0 || step >= snapshots.size()) {
            throw new IndexOutOfBoundsException("Step index out of bounds: " + step);
        }
        StepSnapshot<T> snapshot = snapshots.get(step);
        List<Object[]> types = snapshot.getTypes();
        return types.stream()
                .map(type -> Algotype.values()[(Integer) type[1]])
                .toArray(Algotype[]::new);
    }

    @Override
    public Map<Algotype, Integer> getCellTypeDistribution(int step) {
        Algotype[] types = getTypesSnapshot(step);
        Map<Algotype, Integer> distribution = new HashMap<>();
        for (Algotype type : types) {
            distribution.put(type, distribution.getOrDefault(type, 0) + 1);
        }
        return distribution;
    }

    @Override
    public void clear() {
        snapshots.clear();
        resetCounters();
    }

    @Override
    public void setRecordingEnabled(boolean enabled) {
        this.recordingEnabled = enabled;
    }

    @Override
    public boolean isRecordingEnabled() {
        return recordingEnabled;
    }

    // ========== StatusProbe Methods ==========

    @Override
    public void recordSwap() {
        swapCount.incrementAndGet();
    }

    @Override
    public void recordCompareAndSwap() {
        compareAndSwapCount.incrementAndGet();
    }

    @Override
    public void countFrozenSwapAttempt() {
        frozenSwapAttempts.incrementAndGet();
    }

    @Override
    public int getSwapCount() {
        return swapCount.get();
    }

    @Override
    public int getCompareAndSwapCount() {
        return compareAndSwapCount.get();
    }

    @Override
    public int getFrozenSwapAttempts() {
        return frozenSwapAttempts.get();
    }

    @Override
    public void resetCounters() {
        swapCount.set(0);
        compareAndSwapCount.set(0);
        frozenSwapAttempts.set(0);
    }
}