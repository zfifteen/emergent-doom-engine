package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import com.emergent.doom.cell.HasAlgotype;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Records execution trajectory by capturing snapshots at each step.
 */
public class Probe<T extends Cell<T>> {

    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;

    // StatusProbe fields matching cell_research Python implementation
    protected final AtomicInteger swapCount;
    private final AtomicInteger compareAndSwapCount;
    private final AtomicInteger frozenSwapAttempts;

    // Convergence tracking (independent of recordingEnabled)
    protected final AtomicInteger stepsSinceLastSwap;
    protected final AtomicInteger totalSteps;

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
        updateCounters(stepNumber, localSwapCount);

        if (recordingEnabled) {
            List<Comparable<?>> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();
            for (T cell : cells) {
                Comparable<?> value = cell.getValue();
                values.add(value);

                int groupId = -1;
                int algotypeLabel = 0;
                if (cell instanceof HasAlgotype) {
                    Algotype a = ((HasAlgotype) cell).getAlgotype();
                    if (a != null) {
                        algotypeLabel = a.ordinal();
                    }
                }
                int isFrozen = 0;
                types.add(new Object[]{groupId, algotypeLabel, value, isFrozen});
            }
            snapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    /**
     * Updates internal counters for convergence tracking.
     * Protected so subclasses (ThreadSafeProbe) can reuse this logic.
     */
    protected void updateCounters(int stepNumber, int localSwapCount) {
        totalSteps.set(stepNumber);

        if (localSwapCount > 0) {
            stepsSinceLastSwap.set(0);
            swapCount.addAndGet(localSwapCount);
        } else {
            stepsSinceLastSwap.incrementAndGet();
        }
    }

    /**
     * Records a snapshot with additional type information.
     *
     * @param stepNumber the step number of the snapshot
     * @param cells      the array of cells to record
     * @param localSwapCount the number of swaps in this step
     */
    public void recordSnapshotWithTypes(int stepNumber, T[] cells, int localSwapCount) {
        recordSnapshot(stepNumber, cells, localSwapCount);
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

    /**
     * Returns the number of snapshots recorded.
     *
     * <p>This satisfies the requirement for tracking the size of the recorded trajectory
     * and is used by analysis tools to validate data availability.</p>
     */
    public int getSnapshotCount() {
        return snapshots.size();
    }

    /**
     * Retrieves a specific snapshot by its step number.
     *
     * <p>This is the primary lookup mechanism for historical state. It performs
     * a linear search through the recorded snapshots.</p>
     *
     * @param stepNumber the step number to find
     * @return the matching snapshot, or null if not found
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
     * Retrieves the type metadata for a specific step.
     *
     * <p>Delegates to getSnapshot() to find the relevant step and then
     * extracts the type information captured in that snapshot.</p>
     *
     * @param stepNumber the step number to find
     * @return list of type metadata arrays, or null if step not found
     */
    public List<Object[]> getTypesSnapshot(int stepNumber) {
        StepSnapshot<T> snapshot = getSnapshot(stepNumber);
        return (snapshot != null) ? snapshot.getTypes() : null;
    }

    /**
     * Retrieves the cell type distribution for a specific step.
     *
     * <p>Delegates to getSnapshot() to find the relevant step and then
     * uses the snapshot's built-in distribution calculation.</p>
     *
     * @param stepNumber the step number to find
     * @return map of algotypes to their counts, or null if step not found
     */
    public Map<Algotype, Integer> getCellTypeDistribution(int stepNumber) {
        StepSnapshot<T> snapshot = getSnapshot(stepNumber);
        return (snapshot != null) ? snapshot.getCellTypeDistribution() : null;
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

    public int getSwapCount() {
        return swapCount.get();
    }

    public int getCompareAndSwapCount() {
        return compareAndSwapCount.get();
    }

    public int getFrozenSwapAttempts() {
        return frozenSwapAttempts.get();
    }

    public void resetCounters() {
        swapCount.set(0);
        compareAndSwapCount.set(0);
        frozenSwapAttempts.set(0);
        stepsSinceLastSwap.set(0);
        totalSteps.set(0);
    }
}
