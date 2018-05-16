// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch;

import android.util.Log;

public abstract class StaticInvocationHandler {

    public static StaticInvocationHandler sHandler = new StaticInvocationHandler() {
            @Override
            public Object handleInvocation(Class klass, String method, Object[] args) {
                return "foo2";
            }
        };

    public StaticInvocationHandler() {
    }

    public abstract Object handleInvocation(Class klass, String method, Object[] args);

    public static Object invokeStatic(Class klass, String method, Object[] args) {
        Log.i("StaticInvocationHandler", "Invoked: " + method);
        if (sHandler != null) {
            return sHandler.handleInvocation(klass, method, args);
        }

        return null;
    }
}
