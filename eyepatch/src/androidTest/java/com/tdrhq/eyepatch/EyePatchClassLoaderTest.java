package com.tdrhq.eyepatch;

import android.util.Log;
import java.lang.reflect.Method;
import org.junit.*;
import org.junit.rules.*;
import static org.junit.Assert.*;

public class EyePatchClassLoaderTest {
    private EyePatchClassLoader mEyePatchClassLoader;

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        mEyePatchClassLoader = new EyePatchClassLoader(tmpdir.getRoot());
    }


    @After
    public void after() throws Throwable {
        for (String s : tmpdir.getRoot().list()) {
            Log.i("EyePatchClassLoaderTest", "After: " + s);
        }
    }

    @Test
    public void testWrapping() throws Exception {
        Class barWrapped = mEyePatchClassLoader.wrapClass(Bar.class);
        Method method = barWrapped.getMethod("foo");
        assertEquals("foo2", method.invoke(null));
    }

    public static class Bar {
        public static String foo() {
            return "foot";
        }
    }
}
