import lejos.nxt.*;

/**
 * Performs navigation commands on a separate thread, which allows for sensor monitoring to be done on the calling thread. Commands can be aborted or the calling thread can be blocked until they
 * complete. Only one command at a time can be issued, any current command will be aborted.
 * <p/>
 * THREAD SAFE
 */
public class Navigation extends Thread {
    private static final float TARGET_DISTANCE_MULTIPLIER = 1.025f;
    // Motor speed constants
    private static final int MOTOR_STRAIGHT = 200;
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
     * Travel by a relative distance in centimeters.
     *
     * @param x The x distance
     * @param y The y distance
     */
    public void travelBy(float x, float y) {
        Odometer.Position pos = odometer.getPosition();
        travelTo(pos.x + x, pos.y + y);
    }

    /**
     * Travel to absolute coordinates in centimeters.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void travelTo(float x, float y) {
        command = new Travel(x, y);
        startCommand();
    }

    /**
     * Turns by a relative angle in radians.
     *
     * @param theta The angle
     */
    public void turnBy(float theta) {
        turnTo(odometer.getTheta() + theta);
    }

    /**
     * Turns to an absolute angle in radians.
     *
     * @param theta The angle
     */
    public void turnTo(float theta) {
        command = new Turn(theta);
        startCommand();
    }

    /**
     * Moves forward by the specified distance.
     *
     * @param distance The distance to travel forward
     */
    public void forward(float distance) {
        float theta = odometer.getTheta();
        float x = (float) Math.cos(theta) * distance;
        float y = (float) Math.sin(theta) * distance;
        travelBy(x, y);
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

    private void doTravel(float x, float y) {
        // Set motor speds
        leftMotor.setSpeed(MOTOR_STRAIGHT);
        rightMotor.setSpeed(MOTOR_STRAIGHT);
        // Find turn angle
        float startX = odometer.getX();
        float startY = odometer.getY();
        float differenceX = x - startX;
        float differenceY = y - startY;
        // Do turn
        doTurn((float) Math.atan2(differenceY, differenceX));
        // Set motors forward
        leftMotor.forward();
        rightMotor.forward();
        // A bit larger than the square of the distance to the target
        float distanceToTarget = differenceX * differenceX + differenceY * differenceY * TARGET_DISTANCE_MULTIPLIER;
        // Main loop
        while (true) {
            // check for thread interruption, aka command abort
            if (interrupted()) {
                // end early
                break;
            }
            // Check for target reached
            differenceX = startX - odometer.getX();
            differenceY = startY - odometer.getY();
            if (differenceX * differenceX + differenceY * differenceY >= distanceToTarget) {
                break;
            }
        }
        // complete command
        endCommand();
    }

    private void doTurn(float theta) {
        // Set motor speeds
        leftMotor.setSpeed(MOTOR_STRAIGHT);
        rightMotor.setSpeed(MOTOR_STRAIGHT);
        // Find min angle difference
        float difference = theta - odometer.getTheta();
        if (difference >= (float) Math.PI) {
            difference = -2 * (float) Math.PI + difference;
        } else if (difference <= (float) -Math.PI) {
            difference = 2 * (float) Math.PI + difference;
        }
        // Compute wheel rotation in angle
        float wheelRotation = (difference * Odometer.WHEEL_DISTANCE / Odometer.WHEEL_RADIUS) / 2;
        int rotationDegrees = (int) Math.round(Math.toDegrees(wheelRotation));
        // Rotate
        leftMotor.rotate(-rotationDegrees, true);
        rightMotor.rotate(rotationDegrees, true);
        // Wait for completion
        while (leftMotor.isMoving() || rightMotor.isMoving()) {
            // check for thread interruption, aka command abort
            if (interrupted()) {
                // end early
                break;
            }
        }
        endCommand();
    }

    private void endCommand() {
        // Stop travel
        leftMotor.stop(true);
        rightMotor.stop(false);
    }

    // A command to travel to absolute coordinates
    private class Travel implements Runnable {
        private final float x, y;

        private Travel(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void run() {
            doTravel(x, y);
        }
    }

    // A command to rotate to an absolute angle
    private class Turn implements Runnable {
        private final float theta;

        private Turn(float theta) {
            this.theta = Odometer.wrapAngle(theta);
        }

        public void run() {
            doTurn(theta);
        }
    }
}
