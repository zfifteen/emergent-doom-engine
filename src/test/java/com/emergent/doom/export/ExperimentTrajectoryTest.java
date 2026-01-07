package com.emergent.doom.export;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExperimentTrajectory data structure.
 * 
 * <p>Validates the immutable trajectory and metadata records that encapsulate
 * per-step experiment data for metric dashboard validation.</p>
 */
class ExperimentTrajectoryTest {

    @Nested
    @DisplayName("TrajectoryStep Validation")
    class TrajectoryStepTests {
        
        /**
         * PURPOSE: As a developer, I want valid trajectory steps to be created successfully
         * so that I can build complete experiment trajectories.
         * 
         * INPUTS: Valid step parameters (stepNumber=0, sortedness=75.0, etc.)
         * EXPECTED OUTPUT: TrajectoryStep created without exception
         * TEST DATA: step 0 with realistic metric values
         * REPRODUCTION: new TrajectoryStep(...) with valid parameters
         */
        @Test
        @DisplayName("Valid trajectory step creation succeeds")
        void validTrajectoryStepCreation() {
            assertDoesNotThrow(() -> {
                new ExperimentTrajectory.TrajectoryStep(
                    0,      // stepNumber
                    75.0,   // sortedness
                    5,      // monotonicityError
                    null,   // aggregation (non-chimeric)
                    10,     // cumulativeSwaps
                    50      // cumulativeComparisons
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want chimeric trajectory steps to include aggregation
         * so that I can analyze clustering dynamics.
         * 
         * INPUTS: Step with aggregation=80.0
         * EXPECTED OUTPUT: isChimeric() returns true
         * TEST DATA: step with aggregation value
         * REPRODUCTION: Create step with non-null aggregation, check isChimeric()
         */
        @Test
        @DisplayName("Chimeric trajectory step has aggregation data")
        void chimericStepHasAggregation() {
            ExperimentTrajectory.TrajectoryStep step = 
                new ExperimentTrajectory.TrajectoryStep(
                    0, 75.0, 5, 80.0, 10, 50
                );
            
            assertTrue(step.isChimeric());
            assertEquals(80.0, step.aggregation());
        }
        
        /**
         * PURPOSE: As a developer, I want non-chimeric steps to have null aggregation
         * so that I can distinguish homogeneous experiments.
         * 
         * INPUTS: Step with aggregation=null
         * EXPECTED OUTPUT: isChimeric() returns false
         * TEST DATA: step without aggregation
         * REPRODUCTION: Create step with null aggregation, check isChimeric()
         */
        @Test
        @DisplayName("Non-chimeric trajectory step has null aggregation")
        void nonChimericStepHasNullAggregation() {
            ExperimentTrajectory.TrajectoryStep step = 
                new ExperimentTrajectory.TrajectoryStep(
                    0, 75.0, 5, null, 10, 50
                );
            
            assertFalse(step.isChimeric());
            assertNull(step.aggregation());
        }
        
        /**
         * PURPOSE: As a developer, I want negative step numbers to be rejected
         * so that trajectory data remains valid.
         * 
         * INPUTS: stepNumber=-1
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid negative step number
         * REPRODUCTION: new TrajectoryStep(-1, ...)
         */
        @Test
        @DisplayName("Negative step number throws exception")
        void negativeStepNumberThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.TrajectoryStep(
                    -1, 75.0, 5, null, 10, 50
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want sortedness out of range to be rejected
         * so that metric values remain valid.
         * 
         * INPUTS: sortedness=150.0 (invalid, should be 0-100)
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid sortedness value
         * REPRODUCTION: new TrajectoryStep(..., 150.0, ...)
         */
        @Test
        @DisplayName("Sortedness out of range throws exception")
        void sortednessOutOfRangeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.TrajectoryStep(
                    0, 150.0, 5, null, 10, 50
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want aggregation out of range to be rejected
         * so that metric values remain valid.
         * 
         * INPUTS: aggregation=120.0 (invalid, should be 0-100)
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid aggregation value
         * REPRODUCTION: new TrajectoryStep(..., ..., 120.0, ...)
         */
        @Test
        @DisplayName("Aggregation out of range throws exception")
        void aggregationOutOfRangeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.TrajectoryStep(
                    0, 75.0, 5, 120.0, 10, 50
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want negative cumulative swaps to be rejected
         * so that counter data remains valid.
         * 
         * INPUTS: cumulativeSwaps=-5
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid negative swap count
         * REPRODUCTION: new TrajectoryStep(..., -5, ...)
         */
        @Test
        @DisplayName("Negative cumulative swaps throws exception")
        void negativeCumulativeSwapsThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.TrajectoryStep(
                    0, 75.0, 5, null, -5, 50
                );
            });
        }
    }
    
    @Nested
    @DisplayName("ExperimentMetadata Validation")
    class ExperimentMetadataTests {
        
        /**
         * PURPOSE: As a developer, I want valid metadata to be created successfully
         * so that I can associate context with trajectories.
         * 
         * INPUTS: Valid metadata parameters
         * EXPECTED OUTPUT: ExperimentMetadata created without exception
         * TEST DATA: Bubble algotype, 2 frozen cells, trial 0, size 50
         * REPRODUCTION: new ExperimentMetadata(...) with valid parameters
         */
        @Test
        @DisplayName("Valid experiment metadata creation succeeds")
        void validExperimentMetadataCreation() {
            assertDoesNotThrow(() -> {
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble",               // algotype
                    2,                      // frozenCells
                    0,                      // trialNumber
                    50,                     // arraySize
                    System.currentTimeMillis()  // timestamp
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want null algotype to be rejected
         * so that metadata remains valid.
         * 
         * INPUTS: algotype=null
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: null algotype string
         * REPRODUCTION: new ExperimentMetadata(null, ...)
         */
        @Test
        @DisplayName("Null algotype throws exception")
        void nullAlgotypeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.ExperimentMetadata(
                    null, 2, 0, 50, System.currentTimeMillis()
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want negative frozen cells to be rejected
         * so that metadata remains valid.
         * 
         * INPUTS: frozenCells=-1
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid negative frozen cells
         * REPRODUCTION: new ExperimentMetadata(..., -1, ...)
         */
        @Test
        @DisplayName("Negative frozen cells throws exception")
        void negativeFrozenCellsThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble", -1, 0, 50, System.currentTimeMillis()
                );
            });
        }
        
        /**
         * PURPOSE: As a developer, I want zero or negative array size to be rejected
         * so that metadata remains valid.
         * 
         * INPUTS: arraySize=0
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: invalid zero array size
         * REPRODUCTION: new ExperimentMetadata(..., 0, ...)
         */
        @Test
        @DisplayName("Zero array size throws exception")
        void zeroArraySizeThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble", 2, 0, 0, System.currentTimeMillis()
                );
            });
        }
    }
    
