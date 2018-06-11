// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

/**
 * This provides all the information need by the generated class to
 * figure out what to call to super.
 */
public class SuperInvocation {
    private Class[] mArgTypes;
    private Object[] mArgs;

    public Object[] getArgs() {
        return mArgs;
    }

    public Class[] getArgTypes() {
        return mArgTypes;
    }

    public SuperInvocation(Class[] argTypes,
                           Object[] args) {
        mArgTypes = argTypes;
        mArgs = args;
    }

    public static SuperInvocation empty() {
        return new SuperInvocation(
                new Class[] {},
                new Object[] {});
    }
}
