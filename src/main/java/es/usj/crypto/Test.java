package es.usj.crypto;

import com.acidmanic.consoletools.terminal.Terminal;
import com.acidmanic.consoletools.terminal.styling.TerminalStyles;
import es.usj.crypto.utils.EnigmaManager;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    private static final int PLUGBOARD_SIZE = 10;
    private static final int FIXED_PLUGBOARD_SIZE = 0;
    private static final boolean FIX_ROTOR_POSITIONS = true;

    private static final int TOP_NUMBER = 5000;

    private static final Path plainTextPath = Paths.get("data/plain_text.txt");
    private static EnigmaManager manager = new EnigmaManager(plainTextPath);

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.setScreenAttributes(TerminalStyles.BIOS);


        List<Integer> positions = new ArrayList<>();
        EnigmaConfig initialConfig = manager.cipherInitialText(PLUGBOARD_SIZE);
        System.out.println("Initial Configuration:" + initialConfig);
        for (int iteration = 0; iteration < 10; iteration++) {
            positions.add(generateAndTestConfigs(initialConfig));
            manager.shutdown();
            manager = new EnigmaManager(plainTextPath);
            initialConfig = manager.cipherInitialText(PLUGBOARD_SIZE);
            System.out.println("Initial Configuration:" + initialConfig);
        }
        System.out.println("Positions: " + positions);

    }


    private static int generateAndTestConfigs(EnigmaConfig initialConfig) {
        String fixedPlugboard = initialConfig.getFixedPlugboard(FIXED_PLUGBOARD_SIZE);

        ConcurrentLinkedQueue<EnigmaConfig> configs = new ConcurrentLinkedQueue<>();
        if (FIX_ROTOR_POSITIONS) {
            // Fix the rotor positions to the initial configuration
            for (int i = 1; i <= 5; i++) {
                for (int j = 1; j <= 5; j++) {
                    for (int k = 1; k <= 5; k++) {
                        if (k == i || k == j || j == i) continue;
                        EnigmaConfig config = new EnigmaConfig(new int[]{i,j,k}, initialConfig.getRotorPositions(), fixedPlugboard);
                        configs.add(config);
                    }
                }
            }
        } else {
            // Iterate through all rotor positions
            for (int i = 1; i <= 5; i++) {
                for (int j = 1; j <= 5; j++) {
                    for (int k = 1; k <= 5; k++) {
                        if (k == i || k == j || j == i) continue;
                        for (char a = 'A'; a <= 'Z'; a++) {
                            for (char b = 'A'; b <= 'Z'; b++) {
                                for (char c = 'A'; c <= 'Z'; c++) {
                                    EnigmaConfig config = new EnigmaConfig(new int[]{i, j, k}, new char[]{a, b, c}, fixedPlugboard);
                                    configs.add(config);
                                }
                            }
                        }
                    }
                }
            }
        }

        manager.scoreConfigurations(new ArrayList<>(configs), true);

        Path path = Paths.get("data/" + Arrays.toString(initialConfig.getRotorTypes()) + "_"
                + Arrays.toString(initialConfig.getRotorPositions())
                + initialConfig.getPlugboard().replace(":", "-")
                + ".csv");

        //saveConfigurationsToFile(new ArrayList<>(configs), path.toString());

        List<EnigmaConfig> topScores = configs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .limit(10)
                .toList();

        System.out.println("Top 10 Scores with Configurations:");
        for (int i = 0; i < topScores.size(); i++) {
            EnigmaConfig cs = topScores.get(i);
            System.out.println(i + ": " + cs);
        }

        AtomicInteger i = new AtomicInteger();
        Optional<EnigmaConfig> matchingConfig = configs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .filter(cs -> {
                    boolean matches = cs.equalsWithoutPlugboard(initialConfig);
                    i.getAndIncrement();
                    return matches;
                })
                .findFirst();

        //System.out.println("Matching Configurations:" + matchingConfig);
        if (matchingConfig.isPresent()) {
            System.out.println("(" + i.get() + ") Matching Configuration found: " + matchingConfig.get());
        } else {
            System.out.println("No matching configuration found.");
        }

        return i.get();
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