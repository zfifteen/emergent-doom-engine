package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.CellStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe implementation of the Probe for parallel execution.
 *
 * <p>Uses CopyOnWriteArrayList for concurrent snapshot adds and iterations.</p>
 *
 * <p>Overrides extraction in recordSnapshot for consistency.</p>
 * 
 * <p>Note: Works with lightweight cells that don't carry metadata.</p>
 */
public class ThreadSafeProbe<T extends Cell<T>> extends Probe<T> {

    private final CopyOnWriteArrayList<StepSnapshot<T>> concurrentSnapshots;

    public ThreadSafeProbe() {
        super();
        this.concurrentSnapshots = new CopyOnWriteArrayList<>();
    }

    @Override
    @Override
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        // Update convergence metrics in parent (atomic)
        updateCounters(stepNumber, localSwapCount);

        if (super.isRecordingEnabled()) {
            List<Comparable<?>> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();
            for (T cell : cells) {
                // Cell is Comparable - use it directly as value
                values.add(cell);
                // Metadata no longer available from cells - record minimal info
                int groupId = -1;  // Groups not supported with lightweight cells
                int algotypeLabel = 0;  // Algotype not available from cell
                int isFrozen = 0;  // Status not available from cell
                types.add(new Object[]{groupId, algotypeLabel, cell, isFrozen});
            }
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

    @Override
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
