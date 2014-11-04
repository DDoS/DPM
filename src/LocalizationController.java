import java.util.ArrayList;


public class LocalizationController {
	private Navigation nav;
	private Map map;
	private FilteredUltrasonicSensor us;
	
	public LocalizationController(Navigation n, Map m, FilteredUltrasonicSensor s){
		nav = n;
		map = m;
		us = s;
	}
	
	public double stdDev(int[] arr){
		double avg = 0;
		for(int i=0; i<arr.length; i++){
			avg += (double)arr[i]/arr.length;
		}
		double newAvg = 0;
		double[] arr2 = new double[arr.length];
		for(int i=0; i<arr.length; i++){
			arr2[i] = Math.pow(arr[i]-avg, 2);
			newAvg += arr2[i]/arr2.length;
		}
		return Math.sqrt(newAvg);
	}
	
	public void run(){
		int[][] arr = {
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
				{0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
				{0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
				{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
				
		};
		
		int[][] arr2 = {
				{0, 0, 1},
				{1, 0, 1},
				{0, 0, 0}
		};
		
		Map map = new Map(arr);
		
		MapPath path = null;
		ArrayList<MapNode> nodes = map.getRemaningNodes();
		while(nodes.size()>1){
			int dist = us.getDistanceData();
			int tiles = dist/30;
			if(tiles>8){
				tiles = 8;
			}
			for(MapNode m : nodes){
				MapNode n = m.getNodeFromPath(path);
				for(int i=0; i<=tiles; i++){
					if(i==8){
						break;
					}else if(i==tiles){
						n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
						if(n!=null){
							m.setIsValidStart(false);
							break;
						}
					}else{
						n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
						if(n==null){
							m.setIsValidStart(false);
							break;
						}
					}
				}
			}
			nodes = map.getRemaningNodes();
			int[][] tileCount = {
					{0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0},
					{0, 0, 0, 0, 0, 0, 0, 0, 0}
			};
			for(MapNode m : nodes){
				MapNode n = m.getNodeFromPath(path);
				MapNode l = n.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
				MapNode r = n.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
				MapNode f = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
				int i=0;
				while(l!=null){
					i++;
					l = l.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
				}
				if(i>8){
					i=8;
				}
				tileCount[0][i]++;
				i=0;
				while(r!=null){
					i++;
					r = r.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
				}
				if(i>8){
					i=8;
				}
				tileCount[1][i]++;
				i=0;
				while(f!=null){
					i++;
					f = f.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
				}
				if(i>8){
					i=8;
				}
				tileCount[2][i]++;
			}
			double stdL = stdDev(tileCount[0]);
			double stdR = stdDev(tileCount[1]);
			double stdF = stdDev(tileCount[2]);
			if(stdF<=stdL&&stdF<=stdR&&tiles!=0){
				if(path!=null){
					path.addMapPath(new MapPath(MapPath.Direction.FRONT));
				}else{
					path = new MapPath(MapPath.Direction.FRONT);
				}
				//Move forward
				nav.forward(30);
			}else if(stdL<stdR){
				if(path!=null){
					path.addMapPath(new MapPath(MapPath.Direction.LEFT));
				}else{
					path = new MapPath(MapPath.Direction.LEFT);
				}
				//Move left
				nav.turnBy(90);
			}else{
				if(path!=null){
					path.addMapPath(new MapPath(MapPath.Direction.RIGHT));
				}else{
					path = new MapPath(MapPath.Direction.RIGHT);
				}
				//Move right
				nav.turnBy(-90);
			}
		}
	}
}
