/**
 * Integer factorization domain for Emergent Doom Engine.
 *
 * <p><strong>PURPOSE:</strong> Demonstrate EDE framework on a non-sorting problem,
 * validating that emergent clustering can partition problem spaces beyond sorting.</p>
 *
 * <p><strong>DOMAIN MAPPING:</strong></p>
 * <table>
 *   <tr><th>EDE Concept</th><th>Factorization Mapping</th></tr>
 *   <tr><td>Cell</td><td>Factor candidate (integer)</td></tr>
 *   <tr><td>Cell.value</td><td>Candidate integer</td></tr>
 *   <tr><td>Cell.algotype</td><td>Factor-finding strategy (SMALL_PRIMES, FERMAT_NEAR_SQRT, RANDOM_SAMPLE)</td></tr>
 *   <tr><td>"Sorted" state</td><td>Candidates ordered by factor-fitness score (descending)</td></tr>
 *   <tr><td>Aggregation</td><td>Clustering of strategies around similar candidates</td></tr>
 *   <tr><td>Convergence</td><td>True factors (remainder=0) migrate to array front</td></tr>
 * </table>
 *
 * <p><strong>HYPOTHESIS:</strong> When cells encode factor candidates and algotypes
 * encode factor-finding strategies, emergent clustering will group candidates by
 * their "closeness" to true factors, providing a partitioning that narrows the
 * search space.</p>
 *
 * <p><strong>KEY COMPONENTS:</strong></p>
 * <ul>
 *   <li>{@link com.emergent.doom.factorization.FactorStrategy} - Algotype enum for factor-finding strategies</li>
 *   <li>{@link com.emergent.doom.factorization.FactorCell} - Cell implementation with fitness-based comparison</li>
 *   <li>{@link com.emergent.doom.factorization.CandidateGenerator} - Utility for generating strategy-specific candidates</li>
 * </ul>
 *
 * <p><strong>EXPERIMENT DESIGN:</strong></p>
 * <ul>
 *   <li>Target: N=143 (11×13 semiprime)</li>
 *   <li>Array size: 50 candidates</li>
 *   <li>Strategy distribution: 33% SMALL_PRIMES, 33% FERMAT_NEAR_SQRT, 34% RANDOM_SAMPLE</li>
 *   <li>Success criteria: Peak aggregation > 60% (baseline ~50-61%)</li>
 *   <li>Success criteria: True factors co-locate in clusters > 70% of runs</li>
 * </ul>
 *
 * <p><strong>REFERENCE:</strong> See Space file FIRST_NON_SORTING_EXPERIMENT.md for
 * complete experimental protocol, controls, and analysis plan.</p>
 *
 * @since 0.3.0-alpha
 */
package com.emergent.doom.factorization;
