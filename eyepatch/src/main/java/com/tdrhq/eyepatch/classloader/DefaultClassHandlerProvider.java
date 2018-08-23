package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.util.Checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultClassHandlerProvider implements ClassHandlerProvider {
    private Map<String, ClassHandler> mockedClasses = new HashMap<>();
    private List<ClassHandler> classHandlers;

    public DefaultClassHandlerProvider(List<ClassHandler> classHandlers) {
        this.classHandlers = Checks.notNull(classHandlers);

        for (ClassHandler classHandler : classHandlers) {
            mockedClasses.put(classHandler.getResponsibility().getName(),
                    classHandler);
        }
    }

    @Override
    public ClassHandler getClassHandler(Class klass) {
       return getClassHandler(klass.getName());
    }

    @Override
    public boolean hasClassHandler(String name) {
        return mockedClasses.containsKey(name);
    }

    @Override
    public ClassHandler getClassHandler(String name) {
        return mockedClasses.get(name);
    }
}
