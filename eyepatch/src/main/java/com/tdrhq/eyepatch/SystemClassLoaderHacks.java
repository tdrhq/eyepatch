package com.tdrhq.eyepatch;

import com.tdrhq.eyepatch.util.Whitebox;

public class SystemClassLoaderHacks {
    public static void registerSystemClassLoader(ClassLoader classLoader) {
        setSystemClassLoader(classLoader);
    }

    private static void setSystemClassLoader(ClassLoader classLoader) {

        try {
            Class klass = Class.forName("java.lang.ClassLoader$SystemClassLoader");
            Whitebox.setStaticField(klass, "loader", classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
