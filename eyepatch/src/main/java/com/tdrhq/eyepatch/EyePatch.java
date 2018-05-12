package com.tdrhq.eyepatch;

import java.lang.reflect.Method;

public class EyePatch {
    public static void patch(Class klass, Method method, Callback callback) {

    }

    public interface Callback {
        public Object run(Object instance, Object[] arguments);
    }
}
