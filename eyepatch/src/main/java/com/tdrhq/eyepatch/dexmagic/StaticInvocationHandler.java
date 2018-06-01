// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.tdrhq.eyepatch.util.Checks;

public abstract class StaticInvocationHandler {

    private static StaticInvocationHandler DEFAULT_HANDLER = new StaticInvocationHandler() {
            @Override
            public Object handleInvocation(Class klass, Object instance, String method, Object[] args) {
                return "foo2";
            }
        };

    private static StaticInvocationHandler sHandler = DEFAULT_HANDLER;

    public static void setDefaultHandler() {
        sHandler = DEFAULT_HANDLER;
    }

    public static void setHandler(StaticInvocationHandler handler) {
        sHandler = Checks.notNull(handler);
    }

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
