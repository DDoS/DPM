import lejos.nxt.*;

public class Group7Robot {
	public static void main(String[] args) {
		// MOTORS
		NXTRegulatedMotor leftMotor = Motor.A;
		NXTRegulatedMotor rightMotor = Motor.C;
		NXTRegulatedMotor clawMotor = Motor.B;

		// SENSORS
		FilteredLightSensor lightSensor = new FilteredLightSensor(SensorPort.S1);
		FilteredUltrasonicSensor ultrasonicSensor = new FilteredUltrasonicSensor(SensorPort.S1);

		// HELPER THREADS
		Odometer odometer = new Odometer(leftMotor, rightMotor, lightSensor);
		Navigation navigation = new Navigation(leftMotor, rightMotor, odometer);

		// CONTROLLERS
		LocalizationController localization = new LocalizationController(navigation, null, ultrasonicSensor);

		// LOGIC
		odometer.start();
		navigation.start();

		Button.waitForAnyPress();

		// logic for presentation vid
		// TODO: use claw correctly
		// TODO: do quick callibration so navig works (Odometer WHEEL_RADIUS and WHEEL_DISTANCE)
		clawMotor.rotate(180);
		navigation.forward(60);
		navigation.waitUntilDone();
		clawMotor.rotate(-180);
		navigation.forward(-60);
		navigation.waitUntilDone();
	}
}
