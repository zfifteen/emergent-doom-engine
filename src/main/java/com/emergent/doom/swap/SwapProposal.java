package com.emergent.doom.swap;

import java.util.Objects;

/**
 * Immutable value class representing a proposed swap between two cells.
 *
 * <p>Thread-safe by design: all fields are final and the class is immutable.
 * Used in parallel cell evaluation to collect swap intentions before
 * centralized conflict resolution.</p>
 *
 * <p><strong>Priority:</strong> Lower priority value means higher precedence.
 * Typically the initiator's position is used as priority (leftmost first).</p>
 */
public final class SwapProposal implements Comparable<SwapProposal> {

    private final int initiatorIndex;
    private final int targetIndex;
    private final int priority;

    /**
     * Create a new swap proposal.
     *
     * @param initiatorIndex position of the cell initiating the swap
     * @param targetIndex position of the cell being swapped with
     * @param priority resolution priority (lower = higher precedence)
     */
    public SwapProposal(int initiatorIndex, int targetIndex, int priority) {
        this.initiatorIndex = initiatorIndex;
        this.targetIndex = targetIndex;
        this.priority = priority;
    }

    /**
     * Create a swap proposal using initiator position as priority.
     *
     * @param initiatorIndex position of the cell initiating the swap
     * @param targetIndex position of the cell being swapped with
     */
    public SwapProposal(int initiatorIndex, int targetIndex) {
        this(initiatorIndex, targetIndex, initiatorIndex);
    }

    public int getInitiatorIndex() {
        return initiatorIndex;
    }

    public int getTargetIndex() {
        return targetIndex;
    }

    public int getPriority() {
        return priority;
    }

    /**
     * Check if this proposal involves a given cell index.
     *
     * @param index the cell index to check
     * @return true if this proposal involves the cell at the given index
     */
    public boolean involves(int index) {
        return initiatorIndex == index || targetIndex == index;
    }

    @Override
    public int compareTo(SwapProposal other) {
        return Integer.compare(this.priority, other.priority);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwapProposal that = (SwapProposal) o;
        return initiatorIndex == that.initiatorIndex &&
               targetIndex == that.targetIndex &&
               priority == that.priority;
    }

    @Override
    public int hashCode() {
        return Objects.hash(initiatorIndex, targetIndex, priority);
    }

    @Override
    public String toString() {
        return String.format("SwapProposal{%d -> %d, priority=%d}",
                initiatorIndex, targetIndex, priority);
    }
}
