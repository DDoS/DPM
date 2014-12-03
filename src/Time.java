/**
 * A singleton class for keeping track of time. The shared nature of it makes sharing this easier, and we only need one time for the robot, so it doesn't matter that we can only have on timer.
 */
public final class Time {
    //Keeps track of the total time allowed for the run, as well as the system time when it started
    private static long limitSeconds;
    private static long startTime;

    // prevent instance of the singleton
    private Time() {
    }

    /**
     * Call to set the starting time of the robot, and pass in the time limit of the run
     *
     * @param lim Integer specifying the time limit in seconds that the robot is allowed
     */
    public static void startTime(long lim) {
        limitSeconds = lim;
        startTime = System.currentTimeMillis();
    }

    /**
     * Compares the start system time to the current system time to find the amount of time that elapsed
     *
     * @return Returns the seconds left in the run as a long integer.
     */
    public static long timeLeft() {
        long timeDiff = System.currentTimeMillis() - startTime;
        return limitSeconds - timeDiff / 1000;
    }
}
