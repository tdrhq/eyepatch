// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

public class DefaultInvocationHandler extends StaticInvocationHandler {
    DefaultInvocationHandler() {
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return null;
    }

    ClassHandler getClassHandler(Class klass) {
        return new ClassHandler() {
            @Override
            public Object handleInvocation(Invocation invocation) {
                return null;
            }
        };
    }

    public static DefaultInvocationHandler newInstance() {
        return new DefaultInvocationHandler();
    }
}
