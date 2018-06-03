// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.Arrays;

public class Invocation {
    private final Class mClass;
    private Object mInstance;
    private final String mMethod;
    private Class[] argTypes;
    private Object[] mArgs;

    public Object[] getArgs() {
        return mArgs;
    }

    public Class[] getArgTypes() {
        return argTypes;
    }


    public Object getInstance() {
        return mInstance;
    }

    public String getMethod() {
        return mMethod;
    }

    public Class getInstanceClass() {
        return mClass;
    }

    public Invocation(Class klass,
                      Object instance,
                      String method,
                      Class[] argTypes,
                      Object[] args) {
        mClass = klass;
        mMethod = method;
        mInstance = instance;
        this.argTypes = argTypes;
        mArgs = args;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Invocation));
        Invocation otherInvocation = (Invocation) other;
        return mClass.getName().equals(otherInvocation.mClass.getName())
                && mClass.getClassLoader() == otherInvocation.mClass.getClassLoader()
                && mInstance == otherInvocation.mInstance
                && mMethod.equals(otherInvocation.mMethod)
                && Arrays.equals(argTypes, otherInvocation.argTypes)
                && Arrays.equals(mArgs, otherInvocation.mArgs);

    }

    @Override
    public String toString() {
        return String.format(
                "%s(from %s); %s; %s; %s",
                mClass.toString(),
                mClass.getClassLoader().toString(),
                mInstance.toString(),
                mMethod,
                Arrays.toString(mArgs));
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
