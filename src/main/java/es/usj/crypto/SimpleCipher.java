package es.usj.crypto;

import es.usj.crypto.utils.EnigmaManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class SimpleCipher {
    static Path plainTextPath = Paths.get("data/plain_text.txt");

    public static void main(String[] args) {
        int[] rotorTypes = {1, 2, 3};
        char[] rotorPositions = {'A', 'A', 'A'};
        String plugboard = "JQ:BR:SZ:HX:TV:EM:NI:AG:UW:KY";

        EnigmaConfig config = new EnigmaConfig(rotorTypes, rotorPositions, plugboard);
        EnigmaManager enigmaManager = new EnigmaManager(plainTextPath);
        String txt = enigmaManager.process(config);
        System.out.println("Initial text: " + txt);
        enigmaManager.shutdown();
    }
}