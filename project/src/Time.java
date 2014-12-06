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
 * A singleton class for keeping track of time. The shared nature of it makes sharing this easier, and we only need one time for the robot, so it doesn't matter that we can only have on timer.
 */
public final class Time {
    //Keeps track of the total time allowed for the run, as well as the system time when it started
    private static long limitSeconds;
    private static long startTime;

    // prevent instance of the singleton
    private Time() {
    }

    /**
     * Call to set the starting time of the robot, and pass in the time limit of the run
     *
     * @param lim Integer specifying the time limit in seconds that the robot is allowed
     */
    public static void startTime(long lim) {
        limitSeconds = lim;
        startTime = System.currentTimeMillis();
    }

    /**
     * Compares the start system time to the current system time to find the amount of time that elapsed
     *
     * @return Returns the seconds left in the run as a long integer.
     */
    public static long timeLeft() {
        long timeDiff = System.currentTimeMillis() - startTime;
        return limitSeconds - timeDiff / 1000;
    }
}
