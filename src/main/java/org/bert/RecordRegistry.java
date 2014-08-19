package org.bert;

import lombok.extern.java.Log;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Quick & dirty bert2obj assembler.
 *
 * Created by frepond on 14/8/14.
 */
@Log
public class RecordRegistry {
    private final static HashMap<Bert.Atom, Class> RECORD_REGISTRY = new HashMap<>();
    private final static HashMap<Class, Bert.Atom> CLASS_REGISTRY = new HashMap<>();
    private final static HashMap<Bert.Atom, Field[]> RECORD_ATTRIBUTES = new HashMap<>();

    public final static void register(Bert.Atom atom, Class clazz, String[] attrs) {
        RECORD_REGISTRY.put(atom, clazz);
        CLASS_REGISTRY.put(clazz, atom);
        Field[] methods = new Field[attrs.length];

        for(int i = 0; i < attrs.length; i++) {
            try {
                methods[i] = clazz.getDeclaredField(attrs[i]);
                methods[i].setAccessible(true);
            } catch(Exception e) {
                log.warning("Error registering " + clazz + "." + attrs[i]);
            }

            RECORD_ATTRIBUTES.put(atom, methods);
        }
    }

    public final static Object create(Object tag, int len) {
        Class recordClass = RECORD_REGISTRY.get(tag);

        // if it's not a tag or not registered, return a basic tuple and set first element
        if (recordClass == null) {
            Bert.Tuple tuple = new Bert.Tuple(len);
            tuple.add(tag);

            return tuple;
        } else { // if it's a registered record, create a new instance of the registered class
            try {
                return recordClass.newInstance();
            }
            catch(Exception e) {
                return new Bert.Tuple(len);
            }
        }
    }

    public final static Object set(Object tag, int index, Object obj, Object val) {
        // if it's a common tuple, just set the tuple element
        if (obj instanceof Bert.Tuple)
            ((Bert.Tuple) obj).add(val);
        else {  // if not, set the object's property
            try {
                RECORD_ATTRIBUTES.get(tag)[index - 1].set(obj, val);
            } catch(Exception e) {
                log.warning("Error setting attribute " + RECORD_ATTRIBUTES.get(tag)[index - 1] +
                        " on " + obj.getClass());
            }
        }

        return obj;
    }

    public final static Bert.Tuple toTuple(Object obj) throws BertException {
        if (obj instanceof Bert.Tuple)
            return (Bert.Tuple) obj;

        Bert.Atom tag = CLASS_REGISTRY.get(obj.getClass());
        Field[] attrs = RECORD_ATTRIBUTES.get(tag);

        if (attrs == null)
            throw new BertException("Not registered " + obj.getClass());

        Bert.Tuple tuple = new Bert.Tuple(attrs.length + 1);

        tuple.add(tag);

        for (int i = 0; i < attrs.length; i++)
            try {
                tuple.add(attrs[i].get(obj));
            } catch(Exception e) {
                log.warning("Error setting attribute " + RECORD_ATTRIBUTES.get(tag)[i] +
                        " on " + obj.getClass());
            }

        return tuple;
    }
}