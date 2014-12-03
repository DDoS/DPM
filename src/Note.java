import lejos.nxt.*;

/**
 * A class for playing the A note on the robot. Octave 0 is actually octave 1 as it's the lowest the robot supports.
 */
public final class Note {
    private static final int A_NOTE = 55;

    // Prevent instances of the singleton
    private Note() {
    }

    /**
     * Plays the A note once at octave 3 for 500ms.
     */
    public static void play() {
        play(3);
    }

    /**
     * Plays the A note once at the desired octave for 500ms.
     *
     * @param octave The octave
     */
    public static void play(int octave) {
        play(octave, 500);
    }

    /**
     * Plays the A note once at the desired octave for the desired duration
     * @param octave The octave
     * @param duration The duration in milliseconds
     */
    public static void play(int octave, int duration) {
        play(octave, duration, 1);
    }

    /**
     * Repeats the A note for the desired number of times at the desired octave for the desired duration
     * @param octave The octave
     * @param duration The duration in milliseconds
     * @param repeat How many times to repeat
     */
    public static void play(int octave, int duration, int repeat) {
        for (int i = 0; i < repeat; i++) {
            Sound.playNote(Sound.FLUTE, A_NOTE << octave, duration);
        }
    }
}
