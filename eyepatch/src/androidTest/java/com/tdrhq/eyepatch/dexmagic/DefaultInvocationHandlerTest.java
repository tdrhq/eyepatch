package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.classloader.ClassHandlerProvider;
import com.tdrhq.eyepatch.util.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.iface.Invocation;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.*;
import com.tdrhq.eyepatch.iface.GeneratedMethod;

public class DefaultInvocationHandlerTest {
    private DefaultInvocationHandler handler;

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Before
    public void before() throws Throwable {
        List<ClassHandler> handlers = new ArrayList<>();
        handlers.add(new SimpleClassHandler(Foo.class));
        handlers.add(new SimpleClassHandler(Bar.class));

        handler = new DefaultInvocationHandler(new ClassHandlerProvider(handlers));
    }

    static class SimpleClassHandler implements ClassHandler {
        Class klass;

        public SimpleClassHandler(Class klass) {
            this.klass = klass;
        }

        @Override
        public Object handleInvocation(Invocation invocation) {
            return null;
        }

        @Override
        public Class getResponsibility() {
            return klass;
        }
    }

    @Test
    public void testHandleInvocationHappyPath() throws Throwable {
        Invocation invocation = new Invocation(
                new GeneratedMethod(
                        Foo.class,
                        "bar",
                        new Class[] {}),
                new Foo(),
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
