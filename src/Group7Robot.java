import lejos.nxt.*;

public class Group7Robot {
	public static void main(String[] args) {
		// MOTORS
		NXTRegulatedMotor leftMotor = Motor.A;
		NXTRegulatedMotor rightMotor = Motor.C;
		NXTRegulatedMotor clawMotor = Motor.B;

		// SENSORS
		FilteredUltrasonicSensor frontUltrasonicSensor = new FilteredUltrasonicSensor(SensorPort.S1);
		FilteredUltrasonicSensor rearUltrasonicSensor = new FilteredUltrasonicSensor(SensorPort.S2);
		FilteredLightSensor lightSensor = new FilteredLightSensor(SensorPort.S3);
		FilteredColorSensor colorSensor = new FilteredColorSensor(SensorPort.S4);

		// ACTUATORS
		Claw claw = new Claw(clawMotor);

		// HELPER THREADS
		Odometer odometer = new Odometer(leftMotor, rightMotor, lightSensor);
		Navigation navigation = new Navigation(leftMotor, rightMotor, odometer);

		// CONTROLLERS
		LocalizationController localization = new LocalizationController(navigation, null, frontUltrasonicSensor, rearUltrasonicSensor);

		// LOGIC
		odometer.start();
		navigation.start();

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);
	}
}
