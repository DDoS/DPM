import lejos.nxt.Button;


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

		//while(true){ //change to timer things
			//MOVE TO PICKUP
			dest = map.getCollectionNode();
			MapPath path = map.getPathFromNodeToNode(current, dest);

			Display.update("Status", "Moving");
			while(path!=null){
				current = current.getNodeFromPath(new MapPath(path.getDirection()));
				int num = current.getNum();
				float x = Odometer.HALF_TILE_SPACING + Odometer.TILE_SPACING*(int)((num/4)%(map.getLength()));
				float y = (map.getLength()-1)*Odometer.TILE_SPACING+Odometer.HALF_TILE_SPACING - Odometer.TILE_SPACING*(int)((num/4)/(map.getLength()));
				float theta = (float) ((num%4)*Math.PI/2);
				
				if(path.getDirection()!=MapPath.Direction.FRONT){
					nav.travelTo(x, y);
					nav.waitUntilDone();
				}
				path = path.getNextMapPath();

				if(path==null){
					nav.turnTo(theta);
					nav.waitUntilDone();
				}
			}

			Display.update("Status", "Searching");
			//COLOR SENSING --needs so much work

            claw.sense();
			int c = color.getColorData();
			int b = c & 255;
			int g = (c >> 8) & 255;
			int r = (c >> 16) & 255;
			while(r<100 && g<100 && b<100){
				nav.forward(1);
				nav.waitUntilDone();
                c = color.getColorData();
				b = c & 255;
				g = (c >> 8) & 255;
				r = (c >> 16) & 255;
			}
			Display.update("Status", "Collecting");

			nav.forward(15);
			nav.waitUntilDone();

			//CLAW
			claw.close();
			
			Display.update("Status", "Final");
			
			/*
			//MOVE TO DROP OFF
			dest = map.getDeliveryNode();
			path = map.getPathFromNodeToNode(current, dest);

			//Display.update("Status", "Returning");
			while(path!=null){
				current = current.getNodeFromPath(new MapPath(path.getDirection()));
				path = path.getNextMapPath();

				int num = current.getNum();
				float x = Odometer.HALF_TILE_SPACING + Odometer.TILE_SPACING*(int)((num/4)%(map.getLength()));
				float y = (map.getLength()-1)*Odometer.TILE_SPACING+Odometer.HALF_TILE_SPACING - Odometer.TILE_SPACING*(int)((num/4)/(map.getLength()));
				float theta = (float) ((num%4)*Math.PI/2);
				nav.travelTo(x, y);
				nav.waitUntilDone();
				if(path==null){
					nav.turnTo(theta);
					nav.waitUntilDone();
				}
			}

			//CLAW AGAIN
			nav.forward(13);
			nav.waitUntilDone();
			
			claw.open();

			//Display.update("Status", "Final");
			 
		}*/

    }

    public void setCurrent(MapNode c){
    	current = c;
    }
}
