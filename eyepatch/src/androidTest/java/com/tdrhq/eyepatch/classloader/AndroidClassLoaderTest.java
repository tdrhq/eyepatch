package com.tdrhq.eyepatch.classloader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AndroidClassLoaderTest {
    private AndroidClassLoader classLoader;

    @Before
    public void before() throws Throwable {
        classLoader = new AndroidClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testVerifyDefaultClassLoader() throws Throwable {
        assertEquals("dalvik.system.PathClassLoader", getClass().getClassLoader().getClass().getName());
    }

    @Test
    public void testSimpleCreation() throws Throwable {
        Class<?> klass = classLoader.loadClass(Foo.class.getName());
    }

    public static class Foo {
    }
}
