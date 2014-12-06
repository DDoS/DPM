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

public class Lab3 {
	public static void main(String[] args) {
		Controller contoller = new Controller();

		int selection = Button.waitForAnyPress();

		switch (selection) {
			case Button.ID_LEFT:
				// run 1
				contoller.setPath(
					60, 30,
					30, 30,
					30, 60,
					60, 0
				);
				break;
			case Button.ID_RIGHT:
				// run 2
				contoller.enableAvoidance(true);
				contoller.setPath(
					0, 60,
					60, 0
				);
				break;
			default:
				return;
		}

		contoller.start();

		do {
			LCD.clear();
			LCD.drawString(Float.toString((float) contoller.getX()), 0, 0);
			LCD.drawString(Float.toString((float) contoller.getY()), 0, 1);
			LCD.drawString(Float.toString((float) contoller.getTheta()), 0, 2);
			try {
				Thread.sleep(500);
			} catch (InterruptedException ex) {
				// not expected
			}
		} while (Button.ESCAPE.isUp());

		System.exit(0);
	}
}
