package com.tdrhq.eyepatch;

import com.tdrhq.eyepatch.dexmagic.StaticVerificationHandler;

public class EyePatchMockito {
    public static Class verifyStaticClass = null;

    public static void verifyStatic(Class klass) {
        StaticVerificationHandler handler = getStaticVerificationHandler(klass);
        handler.verifyStatic(klass);
    }

    public static void resetStatic(Class klass) {
        StaticVerificationHandler handler = getStaticVerificationHandler(klass);
        handler.resetStatic(klass);
    }

    private static StaticVerificationHandler getStaticVerificationHandler(Class klass) {
        ClassLoader classLoader = klass.getClassLoader();
        if (!(classLoader instanceof StaticVerificationHandler)) {
            throw new IllegalArgumentException(
                    String.format("%s (from classLoader %s) can't be verified",
                                  klass,
                                  classLoader));
        }

        return (StaticVerificationHandler) classLoader;
    }
}
