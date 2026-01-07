package com.emergent.doom.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExperimentTrajectory data structure.
 * 
 * <p>Validates correct construction, immutability, and accessor methods
 * for trajectory metadata and per-step metrics.</p>
 * 
 * <p><strong>Test Coverage:</strong> 15 tests</p>
 * <ul>
 *   <li>Construction with valid inputs</li>
 *   <li>Metadata accessor methods</li>
 *   <li>Step collection immutability</li>
 *   <li>Aggregation detection</li>
 *   <li>TrajectoryStep accessors</li>
 * </ul>
 */
@DisplayName("ExperimentTrajectory Tests")
class ExperimentTrajectoryTest {
    
    /**
     * PURPOSE: Verify trajectory can be constructed with valid metadata and steps.
     * 
     * INPUTS: Valid algotype, frozen cells, trial number, array size, timestamp, and step list
     * EXPECTED OUTPUT: Non-null trajectory with correct metadata
     * TEST DATA: "Bubble", 0 frozen, trial 0, size 10, current timestamp, 3 steps
     */
    @Test
    @DisplayName("constructs trajectory with valid inputs")
    void constructsTrajectoryWithValidInputs() {
        // Arrange
        String algotype = "Bubble";
        int frozenCells = 0;
        int trialNumber = 0;
        int arraySize = 10;
        long timestamp = System.currentTimeMillis();
        List<ExperimentTrajectory.TrajectoryStep> steps = createSampleSteps(3);
        
        // Act
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            algotype, frozenCells, trialNumber, arraySize, timestamp, steps
        );
        
