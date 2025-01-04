package es.usj.crypto;

import es.usj.crypto.enigma.AlphabetCircle;
import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Machine;

import javax.swing.*;
import java.util.*;

public class Bombe {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Machine enigma;
    private final String ciphertext;
    private final String crib;
    private final Map<Character, Set<Character>> letterConnections;
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

    private Map<Character, Set<Character>> buildMenu() {
        Map<Character, Set<Character>> menu = new LinkedHashMap<>();

        for (int i = 0; i < crib.length(); i++) {
            char plainChar = crib.charAt(i);
            char cipherChar = ciphertext.charAt(i);

            menu.putIfAbsent(plainChar, new HashSet<>());
            menu.putIfAbsent(cipherChar, new HashSet<>());

            menu.get(plainChar).add(cipherChar);
            menu.get(cipherChar).add(plainChar);

            String step = "Step " + (i + 1) + ": " + plainChar + " <-> " + cipherChar;
            steps.add(step);
            System.out.println(step);
        }

        findLoopSteps(menu);
        return menu;
    }

    private void findLoopSteps(Map<Character, Set<Character>> menu) {
        Set<Set<Character>> closedLoops = findClosedLoops(menu);
        Set<String> uniqueMappings = new HashSet<>();

        for (Set<Character> loop : closedLoops) {
            for (Character c1 : loop) {
                for (Character c2 : menu.get(c1)) {
                    if (loop.contains(c2)) {
                        char[] mapping = {c1, c2};
                        Arrays.sort(mapping);
                        String mappingStr = new String(mapping);

                        if (!uniqueMappings.contains(mappingStr)) {
                            uniqueMappings.add(mappingStr);
                            int stepNumber = findStepNumber(c1, c2);
                            Map<String, Object> loopStep = new HashMap<>();
                            loopStep.put("numStep", stepNumber);
                            loopStep.put("Mapping", mapping);
                            loopSteps.add(loopStep);
                        }
                    }
                }
            }
        }
    }

    private int findStepNumber(char c1, char c2) {
        for (int i = 0; i < crib.length(); i++) {
            if ((crib.charAt(i) == c1 && ciphertext.charAt(i) == c2) || (crib.charAt(i) == c2 && ciphertext.charAt(i) == c1)) {
                return i + 1;
            }
        }
        return -1;
    }

    private Set<Set<Character>> findClosedLoops(Map<Character, Set<Character>> menu) {
        Set<Set<Character>> closedLoops = new HashSet<>();
        for (char start : menu.keySet()) {
            findClosedLoop(start, start, new HashSet<>(), new LinkedHashSet<>(), closedLoops, menu);
        }
        return closedLoops;
    }

    private void findClosedLoop(char start, char current, Set<Character> visited, Set<Character> path, Set<Set<Character>> closedLoops, Map<Character, Set<Character>> menu) {
        if (visited.contains(current)) {
            if (current == start && path.size() >= 3) {
                closedLoops.add(new HashSet<>(path));
            }
            return;
        }
        visited.add(current);
        path.add(current);
        for (char next : menu.get(current)) {
            findClosedLoop(start, next, visited, path, closedLoops, menu);
        }
        visited.remove(current);
        path.remove(current);
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

    public void visualizeConnections() {
        JFrame frame = new JFrame("Bombe Connections Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.add(new AlphabetCircle(letterConnections));
        frame.setVisible(true);
    }

    public List<String> getSteps() {
        return steps;
    }

    public Set<Map<String, Object>> getLoopSteps() {
        return loopSteps;
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

        //bombe.visualizeConnections();

        System.out.println("Mappings that form the loop:");

        List<Map<String, Object>> steps = new ArrayList<>(bombe.getLoopSteps());

        // Sort the mappings to form a continuous loop
        List<Map<String, Object>> sortedSteps = new ArrayList<>();
        if (!steps.isEmpty()) {
            // Start with the first mapping
            sortedSteps.add(steps.get(0));
            steps.remove(0);

            // Continue until we've used all mappings
            while (!steps.isEmpty()) {
                Map<String, Object> lastStep = sortedSteps.get(sortedSteps.size() - 1);
                char[] lastMapping = (char[]) lastStep.get("Mapping");
                char lastChar = lastMapping[1];  // Get the second character of the last mapping

                // Find the next mapping that starts with our last character
                Map<String, Object> nextStep = null;
                for (Map<String, Object> step : steps) {
                    char[] mapping = (char[]) step.get("Mapping");
                    if (mapping[0] == lastChar || mapping[1] == lastChar) {
                        nextStep = step;
                        // If we found it connecting to mapping[1], swap the characters to maintain the loop
                        if (mapping[1] == lastChar) {
                            char temp = mapping[0];
                            mapping[0] = mapping[1];
                            mapping[1] = temp;
                        }
                        break;
                    }
                }

                if (nextStep != null) {
                    sortedSteps.add(nextStep);
                    steps.remove(nextStep);
                }
            }
        }

        // Print the sorted loop

        sortedSteps.forEach(step -> {
            char[] mapping = (char[]) step.get("Mapping");
            System.out.println("Mapping: " + mapping[0] + " <-> " + mapping[1]);
        });

        // Rest of the code remains the same
        Map<Integer, Machine> machines = new HashMap<>();
        for (Map<String, Object> step : sortedSteps) {
            Machine machine = bombe.createMachine(config);
            int numStep = (int) step.get("numStep");
            System.out.println("Rotating rotors for step " + numStep);
            machine.rotateRotors(numStep);
            machines.put(numStep, machine);
        }



        int numStep1 = (int) sortedSteps.get(0).get("numStep");
        Machine machine1 = machines.get(numStep1);

        int numStep2 = (int) sortedSteps.get(1).get("numStep");
        Machine machine2 = machines.get(numStep2);

        int numStep3 = (int) sortedSteps.get(2).get("numStep");
        Machine machine3 = machines.get(numStep3);

        char[] mapping1 = (char[]) sortedSteps.get(0).get("Mapping");
        char[] mapping2 = (char[]) sortedSteps.get(1).get("Mapping");
        char[] mapping3 = (char[]) sortedSteps.get(2).get("Mapping");

        System.out.println("Initial character: " + mapping1[0]);

        String sequence = mapping1[0] + "->" + mapping2[0] + "->"  + mapping3[0];
        System.out.println("Sequence: " + sequence);

        for (char map = 'A'; map <= 'Z'; map++) {
            System.out.println("Mapping " + mapping1[0] + " -> " + map);

            // Pass the mapped character through the machines
            char c1 = machine1.cipherCharacter(map);
            char c2 = machine2.cipherCharacter(c1);
            char c3 = machine3.cipherCharacter(c2);

            System.out.println("Result: " + c3);
            if (c3 == map) {
                System.out.println("Found match: ");
                System.out.println("Mapping: ");
                System.out.println(mapping2[0] + " -> " + c1);
                System.out.println(mapping3[0] + " -> " + c2);
                System.out.println(mapping1[0] + " -> " + c3);
            }
        }
    }
}