package com.scalesGenerator;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;
import java.util.Map;
import javafx.application.Platform;

class PianoKey extends StackPane {
    private final String note;
    private final Rectangle rectangle;
    private boolean active;
    private final boolean isBlack;
    private final Color baseColor;
    private final Color activeColor = Color.DODGERBLUE;
    private final int midiNoteNumber;

    // Reference to parent keyboard for refresh operations
    private PianoKeyboard parentKeyboard;

    public PianoKey(String note, double width, double height, boolean isBlack) {
        this.note = note;
        this.isBlack = isBlack;
        this.baseColor = isBlack ? Color.BLACK : Color.WHITE;
        this.midiNoteNumber = calculateMidiNote(note);
        this.active = false;

        rectangle = new Rectangle(width, height);
        rectangle.setFill(baseColor);
        rectangle.setStroke(Color.GRAY);
        rectangle.setStrokeWidth(1.2);
        rectangle.setArcWidth(isBlack ? 3 : 6);
        rectangle.setArcHeight(isBlack ? 3 : 6);
        rectangle.getStyleClass().add("piano-key");
        rectangle.getStyleClass().add(isBlack ? "black-key" : "white-key");

        getChildren().add(rectangle);

        // Set up mouse event handlers
        setOnMousePressed(e -> {

            // Request refresh of all keys after this key is pressed
            if (parentKeyboard != null) {
                // Short delay to allow the note to play first
                Platform.runLater(() -> parentKeyboard.refreshAllKeys());
            }
            // Play the note
            MidiPlayer.playNote(midiNoteNumber);
        });
    }

    // Setter method to set parent keyboard reference
    public void setParentKeyboard(PianoKeyboard keyboard) {
        this.parentKeyboard = keyboard;
    }

    private int calculateMidiNote(String noteWithOctave) {
        // Extract note and octave
        String noteName = noteWithOctave.replaceAll("\\d", "");
        int octave = Integer.parseInt(noteWithOctave.replaceAll("\\D", ""));

        // Base value for notes using Map.ofEntries()
        Map<String, Integer> noteValues = Map.ofEntries(
                Map.entry("C", 0), Map.entry("C#", 1), Map.entry("D", 2),
                Map.entry("D#", 3), Map.entry("E", 4), Map.entry("F", 5),
                Map.entry("F#", 6), Map.entry("G", 7), Map.entry("G#", 8),
                Map.entry("A", 9), Map.entry("A#", 10), Map.entry("B", 11)
        );

        // MIDI note formula: (octave+1)*12 + noteValue
        return (octave + 1) * 12 + noteValues.get(noteName);
    }

    public String getNote() {
        return note;
    }

    public void setActive(boolean active) {
        this.active = active;
        updateFill();
    }

    private void updateFill() {
        if (active) {
            rectangle.setFill(activeColor);
        } else {
            rectangle.setFill(baseColor);
        }
    }
}