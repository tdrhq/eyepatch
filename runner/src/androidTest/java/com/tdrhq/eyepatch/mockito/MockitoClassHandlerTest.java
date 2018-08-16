package com.tdrhq.eyepatch.mockito;

import com.tdrhq.eyepatch.util.EyePatchTemporaryFolder;
import com.tdrhq.eyepatch.iface.Invocation;
import com.tdrhq.eyepatch.iface.GeneratedMethod;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.*;
import static org.junit.Assert.*;

public class MockitoClassHandlerTest {
    private MockitoClassHandler mMockitoClassHandler;

    @Rule
    public EyePatchTemporaryFolder tmpdir = new EyePatchTemporaryFolder();

    @Before
    public void before() throws Throwable {
        mMockitoClassHandler = new MockitoClassHandler(
                Foo.class,
                new CompanionBuilder(tmpdir.getRoot()));
    }

    @Test
    public void testVerify() throws Throwable {
        testVerifyOn(Foo.class);
    }

    private void testVerifyOn(Class fooClass) {
        Invocation invocation = new Invocation(
                new GeneratedMethod(fooClass, "bar", new Class[] { String.class }),
                null,
                new Object[] { "car" });


        mMockitoClassHandler.handleInvocation(invocation);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        mMockitoClassHandler.verifyStatic();

        invocation = new Invocation(
                new GeneratedMethod(fooClass, "bar", new Class[] { String.class }),
                null,
                new Object[] { captor.capture() });

        mMockitoClassHandler.handleInvocation(invocation);

        assertEquals("car", captor.getValue());
        mMockitoClassHandler.resetStatic();
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
