package com.emergent.doom.factorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BatchExperimentRunner")
class BatchExperimentRunnerTest {
    
    private static final int TEST_TARGET = 143;
    private static final int TEST_ARRAY_SIZE = 30;  // Smaller for test speed
    private static final int TEST_NUM_TRIALS = 10;
    private static final long TEST_BASE_SEED = 42L;
    
    @Nested
    @DisplayName("Batch Lifecycle")
    class BatchLifecycleTests {
        
        @Test
        @DisplayName("should create runner with default configuration")
        void shouldCreateRunnerWithDefaults() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            assertNotNull(runner);
        }
        
        @Test
        @DisplayName("should create runner with custom configuration")
        void shouldCreateRunnerWithCustomConfig() {
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.FERMAT_NEAR_SQRT, 0.3,
                FactorStrategy.RANDOM_SAMPLE, 0.2
            );
            
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED,
                1000, distribution
            );
            assertNotNull(runner);
        }
        
        @Test
        @DisplayName("should run complete batch")
        void shouldRunCompleteBatch() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            
            assertNotNull(results);
            assertEquals(TEST_NUM_TRIALS, results.trialResults.size());
            assertTrue(results.trialResults.stream().allMatch(r -> r.getConvergenceStep() > 0));
        }
        
        @Test
        @DisplayName("should have non-empty trial results")
        void shouldHaveNonEmptyTrialResults() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            
            assertTrue(results.trialResults.size() > 0);
            results.trialResults.forEach(trial -> {
                assertNotNull(trial);
                assertTrue(trial.getPeakAggregation() >= 0);
                assertTrue(trial.getPeakAggregation() <= 100);
            });
        }
    }
    
    @Nested
    @DisplayName("Statistics Computation")
    class StatisticsTests {
        
        @Test
        @DisplayName("should compute peak aggregation mean")
        void shouldComputePeakAggregationMean() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            double mean = results.getPeakAggregationMean();
            
            assertTrue(mean >= 0 && mean <= 100);
            assertTrue(mean > 50);  // Should exceed random baseline
        }
        
        @Test
        @DisplayName("should compute standard deviation")
        void shouldComputeStandardDeviation() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            double stdDev = results.getPeakAggregationStdDev();
            
            assertTrue(stdDev >= 0);
        }
        
        @Test
        @DisplayName("should compute min and max correctly")
        void shouldComputeMinAndMax() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            double min = results.getPeakAggregationMin();
            double max = results.getPeakAggregationMax();
            double mean = results.getPeakAggregationMean();
            
            assertTrue(min <= mean);
            assertTrue(max >= mean);
            assertTrue(min <= max);
        }
        
        @Test
        @DisplayName("should compute convergence step mean")
        void shouldComputeConvergenceStepMean() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            double convergenceMean = results.getConvergenceStepMean();
            
            assertTrue(convergenceMean > 0);
        }
    }
    
    @Nested
    @DisplayName("Threshold Testing")
    class ThresholdTests {
        
        @Test
        @DisplayName("should count trials above threshold")
        void shouldCountTrialsAboveThreshold() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            int countAbove60 = results.countTrialsAboveThreshold(60.0);
            int countAboveBaseline = results.countTrialsAboveThreshold(61.0);
            
            assertTrue(countAbove60 >= 0);
            assertTrue(countAbove60 <= TEST_NUM_TRIALS);
            assertTrue(countAboveBaseline <= countAbove60);
        }
        
        @Test
        @DisplayName("should support hypothesis threshold checking")
        void shouldSupportHypothesisThresholdChecking() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            
            // At least some trials should exceed baseline
            int trialsAboveBaseline = results.countTrialsAboveThreshold(60.0);
            assertTrue(trialsAboveBaseline > 0);
        }
    }
    
    @Nested
    @DisplayName("CSV Export")
    class CSVExportTests {
        
        @Test
        @DisplayName("should export trial CSVs")
        void shouldExportTrialCSVs() throws IOException {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, TEST_BASE_SEED  // Fewer trials for speed
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            Path tempDir = Files.createTempDirectory("batch_test_");
            
            try {
                results.exportAllTrialsToCSV(tempDir.toString());
                
                // Verify files created
                List<Path> csvFiles = Files.list(tempDir)
                    .filter(p -> p.toString().endsWith(".csv"))
                    .collect(Collectors.toList());
                
                assertEquals(3, csvFiles.size());
                
                // Verify each has content
                csvFiles.forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        assertTrue(content.contains("step"));
                        assertTrue(content.length() > 0);
                    } catch (IOException e) {
                        fail("Failed to read CSV: " + path);
                    }
                });
            } finally {
                Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
            }
        }
        
        @Test
        @DisplayName("should export statistics CSV")
        void shouldExportStatisticsCSV() throws IOException {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            Path tempFile = Files.createTempFile("batch_stats_", ".csv");
            
            try {
                results.exportStatisticsToCSV(tempFile.toString());
                
                String content = Files.readString(tempFile);
                assertTrue(content.contains("peak_aggregation_mean"));
                assertTrue(content.contains("peak_aggregation_stddev"));
                assertTrue(content.contains("convergence_step_mean"));
            } finally {
                Files.delete(tempFile);
            }
        }
    }
    
    @Nested
    @DisplayName("Output Methods")
    class OutputMethodsTests {
        
        @Test
        @DisplayName("should not throw on printSummary")
        void shouldNotThrowOnPrintSummary() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            
            // Should not throw
            assertDoesNotThrow(results::printSummary);
        }
        
        @Test
        @DisplayName("should have correct batch metadata")
        void shouldHaveCorrectBatchMetadata() {
            BatchExperimentRunner runner = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, TEST_NUM_TRIALS, TEST_BASE_SEED
            );
            
            BatchExperimentRunner.BatchResults results = runner.runBatch();
            
            assertEquals(TEST_TARGET, results.target);
            assertEquals(TEST_ARRAY_SIZE, results.arraySize);
            assertEquals(TEST_NUM_TRIALS, results.numTrials);
            assertEquals(TEST_BASE_SEED, results.baseSeed);
            assertNotNull(results.distribution);
        }
    }
    
    @Nested
    @DisplayName("Reproducibility")
    class ReproducibilityTests {
        
        @Test
        @DisplayName("should produce identical results with same seed")
        void shouldProduceIdenticalResultsWithSameSeed() {
            // Run 1
            BatchExperimentRunner runner1 = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, 12345L
            );
            BatchExperimentRunner.BatchResults results1 = runner1.runBatch();
            
            // Run 2
            BatchExperimentRunner runner2 = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, 12345L
            );
            BatchExperimentRunner.BatchResults results2 = runner2.runBatch();
            
            // Verify identical statistics
            assertEquals(
                results1.getPeakAggregationMean(),
                results2.getPeakAggregationMean(),
                0.01
            );
        }
        
        @Test
        @DisplayName("should produce different results with different seeds")
        void shouldProduceDifferentResultsWithDifferentSeeds() {
            // Run 1
            BatchExperimentRunner runner1 = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, 11111L
            );
            BatchExperimentRunner.BatchResults results1 = runner1.runBatch();
            
            // Run 2
            BatchExperimentRunner runner2 = new BatchExperimentRunner(
                TEST_TARGET, TEST_ARRAY_SIZE, 3, 22222L
            );
            BatchExperimentRunner.BatchResults results2 = runner2.runBatch();
            
            // Statistics should differ (very unlikely to be identical)
            // We check they're not exactly equal
            double diff = Math.abs(
                results1.getPeakAggregationMean() - results2.getPeakAggregationMean()
            );
            assertTrue(diff > 0.01);  // Essentially never identical
        }
    }
}
