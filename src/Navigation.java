import lejos.nxt.*;

/**
 * Performs navigation commands on a separate thread, which allows for sensor monitoring to be done on the calling thread. Commands can be aborted or the calling thread can be blocked until they
 * complete. Only one command at a time can be issued, any current command will be aborted.
 * <p/>
 * THREAD SAFE
 */
public class Navigation extends Thread {
    // Motor speed constants
    private static final int MOTOR_SPEED = 200;
    private static final int MOTOR_ACCELERATION = 1000;
    // Robot design contants
    private static final float CLAW_DOWN_EXTENSION = 4;
    // Motors (left and right)
    private final NXTRegulatedMotor leftMotor;
    private final NXTRegulatedMotor rightMotor;
    // Odometer instance
    private final Odometer odometer;
    // Properties for thread control and command execution
    private final Object condition = new Object();
    private volatile boolean navigating = false;
    private volatile Runnable command = null;
    private volatile Thread runner = null;
    // Whether or not to use special forward navigation when carrying blocks
    private volatile boolean clawDownMode = false;

    /**
     * Created a new navigation class from the odometer to use to obtain position information for the robot.
     *
     * @param odometer The odometer that will provide the position information
     */
    public Navigation(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, Odometer odometer) {
        this.leftMotor = leftMotor;
        this.rightMotor = rightMotor;
        this.odometer = odometer;
    }

    /**
     * Runs the navigation commands.
     */
    public void run() {
        // Register the current thread as the runner, so we can interrupt it
        runner = Thread.currentThread();
        while (true) {
            // Wait util a command is sent
            while (command == null) {
                try {
                    synchronized (condition) {
                        // Set navigation to false and start waiting
                        navigating = false;
                        condition.wait();
                    }
                } catch (InterruptedException ignored) {
                }
            }
            // Get the command and remove it
            Runnable c = command;
            command = null;
            // Execute it
            c.run();
        }
    }

    /**
     * A special mode which reduces forward motion a bit to account for a longer robot caused by the claw being down.
     *
     * @param enable Whether or not to enable the claw down mode
     */
    public void enableClawDownMode(boolean enable) {
        clawDownMode = enable;
    }

    /**
    * Travel by a relative distance in centimeters at the default speed.
    *
    * @param x The x distance
    * @param y The y distance
    */
    public void travelBy(float x, float y) {
        travelBy(x, y, MOTOR_SPEED);
    }

    /**
     * Travel by a relative distance in centimeters at the desired speed.
     *
     * @param x The x distance
     * @param y The y distance
     * @param speed The speed
     */
    public void travelBy(float x, float y, int speed) {
        Odometer.Position pos = odometer.getPosition();
        travelTo(pos.x + x, pos.y + y, speed);
    }

    /**
    * Travel to absolute coordinates in centimeters at the default speed.
    *
    * @param x The x coordinate
    * @param y The y coordinate
    */
    public void travelTo(float x, float y) {
        travelTo(x, y, MOTOR_SPEED);
    }

    /**
     * Travel to absolute coordinates in centimeters at the desired speed.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param speed The speed
     */
    public void travelTo(float x, float y, int speed) {
        command = new Travel(x, y, speed);
        startCommand();
    }

    /**
    * Turns by a relative angle in radians at the default speed.
    *
    * @param theta The angle
    */
    public void turnBy(float theta) {
        turnBy(theta, MOTOR_SPEED);
    }

    /**
     * Turns by a relative angle in radians at the desired speed.
     *
     * @param theta The angle
     * @param speed The speed
     */
    public void turnBy(float theta, int speed) {
        turnTo(odometer.getTheta() + theta, speed);
    }

    /**
    * Turns to an absolute angle in radians at the default speed.
    *
    * @param theta The angle
    */
    public void turnTo(float theta) {
        turnTo(theta, MOTOR_SPEED);
    }

    /**
     * Turns to an absolute angle in radians at the desired speed.
     *
     * @param theta The angle
     * @param speed The speed
     */
    public void turnTo(float theta, int speed) {
        command = new Turn(theta, speed);
        startCommand();
    }

    /**
    * Moves backward by the specified distance at the default speed.
    *
    * @param distance The distance to travel backward
    */
    public void backward(float distance) {
        backward(distance, MOTOR_SPEED);
    }

    /**
    * Moves backward by the specified distance at the desired speed.
    *
    * @param distance The distance to travel backward
    * @param speed The speed
    */
    public void backward(float distance, int speed) {
        forward(-distance, speed);
    }

    /**
    * Moves forward by the specified distance at the default speed.
    *
    * @param distance The distance to travel forward
    */
    public void forward(float distance) {
        forward(distance, MOTOR_SPEED);
    }

    /**
     * Moves forward by the specified distance at the desired speed.
     *
     * @param distance The distance to travel forward
     * @param speed The speed
     */
    public void forward(float distance, int speed) {
        float theta = odometer.getTheta();
        float x = (float) Math.cos(theta) * distance;
        float y = (float) Math.sin(theta) * distance;
        travelBy(x, y, speed);
    }

