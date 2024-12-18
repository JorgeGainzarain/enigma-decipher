package es.usj.crypto.enigma;

import es.usj.crypto.Fitness.BigramFitness;
import es.usj.crypto.Fitness.TrigramFitness;
import es.usj.crypto.Fitness.QuadgramFitness;
import es.usj.crypto.Fitness.EnglishWordChecker;
import es.usj.crypto.Fitness.IndexOfCoincidence;

public class Score {

    private static final BigramFitness bigramFitness = new BigramFitness();
    private static final TrigramFitness trigramFitness = new TrigramFitness();
    private static final QuadgramFitness quadgramFitness = new QuadgramFitness();
    private static final EnglishWordChecker englishWordChecker = new EnglishWordChecker();

    // Weights for each score component
    private static final double BIGRAM_WEIGHT = 0.2;
    private static final double TRIGRAM_WEIGHT = 0.2;
    private static final double QUADGRAM_WEIGHT = 0.2;
    private static final double ENGLISH_WORD_WEIGHT = 0.2;
    private static final double IOC_WEIGHT = 0.2;

    public static double evaluate(String text) {
        double score = 0;
        score += BIGRAM_WEIGHT * bigramFitnessScore(text);
        score += TRIGRAM_WEIGHT * trigramFitnessScore(text);
        score += QUADGRAM_WEIGHT * quadgramFitnessScore(text);
        score += ENGLISH_WORD_WEIGHT * englishWordCheckerScore(text);
        score += IOC_WEIGHT * indexOfCoincidenceScore(text);
        return score;
    }

    private static double bigramFitnessScore(String text) {
        return bigramFitness.score(text);
    }

    private static double trigramFitnessScore(String text) {
        return trigramFitness.score(text);
    }

    private static double quadgramFitnessScore(String text) {
        return quadgramFitness.score(text);
    }

    private static double englishWordCheckerScore(String text) {
        return englishWordChecker.score(text);
    }

    private static double indexOfCoincidenceScore(String text) {
        return IndexOfCoincidence.score(text);
    }
}