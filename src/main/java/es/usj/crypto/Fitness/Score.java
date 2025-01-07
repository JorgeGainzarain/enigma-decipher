package es.usj.crypto.Fitness;

/**
 * The Score class evaluates the fitness of a given text based on various metrics such as bigram, trigram, quadgram fitness, English word presence, and index of coincidence.
 */
public class Score {

    private static final BigramFitness bigramFitness = new BigramFitness();
    private static final TrigramFitness trigramFitness = new TrigramFitness();
    private static final QuadgramFitness quadgramFitness = new QuadgramFitness();
    private static final EnglishWordChecker englishWordChecker = new EnglishWordChecker();

    /**
     * Weights for each score component
     * Adjust these weights to change the importance of each score component.
     * The sum of all weights should be equal to 1.
     * The higher the weight, the more important the score component.
     */
    // Weights for each score component
    private static final double BIGRAM_WEIGHT = 0.2;
    private static final double TRIGRAM_WEIGHT = 0.2;
    private static final double QUADGRAM_WEIGHT = 0.2;
    private static final double ENGLISH_WORD_WEIGHT = 0.2;
    private static final double IOC_WEIGHT = 0.2;

    /**
     * Evaluates the fitness of the given text.
     *
     * @param text The text to evaluate.
     * @return The fitness score of the text.
     */
    public static double evaluate(String text) {
        double score = 0;
        score += BIGRAM_WEIGHT * bigramFitnessScore(text);
        score += TRIGRAM_WEIGHT * trigramFitnessScore(text);
        score += QUADGRAM_WEIGHT * quadgramFitnessScore(text);
        score += ENGLISH_WORD_WEIGHT * englishWordCheckerScore(text);
        score += IOC_WEIGHT * indexOfCoincidenceScore(text);
        return score;
    }

    /**
     * Calculates the bigram fitness score of the given text.
     *
     * @param text The text to evaluate.
     * @return The bigram fitness score.
     */
    private static double bigramFitnessScore(String text) {
        return bigramFitness.score(text);
    }

    /**
     * Calculates the trigram fitness score of the given text.
     *
     * @param text The text to evaluate.
     * @return The trigram fitness score.
     */
    private static double trigramFitnessScore(String text) {
        return trigramFitness.score(text);
    }

    /**
     * Calculates the quadgram fitness score of the given text.
     *
     * @param text The text to evaluate.
     * @return The quadgram fitness score.
     */
    private static double quadgramFitnessScore(String text) {
        return quadgramFitness.score(text);
    }

    /**
     * Calculates the English word checker score of the given text.
     *
     * @param text The text to evaluate.
     * @return The English word checker score.
     */
    private static double englishWordCheckerScore(String text) {
        return englishWordChecker.score(text);
    }

    /**
     * Calculates the index of coincidence score of the given text.
     *
     * @param text The text to evaluate.
     * @return The index of coincidence score.
     */
    private static double indexOfCoincidenceScore(String text) {
        return IndexOfCoincidence.score(text);
    }
}