import lejos.nxt.*;
import lejos.robotics.Color;

/**
 * A sliding average filtered light sensor. This is implemented as thin wrapper around the ColorSensor class. This class is meant for periodic high frequency sampling. Note that the first few readings
 * will be less accurate as the filtering data has yet to be accumulated.
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
     * Applies the filtering and returns the light data, a value between 0 and 100 (darkest to brightest). Performs only one sampling per call.
     *
     * @return The light level data
     */
    public int getLightData() {
        final int sampleCount = samples.length;
        // Get the light value
        samples[index] = handle.getLightValue();
        // Compute the sample count, maxing at the array size
        currentCount = Math.min(currentCount + 1, sampleCount);
        // Compute the next index, wrapping around
        index = (index + 1) % sampleCount;
        // Get the start of the array, which is different before wrapping
        int start = currentCount < sampleCount ? 0 : index;
        int average = 0;
        // Compute the average of the samples
        for (int i = 0; i < currentCount; i++) {
            // Get the sample with a wrapping index
            average += samples[(start + i) % sampleCount];
        }
        return average / currentCount;
    }
}
