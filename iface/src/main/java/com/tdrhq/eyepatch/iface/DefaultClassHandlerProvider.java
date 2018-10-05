package com.tdrhq.eyepatch.iface;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.iface.ClassHandlerProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultClassHandlerProvider implements ClassHandlerProvider {
    private Map<String, ClassHandler> handledClasses = new HashMap<>();
    private List<ClassHandler> classHandlers;

    public DefaultClassHandlerProvider(List<ClassHandler> classHandlers) {
        if (classHandlers == null) {
            throw new NullPointerException("null classHandlers");
        }
        this.classHandlers = classHandlers;

        for (ClassHandler classHandler : classHandlers) {
            handledClasses.put(classHandler.getResponsibility().getName(),
                    classHandler);
        }
    }

    @Override
    public ClassHandler getClassHandler(Class klass) {
       return getClassHandler(klass.getName());
    }

    @Override
    public boolean hasClassHandler(String name) {
        return handledClasses.containsKey(name);
    }

    @Override
    public ClassHandler getClassHandler(String name) {
        return handledClasses.get(name);
    }
}
