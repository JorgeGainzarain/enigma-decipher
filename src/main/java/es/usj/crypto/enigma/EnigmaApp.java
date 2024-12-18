package es.usj.crypto.enigma;

import es.usj.crypto.enigma.constant.ReflectorConfiguration;
import es.usj.crypto.enigma.constant.RotorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.util.Optional;

/**
 * Wehrmacht Enigma machine custom implementation with 3 rotors
 *
 * Rotors and reflector are different from the ones used in the original machine, but complexity has been preserved:
 * - 3-rotor with selection out of 5 rotors
 * - Default reflector
 * - 10 plug cables on the plugboard
 *
 * Combinations of 3 rotors out of 5 = (5 x 4 x 3) = 60
 * Each ring can be set in any of 26 positions = (26 x 26 x 26) = 17,576
 * Notch combinations (most-left rotor is excluded) = (26 x 26) = 676
 * Plugboard combinations (10 cables) = 26! / (26 - 2 · 10)! · 10! · 2 · 10 = 150,738,274,937,250
 *
 * Complexity is equals to 60 x 17,576 x 676 x 150,738,274,937,250 = 107,458,687,327,250,619,360,000
 *
 * That can be expressed as 1.07 x 10^23, and it's comparable with a 77 bit key
 *
 * Additional details on Enigma classical configuration available in <a href="https://www.ciphermachinesandcryptology.com/en/enigmatech.htm"/>
 */

@SpringBootApplication
public class EnigmaApp {

    private static final Logger LOG = LoggerFactory.getLogger(EnigmaApp.class);
    private static final int MIN_ROTOR = 1;
    private static final int MAX_ROTOR = 5;
    private static final char MIN_POSITION = 'A';
    private static final char MAX_POSITION = 'Z';

    private String input;
    private String plugboard;
    private int leftRotor;
    private char leftRotorPosition;
    private int middleRotor;
    private char middleRotorPosition;
    private int rightRotor;
    private char rightRotorPosition;
    //private String outFile;

    /**
     * Main method to start the EnigmaApp.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {}

    /**
     * Command line runner that initializes the Enigma machine and processes input/output files.
     *
     * @param args Command line arguments
     * @throws Exception If an error occurs while processing the files
     */


    public String run(String... args) throws Exception {
        try {
            parseArguments(args);
            Machine machine = createMachine();
            return processFile(machine);
        } catch (Exception e) {
            LOG.error("An error occurred: {}", e.getMessage(), e);
            System.exit(-1);
            return "";
        }
    }

    /**
     * Parses the command line arguments to configure the Enigma machine settings.
     *
     * @param args Command line arguments
     */
    private void parseArguments(String... args) {
        PropertySource<?> ps = new SimpleCommandLinePropertySource(args);

        input = validateRequiredProperty(ps, "input");

        plugboard = validateRequiredProperty(ps, "plugboard");

        leftRotor = parseRotorNumber(validateRequiredProperty(ps, "left-rotor"));
        leftRotorPosition = parseRotorPosition(validateRequiredProperty(ps, "left-rotor-position"));

        middleRotor = parseRotorNumber(validateRequiredProperty(ps, "middle-rotor"));
        middleRotorPosition = parseRotorPosition(validateRequiredProperty(ps, "middle-rotor-position"));

        rightRotor = parseRotorNumber(validateRequiredProperty(ps, "right-rotor"));
        rightRotorPosition = parseRotorPosition(validateRequiredProperty(ps, "right-rotor-position"));

        //outFile = validateRequiredProperty(ps, "output-file");
    }

    /**
     * Validates that a required property is present in the command line arguments.
     *
     * @param ps           PropertySource representing command line arguments
     * @param propertyName The name of the required property
     * @return The value of the property as a string
     * @throws IllegalArgumentException if the property is missing
     */
    private String validateRequiredProperty(PropertySource<?> ps, String propertyName) {
        return Optional.ofNullable(ps.getProperty(propertyName))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("'%s' argument is required.", propertyName)));
    }

    /**
     * Parses the rotor number from the command line argument.
     *
     * @param rotorValue The rotor number as a string
     * @return The rotor number as an integer
     * @throws IllegalArgumentException if the rotor number is out of range
     */
    private int parseRotorNumber(String rotorValue) {
        int rotor = Integer.parseInt(rotorValue);
        if (rotor < MIN_ROTOR || rotor > MAX_ROTOR) {
            throw new IllegalArgumentException("Rotor number must be between " + MIN_ROTOR + " and " + MAX_ROTOR);
        }
        return rotor;
    }

    /**
     * Parses the rotor position from the command line argument.
     *
     * @param positionValue The rotor position as a string
     * @return The rotor position as a character
     * @throws IllegalArgumentException if the rotor position is invalid
     */
    private char parseRotorPosition(String positionValue) {
        if (positionValue.length() != 1 || positionValue.charAt(0) < MIN_POSITION || positionValue.charAt(0) > MAX_POSITION) {
            throw new IllegalArgumentException("Rotor position must be a single letter between " + MIN_POSITION + " and " + MAX_POSITION);
        }
        return positionValue.charAt(0);
    }

    /**
     * Creates and configures the Enigma machine with the provided settings.
     *
     * @return The configured Enigma machine
     */
    private Machine createMachine() {
        return new Machine(
                new Plugboard(plugboard),
                createRotor(rightRotor, rightRotorPosition),
                createRotor(middleRotor, middleRotorPosition),
                createRotor(leftRotor, leftRotorPosition),
                new Reflector(ReflectorConfiguration.REFLECTOR_DEFAULT)
        );
    }

    /**
     * Creates a rotor based on its configuration and initial position.
     *
     * @param rotorNumber     The rotor number (1-5)
     * @param initialPosition The initial position of the rotor (A-Z)
     * @return A new Rotor instance
     */
    private Rotor createRotor(int rotorNumber, char initialPosition) {
        return new Rotor(RotorConfiguration.getRotorConfiguration(rotorNumber), initialPosition);
    }

    /**
     * Processes the input file to cipher the plain text and writes the output to the specified file.
     *
     * @param machine The configured Enigma machine
     * @throws Exception If an error occurs while reading/writing files
     */
    private String processFile(Machine machine) throws Exception {
        return  machine.getCipheredText(input);

        //Files.writeString(Path.of(outFile), output);

        //LOG.debug("IN:  {}", input);
        //LOG.debug("OUT: {}", output);
    }

}