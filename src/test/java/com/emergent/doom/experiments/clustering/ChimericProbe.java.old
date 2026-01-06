package com.emergent.doom.experiments.clustering;

import com.emergent.doom.cell.Cell;
import com.emergent.doom.chimeric.AlgotypeProvider;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.probe.StepSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Probe that records algotype information for chimeric populations.
 *
 * <p><strong>PURPOSE:</strong> Extends basic Probe to track algotype assignments
 * in chimeric experiments where GenericCell instances don't carry algotype metadata.</p>
 *
 * <p><strong>USAGE:</strong> Initialize with the same AlgotypeProvider used to create
 * the chimeric population, so algotype assignments can be looked up by position.</p>
 *
 * @param <T> the type of cell
 */
public class ChimericProbe<T extends Cell<T>> extends Probe<T> {

    private final AlgotypeProvider algotypeProvider;
    private final int arraySize;
    private final List<StepSnapshot<T>> chimericSnapshots;

    /**
     * Create a ChimericProbe with algotype tracking.
     *
     * @param algotypeProvider the provider that assigned algotypes to positions
     * @param arraySize the size of the cell array
     */
    public ChimericProbe(AlgotypeProvider algotypeProvider, int arraySize) {
        super();
        this.algotypeProvider = algotypeProvider;
        this.arraySize = arraySize;
        this.chimericSnapshots = new ArrayList<>();
    }

    /**
     * Record a snapshot with algotype information.
     *
     * <p>Overrides the base Probe to include algotype labels in the type information,
     * allowing AlgotypeAggregationIndex to compute clustering metrics.</p>
     *
     * @param stepNumber the step number
     * @param cells the current cell array
     * @param localSwapCount swaps in this step
     */
    @Override
    public void recordSnapshot(int stepNumber, T[] cells, int localSwapCount) {
        // Always update counters (convergence tracking)
        updateCounters(stepNumber, localSwapCount);

        if (isRecordingEnabled()) {
            List<Comparable<?>> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();

            for (int i = 0; i < cells.length; i++) {
                T cell = cells[i];
                values.add(cell);

                // Get algotype from provider by position
                String algotypeName = algotypeProvider.getAlgotype(i, arraySize);
                int algotypeLabel = algotypeNameToLabel(algotypeName);

                // Record type info with actual algotype
                int groupId = -1;  // Groups not supported
                int isFrozen = 0;  // Frozen status not tracked here
                types.add(new Object[]{groupId, algotypeLabel, cell, isFrozen});
            }

            chimericSnapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    /**
     * Get snapshots with algotype information.
     *
     * <p>Overrides parent method to return chimeric snapshots instead of base snapshots.</p>
     *
     * @return unmodifiable list of chimeric snapshots
     */
    @Override
    public List<StepSnapshot<T>> getSnapshots() {
        return Collections.unmodifiableList(chimericSnapshots);
    }

    /**
     * Convert algotype name to numeric label for type array.
     *
     * @param algotypeName the algotype name (e.g., "BUBBLE", "SELECTION")
     * @return numeric label (0=BUBBLE, 1=SELECTION, 2=INSERTION, etc.)
     */
    private int algotypeNameToLabel(String algotypeName) {
        switch (algotypeName.toUpperCase()) {
            case "BUBBLE":
                return 0;
            case "SELECTION":
                return 1;
            case "INSERTION":
                return 2;
            case "FIBONACCI":
                return 3;
            default:
                return -1;  // Unknown algotype
        }
    }
}
