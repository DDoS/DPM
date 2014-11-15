
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
			
			Display.update("Status", "Moving");
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
			
			Display.update("Status", "Searching");
			//COLOR SENSING --needs so much work
			int c = color.getColorData();
			int b = c & 255;
			int g = (c >> 8) & 255;
			int r = (c >> 16) & 255;
			while(r<100 && g<100 && b<100){
				nav.forward(1);
				while(nav.isNavigating()){}
				c = color.getColorData();
				b = c & 255;
				g = (c >> 8) & 255;
				r = (c >> 16) & 255;
			}
			Display.update("Status", "Collecting");
			nav.forward(10);
			while(nav.isNavigating()){}
			
			//CLAW
			claw.close();
			
			
			//MOVE TO DROP OFF
			dest = map.getDeliveryNode();
			path = map.getPathFromNodeToNode(current, dest);
			
			Display.update("Status", "Returning");
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
			
			Display.update("Status", "Final");
		}
		
    }
    
    public void setCurrent(MapNode c){
    	current = c;
    }
}
