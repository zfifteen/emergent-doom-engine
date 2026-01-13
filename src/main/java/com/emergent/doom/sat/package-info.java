/**
 * SAT package documentation.
 *
 * &lt;p&gt;&lt;strong&gt;PURPOSE:&lt;/strong&gt; Implement Boolean satisfiability clustering primitive per TECH_SPEC.md v1.1.
 * Maps SAT problems to EDE cell-based architecture for emergent strategy partitioning.&lt;/p&gt;
 *
 * &lt;p&gt;&lt;strong&gt;KEY COMPONENTS:&lt;/strong&gt;&lt;/p&gt;
 * &lt;ul&gt;
 *   &lt;li&gt;SATStrategy: Algotype enum with parameters.&lt;/li&gt;
 *   &lt;li&gt;SATCell: Extends AbstractCell for assignment candidates.&lt;/li&gt;
 *   &lt;li&gt;CNFFormula: CNF representation.&lt;/li&gt;
 *   &lt;li&gt;SATExperiment: Runner integrating with CellBasedExecutionEngine.&lt;/li&gt;
 * &lt;/ul&gt;
 *
 * &lt;p&gt;&lt;strong&gt;INTEGRATION:&lt;/strong&gt; No modifications to core EDE; reuses AbstractCell, AlgotypeAggregationIndex.&lt;/p&gt;
 *
 * @see TECH_SPEC.md
 * @since 1.1
 */
package com.emergent.doom.sat;