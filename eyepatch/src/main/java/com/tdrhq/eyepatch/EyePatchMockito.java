package com.tdrhq.eyepatch;

import com.tdrhq.eyepatch.dexmagic.StaticVerificationHandler;

public class EyePatchMockito {
    public static Class verifyStaticClass = null;

    public static void verifyStatic(Class klass) {
        ClassLoader classLoader = klass.getClassLoader();
        if (!(classLoader instanceof StaticVerificationHandler)) {
            throw new IllegalArgumentException(
                    String.format("%s (from classLoader %s) can't be verified",
                                  klass,
                                  classLoader));
        }

        StaticVerificationHandler handler = (StaticVerificationHandler) classLoader;
        handler.verifyStatic(klass);
    }
}
