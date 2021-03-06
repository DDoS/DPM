/*
This file is part of DPM, licensed under the MIT License (MIT).

Copyright (c) 2014 Team 7

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
import java.util.ArrayList;

/**
 * The controller used to localized the robot inside the map. Once the run method returns, the odometer will have been updated to correct position of the robot. The robot is very likely to have moved
 * after the method returns.
 */
public class LocalizationController {
    private static final int MAX_TILES = 3;
    private static final boolean DEBUG_MODE = false;
    //Has a copy of the nav, has a map, and has two sensors
    private Navigation nav;
    private Map map;
    private FilteredUltrasonicSensor front_us;
    private SearchAndRescueController searchAndRescue;
    private static final int SPEED = 500;

    /**
     * Constructs a new localization controller with every property
     *
     * @param n The nav for the controller to use
     * @param m The map for it to use (in our case, will be null)
     * @param s1 The front ultrasonic sensor
     */
    public LocalizationController(Navigation n, Map m, FilteredUltrasonicSensor s1, SearchAndRescueController sarC) {
        nav = n;
        map = m;
        front_us = s1;
        searchAndRescue = sarC;
    }

    /**
     * Method called to start the Localization Controller Handles all the logic of the controller
     */
    public boolean run() {
        //Setting up the display
        Display.clear();
        Display.reserve("Status", "SX", "SY", "ST", "Moves");
        Display.update("Status", "Init");

        //path represents the current path that the robot has actually traveled
        Path path = null;
        //nodes represents all of the nodes that are considered to be valid starting options
        ArrayList<Node> nodes = map.getRemaningNodes();

        float currTheta = Pi.ONE_HALF; //Keep track of the current heading so our turning before localization is accurate

        int moves = 0; //Keep track of the number of moves taken for testing purposes

        //We continue to run this algorithm until there is one (or zero) nodes left
        while (nodes.size() > 1) {
            //Update moves
            Display.update("Moves", "" + moves);
            moves++;

            Display.update("Status", "Ping");

            //Get the distance data and use it to find out how many empty tiles surround the robot
            int frontTiles = 0;

            //Look at 5 slightly different angles, and find the max distance (to avoid the weird problems with the ultrasonic)
            for (int i = -2; i <= 2; i++) {
                nav.turnTo(currTheta + (float) i / 60);
                nav.waitUntilDone();
                int fDist = front_us.getDistanceData();
                frontTiles = Math.max((int) (fDist / Tile.ONE), frontTiles);
            }
            nav.turnTo(currTheta); //make sure to rotate back to the normal angle
            nav.waitUntilDone();

            Display.update("Status", "Calculate");

            //cutoff at max number of tiles (due to sensor accuracy)
            frontTiles = Math.min(Math.max(frontTiles, 0), MAX_TILES);

            //TEST CODE TO OUTPUT TILES SEEN
            if (DEBUG_MODE) {
                Note.play(1 + frontTiles, 250);
            }

            //Remove the nodes that don't fit with our sensor data
            this.updateMapWithSensorData(frontTiles, nodes, path);

            //Now that we have removed some nodes, we update nodes
            nodes = map.getRemaningNodes();

            //Now we will look for the next move.
            //This array stores values that will be used to determine which move to take
            //Eg. If there is a tile 4 blocks in front of a given space, it will increment tileCount[2][4]
            //If there is a tile 0 blocks to the right of a given space, it will increment tileCount[1][0]
            //Then we take the standard deviation of these 3 arrays, and the lowest one is the best move
            int[][] tileCount = {
                    {0, 0, 0, 0, 0, 0, 0, 0, 0},//left
                    {0, 0, 0, 0, 0, 0, 0, 0, 0},//right
                    {0, 0, 0, 0, 0, 0, 0, 0, 0}//front
            };

            this.getDistancesToTiles(tileCount, nodes, path);

            //Now we compute the standard deviation of the arrays and use the lowest one for the move
            float stdL = stdDev(tileCount[0]);
            float stdR = stdDev(tileCount[1]);
            float stdF = stdDev(tileCount[2]);

            //FIX STD DEVS
            //If the std devs are all the same, we want to go to the move that has the obstacle which is farthest away (with bias towards moving front, then left, then right, to avoid getting stuck in a loop)
            if (stdL - stdR < 0.01 && stdL - stdF < 0.01) {
                //search through the lists backwards until we find a spot that isn't 0, then break. (can hit multiple spots at once, then the comparisons below take care of the rest)
                for (int i = MAX_TILES; i >= 0; i--) {

                    boolean br = false; //If we hit a spot that isn't 0, we break from this loop, but only after we've checked all 3 directions (hence using a boolean here)

                    if (tileCount[0][i] != 0) { //If we see a non-zero in the left column
                        stdL = -0.5f; //Bias towards moving left
                        br = true;
                    }

                    if (tileCount[1][i] != 0) { //If we see a non-zero in the right column
                        stdR = -0.25f; //Less bias towards moving right
                        br = true;
                    }

                    if (tileCount[2][i] != 0) { //If we see a non-zero in the front column
                        stdF = -1;    //Greatest bias towards moving forward
                        br = true;
                    }

                    if (br) { //If we've seen a non-zero anywhere, stop doing this
                        break;
                    }
                }
            }

            //Now that we know the standard devs aren't equal, we figure out which move to do

            nodes = map.getRemaningNodes(); //Find the remaining nodes (should only have 1 node, in spot 0)

            if (nodes.size() > 1) { //we only need to do another move if we're not done with the algorithm yet

                //If front is less than or equal to both right and left, go front
                if (stdF <= stdL && stdF <= stdR && frontTiles != 0) { //Make sure there isn't a tile in front of us already if we want to move forward
                    //The <= comparisons here make the forward move a little more common

                    //Add a forward node to the path
                    if (path != null) {
                        path.addPath(new Path(Path.Direction.FRONT));
                    } else {
                        path = new Path(Path.Direction.FRONT);
                    }

                    Display.update("Status", "Move");

                    //Move forward
                    nav.forward(Tile.ONE, SPEED);
                    nav.waitUntilDone();//Wait for the navigation to finish

                    //If the left is less than the right, go left
                } else if (stdL < stdR) { //If we want to move left, left has to have a lower standard deviation

                    //Add a left node to the path
                    if (path != null) {
                        path.addPath(new Path(Path.Direction.LEFT));
                    } else {
                        path = new Path(Path.Direction.LEFT);
                    }

                    Display.update("Status", "Move");

                    //Move left
                    currTheta += Pi.ONE_HALF;
                    nav.turnTo(currTheta, SPEED);
                    nav.waitUntilDone();//Wait for the navigation to finish
                } else {//else we want to move right

                    //add a right node to the path
                    if (path != null) {
                        path.addPath(new Path(Path.Direction.RIGHT));
                    } else {
                        path = new Path(Path.Direction.RIGHT);
                    }

                    Display.update("Status", "Move");

                    //Move right
                    currTheta -= Pi.ONE_HALF;
                    nav.turnTo(currTheta, SPEED);
                    nav.waitUntilDone();//Wait for the navigation to finish
                }
            }
        }

        //End of algorithm, update position
        Display.update("Status", "Final");

        Node current;//Current spot that the robot is in

        if (nodes.size() < 1) {//If the algorithm failed, return failure
            return false;
        } else {
            current = nodes.get(0).getNodeFromPath(path);//Else use what the algorithm found
            //nodes.get(0) returns the only node left in the map (which is the starting node of the robot). getNodeFromPath(path) moves from the start node to the current node
        }

        float x = nodes.get(0).getX() - Tile.ONE;
        float y = nodes.get(0).getY() - Tile.ONE;
        float theta = nodes.get(0).getTheta();

        //Update the display and the odometer
        Display.update("SX", "" + x);
        Display.update("SY", "" + y);
        Display.update("ST", "" + theta);

        x = current.getX();
        y = current.getY();
        theta = current.getTheta();

        nav.getOdometer().setPosition(x, y, theta);

        Note.play(4); //Play a sound to signal that we've localized

        //Set the SearchAndRescueController so it knows where the robot is
        searchAndRescue.setCurrent(current);

        return true;
    }

