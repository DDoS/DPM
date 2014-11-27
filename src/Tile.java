public final class Tile {
    // Spacing of the tiles in centimeters
    public static final float ONE = 30.48f;
    public static final float HALF = ONE / 2;
    public static final float QUARTER = ONE / 4;

    // Prevent instances of the singleton
    private Tile() {
    }

    public static float toOdo(int tile) {
        return tile * ONE + HALF;
    }

    public static int fromOdo(float odo) {
        return (int) ((odo - HALF) / ONE);
    }
}
