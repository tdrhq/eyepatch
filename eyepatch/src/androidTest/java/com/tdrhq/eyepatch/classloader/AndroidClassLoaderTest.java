package com.tdrhq.eyepatch.classloader;

import android.os.Bundle;
import android.view.View;
import com.tdrhq.eyepatch.util.Whitebox;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
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

        assertSame(classLoader, klass.getClassLoader());
    }

    @Test
    public void testNestedCreation() throws Throwable {
        Class<?> klass = classLoader.loadClass(OtherClass.class.getName());

        Whitebox.invoke(klass.newInstance(), "doStuff");
    }

    @Test
    public void testgetOriginalDexPath() throws Throwable {
        assertThat(
                classLoader.getOriginalDexPath(),
                hasSize(greaterThan(1)));
    }

    public static class Foo {
    }

    public static class OtherClass {
        public void doStuff() {
            assertEquals(
                    AndroidClassLoader.class.getName(),
                    Foo.class.getClassLoader().getClass().getName());

            assertEquals(
                    "java.lang.BootClassLoader",
                    Bundle.class.getClassLoader().getClass().getName());

            assertEquals(
                    "java.lang.BootClassLoader",
                    View.class.getClassLoader().getClass().getName());
        }
    }
}
