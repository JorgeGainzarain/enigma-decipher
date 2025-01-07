package es.usj.crypto.Fitness;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The EnglishWordChecker class checks if words are valid English words based on a dictionary and calculates the score of valid words in a given text.
 * It returns the ratio of valid words to total words in the text.
 */
public class EnglishWordChecker {
    private final Set<String> dictionary = new HashSet<>();

    /**
     * Constructs an EnglishWordChecker and loads dictionary words from a resource file into a Set for fast lookup.
     */
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

    /**
     * Checks if a word is in the dictionary.
     *
     * @param word The word to check.
     * @return {@code true} if the word is in the dictionary, otherwise {@code false}.
     */
    public boolean isEnglishWord(String word) {
        return dictionary.contains(word.toLowerCase());
    }

    /**
     * Calculates the valid words score from a given text.
     *
     * @param text The text to evaluate.
     * @return The valid words score.
     */
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

        return (double) validWordCount / totalWords;
    }
}