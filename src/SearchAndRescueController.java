import lejos.nxt.Button;


public class SearchAndRescueController {
    private Navigation nav;
    private Map map;
    private Node current;
    private FilteredColorSensor color;
    private Claw claw;
    private static final int SPEED_FAST = 500;
    private static final int SPEED_SLOW = 400;
    private static final int BLOCK_DIST = 10;

    public SearchAndRescueController(Navigation n, Map m, FilteredColorSensor cs, Claw c) {
        nav = n;
        map = m;
        color = cs;
        claw = c;
    }

    /**
     * Method called to start the SAR controller
     * Handles all of the logic of the controller
     */
    public void run() {

    	//Three arrays of offsets used to keep track of the path for the robot to follow while searching for blocks
    	//It will step through the arrays and move the offset amount of distance, then search again
		float[] xOffset = {-Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3    ,     -Tile.ONE/3,           0,           0,               0,                0,  Tile.ONE/3,  Tile.ONE/3,  Tile.ONE/3 - 5};
		float[] yOffset = {          0,           0,           0,           0,           0,               0,               0, -Tile.ONE/3, -Tile.ONE/3, -Tile.ONE/3    ,      -Tile.ONE/3,           0,           0,               0};
		float[] tOffset = {          0,           0,           0,          0 ,          0 ,              0 ,     Pi.ONE_HALF,           0,           0,               0,      Pi.ONE_HALF,           0,           0,               0};
		//An int to keep track of which iteration of the offset array the robot is in
		int iteration = 0;
		
		int blocks = 0;

		//Node to keep track of where the robot wants to go
		Node dest;

		//Keep track of the state of the robots collection
		boolean statusFinal = false;

		while(statusFinal == false && Time.timeLeft()>0){ //Only loop if we have 60 seconds left to try to find a block

			//Find the path between the pickup location and the current location
			dest = map.getCollectionNode();
			Path path = map.getPathFromNodeToNode(current, dest);

			//Since we enable this later in the loop, make sure to re-disable it before we begin
			nav.enableClawDownMode(false);

			//Move to the pickup location
			moveAlongPath(path, false);

			Display.update("Status", "SettingUp");

			//Disable correction because our path is very odd but also precise. correction messes it up.
			nav.getOdometer().enableCorrection(false);

			//Get the x, y, and theta of the pickup location
            float x = dest.getX();
            float y = dest.getY();
            float theta = dest.getTheta();

            //Since we might have already visited some of the search locations, we need to iterate to the current position in the list and add it to our position.
            //Only travel to the locations which contain turns (to minimize time waiting between travels)
			for(int i= 0; i<iteration; i++){
				x += xOffset[i];
				y += yOffset[i];
				theta += tOffset[i];
				if(tOffset[i]!=0){
					nav.travelTo(x, y, SPEED_FAST);
					nav.waitUntilDone();
				}
			}
			//Travel to the last location (in case we aren't here already)
			nav.travelTo(x, y, SPEED_FAST);
			nav.waitUntilDone();
			nav.turnTo(theta, SPEED_FAST);
			nav.waitUntilDone();


			//Two variables to keep track of where- if found- the next block is
			boolean foundBlock = false;
	    	float blockAng;

	    	//Keep looking until we either find a block (so we need to go return it), or we reach the end of the algorithm
	    	while(foundBlock == false && statusFinal == false){
				Display.update("Status", "Searching");
	    		//Put the claw into sensing mode
		    	claw.sense();
		    	//Scan for a block
				blockAng = scanForBlock(theta);
				//If we found a block with the scan, set foundBlock accordingly
				if(blockAng != -1){
					foundBlock = true;
				}

				//If we do, we have to do more work
				if(foundBlock == true){

					Display.update("Status", "Collecting");

					//Add to iteration because we now have checked this location successfully
					iteration++;

					//Turn to the block angle, which is hopefully dead-on
					nav.turnTo(blockAng);
					nav.waitUntilDone();

					//move forward, but don't call waitUntilDone, because we want to keep doing stuff while moving
					nav.forward((int)(BLOCK_DIST*1.2));

					boolean blockInReach = waitForEdge();

					//Now, triggered will tell us if there's a block there or not, and we will also be placed right at the far edge of the block

					if(blockInReach == true){//If there is a block

						//Move forward and close the claw a couple times to secure the block in place
						nav.forward(4);
						nav.waitUntilDone();
						
						//Close the claw and get whether or not it worked
						boolean closed = grabBlock();
						
						//while it doesn't work, follow a routine
						while(!closed){
							
							//flip the pushing direction based on where the robot is
							int direction = 1;
							
							if(iteration<6){
								direction = -1;
							}
							
							//lift the claw
							claw.sense();
							
							//move back a bit
							nav.backward((int)(3.0*BLOCK_DIST/4));
							nav.waitUntilDone();
							
							//turn
							nav.turnBy(-Pi.ONE_QUARTER);
							nav.waitUntilDone();
							
							//lower the claw
							claw.close();
							
							
							//turn so you swing by the block and push it to a better angle
							nav.turnBy(Pi.ONE_HALF);
							nav.waitUntilDone();
							
							//lift the claw
							claw.sense();
							
							//turn back a bit
							nav.turnBy(-Pi.ONE_SIXTH);
							nav.waitUntilDone();
							
							//keep scanning for the block and looking forward until you find it
							float angle;
							do{
								nav.forward(4);
								nav.waitUntilDone();
								
								angle = scanForBlock(nav.getOdometer().getTheta());
							
							}while(angle == -1);
							
							//turn to where the block is
							nav.turnTo(angle);
							nav.waitUntilDone();
							
							//move forward, but don't call waitUntilDone, because we want to keep doing stuff while moving
							nav.forward((int)(BLOCK_DIST*1.2));
			
							//stop moving when you see the edge
							waitForEdge();
							
							//move forward and attempt to grab again
							nav.forward(4);
							nav.waitUntilDone();
							
							closed = grabBlock();
							
						}
						
						//go back to where we just were as part of the search algorithm
						nav.travelTo(x, y, SPEED_FAST);
						nav.waitUntilDone();
						//Keep the claw closed so we can begin to carry it back

					}else{//If there isn't a block
						//Backup, turn back to the right angle, and restart the loop by setting foundBlock to false again
						nav.backward((int)(BLOCK_DIST*1.5), SPEED_FAST);
						nav.waitUntilDone();
						nav.turnTo(theta, SPEED_FAST);
						nav.waitUntilDone();
						foundBlock = false;
						//Now we're ready to restart the loop and scan again
					}

				}else if(iteration < xOffset.length-1){ //If we don't find a block with our scan and we have more places to check

					//Open the claw (to allow easier movement)
					claw.open();

					//Add one to iteration, so we check the next location
					iteration++;

					//Add to our position based on the offset tables
					x += xOffset[iteration];
					y += yOffset[iteration];
					theta += tOffset[iteration];

					//If we have an x/y change in position, move there
					if(xOffset[iteration]!=0 || yOffset[iteration]!=0){
						nav.travelTo(x, y, SPEED_FAST);
						nav.waitUntilDone();
					}

					//Turn to face the right angle
					nav.turnTo(theta, SPEED_FAST);
					nav.waitUntilDone();
					//Now we're ready to restart the loop and scan again

				}else{ //If we don't find a block with our scan but we've checked everywhere, then we can be done!

					claw.open();
					statusFinal = true;
					//End the SAR algorithm

				}
	    	}



	    	if(!statusFinal){ //If we're not done, since we finished searching, that means it's time to rescue!

	    		//We have a block, so enable the nav's adjusted movements
				nav.enableClawDownMode(true);

				Display.update("Status", "Retrieving");

				//Backup along our path by looping through the offset table in reverse
	    		for(int i= iteration; i>0; i--){
					x -= xOffset[i];
					y -= yOffset[i];
					theta -= tOffset[i];
					if(tOffset[i-1]!=0){
						//Only travel to the major turns in the path. This helps avoid collisions, as space is tight
						nav.travelTo(x, y, SPEED_SLOW);
						nav.waitUntilDone();
					}
				}

	    		//Get the collection node (where we started)
	            x = dest.getX();
	            y = dest.getY();
	            theta = dest.getTheta();
				//And travel to it
				nav.travelTo(x, y, SPEED_SLOW);
				nav.waitUntilDone();

				//Enable corrections again while we travel to the dropoff
				nav.getOdometer().enableCorrection(true);

				//Now the destination is set to the delivery node, and a new path is calculated
				dest = map.getDeliveryNode();
				path = map.getPathFromNodeToNode(current, dest);

				//Move along the new path
				moveAlongPath(path, true); 
				
				blocks++;
				
				if(blocks==9){
					statusFinal = true;
				}
				
				//Drop off the block!
				claw.open();

	    	}


		}

		Display.update("Status", "Final");
		//FINISH

    }


