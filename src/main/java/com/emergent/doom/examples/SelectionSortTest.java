package com.emergent.doom.examples;

import com.emergent.doom.cell.HasAlgotype;
import com.emergent.doom.cell.Algotype;
import com.emergent.doom.group.CellGroup;
import com.emergent.doom.group.CellStatus;

import com.emergent.doom.cell.SelectionCell;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.NoSwapConvergence;

import java.util.Arrays;

/**
 * Simple test for Selection sort algotype
 */
public class SelectionSortTest {

    static class TestCell extends SelectionCell<TestCell> implements com.emergent.doom.group.GroupAwareCell<TestCell>, HasAlgotype {
        public TestCell(int value) {
            super(value);
        }

        // ========== GroupAwareCell stubs ==========
        private CellStatus status = CellStatus.ACTIVE;
        private CellStatus previousStatus = CellStatus.ACTIVE;

        @Override
        public CellGroup<TestCell> getGroup() { return null; }

        @Override
        public void setGroup(CellGroup<TestCell> group) { /* no-op */ }

        @Override
        public int getLeftBoundary() { return 0; }

        @Override
        public void setLeftBoundary(int leftBoundary) { /* no-op */ }

        @Override
        public int getRightBoundary() { return 0; }

        @Override
        public void setRightBoundary(int rightBoundary) { /* no-op */ }

        @Override
        public CellStatus getStatus() { return status; }

        @Override
        public CellStatus getPreviousStatus() { return previousStatus; }

        @Override
        public void setStatus(CellStatus status) { this.status = status; }

        @Override
        public void setPreviousStatus(CellStatus previousStatus) { this.previousStatus = previousStatus; }

        @Override
        public void updateForGroupMerge() {
            // Selection merge behavior is implementation-specific; for this demo cell, do nothing.
        }

        @Override
        public Algotype getAlgotype() { return Algotype.SELECTION; }

        @Override
        public int compareTo(TestCell other) {
            return Integer.compare(this.getValue(), other.getValue());
        }
    }

    public static void main(String[] args) {
        // Create test array: [5, 2, 8, 1, 9] -> should sort to [1, 2, 5, 8, 9]
        TestCell[] cells = new TestCell[] {
            new TestCell(5),
            new TestCell(2),
            new TestCell(8),
            new TestCell(1),
            new TestCell(9)
        };

        FrozenCellStatus frozen = new FrozenCellStatus();
        SwapEngine<TestCell> swapEngine = new SwapEngine<>(frozen);
        Probe<TestCell> probe = new Probe<>();
        probe.setRecordingEnabled(true);
        ConvergenceDetector<TestCell> detector = new NoSwapConvergence<>(10);

        ExecutionEngine<TestCell> engine = new ExecutionEngine<>(cells, swapEngine, probe, detector);

        int[] initialValues = Arrays.stream(cells).mapToInt(TestCell::getValue).toArray();
        int steps = engine.runUntilConvergence(100);
        boolean converged = engine.hasConverged();
        int[] finalValues = Arrays.stream(cells).mapToInt(TestCell::getValue).toArray();

        System.out.println("Selection Sort Test");
        System.out.println("Initial: " + Arrays.toString(initialValues));
        System.out.println("Final:   " + Arrays.toString(finalValues));
        System.out.println("Steps: " + steps);
        System.out.println("Converged: " + converged);

        // Check if sorted
        boolean sorted = true;
        for (int i = 0; i < cells.length - 1; i++) {
            if (cells[i].compareTo(cells[i + 1]) > 0) {
                sorted = false;
                break;
            }
        }
        System.out.println("Sorted: " + sorted);
    }
}
