package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.*;
import static org.junit.Assert.*;

public class MockitoClassHandlerTest {
    private MockitoClassHandler mMockitoClassHandler;

    @Rule
    public TemporaryFolder tmpdir = new TemporaryFolder();

    @Before
    public void before() throws Throwable {
        mMockitoClassHandler = new MockitoClassHandler(
                Foo.class,
                new CompanionBuilder(tmpdir.getRoot()));
    }

    @Test
    public void testVerify() throws Throwable {
        Invocation invocation = new Invocation(
                Foo.class,
                null,
                "bar",
                new Class[] { String.class },
                new Object[] { "car" });


        mMockitoClassHandler.handleInvocation(invocation);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        mMockitoClassHandler.verifyStatic();

        invocation = new Invocation(
                Foo.class,
                null,
                "bar",
                new Class[] { String.class },
                new Object[] { captor.capture() });

        mMockitoClassHandler.handleInvocation(invocation);

        assertEquals("car", captor.getValue());
    }

    @Test
    public void testVerify2() throws Throwable {
        testVerify(); // just try this again to be sure
    }

    public static class Foo {
        public static String bar(String arg) {
            return "notseenever";
        }

        public static void voidMethod(String arg) {
        }
    }

}
