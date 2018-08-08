// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.Arrays;

public class GeneratedMethod {
    private Class klass;
    private String method;
    private Class[] argTypes;

    public GeneratedMethod(Class klass, String method, Class[] argTypes) {
        this.klass = klass;
        this.method = method;
        this.argTypes = argTypes;
    }

    public Class getTargetClass() {
        return klass;
    }

    public String getMethod() {
        return method;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GeneratedMethod)) {
            return false;
        }

        GeneratedMethod otherMethod = (GeneratedMethod) other;
        return klass.equals(otherMethod.klass) &&
                method.equals(otherMethod.method) &&
                Arrays.equals(argTypes, otherMethod.argTypes);
    }
}
