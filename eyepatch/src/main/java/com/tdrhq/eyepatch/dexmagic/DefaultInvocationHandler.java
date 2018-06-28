// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.List;

public class DefaultInvocationHandler implements StaticInvocationHandler {

    private List<? extends ClassHandler> prebuiltHandlers;

    DefaultInvocationHandler(
            List<? extends ClassHandler> prebuiltHandlers) {
        this.prebuiltHandlers = prebuiltHandlers;
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return getClassHandler(invocation.getInstanceClass())
                .handleInvocation(invocation);
    }

    ClassHandler getClassHandler(Class klass) {
        for (ClassHandler handler : prebuiltHandlers) {
            if (handler.getResponsibility() == klass) {
                return handler;
            }
        }

        throw new RuntimeException("No class handler for class: " + klass.getName());
    }

    public static DefaultInvocationHandler newInstance(List<? extends ClassHandler> handlers) {
        return new DefaultInvocationHandler(handlers);
    }
}
