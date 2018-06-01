package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultInvocationHandlerTest {
    private DefaultInvocationHandler handler;
    private ClassHandlerFactory classHandlerFactory;

    @Before
    public void before() throws Throwable {
        classHandlerFactory = new ClassHandlerFactory() {
                @Override
                public ClassHandler create(Class klass) {
                    return mock(ClassHandler.class);
                }
            };
        handler = new DefaultInvocationHandler(classHandlerFactory);
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


    @Test
    public void testMultipleClassesHandlers() throws Throwable {
        ClassHandler handler1 = handler.getClassHandler(Foo.class);
        ClassHandler handler2 = handler.getClassHandler(Bar.class);

        assertNotSame(handler1, handler2);

        assertSame(handler1, handler.getClassHandler(Foo.class));

    }
    public static class Foo {
        public String bar() {
            return "zoidberg";
        }
    }

    public static class Bar {
    }
}
