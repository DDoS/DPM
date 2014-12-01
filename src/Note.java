import lejos.nxt.*;

public final class Note {
    private static final int A_NOTE = 55;

    // Prevent instances of the singleton
    private Note() {
    }

    public static void play() {
        play(3);
    }

    public static void play(int octave) {
        play(octave, 500);
    }

    public static void play(int octave, int duration) {
        play(octave, duration, 1);
    }

    public static void play(int octave, int duration, int repeat) {
        for (int i = 0; i < repeat; i++) {
            Sound.playNote(Sound.FLUTE, A_NOTE << octave, duration);
        }
    }
}
