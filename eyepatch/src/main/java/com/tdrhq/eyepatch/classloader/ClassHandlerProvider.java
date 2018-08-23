package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.util.Checks;

import java.util.List;

public class ClassHandlerProvider {
    public List<ClassHandler> getClassHandlers() {
        return classHandlers;
    }

    List<ClassHandler> classHandlers;
    public ClassHandlerProvider(List<ClassHandler> classHandlers) {
        this.classHandlers = Checks.notNull(classHandlers);
    }

    /**
     * Find the ClassHandler for the given class. Since the class is already pre-created, it's assumed that the ClassHandler already exists.
     *
     * @param klass
     * @return
     */
    public ClassHandler getClassHandler(Class klass) {
        for (ClassHandler classHandler : classHandlers) {
            if (classHandler.getResponsibility() == klass) {
                return classHandler;
            }
        }
        throw new IllegalArgumentException("could not find handler for class: " + klass);
    }
}
