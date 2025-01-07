# Enigma Machine Configuration Project

This project is a Java-based implementation for configuring and evaluating Enigma machine settings. It includes classes for managing rotor types, rotor positions, plugboard settings, and scoring configurations based on various fitness metrics.

## Project Structure

- `src/main/java/es/usj/crypto/`
  - `EnigmaConfig.java`: Represents the configuration of an Enigma machine.
  - `Main.java`: Contains the main method for testing the EnigmaConfig class.
- `src/main/java/es/usj/crypto/Fitness/`
  - `Score.java`: Evaluates the fitness of a given text using various metrics.
  - `BigramFitness.java`, `TrigramFitness.java`, `QuadgramFitness.java`, `EnglishWordChecker.java`, `IndexOfCoincidence.java`: Classes for different fitness scoring methods.

## EnigmaConfig Class

### Purpose
Represents the configuration of an Enigma machine, including rotor types, rotor positions, and plugboard settings.

### Attributes
- `rotorTypes`: Types of rotors used.
- `rotorPositions`: Positions of the rotors.
- `plugboard`: Plugboard configuration.
- `score`: Score of the configuration.

### Methods
- Getters and setters for attributes.
- `findMapping`: Finds the mapping of a character in the plugboard.
- `addPlug`: Adds a new plug to the plugboard.
- `containsDuplicateCharacters`: Checks for duplicate characters in the plugboard.
- `getFixedPlugboard`: Returns a fixed-size plugboard configuration.
- `toString`, `equals`, and `hashCode` methods for object representation and comparison.

## Score Class

### Purpose
Deciphers a code ciphered with the custom enigma implementation

## Usage

### Running the Project
To run the project, you have 2 functionalities:
-Main class (Burte force + Heuristics Approach)
-Bombe class (Crib + Heuristics Approach)
Note: The bombe class

## Dependencies
- Java Development Kit (JDK)
- Maven (for managing dependencies and building the project)

## Building the Project
To build the project, use the following Maven command:
mvn clean install

## License
This project is licensed under the MIT License. See the `LICENSE` file for more details.
