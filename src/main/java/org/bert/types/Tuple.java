package org.bert.types;


/**
 * Created by frepond on 19/8/14.
 */
public class Tuple {
    private Object[] values;

    public Tuple(int size) {
        values = new Object[size];
    }

    public Object get(int i) {
        if (i > values.length)
            throw new InvalidTupleArityException("Actual size " + values.length + ", accessing " + i);

        return values[i];
    }

    public void put(int i, Object val) {
        values[i] = val;
    }

    public int size() {
        return values.length;
    }
}
