package es.usj.crypto;

import es.usj.crypto.utils.EnigmaManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


public class Main {

    private static final int MIN_ROTOR = 1;
    private static final int MAX_ROTOR = 5;
    private static final char MIN_POSITION = 'A';
    private static final char MAX_POSITION = 'Z';
    private static final int PLUGBOARD_SIZE = 10; // Size of the plugs for the plugboard
    private static final int FIXED_PLUGBOARD_SIZE = 4   ; // Size of the fixed part of the plugboard
    private static final int TOP_NUMBER = 5000; // Number of top configurations to display

public static void main(String[] args) {
    Path plainTextPath = Paths.get("data/plain_text.txt");
    Path cipherTextPath = Paths.get("data/cipher.txt");

    int iterations = 20;
    int foundCount = 0;

    for (int i = 0; i < iterations; i++) {
        // Cipher the plain text and get the initial configuration
        int[] initialRotorTypes = new int[3];
        char[] initialRotorPositions = new char[3];
        String initialPlugboard = cipherInitialText(plainTextPath, cipherTextPath, initialRotorTypes, initialRotorPositions);

        // Display the configuration used
        System.out.printf("Iteration %d: Configuration used for ciphering: Rotors=%s, Positions=%s, Plugboard=%s%n",
                i + 1, arrayToString(initialRotorTypes), arrayToString(initialRotorPositions), initialPlugboard);

        // Try to decipher the text
        boolean found = generateAndTestConfigs(cipherTextPath.toString(), initialRotorTypes, initialRotorPositions, initialPlugboard);

        if (found) {
            foundCount++;
        } else {
            System.out.printf("Configuration not found in iteration %d. Total found: %d%n", i + 1, foundCount);
            break;
        }
    }

    System.out.printf("Total configurations found: %d out of %d iterations.%n", foundCount, iterations);
}

