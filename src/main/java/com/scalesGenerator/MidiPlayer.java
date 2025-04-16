package com.scalesGenerator;

import javax.sound.midi.*;

public class MidiPlayer {
    private static Synthesizer synthesizer;
    private static MidiChannel channel;
    private static final int VOLUME = 80; // Default volume (0-127)

    static {
        try {
            // Initialize the synthesizer
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();

            // Get the MIDI channels
            MidiChannel[] channels = synthesizer.getChannels();

            // Use channel 0 (piano) by default
            channel = channels[0];

            // Set the instrument to piano (program change 0)
            channel.programChange(0);
        } catch (MidiUnavailableException e) {
            System.err.println("Error initializing MIDI system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void playNote(int noteNumber) {
        if (channel != null) {
            // Play the note
            channel.noteOn(noteNumber, VOLUME);

            // Schedule note off after a short duration
            new Thread(() -> {
                try {
                    Thread.sleep(300); // Note duration in milliseconds
                    channel.noteOff(noteNumber);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    // Method to clean up resources when application closes
    public static void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }
}