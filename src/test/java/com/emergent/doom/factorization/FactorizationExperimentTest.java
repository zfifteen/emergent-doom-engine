package com.emergent.doom.factorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FactorizationExperiment.
 *
 * <p><strong>PURPOSE:</strong> Validate Step 2 success criteria:
 * <ul>
 *   <li>Experiment runs complete lifecycle</li>
 *   <li>Trajectory recording works correctly</li>
 *   <li>Aggregation measurement is accurate</li>
 *   <li>CSV export format is valid</li>
 *   <li>Perfect factor detection works</li>
 * </ul>
 */
@DisplayName("FactorizationExperiment Tests")
class FactorizationExperimentTest {
    
    private static final int TARGET_N = 143;  // 11 × 13
    
    @Nested
    @DisplayName("Experiment Lifecycle")
    class LifecycleTests {
        
        @Test
        @DisplayName("Experiment completes successfully")
        void experimentCompletes() {
            // GIVEN: Configured experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 20, distribution, 42L, 1000
            );
            
            // WHEN: Running trial
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Results contain data
            assertNotNull(results);
            assertNotNull(results.trajectory);
            assertFalse(results.trajectory.isEmpty());
        }
        
        @Test
        @DisplayName("Trajectory includes initial and final states")
        void trajectoryHasInitialAndFinal() {
            // GIVEN: Experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 10, distribution, 42L, 100
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Has at least 2 entries (initial + at least one step)
            assertTrue(results.trajectory.size() >= 2);
            assertEquals(0, results.trajectory.get(0).step, "First entry should be step 0");
        }
        
