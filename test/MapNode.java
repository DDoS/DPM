import java.util.ArrayList;

/**
 * MapNode class used to represent a single position of the robot
 *
 * @author Jonah
 */
public class MapNode {
    //Each node contains pointers to the nodes to the left, right, and front
    private MapNode left;
    private MapNode right;
    private MapNode front;
    public int num;
    private boolean isValidStart;//Whether or not to consider this node as a valid starting node
    //These two used when searching for the shortest path
    private boolean visited;
    private MapNode parent;

    /**
     * Constructs a new empty MapNode
     */
    public MapNode() {
        isValidStart = true;
        visited = false;
        left = null;
        right = null;
        front = null;
    }

    /**
     * Returns the MapNode at the end of the MapPath as followed if starting from this MapPath
     *
     * @param path The MapPath to follow
     * @return The MapNode at the end of the path
     */
    public MapNode getNodeFromPath(MapPath path) {
        if (path != null) {//If there is no path, skip this and return the current node (below)
            if (path.getDirection() == MapPath.Direction.LEFT) {//If the path is going left
                if (this.left != null) {//Make sure the left node isn't null
                    return this.left.getNodeFromPath(path.getNextMapPath());//Then return whatever the left node returns
                }
                return null;//There is some error, return null
            } else if (path.getDirection() == MapPath.Direction.RIGHT) {//If the path is right, do the same
                if (this.right != null) {
                    return this.right.getNodeFromPath(path.getNextMapPath());
                }
                return null;
            } else if (path.getDirection() == MapPath.Direction.FRONT) {//If the path is front, do the same
                if (this.front != null) {
                    return this.front.getNodeFromPath(path.getNextMapPath());
                }
                return null;
            }
        }
        return this;//return the current node
    }

    /**
     * Returns a MapPath representing the shortest path from this node to a given MapNode
     *
     * @param m MapNode to find shortest path to
     * @return MapPath representing the shortest path
     */
    public MapPath getShortestPathTo(MapNode m) {
        //Use a queue to run a BFS through the map, but keep track of the path as well
        ArrayList<MapNode> queue = new ArrayList<MapNode>();
        this.parent = null;
        this.visited = true;
        queue.add(this);//Add the current node as already visited, with a null parent.
        MapNode currNode = this;
        //Keep running the algorithm until the queue is empty (failure) or the current node is the one we are looking for (success)
        while (!queue.isEmpty() && currNode != m) {
            //Pop the next node out of the queue, and add all of its children to the queue if they havent been visited
            currNode = (MapNode) queue.remove(0);

            MapNode nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));//Check the left child to see if it's been visited
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);//Set the node's parent so we can trace the path back later
                nextNode.setVisited(true);//Set it to visited
                queue.add(nextNode);//Add to the queue
            }

            nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));//Repeat the same process for the right child
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);
                nextNode.setVisited(true);
                queue.add(nextNode);
            }

            nextNode = currNode.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Repeat the same process for the front child
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);
                nextNode.setVisited(true);
                queue.add(nextNode);
            }
        }

        //Now that we've found the node (or not), we will return the result of the trace through the path
        MapPath result = null;
        if (currNode == m) {//If we haven't found the node, we will end up returning null
            //Keep looping until we're at the starting node (which has a parent of null
            while (currNode.getParent() != null) {
                if (currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.LEFT)) == currNode) {//If the parent's left child is this node, we know we must go left at the point in the path
                    result = new MapPath(MapPath.Direction.LEFT, result);//add a left node to the beginning of the result path
                } else if (currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.RIGHT)) == currNode) {//Check the parent's right child
                    result = new MapPath(MapPath.Direction.RIGHT, result);
                } else if (currNode.getParent().getNodeFromPath(new MapPath(MapPath.Direction.FRONT)) == currNode) {//Check the parent's front child
                    result = new MapPath(MapPath.Direction.FRONT, result);
                }

                currNode = currNode.getParent();//Move on to the parent before iterating again
            }
        }
        return result; //return the path (or null for lack of path)
    }

    /**
     * Sets one of the three children of the MapNode
     *
     * @param d Direction to specify which child to set
     * @param m MapNode to set the child to
     */
    public void setChild(MapPath.Direction d, MapNode m) {
        //Based on direction, sets either left, right or front to the MapNode that was passed in
        if (d == MapPath.Direction.LEFT) {
            left = m;
        } else if (d == MapPath.Direction.RIGHT) {
            right = m;
        } else if (d == MapPath.Direction.FRONT) {
            front = m;
        }
    }

    /**
     * Getter for the isValidStart boolean
     *
     * @return isValidStart
     */
    public boolean getIsValidStart() {
        return isValidStart;
    }

    /**
     * Setter for the isValidStart boolean
     *
     * @param b Value to set isValidStart
     */
    public void setIsValidStart(boolean b) {
        isValidStart = b;
    }

    /**
     * Getter for the parent MapNode
     *
     * @return parent
     */
    public MapNode getParent() {
        return parent;
    }

    /**
     * Setter for the parent MapNode
     *
     * @param p Value to set parent
     */
    public void setParent(MapNode p) {
        parent = p;
    }

    /**
     * Getter for the visited boolean
     *
     * @return visited
     */
    public boolean getVisited() {
        return visited;
    }

    /**
     * Setter for the visited boolean
     *
     * @param b Value to set visited
     */
    public void setVisited(boolean b) {
        visited = b;
    }

    /**
     * Getter for the num int
     *
     * @return num
     */
    public int getNum() {
        return num;
    }

    /**
     * Setter for the num int
     *
     * @param n Value to set num
     */
    public void setNum(int n) {
        num = n;
    }
}