        // Assert
        assertNotNull(trajectory, "Trajectory should be constructed");
        assertEquals(algotype, trajectory.getAlgotype());
        assertEquals(frozenCells, trajectory.getFrozenCells());
        assertEquals(trialNumber, trajectory.getTrialNumber());
        assertEquals(arraySize, trajectory.getArraySize());
        assertEquals(timestamp, trajectory.getTimestamp());
    }
    
    /**
     * PURPOSE: Verify getSteps() returns immutable copy of steps.
     * 
     * INPUTS: Trajectory with 5 steps
     * EXPECTED OUTPUT: Modification of returned list throws exception
     * TEST DATA: 5 sample trajectory steps
     */
    @Test
    @DisplayName("getSteps() returns immutable list")
    void getStepsReturnsImmutableList() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = createSampleSteps(5);
        ExperimentTrajectory trajectory = createSampleTrajectory(steps);
        
        // Act
        List<ExperimentTrajectory.TrajectoryStep> returnedSteps = trajectory.getSteps();
        
        // Assert
        assertThrows(UnsupportedOperationException.class, () -> {
            returnedSteps.add(createStep(99, 50.0, 5, null, 100, 500));
        }, "Should not be able to modify returned list");
    }
    
    /**
     * PURPOSE: Verify getStepCount() returns correct number of steps.
     * 
     * INPUTS: Trajectory with 10 steps
     * EXPECTED OUTPUT: getStepCount() returns 10
     * TEST DATA: 10 sample trajectory steps
     */
    @Test
    @DisplayName("getStepCount() returns correct count")
    void getStepCountReturnsCorrectCount() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = createSampleSteps(10);
        ExperimentTrajectory trajectory = createSampleTrajectory(steps);
        
        // Act & Assert
        assertEquals(10, trajectory.getStepCount());
    }
    
    /**
     * PURPOSE: Verify hasAggregation() returns false when no aggregation data present.
     * 
     * INPUTS: Trajectory with steps having null aggregation
     * EXPECTED OUTPUT: hasAggregation() returns false
     * TEST DATA: 3 steps with null aggregation values
     */
    @Test
    @DisplayName("hasAggregation() returns false for non-chimeric experiment")
    void hasAggregationReturnsFalseForNonChimeric() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = createSampleSteps(3);
        ExperimentTrajectory trajectory = createSampleTrajectory(steps);
        
        // Act & Assert
        assertFalse(trajectory.hasAggregation(), "Should not have aggregation for non-chimeric");
    }
    
    /**
     * PURPOSE: Verify hasAggregation() returns true when aggregation data present.
     * 
     * INPUTS: Trajectory with steps having non-null aggregation
     * EXPECTED OUTPUT: hasAggregation() returns true
     * TEST DATA: 3 steps with aggregation values
     */
    @Test
    @DisplayName("hasAggregation() returns true for chimeric experiment")
    void hasAggregationReturnsTrueForChimeric() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        steps.add(createStep(0, 10.0, 9, 75.0, 0, 10));
        steps.add(createStep(1, 20.0, 7, 72.5, 5, 25));
        steps.add(createStep(2, 30.0, 5, 70.0, 10, 40));
        ExperimentTrajectory trajectory = createSampleTrajectory(steps);
        
        // Act & Assert
        assertTrue(trajectory.hasAggregation(), "Should have aggregation for chimeric");
    }
    
    /**
     * PURPOSE: Verify TrajectoryStep accessors return correct values.
     * 
     * INPUTS: Single TrajectoryStep with known values
     * EXPECTED OUTPUT: All accessors return expected values
     * TEST DATA: step 5, sortedness 67.5%, error 3, aggregation 50.0%, swaps 25, comparisons 125
     */
    @Test
    @DisplayName("TrajectoryStep accessors return correct values")
    void trajectoryStepAccessorsReturnCorrectValues() {
        // Arrange
        int stepNumber = 5;
        double sortedness = 67.5;
        int monotonicityError = 3;
        Double aggregation = 50.0;
        int cumulativeSwaps = 25;
        int cumulativeComparisons = 125;
        
        // Act
        ExperimentTrajectory.TrajectoryStep step = createStep(
            stepNumber, sortedness, monotonicityError, aggregation, 
            cumulativeSwaps, cumulativeComparisons
        );
        
        // Assert
        assertEquals(stepNumber, step.stepNumber());
        assertEquals(sortedness, step.sortedness(), 0.01);
        assertEquals(monotonicityError, step.monotonicityError());
        assertEquals(aggregation, step.aggregation());
        assertEquals(cumulativeSwaps, step.cumulativeSwaps());
        assertEquals(cumulativeComparisons, step.cumulativeComparisons());
    }
    
    /**
     * PURPOSE: Verify getAlgotype() returns correct algorithm identifier.
     * 
     * INPUTS: Trajectory with "Selection" algotype
     * EXPECTED OUTPUT: getAlgotype() returns "Selection"
     * TEST DATA: "Selection" algotype string
     */
    @Test
    @DisplayName("getAlgotype() returns correct value")
    void getAlgotypeReturnsCorrectValue() {
        // Arrange
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Selection", 1, 2, 50, System.currentTimeMillis(), createSampleSteps(2)
        );
        
        // Act & Assert
        assertEquals("Selection", trajectory.getAlgotype());
    }
    
    /**
     * PURPOSE: Verify getFrozenCells() returns correct count.
     * 
     * INPUTS: Trajectory with 3 frozen cells
     * EXPECTED OUTPUT: getFrozenCells() returns 3
     * TEST DATA: frozenCells = 3
     */
    @Test
    @DisplayName("getFrozenCells() returns correct value")
    void getFrozenCellsReturnsCorrectValue() {
        // Arrange
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Bubble", 3, 0, 30, System.currentTimeMillis(), createSampleSteps(2)
        );
        
        // Act & Assert
        assertEquals(3, trajectory.getFrozenCells());
    }
    
    /**
     * PURPOSE: Verify getTrialNumber() returns correct identifier.
     * 
     * INPUTS: Trajectory with trial number 7
     * EXPECTED OUTPUT: getTrialNumber() returns 7
     * TEST DATA: trialNumber = 7
     */
    @Test
    @DisplayName("getTrialNumber() returns correct value")
    void getTrialNumberReturnsCorrectValue() {
        // Arrange
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Insertion", 0, 7, 20, System.currentTimeMillis(), createSampleSteps(2)
        );
        
        // Act & Assert
        assertEquals(7, trajectory.getTrialNumber());
    }
    
    /**
     * PURPOSE: Verify getArraySize() returns correct size.
     * 
     * INPUTS: Trajectory with array size 100
     * EXPECTED OUTPUT: getArraySize() returns 100
     * TEST DATA: arraySize = 100
     */
    @Test
    @DisplayName("getArraySize() returns correct value")
    void getArraySizeReturnsCorrectValue() {
        // Arrange
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Bubble", 0, 0, 100, System.currentTimeMillis(), createSampleSteps(2)
        );
        
        // Act & Assert
        assertEquals(100, trajectory.getArraySize());
    }
    
    /**
     * PURPOSE: Verify getTimestamp() returns correct value.
     * 
     * INPUTS: Trajectory with specific timestamp
     * EXPECTED OUTPUT: getTimestamp() returns exact timestamp
     * TEST DATA: timestamp = 1704675000000L
     */
    @Test
    @DisplayName("getTimestamp() returns correct value")
    void getTimestampReturnsCorrectValue() {
        // Arrange
        long timestamp = 1704675000000L;
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Bubble", 0, 0, 10, timestamp, createSampleSteps(2)
        );
        
        // Act & Assert
        assertEquals(timestamp, trajectory.getTimestamp());
    }
    
    /**
     * PURPOSE: Verify empty trajectory works correctly.
     * 
     * INPUTS: Trajectory with empty step list
     * EXPECTED OUTPUT: getStepCount() returns 0, hasAggregation() returns false
     * TEST DATA: Empty step list
     */
    @Test
    @DisplayName("handles empty trajectory")
    void handlesEmptyTrajectory() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> emptySteps = new ArrayList<>();
        ExperimentTrajectory trajectory = new ExperimentTrajectory(
            "Bubble", 0, 0, 10, System.currentTimeMillis(), emptySteps
        );
        
        // Act & Assert
        assertEquals(0, trajectory.getStepCount());
        assertFalse(trajectory.hasAggregation());
    }
    
    /**
     * PURPOSE: Verify trajectory with single step works correctly.
     * 
     * INPUTS: Trajectory with exactly one step
     * EXPECTED OUTPUT: getStepCount() returns 1, getSteps() contains single step
     * TEST DATA: Single step at step 0
     */
    @Test
    @DisplayName("handles single-step trajectory")
    void handlesSingleStepTrajectory() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> singleStep = createSampleSteps(1);
        ExperimentTrajectory trajectory = createSampleTrajectory(singleStep);
        
        // Act & Assert
        assertEquals(1, trajectory.getStepCount());
        assertEquals(1, trajectory.getSteps().size());
    }
    
    /**
     * PURPOSE: Verify TrajectoryStep with null aggregation works correctly.
     * 
     * INPUTS: TrajectoryStep with aggregation = null
     * EXPECTED OUTPUT: aggregation() returns null
     * TEST DATA: Step with null aggregation
     */
    @Test
    @DisplayName("TrajectoryStep handles null aggregation")
    void trajectoryStepHandlesNullAggregation() {
        // Arrange & Act
        ExperimentTrajectory.TrajectoryStep step = createStep(0, 50.0, 5, null, 10, 50);
        
        // Assert
        assertNull(step.aggregation(), "Aggregation should be null for non-chimeric");
    }
    
    /**
     * PURPOSE: Verify trajectory preserves step order.
     * 
     * INPUTS: Trajectory with 5 steps in specific order
     * EXPECTED OUTPUT: getSteps() returns steps in same order
     * TEST DATA: 5 steps with stepNumbers 0, 1, 2, 3, 4
     */
    @Test
    @DisplayName("preserves step order")
    void preservesStepOrder() {
        // Arrange
        List<ExperimentTrajectory.TrajectoryStep> steps = createSampleSteps(5);
        ExperimentTrajectory trajectory = createSampleTrajectory(steps);
        
        // Act
        List<ExperimentTrajectory.TrajectoryStep> returnedSteps = trajectory.getSteps();
        
        // Assert
        for (int i = 0; i < 5; i++) {
            assertEquals(i, returnedSteps.get(i).stepNumber(), 
                "Step order should be preserved");
        }
    }
    
    // ============ Helper Methods ============
    
    /**
     * Creates a sample trajectory with given steps.
     */
    private ExperimentTrajectory createSampleTrajectory(List<ExperimentTrajectory.TrajectoryStep> steps) {
        return new ExperimentTrajectory(
            "Bubble",
            0,
            0,
            10,
            System.currentTimeMillis(),
            steps
        );
    }
    
    /**
     * Creates a list of sample trajectory steps.
     */
    private List<ExperimentTrajectory.TrajectoryStep> createSampleSteps(int count) {
        List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double sortedness = 10.0 + (i * 10.0);
            int monotonicityError = Math.max(0, 9 - i);
            int cumulativeSwaps = i * 5;
            int cumulativeComparisons = i * 25;
            steps.add(createStep(i, sortedness, monotonicityError, null, 
                cumulativeSwaps, cumulativeComparisons));
        }
        return steps;
    }
    
    /**
     * Creates a single TrajectoryStep with given values.
     */
    private ExperimentTrajectory.TrajectoryStep createStep(
            int stepNumber,
            double sortedness,
            int monotonicityError,
            Double aggregation,
            int cumulativeSwaps,
            int cumulativeComparisons) {
        return new ExperimentTrajectory.TrajectoryStep(
            stepNumber,
            sortedness,
            monotonicityError,
            aggregation,
            cumulativeSwaps,
            cumulativeComparisons
        );
    }
}
