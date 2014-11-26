import lejos.nxt.Button;


public class SearchAndRescueController {
    private Navigation nav;
    private Map map;
    private Node current;
    private FilteredColorSensor color;
    private Claw claw;

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
		float[] xOffset = {0          ,         0  ,         0  ,           0,         0  ,           0, 0           , -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, 0           ,           0,           0,           0};
		float[] yOffset = {-Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3,-Tile.ONE/3 , -Tile.ONE/3, 0           ,           0,           0,           0, 0           ,  Tile.ONE/3,  Tile.ONE/3,  Tile.ONE/3};
		float[] tOffset = {0          ,           0,           0,          0 ,          0 ,          0 , -Pi.ONE_HALF,           0,           0,           0, -Pi.ONE_HALF,           0,           0,           0};
		int iteration = 0;

		Node dest;
		
		boolean finished = false;
		
		while(finished == false){ //change to timer things
			//MOVE TO PICKUP
			dest = map.getCollectionNode();
			Path path = map.getPathFromNodeToNode(current, dest);
			
			nav.enableClawDownMode(false);
			
			moveAlongPath(path);

			Display.update("Status", "Searching");
			//COLOR SENSING --needs so much work


            final int num = dest.getNum();
			float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
			float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
			float theta = (num%4)*Pi.ONE_HALF;
			
			for(int i= 0; i<iteration; i++){
				x += xOffset[i];
				y += yOffset[i];
				theta += tOffset[i];
				if(xOffset[iteration]!=0 || yOffset[iteration]!=0){
					nav.travelTo(x, y, 400);
					nav.waitUntilDone();
				}else if(tOffset[iteration]!=0){
					nav.turnTo(theta, 400);
					nav.waitUntilDone();
				}
			}
				
			Display.update("Status", "Collecting");
	    	
	    	float blockAng = -1;
			
	    	while(blockAng == -1 && finished == false){
		    	claw.sense();
				blockAng = scanForBlock(theta);
				
				if(blockAng != -1){
					iteration++;
					
					nav.turnTo(blockAng);
					nav.waitUntilDone();
					nav.forward(15);
					
					while(nav.isNavigating()){
						if(checkForBlock() == false){
							nav.abort();
						}
					}
					
					nav.forward(4);
					nav.waitUntilDone();
					
					claw.close();
				}else if(iteration < xOffset.length-1){
					claw.open();
					iteration++;
					x += xOffset[iteration];
					y += yOffset[iteration];
					theta += tOffset[iteration];
					if(xOffset[iteration]!=0 || yOffset[iteration]!=0){
						nav.travelTo(x, y, 400);
						nav.waitUntilDone();
					}else if(tOffset[iteration]!=0){
						nav.turnTo(theta, 400);
						nav.waitUntilDone();
					}
				}else{
					claw.open();
					finished = true;
				}
	    	}

	//		


			//CLAW
	//		

	    	
	    	if(!finished){
	    	
				x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
				y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
				theta = (num%4)*Pi.ONE_HALF;
				
				nav.travelTo(x, y);
				nav.waitUntilDone();
				
				//MOVE TO DROP OFF
				Display.update("Status", "Returning");
				dest = map.getDeliveryNode();
				path = map.getPathFromNodeToNode(current, dest);
				
				nav.enableClawDownMode(true);
	
				moveAlongPath(path);
	
				nav.forward(30);
				nav.waitUntilDone();
				
				claw.open();
				
				nav.backward(30);
				nav.waitUntilDone();
				
	    	}


		}
		
		Display.update("Status", "Final");
		//FINISH

    }
    
    private void moveAlongPath(Path path){
    	Display.update("Status", "Moving");
		while(path!=null){
			current = current.getNodeFromPath(new Path(path.getDirection()));
			int num = current.getNum();
			float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
			float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
			float theta = (num%4)*Pi.ONE_HALF;

			if(path.getDirection()==Path.Direction.FRONT){
				nav.travelTo(x, y, 400);
				nav.waitUntilDone();
			}
			path = path.getNextPath();

			if(path==null){
				nav.turnTo(theta, 400);
				nav.waitUntilDone();
			}
		}
    }
    
    private float scanForBlock(float theta){
    	
    	boolean triggered = false;
    	boolean seesBlock = false;
    	
    	float ang1 = -1;
    	float ang2 = -1;
    	
    	float turnAng = theta - Pi.ONE_SIXTH;
    	System.out.println(turnAng); // Alexi, please leave this println in
    								/*	For some reason, every time I take it out, the nav just doesn't move.
    								 * 	I've been debugging this for a while and it has nothing to do with the while loop below, it just doesn't get an angle that it moves to
    								 * 	So turnAng isn't correct unless I print it????? I don't know, just leave it in for now.
    								 */
    	nav.turnTo(turnAng);
    	
    	while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
    		seesBlock = checkForBlock();
    		if(seesBlock==true){
    			if(triggered==false){
    				triggered=true;
    			}
    		}else{
    			if(triggered==true){
    				nav.abort();
    				ang1 = nav.getOdometer().getTheta();
    			}
    		}

    	}
    	if(triggered==true && ang1 == -1){
    		ang1 = nav.getOdometer().getTheta();
    	}
    	
    	
    	turnAng = theta + Pi.ONE_SIXTH;
    	nav.turnTo(turnAng);
    	
    	triggered = false;
    	
    	while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
    		seesBlock = checkForBlock();
    		if(seesBlock==true){
    			if(triggered==false){
    				triggered=true;
    			}
    		}else{
    			if(triggered==true){
    				nav.abort();
    				ang2 = nav.getOdometer().getTheta();
    			}
    		}
    	}
    	
    	if(triggered==true && ang2 == -1){
    		ang2 = nav.getOdometer().getTheta();
    	}
    	
    	
    	if(ang1==-1){
        	nav.turnTo(theta);
        	
    		triggered = false;
    		
    		while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
        		seesBlock = checkForBlock();
        		if(seesBlock==true){
        			if(triggered==false){
        				triggered=true;
        			}
        		}else{
        			if(triggered==true){
        				nav.abort();
        				ang1 = nav.getOdometer().getTheta();
        			}
        		}
        	}
    		
    		if(triggered==true && ang1 == -1){
        		ang1 = nav.getOdometer().getTheta();
        	}
    	}
    	
    	if(ang1 != -1 && ang2 != -1){
    		if(ang1 > Pi.THREE_HALF && ang2 < Pi.ONE_HALF){
    			ang1 -= Pi.TWO;
    		}
    		float result = (ang1+ang2)/2;
    		return result;
    	}
    	return -1;
    }

    private boolean checkForBlock() {
        int c = color.getColorData();
        int b = c & 255;
        int g = (c >> 8) & 255;
        int r = (c >> 16) & 255;
        return r>=50 || b>=50 || g>=50;
    }

    public void setCurrent(Node c){
    	current = c;
    }
}
