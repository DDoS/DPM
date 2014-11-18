import java.util.ArrayList;

import lejos.nxt.Sound;
import lejos.nxt.comm.RConsole;


public class LocalizationController {
	//Has a copy of the nav, has a map, and has two sensors
	private Navigation nav;
	private Map map;
	private FilteredUltrasonicSensor front_us, rear_us;
	private static final int MAX_TILES = 2;
	private static final boolean DUEL_SENSOR = false;
	private SearchAndRescueController searchAndRescue;

	/**
	 * Constructs a new localization controller with every property
	 * @param n The nav for the controller to use
	 * @param m The map for it to use (in our case, will be null)
	 * @param s1 The front ultrasonic sensor
	 * @param s2 The rear ultrasonic sensor
	 */
	public LocalizationController(Navigation n, Map m, FilteredUltrasonicSensor s1, FilteredUltrasonicSensor s2, SearchAndRescueController sarC){
		nav = n;
		map = m;
		front_us = s1;
		rear_us = s2;
		searchAndRescue = sarC;
	}

	/**
	 * Computes the standard deviation of a given integer array
	 * @param arr The array of integers (any size)
	 * @return The standard deviation as a float
	 */
	public float stdDev(int[] arr){
		//Loop through the array and sum the values divided by the length to find the average
		float avg = 0;
		for(int i=0; i<arr.length; i++){
			avg += (float)arr[i]/arr.length;
		}
		//Loop through the array and add to a new sum (each element-avg)^2/length
		float newAvg = 0;
		for(int i=0; i<arr.length; i++){
			newAvg += Math.pow(arr[i]-avg, 2)/arr.length;
		}
		//return the square root of that sum (the standard deviation of the original array)
		return (float) Math.sqrt(newAvg);
	}

	/**
	 * Method called to start the Localization Controller
	 * Handles all the logic of the controller
	 */
	public void run(){
		//Setting up the display
//		Display.clear();
//		Display.reserve("Status", "X", "Y", "Th", "Moves");
//		Display.update("Status", "Init");

		//path represents the current path that the robot has actually traveled
		MapPath path = null;
		//nodes represents all of the nodes that are considered to be valid starting options
		ArrayList<MapNode> nodes = map.getRemaningNodes();

		//We continue to run this algorithm until there is one (or zero) nodes left
		//Display.update("Status", "Run");
		int moves = 0;
		float currTheta = (float) (Math.PI/2);
		while(nodes.size()>1){
			//Display.update("Moves", ""+moves);
			moves++;
			int rearTiles;
			//Get the distance data and use it to find out how many empty tiles surround the robot
			int fDist = front_us.getDistanceData();
			if(DUEL_SENSOR) {
				int rDist = rear_us.getDistanceData();
				rearTiles = rDist/30;
			}
			int frontTiles = fDist/30;
			//cutoff at max number of tiles (due to sensor accuracy)
			if(frontTiles>MAX_TILES){
				frontTiles = MAX_TILES;
			}
			if(DUEL_SENSOR){
				if(rearTiles>MAX_TILES){
					rearTiles = MAX_TILES;
				}
			}


			//Loop through all of the nodes that are still considered valid, so we can check if they're still valid when compared to this new sensor data
			for(MapNode m : nodes){
				//if m represents the node we started from, n represents the place that the robot would be if it followed path from m
				MapNode n = m.getNodeFromPath(path);
				MapNode r = n.getNodeFromPath(new MapPath(MapPath.Direction.LEFT, new MapPath(MapPath.Direction.LEFT))); //This one is facing the rear
				//scan forward one by one and make sure the sensor data matches with the map data, otherwise remove m as a valid option
				for(int i=0; i<=frontTiles; i++){
					if(i==MAX_TILES){ //If we are at the max distance, we don't actually care about this tile (due to sensor inaccuracy)
						break;
					}else if(i==frontTiles){//If we are at the end of what the sensor picked up, that means there's supposed to be a tile here
						n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
						if(n!=null){//If there isn't a tile here, then remove m
							m.setIsValidStart(false);
							break;
						}
					}else{//Else, there's not supposed to be a tile here
						n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
						if(n==null){//If there is a tile here, remove m
							m.setIsValidStart(false);
							break;
						}
					}
				}

				if(DUEL_SENSOR){
					//Repeat the same logic for the rear sensor
					for(int i=0; i<=rearTiles; i++){
						if(i==MAX_TILES){ //If we are at the max distance, we don't actually care about this tile (due to sensor inaccuracy)
							break;
						}else if(i==rearTiles){//If we are at the end of what the sensor picked up, that means there's supposed to be a tile here
							r = r.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
							if(r!=null){//If there isn't a tile here, then remove m
								m.setIsValidStart(false);
								break;
							}
						}else{//Else, there's not supposed to be a tile here
							r = r.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
							if(r==null){//If there is a tile here, remove m
								m.setIsValidStart(false);
								break;
							}
						}
					}
				}
			}

			//Now that we have removed some nodes, we update nodes
			nodes = map.getRemaningNodes();

			//Now we will look for the next move.
			//This array stores values that will be used to determine which move to take
			//Eg. If there is a tile 4 blocks in front of a given space, it will increment tileCount[2][4]
			//If there is a tile 0 blocks to the right of a given space, it will increment tileCount[1][0]
			//Then we take the standard deviation of these 3 arrays, and the lowest one is the best move
			int[][] tileCount = {
					{0, 0, 0, 0, 0, 0, 0, 0, 0},//left
					{0, 0, 0, 0, 0, 0, 0, 0, 0},//right
					{0, 0, 0, 0, 0, 0, 0, 0, 0}//front
			};

			//for each valid starting node left, we check the distance to the next tile from each direction
			for(MapNode m : nodes){

				MapNode n = m.getNodeFromPath(path);
				n = n.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
				n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Must add one node to the front to avoid off-by-one error

				//We loop and move the node forward until we hit a wall, then we increment the corresponding value in tileCount
				int i=0;
				while(n!=null){
					i++;
					n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT)); //Using l, facing left
				}
				//Cutoff at the max
				if(i>MAX_TILES){
					i=MAX_TILES;
				}
				//Increment the right position in the array
				tileCount[0][i]++;

				//reset and repeat for facing right
				n = m.getNodeFromPath(path);
				n = n.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
				n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Must add one node to the front to avoid off-by-one error
				i=0;
				while(n!=null){
					i++;
					n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Using r, facing right
				}
				if(i>MAX_TILES){
					i=MAX_TILES;
				}
				tileCount[1][i]++;

				//reset and repeat facing front
				n = m.getNodeFromPath(path);
				n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
				i=0;
				i=0;
				while(n!=null){
					i++;
					n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Using f, facing front
				}
				if(i>MAX_TILES){
					i=MAX_TILES;
				}
				tileCount[2][i]++;
			}

