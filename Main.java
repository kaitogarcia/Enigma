package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author KaitoGarcia
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine machine = readConfig();

        if (!_input.hasNext("(?<=^|\n)\\*.*")) {
            throw error("Invalid start of input file.");
        }

        while (_input.hasNext("(?<=^|\n)\\*.*")) {
            String[] rotors = new String[machine.numRotors()];

            String first = _input.next();
            if (first.equals("*")) {
                rotors[0] = _input.next();
            } else {
                rotors[0] = first.substring(1);
            }

            for (int i = 1; i < machine.numRotors(); i += 1) {
                rotors[i] = _input.next();
            }
            machine.insertRotors(rotors);

            String setting = _input.next();
            setUp(machine, setting);

            String tail = _input.nextLine();
            Scanner scan = new Scanner(tail);
            String set = "";
            if (scan.hasNext() && !scan.hasNext("(?<!\\()(\\(.+\\))(?!\\))")) {
                set = scan.next();
            }

            machine.setRotors(set);

            String cycles = "";
            while (scan.hasNext(".*[\\(|\\)]+.*")) {
                cycles += scan.next();
            }

            machine.setPlugboard(new Permutation(cycles, _alphabet));

            while (_input.hasNextLine() && !_input.hasNext("(?<=^|\n)\\*.*")) {
                String nextLine = _input.nextLine().replaceAll("[ \t]", "");
                printMessageLine(machine.convert(nextLine));
            }

            if (_input.hasNextLine()) {

                _input.useDelimiter("[ \t*]+");
                while (_input.hasNext("(\r\n)+") || _input.hasNext("(\n)+")) {
                    String str = _input.next();
                    str = str.replaceAll("\r", "");
                    for (int i = 0; i < str.length(); i += 1) {
                        _output.print("\r\n");
                    }
                }
                _input.useDelimiter("\\s+");
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            if (!_config.hasNext()) {
                throw new EnigmaException("config empty or no config");
            }

            String advanceToAlphabet = _config.next();
            _alphabet = new Alphabet(advanceToAlphabet);


            if (!_config.hasNextInt()) {
                throw new EnigmaException("invalid num rotors or num pawls");
            }

            int advanceToNumRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("invalid num rotors or num pawls");
            }

            int numRotors = advanceToNumRotors;
            int numPawls = _config.nextInt();

            Collection<Rotor> rotors = new ArrayList<>();
            while (_config.hasNext()) {
                Rotor newRotor = readRotor();
                if (rotors.contains(newRotor)) {
                    throw error("Duplicate rotor");
                } else {
                    rotors.add(newRotor);
                }
            }


            return new Machine(_alphabet, numRotors, numPawls, rotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            Rotor result;
            String rName = _config.next();
            if (rName.contains("(") || rName.contains(")")) {
                throw error("Wrong rotor name format");
            }
            String info = _config.next();
            String notches = info.substring(1);

            StringBuilder cycles = new StringBuilder();
            while (_config.hasNext(".*[\\(|\\)]+.*")) {
                cycles.append(_config.next());
            }

            Permutation perm = new Permutation(cycles.toString(), _alphabet);

            switch (info.charAt(0)) {
            case 'M':
                if (notches.length() < 1) {
                    throw error("No notch");
                }
                for (int i = 0; i < notches.length(); i += 1) {
                    if (!_alphabet.contains(notches.charAt(i))) {
                        throw error("no Notch in alphabet");
                    }
                }
                result = new MovingRotor(rName, perm, notches); break;
            case 'N':
                if (!(notches.length() == 0)) {
                    throw error("Notch for fixed rotor detected.");
                }
                result = new FixedRotor(rName, perm); break;
            case 'R':
                if (notches.length() != 0) {
                    throw error("Notch for reflector detected.");
                }
                result = new Reflector(rName, perm);
                if (!result.permutation().derangement()) {
                    throw error("Reflector is not a derangement.");
                }
                break;
            default:
                throw error("Wrong rotor type. Must be M, N or R.");
            }
            return result;
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        M.setRotors(settings);
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            result.append(c);

            if (((i + 1) % 5 == 0) && (i != msg.length() - 1)) {
                result.append(" ");
            }
        }
        _output.print(result + "\n");

    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;
}
