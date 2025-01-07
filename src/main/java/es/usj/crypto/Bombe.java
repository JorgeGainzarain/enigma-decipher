package es.usj.crypto;

import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Machine;
import es.usj.crypto.utils.EnigmaManager;
import es.usj.crypto.utils.ProgressBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static es.usj.crypto.Main.generatePlugboardConfig;

public class Bombe {

    String ciphertext;
    String crib;
    int initialStep;
    private final List<String> steps;
    public final Map<Character, List<Map.Entry<Character, Integer>>> letterConnections;


    public Machine getMachineFromPool(EnigmaConfig config) {
        Machine machine = machinePool.poll();
        if (machine == null) {
            machine = createMachine(config);
        }
        else {
            machine.setRotors(config.getRotorTypes(), config.getRotorPositions());
            machine.setPlugboard(config.getPlugboard());
        }
        return machine;
    }

    public void returnMachineToPool(Machine machine) {
        machinePool.offer(machine);
    }

    public Bombe(EnigmaConfig config, String ciphertext, String crib, int initialStep) {
        this.ciphertext = ciphertext;
        this.crib = crib;
        this.initialStep = initialStep;
        this.steps = new ArrayList<>();
        this.letterConnections = buildMenu();

    }

    private Map<Character, List<Map.Entry<Character, Integer>>> buildMenu() {
        Map<Character, List<Map.Entry<Character, Integer>>> menu = new LinkedHashMap<>();

        for (int i = initialStep; i < crib.length(); i++) {
            char plainChar = crib.charAt(i);
            char cipherChar = ciphertext.charAt(i);
            int stepNumber =  initialStep + i + 1;

            menu.putIfAbsent(plainChar, new ArrayList<>());
            menu.putIfAbsent(cipherChar, new ArrayList<>());

            menu.get(plainChar).add(new AbstractMap.SimpleEntry<>(cipherChar, stepNumber));
            menu.get(cipherChar).add(new AbstractMap.SimpleEntry<>(plainChar, stepNumber));

            String step = "Step " + stepNumber + ": " + plainChar + " <-> " + cipherChar;
            steps.add(step);
            System.out.println(step);
        }

        // Log the menu
        System.out.println("\nMenu:");
        menu.forEach((key, value) -> {
            System.out.print(key + " -> ");
            value.forEach(entry -> System.out.print(entry.getKey() + " (" + entry.getValue() + "), "));
            System.out.println();
        });
        return menu;
    }

