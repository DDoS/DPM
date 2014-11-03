import lejos.nxt.*;
import lejos.robotics.Color;

// NOT THREAD SAFE, ONLY USE WITH SINGLE THREAD AND PERIODIC SAMPLING
public class FilteredLightSensor {
	private final ColorSensor handle;
	// Array of last sensor samples
	private int[] samples;
	// Current count in the array
	private int currentCount;
	// Current index in the array for the next sample
	private int index;

	public FilteredLightSensor(SensorPort port) {
		handle = new ColorSensor(port);
	}

	public void setSampleCount(int sampleCount) {
		samples = new int[sampleCount];
		currentCount = 0;
		index = 0;
	}

	public void setFloodlight(boolean state) {
		if (state) {
			handle.setFloodlight(Color.GREEN);
		} else {
			handle.setFloodlight(Color.NONE);
		}
	}

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
