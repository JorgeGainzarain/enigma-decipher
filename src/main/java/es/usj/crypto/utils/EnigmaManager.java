package es.usj.crypto.utils;

import es.usj.crypto.enigma.EnigmaApp;
import es.usj.crypto.enigma.Score;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class EnigmaManager {
    private final ExecutorService executor;
    private static String text;
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
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(
                availableProcessors, // Core pool size
                availableProcessors * 2, // Maximum pool size
                60L, TimeUnit.SECONDS, // Keep-alive time for idle threads
                new LinkedBlockingQueue<>(MAX_QUEUE_SIZE), // Bounded queue to prevent resource exhaustion
                new ThreadPoolExecutor.CallerRunsPolicy() // Backpressure policy
        );
    }

    public List<ConfigurationScore> processConfigurations(int[][] rotorTypes, char[][] rotorPositions, String[] plugboards, boolean verbose) {
        int totalConfigs = rotorTypes.length;
        ProgressBar progressBar = new ProgressBar(totalConfigs);
        if (verbose) {
            System.out.println("Starting processing of " + totalConfigs + " configurations:");
        }

        int progressInterval = Math.max(1, totalConfigs / 100); // Update progress every 1%
        AtomicInteger progressCounter = new AtomicInteger(0);
        List<ConfigurationScore> scores = new ArrayList<>(totalConfigs);

        try {
            List<CompletableFuture<ConfigurationScore>> futures = new ArrayList<>(totalConfigs);

            for (int i = 0; i < totalConfigs; i++) {
                int[] currentRotorTypes = rotorTypes[i];
                char[] currentRotorPositions = rotorPositions[i];
                String currentPlugboard = plugboards[i];

                CompletableFuture<ConfigurationScore> future = processFuture(currentRotorTypes, currentRotorPositions, currentPlugboard)
                        .thenApply(score -> {
                            int progress = progressCounter.incrementAndGet();
                            if (progress % progressInterval == 0) {
                                synchronized (progressBar) {
                                    progressBar.add(Math.min(progressInterval, totalConfigs - progress));
                                }
                            }
                            return new ConfigurationScore(currentRotorTypes, currentRotorPositions, currentPlugboard, score);
                        })
                        .exceptionally(ex -> {
                            System.err.println("Error processing configuration: " + ex.getMessage());
                            return new ConfigurationScore(currentRotorTypes, currentRotorPositions, currentPlugboard, 0); // Default score in case of error
                        });

                futures.add(future);
            }

            for (CompletableFuture<ConfigurationScore> future : futures) {
                scores.add(future.join());
            }

            progressBar.add(progressInterval);

            if (verbose) {
                System.out.println("\nProcessing completed.");
            }
        } catch (Exception e) {
            System.err.println("Error during configuration processing: " + e.getMessage());
        }

        return scores;
    }

    public double processConfiguration(int[] rotorTypes, char[] rotorPositions, String plugboard) {
        String[] args = {
                "--input=" + text,
                "--plugboard=" + plugboard,
                "--left-rotor=" + rotorTypes[0],
                "--left-rotor-position=" + rotorPositions[0],
                "--middle-rotor=" + rotorTypes[1],
                "--middle-rotor-position=" + rotorPositions[1],
                "--right-rotor=" + rotorTypes[2],
                "--right-rotor-position=" + rotorPositions[2]
        };

        try {
            String result = new EnigmaApp().run(args);
            return Score.evaluate(result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String cipherText(int[] rotorTypes, char[] rotorPositions, String plugboard) {
        String[] args = {
                "--input=" + text,
                "--plugboard=" + plugboard,
                "--left-rotor=" + rotorTypes[0],
                "--left-rotor-position=" + rotorPositions[0],
                "--middle-rotor=" + rotorTypes[1],
                "--middle-rotor-position=" + rotorPositions[1],
                "--right-rotor=" + rotorTypes[2],
                "--right-rotor-position=" + rotorPositions[2]
        };

        try {
            return new EnigmaApp().run(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    private CompletableFuture<Double> processFuture(int[] rotorTypes, char[] rotorPositions, String plugboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return new EnigmaRunner(rotorTypes, rotorPositions, plugboard).call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    private record EnigmaRunner(int[] rotorTypes, char[] rotorPositions, String plugboard) implements Callable<Double> {

        @Override
        public Double call() {
            String[] args = {
                    "--input=" + text,
                    "--plugboard=" + plugboard,
                    "--left-rotor=" + rotorTypes[0],
                    "--left-rotor-position=" + rotorPositions[0],
                    "--middle-rotor=" + rotorTypes[1],
                    "--middle-rotor-position=" + rotorPositions[1],
                    "--right-rotor=" + rotorTypes[2],
                    "--right-rotor-position=" + rotorPositions[2]
            };

            try {
                String result = new EnigmaApp().run(args);
                return Score.evaluate(result);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ConfigurationScore {
        private final int[] rotorTypes;
        private final char[] rotorPositions;
        private final String plugboard;
        private final double score;

        public ConfigurationScore(int[] rotorTypes, char[] rotorPositions, String plugboard, double score) {
            this.rotorTypes = rotorTypes;
            this.rotorPositions = rotorPositions;
            this.plugboard = plugboard;
            this.score = score;
        }

        public int[] getRotorTypes() {
            return rotorTypes;
        }

        public char[] getRotorPositions() {
            return rotorPositions;
        }

        public String getPlugboard() {
            return plugboard;
        }

        public double getScore() {
            return score;
        }
    }
}