        @Test
        @DisplayName("Convergence detected when no more swaps")
        void convergenceDetection() {
            // GIVEN: Small array that will converge quickly
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 5, distribution, 42L, 1000
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Converges before max steps
            assertTrue(results.getConvergenceStep() < 1000,
                "Small array should converge before max steps");
        }
    }
    
    @Nested
    @DisplayName("Aggregation Measurement")
    class AggregationTests {
        
        @Test
        @DisplayName("Initial aggregation computed")
        void initialAggregation() {
            // GIVEN: Experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 20, distribution, 42L, 100
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Initial aggregation in valid range [0, 100]
            FactorizationExperiment.StepData initial = results.trajectory.get(0);
            assertTrue(initial.aggregation >= 0.0);
            assertTrue(initial.aggregation <= 100.0);
        }
        
        @Test
        @DisplayName("Peak aggregation >= initial aggregation")
        void peakAggregationIncreases() {
            // GIVEN: Experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.FERMAT_NEAR_SQRT, 0.5
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 30, distribution, 42L, 500
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Peak >= initial (clustering should not decrease peak)
            FactorizationExperiment.StepData initial = results.trajectory.get(0);
            double peak = results.getPeakAggregation();
            
            assertTrue(peak >= initial.aggregation,
                "Peak aggregation should be >= initial");
        }
    }
    
    @Nested
    @DisplayName("Fitness Tracking")
    class FitnessTests {
        
        @Test
        @DisplayName("Average fitness computed")
        void avgFitnessComputed() {
            // GIVEN: Experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 10, distribution, 42L, 100
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: All trajectory entries have valid avg fitness
            for (FactorizationExperiment.StepData data : results.trajectory) {
                assertTrue(data.avgFitness >= 0.0);
                assertTrue(data.avgFitness <= 1.0);
            }
        }
        
        @Test
        @DisplayName("Max fitness should be >= avg fitness")
        void maxFitnessGreaterThanAvg() {
            // GIVEN: Experiment
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 20, distribution, 42L, 100
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: Max >= avg always
            for (FactorizationExperiment.StepData data : results.trajectory) {
                assertTrue(data.maxFitness >= data.avgFitness,
                    "Max fitness should be >= avg fitness");
            }
        }
        
        @Test
        @DisplayName("Perfect factors detected when present")
        void perfectFactorsDetected() {
            // GIVEN: Experiment with SMALL_PRIMES (includes factor 11)
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 10, distribution, 42L, 100
            );
            
            // WHEN: Running
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // THEN: At least one step should detect perfect factor
            boolean foundPerfectFactor = results.trajectory.stream()
                .anyMatch(d -> d.perfectFactorCount > 0);
            
            assertTrue(foundPerfectFactor, "Should detect perfect factor 11");
        }
    }
    
    @Nested
    @DisplayName("CSV Export")
    class CSVExportTests {
        
        @Test
        @DisplayName("CSV export creates valid file")
        void csvExportCreatesFile(@TempDir Path tempDir) throws IOException {
            // GIVEN: Experiment results
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 10, distribution, 42L, 50
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // WHEN: Exporting to CSV
            Path csvFile = tempDir.resolve("test_export.csv");
            results.exportToCSV(csvFile.toString());
            
            // THEN: File exists and has content
            assertTrue(Files.exists(csvFile));
            List<String> lines = Files.readAllLines(csvFile);
            assertFalse(lines.isEmpty());
        }
        
        @Test
        @DisplayName("CSV has correct header")
        void csvHasCorrectHeader(@TempDir Path tempDir) throws IOException {
            // GIVEN: Results
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.RANDOM_SAMPLE, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 5, distribution, 42L, 20
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // WHEN: Exporting
            Path csvFile = tempDir.resolve("test_header.csv");
            results.exportToCSV(csvFile.toString());
            
            // THEN: First line is header
            List<String> lines = Files.readAllLines(csvFile);
            String header = lines.get(0);
            assertEquals("step,aggregation,avg_fitness,max_fitness,perfect_factor_count,perfect_factor_positions",
                header);
        }
        
        @Test
        @DisplayName("CSV has data rows for each step")
        void csvHasDataRows(@TempDir Path tempDir) throws IOException {
            // GIVEN: Results with known trajectory size
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 5, distribution, 42L, 10
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // WHEN: Exporting
            Path csvFile = tempDir.resolve("test_rows.csv");
            results.exportToCSV(csvFile.toString());
            
            // THEN: Number of data rows = trajectory size
            List<String> lines = Files.readAllLines(csvFile);
            int dataRows = lines.size() - 1;  // Subtract header
            assertEquals(results.trajectory.size(), dataRows);
        }
    }
    
    @Nested
    @DisplayName("Result Metrics")
    class ResultMetricsTests {
        
        @Test
        @DisplayName("getPeakAggregation returns maximum")
        void peakAggregationIsMaximum() {
            // GIVEN: Results
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 0.5,
                FactorStrategy.RANDOM_SAMPLE, 0.5
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 20, distribution, 42L, 100
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // WHEN: Getting peak
            double peak = results.getPeakAggregation();
            
            // THEN: Peak equals max from trajectory
            double manualMax = results.trajectory.stream()
                .mapToDouble(d -> d.aggregation)
                .max()
                .orElse(0.0);
            
            assertEquals(manualMax, peak, 0.01);
        }
        
        @Test
        @DisplayName("getConvergenceStep returns final step")
        void convergenceStepIsFinalStep() {
            // GIVEN: Results
            Map<FactorStrategy, Double> distribution = Map.of(
                FactorStrategy.SMALL_PRIMES, 1.0
            );
            FactorizationExperiment experiment = new FactorizationExperiment(
                TARGET_N, 10, distribution, 42L, 50
            );
            FactorizationExperiment.ExperimentResults results = experiment.runTrial();
            
            // WHEN: Getting convergence step
            int convergenceStep = results.getConvergenceStep();
            
            // THEN: Equals last trajectory entry's step
            int finalStep = results.trajectory.get(results.trajectory.size() - 1).step;
            assertEquals(finalStep, convergenceStep);
        }
    }
}
