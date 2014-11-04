import java.util.Queue;

/**
 * MapNode class used to represent a single position of the robot
 * @author Jonah
 *
 */
public class MapNode {
	private MapNode left;
	private MapNode right;
	private MapNode front;
	public int num;
	
	private boolean isValidStart;
	private boolean visited;
	private MapNode parent;
	
	/**
	 * Constructs a new empty MapNode
	 */
	public MapNode(){
		isValidStart = false;
		visited = false;
		left = null;
		right = null;
		front = null;	
	}
	
	/**
	 * Returns the MapNode at the end of the MapPath as followed if starting from this MapPath
	 * @param path The MapPath to follow
	 * @return The MapNode at the end of the path
	 */
	public MapNode getNodeFromPath(MapPath path){
		if(path!=null){
			if(path.getDirection() == MapPath.Direction.LEFT){
				if(this.left!=null){
					return this.left.getNodeFromPath(path.getNextMapPath());
				}
				return null;
			}else if(path.getDirection() == MapPath.Direction.RIGHT){
				if(this.right!=null){
					return this.right.getNodeFromPath(path.getNextMapPath());
				}
				return null;
			}else if(path.getDirection() == MapPath.Direction.FRONT){
				if(this.front!=null){
					return this.front.getNodeFromPath(path.getNextMapPath());
				}
				return null;
			}
		}
		return this;
	}
	
	/**
	 * Returns a MapPath representing the shortest path from this node to a given MapNode
	 * @param m MapNode to find shortest path to
	 * @return MapPath representing the shortest path
	 */
	public MapPath getShortestPathTo(MapNode m){
		Queue<MapNode> queue = new Queue<MapNode>();
		this.parent = null;
		this.visited = true;
		queue.push(this);
		MapNode currNode = this;
		while(!queue.isEmpty()&&currNode!=m){
			currNode = (MapNode) queue.pop();
			MapNode nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
			if(nextNode!=null&&nextNode.getVisited()==false){
				nextNode.setParent(currNode);
				nextNode.setVisited(true);
				queue.push(nextNode);
			}
			nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
			if(nextNode!=null&&nextNode.getVisited()==false){
				nextNode.setParent(currNode);
				nextNode.setVisited(true);
				queue.push(nextNode);
			}
			nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
			if(nextNode!=null&&nextNode.getVisited()==false){
				nextNode.setParent(currNode);
				nextNode.setVisited(true);
				queue.push(nextNode);
			}
		}
		MapPath result = null;
		if(currNode==m){	
			while(currNode.getParent()!=null){
				if(currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.LEFT)) == currNode){
					result = new MapPath(MapPath.Direction.LEFT, result);
				}else if(currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.RIGHT)) == currNode){
					result = new MapPath(MapPath.Direction.RIGHT, result);
				}else if(currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.FRONT)) == currNode){
					result = new MapPath(MapPath.Direction.FRONT, result);
				}
				currNode = currNode.getParent();
			}
		}
		return result;
	}
	
	/**
	 * Sets one of the three children of the MapNode
	 * @param d Direction to specify which child to set
	 * @param m MapNode to set the child to
	 */
	public void setChild(MapPath.Direction d, MapNode m){
		if(d == MapPath.Direction.LEFT){
			left = m;
		}else if(d == MapPath.Direction.RIGHT){
			right = m;
		}else if(d == MapPath.Direction.FRONT){
			front = m;
		}
	}
	
	/**
	 * Getter for the isValidStart boolean
	 * @return isValidStart
	 */
	public boolean getIsValidStart(){
		return isValidStart;
	}
	
	/**
	 * Setter for the isValidStart boolean
	 * @param b Value to set isValidStart
	 */
	public void setIsValidStart(boolean b){
		isValidStart = b;
	}
	
	/**
	 * Getter for the parent MapNode
	 * @return parent
	 */
	public MapNode getParent(){
		return parent;
	}
	
	/**
	 * Setter for the parent MapNode
	 * @param p Value to set parent
	 */
	public void setParent(MapNode p){
		parent = p;
	}
	
	/**
	 * Getter for the visited boolean
	 * @return visited
	 */
	public boolean getVisited(){
		return visited;
	}
	
	/**
	 * Setter for the visited boolean
	 * @param b Value to set visited
	 */
	public void setVisited(boolean b){
		visited = b;
	}
}
