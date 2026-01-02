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
 * Interface for recording execution trajectory by capturing snapshots at each step.
 *
 * <p>The probe maintains a complete history of cell states throughout
 * execution, enabling post-hoc analysis, visualization, and metric
 * computation.</p>
 *
 * @param <T> the type of cell
 */
public interface Probe<T extends HasValue & HasGroup & HasStatus & HasAlgotype> {

    /**
     * Record a snapshot at the given step.
     *
     * @param stepNumber current global step
     * @param cells current cell array
     * @param swapCount swaps in this step
     */
    void recordSnapshot(int stepNumber, T[] cells, int swapCount);

    /**
     * Get all snapshots.
     *
     * @return list of snapshots
     */
    List<StepSnapshot<T>> getSnapshots();

    /**
     * Get snapshot at specific step.
     *
     * @param stepNumber the step
     * @return the snapshot or null
     */
    StepSnapshot<T> getSnapshot(int stepNumber);

    /**
     * Get the last snapshot.
     *
     * @return the last snapshot or null
     */
    StepSnapshot<T> getLastSnapshot();

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

    /**
     * Clear all snapshots.
     */
    void clear();

    /**
     * Set recording enabled/disabled.
     *
     * @param enabled true to enable
     */
    void setRecordingEnabled(boolean enabled);

    /**
     * Check if recording is enabled.
     *
     * @return true if enabled
     */
    boolean isRecordingEnabled();

    // StatusProbe methods
    void recordSwap();
    void recordCompareAndSwap();
    void countFrozenSwapAttempt();
    int getSwapCount();
    int getCompareAndSwapCount();
    int getFrozenSwapAttempts();
    void resetCounters();
}