package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.group.CellStatus;
import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.cell.HasAlgotype;
import com.emergent.doom.group.CellGroup;
import java.util.List;
import java.util.Map;

/**
 * Records execution trajectory by capturing snapshots at each step.
 */
public class Probe<T extends HasValue & HasGroup & HasStatus & HasAlgotype> {

    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;

    // StatusProbe fields matching cell_research Python implementation
    protected final AtomicInteger swapCount;
    private final AtomicInteger compareAndSwapCount;
    private final AtomicInteger frozenSwapAttempts;
    
    // Convergence tracking (independent of recordingEnabled)
    private final AtomicInteger stepsSinceLastSwap;
    private final AtomicInteger totalSteps;

    public Probe() {
        this.snapshots = new ArrayList<>();
        this.recordingEnabled = true;
        this.swapCount = new AtomicInteger(0);
        this.compareAndSwapCount = new AtomicInteger(0);
        this.frozenSwapAttempts = new AtomicInteger(0);
        this.stepsSinceLastSwap = new AtomicInteger(0);
        this.totalSteps = new AtomicInteger(0);
    }

    /**
     * Record a snapshot. 
     * CRITICAL: Now tracks convergence metrics even if recordingEnabled is false.
     */
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        totalSteps.set(stepNumber);
        
        if (localSwapCount > 0) {
            stepsSinceLastSwap.set(0);
            swapCount.addAndGet(localSwapCount);
        } else {
            stepsSinceLastSwap.incrementAndGet();
        }

        if (recordingEnabled) {
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
            snapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    public int getStepsSinceLastSwap() {
        return stepsSinceLastSwap.get();
    }

    public int getTotalSteps() {
        return totalSteps.get();
    }

    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    public StepSnapshot<T> getLastSnapshot() {
        if (snapshots.isEmpty()) return null;
        return snapshots.get(snapshots.size() - 1);
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

    public void recordSwap() {
        swapCount.incrementAndGet();
    }

    public void recordCompareAndSwap() {
        compareAndSwapCount.incrementAndGet();
    }

    public void countFrozenSwapAttempt() {
        frozenSwapAttempts.incrementAndGet();
    }

    /**
     * Get the total number of snapshots recorded.
     *
     * @return number of snapshots
     */
    int getSnapshotCount();

    /**
     * Get the types snapshot at a specific step.
     *
     * @param step the step index
     * @return list of type arrays at that step
     */
    List<Object[]> getTypesSnapshot(int step);

    /**
     * Get the cell type distribution at a specific step.
     *
     * @param step the step index
     * @return map of algotype to count
     */
    Map<Algotype, Integer> getCellTypeDistribution(int step);

    public void resetCounters() {
        swapCount.set(0);
        compareAndSwapCount.set(0);
        frozenSwapAttempts.set(0);
        stepsSinceLastSwap.set(0);
        totalSteps.set(0);
    }
}
