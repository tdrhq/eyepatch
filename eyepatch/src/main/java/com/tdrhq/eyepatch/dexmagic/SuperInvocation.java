// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This provides all the information need by the generated class to
 * figure out what to call to super.
 */
public class SuperInvocation {
    private static Map<List<String>, Integer> idMap = new HashMap<>();
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
        for (int i = 0; i < argTypes.length; i ++) {
            Class klass = argTypes[i];
            if (klass == null) {
                throw new NullPointerException("null types not allows, got: " +
                                               argTypes.length + " args: " +
                                               Arrays.toString(argTypes));
            }
        }

        mArgTypes = Arrays.copyOf(argTypes, argTypes.length);
        mArgs = Arrays.copyOf(args, args.length);
        mConsId = getConstructorId(argTypes);
    }

    public static SuperInvocation empty() {
        return new SuperInvocation(
                new Class[] {},
                new Object[] {});
    }

    public int getConsId() {
        return mConsId;
    }

    public synchronized static int getConstructorId(Class[] types) {
        List<String> key = new ArrayList<String>();
        for (Class klass : types) {
            key.add(klass.getName());
        }
        Integer oldId = idMap.get(key);
        if (oldId == null) {
            oldId = idMap.size() + 1;
            idMap.put(key, oldId);
            Log.i("SuperInvocation", String.format("%s is getting: %d", key, oldId));
        }

        return oldId;
    }

    public static int getConstructorId(SuperInvocation superInvocation) {
        if (superInvocation == null) {
            return getConstructorId(new Class[] {});
        }

        return superInvocation.getConsId();
    }
}
