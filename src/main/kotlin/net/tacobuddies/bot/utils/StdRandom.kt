package net.tacobuddies.bot.utils

import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.CharArray
import kotlin.Deprecated
import kotlin.DoubleArray
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.IntArray
import kotlin.Long
import kotlin.also
import kotlin.assert
import kotlin.math.*
import kotlin.require
import kotlin.requireNotNull

/**
 * The `StdRandom` class provides static methods for generating
 * random number from various discrete and continuous distributions,
 * including uniform, Bernoulli, geometric, Gaussian, exponential, Pareto,
 * Poisson, and Cauchy. It also provides method for shuffling an
 * array or subarray and generating random permutations.
 *
 *
 * By convention, all intervals are half open. For example,
 * `uniform(-1.0, 1.0)` returns a random number between
 * `-1.0` (inclusive) and `1.0` (exclusive).
 * Similarly, `shuffle(a, lo, hi)` shuffles the `hi - lo`
 * elements in the array `a[]`, starting at index `lo`
 * (inclusive) and ending at index `hi` (exclusive).
 *
 *
 * For additional documentation,
 * see [Section 2.2](https://introcs.cs.princeton.edu/22library) of
 * *Computer Science: An Interdisciplinary Approach*
 * by Robert Sedgewick and Kevin Wayne.
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
object StdRandom {
    private val seed = System.currentTimeMillis()
    private val random: Random = Random(seed)

    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return a random real number uniformly in [0, 1)
     */
    fun uniform(): Double {
        return random.nextDouble()
    }

    /**
     * Returns a random integer uniformly in [0, n).
     *
     * @param n number of possible integers
     * @return a random integer uniformly between 0 (inclusive) and `n` (exclusive)
     * @throws IllegalArgumentException if `n <= 0`
     */
    fun uniform(n: Int): Int {
        require(n > 0) { "argument must be positive: $n" }
        return random.nextInt(n)
    }


    /**
     * Returns a random long integer uniformly in [0, n).
     *
     * @param n number of possible `long` integers
     * @return a random long integer uniformly between 0 (inclusive) and `n` (exclusive)
     * @throws IllegalArgumentException if `n <= 0`
     */
    fun uniform(n: Long): Long {
        require(n > 0L) { "argument must be positive: $n" }

        // https://docs.oracle.com/javase/8/docs/api/java/util/Random.html#longs-long-long-long-
        var r: Long = random.nextLong()
        val m = n - 1

        // power of two
        if ((n and m) == 0L) {
            return r and m
        }

        // reject over-represented candidates
        var u = r ushr 1
        while (u + m - ((u % n).also { r = it }) < 0L) {
            u = random.nextLong() ushr 1
        }
        return r
    }

    ///////////////////////////////////////////////////////////////////////////
    //  STATIC METHODS BELOW RELY ON JAVA.UTIL.RANDOM ONLY INDIRECTLY VIA
    //  THE STATIC METHODS ABOVE.
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Returns a random real number uniformly in [0, 1).
     *
     * @return     a random real number uniformly in [0, 1)
     */
    @Deprecated("Replaced by {@link #uniform()}.",
        ReplaceWith("uniform()", "net.tacobuddies.bot.utils.StdRandom.uniform")
    )
    fun random(): Double {
        return uniform()
    }

    /**
     * Returns a random integer uniformly in [a, b).
     *
     * @param  a the left endpoint
     * @param  b the right endpoint
     * @return a random integer uniformly in [a, b)
     * @throws IllegalArgumentException if `b <= a`
     * @throws IllegalArgumentException if `b - a >= Integer.MAX_VALUE`
     */
    fun uniform(a: Int, b: Int): Int {
        require(!((b <= a) || (b.toLong() - a >= Int.MAX_VALUE))) { "invalid range: [$a, $b)" }
        return a + uniform(b - a)
    }

    /**
     * Returns a random real number uniformly in [a, b).
     *
     * @param  a the left endpoint
     * @param  b the right endpoint
     * @return a random real number uniformly in [a, b)
     * @throws IllegalArgumentException unless `a < b`
     */
    fun uniform(a: Double, b: Double): Double {
        require((a < b)) { "invalid range: [$a, $b)" }
        return a + uniform() * (b - a)
    }

    /**
     * Returns a random boolean from a Bernoulli distribution with success
     * probability *p*.
     *
     * @param  p the probability of returning `true`
     * @return `true` with probability `p` and
     * `false` with probability `1 - p`
     * @throws IllegalArgumentException unless `0`  `p`  `1.0`
     */
    /**
     * Returns a random boolean from a Bernoulli distribution with success
     * probability 1/2.
     *
     * @return `true` with probability 1/2 and
     * `false` with probability 1/2
     */
    @JvmOverloads
    fun bernoulli(p: Double = 0.5): Boolean {
        require((p in 0.0..1.0)) { "probability p must be between 0.0 and 1.0: $p" }
        return uniform() < p
    }

    /**
     * Returns a random real number from a standard Gaussian distribution.
     *
     * @return a random real number from a standard Gaussian distribution
     * (mean 0 and standard deviation 1).
     */
    fun gaussian(): Double {
        // use the polar form of the Box-Muller transform
        var r: Double
        var x: Double
        var y: Double
        do {
            x = uniform(-1.0, 1.0)
            y = uniform(-1.0, 1.0)
            r = x * x + y * y
        } while (r >= 1 || r == 0.0)
        return x * sqrt(-2 * ln(r) / r)

        // Remark:  y * Math.sqrt(-2 * Math.log(r) / r)
        // is an independent random gaussian
    }

    /**
     * Returns a random real number from a Gaussian distribution with mean
     * and standard deviation .
     *
     * @param  mu the mean
     * @param  sigma the standard deviation
     * @return a real number distributed according to the Gaussian distribution
     * with mean `mu` and standard deviation `sigma`
     */
    fun gaussian(mu: Double, sigma: Double): Double {
        return mu + sigma * gaussian()
    }

    /**
     * Returns a random real number from a Gaussian distribution with mean
     * and standard deviation .
     *
     * @param  min the minimum range
     * @param  max the maximum range
     * @param  mu the mean
     * @param  sigma the standard deviation
     * @return a real number distributed according to the Gaussian distribution
     * with mean `mu` and standard deviation `sigma`
     */
    fun gaussian(min: Double, max: Double, mu: Double, sigma: Double): Double {
        // use the polar form of the Box-Muller transform
        var x: Double
        do {
            x = gaussian(mu, sigma)
        } while (x < min || x > max)
        return x
    }

    /**
     * Returns a random real number from a Gaussian Mixture distribution with means &mus,;
     * standard deviations &sigmas and probabilities for selecting each distribution .
     *
     * @param  min the minimum range
     * @param  max the maximum range
     * @param  mus list of the means for each gaussian
     * @param  sigmas list of the standard deviations for each gaussian
     * @param  probabilities list of the probabilities of selecting each gaussian
     * @return a real number distributed according to the Gaussian Mixture distribution
     * with means `mus`, standard deviations `sigmas` and probabilities of selecting each gaussian `probabilities`
     */
    fun gaussianMixture(
        min: Double,
        max: Double,
        mus: DoubleArray?,
        sigmas: DoubleArray?,
        probabilities: DoubleArray?
    ): Double {
        require(!(mus == null || sigmas == null || probabilities == null || mus.size != sigmas.size || sigmas.size != probabilities.size)) { "mus, sigmas, or ratios not valid" }
        val i: Int = discrete(probabilities)

        return gaussian(min, max, mus[i], sigmas[i])
    }

    /**
     * Returns a random integer from a geometric distribution with success
     * probability *p*.
     * The integer represents the number of independent trials
     * before the first success.
     *
     * @param  p the parameter of the geometric distribution
     * @return a random integer from a geometric distribution with success
     * probability `p`; or `Integer.MAX_VALUE` if
     * `p` is (nearly) equal to `1.0`.
     * @throws IllegalArgumentException unless `p >= 0.0` and `p <= 1.0`
     */
    fun geometric(p: Double): Int {
        require((p >= 0)) { "probability p must be greater than 0: $p" }
        require((p <= 1.0)) { "probability p must not be larger than 1: $p" }
        // using algorithm given by Knuth
        return ceil(ln(uniform()) / ln(1.0 - p)).toInt()
    }

    /**
     * Returns a random integer from a Poisson distribution with mean .
     *
     * @param  lambda the mean of the Poisson distribution
     * @return a random integer from a Poisson distribution with mean `lambda`
     * @throws IllegalArgumentException unless `lambda > 0.0` and not infinite
     */
    fun poisson(lambda: Double): Int {
        require((lambda > 0.0)) { "lambda must be positive: $lambda" }
        require(!lambda.isInfinite()) { "lambda must not be infinite: $lambda" }
        // using algorithm given by Knuth
        // see http://en.wikipedia.org/wiki/Poisson_distribution
        var k = 0
        var p = 1.0
        val expLambda = kotlin.math.exp(-lambda)
        do {
            k++
            p *= uniform()
        } while (p >= expLambda)
        return k - 1
    }

    /**
     * Returns a random real number from a Pareto distribution with
     * shape parameter .
     *
     * @param  alpha shape parameter
     * @return a random real number from a Pareto distribution with shape
     * parameter `alpha`
     * @throws IllegalArgumentException unless `alpha > 0.0`
     */
    /**
     * Returns a random real number from the standard Pareto distribution.
     *
     * @return a random real number from the standard Pareto distribution
     */
    @JvmOverloads
    fun pareto(alpha: Double = 1.0): Double {
        require((alpha > 0.0)) { "alpha must be positive: $alpha" }
        return (1 - uniform()).pow(-1.0 / alpha) - 1.0
    }

    /**
     * Returns a random real number from the Cauchy distribution.
     *
     * @return a random real number from the Cauchy distribution.
     */
    fun cauchy(): Double {
        return tan(Math.PI * (uniform() - 0.5))
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param  probabilities the probability of occurrence of each integer
     * @return a random integer from a discrete distribution:
     * `i` with probability `probabilities[i]`
     * @throws IllegalArgumentException if `probabilities` is `null`
     * @throws IllegalArgumentException if sum of array entries is not (very nearly) equal to `1.0`
     * @throws IllegalArgumentException unless `probabilities[i] >= 0.0` for each index `i`
     */
    fun discrete(probabilities: DoubleArray?): Int {
        requireNotNull(probabilities) { "argument array is null" }
        val EPSILON = 1.0E-14
        var sum = 0.0
        for (i in probabilities.indices) {
            require((probabilities[i] >= 0.0)) { "array entry " + i + " must be nonnegative: " + probabilities[i] }
            sum += probabilities[i]
        }
        require(!(sum > 1.0 + EPSILON || sum < 1.0 - EPSILON)) { "sum of array entries does not approximately equal 1.0: $sum" }

        // the for loop may not return a value when both r is (nearly) 1.0 and when the
        // cumulative sum is less than 1.0 (as a result of floating-point roundoff error)
        while (true) {
            val r: Double = uniform()
            sum = 0.0
            for (i in probabilities.indices) {
                sum = sum + probabilities[i]
                if (sum > r) return i
            }
        }
    }

    /**
     * Returns a random integer from the specified discrete distribution.
     *
     * @param  frequencies the frequency of occurrence of each integer
     * @return a random integer from a discrete distribution:
     * `i` with probability proportional to `frequencies[i]`
     * @throws IllegalArgumentException if `frequencies` is `null`
     * @throws IllegalArgumentException if all array entries are `0`
     * @throws IllegalArgumentException if `frequencies[i]` is negative for any index `i`
     * @throws IllegalArgumentException if sum of frequencies exceeds `Integer.MAX_VALUE` (2<sup>31</sup> - 1)
     */
    fun discrete(frequencies: IntArray?): Int {
        requireNotNull(frequencies) { "argument array is null" }
        var sum: Long = 0
        for (i in frequencies.indices) {
            require(frequencies[i] >= 0) { "array entry " + i + " must be nonnegative: " + frequencies[i] }
            sum += frequencies[i].toLong()
        }
        require(sum != 0L) { "at least one array entry must be positive" }
        require(sum < Int.MAX_VALUE) { "sum of frequencies overflows an int" }

        // pick index i with probabilitity proportional to frequency
        val r: Double = uniform(sum.toInt()).toDouble()
        sum = 0
        for (i in frequencies.indices) {
            sum += frequencies[i].toLong()
            if (sum > r) return i
        }

        // can't reach here
        assert(false)
        return -1
    }

    /**
     * Returns a random real number from an exponential distribution
     * with rate .
     *
     * @param  lambda the rate of the exponential distribution
     * @return a random real number from an exponential distribution with
     * rate `lambda`
     * @throws IllegalArgumentException unless `lambda > 0.0`
     */
    fun exp(lambda: Double): Double {
        require((lambda > 0.0)) { "lambda must be positive: $lambda" }
        return -ln(1 - uniform()) / lambda
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: Array<Any?>) {
        validateNotNull(a)
        val n = a.size
        for (i in 0 until n) {
            val r: Int = i + uniform(n - i) // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: DoubleArray) {
        validateNotNull(a)
        val n = a.size
        for (i in 0 until n) {
            val r: Int = i + uniform(n - i) // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: IntArray) {
        validateNotNull(a)
        val n = a.size
        for (i in 0 until n) {
            val r: Int = i + uniform(n - i) // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified array in uniformly random order.
     *
     * @param  a the array to shuffle
     * @throws IllegalArgumentException if `a` is `null`
     */
    fun shuffle(a: CharArray) {
        validateNotNull(a)
        val n = a.size
        for (i in 0 until n) {
            val r: Int = i + uniform(n - i) // between i and n-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param  a the array to shuffle
     * @param  lo the left endpoint (inclusive)
     * @param  hi the right endpoint (exclusive)
     * @throws IllegalArgumentException if `a` is `null`
     * @throws IllegalArgumentException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: Array<Any?>, lo: Int, hi: Int) {
        validateNotNull(a)
        validateSubarrayIndices(lo, hi, a.size)

        for (i in lo until hi) {
            val r: Int = i + uniform(hi - i) // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param  a the array to shuffle
     * @param  lo the left endpoint (inclusive)
     * @param  hi the right endpoint (exclusive)
     * @throws IllegalArgumentException if `a` is `null`
     * @throws IllegalArgumentException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: DoubleArray, lo: Int, hi: Int) {
        validateNotNull(a)
        validateSubarrayIndices(lo, hi, a.size)

        for (i in lo until hi) {
            val r: Int = i + uniform(hi - i) // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Rearranges the elements of the specified subarray in uniformly random order.
     *
     * @param  a the array to shuffle
     * @param  lo the left endpoint (inclusive)
     * @param  hi the right endpoint (exclusive)
     * @throws IllegalArgumentException if `a` is `null`
     * @throws IllegalArgumentException unless `(0 <= lo) && (lo < hi) && (hi <= a.length)`
     */
    fun shuffle(a: IntArray, lo: Int, hi: Int) {
        validateNotNull(a)
        validateSubarrayIndices(lo, hi, a.size)

        for (i in lo until hi) {
            val r: Int = i + uniform(hi - i) // between i and hi-1
            val temp = a[i]
            a[i] = a[r]
            a[r] = temp
        }
    }

    /**
     * Returns a uniformly random permutation of *n* elements.
     *
     * @param  n number of elements
     * @throws IllegalArgumentException if `n` is negative
     * @return an array of length `n` that is a uniformly random permutation
     * of `0`, `1`, ..., `n-1`
     */
    fun permutation(n: Int): IntArray {
        require(n >= 0) { "argument is negative" }
        val perm = IntArray(n)
        for (i in 0 until n) perm[i] = i
        shuffle(perm)
        return perm
    }

    /**
     * Returns a uniformly random permutation of *k* of *n* elements.
     *
     * @param  n number of elements
     * @param  k number of elements to select
     * @throws IllegalArgumentException if `n` is negative
     * @throws IllegalArgumentException unless `0 <= k <= n`
     * @return an array of length `k` that is a uniformly random permutation
     * of `k` of the elements from `0`, `1`, ..., `n-1`
     */
    fun permutation(n: Int, k: Int): IntArray {
        require(n >= 0) { "argument is negative" }
        require(!(k < 0 || k > n)) { "k must be between 0 and n" }
        val perm = IntArray(k)
        for (i in 0 until k) {
            val r: Int = uniform(i + 1) // between 0 and i
            perm[i] = perm[r]
            perm[r] = i
        }
        for (i in k until n) {
            val r: Int = uniform(i + 1) // between 0 and i
            if (r < k) perm[r] = i
        }
        return perm
    }

    // throw an IllegalArgumentException if x is null
    // (x can be of type Object[], double[], int[], ...)
    private fun validateNotNull(x: Any?) {
        requireNotNull(x) { "argument is null" }
    }

    // throw an exception unless 0 <= lo <= hi <= length
    private fun validateSubarrayIndices(lo: Int, hi: Int, length: Int) {
        require(!(lo < 0 || hi > length || lo > hi)) { "subarray indices out of bounds: [$lo, $hi)" }
    }
}