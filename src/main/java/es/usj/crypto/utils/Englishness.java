package es.usj.crypto.utils;

import java.util.HashMap;
import java.util.Map;

public class Englishness {

    private static final Map<Character, Double> FREQUENCY_TABLE = new HashMap<>();

    static {
        FREQUENCY_TABLE.put('a', 0.08167);
        FREQUENCY_TABLE.put('b', 0.01492);
        FREQUENCY_TABLE.put('c', 0.02782);
        FREQUENCY_TABLE.put('d', 0.04253);
        FREQUENCY_TABLE.put('e', 0.1270);
        FREQUENCY_TABLE.put('f', 0.02228);
        FREQUENCY_TABLE.put('g', 0.02015);
        FREQUENCY_TABLE.put('h', 0.06094);
        FREQUENCY_TABLE.put('i', 0.06966);
        FREQUENCY_TABLE.put('j', 0.00153);
        FREQUENCY_TABLE.put('k', 0.00772);
        FREQUENCY_TABLE.put('l', 0.04025);
        FREQUENCY_TABLE.put('m', 0.02406);
        FREQUENCY_TABLE.put('n', 0.06749);
        FREQUENCY_TABLE.put('o', 0.07507);
        FREQUENCY_TABLE.put('p', 0.01929);
        FREQUENCY_TABLE.put('q', 0.00095);
        FREQUENCY_TABLE.put('r', 0.05987);
        FREQUENCY_TABLE.put('s', 0.06327);
        FREQUENCY_TABLE.put('t', 0.09056);
        FREQUENCY_TABLE.put('u', 0.02758);
        FREQUENCY_TABLE.put('v', 0.00978);
        FREQUENCY_TABLE.put('w', 0.02360);
        FREQUENCY_TABLE.put('x', 0.00150);
        FREQUENCY_TABLE.put('y', 0.01974);
        FREQUENCY_TABLE.put('z', 0.00074);
    }

    public static double englishness(String text) {
        String lowerText = text.toLowerCase();
        int totalCharacters = lowerText.length();
        Map<Character, Integer> characterCounts = new HashMap<>();

        for (char c : lowerText.toCharArray()) {
            characterCounts.put(c, characterCounts.getOrDefault(c, 0) + 1);
        }

        double coefficient = 0.0;

        for (Map.Entry<Character, Integer> entry : characterCounts.entrySet()) {
            char character = entry.getKey();
            double frequencyInText = (double) entry.getValue() / totalCharacters;
            double frequencyInEnglish = FREQUENCY_TABLE.getOrDefault(character, 0.0);
            coefficient += Math.sqrt(frequencyInText * frequencyInEnglish);
        }

        return coefficient;
    }

    public static void main(String[] args) {
        System.out.println(englishness("Hello, World!"));
    }
}