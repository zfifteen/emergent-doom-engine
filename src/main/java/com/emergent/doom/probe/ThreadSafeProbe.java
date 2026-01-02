package com.emergent.doom.probe;

import com.emergent.doom.group.CellStatus;
import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.cell.HasAlgotype;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.CellStatus;
import com.emergent.doom.group.CellGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe implementation of the Probe for parallel execution.
 *
 * <p>Uses CopyOnWriteArrayList for concurrent snapshot adds and iterations.</p>
 *
 * <p>Overrides extraction in recordSnapshot for consistency.</p>
 */
public class ThreadSafeProbe<T extends HasValue & HasGroup & HasStatus & HasAlgotype> extends BasicProbe<T> implements Probe<T> {

    private final CopyOnWriteArrayList<StepSnapshot<T>> concurrentSnapshots;

    public ThreadSafeProbe() {
        super();
        this.concurrentSnapshots = new CopyOnWriteArrayList<>();
    }

    @Override
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        if (super.isRecordingEnabled()) {
            List<Integer> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();
            for (T cell : cells) {
                values.add(cell.getValue());
                int groupId = (cell.getGroup() != null) ? cell.getGroup().getGroupId() : -1;
                int algotypeLabel = cell.getAlgotype().ordinal();
                int value = cell.getValue();
                int isFrozen = (cell.getStatus() == CellStatus.FREEZE) ? 1 : 0;
                types.add(new Object[]{groupId, algotypeLabel, value, isFrozen});
            }
            swapCount.addAndGet(localSwapCount); // Direct access now protected
            concurrentSnapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    @Override
    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(concurrentSnapshots);
    }

    @Override
    public StepSnapshot<T> getSnapshot(int stepNumber) {
        for (StepSnapshot<T> snapshot : concurrentSnapshots) {
            if (snapshot.getStepNumber() == stepNumber) {
                return snapshot;
            }
        }
        return null;
    }

    @Override
    public StepSnapshot<T> getLastSnapshot() {
        int size = concurrentSnapshots.size();
        if (size == 0) {
            return null;
        }
        return concurrentSnapshots.get(size - 1);
    }

    public int getSnapshotCount() {
        return concurrentSnapshots.size();
    }

    @Override
    public void clear() {
        concurrentSnapshots.clear();
        super.resetCounters();
    }

    // Delegate to super for recordingEnabled (no shadowing)
    @Override
    public void setRecordingEnabled(boolean enabled) {
        super.setRecordingEnabled(enabled);
    }

    @Override
    public boolean isRecordingEnabled() {
        return super.isRecordingEnabled();
    }

    @Override
    public void recordSwap() {
        super.recordSwap();
    }

    @Override
    public void recordCompareAndSwap() {
        super.recordCompareAndSwap();
    }

    @Override
    public void countFrozenSwapAttempt() {
        super.countFrozenSwapAttempt();
    }

    @Override
    public int getSwapCount() {
        return super.getSwapCount();
    }

    @Override
    public int getCompareAndSwapCount() {
        return super.getCompareAndSwapCount();
    }

    @Override
    public int getFrozenSwapAttempts() {
        return super.getFrozenSwapAttempts();
    }

    @Override
    public List<Object[]> getTypesSnapshot(int stepNumber) {
        return super.getTypesSnapshot(stepNumber);
    }

    @Override
    public Map<Algotype, Integer> getCellTypeDistribution(int stepNumber) {
        return super.getCellTypeDistribution(stepNumber);
    }

    @Override
    public void resetCounters() {
        super.resetCounters();
    }
}
