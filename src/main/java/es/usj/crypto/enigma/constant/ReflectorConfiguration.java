package es.usj.crypto.enigma.constant;

/**
 * Enum representing the character pairings used in the Enigma machine's Reflector component.
 *
 * Each configuration defines a specific set of character pairings, ensuring that each character is mapped
 * to another character in the ALPHABET. These pairings are symmetric, meaning each character's mapping
 * reflects back on itself, but no character can be mapped to itself.
 */
public enum ReflectorConfiguration {

    // Enigma M4 - Beta model configuration for the Reflector
    REFLECTOR_DEFAULT("LE:YJ:VC:NI:XW:PB:QM:DR:TA:KZ:GF:UH:OS");

    // String representing the reflector pairings
    private final String sequence;

    /**
     * Constructor for the ReflectorConfiguration enum.
     *
     * @param sequence The character pairings used by the Reflector, represented as a colon-separated string of pairs.
     */
    ReflectorConfiguration(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Retrieves the character pairing sequence for the Reflector.
     *
     * @return The sequence of paired characters in the Reflector, represented as a colon-separated string.
     */
    public String getSequence() {
        return sequence;
    }

}
