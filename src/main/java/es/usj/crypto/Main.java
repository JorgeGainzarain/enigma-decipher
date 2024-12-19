package es.usj.crypto;

import com.acidmanic.consoletools.terminal.Terminal;
import com.acidmanic.consoletools.terminal.styling.TerminalStyles;
import es.usj.crypto.utils.EnigmaManager;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;


public class Main {

    private static final int MIN_ROTOR = 1;
    private static final int MAX_ROTOR = 5;
    private static final char MIN_POSITION = 'A';
    private static final char MAX_POSITION = 'Z';

    private static final int PLUGBOARD_SIZE = 10; // Size of the plugs for the plugboard
    private static final int FIXED_PLUGBOARD_SIZE = 0; // Size of the fixed part of the plugboard
    private static final int TOP_NUMBER = 5000; // Number of top configurations to display

    private static final Path plainTextPath = Paths.get("data/plain_text.txt");
    private static final EnigmaManager manager = new EnigmaManager(plainTextPath);

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.setScreenAttributes(TerminalStyles.BIOS);

        int iterations = 20;
        int foundCount = 0;

        for (int i = 0; i < iterations; i++) {
            // Cipher the plain text and get the initial configuration
            EnigmaConfig initialConfig = manager.cipherInitialText(PLUGBOARD_SIZE);

            // Display the configuration used
            System.out.println("Initial Configuration:" + initialConfig);

            // Try to decipher the text
            boolean found = generateAndTestConfigs(initialConfig);

            if (found) {
                foundCount++;
            } else {
                System.out.printf("Configuration not found in iteration %d. Total found: %d%n", i + 1, foundCount);
                break;
            }
        }

        System.out.printf("Total configurations found: %d out of %d iterations.%n", foundCount, iterations);
    }

    private static boolean generateAndTestConfigs(EnigmaConfig initialConfig) {
        String fixedPlugboard = initialConfig.getFixedPlugboard(FIXED_PLUGBOARD_SIZE);

        List<EnigmaConfig> configs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (j == i) continue;
                for (int k = 0; k < 5; k++) {
                    if (k == i || k == j) continue;
                    for (char a = 'A'; a <= 'Z'; a++) {
                        for (char b = 'A'; b <= 'Z'; b++) {
                            for (char c = 'A'; c <= 'Z'; c++) {
                                EnigmaConfig config = new EnigmaConfig(new int[]{i + 1, j + 1, k + 1}, new char[]{a, b, c}, fixedPlugboard);
                                configs.add(config);
                            }
                        }
                    }
                }
            }
        }

        manager.scoreConfigurations(configs, true);
        manager.shutdown();

        Path path = Paths.get("data/" + Arrays.toString(initialConfig.getRotorTypes()) + "_"
                + Arrays.toString(initialConfig.getRotorPositions())
                + initialConfig.getPlugboard().replace(":", "-")
                + ".csv");

        saveConfigurationsToFile(configs, path.toString());

        // Collect and display the top 10 scores with configurations
        List<EnigmaConfig> topScores = configs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .limit(10)
                .toList();

        System.out.println("Top 10 Scores with Configurations:");
        for (int i = 0; i < topScores.size(); i++) {
            EnigmaConfig cs = topScores.get(i);
            System.out.println(i + ": " + cs);
        }

        // Check if the original configuration is in the top TOP_NUMBER
        boolean found = configs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .limit(TOP_NUMBER)
                .anyMatch(cs -> cs.equalsWithoutPlugboard(initialConfig));

        if (found) {
            System.out.println("The original configuration was found in the top TOP_NUMBER.");
        } else {
            System.out.println("The original configuration was not found in the top TOP_NUMBER.");
        }
        return found;
    }

    private static void saveConfigurationsToFile(List<EnigmaConfig> configs, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("leftType,middleType,rightType,leftPosition,middlePosition,rightPosition,score\n");
            for (EnigmaConfig config : configs) {
                writer.write(String.format("%d,%d,%d,%c,%c,%c,%d\n",
                        config.getRotorTypes()[0], config.getRotorTypes()[1], config.getRotorTypes()[2],
                        config.getRotorPositions()[0], config.getRotorPositions()[1], config.getRotorPositions()[2],
                        (int) (config.getScore() * 100)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}