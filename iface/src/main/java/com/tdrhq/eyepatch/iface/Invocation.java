// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import java.util.Arrays;

public class Invocation {
    private final GeneratedMethod mGeneratedMethod;
    private Object mInstance;
    private Object[] mArgs;

    public Object[] getArgs() {
        return mArgs;
    }

    public Class[] getArgTypes() {
        return mGeneratedMethod.getArgTypes();
    }


    public Object getInstance() {
        return mInstance;
    }

    public String getMethod() {
        return mGeneratedMethod.getMethod();
    }

    public Class getInstanceClass() {
        return mGeneratedMethod.getTargetClass();
    }

    public Invocation(GeneratedMethod generatedMethod,
                      Object instance,
                      Object[] args) {
        mGeneratedMethod = generatedMethod;
        mInstance = instance;
        mArgs = args;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Invocation));
        Invocation otherInvocation = (Invocation) other;
        return mGeneratedMethod.equals(otherInvocation.mGeneratedMethod)
                && mInstance == otherInvocation.mInstance
                && Arrays.equals(mArgs, otherInvocation.mArgs);

    }

    @Override
    public String toString() {
        return String.format(
                "%s(from %s); %s; %s; %s",
                getInstanceClass().toString(),
                getInstanceClass().getClassLoader().toString(),
                String.valueOf(mInstance),
                getMethod(),
                Arrays.toString(mArgs));
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
