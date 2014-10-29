import lejos.nxt.*;
import lejos.robotics.Color;

public class LightLocalizer {
	// Max light value reading for a grid line
	private static final int LINE_LIGHT = 500;
	private static final double LIGHT_SENSOR_DISTANCE = 11.5;
	private final Odometer odo;
	private final Navigation navig;
	private final ColorSensor ls;
	private boolean lineDetected = false;

	public LightLocalizer(Odometer odo, Navigation navig, ColorSensor ls) {
		this.odo = odo;
		this.navig = navig;
		this.ls = ls;
		// set the sensor flood light to green
		ls.setFloodlight(Color.GREEN);
	}

	public void doLocalization() {
		// travel to location		
		// cross a line and stop
		navig.travelBy(100, -100);
		while (!lineCrossed()) {
			Thread.yield();
		}
		navig.abort();
		// travel over the grid lines
		navig.travelBy(-LIGHT_SENSOR_DISTANCE * 0.8, LIGHT_SENSOR_DISTANCE * 0.8);
		navig.waitUntilDone();
		// turn to zero to prepare for next step
		navig.turnTo(0);
		navig.waitUntilDone();
		// Lines seens and angles and count
		double[] lineAngles = new double[4];
		int lineCount = 0;
		// Turn and record each line seen (-x, -y, +x, +y)
		while (lineCount < 4) {
			if (!navig.isNavigating()) {
				navig.turnBy(0.9 * Math.PI);
			}
			if (lineCrossed()) {
				lineAngles[lineCount++] = odo.getTheta();
			}
			Thread.yield();
		}
		navig.abort();
		// find x
		double deltaY = USLocalizer.getAngleDiff(lineAngles[1], lineAngles[3]);
		double x = -LIGHT_SENSOR_DISTANCE * Math.cos(deltaY / 2);
		// find y
		double deltaX = USLocalizer.getAngleDiff(lineAngles[0], lineAngles[2]);
		double y = LIGHT_SENSOR_DISTANCE * Math.cos(deltaX / 2);
		// find theta correction
		double thetaCorrection = deltaY / 2 + Math.PI - lineAngles[3];
		// update odometer
		odo.setPosition(x, y, odo.getTheta() + thetaCorrection);
		// travel to (0, 0) and turn to angle 0
		navig.travelTo(0, 0);
		navig.waitUntilDone();
		navig.turnTo(0);
		navig.waitUntilDone();
	}

	// Returns true if a line has been crossed since last call
	private boolean lineCrossed() {
		// Check for line detected
		boolean newLineDetected = ls.getNormalizedLightValue() <= LINE_LIGHT;
		// Only report a rising edge
		boolean crossed = !lineDetected && newLineDetected;
		lineDetected = newLineDetected;
		return crossed;
	}
}