    @Nested
    @DisplayName("ExperimentTrajectory Construction")
    class ExperimentTrajectoryTests {
        
        /**
         * PURPOSE: As a developer, I want to create a complete experiment trajectory
         * so that I can export it for analysis.
         * 
         * INPUTS: List of 3 trajectory steps and metadata
         * EXPECTED OUTPUT: ExperimentTrajectory with 3 steps
         * TEST DATA: 3 steps with progressing sortedness
         * REPRODUCTION: new ExperimentTrajectory(steps, metadata)
         */
        @Test
        @DisplayName("Complete trajectory creation succeeds")
        void completeTrajectoryCreation() {
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
            steps.add(new ExperimentTrajectory.TrajectoryStep(1, 70.0, 6, null, 5, 100));
            steps.add(new ExperimentTrajectory.TrajectoryStep(2, 90.0, 2, null, 12, 150));
            
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble", 0, 0, 50, System.currentTimeMillis()
                );
            
            ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
            
            assertEquals(3, trajectory.getStepCount());
            assertEquals("Bubble", trajectory.getMetadata().algotype());
        }
        
        /**
         * PURPOSE: As a developer, I want trajectory steps to be immutable
         * so that exported data cannot be corrupted.
         * 
         * INPUTS: Trajectory with 2 steps, attempt to modify original list
         * EXPECTED OUTPUT: Original list modification doesn't affect trajectory
         * TEST DATA: 2 steps, add third step after creation
         * REPRODUCTION: Create trajectory, modify original steps list, verify trajectory unchanged
         */
        @Test
        @DisplayName("Trajectory steps are immutable")
        void trajectoryStepsAreImmutable() {
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
            steps.add(new ExperimentTrajectory.TrajectoryStep(1, 70.0, 6, null, 5, 100));
            
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble", 0, 0, 50, System.currentTimeMillis()
                );
            
            ExperimentTrajectory trajectory = new ExperimentTrajectory(steps, metadata);
            
            // Modify original list
            steps.add(new ExperimentTrajectory.TrajectoryStep(2, 90.0, 2, null, 12, 150));
            
            // Trajectory should still have 2 steps
            assertEquals(2, trajectory.getStepCount());
        }
        
        /**
         * PURPOSE: As a developer, I want null steps to be rejected
         * so that trajectory data remains valid.
         * 
         * INPUTS: steps=null
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: null steps list
         * REPRODUCTION: new ExperimentTrajectory(null, metadata)
         */
        @Test
        @DisplayName("Null steps throws exception")
        void nullStepsThrowsException() {
            ExperimentTrajectory.ExperimentMetadata metadata = 
                new ExperimentTrajectory.ExperimentMetadata(
                    "Bubble", 0, 0, 50, System.currentTimeMillis()
                );
            
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory(null, metadata);
            });
        }
        
        /**
         * PURPOSE: As a developer, I want null metadata to be rejected
         * so that trajectory context remains valid.
         * 
         * INPUTS: metadata=null
         * EXPECTED OUTPUT: IllegalArgumentException
         * TEST DATA: null metadata
         * REPRODUCTION: new ExperimentTrajectory(steps, null)
         */
        @Test
        @DisplayName("Null metadata throws exception")
        void nullMetadataThrowsException() {
            List<ExperimentTrajectory.TrajectoryStep> steps = new ArrayList<>();
            steps.add(new ExperimentTrajectory.TrajectoryStep(0, 50.0, 10, null, 0, 50));
            
            assertThrows(IllegalArgumentException.class, () -> {
                new ExperimentTrajectory(steps, null);
            });
        }
    }
}
