// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GeneratedMethod {
    private final Class klass;
    private final Signature signature;

    private static Map<GeneratedMethod, GeneratedMethod> map = new HashMap<>();

    private GeneratedMethod(Class klass, Signature signature) {
        this.klass = klass;
        this.signature = signature;
    }

    public Class getTargetClass() {
        return klass;
    }

    public String getMethod() {
        return signature.methodName;
    }

    public Class[] getArgTypes() {
        return signature.args;
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GeneratedMethod)) {
            return false;
        }

        GeneratedMethod otherMethod = (GeneratedMethod) other;
        return klass.equals(otherMethod.klass) &&
                signature == otherMethod.signature;
    }

    public static GeneratedMethod create(Class klass, String method, Class[] args) {
        GeneratedMethod ret =  new GeneratedMethod(klass, Signature.create(method, args));
        GeneratedMethod orig = map.get(ret);
        if (orig != null) {
            return orig;
        }

        map.put(ret, ret);
        return ret;
    }
}
