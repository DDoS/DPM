
public class SearchAndRescueController {

    private Map map;
    private MapNode current;


    public SearchAndRescueController( Map m) {

        map = m;

    }

    public int run() {

		int blocks = 0;
		int points = 0;

		MapNode dest;
		int count = 0;

		while(blocks<1){ //change to timer things
			//MOVE TO PICKUP
			blocks++;
			dest = map.getCollectionNode();
			MapPath path = map.getPathFromNodeToNode(current, dest);
			while(path!=null){
				current = current.getNodeFromPath(new MapPath(path.getDirection()));
				if(path.getDirection()==MapPath.Direction.FRONT){
					count++;
				}
				path = path.getNextMapPath();

				int num = current.getNum();
				float x = 15 + 30*(int)((num/4)%(map.getLength()));
				float y = 105 - 30*(int)((num/4)/(map.getLength()));
				float theta = (float) ((num%4)*Math.PI/2);
				//System.out.println("PATH X: "+ x + " Y: "+ y+" T: "+ theta);

			}
			System.out.println("SAR took "+ count + " moves");

			//System.out.println("claw shit");
			//COLOR SENSING --needs so much work
/*
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

			nav.forward(15);
			nav.waitUntilDone();

			//CLAW
			claw.close();

//*/
//			//MOVE TO DROP OFF
//			dest = map.getDeliveryNode();
//			path = map.getPathFromNodeToNode(current, dest);
//			while(path!=null){
//				current = current.getNodeFromPath(new MapPath(path.getDirection()));
//				
//				if(path.getDirection()==MapPath.Direction.FRONT){
//					count++;
//				}
//				path = path.getNextMapPath();
//
//				int num = current.getNum();
//				float x = 15 + 30*(int)((num/4)%(map.getLength()));
//				float y = 105 - 30*(int)((num/4)/(map.getLength()));
//				float theta = (float) ((num%4)*Math.PI/2);
//				//System.out.println("PATH X: "+ x + " Y: "+ y+" T: "+ theta);
//			}
//
//			//CLAW AGAIN
//			//claw.open();

		}
		return count;

    }

    public void setCurrent(MapNode c){
    	current = c;
    }
}
