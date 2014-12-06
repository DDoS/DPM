/*
This file is part of DPM, licensed under the MIT License (MIT).

Copyright (c) 2014 Team 20

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
import lejos.nxt.*;

/*
  A class that runs on the calling thread to orient the robot
  in a predefined "maze" using either a deterministic or stochastic
  method. Once orientation has succeeded, the moveTo method can be
  used to navigate to any tile and face a cardinal direction.

  Note: the implementation of this class makes use of bit manipulation
  to simplify operations and improve performance. It also takes advantage
  of the simplified 4 cardinal directions to use enums with indexes
  (0 to 3, from North, CCW) for rotations, to again, simplify the math
  and improve performance. Flat arrays are also used for simpler iteration.
  (The ARM7TDMI CPU used by NXT has no division instructions or FPU!)
 */
public class Orienteer {
	// Math and terrain constants
	private static final double HALF_PI = Math.PI / 2;
	private static final double TILE_SPACING = 29.5;
	private static final double HALF_TILE_SPACING = TILE_SPACING / 2;
	// Minimal distance for a way to be considered as blocked
	private static final int WALL_THRESHOLD = (int) (TILE_SPACING * 1.1);
	// How many far pings (255) in a row before 255 is reported (for filtering)
	private static final int FAR_PING_THRESHOLD = 3;
	// How many pings to perform for one observation (multiple for filtering)
	private static final int OBSERVATION_PING_COUNT = FAR_PING_THRESHOLD * 2;
	// The maze: false is no obstacle, true is an obstacle
	private static final boolean[] FIELD = {
		false, true, false, false,
		false, false, false, false,
		false, false, true, true,
		true, false, false, false
	};
	// Size of the field in tiles
	private static final int FIELD_SIZE = 4;
	// Mask of the bits needed to represent a coordinate in the field
	private static final int FIELD_MASK = 3;
	// Shift equivalent to multipling by the field size
	private static final int FIELD_SHIFT = 2;
	// Odometer and navigation for location and movement
	private final Odometer odo;
	private final Navigation navig;
	// Ultrasonic sensor for detecting obstacles
	private final UltrasonicSensor us;
	// Type of orientation to use (deterministic or stochastic)
	private final OrienteerType type;
	// A flat grid array of possible starting positions (represented as bits)
	private final int[] grid = new int[FIELD.length];
	// How many far pings seen in a row (filtering)
	private int farPingCount = 0;
	// Last non far ping distance (filtering)
	private int lastValidDistance = 255;

	// Constructs a new orienteer from the odometer, navigation, ultrasonic sensor and type
	public Orienteer(Odometer odo, Navigation navig, UltrasonicSensor us, OrienteerType type) {
		this.odo = odo;
		this.navig = navig;
		this.us = us;
		this.type = type;
	}

