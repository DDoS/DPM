/**
 * Path class used to represent paths between nodes
 *
 * @author Jonah
 */
public class Path {
    private Direction dir;
    private Path next;

    /**
     * Constructs a Path with a given Direction
     *
     * @param d Direction to specify for the Path
     */
    public Path(Direction d) {
        this(d, null);
    }

    /**
     * Constructs a Path with a given Direction, and another Path to link to Returns the head of the new Path
     *
     * @param d Direction to specify
     * @param m Path to set as child of head
     */
    public Path(Direction d, Path m) {
        dir = d;
        next = m;
    }

    /**
     * Adds a new Path to the end of an existing Path
     *
     * @param m Path to add
     */
    public void addPath(Path m) {
        if (this.next != null) {
            this.next.addPath(m);
        } else {
            this.next = m;
        }
    }

    /**
     * Returns the Direction of the Path
     *
     * @return Direction
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * Returns the next Path in order
     *
     * @return Next Path
     */
    public Path getNextPath() {
        return this.next;
    }

    /**
     * An enum used to represent the three valid directions from a node
     *
     * @author Jonah
     */
    public static enum Direction {
        LEFT(-1, 0),
        RIGHT(1, 0),
        FRONT(0, 1),
        BACK(0, -1);
        /**
         * The unit offset for the direction in tile coords.
         */
        public final int xOffset, yOffset;

        private Direction(int xOffset, int yOffset) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
        }
    }
}
