package es.usj.crypto;

import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Machine;
import es.usj.crypto.utils.EnigmaConfig;
import es.usj.crypto.utils.EnigmaManager;
import es.usj.crypto.utils.ProgressBar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Bombe {

    String ciphertext;
    String crib;
    int initialStep;
    private final List<String> steps;
    public final Map<Character, List<Map.Entry<Character, Integer>>> letterConnections;


    public Bombe(EnigmaConfig config, String ciphertext, String crib, int initialStep) {
        this.ciphertext = ciphertext;
        this.crib = crib;
        this.initialStep = initialStep;
        this.steps = new ArrayList<>();
        this.letterConnections = buildMenu();

    }

    private Map<Character, List<Map.Entry<Character, Integer>>> buildMenu() {
        Map<Character, List<Map.Entry<Character, Integer>>> menu = new LinkedHashMap<>();

        for (int i = 0; i < crib.length(); i++) {
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
        /*
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

         */

        String txt = null;
        try {
            txt = Files.readString(Paths.get("data/cipher.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Text: " + txt);

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
        System.out.println(bombe.letterConnections);
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

                                    //EnigmaConfig correctConfig = new EnigmaConfig(new int[]{L, M, R}, new char[]{LPos, MPos, RPos}, "");
                                    EnigmaConfig correctConfig = new EnigmaConfig(new int[]{L, M, R}, new char[]{LPos, 'U', 'E'}, "");
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

        EnigmaManager manager = new EnigmaManager(Paths.get("data/cipher.txt"));

        if (validConfigs.isEmpty()) {
            System.out.println("No valid configurations found");
            System.exit(0);
        }

        manager.scoreConfigurations(validConfigs, false);

        // Get sorted valid configurations
        validConfigs = validConfigs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .toList();

        /*
        int[] rotorTypes = {4, 1, 3};
        char[] rotorPositions = {'A', 'D', 'J'};
        String plugboard = "XZ:AY:BW:CN:DP:EQ:FR:GT:HS:JU";

        EnigmaConfig Validconfig = new EnigmaConfig(rotorTypes, rotorPositions, plugboard);

        // See if we find the validConfig in validConfigs without comparing the plugboard
        EnigmaConfig correctConfig = validConfigs.stream()
                .filter(config1 -> Arrays.equals(config1.getRotorTypes(), Validconfig.getRotorTypes()) &&
                        Arrays.equals(config1.getRotorPositions(), Validconfig.getRotorPositions()))
                .findFirst()
                .orElseThrow();
        System.out.println("Correct configuration found: " + correctConfig);

         */

        // See if any of the valid configurations have a score > 0.6, if so, log it
        validConfigs.stream()
                .filter(config1 -> config1.getScore() > 0.6)
                .forEach(config1 -> System.out.println("Configuration with score > 0.6: " + config1));


        // Get initial top 100 configurations
        List<EnigmaConfig> top100Configs = validConfigs.stream()
                .limit(100)
                .toList();

        //System.out.println("\nTop 100 configurations:");
        //top100Configs.forEach(System.out::println);

        // Group the valid configurations by plugboard pairs length
        Map<Integer, List<EnigmaConfig>> groupedConfigs = validConfigs.stream()
                .collect(Collectors.groupingBy(config1 -> config1.getPlugboard().split(":").length));

        // Display the number of configurations for each plugboard length
        //System.out.println("\nNumber of configurations by plugboard length:");
        //groupedConfigs.forEach((key, value) -> System.out.println(key + " -> " + value.size()));


        top100Configs = new ArrayList<>();
        // For each plugboard length, starting by the ones with less size, add plugs to it until we reach 10, grouping them with the other configurations as they reach same size
        for (Map.Entry<Integer, List<EnigmaConfig>> groupConfig : groupedConfigs.entrySet()) {
            int plugboardLength = groupConfig.getKey();
            List<EnigmaConfig> configs = groupConfig.getValue();

            // Add them to a set to filter out duplicates
            Set<EnigmaConfig> uniqueConfigs = new HashSet<>(configs);

            configs = new ArrayList<>(uniqueConfigs.stream()
                    .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                    .limit(100)
                    .toList());

            configs.addAll(top100Configs);

            if (plugboardLength >= 10) {
                continue;
            }

            List<EnigmaConfig> newConfigs = new ArrayList<>();
            for (EnigmaConfig baseConfig : configs) {
                if (baseConfig.getPlugboard().split(":").length >= 10) {
                    continue;
                }

                List<char[]> plugs = generatePlugboardConfig();
                for (char[] plug : plugs) {
                    EnigmaConfig newConfig = new EnigmaConfig(baseConfig);
                    try {
                        newConfig.addPlug(plug[0] + "" + plug[1]);
                    } catch (AssertionError e) {
                        continue;
                    }
                    newConfigs.add(newConfig);

                }
            }

            manager.scoreConfigurations(newConfigs, false);

            top100Configs = newConfigs.stream()
                    .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                    .limit(100)
                    .toList();

        }

        List<EnigmaConfig> immutableTop10Configs = top100Configs.stream().limit(5).toList();
        immutableTop10Configs.forEach(topConfig -> {
            String currTxt = manager.process(topConfig);
            System.out.println("Configuration:");
            System.out.println(topConfig);
            System.out.println(currTxt + "\n");
        });

        if (top100Configs.get(0).getScore() > 0.8) {
            System.out.println("Top configuration found with score > 0.8");
            manager.process(top100Configs.get(0));
            System.out.println("Crib found: " + crib);
            System.exit(0);
        }

    }

    private static List<char[]> generatePlugboardConfig() {
        List<char[]> plugboards = new ArrayList<>();
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        for (int i = 0; i < alphabet.length; i++) {
            for (int j = i + 1; j < alphabet.length; j++) {
                plugboards.add(new char[]{alphabet[i], alphabet[j]});
            }
        }
        return plugboards;
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