    /**
     * Updates the Map so that starting nodes which no longer fir the path with the new sensor data are removed The map isn't returned, but updated by reference
     *
     * @param frontTiles An int that represents the number of tiles the robot sees in front of it
     * @param nodes An ArrayList of Nodes that contains all the nodes that are still considered valid (so we can update them)
     * @param path A Path that represents the path that the robot has traveled while sensing
     */
    private void updateMapWithSensorData(int frontTiles, ArrayList<Node> nodes, Path path) {

        //Loop through all of the nodes that are still considered valid, so we can check if they're still valid when compared to this new sensor data
        for (Node m : nodes) {
            //if m represents the node we started from, n represents the place that the robot would be if it followed path from m
            Node assumedPos = m.getNodeFromPath(path);

            //scan forward one by one and make sure the sensor data matches with the map data, otherwise remove m as a valid option
            for (int i = 0; i <= frontTiles; i++) {

                if (i == MAX_TILES) { //If we are at the max distance, we don't actually care about this tile (due to sensor inaccuracy)

                    break;
                } else if (i == frontTiles) {//If we are at the end of what the sensor picked up, that means there's supposed to be a tile here

                    assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));
                    if (assumedPos != null) {//If there isn't a tile here, then remove m
                        m.setIsValidStart(false);
                        break;
                    }
                } else {//Else, there's not supposed to be a tile here

                    assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));
                    if (assumedPos == null) {//If there is a tile here, remove m
                        m.setIsValidStart(false);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Update the tileCount array by reference with the number of nodes at each distance, from each of the 3 directions
     *
     * @param tileCount The int 2D 3-by-X array which represents a block at each distance. The number in each spot in the array represents how many blocks there are in total at that distance in that
     * direction
     * @param nodes An ArrayList of Nodes which represents the nodes that are left as starting nodes (we need to check the distances from each of these nodes)
     * @param path A Path that represents the path the robot has traveled
     */
    private void getDistancesToTiles(int[][] tileCount, ArrayList<Node> nodes, Path path) {

        //for each valid starting node left, we check the distance to the next tile from each direction
        for (Node m : nodes) {

            //----CHECKING LEFT-----
            Node assumedPos = m.getNodeFromPath(path);
            assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.LEFT)); //Move the assumed pos to the left (to see how many blocks turning left eliminates)
            assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));//Must add one node to the front to avoid off-by-one error
            //We loop and move the node forward until we hit a wall, then we increment the corresponding value in tileCount
            int i = 0; //i will represent how many blocks we see from this location

            while (assumedPos != null) {
                i++;
                assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT)); //Move the assumedPos forward until we hit a wall
            }

            //Cutoff at the max
            if (i > MAX_TILES) {
                i = MAX_TILES;
            }

            //Increment the right position in the array
            tileCount[0][i]++; //[0] represents a move from left. [i] represents a tile at i distance away. the value of [0][i] is the number of tiles in total that distance away from left
            //-----CHECKED LEFT------

            //-----CHECKING RIGHT----
            //reset and repeat for facing right
            assumedPos = m.getNodeFromPath(path);
            assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.RIGHT));
            assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));//Must add one node to the front to avoid off-by-one error
            i = 0;

            while (assumedPos != null) {
                i++;
                assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));//Using r, facing right
            }

            if (i > MAX_TILES) {
                i = MAX_TILES;
            }

            tileCount[1][i]++;
            //-----CHECKED RIGHT-----

            //-----CHECKING FRONT----
            //reset and repeat facing front
            assumedPos = m.getNodeFromPath(path);
            assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));
            i = 0;

            while (assumedPos != null) {
                i++;
                assumedPos = assumedPos.getNodeFromPath(new Path(Path.Direction.FRONT));//Using f, facing front
            }

            if (i > MAX_TILES) {
                i = MAX_TILES;
            }

            tileCount[2][i]++;
            //------CHECKED FRONT----
        }
        //We have finished populating our distance array with the number of blocks at each distance
    }

    /**
     * Computes the standard deviation of a given integer array.
     *
     * @param arr The array of integers (any size)
     * @return The standard deviation as a float
     */
    private static float stdDev(int[] arr) {
        //Loop through the array and sum the values divided by the length to find the average
        float avg = 0;
        for (int anArr1 : arr) {
            avg += (float) anArr1 / arr.length;
        }
        //Loop through the array and add to a new sum (each element-avg)^2/length
        float newAvg = 0;
        for (int anArr : arr) {
            newAvg += Math.pow(anArr - avg, 2) / arr.length;
        }
        //return the square root of that sum (the standard deviation of the original array)
        return (float) Math.sqrt(newAvg);
    }
}
