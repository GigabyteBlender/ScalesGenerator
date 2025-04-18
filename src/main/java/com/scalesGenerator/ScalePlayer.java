package com.scalesGenerator;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ScalePlayer {
    private static final int DEFAULT_NOTE_DURATION = 300; // ms

    // Play the scale notes in ascending order
    public static void playScale(Set<String> scaleNotes, int tempo) {
        int noteDuration = (60000 / tempo) / 2; // Convert tempo to note duration

        // Convert set to list for ordering
        List<String> orderedNotes = new ArrayList<>(scaleNotes);

        // Sort by octave and note value
        Collections.sort(orderedNotes, (a, b) -> {
            // Extract octave
            int octaveA = Integer.parseInt(a.replaceAll("\\D", ""));
            int octaveB = Integer.parseInt(b.replaceAll("\\D", ""));

            // Different octaves
            if (octaveA != octaveB) {
                return Integer.compare(octaveA, octaveB);
            }

            // Same octave, compare notes
            String noteNameA = a.replaceAll("\\d", "");
            String noteNameB = b.replaceAll("\\d", "");

            return Integer.compare(getNoteValue(noteNameA), getNoteValue(noteNameB));
        });

        // Play notes in a separate thread
        CompletableFuture.runAsync(() -> {
            try {
                for (String note : orderedNotes) {
                    int midiNote = calculateMidiNote(note);
                    MidiPlayer.playNote(midiNote);
                    Thread.sleep(noteDuration);
                }

                // Play root note again at the end (if there are any notes)
                if (!orderedNotes.isEmpty()) {
                    Thread.sleep(100);
                    MidiPlayer.playNote(calculateMidiNote(orderedNotes.get(0)));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static int getNoteValue(String note) {
        String[] noteValues = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        for (int i = 0; i < noteValues.length; i++) {
            if (noteValues[i].equals(note)) {
                return i;
            }
        }
        return 0;
    }

    private static int calculateMidiNote(String noteWithOctave) {
        // Extract note and octave
        String noteName = noteWithOctave.replaceAll("\\d", "");
        int octave = Integer.parseInt(noteWithOctave.replaceAll("\\D", ""));

        int noteValue = getNoteValue(noteName);

        // MIDI note formula: (octave+1)*12 + noteValue
        return (octave + 1) * 12 + noteValue;
    }
}