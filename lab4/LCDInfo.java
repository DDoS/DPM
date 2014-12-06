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
import lejos.util.Timer;
import lejos.util.TimerListener;

public class LCDInfo implements TimerListener {
	private static final int LCD_REFRESH = 100;
	private Odometer odo;
	private Timer lcdTimer;

	public LCDInfo(Odometer odo) {
		this.odo = odo;
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		// start the timer
		lcdTimer.start();
	}

	public void timedOut() {
		Odometer.Position pos = odo.getPosition();
		LCD.clear();
		LCD.drawString("X: " + (float) pos.x, 0, 0);
		LCD.drawString("Y: " + (float) pos.y, 0, 1);
		LCD.drawString("H: " + (int) Math.toDegrees(pos.theta), 0, 2);
		// Allow for exit anytime in case of a problem
		if (Button.ESCAPE.isDown()) {
			System.exit(0);
		}
	}
}