	// Runs the orientating: use the calling thread to move the robot around until
	// position and direction in the maze can be determined. The odometer will be
	// update to the found location
	public void doOrienteering() {
		// Initialize the grid of possible locations
		prepareGrid();
		// Assume current heading to be north
		Direction direction = Direction.NORTH;
		// Assume starting position to be (0, 0)
		int xOffset = 0, yOffset = 0;
		// Number of observations done so far
		int observationCount = 0;
		// How many possible starting points are remaining
		int remaining = grid.length * 4;
		// perform the orientation
		do {
			// Check if e have an obstacle in front of us
			if (isBlocked()) {
				// If so, observe to eliminate possibilities
				observe(direction, xOffset, yOffset, true);
				observationCount++;
				// Turn 90° (ccw) and wait for completion
				navig.turnBy(HALF_PI);
				navig.waitUntilDone();
				// Update direction to new heading
				direction = direction.rotateCCW(1);
			} else if (type == OrienteerType.DETERMINISTIC || Math.random() >= 0.5) {
				// If not blocked, and either deterministic or 50% chance met (stochastic)
				// observe and eliminate possibilities
				observe(direction, xOffset, yOffset, false);
				observationCount++;
				// compute the offset to the next tile by rotating by the heading
				double[] nextTile = rotateCCW(0, TILE_SPACING, direction.index);
				// travel to it
				navig.travelBy(nextTile[0], nextTile[1]);
				navig.waitUntilDone();
				// Update offset to new values
				xOffset += direction.xOffset;
				yOffset += direction.yOffset;
			} else {
				// If not blocked and stochastic and 50% chance wasn't met
				// observe and eliminate possibilities
				observe(direction, xOffset, yOffset, false);
				observationCount++;
				// Turn 90° (ccw) and wait for completion
				navig.turnBy(HALF_PI);
				navig.waitUntilDone();
				// Update direction to new heading
				direction = direction.rotateCCW(1);
			}
			// Get the count of remaining possibilities
			remaining = countUncleared();
			// Update the LCD info
			LCD.clear();
			LCD.drawString("o: " + observationCount, 0, 0);
			LCD.drawString("r: " + remaining, 0, 1);
			LCD.drawString("x: " + xOffset, 0, 2);
			LCD.drawString("y: " + yOffset, 0, 3);
			LCD.drawString("d: " + direction, 0, 4);
			// Repeat until we only have one possibility left
		} while (remaining > 1);
		// All but one is left, so get the position and direction
		int startPosition = getFirstUnclearedIndex();
		Direction startDirection = getFirstUnclearedDirection(startPosition);
		// Rotate the offset to match te absolute coordinate system
		int[] rotatedOffset = rotateCCW(xOffset, yOffset, startDirection.index - Direction.NORTH.index);
		// Add current location information to starting point to get absolute
		int x = rotatedOffset[0] + getX(startPosition);
		int y = rotatedOffset[1] + getY(startPosition);
		direction = direction.rotateCCW(startDirection.index);
		// Update the LCD with found location
		LCD.drawString("sx: " + getX(startPosition), 0, 5);
		LCD.drawString("sy: " + getY(startPosition), 0, 6);
		LCD.drawString("sd: " + startDirection, 0, 7);
		// Update the odo with information (converting to cm and radian angle)
		odo.setPosition(
			x * TILE_SPACING + HALF_TILE_SPACING,
			y * TILE_SPACING + HALF_TILE_SPACING,
			direction.angle
		);
	}

	// Fills all grid cells with all possible directions, but leaves obstacle cells empty (can't state there)
	private void prepareGrid() {
		// No bits set is empty
		int none = 0;
		// All direction bits set is full (OR combines them)
		int all = Direction.NORTH.bit | Direction.EAST.bit | Direction.SOUTH.bit | Direction.WEST.bit;
		for (int i = 0; i < grid.length; i++) {
			// Check for obstacle
			if (FIELD[i]) {
				// Set none at an obstacle
				grid[i] = none;
			} else {
				// Set all at no obstacle
				grid[i] = all;
			}
		}
	}

	// Returns the count of uncleared (remaining) possible starting locations
	private int countUncleared() {
		// Count the uncleared bits for each cell and add to total
		int count = 0;
		for (int cell : grid) {
			count += Integer.bitCount(cell);
		}
		return count;
	}

	// Get the index in the grid array of the first uncleared cell (at least 1 possibility remaining)
	// Returns -1 all are cleared
	private int getFirstUnclearedIndex() {
		for (int i = 0; i < grid.length; i++) {
			// If not 0, then some bits are set
			if (grid[i] != 0) {
				return i;
			}
		}
		// None found
		return -1;
	}

	// Gets the first uncleared direction in the cell at the given index
	// Returns null if none is found
	private Direction getFirstUnclearedDirection(int index) {
		// For each direction, check if the bit is set, return it if that's the case
		int cell = grid[index];
		for (Direction direction : Direction.values()) {
			if ((cell & direction.bit) != 0) {
				return direction;
			}
		}
		// None found
		return null;
	}

