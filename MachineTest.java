package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import static enigma.TestUtils.*;

public class MachineTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Machine machine;
    private ArrayList<Rotor> allRotors;
    private String alpha = UPPER_STRING;

    /* ***** TESTS ***** */


    private void setRotors(HashMap<String, String> given) {
        allRotors = new ArrayList<Rotor>();
        HashMap<String, String> notches = new HashMap<String, String>();
        notches.put("I", "Q");
        notches.put("II", "E");
        notches.put("III", "V");
        notches.put("IV", "J");
        notches.put("V", "Z");
        notches.put("VI", "ZM");
        notches.put("VII", "ZM");
        notches.put("VIII", "ZM");

        for (String name: given.keySet()) {
            if (name.equals("B") || name.equals("C")) {
                allRotors.add(new Reflector(name,
                        new Permutation(given.get(name), new Alphabet(alpha))));
            } else if (name.equals("Beta") || name.equals("Gamma")) {
                allRotors.add(
                        new FixedRotor(name,
                                new Permutation(given.get(name),
                                        new Alphabet(alpha))));
            } else {
                allRotors.add(new MovingRotor(name,
                        new Permutation(given.get(name),
                                new Alphabet(alpha)), notches.get(name)));
            }
        }
    }

    @Test
    public void checkMachine() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 4, 3, allRotors);

    }

    @Test(expected = EnigmaException.class)
    public void testNumRotors1() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 0, 2, allRotors);
        assertEquals(EnigmaException.class, machine.numRotors());
    }
    @Test(expected = EnigmaException.class)
    public void testNumRotors2() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 3, 5, allRotors);
        assertEquals(EnigmaException.class, machine.numRotors());
    }

    @Test(expected = EnigmaException.class)
    public void testNoRotor() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 3, 5, allRotors);
        assertEquals(EnigmaException.class, machine.numRotors());
    }

    @Test
    public void testNumRotors() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 7, 3, allRotors);
        assertEquals(7, machine.numRotors());
    }

    @Test(expected = EnigmaException.class)
    public void testPawls1() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 3, -1, allRotors);
        assertEquals(EnigmaException.class, machine.numPawls());
    }

    @Test(expected = EnigmaException.class)
    public void testPawls2() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 6, 7, allRotors);
        assertEquals(EnigmaException.class, machine.numPawls());
    }

    @Test
    public void testPawls() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 4, 2, allRotors);
        assertEquals(2, machine.numPawls());

        machine = new Machine(new Alphabet(alpha), 2, 1, allRotors);
        assertEquals(1, machine.numPawls());
    }

    @Test
    public void testSetRotors() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 5, 3, allRotors);
        machine.insertRotors(new String[] {"B", "Beta", "III", "IV", "I"});
        machine.setRotors("AXLE");

    }

    @Test
    public void testConvert() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 5, 3, allRotors);
        machine.insertRotors(new String[]{"B", "Beta", "III", "IV", "I"});
        machine.setRotors("AXLE");

        machine.setPlugboard(new Permutation("(YF) (HZ)", new Alphabet(alpha)));
        int y = (new Alphabet(alpha)).toInt('Y');
        int z = (new Alphabet(alpha)).toInt('Z');
        assertEquals(z, machine.convert(y));
    }

    @Test
    public void testConvert2() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 5, 3, allRotors);
        machine.insertRotors(new String[]{"B", "Beta", "I", "II", "III"});
        machine.setRotors("AAAA");

        machine.setPlugboard(new Permutation("", new Alphabet(alpha)));
        int y = (new Alphabet(alpha)).toInt('B');
        int z = (new Alphabet(alpha)).toInt('O');
        assertEquals("ILBDA", machine.convert("HELLO"));
    }
    @Test
    public void testConvert3() {
        setRotors(NAVALA);
        machine = new Machine(new Alphabet(alpha), 5, 3, allRotors);
        machine.insertRotors(new String[]{"B", "Beta", "I", "II", "III"});
        machine.setRotors("AAAA");

        machine.setPlugboard(new Permutation("(BD) (CR)", new Alphabet(alpha)));
        int y = (new Alphabet(alpha)).toInt('B');
        int z = (new Alphabet(alpha)).toInt('M');
        assertEquals(z, machine.convert(y));
    }

}
