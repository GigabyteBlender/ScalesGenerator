package com.scalesGenerator;

import java.util.*;

class ScaleGenerator {
    private static final Map<String, List<Integer>> SCALE_FORMULAS = new HashMap<>();

    static {
        // Basic scales
        SCALE_FORMULAS.put("Major", List.of(2, 2, 1, 2, 2, 2, 1));
        SCALE_FORMULAS.put("Natural Minor", List.of(2, 1, 2, 2, 1, 2, 2));
        SCALE_FORMULAS.put("Harmonic Minor", List.of(2, 1, 2, 2, 1, 3, 1));
        SCALE_FORMULAS.put("Melodic Minor", List.of(2, 1, 2, 2, 2, 2, 1));

        // Additional scales
        SCALE_FORMULAS.put("Dorian", List.of(2, 1, 2, 2, 2, 1, 2));
        SCALE_FORMULAS.put("Phrygian", List.of(1, 2, 2, 2, 1, 2, 2));
        SCALE_FORMULAS.put("Lydian", List.of(2, 2, 2, 1, 2, 2, 1));
        SCALE_FORMULAS.put("Mixolydian", List.of(2, 2, 1, 2, 2, 1, 2));
        SCALE_FORMULAS.put("Locrian", List.of(1, 2, 2, 1, 2, 2, 2));

        // Pentatonic scales
        SCALE_FORMULAS.put("Major Pentatonic", List.of(2, 2, 3, 2, 3));
        SCALE_FORMULAS.put("Minor Pentatonic", List.of(3, 2, 2, 3, 2));

        // Blues scales
        SCALE_FORMULAS.put("Blues", List.of(3, 2, 1, 1, 3, 2));

        // Exotic scales
        SCALE_FORMULAS.put("Whole Tone", List.of(2, 2, 2, 2, 2, 2));
        SCALE_FORMULAS.put("Diminished", List.of(2, 1, 2, 1, 2, 1, 2, 1));
        SCALE_FORMULAS.put("Hungarian Minor", List.of(2, 1, 3, 1, 1, 3, 1));
    }

    // Return all available scales
    public static List<String> getAvailableScales() {
        List<String> scales = new ArrayList<>(SCALE_FORMULAS.keySet());
        Collections.sort(scales);
        return scales;
    }

    // Legacy version for backward compatibility
    public static Set<String> getScaleNotes(String root, String scaleType) {
        return getScaleNotes(root, scaleType, 4, true);
    }

    // Enhanced version with octave and multi-octave parameters
    public static Set<String> getScaleNotes(String root, String scaleType, int startOctave, boolean multiOctave) {
        if (root == null || scaleType == null) {
            return Collections.emptySet();
        }

        List<Integer> intervals = SCALE_FORMULAS.get(scaleType);
        if (intervals == null) {
            return Collections.emptySet();
        }

        List<String> allNotes = Arrays.asList(
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B");

        int rootIndex = allNotes.indexOf(root);
        if (rootIndex == -1) {
            return Collections.emptySet();
        }

        Set<String> scaleNotes = new HashSet<>();

        // Determine how many octaves to generate
        int endOctave = multiOctave ? startOctave + 1 : startOctave;

        // Generate notes for the specified octave range
        for (int octave = startOctave; octave <= endOctave; octave++) {
            int currentIndex = rootIndex;

            // Add root note for this octave
            String rootNote = allNotes.get(currentIndex) + octave;
            scaleNotes.add(rootNote);

            // Generate remaining notes for this octave
            for (int interval : intervals) {
                currentIndex = (currentIndex + interval) % 12;

                // Determine octave for this note
                int noteOctave = octave;
                if (currentIndex < rootIndex && octave == endOctave && multiOctave) {
                    // We've wrapped around to the next octave
                    noteOctave++;
                }

                // Only add the last note (octave) at the end if we're on the last octave
                // to avoid duplicating the root note
                if (octave < endOctave || currentIndex != rootIndex) {
                    scaleNotes.add(allNotes.get(currentIndex) + noteOctave);
                }
            }

            // If not using multi-octave mode, just generate one octave and stop
            if (!multiOctave) {
                break;
            }
        }

        return scaleNotes;
    }
}