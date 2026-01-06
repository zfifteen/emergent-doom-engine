/**
 * Chapter 3: Execution Engines - Tests for emergent computation orchestration.
 *
 * <p>Execution engines orchestrate emergent computation by coordinating cell swaps, managing
 * metadata, and detecting convergence. Tests in this package validate that engines correctly
 * execute sorting algorithms through bottom-up cell interactions without global control.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Doom (Inevitability)</b> - Convergence toward goal state is inevitable</li>
 *   <li><b>Decentralized Orchestration</b> - No centralized planning, order emerges</li>
 *   <li><b>External Metadata Management</b> - Metadata separate from cells</li>
 *   <li><b>Convergence Detection</b> - Stable-step heuristics detect completion</li>
 *   <li><b>Parallel Execution</b> - Thread-safe multi-threaded sorting</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.execution.ExecutionEngineTest} - Core execution logic</li>
 *   <li>{@link com.emergent.doom.execution.ParallelExecutionTest} - Multi-threaded execution</li>
 * </ul>
 *
 * <h2>Execution Lifecycle</h2>
 * <ol>
 *   <li><b>Initialize</b> - Create cells, metadata providers, swap engine</li>
 *   <li><b>Step</b> - Execute one round of pairwise comparisons and swaps</li>
 *   <li><b>Converge</b> - Repeat steps until stable (no swaps for N consecutive steps)</li>
 *   <li><b>Record</b> - Capture trajectory via probe for analysis</li>
 * </ol>
 *
 * <h2>Prerequisites</h2>
 * <p>Required: {@link com.emergent.doom.cell} - Cell interface</p>
 * <p>Required: {@link com.emergent.doom.swap} - Swap mechanics</p>
 * <p>Required: {@link com.emergent.doom.probe} - Trajectory recording</p>
 * <p>Required: {@link com.emergent.doom.metrics} - Progress measurement</p>
 *
 * <h2>Next Steps</h2>
 * <p>After mastering execution engines, proceed to {@link com.emergent.doom.experiment} to learn
 * about experimental validation and statistical analysis.</p>
 *
 * @see com.emergent.doom.execution.CellBasedExecutionEngine
 * @see com.emergent.doom.execution.ConvergenceDetector
 */
package com.emergent.doom.execution;
