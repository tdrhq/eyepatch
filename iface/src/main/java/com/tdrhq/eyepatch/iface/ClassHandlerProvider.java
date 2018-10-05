package com.tdrhq.eyepatch.iface;

import com.tdrhq.eyepatch.iface.ClassHandler;

public interface ClassHandlerProvider {
    /**
     * Find the ClassHandler for the given class. Since the class is already pre-created, it's assumed that the ClassHandler already exists.
     *
     * @param klass
     * @return
     */
    ClassHandler getClassHandler(Class klass);

    boolean hasClassHandler(String name);

    ClassHandler getClassHandler(String name);
}
