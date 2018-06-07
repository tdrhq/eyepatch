package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchTemporaryFolder;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DefaultInvocationHandlerTest {
    private DefaultInvocationHandler handler;

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Before
    public void before() throws Throwable {
        CompanionBuilder companionBuilder = new CompanionBuilder(
                tmpdir.getRoot());

        List<ClassHandler> handlers = new ArrayList<>();
        handlers.add(new MockitoClassHandler(Foo.class, companionBuilder));
        handlers.add(new MockitoClassHandler(Bar.class, companionBuilder));
        handler = new DefaultInvocationHandler(handlers);
    }

    @Test
    public void testHandleInvocationHappyPath() throws Throwable {
        Invocation invocation = new Invocation(
                Foo.class,
                new Foo(),
                "bar",
                new Class[] {},
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
        public static String bar() {
            return "zoidberg";
        }
    }

    public static class Bar {
    }
}
