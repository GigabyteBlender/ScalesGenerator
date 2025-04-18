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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class ScaleVisualizer extends Application {

    private PianoKeyboard piano;
    private ComboBox<String> keySelector;
    private ComboBox<String> scaleTypeSelector;
    private ComboBox<Integer> octaveSelector;
    private CheckBox multiOctaveCheckBox;
    private Text scaleInfoText;
    private String currentRootNote;
    private String currentScaleType;
    private int currentOctave;
    private boolean displayMultiOctave;
    private Set<String> currentScaleNotes;
    private Button playScaleButton;
    private Slider tempoSlider;
    @Override
    public void start(Stage stage) {
        // Create a modern layout with a content container
        BorderPane root = new BorderPane();

        // Apply a modern look with subtle background gradient
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f8f9fa, #e9ecef);");

        // Add padding around the entire UI
        root.setPadding(new Insets(20));

        // Create a responsive scene with improved size
        Scene scene = new Scene(root, 1340, 730);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Create a stylish header with logo
        HBox header = createHeader();

        // Create a content container with shadow effect
        VBox contentContainer = new VBox(20);
        contentContainer.setPadding(new Insets(0, 0, 20, 0));

        // Create the control panel with improved layout
        VBox controlPanel = createControlPanel();
        controlPanel.getStyleClass().add("control-panel");

        // Create the scale information display
        HBox scaleInfoBox = createScaleInfoBox();

        // Create piano container with shadow
        VBox pianoContainer = createPianoContainer();
        pianoContainer.getStyleClass().add("piano-container");

        // Combine all elements
        contentContainer.getChildren().addAll(controlPanel, scaleInfoBox, pianoContainer);

        // Add header and content to main layout
        root.setTop(header);
        root.setCenter(contentContainer);

        // Set up stage properties
        stage.setTitle("Piano Scale Visualizer");
        stage.setMinWidth(880);
        stage.setMinHeight(660);
        stage.setScene(scene);

        // Try to set application icon
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/piano-icon.png")));
        } catch (Exception e) {
            // Icon not found, ignore
        }

        // Clean up MIDI resources when application closes
        stage.setOnCloseRequest(e -> MidiPlayer.close());

        stage.show();

        // Set default values
        currentRootNote = "C";
        currentScaleType = "Major";
        currentOctave = 4;
        displayMultiOctave = true;
        updateScaleDisplay();

        // Refresh piano key highlighting when window regains focus
        stage.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal && currentScaleNotes != null) {
                // Small delay to ensure UI is ready
                PauseTransition pause = new PauseTransition(Duration.millis(100));
                pause.setOnFinished(e -> piano.refreshAllKeys());
                pause.play();
            }
        });
    }

    private HBox createHeader() {
        // Create a stylish header with title
        HBox header = new HBox();
        header.setPadding(new Insets(0, 0, 20, 0));
        header.setAlignment(Pos.CENTER);

        // Try to add a logo image (fallback to text if not found)
        ImageView logoView = null;
        try {
            Image logoImage = new Image(getClass().getResourceAsStream("/icons/piano-logo.png"), 40, 40, true, true);
            logoView = new ImageView(logoImage);
        } catch (Exception e) {
            // Logo not found, we'll use text-only header
        }

        // Create the title text
        Text titleText = new Text("Piano Scale Visualizer");
        titleText.getStyleClass().add("title-text");

        // Create final header layout
        if (logoView != null) {
            header.getChildren().addAll(logoView, new Region(), titleText);
            HBox.setMargin(titleText, new Insets(0, 0, 0, 15));
        } else {
            header.getChildren().add(titleText);
        }

        return header;
    }

    private VBox createControlPanel() {
        // Create main control panel with modern styling
        VBox controlPanel = new VBox(20);
        controlPanel.setPadding(new Insets(25));

        // First row: key and scale type selectors
        HBox mainControlsRow = new HBox(20);
        mainControlsRow.setAlignment(Pos.CENTER_LEFT);

        // Key selector with label
        VBox keyBox = new VBox(5);
        Label keyLabel = new Label("Key");
        keyLabel.getStyleClass().add("control-label");

        keySelector = new ComboBox<>(FXCollections.observableArrayList(
                "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"));
        keySelector.setValue("C");
        keySelector.setPrefWidth(120);
        keySelector.getStyleClass().add("combo-box-styled");
        keySelector.setOnAction(e -> currentRootNote = keySelector.getValue());

        keyBox.getChildren().addAll(keyLabel, keySelector);

        // Scale type selector with label
        VBox scaleBox = new VBox(5);
        Label scaleTypeLabel = new Label("Scale Type");
        scaleTypeLabel.getStyleClass().add("control-label");

        ArrayList<String> scales = new ArrayList<>(ScaleGenerator.getAvailableScales());
        Collections.sort(scales);

        scaleTypeSelector = new ComboBox<>(FXCollections.observableArrayList(scales));
        scaleTypeSelector.setValue("Major");
        scaleTypeSelector.setPrefWidth(200);
        scaleTypeSelector.getStyleClass().add("combo-box-styled");
        scaleTypeSelector.setOnAction(e -> currentScaleType = scaleTypeSelector.getValue());

        scaleBox.getChildren().addAll(scaleTypeLabel, scaleTypeSelector);

        // Generate button
        Button generateButton = new Button("Generate Scale");
        generateButton.getStyleClass().add("button-primary");
        generateButton.setOnAction(e -> updateScaleDisplay());

        // Add spacing to push button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        mainControlsRow.getChildren().addAll(keyBox, scaleBox, spacer, generateButton);

        // Second row: octave selector and multi-octave checkbox
        HBox optionsRow = new HBox(20);
        optionsRow.setAlignment(Pos.CENTER_LEFT);

        // Octave selector with label
        VBox octaveBox = new VBox(5);
        Label octaveLabel = new Label("Octave");
        octaveLabel.getStyleClass().add("control-label");

        octaveSelector = new ComboBox<>(FXCollections.observableArrayList(
                2, 3, 4, 5, 6, 7));
        octaveSelector.setValue(4);
        octaveSelector.setPrefWidth(120);
        octaveSelector.getStyleClass().add("combo-box-styled");
        octaveSelector.setOnAction(e -> currentOctave = octaveSelector.getValue());

        octaveBox.getChildren().addAll(octaveLabel, octaveSelector);

        // Multi-octave checkbox
        multiOctaveCheckBox = new CheckBox("Show Multiple Octaves");
        multiOctaveCheckBox.setSelected(true);
        multiOctaveCheckBox.getStyleClass().add("check-box-styled");
        multiOctaveCheckBox.setOnAction(e -> displayMultiOctave = multiOctaveCheckBox.isSelected());

        // Align checkbox with combo box
        VBox checkboxBox = new VBox();
        checkboxBox.setPadding(new Insets(26, 0, 0, 0));
        checkboxBox.getChildren().add(multiOctaveCheckBox);

        optionsRow.getChildren().addAll(octaveBox, checkboxBox);

        // Add both rows to the control panel
        controlPanel.getChildren().addAll(mainControlsRow, optionsRow);

        return controlPanel;
    }

    private HBox createScaleInfoBox() {
        // Create scale information display
        scaleInfoText = new Text();
        scaleInfoText.getStyleClass().add("scale-info-text");

        HBox scaleInfoBox = new HBox(scaleInfoText);
        scaleInfoBox.getStyleClass().add("scale-info-box");
        scaleInfoBox.setAlignment(Pos.CENTER);
        scaleInfoBox.setPadding(new Insets(16));
        scaleInfoBox.setMaxWidth(Double.MAX_VALUE);

        return scaleInfoBox;
    }

    private VBox createPianoContainer() {
        // Create piano container with 3 octaves
        piano = new PianoKeyboard(3);

        // Create scroll pane for piano
        ScrollPane pianoScrollPane = new ScrollPane(piano);
        pianoScrollPane.setPrefHeight(300);
        pianoScrollPane.setFitToHeight(true);
        pianoScrollPane.setPannable(true);
        pianoScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pianoScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pianoScrollPane.getStyleClass().add("piano-scroll-pane");

        // Create playback controls
        HBox playbackControls = createPlaybackControls();
        playbackControls.getStyleClass().add("playback-controls");

        // Create legend
        HBox legend = createLegend();
        legend.getStyleClass().add("legend-container");

        // Combine all elements
        VBox pianoContainer = new VBox(15);
        pianoContainer.getChildren().addAll(pianoScrollPane, playbackControls, legend);

        return pianoContainer;
    }

    private HBox createPlaybackControls() {
        // Create playback controls with modern styling
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);

        // Play button with icon
        playScaleButton = new Button("Play Scale");
        playScaleButton.getStyleClass().add("button-primary");

        // Try to add a play icon
        try {
            ImageView playIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/play-icon.png"), 16, 16, true, true));
            playScaleButton.setGraphic(playIcon);
        } catch (Exception e) {
            // Icon not found, use text only
        }

        playScaleButton.setOnAction(e -> playCurrentScale());

        // Tempo slider with label
        VBox tempoBox = new VBox(5);
        tempoBox.setAlignment(Pos.CENTER);

        Label tempoLabel = new Label("Tempo: 120 BPM");
        tempoLabel.getStyleClass().add("control-label");

        tempoSlider = new Slider(60, 240, 120);
        tempoSlider.setPrefWidth(220);
        tempoSlider.getStyleClass().add("slider");

        // Update tempo label when slider changes
        tempoSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int tempo = newVal.intValue();
            tempoLabel.setText(String.format("Tempo: %d BPM", tempo));
        });

        tempoBox.getChildren().addAll(tempoLabel, tempoSlider);

        controls.getChildren().addAll(playScaleButton, tempoBox);
        return controls;
    }

    private HBox createLegend() {
        // Create legend for key colors
        HBox legend = new HBox(30);
        legend.setAlignment(Pos.CENTER);

        // Regular key indicator
        Rectangle regularKey = new Rectangle(20, 20);
        regularKey.setFill(Color.WHITE);
        regularKey.setStroke(Color.GRAY);
        regularKey.getStyleClass().add("legend-box");

        Text regularText = new Text("Regular Key");
        regularText.getStyleClass().add("legend-text");

        HBox regularItem = new HBox(8, regularKey, regularText);
        regularItem.setAlignment(Pos.CENTER);
        regularItem.getStyleClass().add("legend-item");

        // Scale note indicator
        Rectangle scaleKey = new Rectangle(20, 20);
        scaleKey.setFill(Color.valueOf("#4cc9f0"));
        scaleKey.setStroke(Color.GRAY);
        scaleKey.getStyleClass().add("legend-box");

        Text scaleText = new Text("Scale Note");
        scaleText.getStyleClass().add("legend-text");

        HBox scaleItem = new HBox(8, scaleKey, scaleText);
        scaleItem.setAlignment(Pos.CENTER);
        scaleItem.getStyleClass().add("legend-item");

        legend.getChildren().addAll(regularItem, scaleItem);
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