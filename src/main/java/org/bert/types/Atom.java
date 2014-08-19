package org.bert.types;

import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by frepond on 19/8/14.
 */
public class Atom {
    private static HashMap<ByteBuffer, Atom> atomTable = new HashMap<>();

    public static Atom get(byte[] val) {
        ByteBuffer bytes = ByteBuffer.wrap(val);
        Atom atom = atomTable.get(bytes);

        if (atom == null) {
            atom = new Atom(bytes);
            atomTable.put(bytes, atom);
        }

        return atom;
    }

    public static Atom get(String name) {
        return get(name.getBytes());
    }

    // predefined Atoms
    public static Atom BERT = Atom.get("bert");
    public static Atom TIME = Atom.get("time");
    public static Atom TRUE = Atom.get("true");
    public static Atom FALSE = Atom.get("false");
    public static Atom NIL = Atom.get("nil");
    public static Atom DICT = Atom.get("dict");

    public final ByteBuffer val;

    private Atom(ByteBuffer val) {
        this.val = val;
    }

    public int hashCode() {
        return val.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Atom)) return false;
        return this == obj;
    }

    public String toString() {
        return new String(val.array());
    }
}