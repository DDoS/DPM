/*
This file is part of DPM, licensed under the MIT License (MIT).

Copyright (c) 2014 Team 7

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

/**
 * A class to control the claw positions. All methods block until movement is complete. The sensing position is meant to place the color sensor at the tip of the claw at the correct height for
 * detecing the color of the blocks used in search and rescue. The claw should initialy be opened as far as it will go as this is consired the 0 degree position.
 * <p/>
 * THREAD SAFE
 */
public class Claw {
    public static final int OPENED_ANGLE = 0;
    public static final int CLOSED_ANGLE = 125;
    public static final int SENSING_ANGLE = 78;
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

    /**
     * Gets the angle in degrees of the claw motor
     */
    public int getAngle() {
        return motor.getTachoCount();
    }
}
