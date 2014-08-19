package org.bert.types;

/**
 * Created by frepond on 19/8/14.
 */
public class Time {

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