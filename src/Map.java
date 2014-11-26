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

						//TODO remove this code and replace with a better way of finding the start/finish
						if(map[j-1][i]==3){
							start = getNodeAtPosition(i, j, Pi.ONE_HALF).getNum();
						}
						//------
					}
					if(i>0&&map[j][i-1]!=1){//Check the square to the west
						getNodeAtPosition(i, j, Pi.ONE).setChild(Path.Direction.FRONT, getNodeAtPosition(i-1, j, Pi.ONE));
					}
					if(j<map[0].length-1&&map[j+1][i]!=1){//Check the square to the south
						getNodeAtPosition(i, j, Pi.THREE_HALF).setChild(Path.Direction.FRONT, getNodeAtPosition(i, j+1, Pi.THREE_HALF));

						//TODO remove this code and replace with a better way of finding the start/finish
						if(map[j+1][i]==2){
							finish = getNodeAtPosition(i, j, Pi.THREE_HALF).getNum();
						}
						//-----------
					}

					//Now we set all the left/right nodes properly

					//Set node facing 0 to have left facing 90, and right facing 270
					getNodeAtPosition(i, j, 0).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, Pi.ONE_HALF));
					getNodeAtPosition(i, j, 0).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, Pi.THREE_HALF));

					//set node facing 90 to have left facing 180, and right facing 0
					getNodeAtPosition(i, j, Pi.ONE_HALF).setChild(Path.Direction.LEFT, getNodeAtPosition(i, j, Pi.ONE));
					getNodeAtPosition(i, j, Pi.ONE_HALF).setChild(Path.Direction.RIGHT, getNodeAtPosition(i, j, 0));

					//set node facing 180 to have left facing 90, and right facing 270
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
		return nodes[(int) ((x + y*(int)Math.sqrt((float)nodes.length/4))*4 + (int)(t/(Pi.ONE_HALF)))];
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
	public int getLength(){
		return (int) Math.sqrt(nodes.length/4);
	}

}
