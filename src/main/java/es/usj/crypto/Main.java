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

public class Main {

    private static final int PLUGBOARD_SIZE = 10;
    private static final int FIXED_PLUGBOARD_SIZE = 0;
    private static final boolean FIX_ROTOR_POSITIONS = true;

    private static final int TOP_NUMBER = 5000;

    private static final Path plainTextPath = Paths.get("data/plain_text.txt");
    private static EnigmaManager manager = new EnigmaManager(plainTextPath);

    public static void main(String[] args) {
        Terminal terminal = new Terminal();
        terminal.setScreenAttributes(TerminalStyles.BIOS);


        EnigmaConfig initialConfig = manager.cipherInitialText(PLUGBOARD_SIZE);
        System.out.println("Initial Configuration:" + initialConfig);

        generateAndTestConfigs();

    }


    private static List<EnigmaConfig> generateAndTestConfigs() {

        ConcurrentLinkedQueue<EnigmaConfig> configs = new ConcurrentLinkedQueue<>();
        // Iterate through all rotor positions
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 5; j++) {
                for (int k = 1; k <= 5; k++) {
                    if (k == i || k == j || j == i) continue;
                    for (char a = 'A'; a <= 'Z'; a++) {
                        for (char b = 'A'; b <= 'Z'; b++) {
                            for (char c = 'A'; c <= 'Z'; c++) {
                                EnigmaConfig config = new EnigmaConfig(new int[]{i, j, k}, new char[]{a, b, c}, "");
                                configs.add(config);
                            }
                        }
                    }
                }
            }
        }

        manager.scoreConfigurations(new ArrayList<>(configs), true);


        List<EnigmaConfig> topScores = configs.stream()
                .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                .limit(TOP_NUMBER)
                .toList();

        System.out.println("Top 10 Scores with Configurations:");
        for (int i = 0; i < topScores.size(); i++) {
            EnigmaConfig cs = topScores.get(i);
            System.out.println(i + ": " + cs);
        }

        List<EnigmaConfig> topScoresWithPlugs = new ArrayList<>();
        while (topScoresWithPlugs.isEmpty() || topScoresWithPlugs.get(0).getPlugboard().split(":").length < 10) {
            List<String> plugs = generatePlugboardConfig();

            List<EnigmaConfig> previousTopScores = new ArrayList<>(topScoresWithPlugs);
            topScoresWithPlugs = new ArrayList<>();

            for (EnigmaConfig config : previousTopScores) {
                for (String plug : plugs) {
                    boolean plugused = false;
                    for (char c : plug.toCharArray()) {
                        if (config.getPlugboard().contains("" + c)) {
                            plugused = true;
                            break;
                        }
                    }
                    if (plugused) continue;
                    String plugboard = Objects.equals(config.getPlugboard(), "") ? plug : config.getPlugboard() + ":" + plug;
                    EnigmaConfig newConfig = new EnigmaConfig(config.getRotorTypes(), config.getRotorPositions(), plugboard);
                    topScoresWithPlugs.add(newConfig);
                }
            }
            manager.scoreConfigurations(topScoresWithPlugs, true);

            topScoresWithPlugs = previousTopScores.stream()
                    .sorted(Comparator.comparingDouble(EnigmaConfig::getScore).reversed())
                    .limit(TOP_NUMBER / 10)
                    .toList();

            System.out.println("Top 10 Scores with Configurations:");
            for (int i = 0; i < topScoresWithPlugs.size(); i++) {
                EnigmaConfig cs = topScoresWithPlugs.get(i);
                System.out.println(i + ": " + cs);
            }
        }

        return topScoresWithPlugs;
    }

    static List<String> generatePlugboardConfig() {
        List<String> plugboards = new ArrayList<>();
        char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        boolean[] used = new boolean[alphabet.length];

        for (int i = 0; i < alphabet.length; i++) {
            if (used[i]) continue;
            for (int j = i + 1; j < alphabet.length; j++) {
                if (used[j]) continue;
                plugboards.add("" + alphabet[i] + alphabet[j]);
                used[i] = true;
                used[j] = true;
                break;
            }
        }
        return plugboards;
    }
}