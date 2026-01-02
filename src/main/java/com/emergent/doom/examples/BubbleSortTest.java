package com.emergent.doom.examples;

import com.emergent.doom.cell.HasValue;
import com.emergent.doom.cell.HasGroup;
import com.emergent.doom.cell.HasStatus;
import com.emergent.doom.cell.HasAlgotype;
import com.emergent.doom.cell.Algotype;
import com.emergent.doom.group.CellGroup;
import com.emergent.doom.group.CellStatus;

import com.emergent.doom.cell.BubbleCell;
import com.emergent.doom.swap.FrozenCellStatus;
import com.emergent.doom.execution.ExecutionEngine;
import com.emergent.doom.probe.Probe;
import com.emergent.doom.swap.SwapEngine;
import com.emergent.doom.execution.ConvergenceDetector;
import com.emergent.doom.execution.NoSwapConvergence;

import java.util.Arrays;

/**
 * Simple test for Bubble sort algotype
 */
public class BubbleSortTest {

    static class TestCell extends BubbleCell<TestCell> implements HasGroup, HasStatus, HasAlgotype {
        public TestCell(int value) {
            super(value);
        }

        @Override
        public CellGroup<TestCell> getGroup() { return null; }

    public CellStatus getStatus() { return CellStatus.ACTIVE; }

    public CellStatus getPreviousStatus() { return CellStatus.ACTIVE; }

    public void setStatus(CellStatus status) { /* no-op */ }

    public void setPreviousStatus(CellStatus previousStatus) { /* no-op */ }

    public void setGroup(CellGroup<TestCell> group) { /* no-op */ }

    @Override
    public Algotype getAlgotype() { return Algotype.BUBBLE; }

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

        System.out.println("Bubble Sort Test");
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