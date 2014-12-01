import lejos.nxt.*;

/**
 * A class to control the claw positions. All methods block until movement is complete. The sensing position is meant to place the color sensor at the tip of the claw at the correct height for
 * detecing the color of the blocks used in search and rescue. The claw should initialy be opened as far as it will go as this is consired the 0 degree position.
 * <p/>
 * THREAD SAFE
 */
public class Claw {
    private static final int OPENED_ANGLE = 0;
    private static final int CLOSED_ANGLE = 125;
    private static final int SENSING_ANGLE = 75;
    private final NXTRegulatedMotor motor;

    /**
     * Constructs a new claw from the motor that controls it.
     *
     * @param motor The control motor
     */
    public Claw(NXTRegulatedMotor motor) {
        this.motor = motor;
        motor.setSpeed(200);
        motor.resetTachoCount();
    }

    /**
     * Moves the claw to it's opened position.
     */
    public void open() {
        motor.rotateTo(OPENED_ANGLE);
    }

    /**
     * Moves the claw to it's closed position.
     */
    public void close() {
        motor.rotateTo(CLOSED_ANGLE);
    }

    /**
     * Moves the claw to it's sensing position.
     */
    public void sense() {
        motor.rotateTo(SENSING_ANGLE);
    }

    /**
     * Floats the claw motor.
     */
    public void flt() {
        motor.flt();
    }
}
