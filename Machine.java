package enigma;
import java.util.ArrayList;
import java.util.Collection;


import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author KaitoGarcia
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = new ArrayList<>();
        _myRotors = new ArrayList<>();
        _allRotors.addAll(allRotors);
        if (_allRotors.isEmpty()) {
            throw new EnigmaException("empty rotors");
        }

    }


    /** Return the number of rotor slots I have. */
    int numRotors() {
        if (_numRotors > 1 && _numRotors > _pawls) {
            return _numRotors;
        }
        throw new EnigmaException("num rotors must be > 0");
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        if (_pawls > -1 && _pawls < _numRotors) {
            return _pawls;
        }
        throw new EnigmaException("num pawls must be > 0");
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {

        for (String rotor : rotors) {
            for (Rotor r : _allRotors) {
                String rotorName = r.name();
                if (!_allRotors.contains(rotorName)) {
                    throw new EnigmaException("rotor doesnt exist");
                }
                if (rotor.equals(rotorName)) {
                    _myRotors.add(r);
                }
            }
        }


    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {

        for (int i = 0; i < setting.length(); i++) {
            if (_alphabet.contains(setting.charAt(i))) {
                _myRotors.get(i + 1).set(setting.charAt(i));
            }
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {

        for (int i = 0; i < numRotors(); i++) {
            if (i == numRotors() - 1 || _myRotors.get(i + 1).atNotch()) {
                for (int j = i; j < numRotors() - 1; j++) {
                    _myRotors.get(j).advance();
                }
                break;
            }
        }
        _myRotors.get(numRotors() - 1).advance();


        int result = _plugboard.permute(c);
        for (int i = _numRotors - 1; i > -1; i--) {
            result = _myRotors.get(i).convertForward(result);
        }
        for (int i = 1; i < numRotors(); i++) {
            result = _myRotors.get(i).convertBackward(result);
        }
        result = _plugboard.invert(result);
        return result;
    }


    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        StringBuilder result = new StringBuilder();
        char msgChar;
        for (int i = 0; i < msg.length(); i++) {
            msgChar = _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
            result.append(msgChar);
        }
        return result.toString();
    }


    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** num of rotors in machine. */
    private int _numRotors;

    /** num of pawls in machine. */
    private int _pawls;

    /** plugboard. */
    private Permutation _plugboard;

    /** array of all possible rotors. */
    private ArrayList<Rotor> _allRotors;

    /** array of my rotors. */
    private ArrayList<Rotor> _myRotors;
}
