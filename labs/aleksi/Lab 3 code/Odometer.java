/*
 * Odometer.java
 */

import lejos.nxt.*;

public class Odometer extends Thread {
	// odometer update period, in ms
	private static final long PERIOD = 25;
	public static final double WHEEL_RADIUS = 2.05;
	public static final double WHEEL_DISTANCE = 15.3;
	private static final double TWO_PI = Math.PI * 2;
	// robot position
	private double x = 0, y = 0, theta = Math.PI / 2;
	// Tachometer last readings in radians, for right and left
	private double lastRho = 0, lastLambda = 0;
	// lock object for mutual exclusion
	private final Object lock = new Object();
	//
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.C;

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;
		// reset motor tachos
		rightMotor.resetTachoCount();
		leftMotor.resetTachoCount();
		//
		while (true) {
			updateStart = System.currentTimeMillis();
			// compute rho and lambda
			double rho = Math.toRadians(rightMotor.getTachoCount());
			double lambda = Math.toRadians(leftMotor.getTachoCount());
			// compute the delta rho and lambda from last values
			double deltaRho = rho - lastRho;
			double deltaLambda = lambda - lastLambda;
			// update last values to current
			lastRho = rho;
			lastLambda = lambda;
			// multiply rho and lambda by the wheel radius
			double deltaRhoRadius = deltaRho * WHEEL_RADIUS;
			double deltaLambdaRadius = deltaLambda * WHEEL_RADIUS;
			// compute delta C
			double deltaC = (deltaRhoRadius + deltaLambdaRadius) / 2;
			// compute delta theta and it's half
			double deltaTheta = (deltaRhoRadius - deltaLambdaRadius) / WHEEL_DISTANCE;
			double halfDeltaTheta = deltaTheta / 2;
			// compute delta x and y, using y forward and a right handed system (x right)
			double deltaX = deltaC * Math.cos(theta + halfDeltaTheta);
			double deltaY = deltaC * Math.sin(theta + halfDeltaTheta);
			// update position
			synchronized (lock) {
				// update x, y, and theta by their deltas
				x += deltaX;
				y += deltaY;
				theta = wrapAngle(theta + deltaTheta);
			}
			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < PERIOD) {
				try {
					Thread.sleep(PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	public double getX() {
		synchronized (lock) {
			return x;
		}
	}

	public double getY() {
		synchronized (lock) {
			return y;
		}
	}

	public double getTheta() {
		synchronized (lock) {
			return theta;
		}
	}

	public static double wrapAngle(double rads) {
		return ((rads % TWO_PI) + TWO_PI) % TWO_PI;
	}
}
