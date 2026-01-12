package com.emergent.doom.swap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ThreadSafeFrozenCellStatus.
 *
 * PURPOSE: Verify that ThreadSafeFrozenCellStatus provides thread-safe tracking
 * of frozen cell states using ConcurrentHashMap for lock-free concurrent access
 * during parallel execution.
 *
 * [TestWeaver: Generated skeleton - expand with specific test scenarios]
 */
@DisplayName("ThreadSafeFrozenCellStatus Tests")
class ThreadSafeFrozenCellStatusTest {

    private ThreadSafeFrozenCellStatus frozenStatus;

    @BeforeEach
    void setUp() {
        frozenStatus = new ThreadSafeFrozenCellStatus();
    }

    /**
     * PURPOSE: As a developer, I want to verify setFrozen and getFrozen operations
     * so that I can confirm frozen states are stored and retrieved correctly.
     *
     * INPUTS: [TestWeaver: Define test inputs - position, FrozenType]
     * EXPECTED OUTPUT: [TestWeaver: getFrozen returns set value]
     * TEST DATA: [TestWeaver: Position 5, FrozenType.IMMOVABLE]
     * REPRODUCTION: [TestWeaver: Manual verification steps]
     *
     * [TestWeaver: Implement test logic based on ThreadSafeFrozenCellStatus API]
     */
    @Test
    @DisplayName("setFrozen and getFrozen work correctly")
    void setFrozenAndGetFrozenWorkCorrectly() {
        frozenStatus.setFrozen(5, FrozenCellStatus.FrozenType.IMMOVABLE);
        assertEquals(FrozenCellStatus.FrozenType.IMMOVABLE, frozenStatus.getFrozen(5));
        
        frozenStatus.setFrozen(10, FrozenCellStatus.FrozenType.MOVABLE);
        assertEquals(FrozenCellStatus.FrozenType.MOVABLE, frozenStatus.getFrozen(10));
        
        // Unset position should return NONE
        assertEquals(FrozenCellStatus.FrozenType.NONE, frozenStatus.getFrozen(99));
    }

    /**
     * PURPOSE: As a developer, I want to verify NONE state removal optimization
     * so that I can confirm setting NONE removes entries from the map.
     *
     * INPUTS: [TestWeaver: Define position with frozen state, then set to NONE]
     * EXPECTED OUTPUT: [TestWeaver: Entry removed from internal map]
     * TEST DATA: [TestWeaver: Set position 3 to IMMOVABLE, then to NONE]
     * REPRODUCTION: [TestWeaver: Manual steps]
     *
     * [TestWeaver: Implement NONE removal test]
     */
    @Test
    @DisplayName("setFrozen removes entry when set to NONE")
    void setFrozenRemovesEntryWhenSetToNone() {
        frozenStatus.setFrozen(3, FrozenCellStatus.FrozenType.IMMOVABLE);
        assertEquals(FrozenCellStatus.FrozenType.IMMOVABLE, frozenStatus.getFrozen(3));
        
        frozenStatus.setFrozen(3, FrozenCellStatus.FrozenType.NONE);
        assertEquals(FrozenCellStatus.FrozenType.NONE, frozenStatus.getFrozen(3));
        
        // Verify position is not in frozen positions set
        assertFalse(frozenStatus.getFrozenPositions().contains(3));
    }

    /**
     * PURPOSE: As a developer, I want to verify isImmovable convenience method
     * so that I can easily check if a cell is completely frozen.
     *
     * INPUTS: [TestWeaver: Define positions with different frozen states]
     * EXPECTED OUTPUT: [TestWeaver: True only for IMMOVABLE]
     * TEST DATA: [TestWeaver: NONE=false, MOVABLE=false, IMMOVABLE=true]
     * REPRODUCTION: [TestWeaver: Manual verification]
     *
     * [TestWeaver: Implement isImmovable test]
     */
    @Test
    @DisplayName("isImmovable returns true only for IMMOVABLE state")
    void isImmovableReturnsTrueOnlyForImmovable() {
        frozenStatus.setFrozen(1, FrozenCellStatus.FrozenType.NONE);
        frozenStatus.setFrozen(2, FrozenCellStatus.FrozenType.MOVABLE);
        frozenStatus.setFrozen(3, FrozenCellStatus.FrozenType.IMMOVABLE);
        
        assertFalse(frozenStatus.isImmovable(1));
        assertFalse(frozenStatus.isImmovable(2));
        assertTrue(frozenStatus.isImmovable(3));
        assertFalse(frozenStatus.isImmovable(99)); // Unset position
    }

    /**
     * PURPOSE: As a developer, I want to verify concurrent access safety
     * so that I can use this class in multi-threaded execution without data races.
     *
     * INPUTS: [TestWeaver: Define concurrent reads/writes from multiple threads]
     * EXPECTED OUTPUT: [TestWeaver: No race conditions, all operations complete]
     * TEST DATA: [TestWeaver: Multiple threads setting/getting different positions]
     * REPRODUCTION: [TestWeaver: Manual verification with concurrent test framework]
     *
     * [TestWeaver: Implement thread safety test with multiple threads]
     */
    @Test
    @DisplayName("concurrent access is thread-safe")
    void concurrentAccessIsThreadSafe() throws InterruptedException {
        int numThreads = 10;
        int operationsPerThread = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        AtomicInteger errors = new AtomicInteger(0);
        
        for (int t = 0; t < numThreads; t++) {
            final int threadId = t;
            new Thread(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    for (int i = 0; i < operationsPerThread; i++) {
                        int position = threadId * operationsPerThread + i;
                        
                        // Set to IMMOVABLE
                        frozenStatus.setFrozen(position, FrozenCellStatus.FrozenType.IMMOVABLE);
                        
                        // Read back
                        FrozenCellStatus.FrozenType type = frozenStatus.getFrozen(position);
                        if (type != FrozenCellStatus.FrozenType.IMMOVABLE) {
                            errors.incrementAndGet();
                        }
                        
                        // Check immovable
                        if (!frozenStatus.isImmovable(position)) {
                            errors.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errors.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }
        
        startLatch.countDown(); // Start all threads
        doneLatch.await(); // Wait for completion
        
        assertEquals(0, errors.get(), "Should have no concurrent access errors");
        
        // Verify all positions were set
        assertEquals(numThreads * operationsPerThread, frozenStatus.getFrozenPositions().size());
    }
}
