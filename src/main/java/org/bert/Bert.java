package org.bert;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

public class Bert {
    private byte[] mFloatStr = new byte[31];
    private ByteBuffer mBuffer = null;
    private ByteArrayOutputStream bao = null;
    private Object mValue = null;

    public static class Atom {
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

    public static class Time {

        public Time() {
        }

        public Time(long ts) {
            timestamp = ts;

            microsecond = (int) ((ts % 1000) * 1000);
            second = (int) ((ts / 1000) % 1000000);
            megasecond = (int) ((ts / 1000) / 1000000);
        }

        public long timestamp = 0;

        public int megasecond = 0;
        public int second = 0;
        public int microsecond = 0;
    }

    public static class Tuple extends ArrayList<Object> {
        public Tuple(int size) {
            super(size);
        }
    }

    public static class List extends ArrayList<Object> {
        public boolean isProper = true;

        public List(int len) {
            super(len);
        }
    }

    public static class Dict extends HashMap<Object, Object> {
    }

    public Bert() {
    }

    private void writeAtom(Atom a, ByteArrayOutputStream bao) throws BertException {
        int len = a.val.array().length;
        if (len >= 65536) throw new BertException("Atom Name too Long");
        bao.write(100);
        bao.write((byte) (len >> 8) & 0x00FF);
        bao.write((byte) (len) & 0x00FF);
        try {
            bao.write(a.val.array());
        } catch (UnsupportedEncodingException ex) {
            throw new BertException("ISO 8859-1 is not Supported at Your Java Environment");
        } catch (IOException ex) {
            throw new BertException(ex.getMessage());
        }
    }

    private void writeTuple(Tuple tuple) throws BertException {
        int len = tuple.size();

        if (len < 256) {
            bao.write(104);
            bao.write((byte) (len & 0x00FF));
        } else {
            bao.write(105);
            bao.write((byte) ((len >> 24) & 0x00FF));
            bao.write((byte) ((len >> 16) & 0x00FF));
            bao.write((byte) ((len >> 8) & 0x00FF));
            bao.write((byte) ((len) & 0x00FF));
        }

        for (int count = 0; count < tuple.size(); count++) {
            encodeTerm(tuple.get(count));
        }
    }

    private void writeList(List list) throws BertException {
        int len = list.size();

        bao.write(108);
        bao.write((byte) ((len >> 24) & 0x00FF));
        bao.write((byte) ((len >> 16) & 0x00FF));
        bao.write((byte) ((len >> 8) & 0x00FF));
        bao.write((byte) ((len) & 0x00FF));

        for (int count = 0; count < list.size(); count++) {
            encodeTerm(list.get(count));
        }

        if (list.isProper) bao.write(106);
    }

    private void encodeTerm(Object o) throws BertException {

        if (o == null) {
            Tuple tup = new Tuple(2);
            tup.add(Atom.BERT);
            tup.add(Atom.NIL);
            writeTuple(tup);
        } else if (o instanceof Boolean) {
            Atom bool = (boolean) o ? Atom.TRUE : Atom.FALSE;
            Tuple tup = new Tuple(2);
            tup.add(Atom.BERT);
            tup.add(bool);
            writeTuple(tup);
        } else if (o instanceof Integer) {
            int value = (int) o;
            if (value >= 0 && value <= 255) {
                bao.write(97);
                bao.write((byte) (value & 0x00FF));
            } else {
                bao.write(98);
                bao.write((byte) ((value >> 24) & 0x00FF));
                bao.write((byte) ((value >> 16) & 0x00FF));
                bao.write((byte) ((value >> 8) & 0x00FF));
                bao.write((byte) ((value) & 0x00FF));
            }
        } else if (o instanceof Double || o instanceof Float) {
            double d = (double) o;
            byte[] val = String.format("%.20e", o).getBytes();
            try {
                bao.write(99);
                bao.write(val);
                if (val.length < 31) {
                    for (int count = 0; count < 31 - val.length; count++) bao.write(0);
                }
            } catch (IOException ex) {
                throw new BertException(ex.getMessage());
            }
        } else if (o instanceof List) {
            List list = (List) o;
            if (list.size() == 0) {
                bao.write(106);
            } else {
                writeList((List) o);
            }
        } else if (o instanceof String) {
            try {
                byte[] str = ((String) o).getBytes("UTF-8");
                bao.write(107);
                bao.write((byte) ((str.length >> 8) & 0x00FF));
                bao.write((byte) ((str.length) & 0x00FF));
                bao.write(str);
            } catch (UnsupportedEncodingException ex) {
                new BertException("String not in UTF-8");
            } catch (IOException ex) {
                new BertException(ex.getMessage());
            }
        } else if (o instanceof Atom) {
            writeAtom((Atom) o, bao);
        } else if (o instanceof byte[]) {
            int value = ((byte[]) o).length;
            bao.write(109);
            bao.write((byte) ((value >> 24) & 0x00FF));
            bao.write((byte) ((value >> 16) & 0x00FF));
            bao.write((byte) ((value >> 8) & 0x00FF));
            bao.write((byte) ((value) & 0x00FF));
            try {
                bao.write((byte[]) o);
            } catch (IOException ex) {
                new BertException(ex.getMessage());
            }
        } else if (o instanceof Time) {
            Time time = (Time) o;

            Tuple tuple = new Tuple(5);
            tuple.add(Atom.BERT);
            tuple.add(Atom.TIME);
            tuple.add(time.megasecond);
            tuple.add(time.second);
            tuple.add(time.microsecond);

            writeTuple(tuple);
        } else if (o instanceof Tuple) {
            writeTuple((Tuple) o);
        }

    }

