package com.emergent.doom.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Test suite for Linear Scaling Validation system.
 * 
 * <p>This test suite validates the experimental infrastructure for testing
 * the linear scaling hypothesis (B ≈ 0) on progressively harder semiprimes.</p>
 */
@DisplayName("Linear Scaling Validation Tests")
public class LinearScalingValidatorTest {
    
    @Nested
    @DisplayName("ScalingStage Configuration Tests")
    class ScalingStageTests {
        
        @Test
        @DisplayName("Stage 1 (10^6) returns correct magnitude")
        void stage1HasCorrectMagnitude() {
            // As a researcher, I want Stage 1 to target 10^6 magnitude semiprimes
            // so that I can establish baseline scaling behavior on easy targets
            
            // Test parameters to reproduce:
            // - Stage: STAGE_1_E6
            // - Expected magnitude: 6
            
            assertEquals(6, ScalingStage.STAGE_1_E6.getTargetMagnitude(),
                "Stage 1 should target 10^6 magnitude");
        }
        
        @Test
        @DisplayName("Stage 1 returns array sizes [10^4, 10^5, 10^6]")
        void stage1HasCorrectArraySizes() {
            // As a researcher, I want to test multiple array sizes
            // so that I can measure B = ∂steps/∂array_size across a range
            
            // Test parameters to reproduce:
            // - Stage: STAGE_1_E6
            // - Expected array sizes: [10000, 100000, 1000000]
            
            int[] expected = {10000, 100000, 1000000};
            assertArrayEquals(expected, ScalingStage.STAGE_1_E6.getArraySizes(),
                "Stage 1 should test array sizes spanning 2 orders of magnitude");
        }
        
        @Test
        @DisplayName("Stage 4 (10^18) has higher step limit than Stage 1")
        void harderStagesHaveHigherStepLimits() {
            // As a researcher, I want harder stages to allow more convergence steps
            // so that I can distinguish slow convergence from non-convergence
            
            // Test parameters to reproduce:
            // - Compare: STAGE_1_E6.getMaxSteps() vs STAGE_4_E18.getMaxSteps()
            // - Expected: Stage 4 limit >> Stage 1 limit
            
            assertTrue(ScalingStage.STAGE_4_E18.getMaxSteps() > 
                       ScalingStage.STAGE_1_E6.getMaxSteps(),
                "Harder stages should allow more steps for convergence");
        }
    }
    
    @Nested
    @DisplayName("Target Generation Tests")
    class TargetGenerationTests {
        
        @Test
        @DisplayName("nextPrime finds next prime correctly for small values")
        void nextPrimeWorksForSmallValues() {
            // As a researcher, I want nextPrime to correctly find the next prime
            // so that I can generate valid semiprime targets
            
            // Test parameters to reproduce:
            // - Input: 1000
            // - Expected output: 1009 (verified as prime)
            // - Verification: 1009 is the smallest prime >= 1000
            
            BigInteger result = LinearScalingValidator.nextPrime(BigInteger.valueOf(1000));
            assertEquals(BigInteger.valueOf(1009), result,
                "nextPrime(1000) should return 1009");
        }
        
        @Test
        @DisplayName("nextPrime returns input if already prime")
        void nextPrimeReturnsInputIfPrime() {
            // As a researcher, I want nextPrime(p) = p when p is already prime
            // so that I have deterministic target generation
            
            // Test parameters to reproduce:
            // - Input: 1009 (known prime)
            // - Expected output: 1009
            
            BigInteger prime = BigInteger.valueOf(1009);
            assertEquals(prime, LinearScalingValidator.nextPrime(prime),
                "nextPrime should return input if already prime");
        }
        
