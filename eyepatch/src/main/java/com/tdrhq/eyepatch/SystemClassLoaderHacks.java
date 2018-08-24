package com.tdrhq.eyepatch;

import android.content.Context;

import com.tdrhq.eyepatch.util.Whitebox;

public class SystemClassLoaderHacks {
    public static void registerSystemClassLoader(ClassLoader classLoader) {
        setSystemClassLoader(classLoader);
    }

    public static void validateClassLoaderCaches(Context context) {
            ClassLoader expected = (ClassLoader) Whitebox.getStaticField(
                    SYSTEM_CLASS_LOADER(),
                    "loader"
            );


    }

    static void setSystemClassLoader(ClassLoader classLoader) {

            Class klass = SYSTEM_CLASS_LOADER();
            Whitebox.setStaticField(klass, "loader", classLoader);

    }

    private static Class<?> SYSTEM_CLASS_LOADER() {
        try {
            return Class.forName("java.lang.ClassLoader$SystemClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
