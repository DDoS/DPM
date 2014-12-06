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
