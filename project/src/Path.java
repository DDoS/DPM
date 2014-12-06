/*
This file is part of DPM, licensed under the MIT License (MIT).

Copyright (c) 2014 Team 7

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
 * Path class used to represent paths between nodes
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
