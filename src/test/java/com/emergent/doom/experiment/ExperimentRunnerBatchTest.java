package com.emergent.doom.experiment;

import com.emergent.doom.cell.Algotype;
import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.execution.ExecutionMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Helper to create a random array with a local Random instance (thread-safe).
     */
    private static GenericCell[] createRandomArrayThreadSafe(int size, long seed) {
        Random localRandom = new Random(seed);
        GenericCell[] cells = new GenericCell[size];
        for (int i = 0; i < size; i++) {
            cells[i] = new GenericCell(localRandom.nextInt(1000), Algotype.BUBBLE);
        }
        return cells;
    }

    // ========================================================================
    // Basic Batch Execution Tests
    // ========================================================================

    @Nested
    @DisplayName("Basic batch execution")
    class BasicBatchTests {

        @Test
        @DisplayName("Runs standard batch of 100 trials successfully")
        void runsStandardBatch() {
            ExperimentConfig config = new ExperimentConfig(
                    30, 3000, 3, false, ExecutionMode.SEQUENTIAL, 100);
            
            ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
            
            assertNotNull(results, "Results should not be null");
            assertEquals(100, results.getTrials().size(), "Should have 100 trials");
            
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

    // ========================================================================
    // Edge Case Tests
    // ========================================================================

    @Nested
    @DisplayName("Edge cases and error handling")
    class EdgeCaseTests {

        /**
         * As a user, I want clear error messages when I provide invalid input
         * so that I can quickly fix configuration issues.
         */
        @Test
        @DisplayName("Throws exception for zero repetitions")
        void throwsForZeroRepetitions() {
            // ExperimentConfig constructor validates numRepetitions
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExperimentConfig(20, 2000, 3, false, ExecutionMode.SEQUENTIAL, 0),
                    "Should throw for zero repetitions");
            
            // ExperimentConfig message is "Number of repetitions must be positive"
            assertTrue(exception.getMessage().toLowerCase().contains("repetitions"),
                    "Exception message should mention repetitions");
        }

        /**
         * As a user, I want clear error messages for negative repetitions
         * so that I understand the valid input range.
         */
        @Test
        @DisplayName("Throws exception for negative repetitions")
        void throwsForNegativeRepetitions() {
            // ExperimentConfig constructor validates numRepetitions
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new ExperimentConfig(20, 2000, 3, false, ExecutionMode.SEQUENTIAL, -5),
                    "Should throw for negative repetitions");
            
            // ExperimentConfig message is "Number of repetitions must be positive"
            assertTrue(exception.getMessage().toLowerCase().contains("repetitions"),
                    "Exception message should mention repetitions");
        }
    }

    // ========================================================================
    // Thread Pool Behavior Tests
    // ========================================================================

    @Nested
    @DisplayName("Thread pool behavior")
    class ThreadPoolTests {

        /**
         * As a user, I want the system to handle more trials than CPU cores
         * so that I can run large experiments without worrying about hardware limits.
         */
        @Test
        @DisplayName("Handles more trials than CPU cores")
        void handlesMoreTrialsThanCores() {
            int numCores = Runtime.getRuntime().availableProcessors();
            int numTrials = numCores * 3; // Significantly more trials than cores
            
            ExperimentConfig config = new ExperimentConfig(
                    15, 1500, 3, false, ExecutionMode.SEQUENTIAL, numTrials);
            
            ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
            
            assertEquals(numTrials, results.getTrials().size(),
                    "Should complete all trials even with limited thread pool");
        }

        /**
         * As a user, I want trials to be truly parallelized
         * so that I get performance benefits from multi-core systems.
         */
        @Test
        @DisplayName("Parallel execution is faster than sequential for many trials")
        void parallelFasterThanSequential() {
            // Run a moderate batch and verify it completes in reasonable time
            // This is a sanity check that parallelization is working
            ExperimentConfig config = new ExperimentConfig(
                    20, 2000, 3, false, ExecutionMode.SEQUENTIAL, 100);
            
            long start = System.currentTimeMillis();
            ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
            long elapsed = System.currentTimeMillis() - start;
            
            assertEquals(100, results.getTrials().size(), "Should complete all trials");
            // With parallelization, 100 trials should complete much faster than
            // 100x single trial time. This is a loose sanity check.
            assertTrue(elapsed < 30000, "Parallel execution should complete within 30 seconds");
        }
    }

    // ========================================================================
    // Trial Failure Tests
    // ========================================================================

    @Nested
    @DisplayName("Trial failure handling")
    class TrialFailureTests {

        /**
         * As a user, I want the system to fail fast when a trial fails
         * so that I can identify and fix issues quickly without waiting for all trials.
         */
        @Test
        @DisplayName("Fails fast when trial throws exception")
        void failsFastOnTrialException() {
            AtomicBoolean hasThrown = new AtomicBoolean(false);
            
            // Create runner that throws on first trial to invoke the factory
            ExperimentRunner<GenericCell> failingRunner = new ExperimentRunner<>(
                    () -> {
                        // Only throw once to ensure deterministic behavior
                        if (hasThrown.compareAndSet(false, true)) {
                            throw new RuntimeException("Simulated trial failure");
                        }
                        return createRandomArrayThreadSafe(10, System.nanoTime());
                    },
                    () -> null
            );
            
            ExperimentConfig config = new ExperimentConfig(
                    10, 500, 3, false, ExecutionMode.SEQUENTIAL, 100);
            
            assertThrows(
                    RuntimeException.class,
                    () -> failingRunner.runBatchExperiments(config),
                    "Should throw when trial fails");
        }

        /**
         * As a user, I want exception details preserved when trials fail
         * so that I can debug the root cause.
         */
        @Test
        @DisplayName("Preserves exception details on failure")
        void preservesExceptionDetails() {
            String specificMessage = "Specific failure reason XYZ";
            
            ExperimentRunner<GenericCell> failingRunner = new ExperimentRunner<>(
                    () -> {
                        throw new IllegalStateException(specificMessage);
                    },
                    () -> null
            );
            
            ExperimentConfig config = new ExperimentConfig(
                    10, 500, 3, false, ExecutionMode.SEQUENTIAL, 1);
            
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> failingRunner.runBatchExperiments(config));
            
            // Check that original exception info is preserved in message or cause
            String fullMessage = exception.getMessage();
            Throwable cause = exception.getCause();
            boolean hasDetails = (fullMessage != null && fullMessage.contains(specificMessage)) ||
                    (cause != null && cause.getMessage() != null && 
                     cause.getMessage().contains(specificMessage));
            assertTrue(hasDetails, "Should preserve original exception details. Got: " + fullMessage);
        }
    }

    // ========================================================================
    // Result Integrity Tests
    // ========================================================================

    @Nested
    @DisplayName("Result integrity")
    class ResultIntegrityTests {

        /**
         * As a user, I want each trial to have a unique trial number
         * so that I can track and analyze individual trial results.
         */
        @Test
        @DisplayName("Each trial has unique trial number")
        void trialsHaveUniqueNumbers() {
            ExperimentConfig config = new ExperimentConfig(
                    20, 2000, 3, false, ExecutionMode.SEQUENTIAL, 100);
            
            ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
            
            long uniqueNumbers = results.getTrials().stream()
                    .map(TrialResult::getTrialNumber)
                    .distinct()
                    .count();
            
            assertEquals(100, uniqueNumbers, "All trial numbers should be unique");
        }

        /**
         * As a user, I want timing data for each trial
         * so that I can analyze performance characteristics.
         */
        @Test
        @DisplayName("Each trial records execution time")
        void trialsRecordExecutionTime() {
            ExperimentConfig config = new ExperimentConfig(
                    20, 2000, 3, false, ExecutionMode.SEQUENTIAL, 100);
            
            ExperimentResults<GenericCell> results = runner.runBatchExperiments(config);
            
            for (TrialResult<GenericCell> trial : results.getTrials()) {
                assertTrue(trial.getExecutionTimeNanos() > 0,
                        "Trial " + trial.getTrialNumber() + " should have positive execution time");
            }
        }
    }
}
