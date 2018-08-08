// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.iface.Invocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInvocationHandler implements StaticInvocationHandler {

    private List<? extends ClassHandler> prebuiltHandlers;
    private final Map<Class, ClassHandler> classHandlerMap = new HashMap<>();

    DefaultInvocationHandler(
            List<? extends ClassHandler> prebuiltHandlers) {
        this.prebuiltHandlers = prebuiltHandlers;

        for (ClassHandler classHandler : prebuiltHandlers) {
            classHandlerMap.put(
                    classHandler.getResponsibility(),
                    classHandler);
        }
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return getClassHandler(invocation.getInstanceClass())
                .handleInvocation(invocation);
    }

    final ClassHandler getClassHandler(Class klass) {
        ClassHandler ret = classHandlerMap.get(klass);

        if (ret == null) {
            throw new RuntimeException("No class handler for class: " + klass.getName());
        }

        return ret;
    }

    public static DefaultInvocationHandler newInstance(List<? extends ClassHandler> handlers) {
        return new DefaultInvocationHandler(handlers);
    }
}
