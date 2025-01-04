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


                                char map = 'N'; // This will be a for iterating each character later....

                                // Make a deduction that the cipherChar maps to 'N'
                                newConfig.setPlugboard(firstChar + "" + map);

                                List<Character> testedChars = new ArrayList<>();
                                testedChars.add(firstChar);

                                List<char[]> deductedMappings = new ArrayList<>();

                                boolean validDeduction = true;

                                validDeduction = findMappingsFromDeduction(
                                        firstCharConnections,
                                        firstChar,
                                        map,
                                        bombe,
                                        newConfig,
                                        deductedMappings);
                                //System.out.println("Deduction is " + (validDeduction ? "valid" : "invalid"));


                                // At this point, we already discarded some of the invalid deductions, however this is still not enough
                                // Now, for each mapping made, we have to explore its connections and make sure the new mappings are valid too

                                List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> finalConnections = new ArrayList<>();

                                //System.out.println("Plugs to test next.");
                                for (char[] plug : deductedMappings) {
                                    char char1 = plug[0];
                                    char char2 = plug[1];
                                    //System.out.println(plug);

                                    // Find the connections for each character
                                    List<Map.Entry<Character, Integer>> connections1 = bombe.letterConnections.get(char1);
                                    List<Map.Entry<Character, Integer>> connections2 = char1 == char2 ? connections1 : bombe.letterConnections.get(char2);

                                    if (connections1 == null) connections1 = new ArrayList<>();
                                    if (connections2 == null) connections2 = new ArrayList<>();

                                    connections1 = connections1.stream().filter(entry -> !testedChars.contains(entry.getKey())).toList();
                                    if (char1 != char2) {
                                        connections2 = connections2.stream().filter(entry -> !testedChars.contains(entry.getKey())).toList();
                                    }

                                    if (!connections1.isEmpty()) {
                                        finalConnections.add(new AbstractMap.SimpleEntry<>(char1, connections1));
                                        //System.out.println("Connections for " + char1 + ": " + connections1);
                                    }

                                    if (char1 != char2 && !connections2.isEmpty()) {
                                        finalConnections.add(new AbstractMap.SimpleEntry<>(char2, connections2));
                                        //System.out.println("Connections for " + char2 + ": " + connections2);
                                    }
                                }

                                // Now we have to iterate over the final connections and make the deductions
                                //System.out.println("Final connections: ");
                                for (Map.Entry<Character, List<Map.Entry<Character, Integer>>> entry : finalConnections) {
                                    char key = entry.getKey();
                                    List<Map.Entry<Character, Integer>> connections = entry.getValue();
                                    //System.out.println(key + " -> " + connections);

                                    // Find the map from the deductedMappings list that corresponds to the current character
                                    map = deductedMappings.stream()
                                            .filter(plug -> plug[0] == key || plug[1] == key)
                                            .map(plug -> plug[0] == key ? plug[1] : plug[0])
                                            .findFirst()
                                            .orElseThrow();


                                    validDeduction = findMappingsFromDeduction(
                                            connections,
                                            key,
                                            map,
                                            bombe,
                                            newConfig,
                                            deductedMappings);
                                }

                                //System.out.println("Deduction is " + (validDeduction ? "valid" : "invalid"));

                                if (validDeduction) {
                                    numValid++;
                                }

                                progressBar.add(1);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Number of valid deductions: " + numValid);

    }

    private static boolean findMappingsFromDeduction(
            List<Map.Entry<Character,
            Integer>> charConnections,
            char currChar,
            char map,
            Bombe bombe, EnigmaConfig newConfig,
            List<char[]> deductedMappings)
    {
        // Get the new mappings from this deduction
        boolean validDeduction = true;
        //int numValid = 0;
        for (Map.Entry<Character, Integer> entry : charConnections) {
            char cipherChar = entry.getKey();
            int stepNumber = entry.getValue();
            //System.out.println("(X=" + stepNumber + "): \n" + currChar + '☰' + map + " -> ?☰" + cipherChar);

            try {
                Machine currMachine = bombe.createMachine(newConfig);
                currMachine.rotateRotors(stepNumber);
                char newChar = currMachine.cipherCharacter(currChar);
                //System.out.println("" + currChar + '☰' + map + " -> " + newChar + '☰' + cipherChar);
                deductedMappings.add(new char[]{newChar, cipherChar});
                newConfig.addPlug(newChar + "" + cipherChar);
            } catch (AssertionError e) {
                //System.out.println("Incongruent mapping detected, discarding...");
                validDeduction = false;
                //numValid++;
                break;
            }
        }
        return validDeduction;
    }
}