    private boolean waitForEdge(){
    	//This variable is triggered once we have seen the block, so we know we are seeing the far edge of the block when we no longer see it
		boolean triggered = false;
		//Keep looping while we're moving (we will abort movement when we stop seeing the block)
		while(nav.isNavigating()){
			//If we see a block and triggered hasn't been set true yet, set it to true
			if(checkForBlock() == true && triggered == false){
				triggered = true;
			}

			//If we no longer see a block, and we have seen one in the past (triggered is true), then stop navigating
			if(checkForBlock() == false && triggered == true){
				nav.abort();
			}
		}
		
		return triggered;
    }
    
    /**
     * Function to grab a block, but give an error if the claw accidentally lifts up the robot
     * @return
     */
    private boolean grabBlock(){
    	//close the claw but then float it.
    	//a good close will stay in pretty much the same position, but one where the robot was lifted up will now fall back down
    	claw.close();
    	claw.flt();
    	
    	//wait a bit for the fall to happen
    	try {
			Thread.sleep(750);
		} catch (InterruptedException e) {
		}
    	
    	//if the didnt change too much, we know it was a good pickup
    	int ANG_ERR = 20;
    	Display.update("SX", Integer.toString(claw.getAngle()));
    	if(claw.getAngle() >= Claw.CLOSED_ANGLE - ANG_ERR && claw.getAngle() <= Claw.CLOSED_ANGLE + ANG_ERR){
    		//re-close the claw, return true
    		claw.close(); 
    		return true;
    	}
    	//else, it was a bad pickup
    	return false;
    }
    
    
    
