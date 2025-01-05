package es.usj.crypto;

import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Machine;
import es.usj.crypto.utils.EnigmaManager;
import es.usj.crypto.utils.ProgressBar;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Bombe {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Machine enigma;
    private final String ciphertext;
    private final String crib;
    private final Map<Character, List<Map.Entry<Character, Integer>>> letterConnections;
    private final List<String> steps;
    private final Set<Map<String, Object>> loopSteps;
    private final ConcurrentLinkedQueue<EnigmaConfig> validConfigurations;


    public Bombe(EnigmaConfig config, String ciphertext, String crib) {
        this.enigma = createMachine(config);
        this.ciphertext = ciphertext;
        this.crib = crib;
        this.steps = new ArrayList<>();
        this.loopSteps = new HashSet<>();
        this.letterConnections = buildMenu();
        this.validConfigurations = new ConcurrentLinkedQueue<>();
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

    // Main processing method with optimizations
    public List<EnigmaConfig> processConnections(EnigmaConfig baseConfig, char firstChar,
                                                 List<Map.Entry<Character, Integer>> firstCharConnections) {
        // Calculate new total including all possible mappings
        int configurationsPerMapping = 5 * 4 * 3 * 26 * 26 * 26;
        int total = configurationsPerMapping * 26;  // Multiply by number of possible mappings
        ProgressBar progressBar = new ProgressBar(total);
        AtomicInteger progressCounter = new AtomicInteger();

        // Create thread pool sized to available processors
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        try {
            // Pre-generate all valid rotor combinations
            List<RotorCombination> rotorCombinations = generateRotorCombinations();

            // Create a list to hold all futures
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // Process each possible mapping from 'A' to 'Z' in parallel
            for (char mapping = 'A'; mapping <= 'Z'; mapping++) {
                final char currentMapping = mapping;

                // Process rotor combinations in parallel for each mapping
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    rotorCombinations.forEach(combo ->
                            processRotorCombination(
                                    combo,
                                    baseConfig,
                                    firstChar,
                                    firstCharConnections,
                                    currentMapping,  // Pass the current mapping being tested
                                    progressCounter,
                                    progressBar,
                                    configurationsPerMapping  // Pass configurations per mapping for progress
                            )
                    );
                }, executor);

                futures.add(future);
            }

            // Wait for all tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        } finally {
            executor.shutdown();
            return new ArrayList<>(validConfigurations);
        }
    }

    // Record to hold rotor combination data
    private record RotorCombination(int left, int middle, int right) {}

    // Generate all valid rotor combinations
    private List<RotorCombination> generateRotorCombinations() {
        List<RotorCombination> combinations = new ArrayList<>();
        for (int L = 1; L <= 5; L++) {
            for (int M = 1; M <= 5; M++) {
                if (L == M) continue;
                for (int R = 1; R <= 5; R++) {
                    if (R == L || R == M) continue;
                    combinations.add(new RotorCombination(L, M, R));
                }
            }
        }
        return combinations;
    }

    // Process a single rotor combination
    private void processRotorCombination(
            RotorCombination combo,
            EnigmaConfig baseConfig,
            char firstChar,
            List<Map.Entry<Character, Integer>> firstCharConnections,
            char mapping,  // Added parameter for current mapping
            AtomicInteger numValid,
            ProgressBar progressBar,
            int configurationsPerMapping) {

        EnigmaConfig threadConfig = new EnigmaConfig(baseConfig);
        threadConfig.setRotorTypes(new int[]{combo.left(), combo.middle(), combo.right()});

        List<Character> testedChars = new ArrayList<>();
        List<char[]> deductedMappings = new ArrayList<>();
        char[] positions = ALPHABET.toCharArray();

        for (char LPos : positions) {
            for (char MPos : positions) {
                for (char RPos : positions) {
                    processPosition(
                            threadConfig,
                            new char[]{LPos, MPos, RPos},
                            firstChar,
                            firstCharConnections,
                            mapping,  // Pass the current mapping
                            testedChars,
                            deductedMappings,
                            numValid,
                            progressBar,
                            configurationsPerMapping
                    );

                    testedChars.clear();
                    deductedMappings.clear();
                }
            }
        }
    }

    // Process a single position combination
    private void processPosition(
            EnigmaConfig config,
            char[] positions,
            char firstChar,
            List<Map.Entry<Character, Integer>> firstCharConnections,
            char mapping,  // Added parameter for current mapping
            List<Character> testedChars,
            List<char[]> deductedMappings,
            AtomicInteger progressCounter,
            ProgressBar progressBar,
            int configurationsPerMapping) {

        EnigmaConfig attemptConfig = new EnigmaConfig(config);
        attemptConfig.setRotorPositions(positions);
        // Use the current mapping instead of hardcoded 'N'
        attemptConfig.setPlugboard(firstChar + "" + mapping);

        testedChars.add(firstChar);

        boolean validDeduction = findMappingsFromDeduction(
                firstCharConnections,
                firstChar,
                mapping,  // Pass the current mapping
                this,
                attemptConfig,
                deductedMappings
        );

        if (validDeduction) {
            validDeduction = processRemainingDeductions(
                    attemptConfig,
                    testedChars,
                    deductedMappings
            );
        }

        if (validDeduction) {
            EnigmaConfig validConfig = new EnigmaConfig(
                    attemptConfig.getRotorTypes(),
                    attemptConfig.getRotorPositions(),
                    attemptConfig.getPlugboard()
            );
            validConfigurations.offer(validConfig);
        }

        // Update progress less frequently to reduce overhead
        int currentProgress = progressCounter.incrementAndGet();
        if (currentProgress % (configurationsPerMapping / 50) == 0) {
            progressBar.add(configurationsPerMapping / 50);
        }
    }

    // Process remaining deductions after initial deduction
    private boolean processRemainingDeductions(
            EnigmaConfig config,
            List<Character> testedChars,
            List<char[]> deductedMappings) {

        List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> finalConnections =
                buildFinalConnections(testedChars, deductedMappings);

        for (Map.Entry<Character, List<Map.Entry<Character, Integer>>> entry : finalConnections) {
            char key = entry.getKey();
            List<Map.Entry<Character, Integer>> connections = entry.getValue();

            char map = deductedMappings.stream()
                    .filter(plug -> plug[0] == key || plug[1] == key)
                    .map(plug -> plug[0] == key ? plug[1] : plug[0])
                    .findFirst()
                    .orElseThrow();

            if (!findMappingsFromDeduction(connections, key, map, this, config, deductedMappings)) {
                return false;
            }
        }

        return true;
    }

    // Build final connections list
    private List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> buildFinalConnections(
            List<Character> testedChars,
            List<char[]> deductedMappings) {

        List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> finalConnections =
                new ArrayList<>();

        for (char[] plug : deductedMappings) {
            char char1 = plug[0];
            char char2 = plug[1];

            List<Map.Entry<Character, Integer>> connections1 = letterConnections.get(char1);
            List<Map.Entry<Character, Integer>> connections2 =
                    char1 == char2 ? connections1 : letterConnections.get(char2);

            if (connections1 == null) connections1 = new ArrayList<>();
            if (connections2 == null) connections2 = new ArrayList<>();

            connections1 = connections1.stream()
                    .filter(entry -> !testedChars.contains(entry.getKey()))
                    .collect(Collectors.toList());

            if (char1 != char2) {
                connections2 = connections2.stream()
                        .filter(entry -> !testedChars.contains(entry.getKey()))
                        .collect(Collectors.toList());
            }

            if (!connections1.isEmpty()) {
                finalConnections.add(new AbstractMap.SimpleEntry<>(char1, connections1));
            }

            if (char1 != char2 && !connections2.isEmpty()) {
                finalConnections.add(new AbstractMap.SimpleEntry<>(char2, connections2));
            }
        }

        return finalConnections;
    }

    public static void main(String[] args) {
        EnigmaConfig config = new EnigmaConfig(
                new int[]{1, 2, 3},
                new char[]{'A', 'A', 'A'},
                ""
        );

        String ciphertext = "KAHYCFKDTCUSGH";
        String crib = "IDENTIFICATION";

        Bombe bombe = new Bombe(config, ciphertext, crib);

        List<Map.Entry<Character, List<Map.Entry<Character, Integer>>>> sortedConnections =
                new ArrayList<>(bombe.letterConnections.entrySet());
        sortedConnections.sort((e1, e2) ->
                Integer.compare(e2.getValue().size(), e1.getValue().size()));

        char firstChar = sortedConnections.get(0).getKey();
        List<Map.Entry<Character, Integer>> firstCharConnections =
                sortedConnections.get(0).getValue();

        System.out.println("-----------------");
        System.out.println("Mappings for character " + firstChar);

        List<EnigmaConfig> validConfigs = bombe.processConnections(config, firstChar, firstCharConnections);
        System.out.println("\nNumber of valid configurations: " + validConfigs.size());

        Path cipherTextPath = Paths.get("data/cipher_text_test.txt");
        EnigmaManager manager = new EnigmaManager(cipherTextPath);
        manager.scoreConfigurations(validConfigs, true);
        manager.shutdown();

        validConfigs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .limit(100)
                .forEach(System.out::println);
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
                //numValid++;
                return false;
            }
        }
        return true;
    }
}