package com.tdrhq.eyepatch;

import android.util.Log;
import java.lang.reflect.Method;
import org.junit.*;
import org.junit.rules.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EyePatchClassLoaderTest {
    private EyePatchClassLoader mEyePatchClassLoader;
    private StaticInvocationHandler oldHandler;

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        mEyePatchClassLoader = new EyePatchClassLoader(tmpdir.getRoot());
        oldHandler = StaticInvocationHandler.sHandler;
    }


    @After
    public void after() throws Throwable {
        for (String s : tmpdir.getRoot().list()) {
            Log.i("EyePatchClassLoaderTest", "After: " + s);
        }
        StaticInvocationHandler.sHandler = oldHandler;
    }

    @Test
    public void testWrapping() throws Exception {
        Class barWrapped = mEyePatchClassLoader.wrapClass(Bar.class);
        Method method = barWrapped.getMethod("foo");
        assertEquals("foo2", method.invoke(null));
    }

    @Test
    public void testHandlerArgs() throws Exception {
        StaticInvocationHandler handler = mock(StaticInvocationHandler.class);
        StaticInvocationHandler.sHandler = handler;

        when(handler.handleInvocation(any(Class.class),
                                      eq("foo"),
                                      (Object[]) eq(null)))
                .thenReturn("foo3");

        when(handler.handleInvocation(any(Class.class),
                                      eq("car"),
                                      (Object[]) eq(null)))
                .thenReturn("car3");

        Class barWrapped = mEyePatchClassLoader.wrapClass(Bar.class);
        Method method = barWrapped.getMethod("foo");
        assertEquals("foo3", method.invoke(null));

        method = barWrapped.getMethod("car");
        assertEquals("car3", method.invoke(null));
    }


    public static class Bar {
        public static String foo() {
            return "foot";
        }

        public static String car() {
            return "foot";
        }
    }
}
