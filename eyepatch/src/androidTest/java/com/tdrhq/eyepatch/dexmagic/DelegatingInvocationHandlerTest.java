package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.PathClassLoader;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DelegatingInvocationHandlerTest {
    private DelegatingInvocationHandler mHandler;

    @Before
    public void before() throws Throwable {
        mHandler = new DelegatingInvocationHandler();
    }

    @Test
    public void testBadClassLoader() throws Throwable {
        try {
            mHandler.handleInvocation(
                    new Invocation(
                            Bar.class,
                            null,
                            "foo",
                            new Object[] {}));
            fail("expected exception");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testClassLoaderWithDelegation() throws Throwable {
        ClassLoader classLoader = new MyPathClassLoader();
        Class barWrapped = classLoader.loadClass(Bar.class.getName());
    }

    public static class MyPathClassLoader extends PathClassLoader {
        public MyPathClassLoader() {
            super(
                    ClassLoaderIntrospector.getOriginalDexPathAsStr(
                            MyPathClassLoader.class.getClassLoader()),
                    null,
                    MyPathClassLoader.class.getClassLoader().getParent());
        }

    }


    public static class Bar {
        public String foo() {
            return "car";
        }
    }
}
