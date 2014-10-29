import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.*;

public class PController implements UltrasonicController {
	// Angle the senor is mounted at, from the front of the robot
	private final double SENSOR_ANGLE = Math.toRadians(45);
	// Correction factor for the distances due to the angle of the sensor mount
	private final float BAND_DISTANCE_CORRECTION = (float) (1 / Math.sin(SENSOR_ANGLE));
	private final float bandCenter, bandWidth;
	private final int motorLow = 100, motorHigh = 400;
	private final int motorStraight = 200;
	private final NXTRegulatedMotor leftMotor = Motor.A, rightMotor = Motor.C;
	private int distance;

	public PController(int bandCenter, int bandWidth) {
		//Default Constructor
		// Apply angle correction and alter the factors slightly to improve results
		this.bandCenter = bandCenter * BAND_DISTANCE_CORRECTION * 1.2f;
		this.bandWidth = bandWidth * BAND_DISTANCE_CORRECTION * 4;
		leftMotor.setSpeed(motorStraight);
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
	}

	@Override
	public void processUSData(int distance) {
		this.distance = distance;
		// Compute the absolute error from the band center and normalize if within the band width
		float error = Math.abs(distance - bandCenter) / bandWidth;
		// Linearly interpolate the high speed from normal to high using the error
		int highSpeed = Math.round(lerp(motorStraight, motorHigh, error));
		// Linearly interpolate the low speed from normal to low using the error
		int lowSpeed = Math.round(lerp(motorStraight, motorLow, error));
		// At the center, the speeds are both straight, at the edge they're on full turn
		if (distance >= bandCenter) {
			// Turn left when erroring by right
			leftMotor.setSpeed(lowSpeed);
			rightMotor.setSpeed(highSpeed);
		} else {
			// Turn right when erroring by left
			leftMotor.setSpeed(highSpeed);
			rightMotor.setSpeed(lowSpeed);
		}
	}

	// Regular linear interpolation function betwewn a and b at percent
	private float lerp(float a, float b, float percent) {
		return (1 - percent) * a + percent * b;
	}

	@Override
	public int readUSDistance() {
		return this.distance;
	}

}
