import lejos.nxt.*;

public class FilteredUltrasonicSensor {
	private static final int MAX = 255;
	private final UltrasonicSensor handle;
	private volatile int sampleCount;

	public FilteredUltrasonicSensor(SensorPort port) {
		handle = new UltrasonicSensor(port);
	}

	public void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

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
			// Else we'll average all the non-max samples
		} else {
			// Odd sample count
			final int first = samples[mid];
			if (first == MAX) {
				// More than half the samples are MAX, so MAX is returned
				return MAX;
			}
			// Else we'll average all the non-max samples
		}
		// Average the results
		int sum = 0;
		for (int i = 0, sample; i < count && (sample = samples[i]) < MAX; i++) {
			sum += sample;
		}
		// Make sure we're not dividing by zero
		if (count == 0) {
			return 0;
		}
		return sum / count;
	}

	private static void insertSorted(int[] list, int size, int element) {
		int i, e, t;
		// Find the index at which to insert
		for (i = 0; i < size && (e = list[i]) <= element; i++) {
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
