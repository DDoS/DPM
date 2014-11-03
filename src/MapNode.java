import java.util.LinkedList;
import java.util.Queue;




public class MapNode {
	private MapNode left;
	private MapNode right;
	private MapNode front;

	private boolean isValidStart;

	private MapNode parent;

	public MapNode(){
		isValidStart = false;
		left = null;
		right = null;
		front = null;
	}
	public MapNode getNodeFromPath(MapPath path){
		if(path!=null){
			if(path.getDirection() == MapPath.Direction.LEFT){
				return this.left.getNodeFromPath(path.getNextMapPath());
			}else if(path.getDirection() == MapPath.Direction.RIGHT){
				return this.right.getNodeFromPath(path.getNextMapPath());
			}else if(path.getDirection() == MapPath.Direction.FRONT){
				return this.front.getNodeFromPath(path.getNextMapPath());
			}
		}
		return this;
	}

	public MapPath getShortestPathTo(MapNode m){
		Queue<MapNode> queue = new LinkedList<MapNode>();
		this.parent = null;
		queue.add(this);
		MapNode currNode = this;
		while(!queue.isEmpty()&&currNode!=m){
			currNode = queue.poll();
			MapNode nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
			if(nextNode!=null&&nextNode.getParent()==null){
				nextNode.setParent(currNode);
				queue.add(nextNode);
			}
			nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
			if(nextNode!=null&&nextNode.getParent()==null){
				nextNode.setParent(currNode);
				queue.add(nextNode);
			}
			nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
			if(nextNode!=null&&nextNode.getParent()==null){
				nextNode.setParent(currNode);
				queue.add(nextNode);
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

	public void setChild(MapPath.Direction d, MapNode m){
		if(d == MapPath.Direction.LEFT){
			left = m;
		}else if(d == MapPath.Direction.RIGHT){
			right = m;
		}else if(d == MapPath.Direction.FRONT){
			front = m;
		}
	}

	public boolean getIsValidStart(){
		return isValidStart;
	}

	public void setIsValidStart(boolean b){
		isValidStart = b;
	}

	public MapNode getParent(){
		return parent;
	}

	public void setParent(MapNode p){
		parent = p;
	}
}
