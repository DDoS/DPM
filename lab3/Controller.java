/*
This file is part of DPM, licensed under the MIT License (MIT).

Copyright (c) 2014 Team 20

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

public class Controller extends Thread {
	private static final double NAVIGATION_EPSILON = 0.5;
	// Angle the senor is mounted at, from the front of the robot
	private static final double SENSOR_ANGLE = Math.toRadians(45);
	// Correction factor for the distances due to the angle of the sensor mount
	private static final double BAND_DISTANCE_CORRECTION = 1 / Math.sin(SENSOR_ANGLE);
	private static final double BAND_CENTER = 20 * BAND_DISTANCE_CORRECTION * 1.2;
	private static final double BAND_WIDTH = 3 * BAND_DISTANCE_CORRECTION * 4;
	// Motor speed constants
	private static final int MOTOR_LOW = 100;
	private static final int MOTOR_HIGH = 400;
	private static final int MOTOR_STRAIGHT = 200;
	//
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.C;
	//
	private final UltrasonicPoller usPoller = new UltrasonicPoller();
	private final Odometer odometer = new Odometer();
	//
	private double[] path = new double[0];
	private volatile boolean navigating = false;
	private volatile boolean avoidance = false;

	public void run() {
		odometer.start();
		usPoller.start();
		// Wait before starting
		sleepFor(2000);
		// Travel path nodes in order
		for (int i = 0; i < path.length; i += 2) {
			travelTo(path[i], path[i + 1]);
		}
	}

	public void travelTo(double x, double y) {
		navigating = true;
		// Turn and start
		leftMotor.setSpeed(MOTOR_STRAIGHT);
		rightMotor.setSpeed(MOTOR_STRAIGHT);
		double differenceX = x - odometer.getX();
		double differenceY = y - odometer.getY();
		turnTo(Math.atan2(differenceY, differenceX));
		leftMotor.forward();
		rightMotor.forward();
		// Main loop
		while (true) {
			if (inDanger()) {
				// Avoid
				do {
					doAvoidance();
				} while (inDanger());
				// Go back to main track
				leftMotor.setSpeed(MOTOR_STRAIGHT);
				rightMotor.setSpeed(MOTOR_STRAIGHT);
				leftMotor.forward();
				rightMotor.forward();
				sleepFor(2000);
				differenceX = x - odometer.getX();
				differenceY = y - odometer.getY();
				turnTo(Math.atan2(differenceY, differenceX));
				leftMotor.forward();
				rightMotor.forward();
			}
			// Check for target reached
			differenceX = x - odometer.getX();
			differenceY = y - odometer.getY();
			if (differenceX * differenceX + differenceY * differenceY < NAVIGATION_EPSILON * NAVIGATION_EPSILON) {
				leftMotor.stop(true);
				rightMotor.stop(false);
				Sound.playNote(Sound.FLUTE, 440, 250);
				navigating = false;
				return;
			}
		}
	}

	public void turnTo(double theta) {
		// Find min difference
		double difference = Odometer.wrapAngle(theta) - odometer.getTheta();
		if (difference >= Math.PI) {
			difference = -2 * Math.PI + difference;
		}
		// Compute wheel rotation in angle
		double wheelRotation = (difference * Odometer.WHEEL_DISTANCE / Odometer.WHEEL_RADIUS) / 2;
		int rotationDegrees = (int) Math.round(Math.toDegrees(wheelRotation));
		boolean navigatingModified = false;
		if (!navigating) {
			navigating = true;
			navigatingModified = true;
		}
		// Rotate
		leftMotor.rotate(-rotationDegrees, true);
		rightMotor.rotate(rotationDegrees, false);
		if (navigatingModified) {
			navigating = false;
		}
	}

	public boolean isNavigating() {
		return navigating;
	}

	public void enableAvoidance(boolean enable) {
		avoidance = enable;
	}

	public void setPath(double... path) {
		this.path = path;
	}

	public double getX() {
		return odometer.getX();
	}

	public double getY() {
		return odometer.getY();
	}

	public double getTheta() {
		return odometer.getTheta();
	}

	private boolean inDanger() {
		return avoidance && usPoller.getDistance() < BAND_CENTER;
	}

	private void doAvoidance() {
		int distance = usPoller.getDistance();
		// Compute the absolute error from the band center and normalize if within the band width
		double error = Math.abs(distance - BAND_CENTER) / BAND_WIDTH;
		// Linearly interpolate the high speed from normal to high using the error
		int highSpeed = (int) Math.round(lerp(MOTOR_STRAIGHT, MOTOR_HIGH, error));
		// Linearly interpolate the low speed from normal to low using the error
		int lowSpeed = (int) Math.round(lerp(MOTOR_STRAIGHT, MOTOR_LOW, error));
		// At the center, the speeds are both straight, at the edge they're on full turn
		if (distance >= BAND_CENTER) {
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
	private static double lerp(double a, double b, double percent) {
		return (1 - percent) * a + percent * b;
	}

	private static void sleepFor(long dur) {
		try {
			Thread.sleep(dur);
		} catch (InterruptedException ex) {
			// not expected
		}
	}
}
