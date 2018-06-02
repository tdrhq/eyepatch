package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import java.util.ArrayList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@EyePatchMockable( { MockitoClassHandlerFactoryTest.Foo.class } )
@RunWith(EyePatchTestRunner.class)
public class MockitoClassHandlerFactoryTest {
    @Before
    public void before() throws Throwable {
        StaticInvocationHandler.setHandler(new DefaultInvocationHandler(new MockitoClassHandlerFactory(), new ArrayList<ClassHandler>()));
    }

    @After
    public void after() throws Throwable {
        StaticInvocationHandler.setDefaultHandler();
    }

    @Test
    public void testPreconditions() throws Throwable {
        when(Foo.foo()).thenReturn("blah");
        assertEquals("blah", Foo.foo());
    }

    @Test
    public void testDifferentArgs() throws Throwable {
        when(Foo.bar("zoid")).thenReturn("berg");
        when(Foo.bar("car")).thenReturn("toyota");

        assertEquals("toyota", Foo.bar("car"));
        assertEquals("berg", Foo.bar("zoid"));
        assertEquals("toyota", Foo.bar("car"));
    }

    @Test
    public void testMathers() throws Throwable {
        when(Foo.bar(eq("zoid"))).thenReturn("berg");
        when(Foo.bar("car")).thenReturn("toyota");

        assertEquals("toyota", Foo.bar("car"));
        assertEquals("berg", Foo.bar("zoid"));
        assertEquals("toyota", Foo.bar("car"));
    }


    public static class Foo {
        public static String foo() {
            return "notseenever";
        }

        public static String bar(String arg) {
            return "notseenever";
        }
    }
}
