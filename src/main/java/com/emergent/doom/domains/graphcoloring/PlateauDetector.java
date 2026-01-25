package com.emergent.doom.domains.graphcoloring;

import java.util.*;

/**
 * Detects fitness plateaus in graph coloring experiments.
 * 
 * <p><strong>PURPOSE:</strong> Identify when the population has stalled on a
 * plateau where best violations haven't improved for W ticks.</p>
 * 
 * <p><strong>OPERATIONAL DEFINITION:</strong></p>
 * <ul>
 *   <li>Plateau window: W ticks (default: 75)</li>
 *   <li>Tick t is "on plateau" if: bestViol(t) == bestViol(t-W)</li>
 *   <li>Optional flatness condition: medianViol(t..t-W) unchanged (±1)</li>
 * </ul>
 */
public class PlateauDetector {
    
    private final int windowSize;
    private final boolean useFlatnessCondition;
    
    // History tracking
    private final Deque<Integer> bestViolationsHistory;
    private final Deque<Integer> medianViolationsHistory;
    
    /**
     * Create plateau detector with specified window size.
     * 
     * @param windowSize number of ticks for plateau window (default: 75)
     * @param useFlatnessCondition if true, also check median flatness
     */
    public PlateauDetector(int windowSize, boolean useFlatnessCondition) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be positive");
        }
        
        this.windowSize = windowSize;
        this.useFlatnessCondition = useFlatnessCondition;
        this.bestViolationsHistory = new ArrayDeque<>();
        this.medianViolationsHistory = new ArrayDeque<>();
    }
    
    /**
     * Create plateau detector with default window (75 ticks, with flatness).
     */
    public PlateauDetector() {
        this(75, true);
    }
    
    /**
     * Record violations for current tick.
     * 
     * @param bestViolations best (minimum) violations in population
     * @param medianViolations median violations in population
     */
    public void recordTick(int bestViolations, int medianViolations) {
        bestViolationsHistory.addLast(bestViolations);
        medianViolationsHistory.addLast(medianViolations);
        
        // Keep only window + 1 history (need W+1 to check if t-W exists)
        while (bestViolationsHistory.size() > windowSize + 1) {
            bestViolationsHistory.removeFirst();
            medianViolationsHistory.removeFirst();
        }
    }
    
    /**
     * Check if current tick is on a plateau.
     * 
     * <p>Requires at least W+1 ticks of history.</p>
     * 
     * @return true if on plateau, false otherwise
     */
    public boolean isOnPlateau() {
        if (bestViolationsHistory.size() < windowSize + 1) {
            return false;  // Not enough history
        }
        
        Integer current = getElementAtIndex(bestViolationsHistory, bestViolationsHistory.size() - 1);
        Integer windowAgo = getElementAtIndex(bestViolationsHistory, 0);
        
        // Check if best hasn't improved
        boolean bestStalled = current.equals(windowAgo);
        
        if (!useFlatnessCondition) {
            return bestStalled;
        }
        
        // Also check median flatness (±1)
        Integer currentMedian = getElementAtIndex(medianViolationsHistory, medianViolationsHistory.size() - 1);
        Integer windowAgoMedian = getElementAtIndex(medianViolationsHistory, 0);
        
        boolean medianFlat = Math.abs(currentMedian - windowAgoMedian) <= 1;
        
        return bestStalled && medianFlat;
    }
    
    /**
     * Get number of consecutive plateau ticks.
     * 
     * <p>Counts backwards from current tick while plateau condition holds.</p>
     * 
     * @return number of consecutive plateau ticks (0 if not on plateau)
     */
    public int getPlateauDuration() {
        if (!isOnPlateau()) {
            return 0;
        }
        
        // Count backwards while plateau condition holds
        int duration = 0;
        List<Integer> bestList = new ArrayList<>(bestViolationsHistory);
        
        if (bestList.size() < windowSize + 1) {
            return 0;
        }
        
        int currentBest = bestList.get(bestList.size() - 1);
        
        for (int i = bestList.size() - 1; i >= windowSize; i--) {
            int windowAgoBest = bestList.get(i - windowSize);
            if (currentBest == windowAgoBest) {
                duration++;
            } else {
                break;
            }
        }
        
        return duration;
    }
    
    /**
     * Reset detector state.
     */
    public void reset() {
        bestViolationsHistory.clear();
        medianViolationsHistory.clear();
    }
    
    /**
     * Get current best violations.
     * 
     * @return current best, or -1 if no history
     */
    public int getCurrentBest() {
        if (bestViolationsHistory.isEmpty()) {
            return -1;
        }
        return getElementAtIndex(bestViolationsHistory, bestViolationsHistory.size() - 1);
    }
    
    /**
     * Helper to get element at index from deque.
     */
    private <T> T getElementAtIndex(Deque<T> deque, int index) {
        int i = 0;
        for (T element : deque) {
            if (i == index) {
                return element;
            }
            i++;
        }
        throw new IndexOutOfBoundsException("Index " + index + " out of bounds for deque of size " + deque.size());
    }
}
