package com.emergent.doom.cell;

/**
 * Interface for cells that know their own Algotype.
 * 
 * <p>Restored to support chimeric experiments with GenericCell subclasses
 * where the algotype is stored in the cell rather than inferred from the class type.</p>
 */
public interface HasAlgotype {
    Algotype getAlgotype();
}
