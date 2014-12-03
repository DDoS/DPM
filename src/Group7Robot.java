import lejos.nxt.*;

/**
 * The entry point of the robot software. Only contains the main method.
 */
public class Group7Robot {
    /**
     * Main method of the robot program runs the map selection, followed by the localization (repeating until it succeeds), and finally the search an rescue.
     *
     * @param args Ignored
     */
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
        int[][][] larges = {
                {//MAP 1
                        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0},
                        {0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 1, 0},
                        {0, 2, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1},
                        {0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0}
                },

                {//MAP 2
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0},
                        {0, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1},
                        {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 2, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0}
                },

                {//MAP 3
                        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
                        {1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 2, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0}
                },

                {//MAP 4
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1},
                        {0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {1, 1, 0, 0, 0, 0, 1, 0, 1, 1, 0, 0},
                        {1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
                        {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0}
                },

                {//MAP 5
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1},
                        {1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0},
                        {1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1},
                        {0, 2, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },

                {//MAP 6
                        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
                        {1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0},
                        {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0},
                        {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0},
                        {0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0},
                        {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0},
                        {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 0},
                        {0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0}
                }
        };

        //MENU
        //Splash screen with version check for safety
        Display.update("Version", "119.1");
        //PLEASE UPDATE VERSION: first number changes with every commit. second number changes with every minor edit.
        Button.waitForAnyPress();
        Display.clear();

        int choice, y, x, m;
        do {
            //MAP CHOICE
            int num = 1;

            do {
                Display.update("M", Integer.toString(num));
                choice = Button.waitForAnyPress();
                if (choice == Button.ID_LEFT && num > 1) {
                    num--;
                } else if (choice == Button.ID_RIGHT && num < 6) {
                    num++;
                }
            } while (choice != Button.ID_ENTER);

            m = num - 1;
            Display.update("M", Integer.toString(num));

            //X POS DROPOFF
            num = -1;
            do {
                Display.update("X", Integer.toString(num));
                choice = Button.waitForAnyPress();
                if (choice == Button.ID_LEFT && num > -1) {
                    num--;
                } else if (choice == Button.ID_RIGHT && num < 11) {
                    num++;
                }
            } while (choice != Button.ID_ENTER);

            x = num;
            Display.update("X", Integer.toString(num));

            //Y POS DROPOFF
            num = -1;
            do {
                Display.update("Y", Integer.toString(num));
                choice = Button.waitForAnyPress();
                if (choice == Button.ID_LEFT && num > -1) {
                    num--;
                } else if (choice == Button.ID_RIGHT && num < 11) {
                    num++;
                }
            } while (choice != Button.ID_ENTER);

            y = num;
            Display.update("Y", Integer.toString(num));

            choice = Button.waitForAnyPress();

            //Final confirm
        } while (choice != Button.ID_ENTER);

        //specs use -1, -1 as lowest corner, so offset to the 0, 0  we use
        x += 1;
        y += 1;

        //set up the dropoff location in the map

        larges[m][11 - y][x] = 3;

        //MAP
        Map map = new Map(larges[m]);

        //TIMER
        Time.startTime(7 * 60 + 30);

        // CONTROLLERS
        SearchAndRescueController searchAndRescue = new SearchAndRescueController(navigation, map, colorSensor, claw);
        LocalizationController localization = new LocalizationController(navigation, map, ultrasonicSensor, searchAndRescue);

        // LOGIC
        odometer.start();
        navigation.start();

        // MAIN RUN
        odometer.enableDebugOutput(false);
        odometer.enableCorrection(true);

        boolean localized = localization.run();
        while (!localized) {
            for (int i = 0; i < 12 * 12 * 4; i++) {
                map.getNodeAtIndex(i).setIsValidStart(true);
            }
            localized = localization.run();
        }
        searchAndRescue.run();

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