    static String cipherInitialText(Path plainTextFile, Path cipherFile, int[] rotorTypes, char[] rotorPositions) {
        EnigmaManager manager = new EnigmaManager(plainTextFile);
        Random random = new Random();

        List<Integer> rotorTypeList = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Collections.shuffle(rotorTypeList, random);
        rotorTypes[0] = rotorTypeList.get(0);
        rotorTypes[1] = rotorTypeList.get(1);
        rotorTypes[2] = rotorTypeList.get(2);

        rotorPositions[0] = (char) ('A' + random.nextInt(26));
        rotorPositions[1] = (char) ('A' + random.nextInt(26));
        rotorPositions[2] = (char) ('A' + random.nextInt(26));

        String plugboard = generatePlugboard();
        System.out.println("Plugboard: " + plugboard);

        String cipher = manager.cipherText(rotorTypes, rotorPositions, plugboard);
        try {
            Files.writeString(cipherFile, cipher);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        manager.shutdown();
        return plugboard;
    }

    static String generatePlugboard() {
        if (PLUGBOARD_SIZE == 0) {
            return "";
        }
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<Character> chars = new ArrayList<>();
        for (char c : alphabet.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        StringBuilder plugboard = new StringBuilder();
        for (int i = 0; i < PLUGBOARD_SIZE * 2; i += 2) {
            plugboard.append(chars.get(i)).append(chars.get(i + 1)).append(':');
        }
        return plugboard.substring(0, plugboard.length() - 1);
    }

    private static boolean generateAndTestConfigs(String inputFilePath, int[] originalRotorTypes, char[] originalRotorPositions, String originalPlugboard) {
        List<int[]> rotorCombinations = generateRotorCombinations();
        List<char[]> rotorPositions = generateRotorPositions();
        String fixedPlugboard = getFixedPlugboard(originalPlugboard);

        int totalConfigs = rotorCombinations.size() * rotorPositions.size();
        int[][] rotorTypes = new int[totalConfigs][3];
        char[][] rotorPos = new char[totalConfigs][3];
        String[] plugboardConfigs = new String[totalConfigs];

        int index = 0;
        for (int[] combination : rotorCombinations) {
            for (char[] position : rotorPositions) {
                rotorTypes[index] = combination;
                rotorPos[index] = position;
                plugboardConfigs[index] = fixedPlugboard;
                index++;
            }
        }

        EnigmaManager manager = new EnigmaManager(Paths.get(inputFilePath));
        List<EnigmaManager.ConfigurationScore> scores = manager.processConfigurations(rotorTypes, rotorPos, plugboardConfigs, true);
        manager.shutdown();

        // Collect and display the top 10 scores with configurations
        List<EnigmaManager.ConfigurationScore> topScores = scores.stream()
                .sorted(Comparator.comparingDouble(EnigmaManager.ConfigurationScore::getScore).reversed())
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("Top 10 Scores with Configurations:");
        for (int i = 0; i < topScores.size(); i++) {
            EnigmaManager.ConfigurationScore cs = topScores.get(i);
            System.out.printf("%d: Score=%f, Rotors=%s, Positions=%s, Plugboard=%s%n",
                    i + 1, cs.getScore(), arrayToString(cs.getRotorTypes()), arrayToString(cs.getRotorPositions()), cs.getPlugboard());
        }

        // Check if the original configuration is in the top TOP_NUMBER
        boolean found = scores.stream()
                .sorted(Comparator.comparingDouble(EnigmaManager.ConfigurationScore::getScore).reversed())
                .limit(TOP_NUMBER)
                .anyMatch(cs -> // Check without plugboard
                    arrayEquals(cs.getRotorTypes(), originalRotorTypes) &&
                    arrayEquals(cs.getRotorPositions(), originalRotorPositions)
                );

        if (found) {
            System.out.println("The original configuration was found in the top TOP_NUMBER.");
        } else {
            System.out.println("The original configuration was not found in the top TOP_NUMBER.");
        }
        return found;
    }

    private static String getFixedPlugboard(String originalPlugboard) {
        if (FIXED_PLUGBOARD_SIZE == 0) {
            return "";
        }
        String[] pairs = originalPlugboard.split(":");
        StringBuilder fixedPlugboard = new StringBuilder();
        for (int i = 0; i < FIXED_PLUGBOARD_SIZE && i < pairs.length; i++) {
            fixedPlugboard.append(pairs[i]).append(':');
        }
        return fixedPlugboard.substring(0, fixedPlugboard.length() - 1);
    }

    private static List<int[]> generateRotorCombinations() {
        List<int[]> combinations = new ArrayList<>();
        for (int i = MIN_ROTOR; i <= MAX_ROTOR; i++) {
            for (int j = MIN_ROTOR; j <= MAX_ROTOR; j++) {
                if (j == i) continue;
                for (int k = MIN_ROTOR; k <= MAX_ROTOR; k++) {
                    if (k == i || k == j) continue;
                    combinations.add(new int[]{i, j, k});
                }
            }
        }
        return combinations;
    }

    private static List<char[]> generateRotorPositions() {
        List<char[]> positions = new ArrayList<>();
        for (char i = MIN_POSITION; i <= MAX_POSITION; i++) {
            for (char j = MIN_POSITION; j <= MAX_POSITION; j++) {
                for (char k = MIN_POSITION; k <= MAX_POSITION; k++) {
                    positions.add(new char[]{i, j, k});
                }
            }
        }
        return positions;
    }

    private static String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int i : array) {
            sb.append(i).append(" ");
        }
        return sb.toString().trim();
    }

    private static String arrayToString(char[] array) {
        StringBuilder sb = new StringBuilder();
        for (char c : array) {
            sb.append(c).append(" ");
        }
        return sb.toString().trim();
    }

    private static boolean arrayEquals(int[] a, int[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private static boolean arrayEquals(char[] a, char[] b) {
        if (a.length != b.length) return false;
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }
}