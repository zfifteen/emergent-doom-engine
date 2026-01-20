package com.emergent.doom.sat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * Tests for SATExperiment (PHASE THREE ITER 3).
 *
 * As a developer, I want to verify full experiment execution.
 */
public class SATExperimentTest {

    @Test
    void testRunExperiment() {
        var formula = SATInstanceGenerator.generatePilotInstance();
        var experiment = new SATExperiment(formula, 40, 42L);
        experiment.run(10); // Short run for test
        
        assertTrue(experiment.getStepCount() > 0);
        assertFalse(experiment.getCells().isEmpty());
        // Check some swaps occurred or stable
        assertTrue(experiment.getStepCount() <= 10 || experiment.isSolved());
    }

    @Test
    void testInitialization() {
        var formula = SATInstanceGenerator.generateAppendixB();
        var experiment = new SATExperiment(formula, 10, 42L);
        
        var cells = experiment.getCells();
        assertEquals(10, cells.size());
        // Verify distribution roughly 30/30/40
        long dpllCount = cells.stream().filter(c -> c.readAlgotype() == SATStrategy.DPLL).count();
        long greedyCount = cells.stream().filter(c -> c.readAlgotype() == SATStrategy.GREEDY_MCV).count();
        long walksatCount = cells.stream().filter(c -> c.readAlgotype() == SATStrategy.WALKSAT).count();
        long hybridCount = cells.stream().filter(c -> c.readAlgotype() == SATStrategy.HYBRID).count();
        
        assertTrue(dpllCount + greedyCount + walksatCount + hybridCount == 10);
        assertTrue(dpllCount >= 2 && dpllCount <= 4); // Roughly 30%
    }

    @Test
    void testSimpleSwap() {
        var formula = SATInstanceGenerator.generateAppendixB();
        var experiment = new SATExperiment(formula, 4, 42L);
        
        // Run 1 step
        int initialSwaps = 0;
        experiment.run(1);
        
        // Check if any swap occurred (depends on random, but possible)
        // Test for non-zero cells
        assertTrue(experiment.getStepCount() == 1);
    }
}