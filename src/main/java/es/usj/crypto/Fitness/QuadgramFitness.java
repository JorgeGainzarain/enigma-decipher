package es.usj.crypto.Fitness;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The QuadgramFitness class calculates the fitness score of a given text based on quadgram frequencies.
 */
public class QuadgramFitness {
    private final float[][][][] quadgramScores = new float[26][26][26][26];
    private float mean;
    private float stdDev;

    /**
     * Constructs a QuadgramFitness object and loads quadgram scores from a file.
     */
    public QuadgramFitness() {
        // Load quadgrams into a 4D array for fast lookup
        try (final InputStream is = QuadgramFitness.class.getResourceAsStream("/data/quadgrams")) {
            assert is != null;
            try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
                 final BufferedReader br = new BufferedReader(r)) {

                // Read the first line to set max
                String firstLine = br.readLine();
                if (firstLine != null) {
                    String[] firstEntry = firstLine.split(" ");
                    setQuadgramScore(firstEntry[0], Float.parseFloat(firstEntry[1]));
                }

                // Process the rest of the lines
                br.lines().map(line -> line.split(" "))
                        .forEach(s -> {
                            String key = s[0];
                            float score = Float.parseFloat(s[1]);
                            setQuadgramScore(key, score);
                        });
            }
        } catch (IOException e) {
            // Handle missing quadgram file case
            for (float[][][] matrix3D : this.quadgramScores) {
                for (float[][] matrix2D : matrix3D) {
                    for (float[] array1D : matrix2D) {
                        Arrays.fill(array1D, Float.NaN);
                    }
                }
            }
        }

        calculateMeanAndStdDev();
    }

    /**
     * Sets the quadgram score for a given key.
     *
     * @param key   The quadgram key.
     * @param score The score to set.
     */
    private void setQuadgramScore(String key, float score) {
        int first = key.charAt(0) - 'A';
        int second = key.charAt(1) - 'A';
        int third = key.charAt(2) - 'A';
        int fourth = key.charAt(3) - 'A';
        this.quadgramScores[first][second][third][fourth] = score;
    }

    /**
     * Calculates the mean and standard deviation of the quadgram scores.
     */
    private void calculateMeanAndStdDev() {
        // Calculate mean
        float sum = 0;
        int count = 0;
        for (float[][][] matrix3D : quadgramScores) {
            for (float[][] matrix2D : matrix3D) {
                for (float[] array1D : matrix2D) {
                    for (float score : array1D) {
                        if (score > 0) {
                            sum += score;
                            count++;
                        }
                    }
                }
            }
        }
        mean = sum / count;

        // Calculate standard deviation
        float variance = 0;
        for (float[][][] matrix3D : quadgramScores) {
            for (float[][] matrix2D : matrix3D) {
                for (float[] array1D : matrix2D) {
                    for (float score : array1D) {
                        if (score > 0) {
                            variance += (float) Math.pow(score - mean, 2);
                        }
                    }
                }
            }
        }
        stdDev = (float) Math.sqrt(variance / count);
    }

    /**
     * Calculates the fitness score of the given text based on quadgram frequencies.
     *
     * @param text The text to evaluate.
     * @return The fitness score of the text.
     */
    public double score(String text) {
        double fitness = 0;
        int totalQuadgrams = 0;

        String[] words = text.split(" ");

        for (String word : words) {
            if (word.length() < 4) continue;

            for (int j = 0; j < word.length() - 3; j++) {
                int char1 = Character.toUpperCase(word.charAt(j)) - 'A';
                int char2 = Character.toUpperCase(word.charAt(j + 1)) - 'A';
                int char3 = Character.toUpperCase(word.charAt(j + 2)) - 'A';
                int char4 = Character.toUpperCase(word.charAt(j + 3)) - 'A';

                if (char1 >= 0 && char1 < 26 && char2 >= 0 && char2 < 26 && char3 >= 0 && char3 < 26 && char4 >= 0 && char4 < 26) {
                    fitness += quadgramScores[char1][char2][char3][char4];
                    totalQuadgrams++;
                }
            }
        }

        // Handle case of no valid quadgrams
        if (totalQuadgrams == 0) {
            return 0; // Neutral score
        }

        // Z-Score Normalization
        double zScore = (fitness - (totalQuadgrams * mean)) / (totalQuadgrams * stdDev);

        // Adjust mapping range to prevent premature maximization
        // Adjusted mapping range
        return Math.max(0, Math.min(1, (zScore + 5) / 10));
    }
}