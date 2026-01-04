package com.emergent.doom.execution;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.SortDirection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for CellMetadata.
 *
 * <p>These tests verify the metadata provider pattern that decouples cell domain data
 * from execution metadata. Tests are framed from the end-user perspective.</p>
 */
class CellMetadataTest {
    
    @Nested
    @DisplayName("Construction tests")
    class ConstructionTests {
        
        @Test
        @DisplayName("As a user I want to create metadata with algotype and direction so that I can configure cell behavior")
        void createMetadataWithAlgotypeAndDirection() {
            // PURPOSE: Verify CellMetadata constructor accepts algotype and sort direction
            // INPUTS: Algotype.BUBBLE, SortDirection.ASCENDING
            // OUTPUTS: CellMetadata instance with correct fields
            
            CellMetadata metadata = new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
            
            assertNotNull(metadata, "Metadata should not be null");
            assertEquals(Algotype.BUBBLE, metadata.getAlgotype(), "Algotype should be BUBBLE");
            assertEquals(SortDirection.ASCENDING, metadata.getSortDirection(), "Sort direction should be ASCENDING");
        }
        
        @Test
        @DisplayName("As a user I want metadata construction to fail with null algotype so that I catch configuration errors early")
        void rejectNullAlgotype() {
            // PURPOSE: Verify null validation for algotype parameter
            // INPUTS: null algotype, valid SortDirection
            // OUTPUTS: IllegalArgumentException
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CellMetadata(null, SortDirection.ASCENDING),
                "Should throw IllegalArgumentException for null algotype"
            );
            
            assertTrue(exception.getMessage().contains("Algotype"),
                "Exception message should mention Algotype");
        }
        
        @Test
        @DisplayName("As a user I want metadata construction to fail with null direction so that I catch configuration errors early")
        void rejectNullSortDirection() {
            // PURPOSE: Verify null validation for sortDirection parameter
            // INPUTS: valid Algotype, null sortDirection
            // OUTPUTS: IllegalArgumentException
            
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CellMetadata(Algotype.BUBBLE, null),
                "Should throw IllegalArgumentException for null sort direction"
            );
            
