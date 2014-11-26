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
		//Written out as displayed in specifications (the Map class handles rotating it)
		//0 - no block
		//1 - block
		//2 - no block, this is the pickup zone
		//3 - no block, this is the dropoff zone
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
		*/
		//Test for small scale
		int[][] arr4 = {
				{0, 0, 3, 1},
				{1, 0, 0, 0},
				{0, 2, 1, 0},
				{0, 0, 1, 0}
		};

		int[][] arr3 = {
				{0, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 0, 0, 0, 1},
				{1, 0, 0, 1, 0, 0, 1, 0},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 0, 0, 1},
				{0, 0, 0, 0, 1, 0, 1, 1},
				{3, 2, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 1}

		};
	/*	int[][] arr4 = {
				{0, 0, 0, 1, 1, 0, 0, 1},
				{0, 1, 0, 0, 1, 0, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 1, 0, 0, 0},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{3, 2, 0, 1, 0, 0, 0, 1},
				{0, 0, 1, 0, 0, 1, 0, 1}

		};*/
		int[][] arr5 = {
				{1, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 1, 1, 0, 0, 0, 1},
				{0, 0, 0, 0, 0, 1, 0, 0},
				{0, 0, 0, 1, 0, 0, 1, 1},
				{0, 0, 1, 1, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0},
				{3, 2, 0, 0, 0, 0, 0, 0},
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
		
		/**/
		// MAIN RUN
		odometer.enableDebugOutput(false);
		odometer.enableCorrection(true);
		localization.run();
		searchAndRescue.run();
		/**/

		/*
		// ULTRASONIC RUN
		while (!Button.ESCAPE.isDown()) {
			Display.update("u", Integer.toString(ultrasonicSensor.getDistanceData()));
		}
		/*

		/*
		// CALIBRATION RUNS
		odometer.enableCorrection(false);
		Button.waitForAnyPress();
		navigation.travelTo(0, Tile.ONE * 2);
		navigation.waitUntilDone();
		Button.waitForAnyPress();
		for (int i = 0; i < 8; i++) {
			navigation.turnBy(Pi.ONE_HALF);
			navigation.waitUntilDone();
		}
		*/

		/*
		// INFINITE RANDOM WALK
		odometer.enableDebugOutput(true);
		Button.waitForAnyPress();
		odometer.enableCorrection(true);
		Random random = new Random();
		int i = 0;
		while (!Button.ESCAPE.isDown()) {
			float next = random.nextBits(2) * Tile.ONE + Tile.HALF;
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
/*
		// INFINITE RANDOM WALK IN MAP WITH BLOCK
		Button.waitForAnyPress();
		odometer.enableCorrection(true);
		odometer.enableDebugOutput(true);
		navigation.enableClawDownMode(true);
		Random random = new Random();
		int[][] array = arr5;
		int size = array.length, sx = 0, sy = 0;
		while (!Button.ESCAPE.isDown()) {
			int nx = random.nextInt(size);
			int ny = random.nextInt(size);
			final int[] path = Map.findPath(array, sx, sy, nx, ny);
			int i = 0;
			while (path[i] != Integer.MAX_VALUE) {
				sx = path[i];
				sy = path[i + 1];
				i += 2;
				navigation.travelTo(sx * Tile.ONE + Tile.HALF, sy * Tile.ONE + Tile.HALF);
				navigation.waitUntilDone();
			}
		}

		*/

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);
	}
}
