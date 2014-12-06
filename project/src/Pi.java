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
 * Contains various ratios of Pi.
 */
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
