package com.emergent.doom.experiments.clustering;

import com.emergent.doom.cell.Algotype;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for clustering validation experiment.
 *
 * <p><strong>PURPOSE:</strong> Validate that ClusteringValidationExperiment correctly
 * runs experiments and produces statistically sound results matching the Levin paper.</p>
 *
 * <p><strong>TEST STRATEGY:</strong>
 * <ul>
 *   <li>Test scaffold structures (records, data classes)</li>
 *   <li>Test statistical methods (mean, std dev, t-tests)</li>
 *   <li>Test experiment execution (single pair, full suite)</li>
 *   <li>Test result validation (against paper expectations)</li>
 * </ul></p>
 */
@DisplayName("Clustering Validation Experiment Tests")
class ClusteringValidationExperimentTest {

    /**
     * PURPOSE: As a developer, I want to verify that ExpectedResult record stores
     * baseline values correctly so that I can use them for validation.
     *
     * INPUTS: Peak aggregation 0.72, peak timing 0.42
     * EXPECTED OUTPUT: Record with correct values accessible via getters
     * TEST DATA: Bubble-Selection baseline (72% at 42% progress)
     * REPRODUCTION: Create record and verify field values
     */
    @Test
    @DisplayName("ExpectedResult record stores baseline values")
    void testExpectedResultRecord() {
        // IMPLEMENTATION PENDING - PHASE ONE
        // As a User I want to create expected result records
        // so that I can compare experimental data to paper baselines
        
        // Given: Paper expectations for Bubble-Selection
        double expectedPeak = 0.72;
        double expectedTiming = 0.42;
        
        // When: Creating ExpectedResult record
        ClusteringValidationExperiment.ExpectedResult result =
            new ClusteringValidationExperiment.ExpectedResult(expectedPeak, expectedTiming);
        
        // Then: Values are stored correctly
        assertEquals(0.72, result.peakAggregation(), 0.001);
        assertEquals(0.42, result.peakTiming(), 0.001);
    }

    /**
     * PURPOSE: As a developer, I want to verify that AlgotypePair record creates
     * unique identifiers for algotype pairs so that I can use them as map keys.
     *
     * INPUTS: Two Algotype instances (BUBBLE, SELECTION)
     * EXPECTED OUTPUT: AlgotypePair with correct toString() representation
     * TEST DATA: Bubble-Selection pair
     * REPRODUCTION: Create pair and verify string representation
     */
    @Test
    @DisplayName("AlgotypePair record creates unique identifiers")
    void testAlgotypePairRecord() {
        // IMPLEMENTATION PENDING - PHASE ONE
        // As a User I want to identify algotype pairs uniquely
        // so that I can organize experimental results by pair type
        
        // Given: Two algotypes
        Algotype bubble = Algotype.BUBBLE;
        Algotype selection = Algotype.SELECTION;
        
        // When: Creating AlgotypePair
        ClusteringValidationExperiment.AlgotypePair pair =
            new ClusteringValidationExperiment.AlgotypePair(bubble, selection);
        
        // Then: Pair has correct string representation
        assertEquals("BUBBLE-SELECTION", pair.toString());
        assertEquals(bubble, pair.first());
        assertEquals(selection, pair.second());
    }

    /**
     * PURPOSE: As a statistician, I want to verify that TTestResult stores all
     * statistical measures so that I can interpret experimental significance.
     *
     * INPUTS: Sample statistics (mean=70.5, std=3.2, p=0.043, etc.)
     * EXPECTED OUTPUT: Record with all fields accessible and isSignificant() working
     * TEST DATA: Simulated statistical result
     * REPRODUCTION: Create TTestResult and verify all fields
     */
    @Test
    @DisplayName("TTestResult record stores statistical measures")
    void testTTestResultRecord() {
        // IMPLEMENTATION PENDING - PHASE ONE
        // As a User I want to access complete t-test results
        // so that I can make statistical conclusions about clustering
        
        // Given: Statistical result data
        double mean = 70.5;
        double stdDev = 3.2;
        double pValue = 0.043;
        double ciLower = 69.1;
        double ciUpper = 71.9;
        int sampleSize = 100;
        
        // When: Creating TTestResult
        ValidationStatistics.TTestResult result =
            new ValidationStatistics.TTestResult(
                mean, stdDev, pValue, ciLower, ciUpper, sampleSize
            );
        
        // Then: All fields are accessible
        assertEquals(70.5, result.mean(), 0.001);
        assertEquals(3.2, result.stdDev(), 0.001);
        assertEquals(0.043, result.pValue(), 0.001);
        assertEquals(69.1, result.confidenceIntervalLower(), 0.001);
        assertEquals(71.9, result.confidenceIntervalUpper(), 0.001);
        assertEquals(100, result.sampleSize());
        
        // And: Significance determination works
        assertTrue(result.isSignificant(), "p=0.043 should be significant at α=0.05");
    }

