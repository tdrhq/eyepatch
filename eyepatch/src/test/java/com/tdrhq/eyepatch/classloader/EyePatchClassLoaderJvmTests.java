// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EyePatchClassLoaderJvmTests {
    private EyePatchClassLoader classLoader;

    @Before
    public void before() throws Throwable {
        classLoader = new EyePatchClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testPreconditions() throws Throwable {
        new EyePatchClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testSimpleCreation() throws Throwable {
        Class<?> klass = classLoader.loadClass(Foo.class.getName());

        assertNotSame(klass, Foo.class);
        assertSame(classLoader, klass.getClassLoader());
    }

    public static class Foo {
    }
}
