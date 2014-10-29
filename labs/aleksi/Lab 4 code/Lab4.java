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
