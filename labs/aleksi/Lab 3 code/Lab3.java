/*
 * Lab3.java
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
