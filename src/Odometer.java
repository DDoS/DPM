import lejos.nxt.*;

/**
 * An odometer class which runs its own thread to monitor the wheel rotation and update the position information every 10 ms.
 * <p/>
 * THREAD SAFE
 */
public class Odometer extends Thread {
    // odometer update period, in ms
    private static final long PERIOD = 10;
    // The distance of the sensor from the wheel axle
    private static final float SENSOR_OFFSET = -5.5f;
    // Min light value reading drop for a grid line
    private static final int LINE_LIGHT_LEFT = -20;
    private static final int LINE_LIGHT_RIGHT = -20;
    // Dampening for heading correction to reduce error from bad correction
    private static final float HEADING_CORRECTION_DAMPEN = 0.5f;
    // Whether or not the odometer is running
    private volatile boolean running = false;
    // robot position
    private float x = Tile.HALF, y = Tile.HALF, theta = (float) Math.PI / 2;
    // Tachometer last readings in radians, for right and left
    private float lastRho = 0, lastLambda = 0;
    // lock object for mutual exclusion
    private final Object lock = new Object();
    // Left and right motors
    private final NXTRegulatedMotor leftMotor;
    private final NXTRegulatedMotor rightMotor;
    // Whether or not to do correction with the light sensor
    private volatile boolean performCorrection = false;
    // Whether or not to output debug information to the display and sound
    private volatile boolean outputDebug = false;
    // Light sensor for correction
    private final FilteredLightSensor leftLightSensor;
    private final FilteredLightSensor rightLightSensor;

    /**
     * Constructs a new odometer from the left and right motors of the robot and the light sensor for correction (can be null if this won't ever be used).
     *
     * @param leftMotor The left motor
     * @param rightMotor The right motor
     * @param leftLightSensor The left wheel orrection light sensor
     * @param rightLightSensor The right wheel correction light sensor
     */
    public Odometer(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, FilteredLightSensor leftLightSensor, FilteredLightSensor rightLightSensor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.leftLightSensor = leftLightSensor;
        this.rightLightSensor = rightLightSensor;
    }

    /**
     * Whether or not to enable the position correction using the light sensor. Do not enable this if the odometer was constructed using null light sensors.
     *
     * @param enabled The enable state
     */
    public void enableCorrection(boolean enabled) {
        performCorrection = enabled;
        if (running) {
            leftLightSensor.setFloodlight(performCorrection);
            rightLightSensor.setFloodlight(performCorrection);
        }
    }

    /**
     * Whether or not to enable the debug output: the odometer position will be mapped to the Display to "ox", "oy" and "ot".
     * When crossing a horizontal line, a low pitch sound will be played.
     * When crossing a vertical line, the sound will be higher pitched.
     *
     * @param enabled The enable state
     */
    public void enableDebugOutput(boolean enabled) {
        if (enabled) {
            Display.reserve("ox", "oy", "ot");
        } else {
            Display.remove("ox");
            Display.remove("oy");
            Display.remove("ot");
        }
        outputDebug = enabled;
    }

