package com.emergent.doom.cell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Test suite for Cell interface contract.
 *
 * PURPOSE: Verify that Cell interface defines minimal contract for domain-agnostic sorting
 * after lightweight cell refactoring.
 *
 * ARCHITECTURE: Tests validate that Cell:
 * - Extends only Comparable<T>
 * - Does not carry engine-specific metadata (managed externally via CellMetadata)
 * - Requires only compareTo() method from implementations
 * - Supports pure data carrier pattern
 */
class CellInterfaceTest {

    /**
     * Simple test implementation of Cell interface.
     */
    private static class TestCell implements Cell<TestCell> {
        private final int value;

        public TestCell(int value) {
            this.value = value;
        }

        
        public int compareTo(TestCell other) {
            return Integer.compare(this.value, other.value);
        }
    }

    /**
     * PURPOSE: As a developer, I want Cell interface to extend only Comparable
     * so that cells remain pure data carriers without engine state.
     *
     * INPUTS: Cell interface class
     * EXPECTED OUTPUT: Only Comparable<T> in interface hierarchy
     * TEST DATA: Cell.class
     * REPRODUCTION: Reflection check on Cell interface superinterfaces
     */
    @Test
    @DisplayName("Cell interface extends only Comparable")
    void testInterfaceExtendsOnlyComparable() {
        // Verify Cell interface does not carry engine-specific metadata
        // All metadata is managed externally via CellMetadata
        Class<?>[] interfaces = Cell.class.getInterfaces();
        assertEquals(1, interfaces.length);
        assertEquals(Comparable.class, interfaces[0]);
    }

    /**
     * PURPOSE: As a developer, I want to implement Cell with just compareTo()
     * so that I can create lightweight domain-specific cells.
     *
     * INPUTS: TestCell implementation with only compareTo()
     * EXPECTED OUTPUT: TestCell compiles and works correctly
     * TEST DATA: new TestCell(42)
     * REPRODUCTION: Instantiate TestCell and verify it satisfies Cell contract
     */
    @Test
    @DisplayName("Cell implementation requires only compareTo()")
    void testMinimalImplementation() {
        // Test will verify: Can create Cell implementation with just compareTo()
        TestCell cell = new TestCell(42);
        assertEquals(42, cell.value); // Can instantiate
        assertEquals(0, cell.compareTo(new TestCell(42))); // compareTo works
    }

    /**
     * PURPOSE: As a developer, I want Cell implementations to be comparable
     * so that sorting engines can order them.
     *
     * INPUTS: Two TestCell instances (42 and 100)
     * EXPECTED OUTPUT: compareTo() works correctly
     * TEST DATA: cell1 = new TestCell(42), cell2 = new TestCell(100)
     * REPRODUCTION: System.out.println("cell1.compareTo(cell2) = " + cell1.compareTo(cell2))
     */
    @Test
    @DisplayName("Cell instances are comparable")
    void testComparableContract() {
        // Test will verify: Cell extends Comparable and compareTo() is available
        TestCell cell1 = new TestCell(42);
        TestCell cell2 = new TestCell(100);

        assertTrue(cell1 instanceof Comparable);
        assertTrue(cell1.compareTo(cell2) < 0); // 42 < 100
        assertTrue(cell2.compareTo(cell1) > 0); // 100 > 42
        assertEquals(0, cell1.compareTo(new TestCell(42))); // equal values
    }

    /**
     * PURPOSE: As a developer, I want to verify Cell has no required methods beyond Comparable
     * so that I can confirm the minimal contract.
     *
     * INPUTS: Cell interface
     * EXPECTED OUTPUT: No abstract methods beyond compareTo()
     * TEST DATA: Cell.class
     * REPRODUCTION: Reflection check for declared methods
     */
    @Test
    @DisplayName("Cell interface declares no additional abstract methods")
    void testNoAdditionalMethods() {
        // Test will verify: Cell interface has no methods beyond inherited compareTo()
        Method[] declaredMethods = Cell.class.getDeclaredMethods();

        // Cell interface declares getValue() as default method
        assertEquals(1, declaredMethods.length, "Cell interface should declare getValue() as default method");

        Method getValueMethod = declaredMethods[0];
        assertEquals("getValue", getValueMethod.getName());

        // Verify getValue is not abstract (it's default)
        assertFalse(Modifier.isAbstract(getValueMethod.getModifiers()), "getValue should not be abstract");

        // Verify no abstract methods are declared on Cell interface
        // (compareTo is inherited from Comparable, not declared here)
        boolean hasAbstractMethod = java.util.Arrays.stream(declaredMethods)
            .anyMatch(method -> Modifier.isAbstract(method.getModifiers()));
        assertFalse(hasAbstractMethod, "Cell interface should declare no abstract methods");
    }
}
