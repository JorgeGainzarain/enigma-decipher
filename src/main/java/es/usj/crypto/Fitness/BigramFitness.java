package es.usj.crypto.Fitness;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

public class BigramFitness {
    private float[][] bigramScores = new float[26][26];
    private float mean;
    private float stdDev;

    public BigramFitness() {
        // Load bigrams.json into a 2D array for faster lookup
        Path path = Path.of("src/main/resources/data/bigrams.json");
        try (FileReader reader = new FileReader(path.toFile())) {
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(reader);
            JsonObject jsonObject = root.getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("data");

            // Populate the bigramScores array
            for (JsonElement dataElement : jsonArray) {
                JsonArray innerArray = dataElement.getAsJsonArray();
                String bigram = innerArray.get(0).getAsString().toUpperCase();
                float score = innerArray.get(1).getAsFloat();

                // Convert bigram characters to indices
                int char1 = bigram.charAt(0) - 'A';
                int char2 = bigram.charAt(1) - 'A';

                // Store the score in the bigramScores array
                bigramScores[char1][char2] = score;
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        calculateMeanAndStdDev();
    }

    private void calculateMeanAndStdDev() {
        // Calculate mean
        float sum = 0;
        int count = 0;
        for (float[] row : bigramScores) {
            for (float score : row) {
                if (score > 0) {
                    sum += score;
                    count++;
                }
            }
        }
        mean = sum / count;

        // Calculate standard deviation
        float variance = 0;
        for (float[] row : bigramScores) {
            for (float score : row) {
                if (score > 0) {
                    variance += Math.pow(score - mean, 2);
                }
            }
        }
        stdDev = (float) Math.sqrt(variance / count);

    }

    public double score(String text) {
        double fitness = 0;
        int totalBigrams = 0;

        String[] words =text.split(" ");

        for (String word : words) {
            if (word.length() < 2) continue;

            for (int j = 0; j < word.length() - 1; j++) {
                int char1 = Character.toUpperCase(word.charAt(j)) - 'A';
                int char2 = Character.toUpperCase(word.charAt(j + 1)) - 'A';

                if (char1 >= 0 && char1 < 26 && char2 >= 0 && char2 < 26) {
                    fitness += bigramScores[char1][char2];
                    totalBigrams++;
                }
            }
        }

        // Handle case of no valid bigrams
        if (totalBigrams == 0) {
            return 0; // Neutral score
        }

        // Z-Score Normalization
        double zScore = (fitness - (totalBigrams * mean)) / (totalBigrams * stdDev);


        // Convert Z-Score to probability-like value between 0 and 1

        return Math.max(0, Math.min(1, (zScore + 3) / 6));
    }
}
