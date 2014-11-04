/**
 * MapPath class used to represent paths between nodes
 * @author Jonah
 *
 */
public class MapPath {
	
	/**
	 * An enum used to represent the three valid directions from a node
	 * @author Jonah
	 *
	 */
	public enum Direction{
		LEFT,
		RIGHT,
		FRONT
	}
	
	private Direction dir;
	private MapPath next;
	
	/**
	 * Constructs a MapPath with a given Direction
	 * @param d Direction to specify for the MapPath
	 */
	public MapPath(Direction d){
		dir = d;
	}
	
	/**
	 * Constructs a MapPath with a given Direction, and another MapPath to link to
	 * Returns the head of the new MapPath
	 * @param d Direction to specify
	 * @param m MapPath to set as child of head
	 */
	public MapPath(Direction d, MapPath m){
		dir = d;
		next = m;
	}
	
	/**
	 * Adds a new MapPath to the end of an existing MapPath
	 * @param m MapPath to add
	 */
	public void addMapPath(MapPath m){
		if(this.next!=null){
			this.addMapPath(m);
		}else{
			this.next = m;
		}
	}
	
	/**
	 * Returns the Direction of the MapPath
	 * @return Direction
	 */
	public Direction getDirection(){
		return dir;
	}
	
	/**
	 * Returns the next MapPath in order
	 * @return Next MapPath
	 */
	public MapPath getNextMapPath(){
		return this.next;
	}
	
}
