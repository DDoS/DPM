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
import lejos.nxt.*;

/**
 * A combined median and average filtered ultrasonic sensor. This is implemented as thin wrapper around the UltrasonicSensor class. This class is meant for random sampling.
 * <p/>
 * THREAD SAFE
 */
public class FilteredUltrasonicSensor {
    private static final int MAX = 255;
    private final UltrasonicSensor handle;
    private volatile int sampleCount;

    /**
     * Constructs a new filtered ultrasonic sensor from the port to which it is connected.
     *
     * @param port The sensor port
     */
    public FilteredUltrasonicSensor(SensorPort port) {
        handle = new UltrasonicSensor(port);
        setSampleCount(5);
    }

    /**
     * Sets the number of samples to use for filtering.
     *
     * @param sampleCount The number of samples to use for filtering
     */
    public void setSampleCount(int sampleCount) {
        this.sampleCount = sampleCount;
    }

    /**
     * Applies the filtering and returns the distance data, a value between 0 and 255 (closest to nothing). Performs the specified number of samples per call.
     *
     * @return The distance data
     */
    public int getDistanceData() {
        // Thread safety precautions, make a local copy of sample count
        final int count = sampleCount;
        // Sample the sensor and save these as a sorted list
        final int[] samples = new int[count];
        for (int i = 0; i < count; i++) {
            insertSorted(samples, i, handle.getDistance());
        }
        // Apply a median filter
        final int mid = count >> 1;
        if ((count & 1) == 0) {
            // Even sample count
            final int first = samples[mid - 1];
            final int second = samples[mid];
            if (first == MAX && second == MAX) {
                // More than half the samples are MAX, so MAX is returned
                return MAX;
            }
        } else {
            // Odd sample count
            final int first = samples[mid];
            if (first == MAX) {
                // More than half the samples are MAX, so MAX is returned
                return MAX;
            }
        }
        // Average all the non-max samples
        int index = 0, sum = 0;
        for (int sample; index < count && (sample = samples[index]) < MAX; index++) {
            sum += sample;
        }
        // Make sure we're not dividing by zero
        if (index == 0) {
            return 0;
        }
        return sum / index;
    }

    private static void insertSorted(int[] list, int size, int element) {
        int i, e, t;
        // Find the index at which to insert
        for (i = 0; i < size && list[i] <= element; i++) {
        }
        // If at end, just insert and return
        if (i == size) {
            list[i] = element;
            return;
        }
        // Else shift all the elements right, inserting the new one first
        for (e = element; i < size; i += 2) {
            t = list[i];
            list[i] = e;
            e = list[i + 1];
            list[i + 1] = t;
        }
        // Edge case for the last element
        if (i == size) {
            list[i] = e;
        }
    }
}
