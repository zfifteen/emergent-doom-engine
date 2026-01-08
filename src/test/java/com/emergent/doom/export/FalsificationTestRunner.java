package com.emergent.doom.export;

import com.emergent.doom.cell.GenericCell;
import com.emergent.doom.probe.Probe;

import java.io.IOException;
import java.util.List;

/**
 * Falsification Test Runner for Trajectory Export Framework
 * 
 * <p>Executes minimal test cases where metrics can be independently verified by hand.
 * This serves as a falsification test to validate the correctness of sortedness
 * and monotonicity error calculations.</p>
 * 
 * <p><strong>Scientific Method:</strong></p>
 * <ol>
 *   <li>Define hypothesis: Framework correctly computes metrics</li>
 *   <li>Design falsification tests with known ground truth</li>
 *   <li>Execute tests and compare with hand calculations</li>
 *   <li>Document discrepancies if any</li>
 * </ol>
 * 
 * <p><strong>Usage:</strong></p>
 * <pre>
 * mvn test-compile exec:java \
 *   -Dexec.mainClass="com.emergent.doom.export.FalsificationTestRunner" \
 *   -Dexec.classpathScope=test
 * </pre>
 */
public class FalsificationTestRunner {
    
    /**
     * Test case data container for falsification testing.
     * 
     * <p>Each test case represents a minimal array configuration with known ground truth
     * for both expected and actual metric values. The test compares the framework's
     * computed metrics against mathematically derived expected values.</p>
     */
    private static class TestCase {
        /** Human-readable name describing this test case */
        String name;
        
        /** Input array configuration to test */
        int[] inputArray;
        
        /** Expected sortedness (Monotonicity) percentage based on manual calculation */
        double expectedSortedness;
        
        /** Expected monotonicity error (number of inversions) based on manual calculation */
        int expectedMonotonicityError;
        
        /** Actual sortedness value computed by the framework */
        double actualSortedness;
        
        /** Actual monotonicity error computed by the framework */
        int actualMonotonicityError;
        
        /** Whether this test case passed (actual matches expected within tolerance) */
        boolean passed;
        
        void printResult() {
            System.out.println("\n" + "=".repeat(80));
            System.out.println("Test Case: " + name);
            System.out.println("=".repeat(80));
            System.out.println("Input Array: " + arrayToString(inputArray));
            System.out.println("\nExpected Metrics:");
            System.out.printf("  Sortedness: %.2f%%%n", expectedSortedness);
            System.out.printf("  Monotonicity Error: %d%n", expectedMonotonicityError);
            System.out.println("\nActual Metrics (from framework):");
            System.out.printf("  Sortedness: %.2f%%%n", actualSortedness);
            System.out.printf("  Monotonicity Error: %d%n", actualMonotonicityError);
            System.out.println("\nVerification:");
            
            double sortednessDiff = Math.abs(actualSortedness - expectedSortedness);
            int errorDiff = Math.abs(actualMonotonicityError - expectedMonotonicityError);
            
            boolean sortednessMatch = sortednessDiff < 0.01; // Within 0.01%
            boolean errorMatch = errorDiff == 0;
            
            System.out.printf("  Sortedness match: %s (diff: %.2f%%)%n", 
                sortednessMatch ? "✓" : "✗", sortednessDiff);
            System.out.printf("  Monotonicity Error match: %s (diff: %d)%n",
                errorMatch ? "✓" : "✗", errorDiff);
            
            passed = sortednessMatch && errorMatch;
            System.out.println("\nResult: " + (passed ? "✓ PASS" : "✗ FAIL"));
        }
        
        private String arrayToString(int[] arr) {
            if (arr.length <= 10) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(arr[i]);
                }
                sb.append("]");
                return sb.toString();
            } else {
                return String.format("[%d,%d,%d,...,%d,%d,%d] (%d elements)",
                    arr[0], arr[1], arr[2],
                    arr[arr.length-3], arr[arr.length-2], arr[arr.length-1],
                    arr.length);
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FALSIFICATION TEST RUNNER");
        System.out.println("Trajectory Export Framework Metric Validation");
        System.out.println("=".repeat(80));
        System.out.println("\nObjective: Verify framework metrics against hand-calculated ground truth");
        System.out.println("Method: Minimal test cases with independently verifiable expected values\n");
        
        TestCase[] testCases = new TestCase[5];
        
        testCases[0] = runTestCase1_SingleElement();
        testCases[1] = runTestCase2_TwoElementsSorted();
        testCases[2] = runTestCase3_TwoElementsReversed();
        testCases[3] = runTestCase4_ThreeElementsOneSwap();
        testCases[4] = runTestCase5_TestRun3Initial();
        
        // Print summary
        System.out.println("\n\n" + "=".repeat(80));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(80));
        