    public static Machine createMachine(EnigmaConfig config) {
        String[] args = {
                "--input=" + "",
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

    public static void main(String[] args) {
        EnigmaConfig config = new EnigmaConfig(new int[]{3, 5, 4}, new char[]{'J', 'D', 'A'}, "XZ:AY:BW:CN:DP:EQ:FR:GT:HS:JU");
        EnigmaManager enigmaManager = new EnigmaManager(Paths.get("data/plain_text.txt"));
        String txt = enigmaManager.process(config);
        System.out.println("Initial text: " + txt);
        // Write the text to data/cipher_test.txx manually
        enigmaManager.shutdown();
        try {
            Files.writeString(Paths.get("data/cipher_test.txt"), txt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Try to decipher the text with bombe approach
        // let's start it simple

        String crib = "SYSTEMATICALLY";
        // Find ciphertext from the txt finding first word with same length as crib
        int index = 0;
        String ciphertext = "";
        for (String word : txt.split(" ")) {
            if (word.length() == crib.length()) {
                ciphertext = word;
                break;
            }
            index += word.length();
        }
        if (ciphertext.isEmpty()) {
            throw new NoSuchElementException();
        }

        System.out.println("Ciphertext: " + ciphertext);
        System.out.println("Crib: " + crib);
        System.out.println("Index: " + index);

        // Create a menu mapping each letter from ciphertext to the possible letters from crib manually
        Bombe bombe = new Bombe(config, ciphertext, crib, index);
      
        // Get the letter with most connections
        char letter = bombe.letterConnections.entrySet().stream()
                .max(Comparator.comparingInt(entry -> entry.getValue().size()))
                .map(Map.Entry::getKey)
                .orElseThrow();

        System.out.println("Letter with most connections: " + letter);
        System.out.println("Connections: " + bombe.letterConnections.get(letter));



        List<EnigmaConfig> validConfigs = new ArrayList<>();

        int total = 5 * 4 * 3; // Rotors
        total *= 26 * 26 * 26; // Positions
        total *= 26; // Mappings
        ProgressBar progressBar = new ProgressBar(total);

        // We would iterate through configs but let's just get correct one without plugboard for now:
        //EnigmaConfig correctConfig = new EnigmaConfig(new int[]{3, 1, 4}, new char[]{'J', 'D', 'A'}, "");

        for (int L = 1; L <= 5; L++) {
            for (int M = 1; M <= 5; M++) {
                if (L == M) continue;
                for (int R = 1; R <= 5; R++) {
                    if (R == L || R == M) continue;
                    for (char LPos = 'A'; LPos <= 'Z'; LPos++) {
                        for (char MPos = 'A'; MPos <= 'Z'; MPos++) {
                            for (char RPos = 'A'; RPos <= 'Z'; RPos++) {
                                for (char map = 'A'; map <= 'Z'; map++) {
                                    total++;

                                    EnigmaConfig correctConfig = new EnigmaConfig(new int[]{L, M, R}, new char[]{LPos, MPos, RPos}, "");
                                    correctConfig.addPlug(letter + "" + map);

                                    List<char[]> testedMappings = new ArrayList<>();
                                    testedMappings.add(new char[]{letter, map});

                                    Map<Character, List<Map.Entry<Character, Integer>>> currentMappings = new HashMap<>();
                                    currentMappings.put(letter, new ArrayList<>(bombe.letterConnections.get(letter)));
                                    //System.out.println("Current mappings initial: " + currentMappings);

                                    boolean validConfig = true;

                                    while (!currentMappings.isEmpty()) {
                                        //System.out.println("Current plugboard: " + correctConfig.getPlugboard());
                                        //System.out.println("Current mappings: " + currentMappings);
                                        //System.out.println("Tested mappings: ");
                                        //testedMappings.forEach(mapping -> System.out.println(mapping[0] + "<->" + mapping[1]));
                                        try {
                                            testDeduction(currentMappings, correctConfig, testedMappings);
                                            //System.out.println("Tested mappings After: ");
                                            //testedMappings.forEach(mapping -> System.out.println(mapping[0] + "<->" + mapping[1]));
                                        } catch (AssertionError e) {
                                            //System.out.println("Not valid config\nError: " + e.getMessage());
                                            validConfig = false;
                                            break;
                                        }

                                        currentMappings = getNextConnections(testedMappings, bombe);
                                    }

                                    if (validConfig) {
                                        validConfigs.add(correctConfig);
                                        //System.out.println("Final config: " + correctConfig);
                                    }
                                }

                            }
                            progressBar.add(26 * 26);
                        }
                    }
                }
            }
        }



        //System.out.println("Valid configs:");
        validConfigs.forEach(System.out::println);
        System.out.println("Total valid configs: " + validConfigs.size());
        System.out.println("Total configs tested: " + total);
        System.out.println("% of valid configs: " + (double) validConfigs.size() / total * 100 + "%");

        // Check if original config was found and show it if it does
        if (validConfigs.stream().anyMatch(currConfig -> currConfig.equalsWithoutPlugboard(config))) {
            System.out.println("Original config found!");
        }

    }

    private static Map<Character, List<Map.Entry<Character, Integer>>> getNextConnections(List<char[]> testedMappings, Bombe bombe) {
        Map<Character, List<Map.Entry<Character, Integer>>> currentMappings = new HashMap<>();
        for (char[] mapping : testedMappings) {
            char letterToTest1 = mapping[0];
            char letterToTest2 = mapping[1];

            // Now get the connections for each of the letters
            List<Map.Entry<Character, Integer>> connections1 = new ArrayList<>(bombe.letterConnections.getOrDefault(letterToTest1, Collections.emptyList()));
            List<Map.Entry<Character, Integer>> connections2 = new ArrayList<>(bombe.letterConnections.getOrDefault(letterToTest2, Collections.emptyList()));

            // Filter out from both lists any mapping that already was tested, note, inverse order still counts as tested
            connections1.removeIf(entry -> testedMappings.stream().anyMatch(testedMapping -> testedMapping[0] == entry.getKey() || testedMapping[1] == entry.getKey()));
            connections2.removeIf(entry -> testedMappings.stream().anyMatch(testedMapping -> testedMapping[0] == entry.getKey() || testedMapping[1] == entry.getKey()));

            // Show the connections
            //System.out.println("Connections for " + letterToTest1 + ": " + connections1);
            //System.out.println("Connections for " + letterToTest2 + ": " + connections2);

            // Add the connections to the current mappings if they are not empty
            if (!connections1.isEmpty()) {
                currentMappings.put(letterToTest1, connections1);
            }
            if (!connections2.isEmpty()) {
                currentMappings.put(letterToTest2, connections2);
            }
        }
        return currentMappings;
    }

    private static void testDeduction(Map<Character, List<Map.Entry<Character, Integer>>> currentMappings, EnigmaConfig correctConfig, List<char[]> testedMappings)  throws AssertionError{
        for (char letter : currentMappings.keySet()) {
            for (Map.Entry<Character, Integer> connection : currentMappings.get(letter)) {
                Machine machine = createMachine(correctConfig);
                machine.getCipheredText("A".repeat(connection.getValue()));
                char c = machine.cipherCharacter(letter);
                //System.out.println("Step " + connection.getValue() + ": " + connection.getKey() + "<->" + c);
                correctConfig.addPlug(connection.getKey() + "" + c);
                testedMappings.add(new char[]{connection.getKey(), c});
            }
        }
    }



}