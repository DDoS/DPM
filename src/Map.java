import java.util.ArrayList;


public class Map {
	private MapNode[] nodes;
	
	public Map(int size){
		nodes = new MapNode[size];
	}
	
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
				}
			}
		}
	}
	
	public ArrayList<MapNode> getRemaningNodes(){
		ArrayList<MapNode> result = new ArrayList<MapNode>();
		for(int i=0; i<nodes.length; i++){
			if(nodes[i].getIsValidStart()){
				result.add(nodes[i]);
			}
		}
		return result;
	}
	
	public MapPath getPathFromNodeToNode(MapNode f, MapNode t){
		for(int i=0; i<nodes.length; i++){
			nodes[i].setVisited(false);
		}
		return f.getShortestPathTo(t);
	}
	
	public MapNode getNodeAtIndex(int i){
		return nodes[i];
	}
	
	public MapNode getNodeAtPosition(int x, int y, int t){
		return nodes[(x + y*(int)Math.sqrt((double)nodes.length/4))*4 + t/90];
	}
	
}
