package es.usj.crypto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CipherTextAnalysis {

    public Map<Integer, List<String>> loadWordsByLength(String wordsFilePath) throws IOException {
        try (InputStream is = CipherTextAnalysis.class.getResourceAsStream(wordsFilePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + wordsFilePath);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<String> words = reader.lines()
                        .map(String::toUpperCase)
                        .toList();
                return words.stream().collect(Collectors.groupingBy(String::length));
            }
        }
    }

    public String readCipherText(String cipherFilePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cipherFilePath), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void main(String[] args) {
        CipherTextAnalysis analysis = new CipherTextAnalysis();
        try {
            Map<Integer, List<String>> wordsByLength = analysis.loadWordsByLength("/data/words_10K.txt");
            String cipherText = analysis.readCipherText("data/cipher.txt");


            // Print the ciphertext
            //System.out.println("Ciphertext: " + cipherText);

            String[] cipherWords = cipherText.split("\\s+");
            Map<String, List<String>> possibleWordsMap = new HashMap<>();

            for (String word : cipherWords) {
                List<String> possibleWords = wordsByLength.get(word.length());
                List<String> filteredWords = possibleWords.stream()
                        .filter(possibleWord -> {
                            for (int i = 0; i < word.length(); i++) {
                                //System.out.println(word.charAt(i) + "=" + possibleWord.charAt(i) + "?");
                                if (word.charAt(i) == possibleWord.charAt(i)) {
                                    //System.out.println(word + "=" + possibleWord);
                                    return false;
                                }
                            }
                            return true;
                        })
                        .collect(Collectors.toList());
                possibleWordsMap.put(word, filteredWords);
            }


            // Print the possible words map sorted by the size of their filtered words from less to more
            possibleWordsMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.comparingInt(List::size)))
                    .forEach(entry -> {
                        System.out.println(entry.getKey() + "->" + entry.getValue().size());
                    });

            // Show all the possible words for the word with the least possible words
            String wordWithLeastPossibleWords = possibleWordsMap.entrySet().stream()
                    .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                    .map(Map.Entry::getKey)
                    .orElseThrow();
            System.out.println("Word with least possible words: " + wordWithLeastPossibleWords);
            System.out.println("Possible words: " + possibleWordsMap.get(wordWithLeastPossibleWords));

            // Show all the possible words for the second word with the least possible words
            String wordWithSecondLeastPossibleWords = possibleWordsMap.entrySet().stream()
                    .filter(entry -> !entry.getKey().equals(wordWithLeastPossibleWords))
                    .min(Comparator.comparingInt(entry -> entry.getValue().size()))
                    .map(Map.Entry::getKey)
                    .orElseThrow();
            System.out.println("Word with second least possible words: " + wordWithSecondLeastPossibleWords);
            System.out.println("Possible words: " + possibleWordsMap.get(wordWithSecondLeastPossibleWords));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}