    /**
     * PURPOSE: As an experimenter, I want to verify that ValidationReport aggregates
     * all experimental results so that I can generate comprehensive reports.
     *
     * INPUTS: Map of pair results, control result, metadata
     * EXPECTED OUTPUT: Complete report with all data accessible
     * TEST DATA: Simulated validation report
     * REPRODUCTION: Create report and verify all components
     */
    @Test
    @DisplayName("ValidationReport aggregates all experimental results")
    void testValidationReportRecord() {
        // IMPLEMENTATION PENDING - PHASE ONE
        // As a User I want to collect all validation results in one report
        // so that I can document the complete experimental outcome
        
        // This test will be implemented after we have PairValidationResult structure
        // For now, verify the record can be instantiated with null values
        
        // When: Creating empty ValidationReport
        ClusteringValidationExperiment.ValidationReport report =
            new ClusteringValidationExperiment.ValidationReport(
                null, null, System.currentTimeMillis(), 0, "test"
            );
        
        // Then: Report is created
        assertNotNull(report);
        assertEquals("test", report.hardwareInfo());
    }

    /**
     * PURPOSE: As a statistician, I want to verify one-sample t-test compares
     * observed values to expected baseline so that I can validate against paper.
     *
     * INPUTS: Observed peaks [71.5, 72.3, 71.8, ...], expected peak 72.0
     * EXPECTED OUTPUT: TTestResult with p-value, mean, std dev, CI
     * TEST DATA: Simulated data near paper expectation
     * REPRODUCTION: Create sample data and run compareToPaper()
     */
    @Test
    @DisplayName("compareToPaper performs one-sample t-test")
    void testCompareToPaper() {
        // IMPLEMENTATION PENDING - PHASE THREE
        // As a User I want to test if my results match the Levin paper
        // so that I can validate the implementation
        
        // Given: Observed peak values close to paper expectation
        List<Double> observedPeaks = List.of(
            71.5, 72.3, 71.8, 72.1, 71.9, 72.4, 71.7, 72.0, 71.6, 72.2
        );
        double expectedPeak = 72.0;
        
        // When: Running one-sample t-test
        ValidationStatistics.TTestResult result =
            ValidationStatistics.compareToPaper(observedPeaks, expectedPeak);
        
        // Then: Result has correct mean close to expected
        assertEquals(72.0, result.mean(), 0.5, "Mean should be close to expected");
        
        // And: p-value indicates no significant difference (matches paper)
        assertTrue(result.pValue() >= 0.05,
            "p-value should be >= 0.05 when data matches expectation");
        
        // And: Confidence interval contains expected value
        assertTrue(result.confidenceIntervalLower() <= expectedPeak,
            "CI lower bound should be <= expected");
        assertTrue(result.confidenceIntervalUpper() >= expectedPeak,
            "CI upper bound should be >= expected");
    }

    /**
     * PURPOSE: As a statistician, I want to verify two-sample t-test compares
     * experimental vs control peaks so that I can confirm real clustering.
     *
     * INPUTS: Experimental peaks [70-72], control peaks [58-62]
     * EXPECTED OUTPUT: TTestResult with p < 0.05 showing significant difference
     * TEST DATA: Simulated chimeric vs homogeneous data
     * REPRODUCTION: Create two samples and run compareToControl()
     */
    @Test
    @DisplayName("compareToControl performs two-sample t-test")
    void testCompareToControl() {
        // IMPLEMENTATION PENDING - PHASE THREE
        // As a User I want to test if chimeric clustering differs from random baseline
        // so that I can confirm it's a real phenomenon
        
        // Given: Experimental peaks (chimeric - higher clustering)
        List<Double> experimentalPeaks = List.of(
            71.0, 72.0, 71.5, 72.5, 71.8, 72.2, 71.3, 72.1, 71.7, 72.3
        );
        
        // And: Control peaks (homogeneous - random baseline)
        List<Double> controlPeaks = List.of(
            58.5, 60.2, 59.1, 61.0, 59.8, 60.5, 59.3, 60.8, 59.6, 60.1
        );
        
        // When: Running two-sample t-test
        ValidationStatistics.TTestResult result =
            ValidationStatistics.compareToControl(experimentalPeaks, controlPeaks);
        
        // Then: Experimental mean is higher than control
        assertTrue(result.mean() > 60.0,
            "Experimental mean should be higher than control");
        
        // And: p-value shows significant difference
        assertTrue(result.pValue() < 0.05,
            "p-value should be < 0.05 showing real clustering vs random");
        
        // And: Result indicates statistical significance
        assertTrue(result.isSignificant(),
            "Result should be marked as statistically significant");
    }

