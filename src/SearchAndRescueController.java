import lejos.nxt.Button;


public class SearchAndRescueController {
    private Navigation nav;
    private Map map;
    private Node current;
    private FilteredColorSensor color;
    private FilteredUltrasonicSensor distanceSensor;
    private Claw claw;
    private static final int SPEED = 400;

    public SearchAndRescueController(Navigation n, Map m, FilteredColorSensor cs, FilteredUltrasonicSensor us, Claw c) {
        nav = n;
        map = m;
        color = cs;
        distanceSensor = us;
        claw = c;
    }

    public void run() {
    //	Display.clear();
	//	Display.reserve("Status", "Blocks", "Points");
	//	Display.update("Status", "Init");
	//	int blocks = 0;
	//	int points = 0;
    	
    	//Three arrays of offsets used to keep track of the path for the robot to follow while searching for blocks
    	//It will step through the arrays and move the offset amount of distance, then search again  
		float[] xOffset = {0          ,         0  ,         0  ,           0,         0  ,           0, 0           , -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3 ,           0,           0,           0};
		float[] yOffset = {-Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3,-Tile.ONE/3 , -Tile.ONE/3, -Tile.ONE/3 ,           0,           0,           0, 0           ,  Tile.ONE/3,  Tile.ONE/3,  Tile.ONE/3};
		float[] tOffset = {0          ,           0,           0,          0 ,          0 ,          0 , -Pi.ONE_HALF,           0,           0,           0, -Pi.ONE_HALF,           0,           0,           0};
		//An int to keep track of which iteration of the offset array the robot is in
		int iteration = 0;

		//Node to keep track of where the robot wants to go
		Node dest;
		
		//Keep track of the state of the robots collection
		boolean finished = false;
		
		while(finished == false){ //change to timer things
			//MOVE TO PICKUP
			dest = map.getCollectionNode();
			Path path = map.getPathFromNodeToNode(current, dest);
			
			nav.enableClawDownMode(false);
			
			moveAlongPath(path);

			Display.update("Status", "Searching");
			//COLOR SENSING --needs so much work
			
			nav.getOdometer().enableCorrection(false);

            int num = dest.getNum();
			float x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
			float y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
			float theta = (num%4)*Pi.ONE_HALF;
			
			for(int i= 0; i<iteration; i++){
				x += xOffset[i];
				y += yOffset[i];
				theta += tOffset[i];
				if(xOffset[iteration]!=0 || yOffset[iteration]!=0){
					nav.travelTo(x, y, SPEED);
					nav.waitUntilDone();
				}
				nav.turnTo(theta, SPEED);
				nav.waitUntilDone();
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
					
					boolean triggered = false;
					while(nav.isNavigating()){
						if(checkForBlock() == true && triggered == false){
							triggered = true;
						}
						if(checkForBlock() == false && triggered == true){
							nav.abort();
						}
					}
					
					if(triggered == true){
						nav.forward(5);
						nav.waitUntilDone();
						claw.close();
				
					}else{
						nav.backward(15, SPEED);
						nav.waitUntilDone();
						nav.turnTo(theta, SPEED);
						nav.waitUntilDone();
						blockAng = -1;
					}

				}else if(iteration < xOffset.length-1){
					claw.open();
					iteration++;
					x += xOffset[iteration];
					y += yOffset[iteration];
					theta += tOffset[iteration];
					if(xOffset[iteration]!=0 || yOffset[iteration]!=0){
						nav.travelTo(x, y, SPEED);
						nav.waitUntilDone();
					}
					nav.turnTo(theta, SPEED);
					nav.waitUntilDone();
				}else{
					claw.open();
					finished = true;
				}
	    	}

	//		


			//CLAW
	//		

	    	
	    	if(!finished){
	    	
				nav.enableClawDownMode(true);
	    		
	    		for(int i= iteration; i>=0; i--){
					x -= xOffset[i];
					y -= yOffset[i];
					theta -= tOffset[i];
					if(tOffset[i]!=0){
						nav.travelTo(x, y, SPEED);
						nav.waitUntilDone();
					}
				}
				
				nav.getOdometer().enableCorrection(true);
				
				//MOVE TO DROP OFF
				Display.update("Status", "Returning");
				dest = map.getDeliveryNode();
				path = map.getPathFromNodeToNode(current, dest);
				
	
				moveAlongPath(path);
	
				nav.forward(30);
				nav.waitUntilDone();
				
				claw.open();
				
				num = dest.getNum();
				x = Tile.HALF + Tile.ONE*(int)((num/4)%(map.getLength()));
				y = (map.getLength()-1)*Tile.ONE+Tile.HALF - Tile.ONE*(int)((num/4)/(map.getLength()));
				theta = (num%4)*Pi.ONE_HALF;
				
				nav.travelTo(x, y, SPEED);
				nav.waitUntilDone();
				nav.turnTo(theta);
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
				nav.travelTo(x, y, SPEED);
				nav.waitUntilDone();
			}
			path = path.getNextPath();

			if(path==null){
				nav.turnTo(theta, SPEED);
				nav.waitUntilDone();
			}
		}
    }
    
    private float scanForBlock(float theta){
    	
    	boolean triggered = false;
    	boolean seesBlock = false;
    	
    	float ang1 = -1;
    	float ang2 = -1;
    	
    	float turnAng = theta - Pi.ONE_FIFTH;
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
    	
    	
    	turnAng = theta + Pi.ONE_FIFTH;
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
