package com.tdrhq.eyepatch.runner;

import com.android.dx.Code;
import com.tdrhq.eyepatch.classloader.AndroidClassLoader;
import dalvik.system.PathClassLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(EyePatchTestRunner.class)
public class EyePatchTestRunnerTest {
    @Test
    public void testPreconditions() throws Throwable {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testVerifyClassLoader() throws Throwable {
        assertEquals(
                AndroidClassLoader.class.getName(),
                getClass().getClassLoader().getClass().getName());
    }

    public static class Foo {
    }

    @Test
    public void testVerifyClassLoaderForOtherClass() throws Throwable {
        assertEquals(
                AndroidClassLoader.class.getName(),
                Foo.class.getClassLoader().getClass().getName());
    }

    @Test
    public void testDexmakerHasInternalClass() throws Throwable {
        assertSame(
                PathClassLoader.class,
                Code.class.getClassLoader().getClass());
    }

    @Test
    public void testEyePatchInternals() throws Throwable {
        assertSame(
                PathClassLoader.class,
                EyePatchTestRunner.class.getClassLoader().getClass());

    }

    @Test
    public void testGetTestClassLoader() throws Throwable {
        assertNotNull(getTestClassLoader());
    }

    private AndroidClassLoader getTestClassLoader() {
        return (AndroidClassLoader) getClass().getClassLoader();
    }
}
