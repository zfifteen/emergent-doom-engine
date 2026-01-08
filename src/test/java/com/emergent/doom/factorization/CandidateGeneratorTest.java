package com.emergent.doom.factorization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for CandidateGenerator.
 *
 * <p><strong>PURPOSE:</strong> Validate Step 1 success criteria:
 * <ul>
 *   <li>Candidate generators produce expected distributions per strategy</li>
 *   <li>Generated candidates respect bounds [2, sqrt(N)]</li>
 *   <li>Sieve of Eratosthenes produces correct primes</li>
 * </ul>
 */
@DisplayName("CandidateGenerator Tests")
class CandidateGeneratorTest {
    
    private static final int TARGET_N = 143;  // 11 × 13
    private static final int SQRT_N = 11;     // floor(sqrt(143))
    private static final Random FIXED_RANDOM = new Random(42);  // Reproducibility
    
    @Nested
    @DisplayName("Small Primes Strategy")
    class SmallPrimesTests {
        
        @Test
        @DisplayName("Generates correct primes up to sqrt(N)")
        void correctPrimes() {
            // GIVEN: Request for small primes
            int count = 10;
            
            // WHEN: Generating candidates
            List<Integer> candidates = CandidateGenerator.generateSmallPrimes(TARGET_N, count, FIXED_RANDOM);
            
            // THEN: Returns primes [2, 3, 5, 7, 11]
            // (Only 5 primes exist up to sqrt(143) = 11)
            assertNotNull(candidates);
            assertEquals(5, candidates.size(), "Only 5 primes exist up to 11");
            assertEquals(List.of(2, 3, 5, 7, 11), candidates);
        }
        
        @Test
        @DisplayName("Includes true factor 11")
        void includesTrueFactor() {
            // GIVEN: Request for small primes
            List<Integer> candidates = CandidateGenerator.generateSmallPrimes(TARGET_N, 10, FIXED_RANDOM);
            
            // THEN: Contains true factor 11
            assertTrue(candidates.contains(11), "Should include factor 11");
        }
        
        @Test
        @DisplayName("Excludes 13 (above sqrt(N))")
        void excludesFactor13() {
            // GIVEN: Request for small primes
            List<Integer> candidates = CandidateGenerator.generateSmallPrimes(TARGET_N, 10, FIXED_RANDOM);
            
            // THEN: Does NOT contain 13 (13 > sqrt(143))
            assertFalse(candidates.contains(13), "Should exclude 13 (above sqrt)");
        }
        
        @Test
        @DisplayName("All candidates are prime")
        void allPrime() {
            // GIVEN: Generated candidates
            List<Integer> candidates = CandidateGenerator.generateSmallPrimes(TARGET_N, 10, FIXED_RANDOM);
            
            // THEN: All are prime
            for (int candidate : candidates) {
                assertTrue(isPrime(candidate), candidate + " should be prime");
            }
        }
        
        @Test
        @DisplayName("Respects count limit when more primes exist")
        void respectsCount() {
            // GIVEN: Large N with many primes
            int largeN = 10000;  // Many primes below sqrt(10000) = 100
            int count = 5;
            
            // WHEN: Requesting limited count
            List<Integer> candidates = CandidateGenerator.generateSmallPrimes(largeN, count, FIXED_RANDOM);
            
            // THEN: Returns exactly 'count' primes
            assertEquals(count, candidates.size());
            assertEquals(List.of(2, 3, 5, 7, 11), candidates);
        }
    }
    
    @Nested
    @DisplayName("Fermat Near-Sqrt Strategy")
    class FermatNearSqrtTests {
        
        @Test
        @DisplayName("Generates candidates clustered around sqrt(N)")
        void clusteredAroundSqrt() {
            // GIVEN: Request for Fermat-style candidates
            int count = 5;
            
            // WHEN: Generating candidates
            List<Integer> candidates = CandidateGenerator.generateFermatNearSqrt(TARGET_N, count, FIXED_RANDOM);
            
            // THEN: Candidates cluster around sqrt(143) ≈ 11
            assertNotNull(candidates);
            assertFalse(candidates.isEmpty());
            
            // All candidates should be near sqrt
            for (int candidate : candidates) {
                assertTrue(candidate >= SQRT_N - count/2 && candidate <= SQRT_N + count/2,
                    candidate + " should be near sqrt(" + TARGET_N + ") = " + SQRT_N);
            }
        }
        
        @Test
        @DisplayName("Includes true factor 11 (near sqrt)")
        void includesFactor11() {
            // GIVEN: Fermat candidates (should cluster around 11)
            List<Integer> candidates = CandidateGenerator.generateFermatNearSqrt(TARGET_N, 10, FIXED_RANDOM);
            
            // THEN: Should include 11
            assertTrue(candidates.contains(11), "Should include factor 11");
        }
        
        @Test
        @DisplayName("Clamps candidates to valid range [2, sqrt(N)]")
        void clampsToValidRange() {
            // GIVEN: Request for many candidates (might generate out-of-bounds)
            int count = 20;
            
            // WHEN: Generating candidates
            List<Integer> candidates = CandidateGenerator.generateFermatNearSqrt(TARGET_N, count, FIXED_RANDOM);
            
            // THEN: All candidates in [2, 11]
            for (int candidate : candidates) {
                assertTrue(candidate >= 2, candidate + " should be >= 2");
                assertTrue(candidate <= SQRT_N, candidate + " should be <= sqrt(N)");
            }
        }
        
