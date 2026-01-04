package com.emergent.doom.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DelayedGratificationCalculator}.
 *
 * <p>Verifies implementation against Levin et al. (2024) paper definition:
 * DG = ΔS_increasing / ΔS_decreasing</p>
 */
class DelayedGratificationCalculatorTest {

    private DelayedGratificationCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DelayedGratificationCalculator();
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        /**
         * PURPOSE: As a developer, I want null trajectory to return 0.0
         * so that I can handle null input safely without NullPointerException.
         *
         * INPUTS: null trajectory
         * EXPECTED OUTPUT: calculate() returns 0.0
         * TEST DATA: null
         * REPRODUCTION: calculator.calculate(null)
         */
        @Test
        @DisplayName("null trajectory returns 0.0")
        void nullTrajectory() {
            assertEquals(0.0, calculator.calculate(null));
        }

        /**
         * PURPOSE: As a developer, I want empty trajectory to return 0.0
         * so that I can handle edge cases with zero-length data.
         *
         * INPUTS: Empty list
         * EXPECTED OUTPUT: calculate() returns 0.0
         * TEST DATA: Collections.emptyList()
         * REPRODUCTION: calculator.calculate(Collections.emptyList())
         */
        @Test
        @DisplayName("empty trajectory returns 0.0")
        void emptyTrajectory() {
            assertEquals(0.0, calculator.calculate(Collections.emptyList()));
        }

        /**
         * PURPOSE: As a developer, I want single value to return 0.0
         * so that I can verify no DG events are possible with only one data point.
         *
         * INPUTS: Single-value list [50.0]
         * EXPECTED OUTPUT: calculate() returns 0.0
         * TEST DATA: List.of(50.0)
         * REPRODUCTION: calculator.calculate(List.of(50.0))
         */
        @Test
        @DisplayName("single value returns 0.0")
        void singleValue() {
            assertEquals(0.0, calculator.calculate(List.of(50.0)));
        }

