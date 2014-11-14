
public class SearchAndRescueController {
    private Navigation nav;
    private Map map;
    private MapNode current;
    private FilteredColorSensor color;
    private Claw claw;

    public SearchAndRescueController(Navigation n, Map m, FilteredColorSensor cs, Claw c) {
        nav = n;
        map = m;
        color = cs;
        claw = c;
    }

    public void run() {
    	Display.clear();
		Display.reserve("Status", "Blocks", "Points");
		Display.update("Status", "Init");
		int blocks = 0;
		int points = 0;
		
		MapNode dest;
		
		while(true){ //change to timer things
			//MOVE TO PICKUP
			dest = map.getCollectionNode();
			MapPath path = map.getPathFromNodeToNode(current, dest);
			MapNode next;
			
			while(path!=null){
				next = current.getNodeFromPath(new MapPath(path.getDirection()));
				path = path.getNextMapPath();
				
				int num = next.getNum();
				float x = 15 + 30*(int)((num/4)%(map.getLength()));
				float y = 15 + 30*(int)((num/4)/(map.getLength()));
				float theta = (num%4)*90;
				nav.travelTo(x, y);
				while(nav.isNavigating()){}
				if(path==null){
					nav.turnTo(theta);
					while(nav.isNavigating()){}
				}
			}
			
			//COLOR SENSING
			while(color.getColorData()==0){
				nav.forward(1);
				while(nav.isNavigating()){}
			}
			nav.forward(10);
			while(nav.isNavigating()){}
			
			//CLAW
			claw.close();
			
			
			//MOVE TO DROP OFF
			dest = map.getDeliveryNode();
			path = map.getPathFromNodeToNode(current, dest);
			
			while(path!=null){
				next = current.getNodeFromPath(new MapPath(path.getDirection()));
				path = path.getNextMapPath();
				
				int num = next.getNum();
				float x = 15 + 30*(int)((num/4)%(map.getLength()));
				float y = 15 + 30*(int)((num/4)/(map.getLength()));
				float theta = (num%4)*90;
				nav.travelTo(x, y);
				while(nav.isNavigating()){}
				if(path==null){
					nav.turnTo(theta);
					while(nav.isNavigating()){}
				}
			}
			
			//CLAW AGAIN
			claw.open();
		}
		
    }
    
    public void setCurrent(MapNode c){
    	current = c;
    }
}
