package com.emergent.doom.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Utility for generating semiprime numbers for experimentation.
 */
public class SemiPrimeGenerator {

    /**
     * Generates a list of semiprime numbers.
     *
     * @param count             the number of semiprimes to generate
     * @param min               the minimum value of the semiprime
     * @param max               the maximum value of the semiprime
     * @param seed              the seed for random number generation
     * @param maxFactorAllowed  the maximum allowed prime factor
     * @return a list of semiprimes
     */
    public static List<BigInteger> generateSemiPrimes(int count, int min, int max, long seed, int maxFactorAllowed) {
        Random rng = new Random(seed);
        int sieveLimit = Math.max(max, maxFactorAllowed);
        boolean[] isComposite = new boolean[sieveLimit + 1];
        List<Integer> primes = new ArrayList<>();
        for (int i = 2; i <= sieveLimit; i++) {
            if (!isComposite[i]) {
                primes.add(i);
                if ((long)i * i <= sieveLimit) {
                    for (int j = i * i; j <= sieveLimit; j += i) isComposite[j] = true;
                }
            }
        }
        List<Integer> smallPrimes = new ArrayList<>();
        for (Integer p : primes) {
            if (p <= maxFactorAllowed) {
                smallPrimes.add(p);
            }
        }
        List<BigInteger> semiprimes = new ArrayList<>();
        while (semiprimes.size() < count) {
            int p = smallPrimes.get(rng.nextInt(smallPrimes.size()));
            int minQ = (int)Math.ceil((double)min / p);
            int maxQ = (int)Math.floor((double)max / p);
            if (minQ > maxQ || maxQ < 2) continue;
            List<Integer> candidates = new ArrayList<>();
            for (int q : primes) {
                if (q >= minQ && q <= maxQ) candidates.add(q);
            }
            if (candidates.isEmpty()) continue;
            int q = candidates.get(rng.nextInt(candidates.size()));
            semiprimes.add(BigInteger.valueOf((long)p * (long)q));
        }
        return semiprimes;
    }
}
