package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
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
        StaticInvocationHandler.setHandler(new DefaultInvocationHandler(new MockitoClassHandlerFactory()));
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

    public static class Foo {
        public static String foo() {
            return "notseenever";
        }
    }
}
