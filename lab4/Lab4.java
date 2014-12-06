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

public class Lab4 {
	public static void main(String[] args) throws Exception {
		// setup the odometer, navigation, display, and ultrasonic and light sensors
		Odometer odo = new Odometer();
		odo.start();
		Navigation navig = new Navigation(odo);
		navig.start();
		LCDInfo lcd = new LCDInfo(odo);
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor ls = new ColorSensor(SensorPort.S1);
		// wait for a button press before starting
		Button.waitForAnyPress();
		// perform the ultrasonic localization
		USLocalizer usl = new USLocalizer(odo, navig, us, USLocalizer.LocalizationType.FALLING_EDGE);
		usl.doLocalization();
		// Wait before doing light
		Button.waitForAnyPress();
		// perform the light sensor localization
		LightLocalizer lsl = new LightLocalizer(odo, navig, ls);
		lsl.doLocalization();
		// Wait for another press before exiting
		Button.waitForAnyPress();
		System.exit(0);
	}
}
