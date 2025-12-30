package com.emergent.doom.swap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages frozen cell states in the Emergent Doom Engine.
 * 
 * <p>Cells can be frozen in three states:
 * <ul>
 *   <li><strong>NONE</strong> - Cell is fully mobile and can swap freely</li>
 *   <li><strong>MOVABLE</strong> - Cell can move to new positions but cannot be displaced by others</li>
 *   <li><strong>IMMOVABLE</strong> - Cell is completely frozen and cannot participate in swaps</li>
 * </ul>
 * </p>
 * 
 * <p>This mechanism allows for progressive crystallization of solutions where
 * good cells become progressively more stable.</p>
 */
public class FrozenCellStatus {
    
    /**
     * Enumeration of possible frozen states for cells.
     * 
     * <p><strong>IMPLEMENTED:</strong> This enum defines the three frozen states
     * that cells can have in the EDE system. The ordering represents increasing
     * levels of constraint on cell movement.</p>
     */
    public enum FrozenType {
        /** Cell is completely mobile and can participate in all swaps */
        NONE,
        
        /** Cell can move to new positions but cannot be displaced by other cells */
        MOVABLE,
        
        /** Cell is completely frozen and cannot participate in any swaps */
        IMMOVABLE
    }
    
    private final Map<Integer, FrozenType> frozenStates;
    
    /**
     * IMPLEMENTED: Initialize an empty frozen cell status tracker
     */
    public FrozenCellStatus() {
        this.frozenStates = new HashMap<>();
    }
    
    /**
     * IMPLEMENTED: Set the frozen status for a specific cell position
     */
    public void setFrozen(int position, FrozenType type) {
        if (type == FrozenType.NONE) {
            frozenStates.remove(position);
        } else {
            frozenStates.put(position, type);
        }
    }
    
    /**
     * IMPLEMENTED: Get the frozen status of a cell at a specific position
     */
    public FrozenType getFrozen(int position) {
        return frozenStates.getOrDefault(position, FrozenType.NONE);
    }
    
    /**
     * IMPLEMENTED: Check if a cell is completely immovable
     */
    public boolean isImmovable(int position) {
        return getFrozen(position) == FrozenType.IMMOVABLE;
    }
    
    /**
     * IMPLEMENTED: Check if a cell can move to new positions
     */
    public boolean canMove(int position) {
        FrozenType type = getFrozen(position);
        return type == FrozenType.NONE || type == FrozenType.MOVABLE;
    }
    
    /**
     * IMPLEMENTED: Check if a cell can be displaced by another cell
     */
    public boolean canBeDisplaced(int position) {
        return getFrozen(position) == FrozenType.NONE;
    }
    
    /**
     * IMPLEMENTED: Freeze all cells at specified positions with given type
     */
    public void freezeAll(Set<Integer> positions, FrozenType type) {
        for (int position : positions) {
            setFrozen(position, type);
        }
    }
    
    /**
     * IMPLEMENTED: Clear all frozen states (reset to all NONE)
     */
    public void clearAll() {
        frozenStates.clear();
    }
    
    /**
     * IMPLEMENTED: Get all positions that have non-NONE frozen status
     */
    public Set<Integer> getFrozenPositions() {
        return frozenStates.keySet();
    }
    
    /**
     * IMPLEMENTED: Count how many cells have a specific frozen type
     */
    public int countByType(FrozenType type) {
        return (int) frozenStates.values().stream()
                .filter(t -> t == type)
                .count();
    }
}
