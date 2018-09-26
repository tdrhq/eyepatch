// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import java.util.Objects;

public class Pair {
    public final Object first;
    public final Object second;

    private Pair(Object first, Object second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        return oHashCode(first) * 31 + oHashCode(second);
    }

    @Override
    public boolean equals(Object _other) {
        if (_other == null || !(_other instanceof Pair)) {
            return false;
        }

        Pair other = (Pair) _other;
        return oEquals(first, other.first) &&
                oEquals(second, other.second);
    }

    private static int oHashCode(Object o) {
        if (o == null) {
            return 10;
        }
        return o.hashCode();
    }

    private static boolean oEquals(Object one, Object two) {
        if (one == null) {
            return two == null;
        }

        return one.equals(two);
    }

    public static Pair create(Object first, Object second) {
        return new Pair(first, second);
    }
}
