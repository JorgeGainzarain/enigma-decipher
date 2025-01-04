package es.usj.crypto;

import es.usj.crypto.enigma.AlphabetCircle;
import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Machine;
import es.usj.crypto.utils.ProgressBar;

import javax.swing.*;
import java.util.*;

public class Bombe {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Machine enigma;
    private final String ciphertext;
    private final String crib;
    private final Map<Character, List<Map.Entry<Character, Integer>>> letterConnections;
    private final List<String> steps;
    private final Set<Map<String, Object>> loopSteps;

    public Bombe(EnigmaConfig config, String ciphertext, String crib) {
        this.enigma = createMachine(config);
        this.ciphertext = ciphertext;
        this.crib = crib;
        this.steps = new ArrayList<>();
        this.loopSteps = new HashSet<>();
        this.letterConnections = buildMenu();
    }

    private Map<Character, List<Map.Entry<Character, Integer>>> buildMenu() {
        Map<Character, List<Map.Entry<Character, Integer>>> menu = new LinkedHashMap<>();

        for (int i = 0; i < crib.length(); i++) {
            char plainChar = crib.charAt(i);
            char cipherChar = ciphertext.charAt(i);
            int stepNumber = i + 1;

            menu.putIfAbsent(plainChar, new ArrayList<>());
            menu.putIfAbsent(cipherChar, new ArrayList<>());

            menu.get(plainChar).add(new AbstractMap.SimpleEntry<>(cipherChar, stepNumber));
            menu.get(cipherChar).add(new AbstractMap.SimpleEntry<>(plainChar, stepNumber));

            String step = "Step " + stepNumber + ": " + plainChar + " <-> " + cipherChar;
            steps.add(step);
            System.out.println(step);
        }

        return menu;
    }


    public Machine createMachine(EnigmaConfig config) {
        String[] args = {
                "--input=" + ciphertext,
                "--plugboard=" + config.getPlugboard(),
                "--left-rotor=" + config.getRotorTypes()[0],
                "--left-rotor-position=" + config.getRotorPositions()[0],
                "--middle-rotor=" + config.getRotorTypes()[1],
                "--middle-rotor-position=" + config.getRotorPositions()[1],
                "--right-rotor=" + config.getRotorTypes()[2],
                "--right-rotor-position=" + config.getRotorPositions()[2]
        };
        return new EnigmaApp().createMachine(args);
    }

    /*
    public void visualizeConnections() {
        JFrame frame = new JFrame("Bombe Connections Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.add(new AlphabetCircle(letterConnections));
        frame.setVisible(true);
    }

     */


