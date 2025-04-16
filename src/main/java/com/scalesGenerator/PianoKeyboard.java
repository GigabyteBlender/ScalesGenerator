package com.scalesGenerator;

import javafx.scene.layout.Pane;
import java.util.*;
import javafx.application.Platform;

public class PianoKeyboard extends Pane {
    // Existing constants remain unchanged
    private static final double WHITE_KEY_WIDTH = 60;
    private static final double BLACK_KEY_WIDTH = WHITE_KEY_WIDTH * 0.6;
    private static final double WHITE_KEY_HEIGHT = 180;
    private static final double BLACK_KEY_HEIGHT = WHITE_KEY_HEIGHT * 0.6;

    private final Map<String, PianoKey> keys = new HashMap<>();
    private Set<String> activeNotes = new HashSet<>();

    public PianoKeyboard(int octaves) {
        setPrefSize(octaves * 7 * WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
        setMinSize(octaves * 7 * WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT);
        createKeys(octaves);
    }

    private void createKeys(int octaves) {
        List<String> naturalNotes = Arrays.asList("C", "D", "E", "F", "G", "A", "B");
        Map<String, Integer> sharpPositions = Map.of(
                "C#", 0, "D#", 1, "F#", 3, "G#", 4, "A#", 5
        );

        // Create white keys first (so they're in the back)
        for (int octave = 0; octave < octaves; octave++) {
            for (int i = 0; i < naturalNotes.size(); i++) {
                String note = naturalNotes.get(i);
                String fullNote = note + (octave + 4);
                double x = octave * naturalNotes.size() * WHITE_KEY_WIDTH + i * WHITE_KEY_WIDTH;

                PianoKey key = new PianoKey(fullNote, WHITE_KEY_WIDTH, WHITE_KEY_HEIGHT, false);
                key.setLayoutX(x);
                key.setLayoutY(0);
                key.setParentKeyboard(this); // Set reference to parent keyboard
                keys.put(fullNote, key);
                getChildren().add(key);
            }
        }

        // Create black keys on top
        for (int octave = 0; octave < octaves; octave++) {
            for (Map.Entry<String, Integer> entry : sharpPositions.entrySet()) {
                String note = entry.getKey();
                String fullNote = note + (octave + 4);
                int position = entry.getValue();

                double x = octave * 7 * WHITE_KEY_WIDTH + position * WHITE_KEY_WIDTH + WHITE_KEY_WIDTH * 0.7;

                PianoKey key = new PianoKey(fullNote, BLACK_KEY_WIDTH, BLACK_KEY_HEIGHT, true);
                key.setLayoutX(x);
                key.setLayoutY(0);  // Start from the top
                key.setParentKeyboard(this); // Set reference to parent keyboard
                keys.put(fullNote, key);
                getChildren().add(key);
            }
        }
    }

    public void highlightNotes(Set<String> notes) {
        // Save the active notes for future reference
        this.activeNotes = new HashSet<>(notes);

        // First reset all keys
        keys.forEach((name, key) -> key.setActive(false));

        // Then highlight the ones in the scale
        for (String note : notes) {
            if (keys.containsKey(note)) {
                keys.get(note).setActive(true);
            }
        }
    }

    // Method to refresh all keys synchronously
    public void refreshAllKeys() {
        // Ensure we're on the JavaFX application thread
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::refreshAllKeys);
            return;
        }

        // First, reset all keys to their default state
        keys.forEach((name, key) -> key.setActive(false));

        // Then reapply active states based on stored activeNotes
        if (activeNotes != null && !activeNotes.isEmpty()) {
            for (String note : activeNotes) {
                if (keys.containsKey(note)) {
                    keys.get(note).setActive(true);
                }
            }
        }

        // Force a visual refresh
        this.requestLayout();
    }

    // Legacy method for backward compatibility
    public void refreshHighlighting() {
        refreshAllKeys();
    }
}