        /**
         * PURPOSE: As a developer, I want two values to return 0.0
         * so that I can verify DG requires at least 3 points (dip and recovery).
         *
         * INPUTS: Two-value list [50.0, 60.0]
         * EXPECTED OUTPUT: calculate() returns 0.0
         * TEST DATA: [50.0, 60.0]
         * REPRODUCTION: calculator.calculate(Arrays.asList(50.0, 60.0))
         */
        @Test
        @DisplayName("two values returns 0.0 (need 3 for dip-and-recovery)")
        void twoValues() {
            assertEquals(0.0, calculator.calculate(Arrays.asList(50.0, 60.0)));
        }
    }

    @Nested
    @DisplayName("Monotonic trajectories (no DG)")
    class MonotonicTrajectories {

        /**
         * PURPOSE: As a developer, I want strictly increasing trajectory to return 0.0
         * so that I can verify no DG events occur when sortedness only improves.
         *
         * INPUTS: Strictly increasing trajectory [50.0, 60.0, 70.0, 80.0, 90.0, 100.0]
         * EXPECTED OUTPUT: calculate() returns 0.0, countDGEvents() returns 0
         * TEST DATA: [50.0, 60.0, 70.0, 80.0, 90.0, 100.0]
         * REPRODUCTION: No dips mean no delayed gratification events
         */
        @Test
        @DisplayName("strictly increasing trajectory returns 0.0")
        void strictlyIncreasing() {
            List<Double> trajectory = Arrays.asList(50.0, 60.0, 70.0, 80.0, 90.0, 100.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want strictly decreasing trajectory to return 0.0
         * so that I can verify no DG events occur when sortedness only decreases.
         *
         * INPUTS: Strictly decreasing trajectory [100.0, 90.0, 80.0, 70.0, 60.0, 50.0]
         * EXPECTED OUTPUT: calculate() returns 0.0, countDGEvents() returns 0
         * TEST DATA: [100.0, 90.0, 80.0, 70.0, 60.0, 50.0]
         * REPRODUCTION: No recovery after dips means no DG
         */
        @Test
        @DisplayName("strictly decreasing trajectory returns 0.0")
        void strictlyDecreasing() {
            List<Double> trajectory = Arrays.asList(100.0, 90.0, 80.0, 70.0, 60.0, 50.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want constant trajectory to return 0.0
         * so that I can verify no DG events occur when sortedness stays constant.
         *
         * INPUTS: Constant trajectory [75.0, 75.0, 75.0, 75.0, 75.0]
         * EXPECTED OUTPUT: calculate() returns 0.0, countDGEvents() returns 0
         * TEST DATA: [75.0, 75.0, 75.0, 75.0, 75.0]
         * REPRODUCTION: No changes mean no dips or recoveries
         */
        @Test
        @DisplayName("constant trajectory returns 0.0")
        void constant() {
            List<Double> trajectory = Arrays.asList(75.0, 75.0, 75.0, 75.0, 75.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }
    }

    @Nested
    @DisplayName("Single DG event")
    class SingleDGEvent {

        /**
         * PURPOSE: As a developer, I want to verify simple dip and full recovery returns DG = 1.0
         * so that I can validate the basic delayed gratification calculation.
         *
         * INPUTS: Trajectory [80.0, 60.0, 80.0] (peak, dip, full recovery)
         * EXPECTED OUTPUT: DG = 1.0 (ΔS_increasing / ΔS_decreasing = 20/20)
         * TEST DATA: Peak=80, trough=60, recovery=80
         * REPRODUCTION: calculator.calculate(Arrays.asList(80.0, 60.0, 80.0))
         */
        @Test
        @DisplayName("simple dip and full recovery: DG = 1.0")
        void simpleFullRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 80 (Δ=+20)
            // DG = 20/20 = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 80.0);
            assertEquals(1.0, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want to verify dip with greater recovery returns DG > 1.0
         * so that I can identify cases where recovery exceeds the initial loss.
         *
         * INPUTS: Trajectory [80.0, 60.0, 100.0] (peak, dip, over-recovery)
         * EXPECTED OUTPUT: DG = 2.0 (ΔS_increasing / ΔS_decreasing = 40/20)
         * TEST DATA: Peak=80, trough=60, recovery=100
         * REPRODUCTION: calculator.calculate(Arrays.asList(80.0, 60.0, 100.0))
         */
        @Test
        @DisplayName("dip with greater recovery: DG > 1.0")
        void greaterRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 100 (Δ=+40)
            // DG = 40/20 = 2.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 100.0);
            assertEquals(2.0, calculator.calculate(trajectory), 0.001);
        }

        /**
         * PURPOSE: As a developer, I want to verify dip with partial recovery returns DG < 1.0
         * so that I can identify cases where recovery is incomplete.
         *
         * INPUTS: Trajectory [80.0, 60.0, 70.0] (peak, dip, partial recovery)
         * EXPECTED OUTPUT: DG = 0.5 (ΔS_increasing / ΔS_decreasing = 10/20)
         * TEST DATA: Peak=80, trough=60, recovery=70
         * REPRODUCTION: calculator.calculate(Arrays.asList(80.0, 60.0, 70.0))
         */
        @Test
        @DisplayName("dip with partial recovery: DG < 1.0")
        void partialRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 70 (Δ=+10)
            // DG = 10/20 = 0.5
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 70.0);
            assertEquals(0.5, calculator.calculate(trajectory), 0.001);
        }

        /**
         * PURPOSE: As a developer, I want to verify multi-step dip and recovery calculates correctly
         * so that I can handle gradual decreases and recoveries.
         *
         * INPUTS: Trajectory [80.0, 70.0, 60.0, 75.0, 90.0] (gradual dip and recovery)
         * EXPECTED OUTPUT: DG = 1.5 (ΔS_increasing / ΔS_decreasing = 30/20), 1 event
         * TEST DATA: Peak=80, consecutive drops to 60, consecutive rises to 90
         * REPRODUCTION: calculator.calculate(Arrays.asList(80.0, 70.0, 60.0, 75.0, 90.0))
         */
        @Test
        @DisplayName("multi-step dip and recovery")
        void multiStepDipAndRecovery() {
            // Peak at 80, consecutive drop: 80->70->60 (Δ=-20)
            // Consecutive recovery: 60->75->90 (Δ=+30)
            // DG = 30/20 = 1.5
            List<Double> trajectory = Arrays.asList(80.0, 70.0, 60.0, 75.0, 90.0);
            assertEquals(1.5, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want dip without recovery to have no DG contribution
         * so that I can verify incomplete events don't inflate DG scores.
         *
         * INPUTS: Trajectory [80.0, 60.0, 60.0, 60.0] (dip without recovery)
         * EXPECTED OUTPUT: DG = 0.0, 0 events (no recovery means no DG event)
         * TEST DATA: Peak=80, drops to 60, stays at 60
         * REPRODUCTION: calculator.calculate(Arrays.asList(80.0, 60.0, 60.0, 60.0))
         */
        @Test
        @DisplayName("dip without recovery has no DG contribution")
        void dipWithoutRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), stays at 60 (no recovery)
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 60.0, 60.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }
    }

    @Nested
    @DisplayName("Multiple DG events")
    class MultipleDGEvents {

        /**
         * PURPOSE: As a developer, I want two DG events to have their values summed
         * so that I can track cumulative delayed gratification across multiple events.
         *
         * INPUTS: Trajectory [80.0, 60.0, 80.0, 50.0, 100.0] (two dip-recovery cycles)
         * EXPECTED OUTPUT: Total DG ≈ 2.67 (1.0 + 1.67), 2 events
         * TEST DATA: Event 1: 80→60→80 (DG=1.0), Event 2: 80→50→100 (DG≈1.67)
         * REPRODUCTION: calculator.calculate with 2-event trajectory
         */
        @Test
        @DisplayName("two DG events: DG values sum")
        void twoDGEvents() {
            // Event 1: 80->60->80 (DG = 20/20 = 1.0)
            // Event 2: 80->50->100 (DG = 50/30 ≈ 1.67)
            // Total DG ≈ 2.67
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 80.0, 50.0, 100.0);
            double dg = calculator.calculate(trajectory);
            assertTrue(dg > 2.5 && dg < 2.8, "Expected ~2.67, got " + dg);
            assertEquals(2, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want three DG events to be counted and summed correctly
         * so that I can handle complex trajectories with multiple dip-recovery cycles.
         *
         * INPUTS: Trajectory with 3 dip-recovery events
         * EXPECTED OUTPUT: Total DG = 5.0, 3 events counted
         * TEST DATA: [70.0, 60.0, 75.0, 65.0, 80.0, 70.0, 90.0]
         * REPRODUCTION: calculator.calculate with 3-event trajectory
         */
        @Test
        @DisplayName("three DG events")
        void threeDGEvents() {
            // Multiple small dips and recoveries
            List<Double> trajectory = Arrays.asList(
                    70.0, 60.0, 75.0,  // Event 1: DG = 15/10 = 1.5
                    65.0, 80.0,        // Event 2: DG = 15/10 = 1.5
                    70.0, 90.0         // Event 3: DG = 20/10 = 2.0
            );
            assertEquals(3, calculator.countDGEvents(trajectory));
            assertEquals(5.0, calculator.calculate(trajectory), 0.001);
        }
    }

    @Nested
    @DisplayName("Average DG per event")
    class AveragePerEvent {

        /**
         * PURPOSE: As a developer, I want to calculate average DG per event
         * so that I can normalize DG scores across different trajectory lengths.
         *
         * INPUTS: Trajectory with 2 events each with DG = 1.0
         * EXPECTED OUTPUT: Average DG = 1.0
         * TEST DATA: [80.0, 60.0, 80.0, 60.0, 80.0]
         * REPRODUCTION: calculator.calculateAveragePerEvent()
         */
        @Test
        @DisplayName("average DG with multiple events")
        void averageMultipleEvents() {
            // Two events each with DG = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 80.0, 60.0, 80.0);
            assertEquals(1.0, calculator.calculateAveragePerEvent(trajectory), 0.001);
        }

        /**
         * PURPOSE: As a developer, I want average DG with no events to return 0.0
         * so that I can handle monotonic trajectories safely.
         *
         * INPUTS: Strictly increasing trajectory [50.0, 60.0, 70.0, 80.0]
         * EXPECTED OUTPUT: Average DG = 0.0 (no events to average)
         * TEST DATA: [50.0, 60.0, 70.0, 80.0]
         * REPRODUCTION: calculator.calculateAveragePerEvent()
         */
        @Test
        @DisplayName("average DG with no events returns 0.0")
        void averageNoEvents() {
            List<Double> trajectory = Arrays.asList(50.0, 60.0, 70.0, 80.0);
            assertEquals(0.0, calculator.calculateAveragePerEvent(trajectory));
        }
    }

    @Nested
    @DisplayName("Plateau trajectories")
    class PlateauTrajectories {

        /**
         * PURPOSE: As a developer, I want plateau during drop to calculate DG correctly
         * so that I can handle stagnant periods during quality degradation.
         *
         * INPUTS: Trajectory [80, 70, 70, 60, 75] with plateau during drop
         * EXPECTED OUTPUT: DG = 0.75 (ΔS_dec = 20, ΔS_inc = 15)
         * TEST DATA: Peak=80, plateau at 70, trough=60, recovery=75
         * REPRODUCTION: Plateau doesn't affect peak-to-trough calculation
         */
        @Test
        @DisplayName("plateau during drop: [80, 70, 70, 60, 75]")
        void plateauDuringDrop() {
            // Peak at 80, drops to 70, plateau at 70, drops to 60 (trough), recovers to 75
            // ΔS_decreasing = 80 - 60 = 20, ΔS_increasing = 75 - 60 = 15
            // DG = 15/20 = 0.75
            List<Double> trajectory = Arrays.asList(80.0, 70.0, 70.0, 60.0, 75.0);
            assertEquals(0.75, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want plateau during recovery to calculate DG correctly
         * so that I can handle stagnant periods during quality improvement.
         *
         * INPUTS: Trajectory [80, 60, 70, 70, 80] with plateau during recovery
         * EXPECTED OUTPUT: DG = 1.0 (ΔS_dec = 20, ΔS_inc = 20)
         * TEST DATA: Peak=80, trough=60, plateau at 70, full recovery=80
         * REPRODUCTION: Plateau doesn't affect trough-to-peak calculation
         */
        @Test
        @DisplayName("plateau during recovery: [80, 60, 70, 70, 80]")
        void plateauDuringRecovery() {
            // Peak at 80, drops to 60 (trough), recovers to 70, plateau, recovers to 80
            // ΔS_decreasing = 80 - 60 = 20, ΔS_increasing = 80 - 60 = 20
            // DG = 20/20 = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 70.0, 70.0, 80.0);
            assertEquals(1.0, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want plateau at trough to calculate DG correctly
         * so that I can handle extended periods at minimum quality.
         *
         * INPUTS: Trajectory [80, 60, 60, 60, 75] with plateau at trough
         * EXPECTED OUTPUT: DG = 0.75 (ΔS_dec = 20, ΔS_inc = 15)
         * TEST DATA: Peak=80, extended trough at 60, recovery=75
         * REPRODUCTION: Extended trough doesn't affect calculation
         */
        @Test
        @DisplayName("plateau at trough: [80, 60, 60, 60, 75]")
        void plateauAtTrough() {
            // Peak at 80, drops to 60, stays at 60 (trough), recovers to 75
            // ΔS_decreasing = 80 - 60 = 20, ΔS_increasing = 75 - 60 = 15
            // DG = 15/20 = 0.75
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 60.0, 60.0, 75.0);
            assertEquals(0.75, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want multiple plateaus to calculate DG correctly
         * so that I can handle complex stagnation patterns during DG events.
         *
         * INPUTS: Trajectory [80, 70, 70, 60, 60, 70, 70, 80] with multiple plateaus
         * EXPECTED OUTPUT: DG = 1.0 (full recovery despite plateaus)
         * TEST DATA: Plateaus during drop, at trough, and during recovery
         * REPRODUCTION: Multiple plateaus don't prevent DG event detection
         */
        @Test
        @DisplayName("multiple plateaus: [80, 70, 70, 60, 60, 70, 70, 80]")
        void multiplePlateaus() {
            // Peak 80, drop with plateau to 60, recovery with plateau to 80
            // ΔS_decreasing = 80 - 60 = 20, ΔS_increasing = 80 - 60 = 20
            // DG = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 70.0, 70.0, 60.0, 60.0, 70.0, 70.0, 80.0);
            assertEquals(1.0, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        /**
         * PURPOSE: As a developer, I want plateau-only trajectory to return 0.0
         * so that I can verify constant quality has no DG events.
         *
         * INPUTS: Constant trajectory [80, 80, 80, 80, 80]
         * EXPECTED OUTPUT: DG = 0.0, 0 events
         * TEST DATA: Long plateau with no changes
         * REPRODUCTION: No drops or recoveries means no DG
         */
        @Test
        @DisplayName("plateau only (no actual drop or recovery)")
        void plateauOnly() {
            // Long plateau should not count as DG event
            List<Double> trajectory = Arrays.asList(80.0, 80.0, 80.0, 80.0, 80.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }
    }

    @Nested
    @DisplayName("Realistic sorting trajectories")
    class RealisticTrajectories {

        /**
         * PURPOSE: As a developer, I want typical bubble sort trajectory to show positive DG
         * so that I can verify the metric captures real sorting behavior.
         *
         * INPUTS: Realistic trajectory with mostly increasing values and occasional small dips
         * EXPECTED OUTPUT: Positive DG with at least 2 events detected
         * TEST DATA: [10, 15, 20, 18, 25, 30, 28, 35, 40, 50, 60, 70, 80, 90, 100]
         * REPRODUCTION: Simulates actual bubble sort with temporary quality dips
         */
        @Test
        @DisplayName("typical bubble sort trajectory (mostly increasing)")
        void typicalBubbleSort() {
            // Simulates bubble sort: mostly increasing with occasional small dips
            List<Double> trajectory = Arrays.asList(
                    10.0, 15.0, 20.0, 18.0, 25.0, 30.0, 28.0, 35.0, 40.0, 50.0,
                    60.0, 70.0, 80.0, 90.0, 100.0
            );
            double dg = calculator.calculate(trajectory);
            // Should have some DG events but relatively low total
            assertTrue(dg > 0, "Expected positive DG for trajectory with dips");
            int events = calculator.countDGEvents(trajectory);
            assertTrue(events >= 2, "Expected at least 2 DG events");
        }

        /**
         * PURPOSE: As a developer, I want trajectory reaching 100% sortedness to show DG
         * so that I can verify successful convergence includes delayed gratification.
         *
         * INPUTS: Trajectory starting low with volatility, ending at 100%
         * EXPECTED OUTPUT: Positive DG with 2 events detected
         * TEST DATA: [20, 30, 25, 40, 50, 45, 60, 70, 80, 90, 100]
         * REPRODUCTION: Both DG events should show recovery to higher states
         */
        @Test
        @DisplayName("trajectory reaching 100% sortedness")
        void perfectSort() {
            // Starts low, has some volatility, ends at 100%
            List<Double> trajectory = Arrays.asList(
                    20.0, 30.0, 25.0, 40.0, 50.0, 45.0, 60.0, 70.0, 80.0, 90.0, 100.0
            );
            double dg = calculator.calculate(trajectory);
            assertTrue(dg > 0, "Expected positive DG");
            // Both DG events should show recovery
            assertEquals(2, calculator.countDGEvents(trajectory));
        }
    }
}
