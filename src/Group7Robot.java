import lejos.nxt.*;

public class Group7Robot {
	public static void main(String[] args) {
		// MOTORS
		NXTRegulatedMotor leftMotor = Motor.A;
		NXTRegulatedMotor rightMotor = Motor.C;
		NXTRegulatedMotor clawMotor = Motor.B;

		// SENSORS
		FilteredLightSensor lightSensor = new FilteredLightSensor(SensorPort.S1);
		FilteredUltrasonicSensor ultrasonicSensor = new FilteredUltrasonicSensor(SensorPort.S2);

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
		clawMotor.setSpeed(100);
		clawMotor.rotate(-120);
		clawMotor.flt();
		navigation.forward(60);
		navigation.waitUntilDone();
		clawMotor.rotate(120);
		clawMotor.flt();
		navigation.forward(-60);
		navigation.waitUntilDone();

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);
	}
}