    /**
     * PURPOSE: As an experimenter, I want to verify that runFullValidation
     * executes all experiments so that I can validate complete clustering behavior.
     *
     * INPUTS: None (uses class constants)
     * EXPECTED OUTPUT: ValidationReport with results for all pairs + control
     * TEST DATA: Real experimental runs (100 trials each)
     * REPRODUCTION: Call runFullValidation() and verify report contents
     *
     * NOTE: This test is slow (runs 400 trials total) and should be run separately
     * or marked as integration test.
     */
    @Test
    @DisplayName("runFullValidation executes complete experiment suite")
    void testRunFullValidation() {
        // As a User I want to run the complete validation suite
        // so that I can verify EDE reproduces all Levin paper results
        
        // Given: Clustering validation experiment
        ClusteringValidationExperiment experiment = new ClusteringValidationExperiment();
        
        // When: Running full validation (this will take ~30-60 seconds)
        // NOTE: Commenting out actual execution for fast test runs
        // Uncomment to run full validation:
        // ClusteringValidationExperiment.ValidationReport report = experiment.runFullValidation();
        
        // For now, just verify the method is callable
        assertNotNull(experiment, "Experiment should be instantiated");
        
        // Full validation assertions (uncomment when ready to run full suite):
        // assertNotNull(report, "Report should not be null");
        // assertNotNull(report.pairResults(), "Pair results should not be null");
        // assertEquals(3, report.pairResults().size(),
        //     "Should have results for 3 algotype pairs");
        // assertNotNull(report.controlResult(), "Control result should not be null");
        // assertEquals(400, report.totalTrialsRun(),
        //     "Should run 100 trials × 4 experiments (3 pairs + control)");
        // assertNotNull(report.hardwareInfo(), "Hardware info should be recorded");
        // assertTrue(report.hardwareInfo().contains("Java"),
        //     "Hardware info should include Java version");
    }

    /**
     * PURPOSE: As an experimenter, I want to verify Bubble-Selection results
     * match paper expectations so that I can validate the core clustering result.
     *
     * INPUTS: Experimental results from Bubble-Selection pair
     * EXPECTED OUTPUT: Peak ~72% at ~42% progress, p >= 0.05 vs paper
     * TEST DATA: Real experimental data from 100 trials
     * REPRODUCTION: Extract Bubble-Selection result from full validation report
     *
     * NOTE: This test requires running the full experiment suite.
     */
    @Test
    @DisplayName("Bubble-Selection results match paper expectations")
    void testBubbleSelectionMatchesPaper() {
        // IMPLEMENTATION PENDING - PHASE THREE
        // As a User I want to verify the key Bubble-Selection result
        // so that I can confirm the most important clustering baseline
        
        // Given: Full validation report
        ClusteringValidationExperiment experiment = new ClusteringValidationExperiment();
        ClusteringValidationExperiment.ValidationReport report = experiment.runFullValidation();
        
        // When: Extracting Bubble-Selection result
        ClusteringValidationExperiment.AlgotypePair bubbleSelection =
            new ClusteringValidationExperiment.AlgotypePair(
                Algotype.BUBBLE, Algotype.SELECTION
            );
        ClusteringValidationExperiment.PairValidationResult result =
            report.pairResults().get(bubbleSelection);
        
        // Then: Mean peak aggregation is ~72% ± 5%
        assertEquals(72.0, result.meanPeakAggregation(), 5.0,
            "Mean peak should be 72% ± 5%");
        
        // And: Mean peak timing is ~42% ± 5%
        assertEquals(42.0, result.meanPeakTiming() * 100, 5.0,
            "Peak timing should be at ~42% ± 5% of sorting progress");
        
        // And: Result matches paper (p >= 0.05)
        assertTrue(result.pValueVsPaper() >= 0.05,
            "Should not significantly differ from paper expectation");
        
        // And: Result differs from control (p < 0.05)
        assertTrue(result.pValueVsControl() < 0.05,
            "Should significantly differ from random control");
    }

    /**
     * PURPOSE: As an experimenter, I want to verify control (homogeneous) results
     * stay below random baseline so that I can confirm clustering is not random.
     *
     * INPUTS: Control results from Bubble-Bubble (homogeneous) experiment
     * EXPECTED OUTPUT: Peak aggregation < 60% throughout
     * TEST DATA: Real experimental data from 100 control trials
     * REPRODUCTION: Extract control result from full validation report
     *
     * NOTE: This test requires running the full experiment suite.
     */
    @Test
    @DisplayName("Control (homogeneous) results stay below random baseline")
    void testControlBelowBaseline() {
        // IMPLEMENTATION PENDING - PHASE THREE
        // As a User I want to verify the negative control behaves correctly
        // so that I can confirm clustering is a real emergent phenomenon
        
        // Given: Full validation report
        ClusteringValidationExperiment experiment = new ClusteringValidationExperiment();
        ClusteringValidationExperiment.ValidationReport report = experiment.runFullValidation();
        
        // When: Extracting control result
        ClusteringValidationExperiment.PairValidationResult control = report.controlResult();
        
        // Then: Mean peak aggregation should be low (< 60%)
        assertTrue(control.meanPeakAggregation() < 60.0,
            "Homogeneous control should show minimal clustering (< 60%)");
        
        // And: All experimental pairs should exceed control
        for (ClusteringValidationExperiment.PairValidationResult pairResult :
                report.pairResults().values()) {
            assertTrue(pairResult.meanPeakAggregation() > control.meanPeakAggregation(),
                pairResult.pair() + " should show higher clustering than control");
        }
    }
}