	// Peform and observation at a location and eleminate the impossible starting positions
	// based on the expected state of blocking (true is blocked, false is not)
	private void observe(Direction direction, int xOffset, int yOffset, boolean blockedState) {
		// Iterate the entire grid line by line
		for (int i = 0; i < grid.length; i++) {
			// Ignore cleared cells
			if (isCleared(i)) {
				continue;
			}
			// Get the coordinates of the cell
			int x = getX(i);
			int y = getY(i);
			// Check all directions
			for (Direction d : Direction.values()) {
				// Skip cleared directions
				if (isCleared(i, d)) {
					continue;
				}
				// Get the current robot offset rotated for the possibility
				int[] localOffset = rotateCCW(xOffset, yOffset, d.index);
				// Rotate the robot direction for the possibility
				Direction localDirection = d.rotateCCW(direction.index);
				// If the blocked state isn't as expected, clear the possibility
				if (isBlocked(x + localOffset[0], y + localOffset[1], localDirection) != blockedState) {
					clearDirection(i, d);
				}
			}
		}
	}

	// Check if a grid cell is cleared
	private boolean isCleared(int index) {
		// No bits must be set
		return grid[index] == 0;
	}

	// Check if a direction is cleared in the cell at the index
	private boolean isCleared(int index, Direction direction) {
		// The direction bit must be cleared
		return (grid[index] & direction.bit) == 0;
	}

	// Clears a direction in the cell at the index
	private void clearDirection(int index, Direction direction) {
		// Keep only the bits that aren't for that direction
		grid[index] &= ~direction.bit;
	}

	// Checks if a cell at the given position has an obstacle next to it
	// in the given direction
	private boolean isBlocked(int x, int y, Direction direction) {
		// Get the adjacent cell coordinates in the direction
		x += direction.xOffset;
		y += direction.yOffset;
		// It needs to be either outside or contain an obstacle
		return !inField(x, y) || FIELD[getIndex(x, y)];
	}

	// Checks if a cell at the current position has an obstacle next to it
	// in the current direction
	private boolean isBlocked() {
		// Average the distance readings
		int distance = 0;
		for (int i = 0; i < OBSERVATION_PING_COUNT; i++) {
			distance += getFilteredData();
		}
		// It needs to be bellow the wall threshold
		return distance / OBSERVATION_PING_COUNT < WALL_THRESHOLD;
	}

	// Reads and filters the data from the ultrasonic sensor
	private int getFilteredData() {
		// get the distance
		int distance = us.getDistance();
		// Check for nothing found
		if (distance == 255) {
			// If nothing found for a while, return that
			if (farPingCount >= FAR_PING_THRESHOLD) {
				return 255;
			} else {
				// Else add to the count and return last valid
				farPingCount++;
				return lastValidDistance;
			}
		} else if (farPingCount > 0) {
			// If something found decrement nothing found count (min zero)
			farPingCount--;
		}
		// Set last valid distance to this one
		lastValidDistance = distance;
		// Return the distance
		return distance;
	}

	// Move to a cell at the given cell coordinate and turns to the desired direction
	// Uses depth first search with a heuristic based on distance to target to find a good path
	public void moveTo(int tileX, int tileY, Direction direction) {
		// Get the current coordinates in tile coords
		int x = (int) Math.floor(odo.getX() / TILE_SPACING);
		int y = (int) Math.floor(odo.getY() / TILE_SPACING);
		// Make a copy of the coordinates
		int cx = x, cy = y;
		// Allocate an array with enough room to store the path to the end (2 ints per node)
		int[] path = new int[32];
		// Current index into the path stack
		int index = 0;
		// A grid of traveled states (true = traveled, false = unvisited)
		boolean[] traveled = new boolean[FIELD.length];
		// Obstacles are marked as traveled
		System.arraycopy(FIELD, 0, traveled, 0, FIELD.length);
		// Repeat until we reach te end node
		while (cx != tileX || cy != tileY) {
			// Find the closest neighboor to the end by checking them all
			int nextX = -1, nextY = -1, distance = Integer.MAX_VALUE;
			for (Direction next : Direction.values()) {
				// Get the neighboor coordinates and index
				int nx = cx + next.xOffset;
				int ny = cy + next.yOffset;
				int ni = getIndex(nx, ny);
				// Check if the neighboor is in the field and hasn't been visited
				if (inField(nx, ny) && !traveled[ni]) {
					// Get the manhattan distance to the end tile
					int newDistance = manhattanDistance(tileX, tileY, nx, ny);
					// If smaller than the current best, update to it as the new best
					if (newDistance < distance) {
						nextX = nx;
						nextY = ny;
						distance = newDistance;
					}
				}
			}
			// Set the current node as traveled
			traveled[getIndex(cx, cy)] = true;
			// Update current coordinates to the neighboor
			cx = nextX;
			cy = nextY;
			// Check if we actually found a neighboor to travel to
			if (distance == Integer.MAX_VALUE) {
				// If not, pop the stack, move the path back to last node
				index -= 2;
				cx = path[index];
				cy = path[index + 1];
				// We will try another direction next iteration
			} else {
				// Else push this node to the stack
				path[index] = cx;
				path[index + 1] = cy;
				index += 2;
			}
		}
		// Now that we have a path stack, travel it, bottom to top
		for (int i = 0; i < index; i += 2) {
			// Get the next coordinates
			x = path[i];
			y = path[i + 1];
			// Travel to them (convert to normal coordinates)
			navig.travelTo(x * TILE_SPACING + HALF_TILE_SPACING, y * TILE_SPACING + HALF_TILE_SPACING);
			navig.waitUntilDone();
		}
		// Turn to desired angle
		navig.turnTo(direction.angle);
		navig.waitUntilDone();
	}

