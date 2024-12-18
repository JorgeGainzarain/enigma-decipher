package es.usj.crypto.enigma.constant;

/**
 * Enum representing the available configurations for the rotors in the Enigma machine.
 *
 * Each rotor configuration consists of a unique ring sequence and a notch position. The ring sequence
 * defines the order of characters, while the notch position indicates the point at which the rotor will
 * cause the adjacent rotor to rotate.
 */
public enum RotorConfiguration {

    // Rotor configurations with their respective ring sequences and notch positions
    ROTOR_I   ("FKQHTLXOCBJSPDZRAMEWNIUYGV", 'H'),
    ROTOR_II  ("SLVGBTFXJQOHEWIRZYAMKPCNDU", 'M'),
    ROTOR_III ("EHRVXGAOBQUSIMZFLYNWKTPDJC", 'V'),
    ROTOR_IV  ("NTZPSFBOKMWRCJDIVLAEYUXHGQ", 'M'),
    ROTOR_V   ("BDFHJLCPRTXVZNYEIWGAKMUSQO", 'D');

    // The sequence of characters on the rotor's ring
    private final String ringSequence;
    // The notch position of the rotor, which determines when it rotates the adjacent rotor
    private final Character notch;

    /**
     * Constructor for the RotorConfiguration enum.
     *
     * @param sequence The character sequence for the rotor's ring.
     * @param notch The character that represents the notch position for the rotor.
     */
    RotorConfiguration(String sequence, Character notch) {
        this.ringSequence = sequence;
        this.notch = notch;
    }

    /**
     * Retrieves the rotor configuration based on the rotor number.
     *
     * @param rotorNumber The number corresponding to the desired rotor configuration (1-5).
     * @return The corresponding RotorConfiguration, or null if the rotor number is invalid.
     */
    public static RotorConfiguration getRotorConfiguration(int rotorNumber) {
        switch (rotorNumber) {
            case 1: return ROTOR_I;
            case 2: return ROTOR_II;
            case 3: return ROTOR_III;
            case 4: return ROTOR_IV;
            case 5: return ROTOR_V;
            default: return null;
        }
    }

    /**
     * Gets the ring sequence of the rotor configuration.
     *
     * @return The ring sequence as a string.
     */
    public String getRingSequence() {
        return ringSequence;
    }

    /**
     * Gets the notch character of the rotor configuration.
     *
     * @return The notch character.
     */
    public Character getNotch() {
        return notch;
    }

}