    /**
     * This method causes the robot to follow a given path that's passed in
     * @param path Path object to follow
     */
    private void moveAlongPath(Path path, boolean delivery){
    	Display.update("Status", "Moving");

    	//Keep looping until we've visited every node on the path
		while(path!=null){
			
			//Move us to the next node along the path
			current = current.getNodeFromPath(new Path(path.getDirection()));

			//Get the position of the next node
			float x = current.getX();
			float y = current.getY();
			float theta = current.getTheta();

			//If it's not a turning node (so we are not currently in the tile), then move to it
			if(path.getDirection()==Path.Direction.FRONT){
				nav.travelTo(x, y, SPEED_SLOW);
				nav.waitUntilDone();
			}

			//Get the next node along the path
			path = path.getNextPath();

			//If path is done, turn to face the right direction
			if(path==null && !delivery){
				nav.turnTo(theta, SPEED_SLOW);
				nav.waitUntilDone();
			}
		}
    }


    /**
     * This method returns a float representing the direction of the found block
     * It causes the robot to scan for a block in order to find one
     * @param theta Current theta orientation of the robot.
     * @return float returned in direction of block (if none found, returns -1)
     */
    private float scanForBlock(float theta){

    	//Keep track of the state of seeing the block, and whether an edge has been triggered. Also the two angles representing each edge of the block
    	boolean triggered = false;
    	boolean seesBlock = false;

    	float angRight = -1;
    	float angLeft = -1;

    	//This angle is to the right of the current angle
    	float turnAng = theta - Pi.ONE_FIFTH;

    //	System.out.println(turnAng); // Alexi, please leave this println in
    								/*	For some reason, every time I take it out, the nav just doesn't move.
    								 * 	I've been debugging this for a while and it has nothing to do with the while loop below, it just doesn't get an angle that it moves to
    								 * 	So turnAng isn't correct unless I print it????? I don't know, just leave it in for now.
    								 */

    	
    	do{
    	//Turn to scan to the right, but dont waitUntilDone, so we can do other things while turning
	    	nav.turnTo(turnAng);
	
	    	//While we are still turning and either haven't seen a block yet at all, or we've seen a block and are still seeing a block, keep checking for a block
	    	while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
	
	    		seesBlock = checkForBlock(); //Check if there's a block (true if there is, false if not)
	
	    		//If we see a block and haven't seen one yet (triggered is false) then set triggered to true
	    		if(seesBlock==true && triggered==false){
	    			triggered=true;
	    		}
	
	    		//If we don't see a block and have already seen one (triggered is true), then we've reached the far edge of the block and we can stop
	    		if(seesBlock==false && triggered==true){
	    			//Stop moving, set the right angle to the current angle we rotated to
	    			nav.abort();
	    			angRight = nav.getOdometer().getTheta();
	    		}
	
	    	}
	    	
	    	//If we have seen a block, but not seen the end of it, then we keep turning even more
	    	turnAng -= Pi.ONE_SIXTH;
	    	//This will loop in that case
    	}while(triggered==true && angRight==-1);

