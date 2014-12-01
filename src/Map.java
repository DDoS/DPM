import java.util.ArrayList;

/**
 * Map class holds all of the Nodes
 *
 * @author Jonah
 */
public class Map {
	private Node[] nodes;
	private int start;
	private int finish;
	private static int length;

	/**
	 * Initialize a new Map object with a set size.
	 * @param size An integer designating the number of nodes in the map.
	 */
	public Map(int size){
		nodes = new Node[size];
		start = 0;
		finish = 0;
	}

	/**
	 * Initialize a new Map object based on an integer array of binary values.
	 * @param map A 2D array of integers representing the layout of the map.
	 */
	public Map(int[][] map){
		//As long as there's a map, we want to create the graph that represents the array
		length = map.length;
		if(map.length>0){
			//create a new Node array of the right size to hold all the values (4x the size of the array, one for each direction per square)
			nodes = new Node[map.length * map[0].length * 4];
			for(int i=0; i<nodes.length; i++){
				nodes[i] = new Node();
				nodes[i].setNum(i);
			}
			//For each square in the array, we need to do a bunch of math to add right children to the node
			for(int i=0; i<map.length; i++){
				for(int j=0; j<map[0].length; j++){
					if(i<map.length-1&&map[j][i+1]!=1){ //If the square isn't at the edge of the array, and it has an empty space to the east, we set the child to the next node over.
						//Otherwise the child is already null, so we don't set anything
						getNodeAtPosition(i, j, 0).setChild(Path.Direction.FRONT, getNodeAtPosition(i+1, j, 0));
					}
					if(j>0&&map[j-1][i]!=1){//Check for the square to the north of this one
						getNodeAtPosition(i, j, Pi.ONE_HALF).setChild(Path.Direction.FRONT, getNodeAtPosition(i, j-1, Pi.ONE_HALF));

						//Look for the delivery node (We deliver facing north)
						if(map[j][i]==3){
							start = getNodeAtPosition(i, j, Pi.ONE_HALF).getNum();
						}
					}
					if(i>0&&map[j][i-1]!=1){//Check the square to the west
						getNodeAtPosition(i, j, Pi.ONE).setChild(Path.Direction.FRONT, getNodeAtPosition(i-1, j, Pi.ONE));
						
						//Look for the collection node (because we always want to collect starting facing west)
						if(map[j][i-1]==2){
							finish = getNodeAtPosition(i, j, Pi.ONE).getNum();
						}
						
					}
					if(j<map[0].length-1&&map[j+1][i]!=1){//Check the square to the south
						getNodeAtPosition(i, j, Pi.THREE_HALF).setChild(Path.Direction.FRONT, getNodeAtPosition(i, j+1, Pi.THREE_HALF));
					}

					//Now we set all the left/right nodes properly

					//Set node facing 0 to have left facing 90, and right facing 270
					getNodeAtPosition(i, j, 0).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, Pi.ONE_HALF));
					getNodeAtPosition(i, j, 0).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, Pi.THREE_HALF));

