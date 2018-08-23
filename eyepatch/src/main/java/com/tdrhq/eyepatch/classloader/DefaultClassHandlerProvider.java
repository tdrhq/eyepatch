package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.util.Checks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultClassHandlerProvider {
    private Map<String, ClassHandler> mockedClasses = new HashMap<>();
    private List<ClassHandler> classHandlers;

    public DefaultClassHandlerProvider(List<ClassHandler> classHandlers) {
        this.classHandlers = Checks.notNull(classHandlers);

        for (ClassHandler classHandler : classHandlers) {
            mockedClasses.put(classHandler.getResponsibility().getName(),
                    classHandler);
        }
    }

    /**
     * Find the ClassHandler for the given class. Since the class is already pre-created, it's assumed that the ClassHandler already exists.
     *
     * @param klass
     * @return
     */
    public ClassHandler getClassHandler(Class klass) {
       return getClassHandler(klass.getName());
    }

    public boolean hasClassHandler(String name) {
        return mockedClasses.containsKey(name);
    }

    public ClassHandler getClassHandler(String name) {
        return mockedClasses.get(name);
    }
}