    	//Now turn to the left past theta
    	turnAng = theta + Pi.ONE_FIFTH;
    	
    	do{
	
	    	nav.turnTo(turnAng);
	
	    	//reset triggered and follow the same logic for going left
	    	triggered = false;
	
	    	while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
	    		seesBlock = checkForBlock();
	
	    		if(seesBlock==true && triggered==false){
	    			triggered=true;
	    		}
	    		if(seesBlock==false && triggered==true){
	    			//Stop moving, set the left angle to the current angle we rotated to
	    			nav.abort();
	    			angLeft = nav.getOdometer().getTheta();
	    		}
	    	}
	    	
	    	//If we have seen a block, but not seen the end of it, then we keep turning even more
	    	turnAng += Pi.ONE_SIXTH;
	    	//This will loop in that case
    	}while(triggered==true && angRight==-1);

    	//Now, if we haven't seen the right edge of a block at all, then we scan on the way back to theta

    	if(angRight==-1){
    		//Turn back to theta
        	nav.turnTo(theta);

        	//Reset triggered, follow the same logic
    		triggered = false;

    		while(nav.isNavigating() && (triggered == false || (triggered == true && seesBlock == true))){
        		seesBlock = checkForBlock();


        		if(seesBlock==true && triggered==false){
        			triggered=true;
        		}

        		if(seesBlock==false && triggered==true){
        			//Stop moving, set the right angle to the current angle we rotated to
        			nav.abort();
        			angRight = nav.getOdometer().getTheta();
        		}

        	}

    	}

    	//NOTICE: This will not save an angle if we start to see a block, but quit navigating before we see the far edge of the block.
    	//That ends up saving our asses, because trying to pickup blocks that we haven't completely seen is really hard!

    	//If we have both a right and left angle then find the angle that the block is at
    	if(angRight != -1 && angLeft != -1){
    		//Correct if the angles are on opposite sides of 0
    		if(angRight > Pi.THREE_HALF && angLeft < Pi.ONE_HALF){
    			angRight -= Pi.TWO;
    		}
    		//Average the two angles and return
    		float result = (angRight+angLeft)/2;
    		return result;
    	}
    	//If we don't have both angles, return -1
    	return -1;
    }


    /**
     * This method checks the color sensor to see if there's a block
     * @return true or false if there's some block or not (regardless of block color)
     */
    private boolean checkForBlock() {
    	//Get the color data and split into parts
        int c = color.getColorData();
        int b = c & 255;
        int g = (c >> 8) & 255;
        int r = (c >> 16) & 255;
        //return true if it's not close to black
        return r>=50 || b>=50 || g>=50;
    }


    /**
     * Sets the current node for the SAR algorithm. Called at the end of localization
     * @param c a Node that specifies the robots current location.
     */
    public void setCurrent(Node c){
    	current = c;
    }
}
