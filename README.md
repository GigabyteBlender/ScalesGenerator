# Piano Scale Visualizer

A JavaFX application that visualizes piano scales and allows users to interact with a virtual piano keyboard. This educational tool helps musicians learn scales by displaying them on an interactive piano keyboard with visual feedback and MIDI sound playback.

## Features

- Interactive piano keyboard spanning 2 octaves
- Visual highlighting of notes in the selected scale
- MIDI sound playback when pressing piano keys
- Support for multiple scale types (Major, Minor, Modal scales)
- Real-time scale generation and display
- Clean, modern UI with professional styling

## Project Structure

The application consists of several key components:

- **ScaleVisualizer.java**: Main application class containing the UI and controller logic
- **PianoKeyboard.java**: Manages the piano keyboard component and key highlighting
- **PianoKey.java**: Represents individual piano keys with visual and interactive capabilities
- **ScaleGenerator.java**: Handles music theory calculations to generate scales
- **MidiPlayer.java**: Provides MIDI sound capabilities for realistic piano sounds
- **styles.css**: Contains styling for the entire application

## How It Works

The application uses JavaFX for the user interface and the Java Sound API (javax.sound.midi) for audio playback. When a user selects a key and scale type, the application:

1. Generates the appropriate scale notes using music theory formulas
2. Highlights the corresponding keys on the virtual piano keyboard
3. Allows users to play the highlighted notes by clicking on the keys
4. Provides visual feedback during interactions

## Scale Types

The application supports the following scale types:

- Major
- Natural Minor
- Harmonic Minor
- Melodic Minor
- Dorian
- Phrygian
- Lydian
- Mixolydian

## Requirements

- Java 11 or higher
- JavaFX 11 or higher
- MIDI-compatible sound system

## Building and Running

### Using an IDE (Eclipse, IntelliJ IDEA, etc.)

1. Import the project into your IDE
2. Ensure JavaFX is properly configured in your IDE
3. Run the `ScaleVisualizer` class

### Using Maven

If you want to use Maven to build the project, make sure to have the following in your pom.xml:

```xml
<dependencies>
    <dependency>
        <groupId>org.openjfx</groupId>
        <artifactId>javafx-controls</artifactId>
        <version>17.0.2</version>
    </dependency>
</dependencies>
```

Then run:

```bash
mvn clean javafx:run
```

## Usage

1. Select a root note (e.g., C, F#, Bb) from the dropdown menu
2. Choose a scale type (e.g., Major, Natural Minor) from the dropdown menu
3. Click "Generate Scale" or let the application auto-generate when selections change
4. The piano keyboard will highlight the notes in the selected scale
5. Click on piano keys to hear the corresponding notes
6. Use the horizontal scroll bar to navigate the keyboard if needed

## Implementation Details

### Key Classes and Components

#### ScaleVisualizer
- The main application class that sets up the GUI and handles user interactions
- Manages the layout of controls, piano keyboard, and informational displays
- Responds to user input and updates the display accordingly

#### PianoKeyboard
- Manages a collection of PianoKey objects
- Handles key highlighting based on the current scale
- Provides methods to refresh all keys simultaneously

#### PianoKey
- Represents an individual piano key (white or black)
- Handles mouse events for user interaction
- Plays MIDI notes when clicked
- Manages the visual state of the key (active/inactive)

#### ScaleGenerator
- Contains the music theory logic for generating scales
- Stores formulas for different scale types
- Calculates which notes belong in a specific scale based on the root note and scale type

#### MidiPlayer
- Provides an interface to the Java MIDI system
- Plays notes with realistic piano sounds
- Manages MIDI resources efficiently

### Key Features and Design Choices

1. **Efficient Key Management**: The application maintains a map of keys for quick access and updates
2. **CSS Styling**: UI elements use CSS for consistent and attractive styling
3. **Responsive Design**: The layout adapts to window size changes
4. **MIDI Resource Management**: MIDI resources are properly initialized and cleaned up
5. **Educational Feedback**: The application displays the notes in the selected scale for educational purposes
6. **Synchronous Key Refreshing**: All keys are refreshed simultaneously for consistent visual feedback

## Future Enhancements

- Fix bugs in displaying the scales
- Add support for chord visualisation
- Include a practice mode with scale exercises
- Add keyboard shortcuts for navigation
- Implement MIDI file export for created scales
- Expand the keyboard to cover more octaves
- Add more scale types and customizations
