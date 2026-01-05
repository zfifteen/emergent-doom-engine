package com.emergent.doom.experiment;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.execution.ExecutionMode;
import com.emergent.doom.topology.BubbleTopology;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for v2.0 refactor (Issue #92).
 *
 * <p>Validates that sequential execution with batch parallelism works correctly
 * after removing deprecated per-cell threading modes.</p>
 */
class RefactorSmokeTest {

    private static final Random random = new Random(42);

    private static GenericCell[] createRandomArray(int size) {
        GenericCell[] cells = new GenericCell[size];
        for (int i = 0; i < size; i++) {
            cells[i] = new GenericCell(random.nextInt(1000));
        }
        return cells;
    }

    @Test
    void smokeTestBatchExecution() {
        ExperimentConfig config = new ExperimentConfig(
            100,                        // arraySize
            10000,                      // maxSteps
            10,                         // requiredStableSteps
            false,                      // recordTrajectory (disabled for speed)
            ExecutionMode.SEQUENTIAL,
            10                          // 10 trials
        );

        ExperimentRunner<GenericCell> runner = new ExperimentRunner<>(
            () -> createRandomArray(100),
            () -> new BubbleTopology<>()
        );

        ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);

        assertEquals(10, results.getTrials().size(), "Should run 10 trials");
        assertEquals(10, results.getTrials().stream().filter(t -> t.isConverged()).count(), "All trials should converge");
        assertTrue(results.getMeanSteps() > 0, "Should record steps taken");
    }

    @Test
    void smokeTestSingleTrialExecution() {
        ExperimentConfig config = new ExperimentConfig(
            50,                         // small array for fast test
            5000,                       // maxSteps
            10,                         // requiredStableSteps
            false,                      // recordTrajectory
            ExecutionMode.SEQUENTIAL,
            1                           // single trial
        );

        ExperimentRunner<GenericCell> runner = new ExperimentRunner<>(
            () -> createRandomArray(50),
            () -> new BubbleTopology<>()
        );

        ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);

        assertEquals(1, results.getTrials().stream().filter(t -> t.isConverged()).count(), "Single trial should converge");
    }
}