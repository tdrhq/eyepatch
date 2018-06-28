package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.ClassLoaderIntrospector;
import dalvik.system.PathClassLoader;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DelegatingInvocationHandlerTest {
    private DelegatingInvocationHandler mHandler;
    private StaticInvocationHandlerInterface staticHandler;

    @Before
    public void before() throws Throwable {
        mHandler = new DelegatingInvocationHandler();
        staticHandler = mock(StaticInvocationHandlerInterface.class);
    }

    @Test
    public void testBadClassLoader() throws Throwable {
        try {
            mHandler.handleInvocation(
                    new Invocation(
                            Bar.class,
                            null,
                            "foo",
                            new Class[] {},
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
        Invocation invocation = new Invocation(
                barWrapped,
                null,
                "foo",
                new Class[] {},
                new Object[] {});
        mHandler.handleInvocation(
                invocation);

        verify(staticHandler).handleInvocation(invocation);
    }

    public class MyPathClassLoader extends PathClassLoader implements HasStaticInvocationHandler {
        public MyPathClassLoader() {
            super(
                    ClassLoaderIntrospector.getOriginalDexPathAsStr(
                            MyPathClassLoader.class.getClassLoader()),
                    null,
                    MyPathClassLoader.class.getClassLoader().getParent());
        }

        @Override
        public StaticInvocationHandlerInterface getStaticInvocationHandler() {
            return staticHandler;
        }
    }


    public static class Bar {
        public String foo() {
            return "car";
        }
    }
}
