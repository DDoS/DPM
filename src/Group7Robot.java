
public class Group7Robot {

	private Map map;
	private Odometer odo;
	private Navigation nav = new Navigation(odo);
	private CalibrationController CC = new CalibrationController(nav);
	private LocalizationController LC = new LocalizationController(nav, map);
	private SearchAndRescueController SRC = new SearchAndRescueController(nav, map);
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
