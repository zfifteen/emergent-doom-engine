package com.emergent.doom.factorization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Utility for generating factor candidates using different strategies.
 *
 * <p><strong>PURPOSE:</strong> Create candidate sets for each FactorStrategy,
 * enabling chimeric population experiments with mixed strategy distributions.</p>
 *
 * <p><strong>DESIGN RATIONALE:</strong></p>
 * <ul>
 *   <li>Strategy-specific: Each method implements intuition behind a strategy</li>
 *   <li>Deterministic: Uses seeded Random for reproducibility</li>
 *   <li>Bounded: All candidates in valid range [2, sqrt(N)]</li>
 *   <li>Documented: Clear comments on generation logic and expected outputs</li>
 * </ul>
 *
 * <p><strong>USAGE EXAMPLE:</strong></p>
 * <pre>
 * Random rand = new Random(42);  // Fixed seed for reproducibility
 * int N = 143;  // Semiprime to factor
 * 
 * List&lt;Integer&gt; smallPrimes = CandidateGenerator.generateSmallPrimes(N, 17, rand);
 * List&lt;Integer&gt; nearSqrt = CandidateGenerator.generateFermatNearSqrt(N, 17, rand);
 * List&lt;Integer&gt; random = CandidateGenerator.generateRandomSample(N, 16, rand);
 * </pre>
 *
 * <p><strong>REFERENCE:</strong> FIRST_NON_SORTING_EXPERIMENT.md Section 3.5</p>
 */
public class CandidateGenerator {
    
    /**
     * Generate candidates using small prime strategy.
     *
     * <p><strong>PURPOSE:</strong> Create first 'count' primes up to sqrt(N).</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>N - the semiprime being factored</li>
     *   <li>count - number of candidates to generate</li>
     *   <li>rand - random instance (currently unused, for API consistency)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Compute sqrtN = floor(sqrt(N))</li>
     *   <li>Generate all primes up to sqrtN using sieve</li>
     *   <li>Take first 'count' primes</li>
     *   <li>If count exceeds available primes, return all available</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> List of prime candidates (may be fewer than count)</p>
     *
     * <p><strong>EXAMPLE:</strong> For N=143, sqrt(143)≈11.96, primes are [2,3,5,7,11].
     * Both true factors 11 and 13 are primes, but 13 > sqrt(143) so only 11 included.</p>
     *
     * @param N the semiprime being factored
     * @param count desired number of candidates
     * @param rand random instance (unused, for API consistency)
     * @return list of prime candidates
     */
    public static List<Integer> generateSmallPrimes(int N, int count, Random rand) {
        int sqrtN = (int) Math.sqrt(N);
        List<Integer> primes = sieveOfEratosthenes(sqrtN);
        
        // Take first 'count' primes (or all if fewer than count exist)
        return primes.stream()
            .limit(count)
            .collect(Collectors.toList());
    }
    
    /**
     * Generate candidates using Fermat near-sqrt strategy.
     *
     * <p><strong>PURPOSE:</strong> Create candidates clustered around sqrt(N),
     * based on Fermat's factorization method intuition.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>N - the semiprime being factored</li>
     *   <li>count - number of candidates to generate</li>
     *   <li>rand - random instance (currently unused, for API consistency)</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Compute sqrtN = floor(sqrt(N))</li>
     *   <li>Generate candidates centered at sqrtN</li>
     *   <li>Range: [sqrtN - count/2, sqrtN + count/2]</li>
     *   <li>Clamp to valid range [2, sqrtN]</li>
     *   <li>Remove duplicates</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> List of candidates near sqrt(N)</p>
     *
     * <p><strong>EXAMPLE:</strong> For N=143, sqrt(143)≈11.96, so candidates cluster
     * around 11-12. Will include true factor 11.</p>
     *
     * @param N the semiprime being factored
     * @param count desired number of candidates
     * @param rand random instance (unused, for API consistency)
     * @return list of candidates near sqrt(N)
     */
    public static List<Integer> generateFermatNearSqrt(int N, int count, Random rand) {
        int sqrtN = (int) Math.sqrt(N);
        List<Integer> candidates = new ArrayList<>();
        
        // Generate candidates centered at sqrtN
        int start = sqrtN - count / 2;
        for (int i = 0; i < count; i++) {
            int candidate = start + i;
            
            // Clamp to valid range [2, sqrtN]
            if (candidate < 2) {
                candidate = 2;
            } else if (candidate > sqrtN) {
                candidate = sqrtN;
            }
            
            // Avoid duplicates
            if (!candidates.contains(candidate)) {
                candidates.add(candidate);
            }
        }
        
        return candidates;
    }
    
    /**
     * Generate candidates using random sampling strategy.
     *
     * <p><strong>PURPOSE:</strong> Create random candidates as baseline control.
     * No structure, pure probabilistic exploration.</p>
     *
     * <p><strong>INPUTS:</strong></p>
     * <ul>
     *   <li>N - the semiprime being factored</li>
     *   <li>count - number of candidates to generate</li>
     *   <li>rand - random instance for reproducibility</li>
     * </ul>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Compute sqrtN = floor(sqrt(N))</li>
     *   <li>Generate 'count' random integers in [2, sqrtN]</li>
     *   <li>Allow duplicates (realistic random sampling)</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> List of random candidates</p>
     *
     * <p><strong>EXAMPLE:</strong> For N=143, generates random integers in [2, 11].
     * May or may not include true factors by chance.</p>
     *
     * @param N the semiprime being factored
     * @param count desired number of candidates
     * @param rand random instance for reproducibility
     * @return list of random candidates
     */
    public static List<Integer> generateRandomSample(int N, int count, Random rand) {
        int sqrtN = (int) Math.sqrt(N);
        List<Integer> candidates = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            // Random integer in [2, sqrtN]
            int candidate = 2 + rand.nextInt(sqrtN - 1);
            candidates.add(candidate);
        }
        
        return candidates;
    }
    
    /**
     * Generate all primes up to limit using Sieve of Eratosthenes.
     *
     * <p><strong>PURPOSE:</strong> Efficiently generate prime numbers for
     * SMALL_PRIMES strategy.</p>
     *
     * <p><strong>INPUTS:</strong> limit - upper bound for prime generation</p>
     *
     * <p><strong>PROCESS:</strong></p>
     * <ol>
     *   <li>Create boolean array of size limit+1</li>
     *   <li>Mark all multiples of each prime as composite</li>
     *   <li>Collect remaining unmarked numbers as primes</li>
     * </ol>
     *
     * <p><strong>OUTPUTS:</strong> List of primes in ascending order</p>
     *
     * <p><strong>COMPLEXITY:</strong> O(n log log n)</p>
     *
     * @param limit upper bound for prime generation
     * @return list of primes up to limit
     */
    private static List<Integer> sieveOfEratosthenes(int limit) {
        if (limit < 2) {
            return new ArrayList<>();
        }
        
        boolean[] isPrime = new boolean[limit + 1];
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }
        
        // Sieve: mark multiples as composite
        for (int p = 2; p * p <= limit; p++) {
            if (isPrime[p]) {
                for (int multiple = p * p; multiple <= limit; multiple += p) {
                    isPrime[multiple] = false;
                }
            }
        }
        
        // Collect primes
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) {
                primes.add(i);
            }
        }
        
        return primes;
    }
}