    public static void main(String[] args) {
        EnigmaConfig config = new EnigmaConfig(
                new int[]{1, 2, 3},
                new char[]{'A', 'A', 'A'},
                ""
        );

        String ciphertext = "KAHYCFKDTCUSGH";
        String crib = "IDENTIFICATION";

        Bombe bombe = new Bombe(config, ciphertext, crib);

        //bombe.visualizeConnections();

        // Convert the map to a list of entries
        List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> sortedConnections = new ArrayList<>(bombe.letterConnections.entrySet());


        // Sort the list by the size of the value lists
        sortedConnections.sort((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()));


        // Print the sorted connections
        for (Map.Entry<Character, List<Map.Entry<Character, Integer>>> entry : sortedConnections) {
            char key = entry.getKey();
            List<Map.Entry<Character, Integer>> value = entry.getValue();
            System.out.println(key + " -> " + value);
        }


        System.out.println("-----------------");
        System.out.println("Start processing the connections");


        // Define a new config to store the mappings
        EnigmaConfig newConfig = new EnigmaConfig(config);

        char firstChar = sortedConnections.get(0).getKey();
        List<Map.Entry<Character, Integer>> firstCharConnections = sortedConnections.get(0).getValue();

        System.out.println("-----------------");
        System.out.println("Mappings for character " + firstChar);

        int numValid = 0;

        int total = 5 * 4 * 3 * 26 * 26 * 26;
        ProgressBar progressBar = new ProgressBar(total);

        // iterate over all the possible rotor configurations
        /*
        for (int L = 1; L <= 5; L++) {
            for (int M = 1; M <= 5; M++) {
                if (L == M) continue;
                for (int R = 1; R <= 5; R++) {
                    if (R == L || R == M) continue;
                    for (char LPos = 'A'; LPos <= 'Z'; LPos++) {
                        for (char MPos = 'A'; MPos <= 'Z'; MPos++) {
                            for (char RPos = 'A'; RPos <= 'Z'; RPos++) {
                                newConfig.setRotorTypes(new int[]{L, M, R});
                                newConfig.setRotorPositions(new char[]{LPos, MPos, RPos});

         */


                                char map = 'N'; // This will be a for iterating each character later....

                                // Make a deduction that the cipherChar maps to 'N'
                                newConfig.setPlugboard(firstChar + "" + map);

                                boolean validDeduction = true;

                                // Get the new mappings from this deduction
                                for (Map.Entry<Character, Integer> entry : firstCharConnections) {
                                    char cipherChar = entry.getKey();
                                    int stepNumber = entry.getValue();
                                    System.out.println("(X=" + stepNumber + "): \n" + firstChar + '☰' + map + " -> ?☰" + cipherChar);

                                    try {
                                        Machine currMachine = bombe.createMachine(newConfig);
                                        currMachine.rotateRotors(stepNumber);
                                        char newChar = currMachine.cipherCharacter(firstChar);
                                        System.out.println("" + firstChar + '☰' + map + " -> " + newChar + '☰' + cipherChar);
                                        newConfig.addPlug(newChar + "" + cipherChar);
                                    } catch (AssertionError e) {
                                        System.out.println("Incongruent mapping detected, discarding...");
                                        validDeduction = false;
                                        numValid++;
                                        break;
                                    }
                                }
                                System.out.println("Deduction is " + (validDeduction ? "valid" : "invalid"));
        /*
                                progressBar.add(1);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Number of valid deductions: " + numValid);

         */

        /*
        System.out.println("-----------------");
        System.out.println("Mappings I");

        char map1 = 'N';
        Machine machine = bombe.createMachine(config);
        machine.rotateRotors(1);
        char c1 = machine.cipherCharacter(map1);
        System.out.println("1 -> " + c1);

        Machine machine6 = bombe.createMachine(config);
        machine6.rotateRotors(6);
        char c6 = machine6.cipherCharacter(map1);
        System.out.println("6 -> " + c6);

        Machine machine8 = bombe.createMachine(config);
        machine8.rotateRotors(8);
        char c8 = machine8.cipherCharacter(map1);
        System.out.println("8 -> " + c8);

        Machine machine12 = bombe.createMachine(config);
        machine12.rotateRotors(12);
        char c12 = machine12.cipherCharacter(map1);
        System.out.println("12 -> " + c12);

        System.out.println("-----------------");
        System.out.println("Mappings K");

        char map2 = c1;
        Machine machine7 = bombe.createMachine(config);
        machine7.rotateRotors(7);
        char c7 = machine7.cipherCharacter(map2);
        System.out.println("7 -> " + c7);

        System.out.println("-----------------");
        System.out.println("Mappings D");

        char map3 = c8;
        Machine machine2 = bombe.createMachine(config);
        machine2.rotateRotors(2);
        char c2 = machine2.cipherCharacter(map3);
        System.out.println("2 -> " + c2);

        System.out.println("-----------------");
        System.out.println("Mappings G");

        char map4 = c2;

        Machine machine10 = bombe.createMachine(config);
        machine10.rotateRotors(10);
        char c13 = machine10.cipherCharacter(map4);
        System.out.println("10 -> " + c13);

         */
    }
}