package es.usj.crypto.enigma;

import es.usj.crypto.enigma.constant.ReflectorConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Represents the reflector in the Enigma machine.
 *
 * The reflector pairs each character with another character. A character cannot be reflected into itself.
 * This component ensures that when a character passes through the rotors, it is reflected back through the rotors in the reverse direction.
 */
public class Reflector {

    // Stores the character-to-character mapping for the reflector
    private final Map<Character, Character> mapping;

    /**
     * Constructs the Reflector with the provided configuration.
     *
     * @param reflectorConfiguration The reflector pairing configuration for the 26 characters of the ALPHABET.
     */
    public Reflector(ReflectorConfiguration reflectorConfiguration) {

        String input = reflectorConfiguration.getSequence();

        // Validate that each character from the ALPHABET appears exactly once in the configuration
        for (int i = 0; i < Machine.ALPHABET.length(); i++) {
            char c = Machine.ALPHABET.charAt(i);
            long count = input.chars().filter(ch -> ch == c).count();
            assertEquals("Character " + c + " is expected to appear only 1 time", 1, count);
        }

        // Initialize the reflector's character mappings based on the input
        mapping = new HashMap<>();
        for (String reflection : input.split(":")) {
            // Validate that each reflection mapping contains exactly 2 characters
            assertEquals("Mapping " + reflection + " should contain 2 characters", 2, reflection.length());
            mapping.put(reflection.charAt(0), reflection.charAt(1));
            mapping.put(reflection.charAt(1), reflection.charAt(0));
        }
    }

    /**
     * Reflects a character according to the reflector's pairing.
     *
     * If the input character is not part of the ALPHABET (e.g., a space), it is returned unchanged.
     *
     * @param input The character to be reflected.
     * @return The reflected character, or the input character if it is not part of the ALPHABET.
     */
    public char getReflection(char input) {
        return (mapping.get(input) == null ? input : mapping.get(input));
    }

}