    /**
     * Return true if robot is navigating (command is under execution)
     *
     * @return Whether or not the robot it navigating
     */
    public boolean isNavigating() {
        return navigating;
    }

    /**
     * Wait until navigation is over, by yielding the calling thread until {@link #isNavigating()} returns false.
     */
    public void waitUntilDone() {
        while (navigating) {
            Thread.yield();
        }
    }

    /**
     * Aborts any navigation in progress.
     */
    public void abort() {
        if (navigating) {
            runner.interrupt();
        }
    }

    /**
     * Getter for the odometer
     * @return the odometer
     */
    public Odometer getOdometer(){
    	return odometer;
    }

    // Starts a navigation command
    private void startCommand() {
        if (navigating) {
            // Interrupt it running
            runner.interrupt();
        } else {
            // Else set navigation stet and notify runner
            synchronized (condition) {
                navigating = true;
                condition.notify();
            }
        }
    }

    private void doTravel(float x, float y, int speed) {
        // Set motor speeds and acceleration
        leftMotor.setSpeed(speed);
        leftMotor.setAcceleration(MOTOR_ACCELERATION);
        rightMotor.setSpeed(speed);
        rightMotor.setAcceleration(MOTOR_ACCELERATION);
        // Find delta of movement
        float differenceX = x - odometer.getX();
        float differenceY = y - odometer.getY();
        // Compute movement: find turn angle and travel distance
        float theta = (float) Math.atan2(differenceY, differenceX);
        float distance = (float) Math.sqrt(differenceX * differenceX + differenceY * differenceY);
        // Negate values if going backwards will be faster
        if (getAngleDiff(theta, odometer.getTheta()) > (clawDownMode ? Pi.THREE_QUARTER : Pi.ONE_HALF)) {
            theta = Pi.wrapAngle(theta - Pi.ONE);
            distance = -distance;
        } else if (clawDownMode) {
            distance -= CLAW_DOWN_EXTENSION;
        }
        // Do turn
        doTurn(theta, speed);
        // Calculate the rotation to apply to the wheels
        int rotationDegreesLeft = (int) Math.round(Math.toDegrees(distance / Odometer.WHEEL_RADIUS_LEFT));
        int rotationDegreesRight = (int) Math.round(Math.toDegrees(distance / Odometer.WHEEL_RADIUS_RIGHT));
        // Move the robot
        leftMotor.rotate(rotationDegreesLeft, true);
        rightMotor.rotate(rotationDegreesRight, true);
        // Wait for completion
        while (leftMotor.isMoving() || rightMotor.isMoving()) {
            // check for thread interruption, aka command abort
            if (interrupted()) {
                // end early
                break;
            }
            Thread.yield();
        }
        endCommand();
    }

    private void doTurn(float theta, int speed) {
        // Set motor speeds and acceleration
        leftMotor.setSpeed(speed);
        leftMotor.setAcceleration(MOTOR_ACCELERATION);
        rightMotor.setSpeed(speed);
        rightMotor.setAcceleration(MOTOR_ACCELERATION);
        // Find min angle difference
        float difference = theta - odometer.getTheta();
        if (difference >= Pi.ONE) {
            difference = -Pi.TWO + difference;
        } else if (difference <= -Pi.ONE) {
            difference = Pi.TWO + difference;
        }
        // Compute wheel rotation in angle
        float wheelRotationLeft = (difference * Odometer.WHEEL_DISTANCE / Odometer.WHEEL_RADIUS_LEFT) / 2;
        float wheelRotationRight = (difference * Odometer.WHEEL_DISTANCE / Odometer.WHEEL_RADIUS_RIGHT) / 2;
        int rotationDegreesLeft = (int) Math.round(Math.toDegrees(wheelRotationLeft));
        int rotationDegreesRight = (int) Math.round(Math.toDegrees(wheelRotationRight));
        // Rotate
        leftMotor.rotate(-rotationDegreesLeft, true);
        rightMotor.rotate(rotationDegreesRight, true);
        // Wait for completion
        while (leftMotor.isMoving() || rightMotor.isMoving()) {
            // check for thread interruption, aka command abort
            if (interrupted()) {
                // end early
                break;
            }
            Thread.yield();
        }
        endCommand();
    }

    private void endCommand() {
        // Stop travel
        leftMotor.stop(true);
        rightMotor.stop(false);
    }

    // Get the absolute difference between two angles
    private static double getAngleDiff(float a, float b) {
        // Convert to vectors and use dot product to compute angle in between
        return Math.abs((float) Math.acos(Math.cos(a) * Math.cos(b) + Math.sin(a) * Math.sin(b)));
    }


    // A command to travel to absolute coordinates
    private class Travel implements Runnable {
        private final float x, y;
        private final int speed;

        private Travel(float x, float y, int speed) {
            this.x = x;
            this.y = y;
            this.speed = speed;
        }

        public void run() {
            doTravel(x, y, speed);
        }
    }

    // A command to rotate to an absolute angle
    private class Turn implements Runnable {
        private final float theta;
        private final int speed;

        private Turn(float theta, int speed) {
            this.theta = Pi.wrapAngle(theta);
            this.speed = speed;
        }

        public void run() {
            doTurn(theta, speed);
        }
    }
}
