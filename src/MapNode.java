import java.util.Queue;




public class MapNode {
	private MapNode left;
	private MapNode right;
	private MapNode front;
	public int num;
	
	private boolean isValidStart;
	private boolean visited;
	private MapNode parent;
	
	public MapNode(){
		isValidStart = false;
		visited = false;
		left = null;
		right = null;
		front = null;	
	}
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
	
	public boolean getVisited(){
		return visited;
	}
	
	public void setVisited(boolean b){
		visited = b;
	}
}
