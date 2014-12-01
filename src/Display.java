import lejos.nxt.*;

/**
 * A singleton class to control the Lego NXT controller brick's single LCD display. On each line the display will print one key/value mapping. Ordering can only be guaranteed when using {@link
 * #reserve(String...)} to reserve multiple lines at once. In such a case the array of mappings are guaranteed to occupy successive lines. Updates are drawn immediately.
 * <p/>
 * THREAD SAFE
 */
public final class Display {
    private static final int LINE_COUNT = LCD.SCREEN_HEIGHT / LCD.FONT_HEIGHT;
    private static final int COLUMN_COUNT = LCD.SCREEN_WIDTH / LCD.FONT_WIDTH;
    private static final Entry[] map = new Entry[LINE_COUNT];
    private static int currentIndex = 0;

    // Prevent instances of the singleton
    private Display() {
    }

    /**
     * Reserves successive lines for the desired array of keys. Lines are allocated to keys following the array ordering, so if fewer lines than keys are available the first keys are given priority.
     *
     * @param keys The keys to reserve
     * @return How many keys where successfuly reserved
     */
    public static int reserve(String... keys) {
        synchronized (map) {
            if (currentIndex + keys.length > LINE_COUNT) {
                return 0;
            }
            int reserved = 0;
            for (; reserved < keys.length && currentIndex < LINE_COUNT; currentIndex++, reserved++) {
                map[currentIndex] = new Entry(keys[reserved]);
                draw(currentIndex);
            }
            return reserved;
        }
    }

    /**
     * Updates a mapping, adding a new one if necessary. Since only a limited number of lines are available on the display, this method returns the success of the update as: false if no line is free,
     * true if the mapping have been given a line. Once the mapping are displayed on a line, they get to keep the line until either {@link #remove(String)} or {@link #clear()} is called. This causes a
     * display refresh.
     *
     * @param key The key for the mapping
     * @param val The value for the mapping
     * @return Whether or not the update succeeded
     */
    public static boolean update(String key, String val) {
        synchronized (map) {
            for (int i = 0; i < currentIndex; i++) {
                final Entry entry = map[i];
                if (entry.hasKey(key)) {
                    entry.value = val;
                    draw(i);
                    return true;
                }
            }
            if (currentIndex < LINE_COUNT) {
                map[currentIndex] = new Entry(key, val);
                draw(currentIndex);
                currentIndex++;
                return true;
            }
            return false;
        }
    }

    /**
     * Removes a mapping from the display. The line is freed. All lines bellow are shifted up.
     *
     * @param key The key of the mapping to remove
     */
    public static void remove(String key) {
        synchronized (map) {
            boolean deleted = false;
            for (int i = 0; i < currentIndex; i++) {
                if (!deleted) {
                    final Entry entry = map[i];
                    if (entry.hasKey(key)) {
                        map[i] = null;
                        deleted = true;
                    }
                }
                if (deleted) {
                    map[i] = i < currentIndex - 1 ? map[i + 1] : null;
                    draw(i);
                }
            }
            if (deleted) {
                currentIndex--;
            }
        }
    }

    /**
     * Clears the display mappings. Use with caution as this will affect the way other threads will have their data displayed (potentially messing up the ordering).
     */
    public static void clear() {
        synchronized (map) {
            for (int i = 0; i < currentIndex; i++) {
                map[i] = null;
                draw(i);
            }
            currentIndex = 0;
        }
    }

    private static void draw(int line) {
        int displayLine = LINE_COUNT - 1 - line;
        LCD.clear(displayLine);
        final Entry entry = map[line];
        if (entry == null) {
            return;
        }
        LCD.drawString(entry.key + ": " + (entry.value != null ? entry.value : ""), 0, displayLine);
    }

    private static class Entry {
        private final String key;
        private String value;

        private Entry(String key) {
            this(key, null);
        }

        private Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        private boolean hasKey(String key) {
            return this.key.equals(key);
        }
    }
}
