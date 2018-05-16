// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch;

import android.util.Log;

public class StaticInvocationHandler {
    public StaticInvocationHandler() {
    }

    public static Object invokeStatic(Class klass, String method, Object[] args) {
        Log.i("StaticInvocationHandler", "Invoked: " + method);
        return "foo2";
    }
}
