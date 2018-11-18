// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.classloader;

import com.tdrhq.eyepatch.util.Whitebox;
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
        assertEquals(5, Whitebox.invokeStatic(klass, "addTwo", Whitebox.arg(int.class, 3)));
    }

    public static class Foo {
        public static int addTwo(int val) {
            return val + 2;
        }
    }
}
