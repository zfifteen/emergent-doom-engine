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

        @Test
        @DisplayName("null trajectory returns 0.0")
        void nullTrajectory() {
            assertEquals(0.0, calculator.calculate(null));
        }

        @Test
        @DisplayName("empty trajectory returns 0.0")
        void emptyTrajectory() {
            assertEquals(0.0, calculator.calculate(Collections.emptyList()));
        }

        @Test
        @DisplayName("single value returns 0.0")
        void singleValue() {
            assertEquals(0.0, calculator.calculate(List.of(50.0)));
        }

        @Test
        @DisplayName("two values returns 0.0 (need 3 for dip-and-recovery)")
        void twoValues() {
            assertEquals(0.0, calculator.calculate(Arrays.asList(50.0, 60.0)));
        }
    }

    @Nested
    @DisplayName("Monotonic trajectories (no DG)")
    class MonotonicTrajectories {

        @Test
        @DisplayName("strictly increasing trajectory returns 0.0")
        void strictlyIncreasing() {
            List<Double> trajectory = Arrays.asList(50.0, 60.0, 70.0, 80.0, 90.0, 100.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }

        @Test
        @DisplayName("strictly decreasing trajectory returns 0.0")
        void strictlyDecreasing() {
            List<Double> trajectory = Arrays.asList(100.0, 90.0, 80.0, 70.0, 60.0, 50.0);
            assertEquals(0.0, calculator.calculate(trajectory));
            assertEquals(0, calculator.countDGEvents(trajectory));
        }

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

        @Test
        @DisplayName("simple dip and full recovery: DG = 1.0")
        void simpleFullRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 80 (Δ=+20)
            // DG = 20/20 = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 80.0);
            assertEquals(1.0, calculator.calculate(trajectory), 0.001);
            assertEquals(1, calculator.countDGEvents(trajectory));
        }

        @Test
        @DisplayName("dip with greater recovery: DG > 1.0")
        void greaterRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 100 (Δ=+40)
            // DG = 40/20 = 2.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 100.0);
            assertEquals(2.0, calculator.calculate(trajectory), 0.001);
        }

        @Test
        @DisplayName("dip with partial recovery: DG < 1.0")
        void partialRecovery() {
            // Peak at 80, drop to 60 (Δ=-20), recover to 70 (Δ=+10)
            // DG = 10/20 = 0.5
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 70.0);
            assertEquals(0.5, calculator.calculate(trajectory), 0.001);
        }

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

        @Test
        @DisplayName("average DG with multiple events")
        void averageMultipleEvents() {
            // Two events each with DG = 1.0
            List<Double> trajectory = Arrays.asList(80.0, 60.0, 80.0, 60.0, 80.0);
            assertEquals(1.0, calculator.calculateAveragePerEvent(trajectory), 0.001);
        }

        @Test
        @DisplayName("average DG with no events returns 0.0")
        void averageNoEvents() {
            List<Double> trajectory = Arrays.asList(50.0, 60.0, 70.0, 80.0);
            assertEquals(0.0, calculator.calculateAveragePerEvent(trajectory));
        }
    }

    @Nested
    @DisplayName("Realistic sorting trajectories")
    class RealisticTrajectories {

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
