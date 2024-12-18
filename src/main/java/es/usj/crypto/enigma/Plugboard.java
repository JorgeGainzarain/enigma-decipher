package es.usj.crypto.enigma;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Represents the plugboard (Steckerbrett) component in the Enigma machine.
 *
 * The plugboard is responsible for swapping pairs of characters before and after they pass through the rotors.
 * Each character can only be swapped with another character, and a character can never be replaced by itself.
 * A standard machine was equipped with a set of 10 cables, allowing up to 10 character pairings.
 * Characters without a pairing in the plugboard pass through unchanged.
 */
public class Plugboard {

    // The number of plugboard pairings allowed
    private static final int PLUGBOARD_PAIRINGS = 10;

    // Stores the character-to-character mapping for the plugboard
    private final Map<Character, Character> mapping;

    /**
     * Constructs the plugboard with the provided pairings.
     *
     * @param input A string representing 10 pairs of characters separated by ':'. Each pair swaps the two characters.
     *              Characters without a pairing pass through unchanged.
     */
    public Plugboard(String input) {

        mapping = new HashMap<>();

        if (!input.isEmpty()) {
            // Validate that no character from the ALPHABET appears more than once in the input
            for (int i = 0; i < Machine.ALPHABET.length(); i++) {
                char c = Machine.ALPHABET.charAt(i);
                long count = input.chars().filter(ch -> ch == c).count();
                assertTrue("Character " + c + " is expected to be 0 or 1 time, not " + count, count == 0 || count == 1);
            }

            // Validate that exactly 10 pairs of characters are provided
            //assertEquals("Plugboard accepts exactly " + PLUGBOARD_PAIRINGS + " mappings", PLUGBOARD_PAIRINGS, input.split(":").length);

            // Populate the character-to-character mappings
            for (String reflection : input.split(":")) {
                assertEquals("Mapping " + reflection + " should contain 2 characters", 2, reflection.length());
                mapping.put(reflection.charAt(0), reflection.charAt(1));
                mapping.put(reflection.charAt(1), reflection.charAt(0));
            }
        }
    }

    /**
     * Swaps a character according to the plugboard's pairings.
     *
     * If the input character is not part of the ALPHABET, or if it is not included in the plugboard's settings,
     * the character is returned unchanged.
     *
     * @param input The character to be swapped.
     * @return The swapped character, or the original character if it is not part of the ALPHABET or not paired.
     */
    public char getPlug(char input) {
        return mapping.get(input) == null ? input : mapping.get(input);
    }

}