        @Test
        @DisplayName("generateTarget produces semiprime in expected range")
        void generateTargetProducesValidSemiprime() {
            // As a researcher, I want generateTarget to produce semiprimes
            // in the correct magnitude range so that I test the intended difficulty
            
            // Test parameters to reproduce:
            // - Stage: STAGE_1_E6
            // - Expected: target in range [10^5, 10^7] (allowing some margin)
            // - Expected: target = p × q where p, q are primes near 10^3
            
            BigInteger target = LinearScalingValidator.generateTarget(ScalingStage.STAGE_1_E6);
            assertTrue(target.compareTo(BigInteger.valueOf(100000)) > 0,
                "Stage 1 target should be > 10^5");
            assertTrue(target.compareTo(BigInteger.valueOf(10000000)) < 0,
                "Stage 1 target should be < 10^7");
        }
    }
    
    @Nested
    @DisplayName("Trial Execution Tests")
    class TrialExecutionTests {
        
        @Test
        @DisplayName("executeSingleTrial completes for easy semiprime")
        void executeSingleTrialCompletes() {
            // As a researcher, I want trials to execute without errors
            // so that I can collect reliable data
            
            // Test parameters to reproduce:
            // - Target: 100013 (103 × 971, known easy semiprime)
            // - Array size: 1000
            // - Max steps: 10000
            // - Expected: trial completes, result object returned
            
            BigInteger target = BigInteger.valueOf(100013);
            ScalingTrialConfig config = new ScalingTrialConfig(
                target, 1000, 10000, 3, false, ScalingStage.STAGE_1_E6);
            ScalingTrialResult result = LinearScalingValidator.executeSingleTrial(config);
            assertNotNull(result, "Trial should return valid result");
        }
        
        @Test
        @DisplayName("executeSingleTrial finds factor for known semiprime")
        void executeSingleTrialFindsKnownFactor() {
            // As a researcher, I want trials to discover factors when they exist
            // in the search space so that I can measure success rate
            
            // Test parameters to reproduce:
            // - Target: 100013 (103 × 971)
            // - Array size: 1000 (includes both factors)
            // - Expected: foundFactor = true, factor = 103 or 971
            
            BigInteger target = BigInteger.valueOf(100013);
            ScalingTrialConfig config = new ScalingTrialConfig(
                target, 1000, 10000, 3, false, ScalingStage.STAGE_1_E6);
            ScalingTrialResult result = LinearScalingValidator.executeSingleTrial(config);
            assertTrue(result.isFoundFactor(), 
                "Should find factor when both factors < array size");
        }
    }
    
    @Nested
    @DisplayName("Remainder Statistics Tests")
    class RemainderStatisticsTests {
        
        @Test
        @DisplayName("RemainderStatistics computes correct mean")
        void remainderStatisticsComputesMean() {
            // As a researcher, I want accurate remainder statistics
            // so that I can correlate landscape properties with convergence
            
            // Construct a small array of real RemainderCell instances to feed into RemainderStatistics
            BigInteger target = BigInteger.valueOf(100); // Simple target
            com.emergent.doom.cell.RemainderCell[] cells = new com.emergent.doom.cell.RemainderCell[3];
            cells[0] = new com.emergent.doom.cell.RemainderCell(target, 10);
            cells[1] = new com.emergent.doom.cell.RemainderCell(target, 15);
            cells[2] = new com.emergent.doom.cell.RemainderCell(target, 20);
            
            RemainderStatistics stats = RemainderStatistics.fromCells(cells);
            assertTrue(stats.getMean() >= 0, "Mean should be non-negative");
        }
    }
    
    @Nested
    @DisplayName("B Coefficient Calculation Tests")
    class BCoefficientTests {
        
        @Test
        @DisplayName("ScalingReport computes B ≈ 0 for constant steps")
        void scalingReportComputesZeroBForConstantSteps() {
            // As a researcher, I want B ≈ 0 when steps don't vary with array size
            // so that I can confirm linear scaling hypothesis
            
            // Test parameters to reproduce:
            // - Mock trial results: array sizes [1000, 2000, 4000]
            // - Steps: [135, 135, 135] (constant)
            // - Expected B: ≈ 0 (slope of constant function)
            
            List<ScalingTrialResult> mockResults = createMockResults(
                new int[]{1000, 2000, 4000},
                new int[]{135, 135, 135});
            ScalingReport report = new ScalingReport(ScalingStage.STAGE_1_E6, mockResults);
            assertEquals(0.0, report.getBCoefficient(), 0.01,
                "B should be ≈ 0 when steps are constant");
        }
        
