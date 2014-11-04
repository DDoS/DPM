/**
 * A singleton class to control the Lego NXT controller brick's single LCD display. On each line the display will print one key/value mapping. Ordering can only be guaranteed when using {@link
 * #reserve(String...)} to reserve multiple lines at once. In such a case the array of mappings are guaranteed to occupy successive lines.
 * <p/>
 * THREAD SAFE
 */
public class Display {
    /**
     * Reserves successive lines for the desired array of keys.
     *
     * @param keys The keys to reserve
     */
    public static void reserve(String... keys) {

    }

    /**
     * Clears the display mappings. Use with caution as this will affect the way other threads will have their data displayed (potentially messing up the ordering).
     */
    public static void clear() {

    }

    /**
     * Updates a mapping, adding a new one if necessary. Since only a limited number of lines are available on the display, this method returns the success of the update as: false if no line is free,
     * true if the mapping have been given a line. Once the mapping are displayed on a line, they get to keep the line until either {@link #remove(String)} or {@link #clear()} is called. This causes a
     * display refresh.
     *
     * @param key The key for the mapping
     * @param val The value for the mapping
     */
    public static boolean update(String key, String val) {
        return false;
    }

    /**
     * Removes a mapping from the display. The line is freed.
     *
     * @param key The key of the mapping to remove
     */
    public static void remove(String key) {

    }
}
