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
		/*//The pattern given to us in the project specifications
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
				{1, 0, 0, 0},
				{3, 0, 1, 1},
				{0, 0, 0, 2},
				{0, 1, 0, 0}
		};
*/
		int[][] arr3 = {
				{3, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 0, 0, 0, 1},
				{1, 0, 0, 1, 0, 0, 1, 2},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 0, 0, 1},
				{0, 0, 0, 0, 1, 0, 1, 1},
				{0, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 1}

		};
		int[][] arr4 = {
				{0, 0, 3, 1, 1, 0, 2, 1},
				{0, 1, 0, 0, 1, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 1, 0, 0, 0},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 1},
				{0, 0, 1, 0, 0, 1, 0, 1}

		};
		int[][] arr5 = {
				{1, 0, 0, 0, 1, 0, 2, 0},
				{0, 0, 1, 1, 0, 0, 0, 1},
				{0, 0, 0, 0, 0, 1, 0, 0},
				{0, 0, 0, 1, 0, 0, 1, 1},
				{0, 0, 1, 1, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0},
				{3, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 1, 0, 1}

		};
		
		map = new Map(64);
		//Initialize the map so we can set it to whichever array we pass in
		int option = Button.waitForAnyPress();
		switch(option){
		case Button.ID_LEFT:
			map = new Map(arr3);
			break;
		case Button.ID_ENTER:
			map = new Map(arr4);
			break;
		case Button.ID_RIGHT:
			map = new Map(arr5);
			break;
		default:
			System.exit(0);
		}


		// CONTROLLERS
		SearchAndRescueController searchAndRescue = new SearchAndRescueController(navigation, map, colorSensor, claw);
		LocalizationController localization = new LocalizationController(navigation, map, ultrasonicSensor, null, searchAndRescue);

		// LOGIC
		odometer.start();
		navigation.start();

		// MAIN RUN
		/**/
		odometer.enableDebugOutput(false);
		odometer.enableCorrection(true);
		localization.run();
		odometer.enableCorrection(true);
		searchAndRescue.run();
		/**/

		/*
		// CALIBRATION RUNS
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
		// INFINITE RANDOM WALK
		odometer.enableDebugOutput(true);
		Button.waitForAnyPress();
		odometer.enableCorrection(true);
		java.util.Random random = new java.util.Random();
		int i = 0;
		while (!Button.ESCAPE.isDown()) {
			float next = random.nextInt(4) * Odometer.TILE_SPACING + Odometer.HALF_TILE_SPACING;
			if (random.nextBoolean()) {
				navigation.travelBy(next - odometer.getX(), 0);
			} else {
				navigation.travelBy(0, next - odometer.getY());
			}
			navigation.waitUntilDone();
			i++;
			Display.update("i", Integer.toString(i));
		}
		*/

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);

	}
}
