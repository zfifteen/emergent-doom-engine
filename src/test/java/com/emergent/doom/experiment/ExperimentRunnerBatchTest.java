package com.emergent.doom.experiment;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.execution.ExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ExperimentRunner batch execution functionality.
 * As a user, I want to run multiple trials in parallel efficiently.
 */
@Timeout(value = 60, unit = TimeUnit.SECONDS)
class ExperimentRunnerBatchTest {

    private ExperimentRunner<GenericCell> runner;
    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(12345);
        runner = new ExperimentRunner<>(
                () -> createRandomArray(30),
                () -> null
        );
    }

    private GenericCell[] createRandomArray(int size) {
        GenericCell[] cells = new GenericCell[size];
        for (int i = 0; i < size; i++) {
            cells[i] = new GenericCell(random.nextInt(1000), Algotype.BUBBLE);
        }
        return cells;
    }

    @Test
    @DisplayName("Runs small batch of trials successfully")
    void runsSmallBatch() {
        ExperimentConfig config = new ExperimentConfig(
                30, 3000, 3, false, ExecutionMode.SEQUENTIAL, 5);
        
        ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
        
        assertNotNull(results, "Results should not be null");
        assertEquals(5, results.getTrials().size(), "Should have 5 trials");
        
        // Verify all trials completed
        for (TrialResult<GenericCell> trial : results.getTrials()) {
            assertTrue(trial.isConverged() || trial.getFinalStep() > 0,
                "Trial should converge or execute steps");
        }
    }

    @Test
    @DisplayName("Handles single trial batch")
    void handlesSingleTrial() {
        ExperimentConfig config = new ExperimentConfig(
                20, 2000, 3, false, ExecutionMode.SEQUENTIAL, 1);
        
        ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
        
        assertEquals(1, results.getTrials().size(), "Should have 1 trial");
    }
}
