package org.bert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(JUnit4.class)
public class BertDecodeTest {

	@Test(expected = BertException.class)
    public void testInvalid() throws BertException {
		byte[] data = { 1, 2, 3 };
		Bert bert = new Bert(data);
    }

	@Test(expected = BertException.class)
    public void testAnotherInvalid() throws BertException {
		byte[] data = { (byte) 131, 8 };
		Bert bert = new Bert(data);
    }

	@Test
    public void testSmallInt() throws BertException {
		byte[] data = { (byte) 131, (byte) 97, (byte) -1};
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assertEquals(o, 255);
		assertNotEquals(o, -1);
	}

	@Test
    public void testInt() throws BertException {
		byte[] data = { (byte) 131, (byte) 98, (byte) 255, (byte) 255, (byte) 255, (byte) 133 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assertEquals(o, -123);
	}

	@Test
    public void testFloat() throws BertException {
		byte[] data = { (byte) 131, 70, (byte) 191, (byte) 243, (byte) 174, 20, 122, (byte) 225, 71, (byte) 174 };

		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assertEquals(o, -1.23);
	}

	@Test
    public void testAtom() throws BertException {
		byte[] data = { (byte) 131, 100, 0, 4, 116, 101, 115, 116 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assertEquals(((Bert.Atom) o).toString(), "test");
	}

	@Test
    public void testString() throws BertException {
		byte[] data = { (byte) 131, 107, 0, 3, 97, 98, 99 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assertEquals((String) o, "abc");
	}

	@Test
    public void testBinary() throws BertException {
		byte[] data = { (byte) 131, 109, 0, 0, 0, 3, 97, 98, 99 };
		byte[] verify = { 'a', 'b', 'c' };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assert(Arrays.equals(verify, (byte[]) o));
	}

	@Test
    public void testSmallTuple() throws BertException {
		byte[] data = { (byte) 131, 104, 3, 100, 0, 1, 97, 100, 0, 1, 98, 100, 0, 1, 99 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assert(o instanceof Bert.Tuple);
		Bert.Tuple l = (Bert.Tuple) o;
		assertEquals(l.size(), 3);
		assert(l.get(0) instanceof Bert.Atom);
		assert(l.get(1) instanceof Bert.Atom);
		assert(l.get(2) instanceof Bert.Atom);
		assertEquals(((Bert.Atom) l.get(0)).toString(), "a");
		assertEquals(((Bert.Atom) l.get(1)).toString(), "b");
		assertEquals(((Bert.Atom) l.get(2)).toString(), "c");
	}

	@Test
    public void testLargeTuple() throws BertException {
		byte[] data = { (byte) 131, 105, 0, 0, 0, 3, 100, 0, 1, 97, 100, 0, 1, 98, 100, 0, 1, 99 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assert(o instanceof Bert.Tuple);
		Bert.Tuple l = (Bert.Tuple) o;
		assertEquals(l.size(), 3);
		assert(l.get(0) instanceof Bert.Atom);
		assert(l.get(1) instanceof Bert.Atom);
		assert(l.get(2) instanceof Bert.Atom);
		assertEquals(((Bert.Atom) l.get(0)).toString(), "a");
		assertEquals(((Bert.Atom) l.get(1)).toString(), "b");
		assertEquals(((Bert.Atom) l.get(2)).toString(), "c");
	}

	@Test
    public void testList() throws BertException {
		byte[] data = { (byte) 131, 108, 0, 0, 0, 3, 100, 0, 1, 97, 100, 0, 1, 98, 100, 0, 1, 99, 106 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assert(o instanceof Bert.List);

		Bert.List l = (Bert.List) o;
		assertEquals(l.size(), 3);
		assert(l.get(0) instanceof Bert.Atom);
		assert(l.get(1) instanceof Bert.Atom);
		assert(l.get(2) instanceof Bert.Atom);
		assertEquals(((Bert.Atom) l.get(0)).toString(), "a");
		assertEquals(((Bert.Atom) l.get(1)).toString(), "b");
		assertEquals(((Bert.Atom) l.get(2)).toString(), "c");

		assert(l.isProper);
	}

	@Test
    public void testImproperList() throws BertException {
		byte[] data = { (byte) 131, 108, 0, 0, 0, 3, 100, 0, 1, 97, 100, 0, 1, 98, 100, 0, 1, 99, 100, 0, 1, 100 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();
		assert(o instanceof Bert.List);

		Bert.List l = (Bert.List) o;
		assertEquals(l.size(), 4);
		assert(l.get(0) instanceof Bert.Atom);
		assert(l.get(1) instanceof Bert.Atom);
		assert(l.get(2) instanceof Bert.Atom);
		assert(l.get(3) instanceof Bert.Atom);
		assertEquals(((Bert.Atom) l.get(0)).toString(), "a");
		assertEquals(((Bert.Atom) l.get(1)).toString(), "b");
		assertEquals(((Bert.Atom) l.get(2)).toString(), "c");
		assertEquals(((Bert.Atom) l.get(3)).toString(), "d");

		assert(!l.isProper);
	}

	@Test
    public void testBertTime() throws BertException {
		byte[] data = { (byte) 131, 104, 5, 100, 0, 4, 98, 101, 114, 116, 100, 0, 4, 116, 105, 109, 101, 98, 0, 0, 5, 116, 98, 0, 7, (byte) 189, (byte) 136, 98, 0, 14, 84, (byte) 185 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();

		assert(o instanceof Bert.Time);

		Bert.Time btime = (Bert.Time) o;
	
		long time = 1396507272939L;

		assertEquals(btime.timestamp, time);
		assertEquals(btime.megasecond, 1396);
		assertEquals(btime.second, 507272);
		assertEquals(btime.microsecond, 939193);

		Date date = new Date(btime.timestamp);
	
		assertEquals(date.getYear(), 114);
		assertEquals(date.getMonth(), 3);
		assertEquals(date.getDate(), 3);

		assertEquals(date.getMinutes(), 41);
		assertEquals(date.getSeconds(), 12);
	}

	@Test
    public void testBertTrueFalseNull() throws BertException {
		byte[] t = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 4, 116, 114, 117, 101 };
		Bert bertt = new Bert(t);
		assertEquals((boolean) bertt.getValue(), true);

		byte[] f = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 5, 102, 97, 108, 115, 101 };
		Bert bertf = new Bert(f);
		assert(!((boolean) bertf.getValue()));

		byte[] n = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 3, 110, 105, 108 };
		Bert bertn = new Bert(n);
		assertEquals(bertn.getValue(), null);
	}

	@Test
	public void testDict() throws BertException {
		byte[] data = { (byte) 131, 104, 3, 100, 0, 4, 98, 101, 114, 116, 100, 0, 4, 100, 105, 99, 116, 108, 0,
						0, 0, 2, 104, 2, 100, 0, 3, 97, 103, 101, 107, 0, 1, 30, 104, 2, 100, 0, 4, 110, 97, 109,
						101, 108, 0, 0, 0, 1, 109, 0, 0, 0, 3, 84, 111, 109, 106, 106 };
		Bert bert = new Bert(data);
		Object o = bert.getValue();

		assert(o instanceof Bert.Dict);

		Bert.Atom name = Bert.Atom.get("name");

		Bert.Atom age = Bert.Atom.get("age");

		Bert.Dict d = (Bert.Dict) o;
		assertEquals(d.size(), 2);
		assert(d.containsKey(name));
		assert(d.containsKey(age));

		byte[] tom = { 'T', 'o', 'm' };
		Bert.List l = (Bert.List) d.get(name);
		assertEquals(l.size(), 1);
		assert(l.get(0) instanceof byte[]);
		assert(Arrays.equals((byte[]) l.get(0), (byte[]) tom));

		String l0 = (String) d.get(age);
		assertEquals(l0.length(), 1);
		assertEquals(l0.charAt(0), 30);
	}

    @Test
    public void testSmallBigInt() throws BertException {
        byte[] data = { (byte) 131, 110, 5, 0, 0, (byte) 228, 11, 84, 2 };

        Bert bert = new Bert(data);
        BigInteger bi = (BigInteger) bert.getValue();

        assertEquals(bi, BigInteger.valueOf(10000000000L));
    }

    @Test
    public void testNegativeSmallBigInt() throws BertException {
        byte[] data = { (byte) 131, 110, 5, 1, 0, (byte) 228, 11, 84, 2 };

        Bert bert = new Bert(data);
        BigInteger bi = (BigInteger) bert.getValue();

        assertEquals(bi, BigInteger.valueOf(-10000000000L));
    }

}