        int passCount = 0;
        for (TestCase tc : testCases) {
            String status = tc.passed ? "✓ PASS" : "✗ FAIL";
            System.out.printf("%s: %s%n", status, tc.name);
            if (tc.passed) passCount++;
        }
        
        System.out.println("\nOverall: " + passCount + "/" + testCases.length + " tests passed");
        
        if (passCount == testCases.length) {
            System.out.println("\n✓ All falsification tests passed - framework metrics are correct");
        } else {
            System.out.println("\n✗ Some tests failed - framework may have bugs or use different metric definitions");
            System.out.println("   Recommendation: Review framework source code and update documentation");
        }
        
        System.out.println("\n" + "=".repeat(80) + "\n");
    }
    
    /**
     * Test Case 1: Single Element (Trivial Case)
     * Expected: 100% sorted, 0 inversions
     */
    private static TestCase runTestCase1_SingleElement() throws IOException {
        TestCase tc = new TestCase();
        tc.name = "Single Element (Trivial Case)";
        tc.inputArray = new int[]{1};
        tc.expectedSortedness = 100.0;  // 1/1 element in correct position
        tc.expectedMonotonicityError = 0;  // No pairs to compare
        
        Probe<GenericCell> probe = new Probe<>();
        GenericCell[] cells = createCellArray(tc.inputArray);
        probe.recordSnapshot(0, cells, 0);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 1, tc.inputArray.length, System.currentTimeMillis()
        );
        
        ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
        tc.actualSortedness = step.sortedness();
        tc.actualMonotonicityError = step.monotonicityError();
        
        String csvPath = "experiments/data/falsification_test_case1.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(csvPath, trajectory);
        
        tc.printResult();
        return tc;
    }
    
    /**
     * Test Case 2: Two Elements (Sorted)
     * Expected: 100% sorted, 0 inversions
     */
    private static TestCase runTestCase2_TwoElementsSorted() throws IOException {
        TestCase tc = new TestCase();
        tc.name = "Two Elements (Sorted)";
        tc.inputArray = new int[]{1, 2};
        tc.expectedSortedness = 100.0;  // 2/2 elements correct
        tc.expectedMonotonicityError = 0;  // No inversions (1<2)
        
        Probe<GenericCell> probe = new Probe<>();
        GenericCell[] cells = createCellArray(tc.inputArray);
        probe.recordSnapshot(0, cells, 0);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 2, tc.inputArray.length, System.currentTimeMillis()
        );
        
        ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
        tc.actualSortedness = step.sortedness();
        tc.actualMonotonicityError = step.monotonicityError();
        
        String csvPath = "experiments/data/falsification_test_case2.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(csvPath, trajectory);
        
        tc.printResult();
        return tc;
    }
    
    /**
     * Test Case 3: Two Elements (Reversed)
     * Expected: 50% monotonicity (first element counts), 1 inversion
     */
    private static TestCase runTestCase3_TwoElementsReversed() throws IOException {
        TestCase tc = new TestCase();
        tc.name = "Two Elements (Reversed)";
        tc.inputArray = new int[]{2, 1};
        tc.expectedSortedness = 50.0;  // Monotonicity: first element counts → 1/2 = 50%
        tc.expectedMonotonicityError = 1;  // One inversion (2>1)
        
        Probe<GenericCell> probe = new Probe<>();
        GenericCell[] cells = createCellArray(tc.inputArray);
        probe.recordSnapshot(0, cells, 0);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 3, tc.inputArray.length, System.currentTimeMillis()
        );
        
        ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
        tc.actualSortedness = step.sortedness();
        tc.actualMonotonicityError = step.monotonicityError();
        
        String csvPath = "experiments/data/falsification_test_case3.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(csvPath, trajectory);
        
        tc.printResult();
        return tc;
    }
    
    /**
     * Test Case 4: Three Elements (One Swap Away)
     * Expected: 66.67% monotonicity, 1 inversion
     */
    private static TestCase runTestCase4_ThreeElementsOneSwap() throws IOException {
        TestCase tc = new TestCase();
        tc.name = "Three Elements (One Swap Away from Sorted)";
        tc.inputArray = new int[]{1, 3, 2};
        // Monotonicity calculation:
        // - Element at index 0 (value 1): counts (first element)
        // - Element at index 1 (value 3): counts (3 >= 1)
        // - Element at index 2 (value 2): doesn't count (2 < 3)
        tc.expectedSortedness = 66.67;  // Monotonicity: 2/3 elements → 66.67%
        tc.expectedMonotonicityError = 1;  // One inversion (3>2)
        
        Probe<GenericCell> probe = new Probe<>();
        GenericCell[] cells = createCellArray(tc.inputArray);
        probe.recordSnapshot(0, cells, 0);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 4, tc.inputArray.length, System.currentTimeMillis()
        );
        
        ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
        tc.actualSortedness = step.sortedness();
        tc.actualMonotonicityError = step.monotonicityError();
        
        String csvPath = "experiments/data/falsification_test_case4.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(csvPath, trajectory);
        
        tc.printResult();
        return tc;
    }
    
    /**
     * Test Case 5: Test Run 3 Initial Configuration
     * This is the critical test that validates the discrepancy found in forensic analysis
     */
    private static TestCase runTestCase5_TestRun3Initial() throws IOException {
        TestCase tc = new TestCase();
        tc.name = "Test Run 3 Initial State (25 elements)";
        tc.inputArray = new int[]{
            20, 19, 18, 17, 16, 15, 14, 13, 12, 11,  // Descending
            10, 9, 8, 7, 6, 5, 4, 3, 2, 1,            // Descending
            21, 22, 23, 24, 25                         // Sorted tail
        };
        
        // Monotonicity calculation:
        // - First element (20): counts (first element)
        // - Elements 1-19 (descending 19...1): don't count (each < predecessor)
        // - Elements 20-24 (21-25): all count (each >= predecessor)
        // Total: 1 + 5 = 6 elements → 6/25 = 24%
        tc.expectedSortedness = 24.0;  // Monotonicity: 6/25 = 24%
        tc.expectedMonotonicityError = 19;  // 19 consecutive inversions
        
        Probe<GenericCell> probe = new Probe<>();
        GenericCell[] cells = createCellArray(tc.inputArray);
        probe.recordSnapshot(0, cells, 0);
        
        ExperimentTrajectory trajectory = TrajectoryBuilder.fromProbe(
            probe, "Test", 0, 5, tc.inputArray.length, System.currentTimeMillis()
        );
        
        ExperimentTrajectory.TrajectoryStep step = trajectory.getSteps().get(0);
        tc.actualSortedness = step.sortedness();
        tc.actualMonotonicityError = step.monotonicityError();
        
        String csvPath = "experiments/data/falsification_test_case5.csv";
        TrajectoryDataExporter.exportTrajectoryToCSV(csvPath, trajectory);
        
        tc.printResult();
        
        // Special analysis for this test case
        System.out.println("\n" + "-".repeat(80));
        System.out.println("SPECIAL ANALYSIS: Test Run 3 Discrepancy");
        System.out.println("-".repeat(80));
        System.out.println("This test case replicates the initial state from Test Run 3.");
        System.out.println("\nForensic Finding:");
        System.out.println("  - Original CSV artifact shows: 24.0% sortedness");
        System.out.println("  - Hand calculation predicts: 20.0% sortedness");
        System.out.println("  - Discrepancy: 4 percentage points");
        System.out.println("\nResolution:");
        if (Math.abs(tc.actualSortedness - 24.0) < 0.01) {
            System.out.println("  ✓ Framework reproduces 24.0% - confirms original artifact");
            System.out.println("  → Framework may use different sortedness definition");
            System.out.println("  → Hand calculation based on 'elements in correct position'");
            System.out.println("  → Framework may use different metric (investigate source code)");
        } else if (Math.abs(tc.actualSortedness - 20.0) < 0.01) {
            System.out.println("  ✓ Framework produces 20.0% - matches hand calculation");
            System.out.println("  → Original CSV artifact may have been from different code version");
            System.out.println("  → Or there was a bug that has been fixed");
        } else {
            System.out.println("  ✗ Framework produces neither 24.0% nor 20.0%");
            System.out.println("  → Unexpected result - requires further investigation");
        }
        System.out.println("-".repeat(80));
        
        return tc;
    }
    
    private static GenericCell[] createCellArray(int[] values) {
        GenericCell[] cells = new GenericCell[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new GenericCell(values[i]);
        }
        return cells;
    }
}
