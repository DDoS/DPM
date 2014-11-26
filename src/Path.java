/**
 * Path class used to represent paths between nodes
 *
 * @author Jonah
 */
public class Path {
    /**
     * An enum used to represent the three valid directions from a node
     *
     * @author Jonah
     */
    public enum Direction {
        LEFT,
        RIGHT,
        FRONT
    }

    private Direction dir;
    private Path next;

    /**
     * Constructs a Path with a given Direction
     *
     * @param d Direction to specify for the Path
     */
    public Path(Direction d) {
        dir = d;
        next = null;
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
}