package org.bert;

import org.bert.Bert.Atom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class BertEncodeTest {

	@Test
	public void encodeAtom() throws BertException {
		Bert.Atom atom = Bert.Atom.get("demo");
		Bert bert = new Bert();

		byte[] data = bert.encode(atom);

		assert(data != null);

		byte[] erlang = { (byte) 131, 100, 0, 4, 100, 101, 109, 111 };

		assert(Arrays.equals(data, erlang));
	}

	@Test
	public void encodeBertTerm() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(null);
		assert(data != null);

		byte[] n = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 3, 110, 105, 108 };
		assert(Arrays.equals(data, n));

		data = bert.encode(true);
		byte[] t = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 4, 116, 114, 117, 101 };
		assert(Arrays.equals(data, t));

		data = bert.encode(false);
		byte[] f = { (byte) 131, 104, 2, 100, 0, 4, 98, 101, 114, 116, 100, 0, 5, 102, 97, 108, 115, 101 };
		assert(Arrays.equals(data, f));
	}

	@Test
	public void encodeInt() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(255);
		assert(data != null);

		byte[] small = { (byte) 131, (byte) 97, (byte) -1};
		assert(Arrays.equals(data, small));

		byte[] intv = { (byte) 131, (byte) 98, (byte) 255, (byte) 255, (byte) 255, (byte) 133 };
		data = bert.encode(-123);
		assert(Arrays.equals(data, intv));
	}
	
	@Test
	public void encodeFloat() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(-1.23);

		byte[] fval = { (byte) 131, 99, 45, 49, 44, 50, 51, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 101, 43, 48, 48, 0, 0, 0, 0 };
		assert(Arrays.equals(data, fval));
	}

	@Test
	public void encodeErlangNil() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode(new Bert.List(0));

		byte[] nil = { (byte) 131, 106 };
		assert(Arrays.equals(data, nil));
	}

	@Test
	public void encodeString() throws BertException {
		Bert bert = new Bert();
		byte[] data = bert.encode("abc");

		byte[] abc = { (byte) 131, 107, 0, 3, 'a', 'b', 'c' };
		assert(Arrays.equals(data, abc));
	}

	@Test
	public void encodeBinary() throws BertException {
		Bert bert = new Bert();
		byte[] abc = { 'a', 'b', 'c' };
		byte[] data = bert.encode(abc);

		byte[] output = { (byte) 131, 109, 0, 0, 0, 3, 'a', 'b', 'c' };
		assert(Arrays.equals(data, output));
	}

	@Test
	public void encodeTuple() throws BertException {
		Bert bert = new Bert();
		Bert.Tuple tuple = new Bert.Tuple(3);
		tuple.add(Atom.get("demo"));
		byte[] five = { 5 };
		tuple.add(five);
		tuple.add("a");
		tuple.add(1);
		byte[] data = bert.encode(tuple);
		
		byte[] output = { (byte) 131, 104, 4, 100, 0, 4, 100, 101, 109, 111, 109, 0, 0, 0, 1, 5, 107, 0, 1, 97, 97, 1 };
		assert(Arrays.equals(data, output));
	}

	@Test
	public void encodeList() throws BertException {
		Bert bert = new Bert();
		Bert.List list = new Bert.List(4);
		list.add(Atom.get("test"));
		list.add(1);
		list.add("a");
		byte[] five = { 5 };
		list.add(five);

		byte[] data = bert.encode(list);
		
		byte[] output = { (byte) 131, 108, 0, 0, 0, 4, 100, 0, 4, 116, 101, 115, 116, 97, 1, 107, 0, 1, 97, 109, 0, 0, 0, 1, 5, 106 };
		assert(Arrays.equals(data, output));
	}

	@Test
	public void encodeComplex() throws BertException, java.io.UnsupportedEncodingException {
		Bert bert = new Bert();
		Bert.List list = new Bert.List(2);

		Bert.Tuple user = new Bert.Tuple(2);
		user.add(Atom.get("user"));
		user.add("demo".getBytes("UTF-8"));

		Bert.Tuple pass= new Bert.Tuple(2);
		pass.add(Atom.get("pass"));
		pass.add("12346".getBytes("UTF-8"));

		list.add(user);
		list.add(pass);

		byte[] data = bert.encode(list);
		byte[] output = { (byte) 131, 108, 0, 0, 0, 2, 104, 2, 100, 0, 4, 117, 115, 101,
						  114, 109, 0, 0, 0, 4, 100, 101, 109, 111, 104, 2, 100, 0, 4, 
						  112, 97, 115, 115, 109, 0, 0, 0, 5, 49, 50, 51, 52, 54, 106 };
		assertEquals(data.length, output.length);

		assert(Arrays.equals(data, output));
	}

	@Test
	public void encodeBertTime() throws BertException {
		Bert bert = new Bert();

		Bert.Time time = new Bert.Time(1396519216811L);

		assertEquals(time.megasecond, 1396);
		assertEquals(time.second, 519216);
		assertEquals(time.microsecond, 811000);

		byte[] output = { (byte) 131, 104, 5, 100, 0, 4, 98, 101, 114, 116, 100,
						  0, 4, 116, 105, 109, 101, 98, 0, 0, 5, 116, 98, 0, 7,
						  (byte) 236, 48, 98, 0, 12, 95, (byte) 248 };

		byte[] data = bert.encode(time);
		assert(Arrays.equals(data, output));
	}



}