package com.emergent.doom.cell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

/**
 * Test suite for GenericCell - lightweight cell implementation.
 *
 * PURPOSE: This simple tale showcases GenericCell as the quintessential pure data carrier: wrapping values for comparison
 * without engine baggage, ensuring seamless integration into any sorting narrative. Tests confirm wrapping, ordering,
 * equality, and metadata absence, embodying domain-agnostic simplicity.
 *
 * ARCHITECTURE: Validates zero-state design post-refactoring: value-only focus for Comparable compliance.
 */
class GenericCellTest {

    /**
     * PURPOSE: Begin with creation: wrap domain integers into cells, bridging raw data to emergent sorting.
     *
     * INPUTS: Integer value (e.g., 42)
     * EXPECTED OUTPUT: GenericCell instance wrapping the value
     * TEST DATA: value=42
     * REPRODUCTION: new GenericCell(42); assert getValue() == 42
     */
    @Test
    @DisplayName("Constructor creates cell with value")
    void testConstructor() {
        GenericCell cell = new GenericCell(42);
        assertEquals(42, cell.getValue());
    }

    /**
     * PURPOSE: Extract wrapped values effortlessly, enabling inspection in logs or metrics without deep dives.
     *
     * INPUTS: GenericCell with value 42
     * EXPECTED OUTPUT: getValue() returns 42
     * TEST DATA: cell = new GenericCell(42)
     * REPRODUCTION: cell.getValue(); assert == 42
     */
    @Test
    @DisplayName("getValue returns wrapped value")
    void testGetValue() {
        GenericCell cell = new GenericCell(42);
        assertEquals(42, cell.getValue());
    }

    /**
     * PURPOSE: Enable ordering: smaller values precede larger, fueling the engine's natural flow toward sorted states.
     *
     * INPUTS: Two GenericCells with different values (42 and 100)
     * EXPECTED OUTPUT: compareTo() returns negative when this < other
     * TEST DATA: cell1 = new GenericCell(42), cell2 = new GenericCell(100)
     * REPRODUCTION: cell1.compareTo(cell2); assert < 0
     */
    @Test
    @DisplayName("compareTo returns negative when this < other")
    void testCompareToLessThan() {
        GenericCell cell1 = new GenericCell(42);
        GenericCell cell2 = new GenericCell(100);
        assertTrue(cell1.compareTo(cell2) < 0);
    }

    /**
     * PURPOSE: Handle equals gracefully: duplicates compare zero, preserving multiplicity in sorted outcomes.
     *
     * INPUTS: Two GenericCells with same value (42)
     * EXPECTED OUTPUT: compareTo() returns 0
     * TEST DATA: cell1 = new GenericCell(42), cell2 = new GenericCell(42)
     * REPRODUCTION: cell1.compareTo(cell2); assert == 0
     */
    @Test
    @DisplayName("compareTo returns zero when values equal")
    void testCompareToEqual() {
        GenericCell cell1 = new GenericCell(42);
        GenericCell cell2 = new GenericCell(42);
        assertEquals(0, cell1.compareTo(cell2));
    }

    /**
     * PURPOSE: Support greater-than: larger values follow, enabling reverse sorts or priority queues.
     *
     * INPUTS: Two GenericCells where first > second (100 vs 42)
     * EXPECTED OUTPUT: compareTo() returns positive
     * TEST DATA: cell1 = new GenericCell(100), cell2 = new GenericCell(42)
     * REPRODUCTION: cell1.compareTo(cell2); assert > 0
     */
    @Test
    @DisplayName("compareTo returns positive when this > other")
    void testCompareToGreaterThan() {
        GenericCell cell1 = new GenericCell(100);
        GenericCell cell2 = new GenericCell(42);
        assertTrue(cell1.compareTo(cell2) > 0);
    }

    /**
     * PURPOSE: Affirm equality for identical values, allowing sets/maps to treat duplicates as one where intended.
     *
     * INPUTS: Two GenericCells with value 42
     * EXPECTED OUTPUT: equals() returns true
     * TEST DATA: cell1 = new GenericCell(42), cell2 = new GenericCell(42)
     * REPRODUCTION: cell1.equals(cell2); assert true (symmetric)
     */
    @Test
    @DisplayName("equals returns true for same value")
    void testEqualsTrue() {
        GenericCell cell1 = new GenericCell(42);
        GenericCell cell2 = new GenericCell(42);
        assertTrue(cell1.equals(cell2));
        assertTrue(cell2.equals(cell1)); // symmetric
    }

    /**
     * PURPOSE: Distinguish differents: unequal values separate, vital for unique tracking in collections.
     *
     * INPUTS: Two GenericCells with different values (42 and 100)
     * EXPECTED OUTPUT: equals() returns false
     * TEST DATA: cell1 = new GenericCell(42), cell2 = new GenericCell(100)
     * REPRODUCTION: cell1.equals(cell2); assert false (symmetric)
     */
    @Test
    @DisplayName("equals returns false for different values")
    void testEqualsFalse() {
        GenericCell cell1 = new GenericCell(42);
        GenericCell cell2 = new GenericCell(100);
        assertFalse(cell1.equals(cell2));
        assertFalse(cell2.equals(cell1)); // symmetric
    }

    /**
     * PURPOSE: Align hash with equality: same values hash equally, different unequally, for efficient hashing.
     *
     * INPUTS: Two equal (42), one unequal (100)
     * EXPECTED OUTPUT: hashCode() matches for equals, differs otherwise
     * TEST DATA: cell1/cell2=42, cell3=100
     * REPRODUCTION: Compare hashes; assert consistency.
     */
    @Test
    @DisplayName("hashCode is consistent with equals")
    void testHashCodeConsistency() {
        GenericCell cell1 = new GenericCell(42);
        GenericCell cell2 = new GenericCell(42);
        GenericCell cell3 = new GenericCell(100);

        assertEquals(cell1.hashCode(), cell2.hashCode()); // equal objects have equal hash codes
        assertNotEquals(cell1.hashCode(), cell3.hashCode()); // unequal objects have unequal hash codes
    }

    /**
     * PURPOSE: Reveal values via toString for quick debugging, surfacing data in traces without inspection.
     *
     * INPUTS: GenericCell with value 42
     * EXPECTED OUTPUT: toString() returns "42"
     * TEST DATA: cell = new GenericCell(42)
     * REPRODUCTION: cell.toString(); assert "42"
     */
    @Test
    @DisplayName("toString returns string representation of value")
    void testToString() {
        GenericCell cell = new GenericCell(42);
        assertEquals("42", cell.toString());
    }

    /**
     * PURPOSE: Affirm purity: no hidden metadata fields, confirming GenericCell as true data carrier.
     *
     * INPUTS: GenericCell instance
     * EXPECTED OUTPUT: Only 'value' field via reflection
     * TEST DATA: cell = new GenericCell(42)
     * REPRODUCTION: getDeclaredFields(); assert length=1, name="value", type=int
     */
    @Test
    @DisplayName("Cell has no metadata fields (lightweight verification)")
    void testNoMetadataFields() {
        Field[] fields = GenericCell.class.getDeclaredFields();

        assertEquals(1, fields.length, "Should have exactly one field: value");
        assertEquals("value", fields[0].getName(), "Field name should be value");
        assertEquals(int.class, fields[0].getType(), "Field type should be int");
    }
}
