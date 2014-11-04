
public class Group7Robot {
    private Map map;
    private Odometer odo;
    private Navigation nav = new Navigation(odo);
    private CalibrationController CC = new CalibrationController(nav);
    private LocalizationController LC = new LocalizationController(nav, map);
    private SearchAndRescueController SRC = new SearchAndRescueController(nav, map);

<<<<<<< HEAD
	private Map map;
	private Odometer odo;
	private FilteredUltrasonicSensor frontSensor;
	private Navigation nav = new Navigation(odo);
	private CalibrationController CC = new CalibrationController(nav);
	private LocalizationController LC = new LocalizationController(nav, map, frontSensor);
	private SearchAndRescueController SRC = new SearchAndRescueController(nav, map);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
=======
    public static void main(String[] args) {
        // TODO Auto-generated method stub
>>>>>>> aaf1c3b2cc902526ac7026812173e43063108362

    }
}
