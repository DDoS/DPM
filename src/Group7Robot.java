import lejos.nxt.*;

public class Group7Robot {
	public static void main(String[] args) {
		// MOTORS
		NXTRegulatedMotor leftMotor = Motor.A;
		NXTRegulatedMotor rightMotor = Motor.C;
		NXTRegulatedMotor clawMotor = Motor.B;

		// SENSORS
		FilteredUltrasonicSensor ultrasonicSensor = new FilteredUltrasonicSensor(SensorPort.S1);
		FilteredLightSensor leftLightSensor = new FilteredLightSensor(SensorPort.S2);
		FilteredLightSensor rightLightSensor = new FilteredLightSensor(SensorPort.S3);
		FilteredColorSensor colorSensor = new FilteredColorSensor(SensorPort.S4);

		// ACTUATORS
		Claw claw = new Claw(clawMotor);

		// HELPER THREADS
		Odometer odometer = new Odometer(leftMotor, rightMotor, leftLightSensor, rightLightSensor);
		Navigation navigation = new Navigation(leftMotor, rightMotor, odometer);

		// MAP
		Map map;
		//The pattern given to us in the project specifications
		int[][] arr = {
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
				{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
				{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}

		};

		//A smaller test pattern
		int[][] arr2 = {
				{0, 0, 1},
				{1, 0, 1},
				{0, 0, 0}
		};
		//Test for midterm
		int[][] arr3 = {
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{1, 0, 0, 1, 0, 0, 0, 0},
				{0, 1, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0},
				{1, 0, 0, 0, 0, 0, 0, 0}

		};

		//Test for small scale
		int[][] arr4 = {
				{3, 1, 2, 0},
				{0, 0, 0, 1},
				{1, 0, 0, 0},
				{1, 0, 1, 0}
		};

		//Initialize the map so we can set it to whichever array we pass in
		map = new Map(arr4);

		// CONTROLLERS
		SearchAndRescueController searchAndRescue = new SearchAndRescueController(navigation, map, colorSensor, claw);
		LocalizationController localization = new LocalizationController(navigation, map, ultrasonicSensor, null, searchAndRescue);

		// LOGIC
		odometer.start();
		navigation.start();

		/*
		Button.waitForAnyPress();
		odometer.enableCorrection(false);
		localization.run();
		*/

		/*
		odometer.enableCorrection(false);
		Button.waitForAnyPress();
		navigation.travelTo(0, Odometer.TILE_SPACING * 2);
		navigation.waitUntilDone();
		Button.waitForAnyPress();
		for (int i = 0; i < 8; i++) {
			navigation.turnBy((float) Math.PI / 2);
			navigation.waitUntilDone();
		}
		*/

		/*
		odometer.enableDebugOutput(true);
		odometer.enableCorrection(true);

		navigation.travelTo(0, 0);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		navigation.travelTo(60, 0);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		navigation.travelTo(0, 0);
		navigation.waitUntilDone();
		Button.waitForAnyPress();
		*/

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);

	}
}
