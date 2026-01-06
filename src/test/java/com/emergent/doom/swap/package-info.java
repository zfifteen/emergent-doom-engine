/**
 * Chapter 2: Swap Mechanics - Tests for conditional swap logic and frozen cell constraints.
 *
 * <p>Swaps are the fundamental local interaction mechanism in the EDE. Tests in this package
 * validate that the {@code SwapEngine} correctly implements conditional swapping, frozen cell
 * states, and unreliable substrate simulation.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Local Agent Interactions</b> - Pairwise swaps with no global orchestration</li>
 *   <li><b>Unreliable Substrate Simulation</b> - Frozen cells model damaged agents</li>
 *   <li><b>Frozen Cell States</b> - NONE (active), MOVABLE (passive), IMMOVABLE (frozen)</li>
 *   <li><b>Swap Count Tracking</b> - Metrics for convergence detection</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.swap.SwapEngineTest} - Swap logic and frozen constraints</li>
 *   <li>{@link com.emergent.doom.swap.IntCellTest} - Integer cell test implementation</li>
 * </ul>
 *
 * <h2>Frozen State Semantics</h2>
 * <p>The swap engine enforces three frozen states:</p>
 * <ul>
 *   <li><b>NONE</b> - Fully active, can initiate and participate in swaps</li>
 *   <li><b>MOVABLE</b> - Passive, can be moved by others but cannot initiate</li>
 *   <li><b>IMMOVABLE</b> - Completely frozen, cannot move or be moved</li>
 * </ul>
 *
 * <h2>Prerequisites</h2>
 * <p>Required: {@link com.emergent.doom.cell} - Understanding of Cell interface and compareTo()</p>
 *
 * <h2>Next Steps</h2>
 * <p>After mastering swap mechanics, proceed to {@link com.emergent.doom.probe} to learn
 * about execution trajectory recording and observability.</p>
 *
 * @see com.emergent.doom.swap.SwapEngine
 * @see com.emergent.doom.swap.FrozenCellStatus
 */
package com.emergent.doom.swap;
