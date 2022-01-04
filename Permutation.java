package enigma;
import java.util.HashMap;
import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author KaitoGarcia
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        _cyclesHash = new HashMap<>();
        addCycle(cycles);

        for (int i = 0; i < _alphabet.size(); i++) {
            if (!_cyclesHash.containsKey(_alphabet.toChar(i))) {
                _cyclesHash.put(_alphabet.toChar(i), _alphabet.toChar(i));
            }
        }

    }


    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {

        String tempCycle = cycle;
        tempCycle = tempCycle.replace("(", " ");
        tempCycle = tempCycle.replace(")", " ");
        String[] arrCycle = tempCycle.split(" ");

        for (String a : arrCycle) {
            if (a.length() == 1) {
                _cyclesHash.put(a.charAt(0), a.charAt(0));
            } else {
                for (int i = 0; i < a.length(); i++) {
                    if (i + 1 != a.length()) {
                        _cyclesHash.put(a.charAt(i), a.charAt(i + 1));
                    } else {
                        _cyclesHash.put(a.charAt(i), a.charAt(0));
                    }
                }
            }

        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char charP = _alphabet.toChar(wrap(p));
        if (!alphabet().contains(charP)) {
            throw new EnigmaException("not in alphabet");

        } else if (_cyclesHash.isEmpty() || !_cyclesHash.containsKey(charP)) {
            return p;

        }
        return _alphabet.toInt(_cyclesHash.get(charP));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char charC = _alphabet.toChar(wrap(c));
        if (!alphabet().contains(charC)) {
            throw new EnigmaException("not in alphabet");

        } else if (_cyclesHash.isEmpty()
                || !_cyclesHash.containsKey(charC)) {
            return c;
        }

        char newC = charC;
        for (int i = 0; i < _cyclesHash.size(); i++) {
            if (_cyclesHash.get(_alphabet.toChar(i)) == charC) {
                newC = _alphabet.toChar(i);
            }
        }
        return _alphabet.toInt(newC);

    }

    /** Return the result of applying this permutation to the index of P
        in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!alphabet().contains(p)) {
            throw new EnigmaException("not in alphabet");

        } else if (_cyclesHash.isEmpty() || !_cyclesHash.containsKey(p)) {
            return p;

        }
        return _cyclesHash.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!alphabet().contains(c)) {
            throw new EnigmaException("not in alphabet");

        } else if (_cyclesHash.isEmpty() || !_cyclesHash.containsKey(c)) {
            return c;
        }

        char newC = c;
        for (int i = 0; i < _cyclesHash.size(); i++) {

            char value = _alphabet.toChar(i);
            if (_cyclesHash.get(value) == c) {
                newC = value;
                break;
            }
        }
        return newC;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (HashMap.Entry<Character, Character> letter
                : _cyclesHash.entrySet()) {
            if (letter.getKey() == letter.getValue()) {
                return false;
            }
        }
        return true;
    }


    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Hash. */
    private HashMap<Character, Character> _cyclesHash;

    /** array of cycles (no parentheses). */
    private String[] _cycles;

    /** temp string to spilt cycle. */
    private String holder;

}