					//set node facing 90 to have left facing 180, and right facing 0
					getNodeAtPosition(i, j, Pi.ONE_HALF).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, Pi.ONE));
					getNodeAtPosition(i, j, Pi.ONE_HALF).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, 0));

					//set node facing 180 to have left facing 270, and right facing 90
					getNodeAtPosition(i, j, Pi.ONE).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, Pi.THREE_HALF));
					getNodeAtPosition(i, j, Pi.ONE).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, Pi.ONE_HALF));

					//set node facing 270 to have left facing 0 and right facing 180
					getNodeAtPosition(i, j, Pi.THREE_HALF).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, 0));
					getNodeAtPosition(i, j, Pi.THREE_HALF).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, Pi.ONE));

					//If we're currently on a tile with a block on it, make sure it's not considered a valid starting spot
					if(map[j][i]==1){
						getNodeAtPosition(i, j, 0).setIsValidStart(false);
						getNodeAtPosition(i, j, Pi.ONE_HALF).setIsValidStart(false);
						getNodeAtPosition(i, j, Pi.ONE).setIsValidStart(false);
						getNodeAtPosition(i, j, Pi.THREE_HALF).setIsValidStart(false);
					}
				}
			}
		}
	}

	/**
	 * Returns an ArrayList of Node objects
	 * This is populated only with the Node objects that are still valid starting locations
	 * @return The ArrayList<Node> with the nodes that are still valid.
	 */
	public ArrayList<Node> getRemaningNodes(){
		//Loop through all the nodes, and if the node is a valid starting node, add it to the list which is returned
		ArrayList<Node> result = new ArrayList<Node>();
		for(int i=0; i<nodes.length; i++){
			if(nodes[i].getIsValidStart()){
				result.add(nodes[i]);
			}
		}
		return result;
	}

	/**
	 * Returns the shortest Path between two nodes.
	 * Takes in a node to begin, and a node to end.
	 * Calls Node.getShortestPathTo(Node n)
	 * @param from The Node to start at
	 * @param to The Node to finish
	 * @return A Path which represents the shortest path between the nodes
	 */
	public Path getPathFromNodeToNode(Node from, Node to){
		//Reset all the nodes to not visited
		for(int i=0; i<nodes.length; i++){
			nodes[i].setVisited(false);
		}
		//Call the Node function to get the right path
		return from.getShortestPathTo(to);
	}

	/**
	 * Returns the Node at the specified index
	 * @param i An integer representing the index
	 * @return The Node at that index
	 */
	public Node getNodeAtIndex(int i){
		return nodes[i];
	}

	/**
	 * Returns the Node at the specified position
	 * Based on the x value (increasing to the east from 0 at the left)
	 * And the y value (increasing to the south from 0 at the top)
	 * And the theta value (0 facing east, increasing counter clockwise)
	 * @param x X value of the node
	 * @param y Y value of the node
	 * @param t Theta value of the node in degrees
	 * @return The Node matching the parameters
	 */
	public Node getNodeAtPosition(int x, int y, double t){
		return nodes[(int) ((x + y*(int)Math.sqrt((float)nodes.length/4))*4 + (int)((t+0.01)/(Pi.ONE_HALF)))];
	}

	/**
	 * Returns the Node where the blocks should be delivered
	 * This node is specified when creating the Map
	 * @return Node where blocks need to go
	 */
	public Node getDeliveryNode(){
		return nodes[start];
	}

	/**
	 * Returns the Node where blocks should be picked up
	 * The block search algorithm should start here
	 * This node is specified when creating the Map
	 * @return Node where blocks are picked up
	 */
	public Node getCollectionNode(){
		return nodes[finish];
	}

	/**
	 * Get the "length" of the map, as in the length of one side (so 12 for the final project)
	 * @return int length based on the number of nodes. assumes map is square
	 */
	public static int getLength(){
		return length;
	}

	/**
	 * Find a path in the map from some tile coordinates to other ones. Uses depth first search with a heuristic based on distance to target to find a good path
	 * @param map The map to path find in
	 * @param fromTileX The starting x coordinate in tile coordinates
	 * @param fromTileY The starting y coordinate in tile coordinates
	 * @param toTileX The target x coordinate in tile coordinates
	 * @param toTileY The target y coordinate in tile coordinates
	 * @return An {@link Integer.MAX_VALUE} terminated stack or integer coordinates, each pair being successive in the array
	 */
	public static int[] findPath(int[][] map, int fromTileX, int fromTileY, int toTileX, int toTileY) {
		int size = map.length;
		// Make a copy of the coordinates
		int cx = fromTileX, cy = fromTileY;
		// Allocate an array with enough room to store the path to the end (2 ints per node)
		int[] path = new int[size * size * 2];
		// Current index into the path stack
		int index = 0;
		// A grid of traveled states (0 = unvisited)
		int[][] traveled = new int[size][size];
		// Obstacles are marked as traveled
		for (int i = 0; i < size; i++) {
			System.arraycopy(map[i], 0, traveled[i], 0, size);
		}
		// Check if the end is actually reachable
		boolean endBlocked = map[size - 1 - toTileY][toTileX] == 1;
		// Repeat until we reach the end node
		outer:
		while (cx != toTileX || cy != toTileY) {
			// Find the closest neighboor to the end by checking them all
			int nextX = -1, nextY = -1, distance = Integer.MAX_VALUE;
			for (Path.Direction next : Path.Direction.values()) {
				// Get the neighboor coordinates and index
				int nx = cx + next.xOffset;
				int ny = cy + next.yOffset;
				// If the end can't be reached, finish when we're right next to it
				if (endBlocked && nx == toTileX && ny == toTileY) {
					break outer;
				}
				// Check if the neighboor is in the map and hasn't been visited
				if (inMap(size, nx, ny) && traveled[size - 1 - ny][nx] != 1) {
					// Get the manhattan distance to the end tile
					int newDistance = manhattanDistance(toTileX, toTileY, nx, ny);
					// If smaller than the current best, update to it as the new best
					if (newDistance < distance) {
						nextX = nx;
						nextY = ny;
						distance = newDistance;
					}
				}
			}
			// Set the current node as traveled
			traveled[size - 1 - cy][cx] = 1;
			// Update current coordinates to the neighboor
			cx = nextX;
			cy = nextY;
			// Check if we actually found a neighboor to travel to
			if (distance == Integer.MAX_VALUE) {
				// If we don't have a previous node, we end here with no path
				if (index == 0) {
					break;
				}
				// Else, pop the stack, move the path back to last node
				index -= 2;
				cx = path[index];
				cy = path[index + 1];
				// We will try another direction next iteration
			} else {
				// Else push this node to the stack
				path[index] = cx;
				path[index + 1] = cy;
				index += 2;
			}
		}
		// Mark path end and return it
		path[index] = Integer.MAX_VALUE;
		path[index + 1] = Integer.MAX_VALUE;
		return path;
	}

	// Checks if the coordinates are inside the field
	private static boolean inMap(int size, int x, int y) {
		// If we have bits outside the mask, the coordinate is outside the range
		return x >= 0 && x < size && y >= 0 && y < size;
	}


	// Compute the manhattan distance between two points
	private static int manhattanDistance(int ax, int ay, int bx, int by) {
		// Sum of the absolute values of the differences
		return Math.abs(bx - ax) + Math.abs(by - ay);
	}
}
