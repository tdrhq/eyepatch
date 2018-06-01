package com.tdrhq.eyepatch;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.MockitoClassHandler;

public class EyePatchMockito {
    public static Class verifyStaticClass = null;

    public static void verifyStatic(Class klass) {
        if (MockitoClassHandler.verifyStaticClass != null) {
            throw new IllegalStateException("You called verifyStatic without doing anything with it");
        }
        //        MockitoClassHandler.verifyStaticClass = klass;
    }
}