    public byte[] encode(Object o) throws BertException {
        bao = new ByteArrayOutputStream();
        bao.write(-125);

        encodeTerm(o);

        return bao.toByteArray();
    }


    public Bert(final byte[] data) throws BertException {
        mBuffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);

        byte value = mBuffer.get();
        if (value != -125)
            throw new BertException("Invalid Bert Data");

        mValue = decode();
    }

    private Object decodeBertTerm(Tuple t) throws BertException {
        if (t.get(1) == Atom.TIME &&
                t.get(2) instanceof Integer &&
                t.get(3) instanceof Integer &&
                t.get(4) instanceof Integer) {

            Time time = new Time();

            time.timestamp = ((int) t.get(2) * (long) 1000000 * (long) 1000) + ((int) t.get(3) * (long) 1000) + ((int) t.get(4) / 1000);
            time.megasecond = (int) t.get(2);
            time.second = (int) t.get(3);
            time.microsecond = (int) t.get(4);

            return time;
        } else if (t.get(1) == Atom.NIL) {
            return null;
        } else if (t.get(1) == Atom.TRUE) {
            return true;
        } else if (t.get(1) == Atom.FALSE) {
            return false;
        } else if (t.get(1) == Atom.DICT) {
            Dict d = new Dict();
            List l = (List) t.get(2);

            for (int count = 0; count < l.size(); count++) {
                Tuple tup = (Tuple) l.get(count);
                if (tup.size() != 2)
                    throw new BertException("Invalid Dict Entry");
                d.put(tup.get(0), tup.get(1));
            }

            return d;
        }

        return t;
    }

    private Object decodeSmallTuple() throws BertException {
        int len = mBuffer.get() & 0x00FF; // & 0x00FFFFFFFF;

        Object tag = decode();
        Object obj = RecordAssembler.create(tag, len);

        for (int count = 1; count < len; count++) {
            RecordAssembler.set(tag, count, obj, decode());
        }

        if (obj instanceof Tuple)
            obj = decodeBertTerm((Tuple) obj);

        return obj;
    }

    private Object decodeLargeTuple() throws BertException {
        int len = mBuffer.getInt(); // & 0x00FF;

        Object tag = decode();
        Object obj = RecordAssembler.create(tag, len);

        for (int count = 1; count < len; count++) {
            RecordAssembler.set(tag, count, obj, decode());
        }

        if (tag instanceof Atom && tag == Atom.BERT)
            obj = decodeBertTerm((Tuple) obj);

        return obj;
    }

    public List decodeList() throws BertException {
        int len = mBuffer.getInt();

        List list = new List(len);
        for (int count = 0; count < len; count++) {
            list.add(decode());
        }

        Object o = decode();
        if (!(o instanceof List)) {
            list.add(o);
            list.isProper = false;
        }

        return list;
    }

    private BigInteger decodeBigInt(int tag) {
        int len = 0;
        BigInteger bigint = BigInteger.ZERO;
        BigInteger b = BigInteger.valueOf(256);

        if (tag == 110) // SMALL_BIG_EXT
            len = mBuffer.get() & 0x00FF;
        else // LARGE_BIG_EXT
            len = mBuffer.getInt();

        int sign = mBuffer.get() == 0 ? 1 : -1;

        byte[] buffer = new byte[len];
        mBuffer.get(buffer);

        for (int i = 0; i < buffer.length; i++) {
            BigInteger d = BigInteger.valueOf(buffer[i] & 0x00FF);
            BigInteger p = b.pow(i);
            bigint = bigint.add(d.multiply(p));
        }

        return bigint.multiply(BigInteger.valueOf(sign));
    }

    private Object decode() throws BertException {
        int tag = mBuffer.get() & 0x00FF;
        byte[] val = null;
        long len = 0;

        switch (tag) {
            case 70:
                return mBuffer.getDouble();
            case 97:  // SmallInt Tag
                return (int) (mBuffer.get() & 0x00FF);
            case 98:  // Int Tag
                return mBuffer.getInt();
            case 99:  // FloatTag
                mBuffer.get(mFloatStr);
                return Double.parseDouble(new String(mFloatStr));
            case 100: // AtomTag
                len = mBuffer.getShort(); // & 0x00FFFF;
                val = new byte[(int) len];
                mBuffer.get(val);
                Atom atom = Atom.get(val);
                return atom;
            case 104: // SmallTupleTag
                return decodeSmallTuple();
            case 105: // LargeTupleTag
                return decodeLargeTuple();
            case 106: // NilTag
                return new List(0);
            case 107: // StringTag
                len = mBuffer.getShort(); // & 0x00FFFF;
                val = new byte[(int) len];
                mBuffer.get(val);
                try {
                    return new String(val, "UTF-8");
                } catch (UnsupportedEncodingException ex) {
                    return new String(val);
                }
            case 108: // ListTag
                return decodeList();
            case 109: // BinTag
                len = mBuffer.getInt(); // & 0x00FFFFFFFF;
                val = new byte[(int) len];
                mBuffer.get(val);
                return val;
            case 110:
                return decodeBigInt(tag);
            case 111:
                return decodeBigInt(tag);
            default:
                throw new BertException("Not Supported Bert Tag: " + tag);
        }
    }

    public Object getValue() {
        return mValue;
    }

    public String toString() {
        return mValue == null ? null : mValue.toString();
    }

}
