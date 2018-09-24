// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import java.util.Arrays;
import java.util.HashMap;

public class Signature {
    public final String methodName;
    public final Class[] args;

    private static HashMap<Pair, Signature> instances = new HashMap<>();

    public Signature(String methodName, Class[] args) {
        this.methodName = methodName;
        this.args = args;
    }

    public static synchronized Signature create(String methodName, Class[] args) {
        Pair key = Pair.create(methodName, Arrays.asList(args));

        {
            Signature old = instances.get(key);

            if (old != null) {
                return old;
            }
        }

        Signature sig = new Signature(methodName, args);
        instances.put(key, sig);
        return sig;
    }
}
