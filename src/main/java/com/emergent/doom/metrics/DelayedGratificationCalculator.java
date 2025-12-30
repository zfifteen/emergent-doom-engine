package com.emergent.doom.metrics;

import java.util.List;

/**
 * Calculates Delayed Gratification (DG) from a trajectory of sortedness values.
 *
 * <p>From Levin et al. (2024), Section 4.2, p.8:
 * "Delayed Gratification is used to evaluate the ability of each algorithm
 * to undertake actions that temporarily increase Monotonicity Error in order
 * to achieve gains later on. Delayed Gratification is defined as the improvement
 * in Sortedness made by a temporarily error increasing action."</p>
 *
 * <p>Formula:
 * <pre>
 * DG = ΔS_increasing / ΔS_decreasing
 *
 * where:
 *   ΔS_decreasing = Total sortedness lost during consecutive drops
 *   ΔS_increasing = Total sortedness gained in subsequent recovery
 * </pre>
 * </p>
 *
 * <p>The total DG for a run is the sum of all individual DG events.</p>
 *
 * <p>Expected values from paper (Table 2):
 * <ul>
 *   <li>Bubble sort: 0.24 (0 frozen) → 0.37 (3 frozen)</li>
 *   <li>Insertion sort: 1.10 (0 frozen) → 1.19 (3 frozen)</li>
 *   <li>Selection sort: ~2.8 (no clear trend)</li>
 * </ul>
 * </p>
 */
public class DelayedGratificationCalculator {

    /**
     * Calculate the total Delayed Gratification from a sortedness trajectory.
     *
     * <p>Identifies all DG events (consecutive decreases followed by increases)
     * and sums up (increase/decrease) ratios for each event.</p>
     *
     * @param sortednessHistory list of sortedness values at each step (0.0 to 100.0)
     * @return total DG value (sum of all DG events), or 0.0 if no DG events
     */
    public double calculate(List<Double> sortednessHistory) {
        if (sortednessHistory == null || sortednessHistory.size() < 3) {
            // Need at least 3 points for a dip-and-recovery pattern
            return 0.0;
        }

        double totalDG = 0.0;
        int i = 0;

        while (i < sortednessHistory.size() - 1) {
            // Look for start of a decrease (peak before drop)
            if (sortednessHistory.get(i + 1) < sortednessHistory.get(i)) {
                double peakValue = sortednessHistory.get(i);
                int dropEndIndex = i + 1;

                // Find bottom of the consecutive drop (allow plateaus as part of the drop)
                double troughValue = sortednessHistory.get(dropEndIndex);
                while (dropEndIndex < sortednessHistory.size() - 1 &&
                       sortednessHistory.get(dropEndIndex + 1) <= sortednessHistory.get(dropEndIndex)) {
                    dropEndIndex++;
                    double currentValue = sortednessHistory.get(dropEndIndex);
                    if (currentValue < troughValue) {
                        troughValue = currentValue;
                    }
                }

                double deltaDecreasing = peakValue - troughValue;

                // Now track the subsequent increase (recovery)
                int recoveryEndIndex = dropEndIndex;

                // Find peak of the consecutive increase (allow plateaus as part of the recovery)
                double recoveryValue = sortednessHistory.get(recoveryEndIndex);
                while (recoveryEndIndex < sortednessHistory.size() - 1 &&
                       sortednessHistory.get(recoveryEndIndex + 1) >= sortednessHistory.get(recoveryEndIndex)) {
                    recoveryEndIndex++;
                    double currentValue = sortednessHistory.get(recoveryEndIndex);
                    if (currentValue > recoveryValue) {
                        recoveryValue = currentValue;
                    }
                }

                double deltaIncreasing = recoveryValue - troughValue;

                // Calculate DG for this event if there was actual decrease and recovery
                if (deltaDecreasing > 0 && deltaIncreasing > 0) {
                    double dgEvent = deltaIncreasing / deltaDecreasing;
                    totalDG += dgEvent;
                }

                // Move past this DG event
                i = recoveryEndIndex;
            } else {
                i++;
            }
        }

        return totalDG;
    }

    /**
     * Count the number of DG events in a trajectory.
     *
     * <p>A DG event is a consecutive decrease followed by a consecutive increase.</p>
     *
     * @param sortednessHistory list of sortedness values at each step
     * @return number of DG events detected
     */
    public int countDGEvents(List<Double> sortednessHistory) {
        if (sortednessHistory == null || sortednessHistory.size() < 3) {
            return 0;
        }

        int eventCount = 0;
        int i = 0;

        while (i < sortednessHistory.size() - 1) {
            if (sortednessHistory.get(i + 1) < sortednessHistory.get(i)) {
                // Found start of a drop
                int dropEndIndex = i + 1;

                // Allow plateaus as part of the drop
                while (dropEndIndex < sortednessHistory.size() - 1 &&
                       sortednessHistory.get(dropEndIndex + 1) <= sortednessHistory.get(dropEndIndex)) {
                    dropEndIndex++;
                }

                // Check if there's a recovery (strict increase after the drop/plateau)
                if (dropEndIndex < sortednessHistory.size() - 1 &&
                    sortednessHistory.get(dropEndIndex + 1) > sortednessHistory.get(dropEndIndex)) {
                    eventCount++;
                }

                // Skip to end of recovery (allow plateaus as part of recovery)
                int recoveryEndIndex = dropEndIndex;
                while (recoveryEndIndex < sortednessHistory.size() - 1 &&
                       sortednessHistory.get(recoveryEndIndex + 1) >= sortednessHistory.get(recoveryEndIndex)) {
                    recoveryEndIndex++;
                }
                i = recoveryEndIndex;
            } else {
                i++;
            }
        }

        return eventCount;
    }

    /**
     * Calculate the average DG per event.
     *
     * @param sortednessHistory list of sortedness values at each step
     * @return average DG per event, or 0.0 if no events
     */
    public double calculateAveragePerEvent(List<Double> sortednessHistory) {
        int eventCount = countDGEvents(sortednessHistory);
        if (eventCount == 0) {
            return 0.0;
        }
        return calculate(sortednessHistory) / eventCount;
    }
}
