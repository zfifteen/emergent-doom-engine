/**
 * Foundations: Cell Interface - Tests for the lightweight Cell interface.
 *
 * <p>The Cell interface is the foundation of the Emergent Doom Engine, defining the minimal
 * contract for domain-agnostic sorting. Tests in this package validate that cells remain
 * pure {@code Comparable<T>} data carriers with zero engine-specific state.</p>
 *
 * <h2>Key Concepts Tested</h2>
 * <ul>
 *   <li><b>Minimal Interface Contract</b> - Cells extend only {@code Comparable<T>}</li>
 *   <li><b>Pure Data Carriers</b> - No engine-specific metadata or state</li>
 *   <li><b>Domain Agnostic Design</b> - Framework provides no domain assumptions</li>
 *   <li><b>Lightweight Implementations</b> - GenericCell, sorting-specific cells</li>
 * </ul>
 *
 * <h2>Test Classes</h2>
 * <ul>
 *   <li>{@link com.emergent.doom.cell.CellInterfaceTest} - Interface contract verification</li>
 *   <li>{@link com.emergent.doom.cell.GenericCellTest} - Basic cell implementation</li>
 *   <li>{@link com.emergent.doom.cell.AbstractCellContractTest} - Cell behavior contracts</li>
 *   <li>{@link com.emergent.doom.cell.AbstractSortingCellTest} - Sorting-specific behaviors</li>
 *   <li>{@link com.emergent.doom.cell.BubbleSortingCellTest} - Bubble sort algotype</li>
 *   <li>{@link com.emergent.doom.cell.InsertionSortingCellTest} - Insertion sort algotype</li>
 *   <li>{@link com.emergent.doom.cell.SelectionSortingCellTest} - Selection sort algotype</li>
 * </ul>
 *
 * <h2>Prerequisites</h2>
 * <p>None - this is the starting point for understanding the EDE framework.</p>
 *
 * <h2>Next Steps</h2>
 * <p>After mastering cell fundamentals, proceed to {@link com.emergent.doom.swap} to learn
 * about swap mechanics and frozen cell constraints.</p>
 *
 * @see com.emergent.doom.cell.Cell
 */
package com.emergent.doom.cell;
