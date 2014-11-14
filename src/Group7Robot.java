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

		//Initialize the map so we can set it to whichever array we pass in
		map = new Map(arr3);

		// CONTROLLERS
		SearchAndRescueController searchAndRescue = new SearchAndRescueController(navigation, map, colorSensor, claw);
		LocalizationController localization = new LocalizationController(navigation, map, ultrasonicSensor, null, searchAndRescue);

		// LOGIC
		odometer.start();
		navigation.start();

		/*
		localization.run();
		*/

		Display.reserve("1", "2", "3", "4", "5");

		odometer.enableDebugOutput(true);
		Button.waitForAnyPress();
		odometer.enableCorrection(true);

		navigation.forward(60);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		navigation.forward(-60);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		navigation.turnBy((float) Math.PI / 2);
		navigation.waitUntilDone();

		navigation.forward(60);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		navigation.forward(-60);
		navigation.waitUntilDone();
		Button.waitForAnyPress();

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);
	}
}
