package com.emergent.doom.swap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread-safe collector for swap proposals during parallel cell evaluation.
 *
 * <p>Multiple cell threads can safely submit proposals concurrently using
 * {@link #propose(SwapProposal)}. The main thread then collects and resolves
 * conflicts using {@link #drainAndSort()}.</p>
 *
 * <p><strong>Usage Pattern:</strong></p>
 * <pre>{@code
 * // Cell threads (parallel):
 * collector.propose(new SwapProposal(i, j));
 *
 * // Main thread (after barrier):
 * List<SwapProposal> sorted = collector.drainAndSort();
 * // ... resolve conflicts and execute swaps ...
 * collector.clear();
 * }</pre>
 *
 * <p>Thread-safe: Uses {@link ConcurrentLinkedQueue} for lock-free insertion.</p>
 */
public class ConcurrentSwapCollector {

    private final ConcurrentLinkedQueue<SwapProposal> proposals;

    public ConcurrentSwapCollector() {
        this.proposals = new ConcurrentLinkedQueue<>();
    }

    /**
     * Submit a swap proposal. Thread-safe, can be called from multiple cell threads.
     *
     * @param proposal the swap proposal to submit
     */
    public void propose(SwapProposal proposal) {
        if (proposal != null) {
            proposals.add(proposal);
        }
    }

    /**
     * Submit a swap proposal using indices. Thread-safe.
     *
     * @param initiatorIndex position of initiating cell
     * @param targetIndex position of target cell
     * @param priority resolution priority (lower = higher precedence)
     */
    public void propose(int initiatorIndex, int targetIndex, int priority) {
        proposals.add(new SwapProposal(initiatorIndex, targetIndex, priority));
    }

    /**
     * Submit a swap proposal using indices with default priority.
     * Priority defaults to initiator index (leftmost first).
     *
     * @param initiatorIndex position of initiating cell
     * @param targetIndex position of target cell
     */
    public void propose(int initiatorIndex, int targetIndex) {
        proposals.add(new SwapProposal(initiatorIndex, targetIndex));
    }

    /**
     * Drain all proposals and return them sorted by priority.
     *
     * <p>Should be called by main thread after all cell threads have
     * completed their evaluation phase (i.e., after barrier sync).</p>
     *
     * @return list of proposals sorted by priority (lowest first)
     */
    public List<SwapProposal> drainAndSort() {
        List<SwapProposal> result = new ArrayList<>();
        SwapProposal p;
        while ((p = proposals.poll()) != null) {
            result.add(p);
        }
        Collections.sort(result);
        return result;
    }

    /**
     * Get the current number of pending proposals.
     *
     * <p>Note: Due to concurrent access, this is only an approximation
     * if called while cell threads are still submitting.</p>
     *
     * @return approximate number of pending proposals
     */
    public int size() {
        return proposals.size();
    }

    /**
     * Check if there are any pending proposals.
     *
     * @return true if no proposals are pending
     */
    public boolean isEmpty() {
        return proposals.isEmpty();
    }

    /**
     * Clear all pending proposals.
     *
     * <p>Should be called by main thread after swaps have been executed
     * and before the next evaluation phase begins.</p>
     */
    public void clear() {
        proposals.clear();
    }
}
