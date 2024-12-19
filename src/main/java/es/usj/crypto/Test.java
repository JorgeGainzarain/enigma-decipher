package es.usj.crypto;

import es.usj.crypto.EnigmaConfig;
import es.usj.crypto.utils.EnigmaManager;

import java.nio.file.Path;
import java.util.*;

public class Test {
    public static void main(String[] args) {

        // Create an EnigmaManager instance
        EnigmaManager manager = new EnigmaManager(Path.of("data", "plain_text.txt"));
        EnigmaConfig config = manager.cipherInitialText(10);
        System.out.println("Initial Configuration: " + config);

        EnigmaConfig config1 = new EnigmaConfig(config.getRotorTypes(), config.getRotorPositions(), config.getFixedPlugboard(0));

        // Second config with empty plugboard and incorrect rotor types and positions (Ensure it's different from the original)
        int[] rotorTypes = new int[config.getRotorTypes().length];
        for (int i = 0; i < rotorTypes.length; i++) {
            rotorTypes[i] = (config.getRotorTypes()[i] + 1) % 5;
        }
        char[] rotorPositions = new char[config.getRotorPositions().length];
        for (int i = 0; i < rotorPositions.length; i++) {
            rotorPositions[i] = (char) (config.getRotorPositions()[i] + 1);
            if (rotorPositions[i] > 'Z') {
                rotorPositions[i] = 'A';
            }
        }
        EnigmaConfig config2 = new EnigmaConfig(rotorTypes, rotorPositions, config.getFixedPlugboard(0));

        String deciphered = manager.process(config);
        System.out.println(("Original Configuration: " + config));
        System.out.println("Deciphered Text Original: " + deciphered);
        System.out.println("Original Score: " + config.getScore());


        String deciphered1 = manager.process(config1);
        System.out.println("\nConfiguration 1: " + config1);
        System.out.println("Deciphered Text 1: " + deciphered1);
        System.out.println("Score 1: " + config1.getScore());

        String deciphered2 = manager.process(config2);
        System.out.println("\nConfiguration 2: " + config2);
        System.out.println("Deciphered Text 2: " + deciphered2);
        System.out.println("Score 2: " + config2.getScore());

        // Shutdown the manager
        manager.shutdown();
    }
}