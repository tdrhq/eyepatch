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
        return Objects.hashCode(first) * 31 + Objects.hashCode(second);
    }

    @Override
    public boolean equals(Object _other) {
        if (_other == null || !(_other instanceof Pair)) {
            return false;
        }

        Pair other = (Pair) _other;
        return Objects.equals(first, other.first) &&
                Objects.equals(second, other.second);
    }

    public static Pair create(Object first, Object second) {
        return new Pair(first, second);
    }
}
