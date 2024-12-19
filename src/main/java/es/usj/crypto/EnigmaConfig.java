package es.usj.crypto;

import java.util.Arrays;

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

    public void setPlugboard(String plugboard) {
        this.plugboard = plugboard;
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