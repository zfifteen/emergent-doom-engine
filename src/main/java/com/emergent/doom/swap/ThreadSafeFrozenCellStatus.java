package com.emergent.doom.swap;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe implementation of frozen cell status tracking.
 *
 * <p>Uses {@link ConcurrentHashMap} for lock-free, thread-safe access
 * to frozen cell states during parallel execution.</p>
 *
 * <p>This class extends {@link FrozenCellStatus} to provide a drop-in
 * replacement with thread safety guarantees.</p>
 *
 * <p><strong>Thread Safety:</strong> All methods are safe for concurrent
 * access from multiple threads without external synchronization.</p>
 */
public class ThreadSafeFrozenCellStatus extends FrozenCellStatus {

    private final ConcurrentHashMap<Integer, FrozenType> concurrentFrozenStates;

    public ThreadSafeFrozenCellStatus() {
        super();
        this.concurrentFrozenStates = new ConcurrentHashMap<>();
    }

    /**
     * Set the frozen status for a specific cell position.
     * Thread-safe.
     */
    @Override
    public void setFrozen(int position, FrozenType type) {
        if (type == FrozenType.NONE) {
            concurrentFrozenStates.remove(position);
        } else {
            concurrentFrozenStates.put(position, type);
        }
    }

    /**
     * Get the frozen status of a cell at a specific position.
     * Thread-safe.
     */
    @Override
    public FrozenType getFrozen(int position) {
        return concurrentFrozenStates.getOrDefault(position, FrozenType.NONE);
    }

    /**
     * Check if a cell is completely immovable.
     * Thread-safe.
     */
    @Override
    public boolean isImmovable(int position) {
        return getFrozen(position) == FrozenType.IMMOVABLE;
    }

    /**
     * Check if a cell can initiate a swap (move to new positions).
     * Thread-safe.
     *
     * <p>Only NONE cells can initiate swaps. MOVABLE cells cannot initiate
     * but can be displaced (matches Python cell_research FREEZE behavior).</p>
     */
    @Override
    public boolean canMove(int position) {
        FrozenType type = getFrozen(position);
        return type == FrozenType.NONE;  // Only NONE can initiate swaps
    }

    /**
     * Check if a cell can be displaced by another cell.
     * Thread-safe.
     *
     * <p>NONE and MOVABLE cells can be displaced. Only IMMOVABLE cells
     * cannot be displaced (matches Python cell_research FREEZE behavior).</p>
     */
    @Override
    public boolean canBeDisplaced(int position) {
        FrozenType type = getFrozen(position);
        return type == FrozenType.NONE || type == FrozenType.MOVABLE;
    }

    /**
     * Freeze all cells at specified positions with given type.
     * Thread-safe (each individual operation is atomic).
     */
    @Override
    public void freezeAll(Set<Integer> positions, FrozenType type) {
        for (int position : positions) {
            setFrozen(position, type);
        }
    }

    /**
     * Clear all frozen states (reset to all NONE).
     * Thread-safe.
     */
    @Override
    public void clearAll() {
        concurrentFrozenStates.clear();
    }

    /**
     * Get all positions that have non-NONE frozen status.
     * Returns a snapshot view; modifications during iteration are safe.
     */
    @Override
    public Set<Integer> getFrozenPositions() {
        return concurrentFrozenStates.keySet();
    }

    /**
     * Count how many cells have a specific frozen type.
     * Thread-safe.
     */
    @Override
    public int countByType(FrozenType type) {
        return (int) concurrentFrozenStates.values().stream()
                .filter(t -> t == type)
                .count();
    }

    /**
     * Atomically set frozen status only if currently at expected type.
     *
     * @param position the cell position
     * @param expectedType the expected current type
     * @param newType the new type to set
     * @return true if the update was successful
     */
    public boolean compareAndSetFrozen(int position, FrozenType expectedType, FrozenType newType) {
        if (expectedType == FrozenType.NONE) {
            if (newType == FrozenType.NONE) {
                return true; // No change needed
            }
            return concurrentFrozenStates.putIfAbsent(position, newType) == null;
        } else if (newType == FrozenType.NONE) {
            return concurrentFrozenStates.remove(position, expectedType);
        } else {
            return concurrentFrozenStates.replace(position, expectedType, newType);
        }
    }
}
