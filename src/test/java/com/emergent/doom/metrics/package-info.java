/**
 * Chapter 2: Metrics - Tests for problem-space navigation measures.
 *
 * <p>Metrics quantify emergent computation by measuring problem-space traversal. Tests in this
 * package validate correct computation of monotonicity, sortedness, delayed gratification, and
 * Spearman distance, matching reference implementations from the Levin et al. (2024) research.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Problem-Space Navigation</b> - Traversal from disorder to order</li>
 *   <li><b>Emergent Competencies</b> - Goal-directed behavior and adaptation</li>
 *   <li><b>State Metrics</b> - Monotonicity, Sortedness (single snapshot)</li>
 *   <li><b>Trajectory Metrics</b> - Delayed Gratification (sequence analysis)</li>
 *   <li><b>Comparative Metrics</b> - Spearman Distance (rank correlation)</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.metrics.MonotonicityTest} - Disorder measurement</li>
 *   <li>{@link com.emergent.doom.metrics.SortednessTest} - Progress toward goal</li>
 *   <li>{@link com.emergent.doom.metrics.DelayedGratificationCalculatorTest} - Adaptive setbacks</li>
 *   <li>{@link com.emergent.doom.metrics.SpearmanDistanceTest} - Rank correlation</li>
 * </ul>
 *
 * <h2>Metric Definitions</h2>
 * <ul>
 *   <li><b>Monotonicity</b> - Percentage of inversions (out-of-order pairs) remaining</li>
 *   <li><b>Sortedness</b> - Percentage of elements in correct final positions</li>
 *   <li><b>Delayed Gratification</b> - Temporary disorder increases for long-term progress</li>
 *   <li><b>Spearman Distance</b> - Statistical correlation with ideal sorted state</li>
 * </ul>
 *
 * <h2>Prerequisites</h2>
 * <p>Required: {@link com.emergent.doom.cell} - Cell interface</p>
 * <p>Required: {@link com.emergent.doom.probe} - Trajectory snapshots</p>
 *
 * <h2>Next Steps</h2>
 * <p>After mastering metrics, proceed to {@link com.emergent.doom.execution} to learn
 * about execution engines and convergence detection.</p>
 *
 * @see com.emergent.doom.metrics.Monotonicity
 * @see com.emergent.doom.metrics.DelayedGratificationCalculator
 */
package com.emergent.doom.metrics;
