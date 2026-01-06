/**
 * Advanced Features: Traditional Algorithm Comparison - Tests validating EDE against classical implementations.
 *
 * <p>Traditional algorithm comparison provides validation that EDE sorting produces functionally
 * equivalent results to classical implementations while revealing differences in execution dynamics
 * and emergent properties.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Functional Equivalence</b> - EDE produces identical sorted arrays to classical algorithms</li>
 *   <li><b>Algorithm Characteristics</b> - Bubble, Insertion, Selection match known properties</li>
 *   <li><b>Performance Metrics</b> - Comparison/swap counts align with expectations</li>
 *   <li><b>Ground Truth Validation</b> - Traditional implementations verify correctness</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.traditional.TraditionalSortEngineTest} - Traditional sorting algorithms</li>
 * </ul>
 *
 * <h2>Algorithm Properties</h2>
 * <p>Classical sorting algorithms tested:</p>
 * <ul>
 *   <li><b>Bubble Sort</b> - O(n²) comparisons, many swaps, stable</li>
 *   <li><b>Insertion Sort</b> - O(n²) comparisons, fewer swaps on nearly-sorted data</li>
 *   <li><b>Selection Sort</b> - O(n²) comparisons, minimal swaps (at most n-1)</li>
 * </ul>
 *
 * <h2>Prerequisites</h2>
 * <p>Required: {@link com.emergent.doom.cell} - Cell interface basics</p>
 * <p>Required: {@link com.emergent.doom.swap} - Swap mechanics understanding</p>
 * <p>Helpful: {@link com.emergent.doom.metrics} - Performance measurement concepts</p>
 *
 * <h2>Next Steps</h2>
 * <p>After understanding traditional comparisons, explore {@link com.emergent.doom.experiment}
 * for experimental validation and statistical analysis of emergent behavior.</p>
 *
 * @see com.emergent.doom.traditional.TraditionalSortEngine
 * @see com.emergent.doom.traditional.TraditionalSortMetrics
 */
package com.emergent.doom.traditional;
