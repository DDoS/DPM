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
