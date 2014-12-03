/**
 * A class with constants and utility methods regarding the tiles on the competition floor.
 */
public final class Tile {
    /**
     * The size of one tile in centimeters.
     */
    public static final float ONE = 30.48f;
    /**
     * The size of half a tile in centimeters.
     */
    public static final float HALF = ONE / 2;
    /**
     * The size of a quarter tile in centimeters.
     */
    public static final float QUARTER = ONE / 4;

    // Prevent instances of the singleton
    private Tile() {
    }

    /**
     * Convert tile coordinates to odometer coordinates (centimeters).
     *
     * @param tile The tile coordinate
     * @return The same coordinate in odometer coords
     */
    public static float toOdo(int tile) {
        return tile * ONE + HALF;
    }

    /**
     * Converts the odometer coordinates (centimeters) to tile coordinates
     *
     * @param odo The odometer coordinate
     * @return The same coordinate in tiles coords
     */
    public static int fromOdo(float odo) {
        return (int) ((odo - HALF) / ONE);
    }
}
