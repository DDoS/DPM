import lejos.nxt.Button;


public class SearchAndRescueController {
    private Navigation nav;
    private Map map;
    private MapNode current;
    private FilteredColorSensor color;
    private Claw claw;
    private boolean seenBlock = false;

    public SearchAndRescueController(Navigation n, Map m, FilteredColorSensor cs, Claw c) {
        nav = n;
        map = m;
        color = cs;
        claw = c;
    }

    public void run() {
    //	Display.clear();
	//	Display.reserve("Status", "Blocks", "Points");
	//	Display.update("Status", "Init");
		int blocks = 0;
		int points = 0;

		MapNode dest;

		//while(true){ //change to timer things
			//MOVE TO PICKUP
			dest = map.getCollectionNode();
/*			MapPath path = map.getPathFromNodeToNode(current, dest);

	//		Display.update("Status", "Moving");
			while(path!=null){
				current = current.getNodeFromPath(new MapPath(path.getDirection()));
				int num = current.getNum();
				float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
				float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
				float theta = (num%4)*Pi.ONE_HALF;

				if(path.getDirection()==MapPath.Direction.FRONT){
					nav.travelTo(x, y);
					nav.waitUntilDone();
				}
				path = path.getNextMapPath();

				if(path==null){
					nav.turnTo(theta);
					nav.waitUntilDone();
				}
			}

	//		Display.update("Status", "Searching");
			//COLOR SENSING --needs so much work
*/
            claw.sense();

            final int num = dest.getNum();
			float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
			float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
			float theta = (num%4)*Pi.ONE_HALF;


			nav.travelTo(x, y-5);

			Thread blockCheck = new Thread(){
				public void run(){
					int c, r, b, g;
					while(true){
		                c = color.getColorData();
						b = c & 255;
						g = (c >> 8) & 255;
						r = (c >> 16) & 255;
						if(r<100 && b<100 && g<100){
							seenBlock = false;
						}else{
							seenBlock = true;
						}
					}
				}
			};

			float ang1 = -1;
			float ang2 = -1;

			blockCheck.start();

			nav.turnTo(theta - 30);

			while(nav.isNavigating() && seenBlock == false){}
			nav.abort();

			if(seenBlock){
				ang1 = nav.getOdometer().getTheta();
			}

			nav.turnTo(theta + 30);

			while(nav.isNavigating() && seenBlock == false){}
			nav.abort();

			if(seenBlock){
				ang2 = nav.getOdometer().getTheta();
			}

			nav.turnTo(theta);

			if(ang1==-1){
				while(nav.isNavigating() && seenBlock == false){}
				nav.abort();

				if(seenBlock){
					ang1 = nav.getOdometer().getTheta();
				}
			}else{
				nav.waitUntilDone();
			}

			if(ang1!=-1 && ang2 != -1){
				float blockAng = (ang1+ang2)/2;
				nav.turnTo(blockAng);
				nav.waitUntilDone();

				nav.forward(10);

				while(nav.isNavigating() && seenBlock == false){}
				nav.abort();

				nav.forward(1);
				nav.waitUntilDone();

				claw.close();
			}


	//		Display.update("Status", "Collecting");


			//CLAW
	//		Display.update("Status", "Final");

			/*
			//MOVE TO DROP OFF
			dest = map.getDeliveryNode();
			path = map.getPathFromNodeToNode(current, dest);

			//Display.update("Status", "Returning");
			while(path!=null){
				current = current.getNodeFromPath(new MapPath(path.getDirection()));
				path = path.getNextMapPath();

				int num = current.getNum();
				float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
				float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
				float theta = (num%4)*Pi.ONE_HALF;
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
