package org.bert.types;

import java.util.ArrayList;

/**
 * Created by frepond on 19/8/14.
 */
public class List extends ArrayList<Object> {
    public boolean isProper = true;

    public List(int len) {
        super(len);
    }
}
