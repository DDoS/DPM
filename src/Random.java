/**
 * A simple random generator. It's not meant to be perfect, but just good enough for testing. This is a replacement for Lejo's broken random. This is a linear congruential random number generator
 * based on the OpenJDK implementation or Random, and so uses the same constants and seed size.
 */
public class Random {
    private static final int BITS = 48;
    private static final long MUL = 0x5DEECE66DL;
    private static final long ADD = 0xBL;
    private static final long MASK = (1L << BITS) - 1;
    private long seed;

    /**
     * Constructs a new random seeded with the current system time.
     */
    public Random() {
        this.seed = (System.currentTimeMillis() ^ MUL) & MASK;
    }

    /**
     * Generates the desired number of bits, with a maximum of 32 and minimum of 1.
     *
     * @param bits The number of bits to generate, between 1 and 32
     * @return The generated bits
     */
    public int nextBits(int bits) {
        seed = (seed * MUL + ADD) & MASK;
        return (int) (seed >>> BITS - bits);
    }

    /**
     * Generates 1 bit and returns it as a boolean.
     *
     * @return The generated boolean
     */
    public boolean nextBoolean() {
        return nextBits(1) == 1;
    }

    /**
     * Generates 32 bits, then performs a modulo to wrap in to the desired range, [0, n). This won't provide a uniform distribution, but this is only a problem for very large ranges. This is good
     * enough for testing.
     *
     * @param n The range for the integer [0, n)
     * @return The generated integer
     */
    public int nextInt(int n) {
        // good enough...
        return (nextBits(32) % n + n) % n;
    }
}
