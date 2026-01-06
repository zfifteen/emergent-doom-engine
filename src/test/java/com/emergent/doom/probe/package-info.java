/**
 * Chapter 2: Probe Recording - Tests for execution trajectory observability.
 *
 * <p>Probes provide observability into emergent computation by capturing execution trajectories.
 * Tests in this package validate that probes correctly record cell state snapshots, track swap
 * counts, and maintain thread safety for parallel execution.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Execution Trajectory Recording</b> - Immutable snapshots at each step</li>
 *   <li><b>Observability Without Intrusion</b> - Recording doesn't affect computation</li>
 *   <li><b>Post-Hoc Analysis</b> - Complete history for metrics and visualization</li>
 *   <li><b>Thread Safety</b> - Concurrent recording for parallel execution</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.probe.ProbeTest} - Basic probe recording functionality</li>
 *   <li>{@link com.emergent.doom.probe.ThreadSafeProbeTest} - Concurrent execution safety</li>
 * </ul>
 *
 * <h2>Snapshot Components</h2>
 * <p>Each snapshot captures:</p>
 * <ul>
 *   <li><b>Step Number</b> - Sequential temporal identifier</li>
 *   <li><b>Cell State</b> - Immutable copy of cell array</li>
 *   <li><b>Swap Count</b> - Successful swaps up to this step</li>
 *   <li><b>Frozen Attempts</b> - Blocked swaps due to frozen cells</li>
 * </ul>
 *
 * <h2>Prerequisites</h2>
 * <p>Required: {@link com.emergent.doom.cell} - Cell interface and immutability</p>
 * <p>Required: {@link com.emergent.doom.swap} - Swap mechanics and frozen states</p>
 *
 * <h2>Next Steps</h2>
 * <p>After mastering probe recording, proceed to {@link com.emergent.doom.metrics} to learn
 * about quality measures and convergence detection.</p>
 *
 * @see com.emergent.doom.probe.Probe
 * @see com.emergent.doom.probe.ThreadSafeProbe
 */
package com.emergent.doom.probe;
