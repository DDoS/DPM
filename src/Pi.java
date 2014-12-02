public final class Pi {
    // Various PI ratios
    public static final float ONE = (float) Math.PI;
    public static final float ONE_HALF = ONE / 2;
    public static final float THREE_HALF = 3 * ONE_HALF;
    public static final float TWO = 2 * ONE;
    public static final float ONE_QUARTER = ONE / 4;
    public static final float THREE_QUARTER = 3 * ONE_QUARTER;
    public static final float FIVE_QUARTER = 5 * ONE_QUARTER;
    public static final float SEVEN_QUARTER = 7 * ONE_QUARTER;
    public static final float ONE_SIXTH = ONE / 6;
    public static final float ONE_SEVENTH = ONE / 7;
    public static final float ONE_FIFTH = ONE / 5;

    // Prevent instances of the singleton
    private Pi() {
    }

    /**
    * Wraps an angle between 0 (inclusive) and 2pi (exclusive). This is a utility method exposed for usage by other classes.
    *
    * @param rads The angle to wrap in radians.
    * @return The wrapped angle in radians
    */
    public static float wrapAngle(float rads) {
        return ((rads % TWO) + TWO) % TWO;
    }
}
