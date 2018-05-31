// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.util;

public class Checks {
    public static <T> T notNull(T t) {
        if (t == null) {
            throw new NullPointerException("");
        }

        return t;
    }
}
