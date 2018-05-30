package com.tdrhq.eyepatch.classloader;

import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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

        assertSame(classLoader, klass.getClassLoader());
    }

    @Test
    public void testgetOriginalDexPath() throws Throwable {
        assertThat(
                classLoader.getOriginalDexPath(),
                hasSize(greaterThan(1)));
    }

    public static class Foo {
    }
}
