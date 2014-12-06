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
