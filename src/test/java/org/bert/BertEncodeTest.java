package org.bert;

import org.bert.types.Atom;
import org.bert.types.List;
import org.bert.types.Time;
import org.bert.types.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class BertEncodeTest {

	@Test
	public void encodeAtom() throws BertException {
		Atom atom = Atom.get("demo");
		Bert bert = new Bert();

		byte[] data = bert.encode(atom);

		assertNotNull(data);

		byte[] erlang = { (byte) 131, 100, 0, 4, 100, 101, 109, 111 };

        assertArrayEquals(data, erlang);
	}

	@Test
	public void encodeBertTerm() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(null);
		assertNotNull(data);

		byte[] n = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 3, 110, 105, 108 };
        assertArrayEquals(data, n);

		data = bert.encode(true);
		byte[] t = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 4, 116, 114, 117, 101 };
        assertArrayEquals(data, t);

		data = bert.encode(false);
		byte[] f = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 5, 102, 97, 108, 115, 101 };
        assertArrayEquals(data, f);
	}

	@Test
	public void encodeInt() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(255);
		assertNotNull(data);

		byte[] small = { (byte) 131, (byte) 97, (byte) -1};
        assertArrayEquals(data, small);

		byte[] intv = { (byte) 131, (byte) 98, (byte) 255, (byte) 255, (byte) 255, (byte) 133 };
		data = bert.encode(-123);
        assertArrayEquals(data, intv);
	}
	
	@Test
	public void encodeFloat() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(-1.23);

		byte[] fval = { (byte) 131, 99, 45, 49, 44, 50, 51, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 101, 43, 48, 48, 0, 0, 0, 0 };
        assertArrayEquals(data, fval);
	}

	@Test
	public void encodeErlangNil() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(new List(0));

		byte[] nil = { (byte) 131,104,2,100,0,4,98,101,114,116,100,0,3,110,105,108 };
        assertArrayEquals(data, nil);
	}

	@Test
	public void encodeString() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode("abc");

		byte[] abc = { (byte) 131, 107, 0, 3, 'a', 'b', 'c' };
        assertArrayEquals(data, abc);
	}

	@Test
	public void encodeBinary() throws BertException {
		Bert bert = new Bert();
		byte[] abc = { 'a', 'b', 'c' };
		byte[] data = bert.encode(abc);

		byte[] output = { (byte) 131, 109, 0, 0, 0, 3, 'a', 'b', 'c' };
        assertArrayEquals(data, output);
	}

	@Test
	public void encodeTuple() throws BertException {
		Bert bert = new Bert();
		Tuple tuple = new Tuple(4);
		tuple.put(0, Atom.get("demo"));
		byte[] five = { 5 };
		tuple.put(1, five);
		tuple.put(2, "a");
		tuple.put(3, 1);
		byte[] data = bert.encode(tuple);

        byte[] output = { (byte) 131, 104, 4, 100, 0, 4, 100, 101, 109, 111, 109, 0, 0, 0, 1, 5, 107, 0, 1, 97, 97, 1 };
        assertArrayEquals(data, output);
	}

	@Test
	public void encodeList() throws BertException {
		Bert bert = new Bert();
		List list = new List(4);
		list.add(Atom.get("test"));
		list.add(1);
		list.add("a");
		byte[] five = { 5 };
		list.add(five);

		byte[] data = bert.encode(list);
		
		byte[] output = { (byte) 131, 108, 0, 0, 0, 4, 100, 0, 4, 116, 101, 115, 116, 97, 1, 107, 0, 1, 97, 109, 0, 0, 0, 1, 5, 106 };
        assertArrayEquals(data, output);
	}

	@Test
	public void encodeComplex() throws BertException, java.io.UnsupportedEncodingException {
		Bert bert = new Bert();
		List list = new List(2);

		Tuple user = new Tuple(2);
		user.put(0, Atom.get("user"));
		user.put(1, "demo".getBytes("UTF-8"));

		Tuple pass= new Tuple(2);
		pass.put(0, Atom.get("pass"));
		pass.put(1, "12346".getBytes("UTF-8"));

		list.add(user);
		list.add(pass);

		byte[] data = bert.encode(list);
		byte[] output = { (byte) 131, 108, 0, 0, 0, 2, 104, 2, 100, 0, 4, 117, 115, 101,
						  114, 109, 0, 0, 0, 4, 100, 101, 109, 111, 104, 2, 100, 0, 4, 
						  112, 97, 115, 115, 109, 0, 0, 0, 5, 49, 50, 51, 52, 54, 106 };
		assertEquals(data.length, output.length);

		assertArrayEquals(data, output);
	}

	@Test
	public void encodeBertTime() throws BertException {
		Bert bert = new Bert();

		Time time = new Time(1396519216811L);

		assertEquals(time.megasecond, 1396);
		assertEquals(time.second, 519216);
		assertEquals(time.microsecond, 811000);

		byte[] output = { (byte) 131, 104, 5, 100, 0, 4, 98, 101, 114, 116, 100,
						  0, 4, 116, 105, 109, 101, 98, 0, 0, 5, 116, 98, 0, 7,
						  (byte) 236, 48, 98, 0, 12, 95, (byte) 248 };

		byte[] data = bert.encode(time);
        assertArrayEquals(data, output);
	}

    @Test
    public void encodeRecord() throws BertException {
        Atom point = Atom.get("point");
        RecordRegistry.register(point, Point.class, new String[]{"x", "y"});
        Bert bert = new Bert();
        Point p = new Point();

        p.setX(1);
        p.setY(2);

        byte[] data = bert.encode(p);

        byte[] output = {(byte) 131,104,3,100,0,5,112,111,105,110,116,97,1,97,2};
        assertArrayEquals(data, output);
    }

    @Test
    public void encodeWildcard() throws BertException {
        Atom wildcard = Atom.get("_");
        Bert bert = new Bert();

        byte[] data = bert.encode(wildcard);

        byte[] output = new byte[] {(byte) 131,100,0,1,95};
        assertArrayEquals(data, output);
    }

    @Test
    public void encodeEmptyList() throws BertException {
        List empty = new List(0);
        Bert bert = new Bert();

        byte[] data = bert.encode(empty);

        byte[] output = new byte[] {(byte) 131,104,2,100,0,4,98,101,114,116,100,0,3,110,105,108};
        assertArrayEquals(data, output);
    }

}