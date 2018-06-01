// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.HashMap;
import java.util.Map;

public class DefaultInvocationHandler extends StaticInvocationHandler {

    private Map<Class, ClassHandler> classHandlerMap = new HashMap<>();

    DefaultInvocationHandler() {
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return null;
    }

    ClassHandler getClassHandler(Class klass) {

        if (classHandlerMap.containsKey(klass)) {
            return classHandlerMap.get(klass);
        }

        ClassHandler ret = new ClassHandler() {
            @Override
            public Object handleInvocation(Invocation invocation) {
                return null;
            }
        };

        classHandlerMap.put(klass, ret);
        return ret;
    }

    public static DefaultInvocationHandler newInstance() {
        return new DefaultInvocationHandler();
    }
}
