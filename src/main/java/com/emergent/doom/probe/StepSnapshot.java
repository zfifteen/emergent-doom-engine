package com.emergent.doom.probe;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.Cell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of cell states at a specific execution step.
 *
 * <p>Step snapshots enable trajectory analysis and visualization by
 * capturing extracted values and types at each iteration, matching Python take_snapshot().</p>
 *
 * <p>Format: values = List of cell values; types = List of [groupId, algotypeLabel, value, isFrozen].</p>
 *
 * @param <T> the type of cell (for compatibility)
 */
public class StepSnapshot<T extends Cell<T>> {

    private final int stepNumber;
    private final List<Comparable<?>> values;
    private final List<Object[]> types;
    private final int swapCount;
    private final long timestamp;

    /**
     * Create snapshot with extracted values and types (primary constructor).
     */
    public StepSnapshot(int stepNumber, List<Comparable<?>> values, List<Object[]> types, int swapCount) {
        this.stepNumber = stepNumber;
        this.values = Collections.unmodifiableList(new ArrayList<>(values));
        this.types = Collections.unmodifiableList(new ArrayList<>(types));
        this.swapCount = swapCount;
        this.timestamp = System.nanoTime();
    }

    /**
     * Deprecated compatibility constructor with full cells (shallow copy).
     */
    @Deprecated
    public StepSnapshot(int stepNumber, T[] cellStates, int swapCount, Map<Algotype, Integer> cellTypeDistribution) {
        this.stepNumber = stepNumber;
        // Extract for fidelity
        List<Comparable<?>> vals = new ArrayList<>();
        List<Object[]> tys = new ArrayList<>();
        for (T cell : cellStates) {
            vals.add(cell.getValue());
            int groupId = (cell.getGroup() != null) ? cell.getGroup().getGroupId() : -1;
            int label = cell.getAlgotype().ordinal();
            int frozen = cell.getStatus() == com.emergent.doom.cell.CellStatus.FREEZE ? 1 : 0;
            tys.add(new Object[]{groupId, label, cell.getValue(), frozen});
        }
        this.values = Collections.unmodifiableList(vals);
        this.types = Collections.unmodifiableList(tys);
        this.swapCount = swapCount;
        this.timestamp = System.nanoTime();
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public List<Comparable<?>> getValues() {
        return Collections.unmodifiableList(values);
    }

    public List<Object[]> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public int getSwapCount() {
        return swapCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getArraySize() {
        return values.size();
    }

    /**
     * Deprecated: Use getValues() for extracted values.
     */
    @Deprecated
    public T[] getCellStates() {
        throw new UnsupportedOperationException("Use getValues() and getTypes() for immutability and fidelity");
    }

    /**
     * Aggregate distribution for backward compatibility.
     */
    public Map<Algotype, Integer> getCellTypeDistribution() {
        Map<Algotype, Integer> dist = new HashMap<>();
        for (Object[] t : types) {
            int label = (Integer) t[1];
            Algotype type = Algotype.values()[label];
            dist.merge(type, 1, Integer::sum);
        }
        return Collections.unmodifiableMap(dist);
    }

    public boolean hasCellTypeDistribution() {
        return true;
    }
}
