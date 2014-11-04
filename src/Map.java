import java.util.ArrayList;

/**
 * Map class holds all of the MapNodes
 *
 * @author Jonah
 */
public class Map {
<<<<<<< HEAD
	private MapNode[] nodes;
	
	/**
	 * Initialize a new Map object with a set size.
	 * @param size An integer designating the number of nodes in the map.
	 */
	public Map(int size){
		nodes = new MapNode[size];
	}
	
	/**
	 * Initialize a new Map object based on an integer array of binary values.
	 * @param map A 2D array of integers representing the layout of the map.
	 */
	public Map(int[][] map){
		if(map.length>0){
			nodes = new MapNode[map.length * map[0].length * 4];
			for(int i=0; i<nodes.length; i++){
				nodes[i] = new MapNode();
				nodes[i].num = i;
			}
			for(int i=0; i<map.length; i++){
				for(int j=0; j<map[0].length; j++){
					if(i<map.length-1&&map[j][i+1]==0){
						nodes[(i + j*map.length)*4 + 0].setChild(MapPath.Direction.FRONT, nodes[(i+1 + j*map.length)*4 + 0]);
					}
					if(j>0&&map[j-1][i]==0){
						nodes[(i + j*map.length)*4 + 1].setChild(MapPath.Direction.FRONT, nodes[(i + (j-1)*map.length)*4 + 1]);
					}
					if(i>0&&map[j][i-1]==0){
						nodes[(i + j*map.length)*4 + 2].setChild(MapPath.Direction.FRONT, nodes[(i-1 + j*map.length)*4 + 2]);
					}
					if(j<map[0].length-1&&map[j+1][i]==0){
						nodes[(i + j*map.length)*4 + 3].setChild(MapPath.Direction.FRONT, nodes[(i + (j+1)*map.length)*4 + 3]);
					}
					
					nodes[(i + j*map.length)*4 + 0].setChild(MapPath.Direction.LEFT, nodes[(i + j*map.length)*4 + 1]);
					nodes[(i + j*map.length)*4 + 0].setChild(MapPath.Direction.RIGHT, nodes[(i + j*map.length)*4 + 3]);
					
					nodes[(i + j*map.length)*4 + 1].setChild(MapPath.Direction.LEFT, nodes[(i + j*map.length)*4 + 2]);
					nodes[(i + j*map.length)*4 + 1].setChild(MapPath.Direction.RIGHT, nodes[(i + j*map.length)*4 + 0]);
					
					nodes[(i + j*map.length)*4 + 2].setChild(MapPath.Direction.LEFT, nodes[(i + j*map.length)*4 + 3]);
					nodes[(i + j*map.length)*4 + 2].setChild(MapPath.Direction.RIGHT, nodes[(i + j*map.length)*4 + 1]);
					
					nodes[(i + j*map.length)*4 + 3].setChild(MapPath.Direction.LEFT, nodes[(i + j*map.length)*4 + 0]);
					nodes[(i + j*map.length)*4 + 3].setChild(MapPath.Direction.RIGHT, nodes[(i + j*map.length)*4 + 2]);
					
					if(map[j][i]==1){
						nodes[(i + j*map.length)*4 + 0].setIsValidStart(false);
						nodes[(i + j*map.length)*4 + 1].setIsValidStart(false);
						nodes[(i + j*map.length)*4 + 2].setIsValidStart(false);
						nodes[(i + j*map.length)*4 + 3].setIsValidStart(false);
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
		for(int i=0; i<nodes.length; i++){
			nodes[i].setVisited(false);
		}
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
		return nodes[(x + y*(int)Math.sqrt((double)nodes.length/4))*4 + t/90];
	}
	
=======
    private MapNode[] nodes;

    /**
     * Initialize a new Map object with a set size.
     *
     * @param size An integer designating the number of nodes in the map.
     */
    public Map(int size) {
        nodes = new MapNode[size];
    }

    /**
     * Initialize a new Map object based on an integer array of binary values.
     *
     * @param map A 2D array of integers representing the layout of the map.
     */
    public Map(int[][] map) {
        if (map.length > 0) {
            nodes = new MapNode[map.length * map[0].length * 4];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = new MapNode();
                nodes[i].num = i;
            }
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (i < map.length - 1 && map[j][i + 1] == 0) {
                        nodes[(i + j * map.length) * 4 + 0].setChild(MapPath.Direction.FRONT, nodes[(i + 1 + j * map.length) * 4 + 0]);
                    }
                    if (j > 0 && map[j - 1][i] == 0) {
                        nodes[(i + j * map.length) * 4 + 1].setChild(MapPath.Direction.FRONT, nodes[(i + (j - 1) * map.length) * 4 + 1]);
                    }
                    if (i > 0 && map[j][i - 1] == 0) {
                        nodes[(i + j * map.length) * 4 + 2].setChild(MapPath.Direction.FRONT, nodes[(i - 1 + j * map.length) * 4 + 2]);
                    }
                    if (j < map[0].length - 1 && map[j + 1][i] == 0) {
                        nodes[(i + j * map.length) * 4 + 3].setChild(MapPath.Direction.FRONT, nodes[(i + (j + 1) * map.length) * 4 + 3]);
                    }

                    nodes[(i + j * map.length) * 4 + 0].setChild(MapPath.Direction.LEFT, nodes[(i + j * map.length) * 4 + 1]);
                    nodes[(i + j * map.length) * 4 + 0].setChild(MapPath.Direction.RIGHT, nodes[(i + j * map.length) * 4 + 3]);

                    nodes[(i + j * map.length) * 4 + 1].setChild(MapPath.Direction.LEFT, nodes[(i + j * map.length) * 4 + 2]);
                    nodes[(i + j * map.length) * 4 + 1].setChild(MapPath.Direction.RIGHT, nodes[(i + j * map.length) * 4 + 0]);

                    nodes[(i + j * map.length) * 4 + 2].setChild(MapPath.Direction.LEFT, nodes[(i + j * map.length) * 4 + 3]);
                    nodes[(i + j * map.length) * 4 + 2].setChild(MapPath.Direction.RIGHT, nodes[(i + j * map.length) * 4 + 1]);

                    nodes[(i + j * map.length) * 4 + 3].setChild(MapPath.Direction.LEFT, nodes[(i + j * map.length) * 4 + 0]);
                    nodes[(i + j * map.length) * 4 + 3].setChild(MapPath.Direction.RIGHT, nodes[(i + j * map.length) * 4 + 2]);
                }
            }
        }
    }

    /**
     * Returns an ArrayList of MapNode objects This is populated only with the MapNode objects that are still valid starting locations
     *
     * @return The ArrayList<MapNode> with the nodes that are still valid.
     */
    public ArrayList<MapNode> getRemaningNodes() {
        ArrayList<MapNode> result = new ArrayList<MapNode>();
        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].getIsValidStart()) {
                result.add(nodes[i]);
            }
        }
        return result;
    }

    /**
     * Returns the shortest MapPath between two nodes. Takes in a node to begin, and a node to end. Calls MapNode.getShortestPathTo(MapNode n)
     *
     * @param from The MapNode to start at
     * @param to The MapNode to finish
     * @return A MapPath which represents the shortest path between the nodes
     */
    public MapPath getPathFromNodeToNode(MapNode from, MapNode to) {
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setVisited(false);
        }
        return from.getShortestPathTo(to);
    }

    /**
     * Returns the MapNode at the specified index
     *
     * @param i An integer representing the index
     * @return The MapNode at that index
     */
    public MapNode getNodeAtIndex(int i) {
        return nodes[i];
    }

    /**
     * Returns the MapNode at the specified position Based on the x value (increasing to the east from 0 at the left) And the y value (increasing to the south from 0 at the top) And the theta value (0
     * facing east, increasing counter clockwise)
     *
     * @param x X value of the node
     * @param y Y value of the node
     * @param t Theta value of the node in degrees
     * @return The MapNode matching the parameters
     */
    public MapNode getNodeAtPosition(int x, int y, int t) {
        return nodes[(x + y * (int) Math.sqrt((double) nodes.length / 4)) * 4 + t / 90];
    }
>>>>>>> aaf1c3b2cc902526ac7026812173e43063108362
}
