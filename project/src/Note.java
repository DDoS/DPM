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