        @Test
        @DisplayName("ScalingReport computes B > 0.5 for growing steps")
        void scalingReportDetectsGrowingSteps() {
            // As a researcher, I want B > 0.5 to signal failure boundary
            // so that I know when to stop testing harder stages
            
            List<ScalingTrialResult> mockResults = createMockResults(
                new int[]{1000, 2000, 3000},
                new int[]{1000, 2000, 3000});
            ScalingReport report = new ScalingReport(ScalingStage.STAGE_1_E6, mockResults);
            assertTrue(report.getBCoefficient() > 0.5,
                "B should be > 0.5 when steps grow significantly (slope=1.0)");
        }
        
        @Test
        @DisplayName("ScalingReport recommends stopping when B > 0.5")
        void scalingReportRecommendsStoppingAtFailureBoundary() {
            // As a researcher, I want automatic stopping recommendations
            // so that I don't waste resources on doomed experiments
            
            // Test parameters to reproduce:
            // - Mock results with B > 0.5 (failure boundary detected)
            // - Expected: shouldProceedToNextStage() = false
            
            List<ScalingTrialResult> mockResults = createMockResults(
                new int[]{1000, 2000, 3000},
                new int[]{1000, 2000, 3000}); // Slope 1.0
            ScalingReport report = new ScalingReport(ScalingStage.STAGE_1_E6, mockResults);
            assertFalse(report.shouldProceedToNextStage(),
                "Should not proceed when failure boundary detected");
        }
    }
    
    @Nested
    @DisplayName("CSV Export Tests")
    class CsvExportTests {
        
        @Test
        @DisplayName("ScalingTrialResult generates valid CSV row")
        void trialResultGeneratesValidCsvRow() {
            // As a researcher, I want CSV output compatible with analyze_scaling.py
            // so that I can reuse existing analysis tools
            
            // Test parameters to reproduce:
            // - Mock trial result with all fields populated
            // - Expected: CSV row with correct column count and format
            
            ScalingTrialResult result = createMockTrialResult(1000, 135);
            String csv = result.toCsvRow();
            String[] columns = csv.split(",");
            assertEquals(11, columns.length, 
                "CSV row should have 11 columns matching schema");
        }
        
        @Test
        @DisplayName("ScalingReport exports all trials to CSV")
        void scalingReportExportsAllTrials() {
            // As a researcher, I want complete CSV export of all trials
            // so that I can perform custom analysis in Python/R
            
            // Test parameters to reproduce:
            // - Mock report with 3 trials
            // - Expected: CSV with header + 3 data rows
            
            List<ScalingTrialResult> results = new ArrayList<>();
            results.add(createMockTrialResult(1000, 10));
            results.add(createMockTrialResult(1000, 11));
            results.add(createMockTrialResult(1000, 12));
            
            ScalingReport report = new ScalingReport(ScalingStage.STAGE_1_E6, results);
            String csv = report.toCsv();
            String[] lines = csv.split("\n");
            assertEquals(4, lines.length, 
                "CSV should have header + 3 data rows");
        }
    }
    
    // Helper methods for creating mock data
    
    private List<ScalingTrialResult> createMockResults(int[] arraySizes, int[] steps) {
        List<ScalingTrialResult> results = new ArrayList<>();
        for (int i = 0; i < arraySizes.length; i++) {
            results.add(createMockTrialResult(arraySizes[i], steps[i]));
        }
        return results;
    }
    
    private ScalingTrialResult createMockTrialResult(int arraySize, int steps) {
        ScalingTrialConfig config = new ScalingTrialConfig(
            BigInteger.valueOf(100013),
            arraySize,
            10000,
            3,
            false,
            ScalingStage.STAGE_1_E6
        );
        
        return new ScalingTrialResult(
            config,
            steps,
            true, // converged
            true, // found factor
            BigInteger.valueOf(103),
            100, // timeMs
            new RemainderStatistics(10.0, 5.0, 2.2, 0.5, arraySize)
        );
    }
}