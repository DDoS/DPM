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
  Lab 5: Orienteering
  Author: Aleksi Sapon (260581670)
  Date: Wednesday October 15 2014
 */
public class Lab5 {
	public static void main(String[] args) throws Exception {
		// allows you to force program end by holding the escape button
		startShitHappensThread();
		// setup the odometer, navigation, display, and ultrasonic sensor
		Odometer odo = new Odometer();
		odo.start();
		Navigation navig = new Navigation(odo);
		navig.start();
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		// wait for a button press before starting
		Button.waitForAnyPress();
		// perform the orienteering
		Orienteer orienteer = new Orienteer(odo, navig, us, Orienteer.OrienteerType.STOCHASTIC);
		orienteer.doOrienteering();
		// Notify that orienteering succeeded (returned)
		Sound.playNote(Sound.FLUTE, 220, 250);
		// Wait for another press before moving to file position
		Button.waitForAnyPress();
		// Move to the top corner and face north
		orienteer.moveTo(3, 3, Orienteer.Direction.NORTH);
		// Wait for another press before exiting
		Button.waitForAnyPress();
		System.exit(0);
	}

	// Starts a thread which polls the state of the escape button every second
	// and exits the JVM if it's being held down
	private static void startShitHappensThread() {
		new Thread() {
			public void run() {
				while (true) {
					if (Button.ESCAPE.isDown()) {
						System.exit(0);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						// ignore
					}
				}
			}
		}.start();
	}
}
