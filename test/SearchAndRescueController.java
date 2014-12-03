public class SearchAndRescueController {
    private Map map;
    private MapNode current;

    public SearchAndRescueController(Map m) {

        map = m;
    }

    public int run() {

        int blocks = 0;

        MapNode dest;
        int count = 0;

        while (blocks < 1) { //change to timer things
            //MOVE TO PICKUP
            blocks++;
            dest = map.getCollectionNode();
            MapPath path = map.getPathFromNodeToNode(current, dest);
            while (path != null) {
                current = current.getNodeFromPath(new MapPath(path.getDirection()));
                if (path.getDirection() == MapPath.Direction.FRONT) {
                    count++;
                }
                path = path.getNextMapPath();
            }
            System.out.println("SAR took " + count + " moves");
        }
        return count;
    }

    public void setCurrent(MapNode c) {
        current = c;
    }
}
