import java.util.ArrayList;

public class LocalizationController {
    //Has a copy of the nav, has a map, and has two sensors
    private Map map;
    private static final int MAX_TILES = 1;
    private static final boolean DUEL_SENSOR = false;
    private SearchAndRescueController search;

    /**
     * Constructs a new localization controller with every property
     *
     * @param m The map for it to use (in our case, will be null)
     */
    public LocalizationController(Map m, SearchAndRescueController sar) {
        map = m;
        search = sar;
    }

    /**
     * Computes the standard deviation of a given integer array
     *
     * @param arr The array of integers (any size)
     * @return The standard deviation as a float
     */
    public float stdDev(int[] arr) {
        //Loop through the array and sum the values divided by the length to find the average
        float avg = 0;
        for (int anArr : arr) {
            avg += (float) anArr / arr.length;
        }
        //Loop through the array and add to a new sum (each element-avg)^2/length
        float newAvg = 0;
        for (int anArr : arr) {
            newAvg += Math.pow(anArr - avg, 2) / arr.length;
        }
        //return the square root of that sum (the standard deviation of the original array)
        return (float) Math.sqrt(newAvg);
    }

    /**
     * Method called to start the Localization Controller Handles all the logic of the controller
     *
     * @throws InterruptedException
     */
    public int run(int node) throws InterruptedException {
        float theta = (float) ((node % 4) * (Math.PI / 2));
        float x = 15 + 30 * (node / 4) % (Map.getLength());
        float y = 15 + 30 * (node / 4) / (Map.getLength());
        System.out.println("\nRunning with " + node + " at X: " + x + " Y: " + y + " T: " + theta);
        //Setting up the display

        //path represents the current path that the robot has actually traveled
        MapPath path = null;
        //nodes represents all of the nodes that are considered to be valid starting options
        ArrayList<MapNode> nodes = map.getRemaningNodes();

        //We continue to run this algorithm until there is one (or zero) nodes left

        int moves = 0;

        MapNode current = map.getNodeAtIndex(node);

        while (nodes.size() > 1) {

            moves++;
            int rearTiles = 0;
            //Get the distance data and use it to find out how many empty tiles surround the robot

            int frontTiles = 0;

            //	System.out.println("X: "+ x + " Y: "+ y+" T: "+ theta);
            MapNode c = current.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
            while (c != null) {
                frontTiles++;
                c = c.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
            }
            c = current.getNodeFromPath(new MapPath(MapPath.Direction.LEFT, new MapPath(MapPath.Direction.LEFT)));
            c = c.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
            while (c != null) {
                rearTiles++;
                c = c.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
            }
            //cutoff at max number of tiles (due to sensor accuracy)
            if (frontTiles > MAX_TILES) {
                frontTiles = MAX_TILES;
            }
            if (DUEL_SENSOR) {
                if (rearTiles > MAX_TILES) {
                    rearTiles = MAX_TILES;
                }
            }

            //Loop through all of the nodes that are still considered valid, so we can check if they're still valid when compared to this new sensor data
            for (MapNode m : nodes) {
                //if m represents the node we started from, n represents the place that the robot would be if it followed path from m
                MapNode n = m.getNodeFromPath(path);
                MapNode r = n.getNodeFromPath(new MapPath(MapPath.Direction.LEFT, new MapPath(MapPath.Direction.LEFT))); //This one is facing the rear
                //scan forward one by one and make sure the sensor data matches with the map data, otherwise remove m as a valid option
                for (int i = 0; i <= frontTiles; i++) {
                    if (i == MAX_TILES) { //If we are at the max distance, we don't actually care about this tile (due to sensor inaccuracy)
                        break;
                    } else if (i == frontTiles) {//If we are at the end of what the sensor picked up, that means there's supposed to be a tile here
                        n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                        if (n != null) {//If there isn't a tile here, then remove m
                            m.setIsValidStart(false);
                            break;
                        }
                    } else {//Else, there's not supposed to be a tile here
                        n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                        if (n == null) {//If there is a tile here, remove m
                            m.setIsValidStart(false);
                            break;
                        }
                    }
                }

                if (DUEL_SENSOR) {
                    //Repeat the same logic for the rear sensor
                    for (int i = 0; i <= rearTiles; i++) {
                        if (i == MAX_TILES) { //If we are at the max distance, we don't actually care about this tile (due to sensor inaccuracy)
                            break;
                        } else if (i == rearTiles) {//If we are at the end of what the sensor picked up, that means there's supposed to be a tile here
                            r = r.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                            if (r != null) {//If there isn't a tile here, then remove m
                                m.setIsValidStart(false);
                                break;
                            }
                        } else {//Else, there's not supposed to be a tile here
                            r = r.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                            if (r == null) {//If there is a tile here, remove m
                                m.setIsValidStart(false);
                                break;
                            }
                        }
                    }
                }
            }

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

            //for each valid starting node left, we check the distance to the next tile from each direction
            for (MapNode m : nodes) {

                MapNode n = m.getNodeFromPath(path);
                n = n.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
                n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                //We loop and move the node forward until we hit a wall, then we increment the corresponding value in tileCount
                int i = 0;
                while (n != null) {
                    i++;
                    n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT)); //Using l, facing left
                }
                //Cutoff at the max
                if (i > MAX_TILES) {
                    i = MAX_TILES;
                }
                //Increment the right position in the array
                tileCount[0][i]++;

                //reset and repeat for facing right
                n = m.getNodeFromPath(path);
                n = n.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
                n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                i = 0;
                while (n != null) {
                    i++;
                    n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Using r, facing right
                }
                if (i > MAX_TILES) {
                    i = MAX_TILES;
                }
                tileCount[1][i]++;

                //reset and repeat facing front
                n = m.getNodeFromPath(path);
                n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
                i = 0;
                i = 0;
                while (n != null) {
                    i++;
                    n = n.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));//Using f, facing front
                }
                if (i > MAX_TILES) {
                    i = MAX_TILES;
                }
                tileCount[2][i]++;
            }
            //	System.out.println(tileCount[0][0] + " " + tileCount[0][1] + " " + tileCount[0][2] + " " + tileCount[0][3]);
            //	System.out.println(tileCount[1][0] + " " + tileCount[1][1] + " " + tileCount[1][2] + " " + tileCount[1][3]);
            //	System.out.println(tileCount[2][0] + " " + tileCount[2][1] + " " + tileCount[2][2] + " " + tileCount[2][3]);
            //Now we compute the standard deviation of the arrays and use the lowest one for the move
            float stdL = stdDev(tileCount[0]);
            float stdR = stdDev(tileCount[1]);
            float stdF = stdDev(tileCount[2]);

            //System.out.println(stdL + " " + stdR + " " + stdF);
            if (stdL - stdR < 0.01 && stdL - stdF < 0.01) {
                for (int i = MAX_TILES; i >= 0; i--) {
                    boolean br = false;
                    if (tileCount[0][i] != 0) {
                        stdL = -0.5f;
                        br = true;
                    }
                    if (tileCount[1][i] != 0) {
                        stdR = -0.25f;
                        br = true;
                    }
                    if (tileCount[2][i] != 0) {
                        stdF = -1;
                        br = true;
                    }
                    if (br) {
                        break;
                    }
                }
            }
            if (stdF <= stdL && stdF <= stdR && frontTiles != 0) { //Make sure there isn't a tile in front of us already if we want to move fowrard
                //Add a forward node to the path
                try {
                    path.addMapPath(new MapPath(MapPath.Direction.FRONT));
                } catch (Exception e) {
                    path = new MapPath(MapPath.Direction.FRONT);
                }
                //Move forward
                //	System.out.println("FORWARD");
                current = current.getNodeFromPath(new MapPath(MapPath.Direction.FRONT));
            } else if (stdL < stdR) { //If we want to move left, left has to have a lower standard deviation
                //Add a left node to the path
                try {
                    path.addMapPath(new MapPath(MapPath.Direction.LEFT));
                } catch (Exception e) {
                    path = new MapPath(MapPath.Direction.LEFT);
                }
                //Move left
                //	System.out.println("LEFT");
                current = current.getNodeFromPath(new MapPath(MapPath.Direction.LEFT));
            } else {//else we want to move right
                //add a right node to the path
                try {
                    path.addMapPath(new MapPath(MapPath.Direction.RIGHT));
                } catch (Exception e) {
                    path = new MapPath(MapPath.Direction.RIGHT);
                }
                //	System.out.println("RIGHT");
                //Move right
                current = current.getNodeFromPath(new MapPath(MapPath.Direction.RIGHT));
            }
        }

        //End of algorithm, update position

        int num = current.getNum();//Do math to find out the position
        theta = (float) ((num % 4) * (Math.PI / 2));
        x = 15 + 30 * (num / 4) % (Map.getLength());
        y = 15 + 30 * (num / 4) / (Map.getLength());

        System.out.println("ACTUAL X: " + x + " Y: " + y + " T: " + theta);

        if (!nodes.isEmpty()) {
            num = nodes.get(0).getNodeFromPath(path).getNum();
            theta = (float) ((num % 4) * (Math.PI / 2));
            x = 15 + 30 * (num / 4) % (Map.getLength());
            y = 15 + 30 * (num / 4) / (Map.getLength());

            System.out.println("CALC'D X: " + x + " Y: " + y + " T: " + theta);
        }
        System.out.println("LOC took " + moves + " moves");

        if (!nodes.isEmpty() && current == nodes.get(0).getNodeFromPath(path)) {
            System.out.println("LOCALIZATION SUCCESS!");
        } else {
            System.out.println("LOCALIZATION FAILURE...");
        }

        //Update the display and the odometer
        search.setCurrent(current);

        return moves;
    }
}
