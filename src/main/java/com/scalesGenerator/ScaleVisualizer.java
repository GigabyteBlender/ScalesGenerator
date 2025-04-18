package com.scalesGenerator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import javafx.scene.effect.DropShadow;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class ScaleVisualizer extends Application {

    private PianoKeyboard piano;
    private ComboBox<String> keySelector;
    private ComboBox<String> scaleTypeSelector;
    private ComboBox<Integer> octaveSelector;  // New selector for octave
    private CheckBox multiOctaveCheckBox;      // New checkbox for multi-octave display
    private Text scaleInfoText;
    private String currentRootNote;
    private String currentScaleType;
    private int currentOctave;                 // New field for octave
    private boolean displayMultiOctave;        // New field for multi-octave display
    private Set<String> currentScaleNotes;
    private Button playScaleButton;            // New button to play scale

    @Override
    public void start(Stage stage) {
        // Create a proper layout with controls at the top and piano below
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Increased window size to accommodate larger keyboard
        Scene scene = new Scene(root, 910, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Create a title with better styling
        Text titleText = new Text("Piano Scale Visualizer");
        titleText.getStyleClass().add("title-text");

        HBox titleBox = new HBox(titleText);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        titleBox.setAlignment(Pos.CENTER);

        // Setup controls in a clearly separated area with a more modern look
        VBox controlArea = createControlArea();

        // Create a scale information display area with better styling
        scaleInfoText = new Text();
        scaleInfoText.getStyleClass().add("scale-info-text");

        HBox scaleInfoBox = new HBox(scaleInfoText);
        scaleInfoBox.getStyleClass().add("scale-info-box");
        scaleInfoBox.setPadding(new Insets(12));
        scaleInfoBox.setAlignment(Pos.CENTER);
        scaleInfoBox.setMaxWidth(Double.MAX_VALUE);

        // Container for title and controls
        VBox topContainer = new VBox(15);
        topContainer.getChildren().addAll(titleBox, controlArea, scaleInfoBox);

        // Create a larger piano in its own container (2 octaves)
        piano = new PianoKeyboard(3);  // Increased to 3 octaves for better display

        // Create a scroll pane to handle the larger keyboard with improved styling
        ScrollPane pianoScrollPane = new ScrollPane(piano);
        pianoScrollPane.setPrefHeight(320);
        pianoScrollPane.setFitToHeight(true);
        pianoScrollPane.setPannable(true); // Allow panning with mouse
        pianoScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pianoScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pianoScrollPane.getStyleClass().add("piano-scroll-pane");

        // Create a legend for the piano
        HBox legend = createLegend();

        // Add playback controls
        HBox playbackControls = createPlaybackControls();

        // Combine bottom elements
        VBox bottomContainer = new VBox(10);
        bottomContainer.getChildren().addAll(playbackControls, legend);
        bottomContainer.setAlignment(Pos.CENTER);

        // Add all components to main layout
        root.setTop(topContainer);
        root.setCenter(pianoScrollPane);
        root.setBottom(bottomContainer);

        // Set up stage and show
        stage.setTitle("Scale Visualizer");
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.setScene(scene);

        // Clean up MIDI resources when application closes
        stage.setOnCloseRequest(e -> MidiPlayer.close());

        stage.show();

        // Set default values and generate initial display
        currentRootNote = "C";
        currentScaleType = "Major";
        currentOctave = 4;           // Default octave
        displayMultiOctave = true;   // Default to multi-octave display
        updateScaleDisplay();

        // Add a listener for window focus changes to refresh highlighting if needed
        stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Window regained focus, refresh highlighting
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> {
                    if (currentScaleNotes != null) {
                        piano.highlightNotes(currentScaleNotes);
                    }
                });
                pause.play();
            }
        });
    }

    private VBox createControlArea() {
        // Main container for controls
        VBox controlArea = new VBox(10);
        controlArea.setPadding(new Insets(15));
        controlArea.setStyle("-fx-background-color: #F5F4ED;" +
                "-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        dropShadow.setRadius(5);
        controlArea.setEffect(dropShadow);

        // Create first row of controls (Key and Scale Type)
        HBox mainControls = createMainControls();

        // Create second row of controls (Octave and Options)
        HBox optionControls = createOptionControls();

        controlArea.getChildren().addAll(mainControls, optionControls);
        return controlArea;
    }

    private HBox createMainControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        // Initialize comboboxes with proper width
        keySelector = new ComboBox<>(FXCollections.observableArrayList(
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"));
        keySelector.setValue("C"); // Default selection
        keySelector.setPrefWidth(100);
        keySelector.getStyleClass().add("combo-box-styled");

        // Get available scales and sort them
        ArrayList<String> scales = new ArrayList<>(ScaleGenerator.getAvailableScales());
        Collections.sort(scales);

        scaleTypeSelector = new ComboBox<>(FXCollections.observableArrayList(scales));
        scaleTypeSelector.setValue("Major"); // Default selection
        scaleTypeSelector.setPrefWidth(170);
        scaleTypeSelector.getStyleClass().add("combo-box-styled");

        Button generateButton = new Button("Generate Scale");
        generateButton.setOnAction(e -> updateScaleDisplay());
        generateButton.getStyleClass().add("generate-button");

        Label keyLabel = new Label("Key:");
        keyLabel.getStyleClass().add("control-label");
        Label scaleTypeLabel = new Label("Scale Type:");
        scaleTypeLabel.getStyleClass().add("control-label");

        controls.getChildren().addAll(
                keyLabel, keySelector,
                scaleTypeLabel, scaleTypeSelector,
                generateButton
        );

        // Add event handlers to update the display when selections change
        keySelector.setOnAction(e -> {
            currentRootNote = keySelector.getValue();
        });

        scaleTypeSelector.setOnAction(e -> {
            currentScaleType = scaleTypeSelector.getValue();
        });

        generateButton.setOnAction(e -> {
            updateScaleDisplay();
        });

        return controls;
    }

    private HBox createOptionControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);

        // Add octave selector
        octaveSelector = new ComboBox<>(FXCollections.observableArrayList(
                2, 3, 4, 5, 6, 7));
        octaveSelector.setValue(4); // Default selection
        octaveSelector.setPrefWidth(70);
        octaveSelector.getStyleClass().add("combo-box-styled");
        octaveSelector.setOnAction(e -> {
            currentOctave = octaveSelector.getValue();
        });

        Label octaveLabel = new Label("Octave:");
        octaveLabel.getStyleClass().add("control-label");

        // Add multi-octave display checkbox
        multiOctaveCheckBox = new CheckBox("Show Multiple Octaves");
        multiOctaveCheckBox.setSelected(true);
        multiOctaveCheckBox.getStyleClass().add("check-box-styled");
        multiOctaveCheckBox.setOnAction(e -> {
            displayMultiOctave = multiOctaveCheckBox.isSelected();
        });

        controls.getChildren().addAll(
                octaveLabel, octaveSelector,
                multiOctaveCheckBox
        );

        return controls;
    }

    private HBox createPlaybackControls() {
        HBox controls = new HBox(15);
        controls.setPadding(new Insets(10, 0, 0, 0));
        controls.setAlignment(Pos.CENTER);

        // Play scale button
        playScaleButton = new Button("Play Scale");
        playScaleButton.getStyleClass().add("play-button");
        playScaleButton.setOnAction(e -> playCurrentScale());

        // Slider for playback speed
        Label tempoLabel = new Label("Tempo:");
        tempoLabel.getStyleClass().add("control-label");
        Slider tempoSlider = new Slider(60, 240, 120);
        tempoSlider.setPrefWidth(200);
        tempoSlider.setShowTickMarks(true);
        tempoSlider.setShowTickLabels(true);
        tempoSlider.setMajorTickUnit(60);
        tempoSlider.setMinorTickCount(1);
        tempoSlider.setBlockIncrement(10);

        controls.getChildren().addAll(
                playScaleButton,
                tempoLabel, tempoSlider
        );

        return controls;
    }

    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(15, 0, 5, 0));
        legend.setAlignment(Pos.CENTER);

        // Regular key indicator
        Rectangle regularKey = new Rectangle(20, 20);
        regularKey.setFill(Color.WHITE);
        regularKey.setStroke(Color.GRAY);
        Text regularText = new Text("Regular Key");

        // Scale note indicator
        Rectangle scaleKey = new Rectangle(20, 20);
        scaleKey.setFill(Color.DODGERBLUE);
        scaleKey.setStroke(Color.GRAY);
        Text scaleText = new Text("Scale Note");

        // Add indicators to legend
        legend.getChildren().addAll(
                new HBox(8, regularKey, regularText),
                new HBox(8, scaleKey, scaleText)
        );

        return legend;
    }

    private void updateScaleDisplay() {
        if (currentRootNote != null && currentScaleType != null) {
            // Get the scale notes - Updated to use the new parameters
            Set<String> scaleNotes;
            if (displayMultiOctave) {
                scaleNotes = ScaleGenerator.getScaleNotes(currentRootNote, currentScaleType, currentOctave, true);
            } else {
                scaleNotes = ScaleGenerator.getScaleNotes(currentRootNote, currentScaleType, currentOctave, false);
            }

            // Save the current scale notes for potential refreshing
            currentScaleNotes = scaleNotes;

            // Highlight the notes on the piano
            piano.highlightNotes(scaleNotes);

            // Extract just the note names without octaves and sort them for display
            Set<String> uniqueNoteNames = scaleNotes.stream()
                    .map(note -> note.replaceAll("\\d", ""))
                    .collect(Collectors.toSet());

            // Convert to list for proper ordering
            ArrayList<String> sortedNotes = new ArrayList<>(uniqueNoteNames);

            // Sort notes in musical order starting from the root note
            String[] allNotes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
            ArrayList<String> orderedNotes = new ArrayList<>();

            // First add the root note
            orderedNotes.add(currentRootNote);

            // Then add the rest in ascending order from the root
            int rootIndex = -1;
            for (int i = 0; i < allNotes.length; i++) {
                if (allNotes[i].equals(currentRootNote)) {
                    rootIndex = i;
                    break;
                }
            }

            if (rootIndex != -1) {
                for (int i = 1; i < allNotes.length; i++) {
                    int idx = (rootIndex + i) % 12;
                    String nextNote = allNotes[idx];
                    if (uniqueNoteNames.contains(nextNote) && !nextNote.equals(currentRootNote)) {
                        orderedNotes.add(nextNote);
                    }
                }
            }

            // Build a formatted string of note names
            StringBuilder notesDisplay = new StringBuilder();
            for (int i = 0; i < orderedNotes.size(); i++) {
                if (i > 0) {
                    notesDisplay.append(" - ");
                }
                notesDisplay.append(orderedNotes.get(i));
            }

            // Update scale info text
            String octaveInfo = displayMultiOctave ? "Multiple Octaves" : "Octave " + currentOctave;
            scaleInfoText.setText(currentRootNote + " " + currentScaleType + " Scale (" + octaveInfo + "): " + notesDisplay);
        }
    }

    private void playCurrentScale() {
        if (currentScaleNotes != null && !currentScaleNotes.isEmpty()) {
            // Disable play button while playing
            playScaleButton.setDisable(true);

            // Play scale in separate thread
            new Thread(() -> {
                try {
                    // Create ordered list of notes
                    List<String> orderedNotes = new ArrayList<>(currentScaleNotes);

                    Collections.sort(orderedNotes, (a, b) -> {
                        int octaveA = Integer.parseInt(a.replaceAll("\\D", ""));
                        int octaveB = Integer.parseInt(b.replaceAll("\\D", ""));

                        if (octaveA != octaveB) {
                            return Integer.compare(octaveA, octaveB);
                        }

                        // Same octave, get note value
                        String noteA = a.replaceAll("\\d", "");
                        String noteB = b.replaceAll("\\D", "");

                        int valueA = getNoteValue(noteA);
                        int valueB = getNoteValue(noteB);

                        return Integer.compare(valueA, valueB);
                    });

                    // Play each note in sequence
                    for (String note : orderedNotes) {
                        int midiNote = calculateMidiNote(note);
                        MidiPlayer.playNote(midiNote);
                        Thread.sleep(350); // Note duration
                    }

                    // Play root note again at the end
                    Thread.sleep(200);
                    String rootWithOctave = currentRootNote + currentOctave;
                    MidiPlayer.playNote(calculateMidiNote(rootWithOctave));

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    // Re-enable play button
                    javafx.application.Platform.runLater(() -> playScaleButton.setDisable(false));
                }
            }).start();
        }
    }

    private int getNoteValue(String note) {
        String[] noteValues = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        for (int i = 0; i < noteValues.length; i++) {
            if (noteValues[i].equals(note)) {
                return i;
            }
        }
        return 0;
    }

    private int calculateMidiNote(String noteWithOctave) {
        // Extract note and octave
        String noteName = noteWithOctave.replaceAll("\\d", "");
        int octave = Integer.parseInt(noteWithOctave.replaceAll("\\D", ""));

        int noteValue = getNoteValue(noteName);

        // MIDI note formula: (octave+1)*12 + noteValue
        return (octave + 1) * 12 + noteValue;
    }

    public static void main(String[] args) {
        launch(args);
    }
}