package com.emergent.doom.factorization;

/**
 * PURPOSE: Define the contract for factorization algotypes (strategies).
 * 
 * SEMANTICS: A FactorizationAlgotype is an instance of one factorization strategy
 * (e.g., Trial Division, Fermat, Pollard) that operates on the same FactorizationCell array.
 * Each algotype executes one step per call, enabling chimeric (multi-strategy) factorization
 * where all three strategies compete and cooperate.
 * 
 * EDE ANALOGUE: Just as Levin et al. (2024) define sorting algotypes (Bubble, Insertion, Selection),
 * we define factorization algotypes. Each navigates the divisor space by comparing remainders,
 * swapping divisors, and locking factors when discovered.
 * 
 * USER STORY:
 * As a factorization researcher, I want each algotype to follow a consistent contract
 * so that I can swap implementations, measure individual performance, and combine them
 * in chimeric configurations to test cooperation and error tolerance.
 * 
 * INPUTS: FactorizationCell array, semiprime N, current step count
 * OUTPUTS: Cell array with one operation executed (compare, swap, or lock)
 * LOGIC: Each algotype defines its own strategy for navigating remainder space.
 */
public interface FactorizationAlgotype {
    
    /**
     * PURPOSE: Execute one step of the factorization strategy.
     * 
     * INPUTS:
     *   - divisors: array of FactorizationCell objects representing candidates d ∈ [2, √N]
     *   - semiprimeN: the semiprime being factored (e.g., 143)
     *   - stepCount: iteration number (used for metrics, history tracking)
     * 
     * OUTPUTS: divisors array modified by one operation (swap, lock, or no-op)
     * 
     * LOGIC:
     *   1. Inspect current state of divisors array (remainders, locked status)
     *   2. Decide on one action: compare two cells, swap two cells, or lock a factor
     *   3. Execute action (potentially modifying cell state or order)
     *   4. Record which algotype performed the action (set claimingAlgotype)
     *   5. Return (no explicit return; divisors array mutated in-place)
     * 
     * CONTRACT:
     *   - Exactly ONE operation per step (no batching multiple swaps)
     *   - Does NOT modify cells already locked as factors (frozen cells)
     *   - DOES lock cells when remainder = 0 (factor discovered)
     *   - CAN swap unlocked, unfactored cells
     *   - MUST record which algotype claimed each cell (for analysis)
     * 
     * EMERGENT BEHAVIOR:
     *   When all three algotypes execute steps on the same array in round-robin,
     *   they compete (sometimes interfering, sometimes cooperating) to find factors.
     *   This chimeric approach tests error tolerance: if one algotype stalls,
     *   do others make progress?
     * 
     * @param divisors array of FactorizationCell candidates d ∈ [2, √N]
     * @param semiprimeN the semiprime being factored (e.g., 143)
     * @param stepCount the current iteration number
     */
    void executeStep(FactorizationCell[] divisors, long semiprimeN, long stepCount);
    
    /**
     * PURPOSE: Get a human-readable name for this algotype.
     * 
     * OUTPUTS: Name like "Trial Division", "Fermat", "Pollard"
     * LOGIC: Simple getter; used for logging and recording which algotype found factors
     * 
     * @return name of this factorization strategy
     */
    String getName();
    
    /**
     * PURPOSE: Reset internal state of the algotype (e.g., iteration counters, indices).
     * 
     * LOGIC: Called before starting a new trial. Allows algotype to prepare for fresh run.
     * Some algotypes may track position pointers, stall counters, etc. that need resetting.
     * 
     * USAGE: Useful when running multiple trials on same N; ensures clean slate per trial.
     */
    void reset();
}