        @Test
        @DisplayName("Removes duplicates caused by clamping")
        void removesDuplicates() {
            // GIVEN: Large count that will cause clamping
            int count = 30;
            
            // WHEN: Generating candidates
            List<Integer> candidates = CandidateGenerator.generateFermatNearSqrt(TARGET_N, count, FIXED_RANDOM);
            
            // THEN: No duplicates
            long uniqueCount = candidates.stream().distinct().count();
            assertEquals(uniqueCount, candidates.size(), "Should have no duplicates");
        }
    }
    
    @Nested
    @DisplayName("Random Sample Strategy")
    class RandomSampleTests {
        
        @Test
        @DisplayName("Generates requested count of candidates")
        void generatesCorrectCount() {
            // GIVEN: Request for random samples
            int count = 10;
            
            // WHEN: Generating candidates
            List<Integer> candidates = CandidateGenerator.generateRandomSample(TARGET_N, count, new Random(42));
            
            // THEN: Returns exactly 'count' candidates
            assertEquals(count, candidates.size());
        }
        
        @Test
        @DisplayName("All candidates in valid range [2, sqrt(N)]")
        void candidatesInValidRange() {
            // GIVEN: Random samples
            int count = 20;
            List<Integer> candidates = CandidateGenerator.generateRandomSample(TARGET_N, count, new Random(42));
            
            // THEN: All in [2, 11]
            for (int candidate : candidates) {
                assertTrue(candidate >= 2, candidate + " should be >= 2");
                assertTrue(candidate <= SQRT_N, candidate + " should be <= sqrt(N)");
            }
        }
        
        @Test
        @DisplayName("Different seeds produce different samples")
        void differentSeedsDifferentSamples() {
            // GIVEN: Two different random seeds
            Random rand1 = new Random(42);
            Random rand2 = new Random(99);
            
            // WHEN: Generating samples
            List<Integer> sample1 = CandidateGenerator.generateRandomSample(TARGET_N, 10, rand1);
            List<Integer> sample2 = CandidateGenerator.generateRandomSample(TARGET_N, 10, rand2);
            
            // THEN: Samples are different (with very high probability)
            assertNotEquals(sample1, sample2, "Different seeds should produce different samples");
        }
        
        @Test
        @DisplayName("Same seed produces reproducible samples")
        void sameSeedReproducible() {
            // GIVEN: Same seed used twice
            Random rand1 = new Random(12345);
            Random rand2 = new Random(12345);
            
            // WHEN: Generating samples
            List<Integer> sample1 = CandidateGenerator.generateRandomSample(TARGET_N, 15, rand1);
            List<Integer> sample2 = CandidateGenerator.generateRandomSample(TARGET_N, 15, rand2);
            
            // THEN: Samples are identical
            assertEquals(sample1, sample2, "Same seed should produce identical samples");
        }
        
        @Test
        @DisplayName("May contain duplicates (realistic random sampling)")
        void mayContainDuplicates() {
            // GIVEN: Many random samples from small range
            Random rand = new Random(777);
            int count = 50;  // Much larger than range [2, 11]
            
            // WHEN: Generating samples
            List<Integer> candidates = CandidateGenerator.generateRandomSample(TARGET_N, count, rand);
            
            // THEN: Likely contains duplicates
            long uniqueCount = candidates.stream().distinct().count();
            assertTrue(uniqueCount < count, "Random sampling should allow duplicates");
        }
    }
    
    @Nested
    @DisplayName("Sieve of Eratosthenes (via Small Primes)")
    class SieveTests {
        
        @Test
        @DisplayName("Generates correct primes up to 20")
        void primesUpTo20() {
            // GIVEN: N with sqrt around 20
            int N = 400;  // sqrt(400) = 20
            
            // WHEN: Generating small primes
            List<Integer> primes = CandidateGenerator.generateSmallPrimes(N, 100, FIXED_RANDOM);
            
            // THEN: Correct primes: [2, 3, 5, 7, 11, 13, 17, 19]
            assertEquals(List.of(2, 3, 5, 7, 11, 13, 17, 19), primes);
        }
        
        @Test
        @DisplayName("Generates correct primes up to 100")
        void primesUpTo100() {
            // GIVEN: N with sqrt = 100
            int N = 10000;
            
            // WHEN: Generating small primes
            List<Integer> primes = CandidateGenerator.generateSmallPrimes(N, 100, FIXED_RANDOM);
            
            // THEN: Should have 25 primes up to 100
            // [2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97]
            assertEquals(25, primes.size());
            assertEquals(2, primes.get(0));
            assertEquals(97, primes.get(24));
        }
        
        @Test
        @DisplayName("No composite numbers in output")
        void noComposites() {
            // GIVEN: Generated primes
            List<Integer> primes = CandidateGenerator.generateSmallPrimes(10000, 100, FIXED_RANDOM);
            
            // THEN: All are prime (no composites)
            for (int p : primes) {
                assertTrue(isPrime(p), p + " should be prime");
            }
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Simple primality test for validation.
     */
    private boolean isPrime(int n) {
        if (n < 2) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}
