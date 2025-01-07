package es.usj.crypto.Fitness;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The TrigramFitness class calculates the fitness score of a given text based on trigram frequencies.
 */
public class TrigramFitness {
    private final float[][][] trigramScore = new float[26][26][26];
    private float mean;
    private float stdDev;

    /**
     * Constructs a TrigramFitness object and loads trigram scores from a JSON file.
     */
    public TrigramFitness() {
        // Load trigrams.json into a 3D array for faster lookup
        Path path = Path.of("src/main/resources/data/trigrams.json");
        try (FileReader reader = new FileReader(path.toFile())) {
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(reader);
            JsonArray jsonArray = root.getAsJsonArray();

            // Populate the trigramScores array
            for (JsonElement dataElement : jsonArray) {
                JsonObject innerObject = dataElement.getAsJsonObject();
                String trigram = innerObject.get("trigram").getAsString().toUpperCase();
                float score = innerObject.get("freq").getAsFloat();

                // Convert trigram characters to indices
                int char1 = trigram.charAt(0) - 'A';
                int char2 = trigram.charAt(1) - 'A';
                int char3 = trigram.charAt(2) - 'A';

                // Store the score in the trigramScores array
                trigramScore[char1][char2][char3] = score;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        calculateMeanAndStdDev();
    }

    /**
     * Calculates the mean and standard deviation of the trigram scores.
     */
    private void calculateMeanAndStdDev() {
        // Calculate mean
        float sum = 0;
        int count = 0;
        for (float[][] matrix : trigramScore) {
            for (float[] row : matrix) {
                for (float score : row) {
                    if (score > 0) {
                        sum += score;
                        count++;
                    }
                }
            }
        }
        mean = sum / count;

        // Calculate standard deviation
        float variance = 0;
        for (float[][] matrix : trigramScore) {
            for (float[] row : matrix) {
                for (float score : row) {
                    if (score > 0) {
                        variance += (float) Math.pow(score - mean, 2);
                    }
                }
            }
        }
        stdDev = (float) Math.sqrt(variance / count);
    }

    /**
     * Calculates the fitness score of the given text based on trigram frequencies.
     *
     * @param text The text to evaluate.
     * @return The fitness score of the text.
     */
    public double score(String text) {
        double fitness = 0;
        int totalTrigrams = 0;

        String[] words = text.split(" ");

        for (String word : words) {
            if (word.length() < 3) continue;

            for (int j = 0; j < word.length() - 2; j++) {
                int char1 = Character.toUpperCase(word.charAt(j)) - 'A';
                int char2 = Character.toUpperCase(word.charAt(j + 1)) - 'A';
                int char3 = Character.toUpperCase(word.charAt(j + 2)) - 'A';

                if (char1 >= 0 && char1 < 26 && char2 >= 0 && char2 < 26 && char3 >= 0 && char3 < 26) {
                    fitness += trigramScore[char1][char2][char3];
                    totalTrigrams++;
                }
            }
        }

        // Handle case of no valid trigrams
        if (totalTrigrams == 0) {
            return 0; // Neutral score
        }

        // Z-Score Normalization
        double zScore = (fitness - (totalTrigrams * mean)) / (totalTrigrams * stdDev);

        // Convert Z-Score to probability-like value between 0 and 1
        return Math.max(0, Math.min(1, (zScore + 3) / 6)); // Clamp between 0 and 1
    }
}