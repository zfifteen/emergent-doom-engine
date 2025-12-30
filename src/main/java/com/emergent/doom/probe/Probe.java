package com.emergent.doom.probe;

import com.emergent.doom.cell.Cell;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Records execution trajectory by capturing snapshots at each step.
 * 
 * <p>The probe maintains a complete history of cell states throughout
 * execution, enabling post-hoc analysis, visualization, and metric
 * computation.</p>
 * 
 * @param <T> the type of cell
 */
public class Probe<T extends Cell<T>> {
    
    private final List<StepSnapshot<T>> snapshots;
    private boolean recordingEnabled;
    
    // PURPOSE: Initialize an empty probe
    // INPUTS: None
    // PROCESS:
    //   1. Create new ArrayList for snapshots
    //   2. Set recordingEnabled = true by default
    // OUTPUTS: Probe instance
    // DEPENDENCIES: ArrayList
    /**
     * IMPLEMENTED: Initialize an empty probe
     */
    public Probe() {
        this.snapshots = new ArrayList<>();
        this.recordingEnabled = true;
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
     * IMPLEMENTED: Clear all recorded snapshots
     */
    public void clear() {
        snapshots.clear();
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
}
