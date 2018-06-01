package com.tdrhq.eyepatch;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;

public class EyePatch {
    public static Class verifyStaticClass = null;

    public static void verifyStatic(Class klass) {
        if (verifyStaticClass == null) {
            throw new IllegalStateException("You called verifyStatic without doing anything with it");
        }
        verifyStaticClass = klass;
    }

    public static Class __peekVerifyStatic() {
        return verifyStaticClass;
    }

    public static void resetVerifyStatic() {
        verifyStaticClass = null;
    }

    private static EyePatchClassLoader getClassLoader() {
        return (EyePatchClassLoader) EyePatch.class.getClassLoader();
    }
}
