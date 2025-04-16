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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Collections;

public class ScaleVisualizer extends Application {

    private PianoKeyboard piano;
    private ComboBox<String> keySelector;
    private ComboBox<String> scaleTypeSelector;
    private Text scaleInfoText;
    private String currentRootNote;
    private String currentScaleType;
    private Set<String> currentScaleNotes;

    @Override
    public void start(Stage stage) {
        // Create a proper layout with controls at the top and piano below
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // Increased window size to accommodate larger keyboard
        Scene scene = new Scene(root, 950, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Create a title with better styling
        Text titleText = new Text("Piano Scale Visualizer");
        titleText.getStyleClass().add("title-text");

        HBox titleBox = new HBox(titleText);
        titleBox.setPadding(new Insets(10, 0, 20, 0));
        titleBox.setAlignment(Pos.CENTER);

        // Setup controls in a clearly separated area with a more modern look
        HBox controls = createControlPanel();

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
        topContainer.getChildren().addAll(titleBox, controls, scaleInfoBox);

        // Create a larger piano in its own container (2 octaves)
        piano = new PianoKeyboard(2);

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

        // Add all components to main layout
        root.setTop(topContainer);
        root.setCenter(pianoScrollPane);
        root.setBottom(legend);

        // Set up stage and show
        stage.setTitle("Scale Visualizer");
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        stage.setScene(scene);

        // Clean up MIDI resources when application closes
        stage.setOnCloseRequest(e -> MidiPlayer.close());

        stage.show();

        // Generate initial display
        currentRootNote = "C";
        currentScaleType = "Major";
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

    private HBox createControlPanel() {
        HBox controls = new HBox(15);
        controls.setPadding(new Insets(15));
        controls.setAlignment(Pos.CENTER);
        controls.setStyle("-fx-background-color: #F5F4ED;" +
                "-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0; " +
                "-fx-border-radius: 5; -fx-background-radius: 5;");


        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(1);
        dropShadow.setRadius(5);
        controls.setEffect(dropShadow);

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
            // Get the scale notes
            Set<String> scaleNotes = ScaleGenerator.getScaleNotes(currentRootNote, currentScaleType);

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

            scaleInfoText.setText(currentRootNote + " " + currentScaleType + " Scale: " + notesDisplay);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}