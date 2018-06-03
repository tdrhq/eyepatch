// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.tdrhq.eyepatch.util.Checks;

public abstract class StaticInvocationHandler {

    private static StaticInvocationHandler DEFAULT_HANDLER = new DelegatingInvocationHandler();

    private static StaticInvocationHandler sHandler = DEFAULT_HANDLER;

    public static void setDefaultHandler() {
        sHandler = DEFAULT_HANDLER;
    }

    public static void setHandler(StaticInvocationHandler handler) {
        sHandler = Checks.notNull(handler);
    }

    public StaticInvocationHandler() {
    }

    public abstract Object handleInvocation(Invocation invocation);

    public static Object invokeStatic(
            Class klass,
            Object instance,
            String method,
            Class[] argTypes,
            Object[] args) {
        Invocation invocation = new Invocation(klass, instance, method, argTypes, args);
        if (sHandler != null) {
            return sHandler.handleInvocation(invocation);
        }

        return null;
    }
}
