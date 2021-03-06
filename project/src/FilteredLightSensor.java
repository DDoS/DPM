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
import lejos.robotics.Color;

/**
 * A sliding average filtered differencial light sensor. This is implemented as thin wrapper around the ColorSensor class. This class is meant for periodic high frequency sampling. Note that the first
 * few readings will be less accurate as the filtering data has yet to be accumulated.
 * <p/>
 * NOT THREAD SAFE
 */
public class FilteredLightSensor {
    private final ColorSensor handle;
    // Array of last sensor samples
    private int[] samples;
    // Current count in the array
    private int currentCount;
    // Current index in the array for the next sample
    private int index;
    // The last value for differencial filtering
    private int lastSample;

    /**
     * Constructs a new filtered light sensor from the port to which it is connected.
     *
     * @param port The sensor port
     */
    public FilteredLightSensor(SensorPort port) {
        handle = new ColorSensor(port);
        setSampleCount(3);
        setFloodlight(true);
    }

    /**
     * Sets the number of samples to use for averaging. This will reset the readings.
     *
     * @param sampleCount The number of samples to average
     */
    public void setSampleCount(int sampleCount) {
        samples = new int[sampleCount];
        currentCount = 0;
        index = 0;
    }

    /**
     * Sets the state of the floodlight: true for ON, false for OFF.
     *
     * @param state Whether or not to use the floodlight
     */
    public void setFloodlight(boolean state) {
        if (state) {
            handle.setFloodlight(Color.GREEN);
        } else {
            handle.setFloodlight(Color.NONE);
        }
    }

    /**
     * Forces the sensor to take a sample and add it to the sliding average filter, effectively slidding it forward by one sample. This doesn't actually compute the light value, but can help improve
     * the frequency of sampling even in slow threads when called at a few strategic locations in the code. The disadvantage is the lost of time coherency in the samples.
     */
    public void forceSample() {
        final int sampleCount = samples.length;
        // Get the light value
        samples[index] = handle.getNormalizedLightValue();
        // Compute the sample count, maxing at the array size
        currentCount = Math.min(currentCount + 1, sampleCount);
        // Compute the next index, wrapping around
        index = (index + 1) % sampleCount;
    }

    /**
     * Applies the filtering and returns the differencial light data value. Performs one sampling per call.
     *
     * @return The light level difference data
     */
    public int getLightData() {
        final int sampleCount = samples.length;
        // compute a new sample
        forceSample();
        // Get the start of the array, which is different before wrapping
        int start = currentCount < sampleCount ? 0 : index;
        int currentSample = 0;
        // Compute the average of the samples
        for (int i = 0; i < currentCount; i++) {
            // Get the sample with a wrapping index
            currentSample += samples[(start + i) % sampleCount];
        }
        currentSample /= currentCount;
        // First sample is ignored
        if (sampleCount <= 1) {
            lastSample = currentSample;
            return 0;
        }
        // Compute the difference and update last sample
        int difference = currentSample - lastSample;
        lastSample = currentSample;
        return difference;
    }
}
