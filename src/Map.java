import java.util.ArrayList;

/**
 * Map class holds all of the MapNodes
 *
 * @author Jonah
 */
public class Map {
	private MapNode[] nodes;
	private int start;
	private int finish;

	/**
	 * Initialize a new Map object with a set size.
	 * @param size An integer designating the number of nodes in the map.
	 */
	public Map(int size){
		nodes = new MapNode[size];
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
			//create a new MapNode array of the right size to hold all the values (4x the size of the array, one for each direction per square)
			nodes = new MapNode[map.length * map[0].length * 4];
			for(int i=0; i<nodes.length; i++){
				nodes[i] = new MapNode();
				nodes[i].setNum(i);
			}
			//For each square in the array, we need to do a bunch of math to add right children to the node
			for(int i=0; i<map.length; i++){
				for(int j=0; j<map[0].length; j++){
					if(i<map.length-1&&map[j][i+1]!=1){ //If the square isn't at the edge of the array, and it has an empty space to the east, we set the child to the next node over.
						//Otherwise the child is already null, so we don't set anything
						getNodeAtPosition(i, j, 0).setChild(MapPath.Direction.FRONT, getNodeAtPosition(i+1, j, 0));
					}
					if(j>0&&map[j-1][i]!=1){//Check for the square to the north of this one
						getNodeAtPosition(i, j, 90).setChild(MapPath.Direction.FRONT, getNodeAtPosition(i, j-1, 90));
						if(map[j-1][i]==2){
							finish = getNodeAtPosition(i, j, 90).getNum();
						}
						if(map[j-1][i]==2){
							start = getNodeAtPosition(i, j, 90).getNum();
						}
					}
					if(i>0&&map[j][i-1]!=1){//Check the square to the west
						getNodeAtPosition(i, j, 180).setChild(MapPath.Direction.FRONT, getNodeAtPosition(i-1, j, 180));
					}
					if(j<map[0].length-1&&map[j+1][i]!=1){//Check the square to the south
						getNodeAtPosition(i, j, 270).setChild(MapPath.Direction.FRONT, getNodeAtPosition(i, j+1, 270));
					}

					//Now we set all the left/right nodes properly
					getNodeAtPosition(i, j, 0).setChild(MapPath.Direction.LEFT, getNodeAtPosition(i, j, 90));
					getNodeAtPosition(i, j, 0).setChild(MapPath.Direction.RIGHT, getNodeAtPosition(i, j, 270));

					getNodeAtPosition(i, j, 90).setChild(MapPath.Direction.LEFT, getNodeAtPosition(i, j, 180));
					getNodeAtPosition(i, j, 90).setChild(MapPath.Direction.RIGHT, getNodeAtPosition(i, j, 0));

					getNodeAtPosition(i, j,180).setChild(MapPath.Direction.LEFT, getNodeAtPosition(i, j, 270));
					getNodeAtPosition(i, j, 180).setChild(MapPath.Direction.RIGHT, getNodeAtPosition(i, j, 90));

					getNodeAtPosition(i, j, 270).setChild(MapPath.Direction.LEFT, getNodeAtPosition(i, j, 0));
					getNodeAtPosition(i, j, 270).setChild(MapPath.Direction.RIGHT, getNodeAtPosition(i, j, 180));

					//If we're currently on a tile with a block on it, make sure it's not considered a valid starting spot
					if(map[j][i]==1){
						getNodeAtPosition(i, j, 0).setIsValidStart(false);
						getNodeAtPosition(i, j, 90).setIsValidStart(false);
						getNodeAtPosition(i, j, 180).setIsValidStart(false);
						getNodeAtPosition(i, j, 270).setIsValidStart(false);
					}
				}
			}
		}
	}

	/**
	 * Returns an ArrayList of MapNode objects
	 * This is populated only with the MapNode objects that are still valid starting locations
	 * @return The ArrayList<MapNode> with the nodes that are still valid.
	 */
	public ArrayList<MapNode> getRemaningNodes(){
		//Loop through all the nodes, and if the node is a valid starting node, add it to the list which is returned
		ArrayList<MapNode> result = new ArrayList<MapNode>();
		for(int i=0; i<nodes.length; i++){
			if(nodes[i].getIsValidStart()){
				result.add(nodes[i]);
			}
		}
		return result;
	}

	/**
	 * Returns the shortest MapPath between two nodes.
	 * Takes in a node to begin, and a node to end.
	 * Calls MapNode.getShortestPathTo(MapNode n)
	 * @param from The MapNode to start at
	 * @param to The MapNode to finish
	 * @return A MapPath which represents the shortest path between the nodes
	 */
	public MapPath getPathFromNodeToNode(MapNode from, MapNode to){
		//Reset all the nodes to not visited
		for(int i=0; i<nodes.length; i++){
			nodes[i].setVisited(false);
		}
		//Call the MapNode function to get the right path
		return from.getShortestPathTo(to);
	}

	/**
	 * Returns the MapNode at the specified index
	 * @param i An integer representing the index
	 * @return The MapNode at that index
	 */
	public MapNode getNodeAtIndex(int i){
		return nodes[i];
	}

	/**
	 * Returns the MapNode at the specified position
	 * Based on the x value (increasing to the east from 0 at the left)
	 * And the y value (increasing to the south from 0 at the top)
	 * And the theta value (0 facing east, increasing counter clockwise)
	 * @param x X value of the node
	 * @param y Y value of the node
	 * @param t Theta value of the node in degrees
	 * @return The MapNode matching the parameters
	 */
	public MapNode getNodeAtPosition(int x, int y, int t){
		return nodes[(x + y*(int)Math.sqrt((float)nodes.length/4))*4 + t/90];
	}

	/**
	 * Returns the MapNode where the blocks should be delivered
	 * This node is specified when creating the Map
	 * @return MapNode where blocks need to go
	 */
	public MapNode getDeliveryNode(){
		return nodes[start];
	}
	
	/**
	 * Returns the MapNode where blocks should be picked up
	 * The block search algorithm should start here
	 * This node is specified when creating the Map
	 * @return MapNode where blocks are picked up
	 */
	public MapNode getCollectionNode(){
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
