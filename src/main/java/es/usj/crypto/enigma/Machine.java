package es.usj.crypto.enigma;

import es.usj.crypto.EnigmaConfig;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Represents an Enigma machine, which takes a plaintext string and returns a corresponding ciphertext string.
 *
 * The machine operates by performing the following steps on each character of the plaintext:
 * <ul>
 *   <li>Apply Plugboard substitution (if the character is not mapped, the same input character is used).</li>
 *   <li>Apply Rotor substitution from right to left (through the right, middle, and left rotors).</li>
 *   <li>Apply Reflector substitution (the character is reflected).</li>
 *   <li>Apply Rotor substitution from left to right (through the left, middle, and right rotors).</li>
 *   <li>Apply Plugboard substitution again (if the character is not mapped, the same input character is used).</li>
 * </ul>
 *
 * After processing each character, the machine updates the rotor positions:
 * <ul>
 *   <li>The right rotor always rotates.</li>
 *   <li>The middle and left rotors rotate only if the rotor to their right is in the notch position.</li>
 * </ul>
 *
 * Encryption follows this flow:
 * <pre>
 * plainText >>
 *     plugboard >>
 *         right rotor >> middle rotor >> left rotor >>
 *             reflector >>
 *         left rotor >> middle rotor >> right rotor >>
 *     plugboard >>
 * cipherText
 * </pre>
 */
public class Machine {
    //static final RotorPool rotorPool = new RotorPool();

    // The accepted input alphabet (uppercase English letters)
    public static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // Components of the Enigma machine
    public Plugboard plugboard;
    private Rotor rightRotor;
    private Rotor middleRotor;
    private Rotor leftRotor;
    private final Reflector reflector;

    /**
     * Constructs an Enigma machine with the specified components.
     *
     * No rotor configuration repetition is allowed; each rotor must have a unique configuration.
     *
     * @param plugboard Pair mapping for the alphabet characters (only 10 pairings are allowed).
     * @param rightRotor The rotor to be placed in the right position.
     * @param middleRotor The rotor to be placed in the middle position.
     * @param leftRotor The rotor to be placed in the left position.
     * @param reflector Pair mapping for the alphabet characters (13 pairings are required for the reflector).
     */
    public Machine(
            Plugboard plugboard,
            Rotor rightRotor,
            Rotor middleRotor,
            Rotor leftRotor,
            Reflector reflector) {
        assertTrue("Each rotor configuration should be different",
                !leftRotor.equals(rightRotor) && !rightRotor.equals(middleRotor) && !middleRotor.equals(rightRotor));
        this.plugboard = plugboard;
        this.leftRotor = leftRotor;
        this.middleRotor = middleRotor;
        this.rightRotor = rightRotor;
        this.reflector = reflector;
    }

    // copy constructor
    public Machine(Machine machine) {
        this.plugboard = machine.plugboard;
        this.leftRotor = machine.leftRotor;
        this.middleRotor = machine.middleRotor;
        this.rightRotor = machine.rightRotor;
        this.reflector = machine.reflector;
    }

    /**
     * Ciphers a given plaintext string into ciphertext.
     *
     * The input plaintext must consist of characters from the machine's ALPHABET and spaces. Non-alphabet characters are
     * not processed.
     *
     * @param plainText A string containing the plaintext (letters and spaces) to be encrypted.
     * @return The ciphertext resulting from the encryption process.
     */
    public String getCipheredText(String plainText) {

        // Convert plaintext to uppercase
        plainText = plainText.toUpperCase(Locale.ROOT);

        Pattern pattern = Pattern.compile("[A-Z\\t\\n\\f\\r\\s]+");
        Matcher matcher = pattern.matcher(plainText);
        StringBuilder filteredString = new StringBuilder();

        while (matcher.find()) {
            filteredString.append(matcher.group());
        }

        plainText = filteredString.toString();

        assertTrue("Plaintext contains characters not in the ALPHABET or not considered blank space", plainText.matches("[A-Z\\t\\n\\f\\r\\s]+"));

        StringBuilder cipherText = new StringBuilder();

        for (char input : plainText.toCharArray()) {

            // Plugboard substitution
            char output = plugboard.getPlug(input);

            // Update the rotor positions after encrypting a character
            if (ALPHABET.indexOf(input) >= 0) {
                rightRotor.update(null);
                middleRotor.update(rightRotor);
                leftRotor.update(middleRotor);
            }

            // Apply rotor substitution (right-to-left)
            output = rightRotor.forward(output);
            output = middleRotor.forward(output);
            output = leftRotor.forward(output);

            // Apply reflector substitution
            output = reflector.getReflection(output);

            // Apply rotor substitution (left-to-right)
            output = leftRotor.backward(output);
            output = middleRotor.backward(output);
            output = rightRotor.backward(output);

            // Apply plugboard substitution again
            output = plugboard.getPlug(output);

            // Append the ciphered character to the result
            cipherText.append(output);
        }

        return cipherText.toString();
    }

    public void rotateRotors() {
        rightRotor.update(null);
        middleRotor.update(rightRotor);
        leftRotor.update(middleRotor);
    }

    public void rotateRotors(int times) {
        for (int i = 0; i < times; i++) {
            rotateRotors();
        }
    }

    public char cipherCharacter(char c) {
        // Plugboard substitution
        char output = plugboard.getPlug(c);

        // DON'T Update the rotor positions after encrypting a character

        // Apply rotor substitution (right-to-left)
        output = rightRotor.forward(output);
        output = middleRotor.forward(output);
        output = leftRotor.forward(output);

        // Apply reflector substitution
        output = reflector.getReflection(output);

        // Apply rotor substitution (left-to-right)
        output = leftRotor.backward(output);
        output = middleRotor.backward(output);
        output = rightRotor.backward(output);

        // Return the character
        return output;
    }

    public void applyConfig(EnigmaConfig config) {
        EnigmaApp app = new EnigmaApp();
        rightRotor = app.createRotor(config.getRotorTypes()[0], config.getRotorPositions()[0]);
        middleRotor = app.createRotor(config.getRotorTypes()[1], config.getRotorPositions()[1]);
        leftRotor = app.createRotor(config.getRotorTypes()[2], config.getRotorPositions()[2]);
    }

    public void setPlugboard(String plugboardSettings) {
        this.plugboard = new Plugboard(plugboardSettings);
    }

    public void setPlugboard(Map<Character, Character> plugboardSettings) {
        StringBuilder plugboard = new StringBuilder();
        for (Map.Entry<Character, Character> entry : plugboardSettings.entrySet()) {
            plugboard.append(entry.getKey()).append(entry.getValue()).append(":");
        }
        this.plugboard = new Plugboard(plugboard.toString());
    }

    public void setRotors(int[] rotorTypes, char[] rotorPositions) {
        this.leftRotor.setConfig(rotorTypes[0], rotorPositions[0]);
        this.middleRotor.setConfig(rotorTypes[1], rotorPositions[1]);
        this.rightRotor.setConfig(rotorTypes[2], rotorPositions[2]);
    }

}
