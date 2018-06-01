// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import android.util.Log;
import com.tdrhq.eyepatch.util.Checks;

public abstract class StaticInvocationHandler {

    private static StaticInvocationHandler DEFAULT_HANDLER = DefaultInvocationHandler.newInstance();

    private static StaticInvocationHandler sHandler = DEFAULT_HANDLER;

    public static void setDefaultHandler() {
        sHandler = DEFAULT_HANDLER;
    }

    public static void setHandler(StaticInvocationHandler handler) {
        sHandler = Checks.notNull(handler);
    }

    public static void prepareClass(Class klass) {
        sHandler.prepare(klass);
    }

    public StaticInvocationHandler() {
    }

    public abstract void prepare(Class klass);
    public abstract Object handleInvocation(Invocation invocation);

    public static Object invokeStatic(Class klass, Object instance, String method, Object[] args) {
        Invocation invocation = new Invocation(klass, instance, method, args);
        if (sHandler != null) {
            return sHandler.handleInvocation(invocation);
        }

        return null;
    }
}
