package io.choerodon.kb.infra.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

/**
 * Created by Zenger on 2019/4/30.
 */
public class ToString {
    public ToString() {
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        this.toString(s);
        return s.toString();
    }

    public void toString(StringBuffer s) {
        s.append(super.toString());
    }

    public static String[] stringToArray(String value) {
        BufferedReader reader = new BufferedReader(new StringReader(value));
        LinkedList l = new LinkedList();

        String s;
        try {
            while((s = reader.readLine()) != null) {
                l.add(s);
            }
        } catch (IOException var5) {
            ;
        }

        return (String[])((String[])l.toArray(new String[l.size()]));
    }

    public static String arrayToString(Object[] o) {
        return arrayToString(o, System.getProperty("line.separator"));
    }

    public static String arrayToString(Object[] o, String eol) {
        StringBuffer buf = new StringBuffer();
        if(o.length > 0) {
            for(int i = 0; i < o.length - 1; ++i) {
                buf.append(o[i]);
                buf.append(eol);
            }

            buf.append(o[o.length - 1]);
        }

        return buf.toString();
    }

    public static String toStringOfChars(String s) {
        StringBuffer buf = new StringBuffer();
        byte[] chars = s.getBytes();

        for(int i = 0; i < chars.length; ++i) {
            buf.append('[');
            buf.append(chars[i]);
            buf.append(']');
        }

        return buf.toString();
    }

    public static String[] stringToArray(String text, String newline) {
        return text.split(newline, -1);
    }
}
