package com.emergent.doom.probe;

import com.emergent.doom.cell.AlgotypedCell;
import com.emergent.doom.cell.Algotype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Probe for recording snapshots of AlgotypedCell arrays with cell-embedded algotypes.
 *
 * <p><strong>PURPOSE:</strong> Track sorting dynamics where algotypes are intrinsic cell
 * properties that move WITH cells during swaps (Levin-style architecture), in contrast
 * to ChimericProbe which reads algotypes from position-indexed AlgotypeProvider.</p>
 *
 * <p><strong>KEY DIFFERENCE FROM CHIMERICPROBE:</strong></p>
 * <ul>
 *   <li><strong>ChimericProbe:</strong> Reads algotype via {@code provider.getAlgotype(position)}.
 *       Algotypes stay at positions when cells swap.</li>
 *   <li><strong>AlgotypedProbe:</strong> Reads algotype via {@code cell.getAlgotype()}.
 *       Algotypes move WITH cells when they swap.</li>
 * </ul>
 *
 * <p><strong>USAGE:</strong></p>
 * <pre>{@code
 * AlgotypedProbe probe = new AlgotypedProbe();
 * probe.setRecordingEnabled(true);
 * 
 * // During execution, record snapshots
 * probe.recordSnapshot(stepNumber, cells, swapCount);
 * 
 * // After execution, retrieve snapshots
 * List<StepSnapshot<AlgotypedCell>> snapshots = probe.getSnapshots();
 * 
 * // Use with AlgotypeAggregationIndex to measure dynamic aggregation
 * AlgotypeAggregationIndex<AlgotypedCell> metric = new AlgotypeAggregationIndex<>();
 * double aggregation = metric.compute(snapshots.get(0));
 * }</pre>
 *
 * <p><strong>EXPERIMENTAL USE:</strong> This probe enables experiments comparing:
 * <ul>
 *   <li>EDE architecture: GenericCell + ChimericProbe → constant aggregation</li>
 *   <li>Levin architecture: AlgotypedCell + AlgotypedProbe → dynamic aggregation</li>
 * </ul>
 * </p>
 */
public class AlgotypedProbe extends Probe<AlgotypedCell> {

    private final List<StepSnapshot<AlgotypedCell>> algotypedSnapshots;

    /**
     * Create an AlgotypedProbe for tracking AlgotypedCell arrays.
     *
     * <p><strong>PURPOSE:</strong> Initialize probe with empty snapshot list.
     * Recording is disabled by default; call {@code setRecordingEnabled(true)} to enable.</p>
     */
    public AlgotypedProbe() {
        super();
        this.algotypedSnapshots = new ArrayList<>();
    }

    /**
     * Record a snapshot with algotypes read from cell objects.
     *
     * <p><strong>PURPOSE:</strong> Capture current array state where algotype information
     * comes from {@code cell.getAlgotype()}, not from position-based lookup.</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Update counters (step number, swap count) for convergence tracking</li>
     *   <li>If recording enabled, create snapshot:
     *     <ul>
     *       <li>For each position, read cell value and cell algotype</li>
     *       <li>Store type info as [groupId, algotypeLabel, cell, frozenFlag]</li>
     *       <li>AlgotypeLabel is read from cell, NOT from position</li>
     *     </ul>
     *   </li>
     *   <li>Add snapshot to list</li>
     * </ol>
     *
     * <p><strong>KEY IMPLEMENTATION DETAIL:</strong> The algotype label is obtained via
     * {@code cells[i].getAlgotype()}, which means if the cell at position i changes
     * (due to a swap), the recorded algotype for position i will also change.
     * This is the Levin-style behavior that enables dynamic aggregation.</p>
     *
     * @param stepNumber the current step number
     * @param cells the current cell array (algotypes embedded in cells)
     * @param localSwapCount number of swaps in this step
     */
    @Override
    public void recordSnapshot(int stepNumber, AlgotypedCell[] cells, int localSwapCount) {
        // Always update counters (convergence tracking)
        updateCounters(stepNumber, localSwapCount);

        if (isRecordingEnabled()) {
            List<Comparable<?>> values = new ArrayList<>();
            List<Object[]> types = new ArrayList<>();

            for (int i = 0; i < cells.length; i++) {
                AlgotypedCell cell = cells[i];
                values.add(cell);

                // Read algotype from cell object (not from position provider)
                // This is the KEY DIFFERENCE from ChimericProbe
                Algotype cellAlgotype = cell.getAlgotype();
                int algotypeLabel = algotypeToLabel(cellAlgotype);

                // Record type info with cell's intrinsic algotype
                int groupId = -1;  // Groups not supported
                int isFrozen = 0;  // Frozen status not tracked here
                types.add(new Object[]{groupId, algotypeLabel, cell, isFrozen});
            }

            algotypedSnapshots.add(new StepSnapshot<>(stepNumber, values, types, localSwapCount));
        }
    }

    /**
     * Get snapshots with cell-embedded algotype information.
     *
     * <p><strong>PURPOSE:</strong> Provide access to recorded snapshots for analysis
     * and visualization. Snapshots contain algotype information that reflects cell
     * identities (not position indices), enabling dynamic aggregation measurement.</p>
     *
     * @return unmodifiable list of snapshots
     */
    @Override
    public List<StepSnapshot<AlgotypedCell>> getSnapshots() {
        return Collections.unmodifiableList(algotypedSnapshots);
    }

    /**
     * Convert algotype enum to numeric label for type array.
     *
     * <p><strong>PURPOSE:</strong> Map Algotype enum to integer label expected by
     * AlgotypeAggregationIndex and other metrics that operate on type arrays.</p>
     *
     * <p><strong>LABEL MAPPING:</strong></p>
     * <ul>
     *   <li>BUBBLE → 0</li>
     *   <li>SELECTION → 1</li>
     *   <li>INSERTION → 2</li>
     *   <li>QUICK → 3</li>
     *   <li>MERGE → 4</li>
     *   <li>HEAP → 5</li>
     * </ul>
     *
     * @param algotype the algotype enum value
     * @return numeric label (0-5)
     */
    private int algotypeToLabel(Algotype algotype) {
        switch (algotype) {
            case BUBBLE:
                return 0;
            case SELECTION:
                return 1;
            case INSERTION:
                return 2;
            case QUICK:
                return 3;
            case MERGE:
                return 4;
            case HEAP:
                return 5;
            default:
                throw new IllegalArgumentException("Unknown algotype: " + algotype);
        }
    }

    /**
     * Clear all recorded snapshots.
     *
     * <p><strong>PURPOSE:</strong> Reset probe state for reuse in multiple experiments.
     * Useful when running batch trials with the same probe instance.</p>
     */
    public void clearSnapshots() {
        algotypedSnapshots.clear();
        reset();
    }
}
