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
    }

    // Return all available scales
    public static List<String> getAvailableScales() {
        List<String> scales = new ArrayList<>(SCALE_FORMULAS.keySet());
        Collections.sort(scales);
        return scales;
    }

    public static Set<String> getScaleNotes(String root, String scaleType) {
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

        // Generate notes for two octaves to ensure we display across the keyboard
        for (int octave = 4; octave <= 5; octave++) {
            int currentIndex = rootIndex;

            // Add root note
            scaleNotes.add(allNotes.get(currentIndex) + octave);

            // Add remaining notes in scale
            for (int interval : intervals) {
                currentIndex = (currentIndex + interval) % 12;

                // Only add the last note (octave) in the second iteration
                if (octave == 4 || currentIndex != rootIndex) {
                    scaleNotes.add(allNotes.get(currentIndex) + (currentIndex < rootIndex && octave == 5 ? 6 : octave));
                }
            }
        }

        return scaleNotes;
    }
}