import lejos.nxt.*;

/**
 * An average filtered color sensor. This is implemented as thin wrapper around the ColorSensor class. This class is meant for random sampling.
 * <p/>
 * THREAD SAFE
 */
public class FilteredColorSensor {
    private final ColorSensor handle;
    private volatile int sampleCount;

    /**
     * Constructs a new filtered color sensor from the port to which it is connected.
     *
     * @param port The sensor port
     */
    public FilteredColorSensor(SensorPort port) {
        handle = new ColorSensor(port);
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
     * Applies the filtering and returns the color data, a value of format 0xRRGGBB where R are the red component bits, G the green ones and B the blue ones. Performs the specified number of samples
     * per call.
     *
     * @return The color data
     */
    public int getColorData() {
        // Thread safety precautions, make a local copy of sample count
        final int count = sampleCount;
        // No samples means no color
        if (count == 0) {
            return 0;
        }
        // Sample the sensoe and average the results per component
        int redSum = 0, greenSum = 0, blueSum = 0;
        for (int i = 0; i < count; i++) {
            final ColorSensor.Color sample = handle.getColor();
            redSum += sample.getRed();
            greenSum += sample.getGreen();
            blueSum += sample.getBlue();
        }
        redSum /= count;
        greenSum /= count;
        blueSum /= count;
        // Assemble components as 0RGB 32-bit color int
        return (redSum & 255) << 16 | (greenSum & 255) << 8 | blueSum & 255;
    }
}