    /**
     * Runs the odometry code.
     */
    public void run() {
        long updateStart, updateEnd;
        // set the line as un-crossed for both sensors
        boolean leftCrossed = false, rightCrossed = false;
        // same as above but combined into one variable
        int crossFlags = 0;
        // stored values when crossing lines for the left and right sensors
        float leftTacho = 0, rightTacho = 0;
        Position leftOdo = null, rightOdo = null;
        // set the sensor flood on if we're using it
        if (performCorrection) {
            leftLightSensor.setFloodlight(true);
            rightLightSensor.setFloodlight(true);
        }
        // reset motor tachos
        rightMotor.resetTachoCount();
        leftMotor.resetTachoCount();
        // main loop
        running = true;
        while (running) {
            updateStart = System.currentTimeMillis();

			/*
             * ODOMETRY
			 */

            if (performCorrection) {
                leftLightSensor.forceSample();
                rightLightSensor.forceSample();
            }
            // compute rho and lambda
            float rho = (float) Math.toRadians(rightMotor.getTachoCount());
            float lambda = (float) Math.toRadians(leftMotor.getTachoCount());
            // compute the delta rho and lambda from last values
            float deltaRho = rho - lastRho;
            float deltaLambda = lambda - lastLambda;
            // update last values to current
            lastRho = rho;
            lastLambda = lambda;
            // multiply rho and lambda by the wheel radius
            float deltaRhoRadius = deltaRho * Wheel.RIGHT_RADIUS;
            float deltaLambdaRadius = deltaLambda * Wheel.LEFT_RADIUS;
            // compute delta C
            float deltaC = (deltaRhoRadius + deltaLambdaRadius) / 2;
            // compute delta theta and it's half
            float deltaTheta = (deltaRhoRadius - deltaLambdaRadius) / Wheel.DISTANCE;
            float halfDeltaTheta = deltaTheta / 2;
            // compute delta x and y, using y forward and a right handed system (x right)
            float deltaX = deltaC * (float) Math.cos(theta + halfDeltaTheta);
            float deltaY = deltaC * (float) Math.sin(theta + halfDeltaTheta);
            // update position
            synchronized (lock) {
                // update x, y, and theta by their deltas
                x += deltaX;
                y += deltaY;
                theta = Pi.wrapAngle(theta + deltaTheta);
            }

			/*
             * CORRECTION
			 */

            if (performCorrection) {
                // force a sample and read the left light value
                int leftLightValue = leftLightSensor.getLightData();
                // check if the left light value corresponds to a line
                if (leftLightValue <= LINE_LIGHT_LEFT) {
                    // check if the left line has yet to be crossed
                    if (!leftCrossed) {
                        leftTacho = lambda;
                        leftOdo = getPosition();
                        leftCrossed = true;
                        crossFlags |= 0x1;
                    }
                } else {
                    leftCrossed = false;
                }
                // force a sample and read the right light value
                int rightLightValue = rightLightSensor.getLightData();
                // check if the right light value corresponds to a line
                if (rightLightValue <= LINE_LIGHT_RIGHT) {
                    // check if the right line has yet to be crossed
                    if (!rightCrossed) {
                        rightTacho = lambda;
                        rightOdo = getPosition();
                        rightCrossed = true;
                        crossFlags |= 0x2;
                    }
                } else {
                    rightCrossed = false;
                }
                // perform the correction when both sensors have crossed the line
                if ((crossFlags & 0x3) == 0x3) {
                    // make sure the lines have been crossed on different odometer ticks for theta correction
                    if (leftTacho != rightTacho) {
                        // compute correction from tachometer delta
                        float correction = (float) Math.atan2((leftTacho - rightTacho) * Wheel.LEFT_RADIUS, Wheel.DISTANCE);
                        // apply correction
                        synchronized (lock) {
                            theta = theta + correction * HEADING_CORRECTION_DAMPEN;
                        }
                    }
                    // do coordinate correction: check which line direction we just crossed using the heading
                    if (theta >= Pi.ONE_QUARTER && theta < Pi.THREE_QUARTER || theta >= Pi.FIVE_QUARTER && theta < Pi.SEVEN_QUARTER) {
                        // crossed a horizontal line
                        float sensorYOffset = (float) Math.sin(theta) * SENSOR_OFFSET;
                        // offset y to account for sensor distance, using the odo average as the position of the sensor
                        float yy = (leftOdo.y + rightOdo.y) / 2 + sensorYOffset;
                        // snap y to closest line
                        yy = Math.round((yy + 0) / Tile.ONE) * Tile.ONE - 0;
                        // correct y, removing the offset
                        synchronized (lock) {
                            y = yy - sensorYOffset;
                        }
                        // signal a horizontal correction with a low note
                        if (outputDebug) {
                            Note.play(6, 250);
                        }
                    } else {
                        // crossed a vertical line
                        float sensorXOffset = (float) Math.cos(theta) * SENSOR_OFFSET;
                        // offset x to account for sensor distance, using the odo average as the position of the sensor
                        float xx = (leftOdo.x + rightOdo.x) / 2 + sensorXOffset;
                        // snap x to closest line
                        xx = Math.round((xx + 0) / Tile.ONE) * Tile.ONE - 0;
                        // correct x, removing the offset
                        synchronized (lock) {
                            x = xx - sensorXOffset;
                        }
                        // signal a vertical correction with a high note
                        if (outputDebug) {
                            Note.play(5, 250);
                        }
                    }
                    crossFlags = 0;
                } else if ((crossFlags & 0x3) != 0) {
                    // only one sensor has crossed
                    final Position crossedPosition;
                    if ((crossFlags & 0x3) == 0x1) {
                        // left has crossed
                        crossedPosition = leftOdo;
                    } else {
                        // right has crossed
                        crossedPosition = rightOdo;
                    }
                    // get the distance since the line was crossed by one of the sensors
                    float diffX = crossedPosition.x - x;
                    float diffY = crossedPosition.y - y;
                    float distance = diffX * diffX + diffY * diffY;
                    // cancel correction if we're too far away, to prevent errors when encountering the next line
                    if (distance >= Tile.QUARTER * Tile.QUARTER) {
                        // reset sensors as uncrossed
                        crossFlags = 0;
                    }
                }
            }

            /*
             * DEBUG
             */

            if (performCorrection) {
                leftLightSensor.forceSample();
                rightLightSensor.forceSample();
            }
            if (outputDebug) {
                updateDebugDisplay();
            }

			/*
             * SLEEP
		     */

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

    private void updateDebugDisplay() {
        Display.update("ox", Float.toString(x));
        Display.update("oy", Float.toString(y));
        Display.update("ot", Float.toString((float) Math.toDegrees(theta)));
    }

    /**
     * Returns the current x coordinate.
     *
     * @return The x coordinate
     */
    public float getX() {
        synchronized (lock) {
            return x;
        }
    }

    /**
     * Returns the current y coordinate.
     *
     * @return The y coordinate
     */
    public float getY() {
        synchronized (lock) {
            return y;
        }
    }

    /**
     * Returns the current theta (heading) angle in radians.
     *
     * @return The theta angle
     */
    public float getTheta() {
        synchronized (lock) {
            return theta;
        }
    }

    /**
     * Sets the position to 3 specified values
     * @param setX float, the new x position
     * @param setY float, the new y position
     * @param setT float, the new theta position
     */
    public void setPosition(float setX, float setY, float setT){
    	synchronized (lock) {
        	x = setX;
        	y = setY;
        	theta = setT;
    	}
    }

    /**
     * Returns the current x, y and theta as a position object.
     *
     * @return All the position information
     */
    public Position getPosition() {
        synchronized (lock) {
            return new Position(x, y, theta);
        }
    }

    /**
     * Represents an immutable position with a heading (x, y and theta).
     */
    public static class Position {
        /**
         * The x coordinate
         */
        public final float x;
        /**
         * The y coordinate
         */
        public final float y;
        /**
         * The theta (heading) angle
         */
        public final float theta;

        /**
         * Constructs a new position object from the x, y and theta values.
         *
         * @param x The x coordinate
         * @param y The y coordinate
         * @param theta The theta (heading) angle
         */
        public Position(float x, float y, float theta) {
            this.x = x;
            this.y = y;
            this.theta = theta;
        }
    }
}
