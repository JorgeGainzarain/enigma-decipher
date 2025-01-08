package es.usj.crypto.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the configuration of an Enigma machine, including rotor types, rotor positions, and plugboard settings.
 * Note: The rotorTypes and rotorPositions go from left to right, 0 being the leftmost rotor.
 */
public class EnigmaConfig {
    private byte[] rotorTypes;
    private byte[] rotorPositions;
    private String plugboard;
    private double score;

    /**
     * Constructs an EnigmaConfig with the specified rotor types, rotor positions, and plugboard settings.
     *
     * @param rotorTypes    An array of integers representing the types of rotors.
     * @param rotorPositions An array of characters representing the initial positions of the rotors.
     * @param plugboard     A string representing the plugboard settings.
     */
    public EnigmaConfig(int[] rotorTypes, char[] rotorPositions, String plugboard) {
        this.rotorTypes = new byte[rotorTypes.length];
        for (int i = 0; i < rotorTypes.length; i++) {
            this.rotorTypes[i] = (byte) rotorTypes[i];
        }
        this.rotorPositions = new byte[rotorPositions.length];
        for (int i = 0; i < rotorPositions.length; i++) {
            this.rotorPositions[i] = (byte) rotorPositions[i];
        }
        this.plugboard = plugboard;
        this.score = 0.0;
    }

    /**
     * Clone constructor.
     *
     * @param config The EnigmaConfig to clone.
     */
    public EnigmaConfig(EnigmaConfig config) {
        this.rotorTypes = new byte[config.rotorTypes.length];
        System.arraycopy(config.rotorTypes, 0, this.rotorTypes, 0, config.rotorTypes.length);
        this.rotorPositions = new byte[config.rotorPositions.length];
        System.arraycopy(config.rotorPositions, 0, this.rotorPositions, 0, config.rotorPositions.length);
        this.plugboard = config.plugboard;
        this.score = config.score;
    }

    /**
     * Gets the rotor types.
     *
     * @return An array of integers representing the rotor types.
     */
    public int[] getRotorTypes() {
        int[] result = new int[rotorTypes.length];
        for (int i = 0; i < rotorTypes.length; i++) {
            result[i] = rotorTypes[i];
        }
        return result;
    }

    /**
     * Sets the rotor types.
     *
     * @param rotorTypes An array of integers representing the rotor types.
     */
    public void setRotorTypes(int[] rotorTypes) {
        this.rotorTypes = new byte[rotorTypes.length];
        for (int i = 0; i < rotorTypes.length; i++) {
            this.rotorTypes[i] = (byte) rotorTypes[i];
        }
    }

    /**
     * Gets the rotor positions.
     *
     * @return An array of characters representing the rotor positions.
     */
    public char[] getRotorPositions() {
        char[] result = new char[rotorPositions.length];
        for (int i = 0; i < rotorPositions.length; i++) {
            result[i] = (char) rotorPositions[i];
        }
        return result;
    }

    /**
     * Sets the rotor positions.
     *
     * @param rotorPositions An array of characters representing the rotor positions.
     */
    public void setRotorPositions(char[] rotorPositions) {
        this.rotorPositions = new byte[rotorPositions.length];
        for (int i = 0; i < rotorPositions.length; i++) {
            this.rotorPositions[i] = (byte) rotorPositions[i];
        }
    }

    /**
     * Gets the plugboard settings.
     *
     * @return A string representing the plugboard settings.
     */
    public String getPlugboard() {
        return plugboard;
    }

    /**
     * Sets the plugboard settings.
     *
     * @param plugboard A string representing the plugboard settings.
     */
    public void setPlugboard(String plugboard) {
        if (Objects.equals(plugboard, "")) {
            this.plugboard = "";
            return;
        }
        if (containsDuplicateCharacters(plugboard)) {
            throw new AssertionError("Plugboard contains duplicate characters");
        }
        this.plugboard = plugboard;
    }

    /**
     * Adds a plug to the plugboard.
     *
     * @param plug The plug to add.
     * @return {@code true} if the plug was added, otherwise {@code false}.
     * @throws AssertionError if the plugboard length is not 2 or if the plugboard contains duplicate characters. (This is used to check for invalid plugboard settings)
     * Workflow:
     * - If the plugboard already contains the plug, return true. (This is not invalid, but it shouldnt be added again)
     * - If the plugboard contains duplicate characters with different mappings than the new plug, throw an AssertionError. (This means the plugboard is invalid)
     * - If the plugboard is not empty, add a colon separator.
     * - Add the plug to the plugboard.
     * - Return true.
     */
    public boolean addPlug(String plug) {
        if (plug.length() != 2) {
            throw new AssertionError("Plugboard length must be 2");
        }
        String pair1 = plug;
        String pair2 = "" + plug.charAt(1) + plug.charAt(0);
        if (this.plugboard.contains(pair1) || this.plugboard.contains(pair2)) {
            return true;
        }
        String newPlugboard1 = this.plugboard + plug.charAt(0);
        String newPlugboard2 = this.plugboard + plug.charAt(1);
        if (containsDuplicateCharacters(newPlugboard1) || containsDuplicateCharacters(newPlugboard2)) {
            throw new AssertionError("Plugboard contains duplicate characters");
        }
        if (plug.charAt(0) == plug.charAt(1)) {
            return true;
        }
        if (!this.plugboard.isEmpty()) {
            this.plugboard += ":";
        }
        this.plugboard += plug;
        return true;
    }

    /**
     * Checks if the plugboard contains duplicate characters.
     *
     * @param plugboard A string representing the plugboard settings.
     * @return {@code true} if the plugboard contains duplicate characters, otherwise {@code false}.
     */
    public boolean containsDuplicateCharacters(String plugboard) {
        Set<Character> seen = new HashSet<>();
        for (char c : plugboard.toCharArray()) {
            if (c != ':' && !seen.add(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the score.
     *
     * @return The score.
     */
    public double getScore() {
        return score;
    }

    /**
     * Sets the score.
     *
     * @param score The score to set.
     */
    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "EnigmaConfig{" +
                "rotorTypes=" + Arrays.toString(getRotorTypes()) +
                ", rotorPositions=" + Arrays.toString(getRotorPositions()) +
                ", plugboard='" + plugboard + '\'' +
                ", score=" + score +
                '}';
    }

    /**
     * Checks if this EnigmaConfig is equal to another EnigmaConfig, ignoring the plugboard settings.
     *
     * @param other The other EnigmaConfig to compare to.
     * @return {@code true} if the rotor types and rotor positions are equal, otherwise {@code false}.
     */
    public boolean equalsWithoutPlugboard(EnigmaConfig other) {
        if (this == other) return true;
        if (other == null) return false;
        return Arrays.equals(rotorTypes, other.rotorTypes) &&
                Arrays.equals(rotorPositions, other.rotorPositions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnigmaConfig that = (EnigmaConfig) o;
        return Arrays.equals(rotorTypes, that.rotorTypes) &&
                Arrays.equals(rotorPositions, that.rotorPositions) &&
                plugboard.equals(that.plugboard);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(rotorTypes);
        result = 31 * result + Arrays.hashCode(rotorPositions);
        result = 31 * result + plugboard.hashCode();
        return result;
    }
}