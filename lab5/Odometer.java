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

/*
  An odometer class which spawns a new thread on start
  to monitor the wheel rotation and update the position
  information every 25 ms.
 */
public class Odometer extends Thread {
	// odometer update period, in ms
	private static final long PERIOD = 25;
	public static final double WHEEL_RADIUS = 2.05;
	public static final double WHEEL_DISTANCE = 14.9;
	private static final double TWO_PI = Math.PI * 2;
	// robot position
	private double x = 0, y = 0, theta = Math.PI / 2;
	// Tachometer last readings in radians, for right and left
	private double lastRho = 0, lastLambda = 0;
	// lock object for mutual exclusion
	private final Object lock = new Object();
	// Left and right motors
	private final NXTRegulatedMotor leftMotor = Motor.A;
	private final NXTRegulatedMotor rightMotor = Motor.C;

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

	// Returns the current x position
	public double getX() {
		synchronized (lock) {
			return x;
		}
	}

	// Returns the current y position
	public double getY() {
		synchronized (lock) {
			return y;
		}
	}

	// Returns the current theta
	public double getTheta() {
		synchronized (lock) {
			return theta;
		}
	}

	// Returns the current x, y and theta as a position object
	public Position getPosition() {
		synchronized (lock) {
			return new Position(x, y, theta);
		}
	}

	// Sets theta to the desired value
	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	// Sets x, y and theta to the desired values
	public void setPosition(double x, double y, double theta) {
		synchronized (lock) {
			this.x = x;
			this.y = y;
			this.theta = theta;
		}
	}

	// Wraps an angle between 0 (inclusive) and 2pi (exclusive)
	public static double wrapAngle(double rads) {
		return ((rads % TWO_PI) + TWO_PI) % TWO_PI;
	}

	// Represents a position with a heading (x, y and theta)
	public static class Position {
		public final double x, y, theta;

		// Constructs a new position object from the x, y and theta values
		public Position(double x, double y, double theta) {
			this.x = x;
			this.y = y;
			this.theta = theta;
		}
	}
}
