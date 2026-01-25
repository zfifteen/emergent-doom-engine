package com.emergent.doom.domains.graphcoloring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PlateauDetector.
 */
public class PlateauDetectorTest {
    
    @Test
    public void testPlateauDetection() {
        PlateauDetector detector = new PlateauDetector(5, false);  // Small window for testing
        
        // Record improving trajectory (6 ticks to fill window)
        detector.recordTick(100, 105);
        detector.recordTick(95, 100);
        detector.recordTick(90, 95);
        detector.recordTick(85, 90);
        detector.recordTick(80, 85);
        detector.recordTick(75, 80);
        
        assertFalse(detector.isOnPlateau());  // Still improving (current=75, 5 ago=100)
        
        // Record more plateau ticks (keep recording 75)
        detector.recordTick(75, 80);  // Now current=75, 5 ago=95 (still improving from 95 to 75)
        detector.recordTick(75, 80);  // current=75, 5 ago=90
        detector.recordTick(75, 80);  // current=75, 5 ago=85
        detector.recordTick(75, 80);  // current=75, 5 ago=80
        detector.recordTick(75, 80);  // current=75, 5 ago=75 (NOW on plateau!)
        detector.recordTick(75, 80);  // current=75, 5 ago=75 (still on plateau)
        
        assertTrue(detector.isOnPlateau());  // Now on plateau
    }
    
    @Test
    public void testPlateauDuration() {
        PlateauDetector detector = new PlateauDetector(3, false);
        
        // Build to plateau
        for (int i = 0; i < 4; i++) {
            detector.recordTick(50, 55);
        }
        
        assertTrue(detector.isOnPlateau());
        assertTrue(detector.getPlateauDuration() > 0);
    }
    
    @Test
    public void testNotEnoughHistory() {
        PlateauDetector detector = new PlateauDetector(10, false);
        
        detector.recordTick(100, 105);
        detector.recordTick(100, 105);
        
        assertFalse(detector.isOnPlateau());  // Not enough history
    }
    
    @Test
    public void testReset() {
        PlateauDetector detector = new PlateauDetector(5, false);
        
        for (int i = 0; i < 6; i++) {
            detector.recordTick(50, 55);
        }
        
        assertTrue(detector.isOnPlateau());
        
        detector.reset();
        assertFalse(detector.isOnPlateau());
        assertEquals(-1, detector.getCurrentBest());
    }
}
