package es.usj.crypto.Fitness;

import java.util.HashMap;
import java.util.Map;

public class IndexOfCoincidence {
    // Standard Index of Coincidence for English language
    private static final double ENGLISH_IOC = 0.065;

    /**
     * Calculates the Index of Coincidence for a given text.
     *
     * @param text The input text to analyze
     * @return The Index of Coincidence value
     */
    public static double calculateIoC(String text) {
        // Remove non-alphabetic characters and convert to uppercase
        String cleanedText = text.replaceAll("[^A-Za-z]", "").toUpperCase();

        // If text is too short, return 0
        if (cleanedText.length() <= 1) {
            return 0.0;
        }

        // Count frequency of each letter
        Map<Character, Integer> letterFrequency = new HashMap<>();
        for (char c : cleanedText.toCharArray()) {
            letterFrequency.put(c, letterFrequency.getOrDefault(c, 0) + 1);
        }

        // Calculate IoC
        double numerator = 0.0;
        int totalChars = cleanedText.length();

        for (int count : letterFrequency.values()) {
            numerator += count * (count - 1);
        }

        double denominator = totalChars * (totalChars - 1);

        return numerator / denominator;
    }

    /**
     * Calculates a score based on how close the text's IoC is to English language IoC.
     *
     * @param text The input text to analyze
     * @return A score between 0 and 1, where 1.0 means perfect match to English IoC
     */
    public static double score(String text) {
        double calculatedIoC = calculateIoC(text);

        // Use Gaussian-like scoring function
        // This creates a bell curve centered at the English IoC
        // Peak score of 1.0 at exact English IoC
        // Scores decay symmetrically as IoC moves away from English IoC
        double sigma = 0.01; // Controls the width of the bell curve
        return Math.exp(-Math.pow(calculatedIoC - ENGLISH_IOC, 2) / (2 * sigma * sigma));
    }

    /**
     * Calculates IoC scores for segmented text.
     *
     * @param text The input text to analyze
     * @param segmentLength Length of each segment
     * @return Array of IoC scores for each segment
     */
    public static double[] calculateSegmentedIoCScores(String text, int segmentLength) {
        // Remove non-alphabetic characters and convert to uppercase
        String cleanedText = text.replaceAll("[^A-Za-z]", "").toUpperCase();

        // Prepare segments
        double[] segmentScores = new double[segmentLength];

        for (int offset = 0; offset < segmentLength; offset++) {
            // Collect characters at this offset
            StringBuilder segment = new StringBuilder();
            for (int i = offset; i < cleanedText.length(); i += segmentLength) {
                segment.append(cleanedText.charAt(i));
            }

            // Calculate IoC score for this segment
            segmentScores[offset] = score(segment.toString());
        }

        return segmentScores;
    }

}