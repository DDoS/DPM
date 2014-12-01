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
		
		//TIMER
		Time.startTime(7*60 + 30);

		
			
		// MAP
		//Written out as displayed in specifications (the Map class handles rotating it)
		//0 - no block
		//1 - block
		//2 - no block, this is the pickup zone
		//3 - no block, this is the dropoff zone
		// A tiny test pattern
		int[][] tiny1 = {
				{0, 0, 1},
				{1, 0, 1},
				{0, 0, 0}
		};
		// Test for small scale
		int[][] small1 = {
				{0, 0, 3, 1},
				{1, 0, 0, 0},
				{0, 2, 1, 0},
				{0, 0, 1, 0}
		};
		// Regular demo patterns
		int[][] normal1 = {
				{0, 1, 1, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 3, 0, 0, 1},
				{1, 0, 0, 1, 0, 0, 1, 0},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 0, 0, 1},
				{0, 0, 0, 0, 1, 0, 1, 1},
				{0, 2, 0, 0, 1, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 1}

		};
		int[][] normal2 = {
				{0, 0, 0, 1, 1, 0, 0, 1},
				{0, 1, 0, 0, 1, 3, 0, 1},
				{1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 1, 0, 1, 0, 0, 0},
				{0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0},
				{0, 2, 0, 1, 0, 0, 0, 1},
				{0, 0, 1, 0, 0, 1, 0, 1}

		};
		int[][] normal3 = {
				{1, 0, 0, 0, 1, 0, 0, 0},
				{0, 0, 1, 1, 0, 0, 0, 1},
				{0, 0, 0, 0, 3, 1, 0, 0},
				{0, 0, 0, 1, 0, 0, 1, 1},
				{0, 0, 1, 1, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0},
				{0, 2, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 1, 1, 0, 1}

		};
		// The pattern given to us in the project specifications
		int[][] large1 = {
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

		// Initialize the map so we can set it to whichever array we pass in
		int option = Button.waitForAnyPress();
		int[][] array;
		switch(option){
			case Button.ID_LEFT:
				array = normal1;
				break;
			case Button.ID_ENTER:
				array = normal2;
				break;
			case Button.ID_RIGHT:
				array = normal3;
				break;
			default:
				array = null;
				System.exit(0);
		}
		Map map = new Map(array);

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
		// ULTRASONIC TEST
		while (!Button.ESCAPE.isDown()) {
			Display.update("u", Integer.toString(ultrasonicSensor.getDistanceData()));
		}
		*/

		/*
		// LIGHT SENSOR TEST
		while (!Button.ESCAPE.isDown()) {
			Display.update("l", Integer.toString(leftLightSensor.getLightData()));
			Display.update("r", Integer.toString(rightLightSensor.getLightData()));
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
			}
		}
		*/

		/*
		// CALIBRATION RUNS
		odometer.enableCorrection(false);
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
		odometer.enableCorrection(true);
		Random random = new Random();
		int i = 0;
		while (!Button.ESCAPE.isDown()) {
			float next = Tile.toOdo(random.nextBits(2));
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
		odometer.enableCorrection(true);
		odometer.enableDebugOutput(true);
		navigation.enableClawDownMode(true);
		Random random = new Random();
		int size = array.length, sx = 7, sy = 7;
		odometer.setPosition(Tile.toOdo(sx), Tile.toOdo(sy), Pi.ONE);
		while (!Button.ESCAPE.isDown()) {
			int nx = random.nextInt(size);
			int ny = random.nextInt(size);
			int[] path = Map.findPath(array, sx, sy, nx, ny);
			int i = 0;
			while (path[i] != Integer.MAX_VALUE) {
				sx = path[i];
				sy = path[i + 1];
				i += 2;
				navigation.travelTo(Tile.toOdo(sx), Tile.toOdo(sy));
				navigation.waitUntilDone();
			}
		}
		*/

		// EXIT
		Button.waitForAnyPress();
		System.exit(0);
	}
}
