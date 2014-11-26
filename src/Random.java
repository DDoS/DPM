public class Random {
    private static final int BITS = 48;
    private static final long MUL = 0x5DEECE66DL;
    private static final long ADD = 0xBL;
    private static final long MASK = (1L << BITS) - 1;
    private long seed;

    public Random() {
        this.seed = (System.currentTimeMillis() ^ MUL) & MASK;
    }

    public int nextBits(int bits) {
        seed = (seed * MUL + ADD) & MASK;
        return (int) (seed >>> BITS - bits);
    }

    public boolean nextBoolean() {
        return nextBits(1) == 1;
    }

    public int nextInt(int n) {
        // good enough...
        return nextBits(32) % n;
    }
}
