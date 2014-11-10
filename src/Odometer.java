import lejos.nxt.*;

/**
 * An odometer class which runs its own thread to monitor the wheel rotation and update the position information every 25 ms.
 * <p/>
 * THREAD SAFE
 */
public class Odometer extends Thread {
    // odometer update period, in ms
    private static final long PERIOD = 10;
    // Various PI ratios
    private static final float TWO_PI = (float) Math.PI * 2;
    private static final float ONE_QUARTER_PI = (float) Math.PI / 4;
    private static final float THREE_QUARTER_PI = 3 * ONE_QUARTER_PI;
    private static final float FIVE_QUARTER_PI = 5 * ONE_QUARTER_PI;
    private static final float SEVEN_QUARTER_PI = 7 * ONE_QUARTER_PI;
    // Robot design parameters
    public static final float WHEEL_RADIUS = 2.05f;
    public static final float WHEEL_DISTANCE = 14.2f;
    // Max light value reading for a grid line
    private static final int LINE_LIGHT = 400;
    // The distance of the sensor from the wheel axle
    private static final float SENSOR_OFFSET = 4.5f;
    // Spacing of the tiles in centimeters
    private static final float TILE_SPACING = 30.48f;
    // Half the said spacing
    private static final float HALF_TILE_SPACING = TILE_SPACING / 2;
    // Whether or not the odometer is running
    private volatile boolean running = false;
    // robot position
    private float x = 0, y = 0, theta = (float) Math.PI / 2;
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
    private final FilteredLightSensor lightSensor;

    /**
     * Constructs a new odometer from the left and right motors of the robot and the light sensor for correction (can be null if this won't ever be used).
     *
     * @param leftMotor The left motor
     * @param rightMotor The right motor
     * @param lightSensor The correction light sensor
     */
    public Odometer(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, FilteredLightSensor lightSensor) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.lightSensor = lightSensor;
    }

    /**
     * Whether or not to enable the position correction using the light sensor. Do not enable this if the odometer was constructed using a null light sensor.
     *
     * @param enabled The enable state
     */
    public void enableCorrection(boolean enabled) {
        performCorrection = enabled;
        if (running) {
            lightSensor.setFloodlight(performCorrection);
        }
    }

    public void enableDebugOutput(boolean enable) {
        if (enable) {
            Display.reserve("ox", "oy", "ot");
        } else {
            Display.remove("ox");
            Display.remove("oy");
            Display.remove("ot");
        }
        outputDebug = enable;
    }

    /**
     * Runs the odometry code.
     */
    public void run() {
        long updateStart, updateEnd;
        // set the line as un-crossed
        boolean crossed = false;
        // set the sensor flood on if we're using it
        if (performCorrection) {
            lightSensor.setFloodlight(true);
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
                lightSensor.getLightData();
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
            float deltaRhoRadius = deltaRho * WHEEL_RADIUS;
            float deltaLambdaRadius = deltaLambda * WHEEL_RADIUS;
            // compute delta C
            float deltaC = (deltaRhoRadius + deltaLambdaRadius) / 2;
            // compute delta theta and it's half
            float deltaTheta = (deltaRhoRadius - deltaLambdaRadius) / WHEEL_DISTANCE;
            float halfDeltaTheta = deltaTheta / 2;
            // compute delta x and y, using y forward and a right handed system (x right)
            float deltaX = deltaC * (float) Math.cos(theta + halfDeltaTheta);
            float deltaY = deltaC * (float) Math.sin(theta + halfDeltaTheta);
            // update position
            synchronized (lock) {
                // update x, y, and theta by their deltas
                x += deltaX;
                y += deltaY;
                theta = wrapAngle(theta + deltaTheta);
            }

			/*
             * CORRECTION
			 */

            if (performCorrection) {
                // read the light value
                int lightValue = lightSensor.getLightData();
                // check if the light value corresponds to a line and it has yet to be crossed
                if (lightValue <= LINE_LIGHT && !crossed) {
                    // check which line direction we just crossed using the heading
                    if (theta >= ONE_QUARTER_PI && theta < THREE_QUARTER_PI || theta >= FIVE_QUARTER_PI && theta < SEVEN_QUARTER_PI) {
                        // cross horizontal line
                        float sensorYOffset = (float) Math.sin(theta) * SENSOR_OFFSET;
                        // offset y to account for sensor distance
                        float yy = y + sensorYOffset;
                        // snap y to closest line
                        yy = Math.round((yy + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
                        // correct y, removing the offset
                        synchronized (lock) {
                            y = yy - sensorYOffset;
                        }
                        // signal a horizontal correction with a low note
                        if (outputDebug) {
                            Sound.playNote(Sound.FLUTE, 440, 250);
                        }
                    } else {
                        // cross vertical line
                        float sensorXOffset = (float) Math.cos(theta) * SENSOR_OFFSET;
                        // offset x to account for sensor distance
                        float xx = x + sensorXOffset;
                        // snap x to closest line
                        xx = Math.round((xx + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
                        // correct x, removing the offset
                        synchronized (lock) {
                            x = xx - sensorXOffset;
                        }
                        // signal a vertical correction with a high note
                        if (outputDebug) {
                            Sound.playNote(Sound.FLUTE, 880, 250);
                        }
                    }
                    // set the line as crossed to prevent repeated events
                    crossed = true;
                } else {
                    // mark the line as done being crossed
                    crossed = false;
                }
            }

            /*
             * DEBUG
             */

            if (outputDebug) {
                updateDebugDisplay();
            }

			/*
             * SLEEP
		     */

            if (performCorrection) {
                lightSensor.getLightData();
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
     * Wraps an angle between 0 (inclusive) and 2pi (exclusive). This is a utility method exposed for usage by other classes. <p/> TODO: move this to another class?
     *
     * @param rads The angle to wrap in radians.
     * @return The wrapped angle in radians
     */
    public static float wrapAngle(float rads) {
        return ((rads % TWO_PI) + TWO_PI) % TWO_PI;
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
