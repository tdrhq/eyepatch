package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.EyePatchMockito;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;


@EyePatchMockable( { ExampleIntegrationTest.Foo.class } )
@RunWith(EyePatchTestRunner.class)
public class ExampleIntegrationTest {

    @After
    public void after() throws Throwable {
        EyePatchMockito.resetStatic(Foo.class);
    }

    @Test
    public void testDifferentArgs() throws Throwable {
        Mockito.when(Foo.bar("zoid")).thenReturn("berg");
        Mockito.when(Foo.bar("car")).thenReturn("toyota");

        assertEquals("toyota", Foo.bar("car"));
        assertEquals("berg", Foo.bar("zoid"));
        assertEquals("toyota", Foo.bar("car"));
    }

    @Test
    public void testMatchers() throws Throwable {
        Mockito.when(Foo.bar(ArgumentMatchers.eq("zoid"))).thenReturn("berg");
        Mockito.when(Foo.bar("car")).thenReturn("toyota");

        assertEquals("toyota", Foo.bar("car"));
        assertEquals("berg", Foo.bar("zoid"));
        assertEquals("toyota", Foo.bar("car"));
    }

    @Test
    public void testCaptureOnVoidMethod() throws Throwable {
        Foo.voidMethod("blah");
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        EyePatchMockito.verifyStatic(Foo.class);
        Foo.voidMethod(captor.capture());

        Assert.assertEquals("blah", captor.getValue());
    }


    public static class Foo {
        public static String bar(String arg) {
            return "notseenever";
        }

        public static void voidMethod(String arg) {
        }
    }
}
