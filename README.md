# Custom Enigma Decipher Configuration 

This project is a Java-based implementation for deciphering a custom implementation of enigma

## Project Structure

### Purpose
Deciphers a code ciphered with the custom enigma implementation

## Usage

### Running the Project
To run the project, you have 2 functionalities:
-Main class (Burte force + Heuristics Approach)
-Bombe class (Crib + Heuristics Approach)
Note: The bombe class is kinda slow, takes around 45 minutes, this could (and will) be improved by avoiding the repetitive creation of machines, however for testing pruposes, some of the configs, like the middle and right positions of rotors, can be fixed to the correct ones, that way it will only take a few seconds and you can see the functionality without needing to wait for the whole search

## Dependencies
- Java Development Kit (JDK)
- Maven (for managing dependencies and building the project)

## Building the Project
To build the project, use the following Maven command:
mvn clean install

## License
This project is licensed under the MIT License. See the `LICENSE` file for more details.
