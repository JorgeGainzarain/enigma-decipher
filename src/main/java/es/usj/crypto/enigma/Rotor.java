package es.usj.crypto.enigma;

import es.usj.crypto.enigma.constant.RotorConfiguration;

import java.util.Objects;

import static es.usj.crypto.enigma.Machine.ALPHABET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Represents a rotor in the Enigma machine.
 *
 * Each Rotor includes:
 * - A 26-character sequence for the ring, containing every character from the ALPHABET without repetitions.
 * - The Notch position, which triggers the rotation of the adjacent rotor.
 * - The Rotor Position, the initial character of the ring sequence, is set to one character from the ALPHABET.
 */
public class Rotor {

    // 26-character sequence including ALPHABET characters in random order with no repetition
    private String ringSequence;
    // Position of the notch in the rotor (enables rotation of the rotor to the left)
    private final char notch;
    // Initial position of the rotor
    private final char rotorPosition;

    /**
     * Constructs a Rotor with the provided configuration and starting position.
     *
     * @param rotorConfiguration Contains the ring sequence and notch position.
     * @param rotorPosition The initial position of the rotor (A-Z).
     */
    public Rotor(RotorConfiguration rotorConfiguration, char rotorPosition) {

        // Validate rotor position is within A-Z
        assertTrue("Initial position should be A to Z", ALPHABET.indexOf(rotorPosition) != -1);
        this.rotorPosition = rotorPosition;

        // Validate that the ring sequence contains each character from the ALPHABET exactly once
        String input = rotorConfiguration.getRingSequence();
        for (int i = 0; i < ALPHABET.length(); i++) {
            char c = ALPHABET.charAt(i);
            long count = input.chars().filter(ch -> ch == c).count();
            assertEquals("Character " + c + " is expected to appear exactly 1 time", 1, count);
        }
        this.ringSequence = input;

        // Rotate the rotor to the initial rotor position
        while (this.ringSequence.charAt(0) != this.rotorPosition) {
            this.ringSequence = rotate(this.ringSequence);
        }

        // Validate notch position is within A-Z
        assertTrue("Notch position should be A to Z", ALPHABET.indexOf(rotorConfiguration.getNotch()) != -1);
        this.notch = rotorConfiguration.getNotch();
    }

    /**
     * Substitutes a character when passing through the rotor from left to right.
     *
     * @param c The plain character to be substituted.
     * @return The substituted character.
     */
    public char forward(char c) {
        int index = ALPHABET.indexOf(c);
        if (index >= 0) {
            return ringSequence.charAt(index);
        } else {
            return c;
        }
    }

    /**
     * Substitutes a character when passing through the rotor from right to left.
     *
     * @param c The plain character to be substituted.
     * @return The substituted character.
     */
    public char backward(char c) {
        int index = ringSequence.indexOf(c);
        if (index >= 0) {
            return ALPHABET.charAt(index);
        } else {
            return c;
        }
    }

    /**
     * Rotates the rotor if the rotor to its right is at the notch position.
     *
     * @param rightRotor The rotor immediately to the right of this rotor.
     */
    public void update(Rotor rightRotor) {
        if (rightRotor == null || rightRotor.ringSequence.charAt(0) == rightRotor.notch) {
            ringSequence = rotate(ringSequence);
        }
    }

    /**
     * Rotates the ring sequence of the rotor by one position.
     *
     * @param original The current ring sequence of the rotor.
     * @return The rotated ring sequence.
     */
    private static String rotate(String original) {
        return original.substring(original.length() - 1) + original.substring(0, original.length() - 1);
    }

    /**
     * Checks if this rotor is equal to another rotor based on the ring sequence, notch, and rotor position.
     *
     * @param o The object to be compared with this rotor.
     * @return {@code true} if the objects are equal, otherwise {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rotor rotor = (Rotor) o;
        return rotorPosition == rotor.rotorPosition &&
                Objects.equals(ringSequence, rotor.ringSequence) &&
                Objects.equals(notch, rotor.notch);
    }

    /**
     * Returns the hash code for this rotor, based on the ring sequence, notch, and rotor position.
     *
     * @return The hash code of this rotor.
     */
    @Override
    public int hashCode() {
        return Objects.hash(ringSequence, notch, rotorPosition);
    }
}
