import lejos.nxt.*;

public class UltrasonicPoller extends Thread {
	// The number of last sensor samples to keep
	private static final int SAMPLE_COUNT = 10;
	//
	private UltrasonicSensor us = new UltrasonicSensor(SensorPort.S1);
	// Array of last sensor samples
	private final int[] samples = new int[SAMPLE_COUNT];
	// Current count in the array
	private int currentCount = 0;
	// Current index in the array for the next sample
	private int index = 0;
	// current sampled distance
	private volatile int distance;

	public void run() {
		while (true) {
			distance = poll();
			try {
				Thread.sleep(10);
			} catch(Exception e) {
			}
		}
	}

	private int poll() {
		// Get the distance
		int sample = us.getDistance();
		// Handle 255 values separately
		if (sample == 255) {
			// Recude them to 40 dampen their impact
			samples[index] = 40;
		} else {
			// Regular values are added to the array
			samples[index] = sample;
		}
		// Compute the sample count, maxing at the array size
		currentCount = Math.min(currentCount + 1, SAMPLE_COUNT);
		// Compute the next index, wrapping around
		index = (index + 1) % SAMPLE_COUNT;
		// Get the start of the array, which is different before wrapping
		int start = currentCount < SAMPLE_COUNT ? 0 : index;
		int average = 0;
		// Compute the average of the samples
		for (int i = 0; i < currentCount; i++) {
			// Get the sample with a wrapping index
			average += samples[(start + i) % SAMPLE_COUNT];
		}
		return average / currentCount;
	}

	public int getDistance() {
		return distance;
	}
}