	// Returns the index of the coordinates in the gird array
	// Does no bound checks
	private static int getIndex(int x, int y) {
		// Faster equivalent to x + y * FIELD_SIZE
		return x + (y << FIELD_SHIFT);
	}

	// Gets the x coordinate from the grid index
	private static int getX(int index) {
		// Faster equivalent to index % FIELD_SIZE
		return index & FIELD_MASK;
	}

	// Gets the y coordinate from the grid index
	private static int getY(int index) {
		// Faster equivalent to index / FIELD_SIZE
		return index >> FIELD_SHIFT;
	}

	// Checks if the coordinates are inside the field
	private static boolean inField(int x, int y) {
		// If we have bits outside the mask, the coordinate is outside the range
		return (x & ~FIELD_MASK) == 0 && (y & ~FIELD_MASK) == 0;
	}

	// Rotates the int vector counter-clockwise by 90° * times
	// Results are returned as an array of format {x, y}
	private static int[] rotateCCW(int x, int y, int times) {
		// Wrap around
		times &= 3;
		// Swap coordinates and negate y to rotate 90° CCW each time
		for (int i = 0; i < times; i++) {
			int t = y;
			y = x;
			x = -t;
		}
		// Return results as array
		return new int[] {x, y};
	}

	// Rotates the double vector counter-clockwise by 90° * times
	// Results are returned as an array of format {x, y}
	private static double[] rotateCCW(double x, double y, int times) {
		// Same as before but with double coordinates
		times &= 3;
		for (int i = 0; i < times; i++) {
			double t = y;
			y = x;
			x = -t;
		}
		return new double[] {x, y};
	}

	// Compute the manhattan distance between two points
	private static int manhattanDistance(int ax, int ay, int bx, int by) {
		// Sum of the absolute values of the differences
		return Math.abs(bx - ax) + Math.abs(by - ay);
	}

	// An enum to represent each cardinal direction, with and index
	// and an angle associated to each
	public static enum Direction {
		// Order matters!
		NORTH(0, HALF_PI),
		WEST(1, Math.PI),
		SOUTH(2, 3 * HALF_PI),
		EAST(3, 0);
		// An index to we don't have to deal with angles
		private final int index;
		// The equivalent angle in radian for when we
		// have to convert back to real life coords
		private final double angle;
		// A unit vector pointing in the direction
		private final int xOffset, yOffset;
		// A unique bit for bit set manipulation
		private final int bit;

		// Contructor
		private Direction(int index, double angle) {
			this.index = index;
			this.angle = angle;
			// Get offset from angle
			xOffset = (int) Math.cos(angle);
			yOffset = (int) Math.sin(angle);
			// Bit position is the same as the index
			bit = 1 << index;
		}

		// Rotates the direction CCW by 90° * times
		// and returns the corresponding enum
		private Direction rotateCCW(int times) {
			// Add the angles using the indices, wrap around
			// and get the value from the enum values array
			return values()[(index + times) & 3];
		}
	}

	// Represents the types of orienteers
	public static enum OrienteerType {
		DETERMINISTIC, STOCHASTIC
	}
}
