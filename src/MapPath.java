
public class MapPath {
	
	public enum Direction{
		LEFT,
		RIGHT,
		FRONT
	}
	
	private Direction dir;
	private MapPath next;
	
	public MapPath(Direction d){
		dir = d;
	}
	
	public MapPath(Direction d, MapPath m){
		dir = d;
		next = m;
	}
	
	public void addMapPath(MapPath m){
		if(this.next!=null){
			this.addMapPath(m);
		}else{
			this.next = m;
		}
	}
	
	public Direction getDirection(){
		return dir;
	}
	
	public MapPath getNextMapPath(){
		return this.next;
	}
	
}
