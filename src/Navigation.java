import lejos.nxt.*;

/**
 * Performs navigation commands on a separate thread, which allows for sensor monitoring to be done on the calling thread. Commands can be aborted or the calling thread can be blocked until they
 * complete. Only one command at a time can be issued, any current command will be aborted.
 * <p/>
 * THREAD SAFE
 */
public class Navigation extends Thread {
    private static final double NAVIGATION_EPSILON = 0.5;
    // Motor speed constants
    private static final int MOTOR_STRAIGHT = 200;
    // Motors (left and right)
    private final NXTRegulatedMotor leftMotor = Motor.A;
    private final NXTRegulatedMotor rightMotor = Motor.C;
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
    public Navigation(Odometer odometer) {
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
    public void travelBy(double x, double y) {
        Odometer.Position pos = odometer.getPosition();
        travelTo(pos.x + x, pos.y + y);
    }

    /**
     * Travel to absolute coordinates in centimeters.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public void travelTo(double x, double y) {
        command = new Travel(x, y);
        startCommand();
    }

    /**
     * Turns by a relative angle in radians.
     *
     * @param theta The angle
     */
    public void turnBy(double theta) {
        turnTo(odometer.getTheta() + theta);
    }

    /**
     * Turns to an absolute angle in radians.
     *
     * @param theta The angle
     */
    public void turnTo(double theta) {
        command = new Turn(theta);
        startCommand();
    }

    /**
     * Moves forward by the specified distance.
     *
     * @param distance The distance to travel forward
     */
    public void forward(double distance) {
        double theta = odometer.getTheta();
        double x = Math.cos(theta) * distance;
        double y = Math.sin(theta) * distance;
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

    private void doTravel(double x, double y) {
        // Set motor speds
        leftMotor.setSpeed(MOTOR_STRAIGHT);
        rightMotor.setSpeed(MOTOR_STRAIGHT);
        // Find turn angle
        double differenceX = x - odometer.getX();
        double differenceY = y - odometer.getY();
        // Do turn
        doTurn(Math.atan2(differenceY, differenceX));
        // Set motors forward
        leftMotor.forward();
        rightMotor.forward();
        // Main loop
        while (true) {
            // check for thread interruption, aka command abort
            if (interrupted()) {
                // end early
                break;
            }
            // Check for target reached
            differenceX = x - odometer.getX();
            differenceY = y - odometer.getY();
            if (differenceX * differenceX + differenceY * differenceY < NAVIGATION_EPSILON * NAVIGATION_EPSILON) {
                break;
            }
        }
        // complete command
        endCommand();
    }

    private void doTurn(double theta) {
        // Set motor speeds
        leftMotor.setSpeed(MOTOR_STRAIGHT);
        rightMotor.setSpeed(MOTOR_STRAIGHT);
        // Find min angle difference
        double difference = theta - odometer.getTheta();
        if (difference >= Math.PI) {
            difference = -2 * Math.PI + difference;
        } else if (difference <= -Math.PI) {
            difference = 2 * Math.PI + difference;
        }
        // Compute wheel rotation in angle
        double wheelRotation = (difference * Odometer.WHEEL_DISTANCE / Odometer.WHEEL_RADIUS) / 2;
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
        private final double x, y;

        private Travel(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void run() {
            doTravel(x, y);
        }
    }

    // A command to rotate to an absolute angle
    private class Turn implements Runnable {
        private final double theta;

        private Turn(double theta) {
            this.theta = Odometer.wrapAngle(theta);
        }

        public void run() {
            doTurn(theta);
        }
    }
}
