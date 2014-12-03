import java.util.Queue;

/**
 * Node class used to represent a single position of the robot
 */
public class Node {
    //Each node contains pointers to the nodes to the left, right, and front
    private Node left;
    private Node right;
    private Node front;
    private int num;
    private boolean isValidStart;//Whether or not to consider this node as a valid starting node
    //These two used when searching for the shortest path
    private boolean visited;
    private Node parent;

    /**
     * Constructs a new empty Node
     */
    public Node() {
        isValidStart = true;
        visited = false;
        left = null;
        right = null;
        front = null;
    }

    /**
     * Returns the Node at the end of the Path as followed if starting from this Path
     *
     * @param path The Path to follow
     * @return The Node at the end of the path
     */
    public Node getNodeFromPath(Path path) {
        if (path != null) {//If there is no path, skip this and return the current node (below)
            if (path.getDirection() == Path.Direction.LEFT) {//If the path is going left
                if (this.left != null) {//Make sure the left node isn't null
                    return this.left.getNodeFromPath(path.getNextPath());//Then return whatever the left node returns
                }
                return null;//There is some error, return null
            } else if (path.getDirection() == Path.Direction.RIGHT) {//If the path is right, do the same
                if (this.right != null) {
                    return this.right.getNodeFromPath(path.getNextPath());
                }
                return null;
            } else if (path.getDirection() == Path.Direction.FRONT) {//If the path is front, do the same
                if (this.front != null) {
                    return this.front.getNodeFromPath(path.getNextPath());
                }
                return null;
            }
        }
        return this;//return the current node
    }

    /**
     * Returns a Path representing the shortest path from this node to a given Node
     *
     * @param m Node to find shortest path to
     * @return Path representing the shortest path
     */
    public Path getShortestPathTo(Node m) {
        //Use a queue to run a BFS through the map, but keep track of the path as well
        Queue<Node> queue = new Queue<Node>();
        this.parent = null;
        this.visited = true;
        queue.push(this);//Add the current node as already visited, with a null parent.
        Node currNode = this;
        //Keep running the algorithm until the queue is empty (failure) or the current node is the one we are looking for (success)
        while (!queue.isEmpty() && currNode != m) {
            //Pop the next node out of the queue, and add all of its children to the queue if they havent been visited
            currNode = (Node) queue.pop();

            Node nextNode = currNode.getNodeFromPath(new Path(Path.Direction.LEFT));//Check the left child to see if it's been visited
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);//Set the node's parent so we can trace the path back later
                nextNode.setVisited(true);//Set it to visited
                queue.push(nextNode);//Add to the queue
            }

            nextNode = currNode.getNodeFromPath(new Path(Path.Direction.RIGHT));//Repeat the same process for the right child
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);
                nextNode.setVisited(true);
                queue.push(nextNode);
            }

            nextNode = currNode.getNodeFromPath(new Path(Path.Direction.FRONT));//Repeat the same process for the front child
            if (nextNode != null && !nextNode.getVisited()) {
                nextNode.setParent(currNode);
                nextNode.setVisited(true);
                queue.push(nextNode);
            }
        }

        //Now that we've found the node (or not), we will return the result of the trace through the path
        Path result = null;
        if (currNode == m) {//If we haven't found the node, we will end up returning null
            //Keep looping until we're at the starting node (which has a parent of null
            while (currNode.getParent() != null) {

                if (currNode.getParent().getNodeFromPath(new Path(Path.Direction.LEFT)) == currNode) {//If the parent's left child is this node, we know we must go left at the point in the path
                    result = new Path(Path.Direction.LEFT, result);//add a left node to the beginning of the result path
                } else if (currNode.getParent().getNodeFromPath(new Path(Path.Direction.RIGHT)) == currNode) {//Check the parent's right child
                    result = new Path(Path.Direction.RIGHT, result);
                } else if (currNode.getParent().getNodeFromPath(new Path(Path.Direction.FRONT)) == currNode) {//Check the parent's front child
                    result = new Path(Path.Direction.FRONT, result);
                }

                currNode = currNode.getParent();//Move on to the parent before iterating again
            }
        }
        return result; //return the path (or null for lack of path)
    }

    /**
     * Sets one of the three children of the Node
     *
     * @param d Direction to specify which child to set
     * @param m Node to set the child to
     */
    public void setChild(Path.Direction d, Node m) {
        //Based on direction, sets either left, right or front to the Node that was passed in
        if (d == Path.Direction.LEFT) {
            left = m;
        } else if (d == Path.Direction.RIGHT) {
            right = m;
        } else if (d == Path.Direction.FRONT) {
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
     * Getter for the parent Node
     *
     * @return parent
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Setter for the parent Node
     *
     * @param p Value to set parent
     */
    public void setParent(Node p) {
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

    /**
     * Get the X position of the center of this tile
     *
     * @return float value of X pos
     */
    public float getX() {
        return Tile.HALF + Tile.ONE * (num / 4) % (Map.getLength());
    }

    /**
     * Get the Y position of the center of this tile
     *
     * @return float value of Y pos
     */
    public float getY() {
        return (Map.getLength() - 1) * Tile.ONE + Tile.HALF - Tile.ONE * (num / 4) / (Map.getLength());
    }

    /**
     * Get the theta value for the orientation of this node
     *
     * @return float value of theta
     */
    public float getTheta() {
        return (num % 4) * Pi.ONE_HALF;
    }
}
