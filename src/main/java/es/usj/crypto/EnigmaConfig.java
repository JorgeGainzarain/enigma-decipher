package es.usj.crypto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EnigmaConfig {
    private byte[] rotorTypes;
    private byte[] rotorPositions;
    private String plugboard;
    private double score;

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

    // Clone constructor
    public EnigmaConfig(EnigmaConfig config) {
        this.rotorTypes = new byte[config.rotorTypes.length];
        System.arraycopy(config.rotorTypes, 0, this.rotorTypes, 0, config.rotorTypes.length);
        this.rotorPositions = new byte[config.rotorPositions.length];
        System.arraycopy(config.rotorPositions, 0, this.rotorPositions, 0, config.rotorPositions.length);
        this.plugboard = config.plugboard;
        this.score = config.score;
    }

    public int[] getRotorTypes() {
        int[] result = new int[rotorTypes.length];
        for (int i = 0; i < rotorTypes.length; i++) {
            result[i] = rotorTypes[i];
        }
        return result;
    }

    public void setRotorTypes(int[] rotorTypes) {
        this.rotorTypes = new byte[rotorTypes.length];
        for (int i = 0; i < rotorTypes.length; i++) {
            this.rotorTypes[i] = (byte) rotorTypes[i];
        }
    }

    public char[] getRotorPositions() {
        char[] result = new char[rotorPositions.length];
        for (int i = 0; i < rotorPositions.length; i++) {
            result[i] = (char) rotorPositions[i];
        }
        return result;
    }

    public void setRotorPositions(char[] rotorPositions) {
        this.rotorPositions = new byte[rotorPositions.length];
        for (int i = 0; i < rotorPositions.length; i++) {
            this.rotorPositions[i] = (byte) rotorPositions[i];
        }
    }

    public String getPlugboard() {
        return plugboard;
    }

    public char findMapping(char c) {
        String[] pairs = plugboard.split(":");
        for (String pair : pairs) {
            if (pair.charAt(0) == c) {
                return pair.charAt(1);
            }
            else if (pair.charAt(1) == c) {
                return pair.charAt(0);
            }
        }
        return c;
    }

    public void setPlugboard(String plugboard) {
        if (Objects.equals(plugboard, "")) {
            this.plugboard = "";
            return;
        }
        if (containsDuplicateCharacters(plugboard)) {
            throw new AssertionError("Plugboard contains duplicate characters");
        }
        else  if (plugboard.charAt(0) == plugboard.charAt(1)) {
            return;
        }
        this.plugboard = plugboard;
    }

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
        else if (plug.charAt(0) == plug.charAt(1)) {
            return true;
        }
        if (!this.plugboard.isEmpty()) {
            this.plugboard += ":";
        }
        this.plugboard += plug;
        return true;
    }

    public boolean containsDuplicateCharacters(String plugboard) {
        Set<Character> seen = new HashSet<>();
        for (char c : plugboard.toCharArray()) {
            if (c != ':' && !seen.add(c)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        int[] rotorTypes = {1, 2, 3};
        char[] rotorPositions = {'A', 'A', 'A'};
        String plugboard = "";

        EnigmaConfig config = new EnigmaConfig(rotorTypes, rotorPositions, plugboard);
        config.setPlugboard("AB:CD:EF");
        config.addPlug("FF");

        System.out.println(config);
    }

    public double getScore() {
        return score;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnigmaConfig that = (EnigmaConfig) o;
        return Arrays.equals(rotorTypes, that.rotorTypes) &&
                Arrays.equals(rotorPositions, that.rotorPositions) &&
                plugboard.equals(that.plugboard);
    }

    public boolean equalsWithoutPlugboard(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnigmaConfig that = (EnigmaConfig) o;
        return Arrays.equals(rotorTypes, that.rotorTypes) &&
                Arrays.equals(rotorPositions, that.rotorPositions);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(rotorTypes);
        result = 31 * result + Arrays.hashCode(rotorPositions);
        result = 31 * result + plugboard.hashCode();
        return result;
    }

    public String getFixedPlugboard(int FIXED_PLUGBOARD_SIZE) {
        if (FIXED_PLUGBOARD_SIZE == 0) {
            return "";
        }
        String[] pairs = plugboard.split(":");
        StringBuilder fixedPlugboard = new StringBuilder();
        for (int i = 0; i < FIXED_PLUGBOARD_SIZE && i < pairs.length; i++) {
            fixedPlugboard.append(pairs[i]).append(':');
        }
        return fixedPlugboard.substring(0, fixedPlugboard.length() - 1);
    }
}