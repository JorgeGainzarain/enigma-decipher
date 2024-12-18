package es.usj.crypto.Fitness;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class EnglishWordChecker {
    private Set<String> dictionary = new HashSet<>();
    private static final double ENGLISH_MEAN_VALID = 1.0; // Mean valid words ratio for English text
    private static final double ENGLISH_STDDEV_VALID = 0.01; // Assumed small standard deviation for valid words ratio

    public EnglishWordChecker() {
        // Load dictionary words from resource file into a Set for fast lookup
        try (final InputStream is = EnglishWordChecker.class.getResourceAsStream("/data/words.txt")) {
            assert is != null;
            try (final Reader r = new InputStreamReader(is, StandardCharsets.UTF_8);
                 final BufferedReader br = new BufferedReader(r);
                 final Stream<String> lines = br.lines()) {

                lines.map(String::trim)
                        .map(String::toLowerCase)
                        .forEach(dictionary::add);
            }
        } catch (IOException e) {
            // Handle missing dictionary file case
            System.err.println("Error loading dictionary: " + e.getMessage());
        }
    }

    // Function to check if a word is in the dictionary
    public boolean isEnglishWord(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    // Score method to calculate valid words score from a given text
    public double score(String text) {
        // Preprocess the text: convert to uppercase and split into words
        String[] words = text.toUpperCase().split(" ");
        int totalWords = words.length;
        int validWordCount = 0;

        for (String word : words) {
            if (isEnglishWord(word)) {
                validWordCount++;
            }
        }

        if (totalWords == 0) {
            return 0; // Avoid division by zero
        }

        // Calculate valid words ratio
        double validWordsRatio = (double) validWordCount / totalWords;

        return validWordsRatio;
    }

}
