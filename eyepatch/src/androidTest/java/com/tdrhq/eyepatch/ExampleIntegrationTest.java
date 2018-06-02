package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchMockito;
import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@EyePatchMockable( { ExampleIntegrationTest.Foo.class } )
@RunWith(EyePatchTestRunner.class)
public class ExampleIntegrationTest {
    @Test
    public void testDifferentArgs() throws Throwable {
        when(Foo.bar("zoid")).thenReturn("berg");
        when(Foo.bar("car")).thenReturn("toyota");

        assertEquals("toyota", Foo.bar("car"));
        assertEquals("berg", Foo.bar("zoid"));
        assertEquals("toyota", Foo.bar("car"));
    }

    @Test
    public void testMatchers() throws Throwable {
        when(Foo.bar(eq("zoid"))).thenReturn("berg");
        when(Foo.bar("car")).thenReturn("toyota");

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

        assertEquals("blah", captor.getValue());
    }


    public static class Foo {
        public static String bar(String arg) {
            return "notseenever";
        }

        public static void voidMethod(String arg) {
        }
    }
}
