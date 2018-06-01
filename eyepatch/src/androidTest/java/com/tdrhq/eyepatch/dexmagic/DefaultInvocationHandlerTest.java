package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DefaultInvocationHandlerTest {
    private DefaultInvocationHandler handler;

    @Before
    public void before() throws Throwable {
        handler = new DefaultInvocationHandler();
    }

    @Test
    public void testHandleInvocationHappyPath() throws Throwable {
        Invocation invocation = new Invocation(
                Foo.class,
                new Foo(),
                "bar",
                new Object[] {});

        assertEquals(null, handler.handleInvocation(invocation));
    }

    @Test
    public void testClassHandler() throws Throwable {
        assertNotNull(handler.getClassHandler(Foo.class));
    }

    public static class Foo {
        public String bar() {
            return "zoidberg";
        }
    }

    public static class Bar {
    }
}
