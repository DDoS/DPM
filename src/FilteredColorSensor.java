/**
 * An average filtered color sensor. This is implemented as thin wrapper around the ColorSensor class. This class is meant for random sampling.
 * <p/>
 * THREAD SAFE
 */
public class FilteredColorSensor {
    /**
     * Constructs a new filtered color sensor from the port to which it is connected.
     *
     * @param port The sensor port
     */
    public FilteredLightSensor(SensorPort port) {
    }

    /**
     * Sets the number of samples to use for filtering.
     *
     * @param sampleCount The number of samples to use for filtering
     */
    public void setSampleCount(int sampleCount) {
    }

    /**
     * Applies the filtering and returns the color data, a value of format 0xRRGGBB where R are the red component bits, G the green ones and B the blue ones. Performs the specified number of samples
     * per call.
     *
     * @return The color data
     */
    public int getColorData() {
        return 0;
    }
}
