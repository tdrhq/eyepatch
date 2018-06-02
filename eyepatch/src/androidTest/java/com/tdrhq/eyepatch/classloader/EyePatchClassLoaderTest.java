package com.tdrhq.eyepatch.classloader;

import android.os.Bundle;
import android.view.View;
import com.tdrhq.eyepatch.dexmagic.CompanionBuilder;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.util.Whitebox;
import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class EyePatchClassLoaderTest {
    private EyePatchClassLoader classLoader;
    private EyePatchClassBuilder classBuilder;
    private CompanionBuilder companionBuilder;

    @Before
    public void before() throws Throwable {
        classBuilder = mock(EyePatchClassBuilder.class);
        companionBuilder = mock(CompanionBuilder.class);

        classLoader = new EyePatchClassLoader(
                getClass().getClassLoader(),
                classBuilder,
                companionBuilder);
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

    public static class Foo {
    }

    public static class OtherClass {
        public void doStuff() {
            assertEquals(
                    EyePatchClassLoader.class.getName(),
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