            assertTrue(exception.getMessage().contains("SortDirection"),
                "Exception message should mention SortDirection");
        }
    }
    
    @Nested
    @DisplayName("Algotype access tests")
    class AlgotypeAccessTests {
        
        @Test
        @DisplayName("As a user I want to retrieve the algotype so that the engine can determine neighbor visibility")
        void retrieveAlgotype() {
            // PURPOSE: Verify getAlgotype() returns stored algotype
            // INPUTS: CellMetadata with each algotype
            // OUTPUTS: Matching algotype from getter
            
            CellMetadata bubbleMetadata = new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
            assertEquals(Algotype.BUBBLE, bubbleMetadata.getAlgotype());
            
            CellMetadata insertionMetadata = new CellMetadata(Algotype.INSERTION, SortDirection.ASCENDING);
            assertEquals(Algotype.INSERTION, insertionMetadata.getAlgotype());
            
            CellMetadata selectionMetadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            assertEquals(Algotype.SELECTION, selectionMetadata.getAlgotype());
        }
    }
    
    @Nested
    @DisplayName("Sort direction access tests")
    class SortDirectionAccessTests {
        
        @Test
        @DisplayName("As a user I want to retrieve the sort direction so that the engine can perform direction-aware comparisons")
        void retrieveSortDirection() {
            // PURPOSE: Verify getSortDirection() returns stored direction
            // INPUTS: CellMetadata with each sort direction
            // OUTPUTS: Matching direction from getter
            
            CellMetadata ascendingMetadata = new CellMetadata(Algotype.BUBBLE, SortDirection.ASCENDING);
            assertEquals(SortDirection.ASCENDING, ascendingMetadata.getSortDirection());
            
            CellMetadata descendingMetadata = new CellMetadata(Algotype.BUBBLE, SortDirection.DESCENDING);
            assertEquals(SortDirection.DESCENDING, descendingMetadata.getSortDirection());
        }
    }
    
    @Nested
    @DisplayName("Ideal position tests for SELECTION algotype")
    class IdealPositionTests {
        
        @Test
        @DisplayName("As a user I want ideal position initialized to 0 so that SELECTION cells start at leftmost position")
        void idealPosInitializedToZero() {
            // PURPOSE: Verify idealPos defaults to 0 for ascending sort
            // INPUTS: CellMetadata with SELECTION algotype
            // OUTPUTS: idealPos = 0
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            assertEquals(0, metadata.getIdealPos(), "Ideal position should initialize to 0");
        }
        
        @Test
        @DisplayName("As a user I want to set ideal position so that I can initialize DESCENDING cells to rightBoundary")
        void setIdealPosition() {
            // PURPOSE: Verify setIdealPos() updates position
            // INPUTS: CellMetadata, newIdealPos = 42
            // OUTPUTS: idealPos = 42
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            metadata.setIdealPos(42);
            assertEquals(42, metadata.getIdealPos(), "Ideal position should be updated to 42");
        }
        
        @Test
        @DisplayName("As a user I want to increment ideal position so that SELECTION cells adjust target when swaps are denied")
        void incrementIdealPosition() {
            // PURPOSE: Verify incrementIdealPos() atomically increments and returns new value
            // INPUTS: CellMetadata starting at idealPos = 0
            // OUTPUTS: incrementIdealPos() returns 1, 2, 3, ...
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            assertEquals(0, metadata.getIdealPos(), "Initial ideal position should be 0");
            
            assertEquals(1, metadata.incrementIdealPos(), "First increment should return 1");
            assertEquals(2, metadata.incrementIdealPos(), "Second increment should return 2");
            assertEquals(3, metadata.incrementIdealPos(), "Third increment should return 3");
            assertEquals(3, metadata.getIdealPos(), "Final ideal position should be 3");
        }
        
        @Test
        @DisplayName("As a user I want compare-and-set for ideal position so that concurrent updates are coordinated")
        void compareAndSetIdealPosition() {
            // PURPOSE: Verify compareAndSetIdealPos() performs atomic CAS operation
            // INPUTS: CellMetadata with idealPos = 5
            // OUTPUTS: CAS(5, 10) succeeds, CAS(5, 15) fails
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            metadata.setIdealPos(5);
            
            // First CAS should succeed (current value is 5)
            assertTrue(metadata.compareAndSetIdealPos(5, 10),
                "CAS should succeed when expected matches current");
            assertEquals(10, metadata.getIdealPos(), "Ideal position should be updated to 10");
            
            // Second CAS should fail (current value is 10, not 5)
            assertFalse(metadata.compareAndSetIdealPos(5, 15),
                "CAS should fail when expected doesn't match current");
            assertEquals(10, metadata.getIdealPos(), "Ideal position should remain 10");
        }
        
        @Test
        @DisplayName("As a user I want updateForBoundary to reset ideal position so that SELECTION cells track group merges")
        void updateForBoundaryAscending() {
            // PURPOSE: Verify updateForBoundary() sets idealPos to leftBoundary for ASCENDING
            // INPUTS: CellMetadata(SELECTION, ASCENDING), leftBoundary=10, rightBoundary=50
            // OUTPUTS: idealPos = 10
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            metadata.updateForBoundary(10, 50);
            assertEquals(10, metadata.getIdealPos(),
                "Ideal position should be set to leftBoundary for ASCENDING");
        }
        
        @Test
        @DisplayName("As a user I want updateForBoundary to set ideal position to rightBoundary for DESCENDING so that cells target correct end")
        void updateForBoundaryDescending() {
            // PURPOSE: Verify updateForBoundary() sets idealPos to rightBoundary for DESCENDING
            // INPUTS: CellMetadata(SELECTION, DESCENDING), leftBoundary=10, rightBoundary=50
            // OUTPUTS: idealPos = 50
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.DESCENDING);
            metadata.updateForBoundary(10, 50);
            assertEquals(50, metadata.getIdealPos(),
                "Ideal position should be set to rightBoundary for DESCENDING");
        }
    }
    
    @Nested
    @DisplayName("Thread safety tests")
    class ThreadSafetyTests {
        
        @Test
        @DisplayName("As a user I want ideal position operations to be thread-safe so that parallel execution doesn't corrupt state")
        void idealPosThreadSafety() throws InterruptedException {
            // PURPOSE: Verify concurrent incrementIdealPos() calls produce correct result
            // INPUTS: CellMetadata, 10 threads, 100 increments each
            // OUTPUTS: idealPos = 1000
            
            CellMetadata metadata = new CellMetadata(Algotype.SELECTION, SortDirection.ASCENDING);
            final int numThreads = 10;
            final int incrementsPerThread = 100;
            
            Thread[] threads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        metadata.incrementIdealPos();
                    }
                });
                threads[i].start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            assertEquals(numThreads * incrementsPerThread, metadata.getIdealPos(),
                "Ideal position should be exactly " + (numThreads * incrementsPerThread) + 
                " after concurrent increments");
        }
    }
}
