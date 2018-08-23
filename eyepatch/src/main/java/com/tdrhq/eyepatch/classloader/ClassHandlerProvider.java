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

}
