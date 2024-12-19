package es.usj.crypto.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PlugboardComparator {

    public static boolean arePlugboardsEquivalent(String plugboard1, String plugboard2) {
        Set<String> normalizedPlugboard1 = normalizePlugboard(plugboard1);
        Set<String> normalizedPlugboard2 = normalizePlugboard(plugboard2);
        return normalizedPlugboard1.equals(normalizedPlugboard2);
    }

    private static Set<String> normalizePlugboard(String plugboard) {
        Set<String> normalizedPairs = new HashSet<>();
        String[] pairs = plugboard.split(":");
        for (String pair : pairs) {
            char[] chars = pair.toCharArray();
            Arrays.sort(chars);
            normalizedPairs.add(new String(chars));
        }
        return normalizedPairs;
    }

    public static void main(String[] args) {
        String plugboard1 = "AB:CD:EF";
        String plugboard2 = "BA:DC:FE";
        System.out.println(arePlugboardsEquivalent(plugboard1, plugboard2)); // true
    }
}