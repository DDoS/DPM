import lejos.nxt.*;

/**
 * An odometer class which runs its own thread to monitor the wheel rotation and update the position information every 25 ms.
 * <p/>
 * THREAD SAFE
 */
public class Odometer extends Thread {
    // odometer update period, in ms
    private static final long PERIOD = 25;
    // Various PI ratios
    private static final double TWO_PI = Math.PI * 2;
    private static final double ONE_QUARTER_PI = Math.PI / 4;
    private static final double THREE_QUARTER_PI = 3 * ONE_QUARTER_PI;
    private static final double FIVE_QUARTER_PI = 5 * ONE_QUARTER_PI;
    private static final double SEVEN_QUARTER_PI = 7 * ONE_QUARTER_PI;
    // Robot design parameters
    public static final double WHEEL_RADIUS = 2.05;
    public static final double WHEEL_DISTANCE = 14.2;
    // Max light value reading for a grid line
    private static final int LINE_LIGHT = 35;
    // The distance of the sensor from the wheel axle
    private static final double SENSOR_OFFSET = 4.5;
    // Spacing of the tiles in centimeters
    private static final double TILE_SPACING = 30.48;
    // Half the said spacing
    private static final double HALF_TILE_SPACING = TILE_SPACING / 2;
    // Whether or not the odometer is running
    private volatile boolean running = false;
    // robot position
    private double x = 0, y = 0, theta = Math.PI / 2;
    // Tachometer last readings in radians, for right and left
    private double lastRho = 0, lastLambda = 0;
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

			/*
             * CORRECTION
			 */

            if (performCorrection) {
                // read the light value
                int lightValue = lightSensor.getLightData();
                //Display.update("5", Integer.toString(lightValue));
                // check if the light value corresponds to a line and it has yet to be crossed
                if (lightValue <= LINE_LIGHT && !crossed) {
                    // check which line direction we just crossed using the heading
                    if (theta >= ONE_QUARTER_PI && theta < THREE_QUARTER_PI || theta >= FIVE_QUARTER_PI && theta < SEVEN_QUARTER_PI) {
                        // cross horizontal line
                        double sensorYOffset = Math.sin(theta) * SENSOR_OFFSET;
                        // offset y to account for sensor distance
                        double yy = y + sensorYOffset;
                        // snap y to closest line
                        yy = Math.round((yy + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
                        // correct y, removing the offset
                        synchronized (lock) {
                            y = yy - sensorYOffset / 2;
                        }
                        // signal a horizontal correction with a low note
                        if (outputDebug) {
                            Sound.playNote(Sound.FLUTE, 440, 250);
                        }
                    } else {
                        // cross vertical line
                        double sensorXOffset = Math.cos(theta) * SENSOR_OFFSET;
                        // offset x to account for sensor distance
                        double xx = x + sensorXOffset;
                        // snap x to closest line
                        xx = Math.round((xx + HALF_TILE_SPACING) / TILE_SPACING) * TILE_SPACING - HALF_TILE_SPACING;
                        // correct x, removing the offset
                        synchronized (lock) {
                            x = xx - sensorXOffset / 2;
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
        Display.update("ox", Double.toString(x));
        Display.update("oy", Double.toString(y));
        Display.update("ot", Double.toString(theta));
    }

    /**
     * Returns the current x coordinate.
     *
     * @return The x coordinate
     */
    public double getX() {
        synchronized (lock) {
            return x;
        }
    }

    /**
     * Returns the current y coordinate.
     *
     * @return The y coordinate
     */
    public double getY() {
        synchronized (lock) {
            return y;
        }
    }

    /**
     * Returns the current theta (heading) angle in radians.
     *
     * @return The theta angle
     */
    public double getTheta() {
        synchronized (lock) {
            return theta;
        }
    }

    /**
     * Sets the position to 3 specified values
     * @param setX double, the new x position
     * @param setY double, the new y position
     * @param setT double, the new theta position
     */
    public void setPosition(double setX, double setY, double setT){
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
    public static double wrapAngle(double rads) {
        return ((rads % TWO_PI) + TWO_PI) % TWO_PI;
    }

    /**
     * Represents an immutable position with a heading (x, y and theta).
     */
    public static class Position {
        /**
         * The x coordinate
         */
        public final double x;
        /**
         * The y coordinate
         */
        public final double y;
        /**
         * The theta (heading) angle
         */
        public final double theta;

        /**
         * Constructs a new position object from the x, y and theta values.
         *
         * @param x The x coordinate
         * @param y The y coordinate
         * @param theta The theta (heading) angle
         */
        public Position(double x, double y, double theta) {
            this.x = x;
            this.y = y;
            this.theta = theta;
        }
    }
}
