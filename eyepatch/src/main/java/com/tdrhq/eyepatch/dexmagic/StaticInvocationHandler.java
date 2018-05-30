// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;

public abstract class StaticInvocationHandler {

    public static StaticInvocationHandler sHandler = new StaticInvocationHandler() {
            @Override
            public Object handleInvocation(Class klass, Object instance, String method, Object[] args) {
                return "foo2";
            }
        };

    public StaticInvocationHandler() {
    }

    public abstract Object handleInvocation(Class klass, Object instance,  String method, Object[] args);

    public static Object invokeStatic(Class klass, Object instance, String method, Object[] args) {
        Log.i("StaticInvocationHandler", "Invoked: " + method);
        if (sHandler != null) {
            return sHandler.handleInvocation(klass, instance, method, args);
        }

        return null;
    }
}
