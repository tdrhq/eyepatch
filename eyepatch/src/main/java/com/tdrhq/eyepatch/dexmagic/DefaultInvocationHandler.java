// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.Checks;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultInvocationHandler extends StaticInvocationHandler {

    private ClassHandlerFactory classHandlerFactory;
    private Map<Class, ClassHandler> classHandlerMap = new HashMap<>();
    private List<ClassHandler> prebuiltHandlers = new ArrayList<>();

    DefaultInvocationHandler(
            ClassHandlerFactory classHandlerFactory,
            List<ClassHandler> prebuiltHandlers) {
        this.classHandlerFactory = classHandlerFactory;
        this.prebuiltHandlers.addAll(Checks.notNull(prebuiltHandlers));
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return getClassHandler(invocation.getInstanceClass())
                .handleInvocation(invocation);
    }

    ClassHandler getClassHandler(Class klass) {
        for (ClassHandler handler : prebuiltHandlers) {
            if (handler.canHandle(klass)) {
                return handler;
            }
        }

        if (classHandlerMap.containsKey(klass)) {
            return classHandlerMap.get(klass);
        }

        ClassHandler ret = classHandlerFactory.create(klass);

        classHandlerMap.put(klass, ret);
        return ret;
    }

    public static DefaultInvocationHandler newInstance(List<ClassHandler> handlers) {
        return new DefaultInvocationHandler(new MockitoClassHandlerFactory(), handlers);
    }
}
