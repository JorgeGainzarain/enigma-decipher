package es.usj.crypto.utils;

import es.usj.crypto.EnigmaConfig;
import es.usj.crypto.Fitness.Score;
import es.usj.crypto.enigma.EnigmaApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EnigmaManager {
    private final ExecutorService executor;
    private static String text;
    private ProgressBar progressBar;
    private static final int MAX_QUEUE_SIZE = 100000; // Prevent unbounded queue growth

    public EnigmaManager(Path path) {
        this();
        try {
            EnigmaManager.text = Files.readString(path);
        } catch (IOException e) {
            System.err.println("Error reading configurations: " + e.getMessage());
        }
    }

    public EnigmaManager() {
        // Use a more sophisticated thread pool configuration
        this.executor = new ThreadPoolExecutor(
                8, // Core pool size
                8, // Maximum pool size
                60L, TimeUnit.SECONDS, // Keep-alive time for idle threads
                new LinkedBlockingQueue<>(MAX_QUEUE_SIZE), // Bounded queue to prevent resource exhaustion
                new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure policy
        );
        this.progressBar = new ProgressBar(0);
    }


    public EnigmaConfig cipherInitialText(int plugboardSize) {
        try {
            // Generate a random plugboard configuration
            String plugboard = generatePlugboard(plugboardSize);

            // Generate random rotor types and positions
            int[] rotorTypes = generateRandomRotorTypes();
            char[] rotorPositions = generateRandomRotorPositions();

            // Create a new EnigmaConfig with the generated values
            EnigmaConfig config = new EnigmaConfig(rotorTypes, rotorPositions, plugboard);

            // Cipher the text using the generated configuration
            text = process(config);

            System.out.println("Initial text: " + text);

            return config;
        } catch (Exception e) {
            System.err.println("Error during ciphering initial text: " + e.getMessage());
            return null;
        }
    }

    static String generatePlugboard(int size) {
        if (size == 0) {
            return "";
        }
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        List<Character> chars = new ArrayList<>();
        for (char c : alphabet.toCharArray()) {
            chars.add(c);
        }
        Collections.shuffle(chars);
        StringBuilder plugboard = new StringBuilder();
        for (int i = 0; i < size * 2; i += 2) {
            plugboard.append(chars.get(i)).append(chars.get(i + 1)).append(':');
        }
        return plugboard.substring(0, plugboard.length() - 1);
    }

    private int[] generateRandomRotorTypes() {
        Random random = new Random();
        Set<Integer> rotorTypesSet = new HashSet<>();
        while (rotorTypesSet.size() < 3) {
            rotorTypesSet.add(random.nextInt(5) + 1);
        }
        return rotorTypesSet.stream().mapToInt(Integer::intValue).toArray();
    }

    private char[] generateRandomRotorPositions() {
        // Generate random rotor positions between 'A' and 'Z'
        Random random = new Random();
        return random.ints(3, 'A', 'Z' + 1).mapToObj(c -> (char) c).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString().toCharArray();
    }
    public void scoreConfigurations(List<EnigmaConfig> configs, boolean verbose) {
        progressBar.reset(configs.size());
        if (verbose) {
            System.out.println("Starting processing of " + configs.size() + " configurations:");
        }

        int totalConfigs = configs.size();
        int progressInterval = Math.max(1, totalConfigs / 100); // Update progress every 0.1%
        AtomicInteger progressCounter = new AtomicInteger(0);

        try {
            List<CompletableFuture<Void>> futures = new ArrayList<>(totalConfigs);

            for (EnigmaConfig config : configs) {
                CompletableFuture<Void> future = processFuture(config)
                        .thenRun(() -> {
                            int progress = progressCounter.incrementAndGet();
                            if (progress % progressInterval == 0 || progress == totalConfigs) {
                                // Atomic update of progress bar
                                synchronized (progressBar) {
                                    progressBar.add(Math.min(progressInterval, totalConfigs - progress));
                                    progressBar.showProgressBar();
                                    System.out.flush();
                                }
                            }
                        })
                        .exceptionally(ex -> {
                            System.err.println("Error processing configuration: " + ex.getMessage());
                            return null;
                        });

                futures.add(future);
            }

            // Wait for all futures to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Add last update to progress bar
            progressBar.add(configs.size() - progressBar.get());

            if (verbose) {
                System.out.println("\nProcessing completed.");
            }
        } catch (Exception e) {
            System.err.println("Error during configuration processing: " + e.getMessage());
        }
    }

    public String process(EnigmaConfig config) {
        try {
            return processFuture(config).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<String> processFuture(EnigmaConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String result = new EnigmaRunner(config).call();
                config.setScore(Score.evaluate(result));
                return result;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    public void shutdown() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private record EnigmaRunner(EnigmaConfig config) implements Callable<String> {

        @Override
        public String call() {
            String[] args = {
                    "--input=" + text,
                    "--plugboard=" + config.getPlugboard(),
                    "--left-rotor=" + config.getRotorTypes()[0],
                    "--left-rotor-position=" + config.getRotorPositions()[0],
                    "--middle-rotor=" + config.getRotorTypes()[1],
                    "--middle-rotor-position=" + config.getRotorPositions()[1],
                    "--right-rotor=" + config.getRotorTypes()[2],
                    "--right-rotor-position=" + config.getRotorPositions()[2]
            };

            try {
                return new EnigmaApp().run(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}