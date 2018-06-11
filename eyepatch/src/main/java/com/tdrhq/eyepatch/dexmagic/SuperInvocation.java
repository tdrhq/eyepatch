// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This provides all the information need by the generated class to
 * figure out what to call to super.
 */
public class SuperInvocation {
    private static Map<List<Class>, Integer> idMap = new HashMap<>();
    private Class[] mArgTypes;
    private Object[] mArgs;
    private int mConsId;

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

        synchronized (SuperInvocation.class) {
            List<Class> key = Arrays.asList(argTypes);
            Integer oldId = idMap.get(key);
            if (oldId == null) {
                oldId = idMap.size() + 1;
                idMap.put(key, oldId);
            }

            mConsId = oldId;
        }
    }

    public static SuperInvocation empty() {
        return new SuperInvocation(
                new Class[] {},
                new Object[] {});
    }

    public int getConsId() {
        return mConsId;
    }
}