			//Now we compute the standard deviation of the arrays and use the lowest one for the move
			float stdL = stdDev(tileCount[0]);
			float stdR = stdDev(tileCount[1]);
			float stdF = stdDev(tileCount[2]);

			//If the std devs are all the same, we want to go to the move that has the obstacle which is farthest away.
			if(stdL==stdR&&stdL==stdF){
				//search through the lists backwards until we find a spot that isn't 0, then break. (can hit multiple spots at once, then the comparisons below take care of the rest)
				for(int i=MAX_TILES; i>=0; i--){
					boolean br = false;
					if(tileCount[0][i]!=0){
						stdL = -1;
						br = true;
					}
					if(tileCount[1][i]!=0){
						stdR = -1;
						br = true;
					}
					if(tileCount[2][i]!=0){
						stdF = -1;
						br = true;
					}
					if(br){
						break;
					}
				}
			}
			
			if(stdF<=stdL&&stdF<=stdR&&frontTiles!=0){ //Make sure there isn't a tile in front of us already if we want to move fowrard
				//The <= comparisons here make the forward move a little more common
				//Add a forward node to the path
				try{
					path.addMapPath(new MapPath(MapPath.Direction.FRONT));
				}catch(NullPointerException e){
					path = new MapPath(MapPath.Direction.FRONT);
				}
				//Move forward
				nav.forward(Odometer.TILE_SPACING);
				nav.waitUntilDone();//Wait for the navigation to finish
			}else if(stdL<stdR){ //If we want to move left, left has to have a lower standard deviation
				//Add a left node to the path
				try{
					path.addMapPath(new MapPath(MapPath.Direction.LEFT));
				}catch(NullPointerException e){
					path = new MapPath(MapPath.Direction.LEFT);
				}
				//Move left
				currTheta += (float) (Math.PI/2);
				nav.turnTo(currTheta);
				nav.waitUntilDone();//Wait for the navigation to finish

			}else{//else we want to move right
				//add a right node to the path
				try{
				 	path.addMapPath(new MapPath(MapPath.Direction.RIGHT));
				}catch(NullPointerException e){
					path = new MapPath(MapPath.Direction.RIGHT);
				}
				//Move right
				currTheta -= (float) (Math.PI/2);
				nav.turnTo(currTheta);
				nav.waitUntilDone();//Wait for the navigation to finish
			}
		}

		//End of algorithm, update position
		//Display.update("Status", "Final");

		MapNode current;//Current spot that the robot is in

		nodes = map.getRemaningNodes();
		if(nodes.size()!=1){//If the algorithm failed, choose a node at random and hope for the best
			current = map.getNodeAtIndex((int)(Math.random()*map.getLength()*4));
		}else{
			current = nodes.get(0).getNodeFromPath(path);//Else use what the algorithm found
		}

		int num = current.getNum();//Do math to find out the position
		float theta = (float) ((num%4)*(Math.PI/2));
		float x = Odometer.HALF_TILE_SPACING + Odometer.TILE_SPACING*(int)((num/4)%(map.getLength()));
		float y = (map.getLength()-1)*Odometer.TILE_SPACING+Odometer.HALF_TILE_SPACING - Odometer.TILE_SPACING*(int)((num/4)/(map.getLength()));

		//Update the display and the odometer
//		Display.update("X", ""+x);
//		Display.update("Y", ""+y);
//		Display.update("Th", ""+theta);
		nav.getOdometer().setPosition(x, y, theta);
		
		lejos.nxt.Button.waitForAnyPress();

		searchAndRescue.setCurrent(current);

	}
}
