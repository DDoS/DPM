/*
 * Odometer.java
 */

import lejos.nxt.*;

public class Odometer extends Thread {
	// robot position
	private double x, y, theta;
	// Tachometer last readings in radians, for right and left
	private double lastRho = 0, lastLambda = 0;
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	private static final double WHEEL_RADIUS = 2.1;
	private static final double WHEEL_DISTANCE = 15.1;
	private static final double TWO_PI = Math.PI * 2;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer() {
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		// reset motor tachos
		Motor.B.resetTachoCount();
		Motor.A.resetTachoCount();
		// set initial position
		x = 0;
		y = 0;
		theta =  Math.PI / 2;

		while (true) {
			updateStart = System.currentTimeMillis();

			// compute rho and lambda
			double rho = Math.toRadians(Motor.B.getTachoCount());
			double lambda = Math.toRadians(Motor.A.getTachoCount());
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

			synchronized (lock) {
				// don't use the variables x, y, or theta anywhere but here!
				// update x, y, and theta by their deltas
				x += deltaX;
				y += deltaY;
				theta += deltaTheta;
				theta = wrapAngle(theta);
			}

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	private double wrapAngle(double rads) {
		return ((rads % TWO_PI) + TWO_PI) % TWO_PI;
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				position[0] = x;
			if (update[1])
				position[1] = y;
			if (update[2])
				position[2] = theta;
		}
	}

	public double getX() {
		double result;

		synchronized (lock) {
			result = x;
		}

		return result;
	}

	public double getY() {
		double result;

		synchronized (lock) {
			result = y;
		}

		return result;
	}

	public double getTheta() {
		double result;

		synchronized (lock) {
			result = theta;
		}

		return result;
	}

	// mutators
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}
}
