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
 * MapPath class used to represent paths between nodes
 */
public class MapPath {
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
    private MapPath next;

    /**
     * Constructs a MapPath with a given Direction
     *
     * @param d Direction to specify for the MapPath
     */
    public MapPath(Direction d) {
        dir = d;
        next = null;
    }

    /**
     * Constructs a MapPath with a given Direction, and another MapPath to link to Returns the head of the new MapPath
     *
     * @param d Direction to specify
     * @param m MapPath to set as child of head
     */
    public MapPath(Direction d, MapPath m) {
        dir = d;
        next = m;
    }

    /**
     * Adds a new MapPath to the end of an existing MapPath
     *
     * @param m MapPath to add
     */
    public void addMapPath(MapPath m) {
        if (this.next != null) {
            this.next.addMapPath(m);
        } else {
            this.next = m;
        }
    }

    /**
     * Returns the Direction of the MapPath
     *
     * @return Direction
     */
    public Direction getDirection() {
        return dir;
    }

    /**
     * Returns the next MapPath in order
     *
     * @return Next MapPath
     */
    public MapPath getNextMapPath() {
        return this.next;
    }
}
