package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.dexmagic.ClassHandler;
import com.tdrhq.eyepatch.dexmagic.Invocation;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable( { CustomClassHolderTest.Foo.class })
@RunWith(EyePatchTestRunner.class)
public class CustomClassHolderTest {

    public static ClassHandler createClassHandler(final Class klass) {
        return new ClassHandler() {
            @Override
            public Class getResponsibility() {
                return klass;
            }

            @Override
            public Object handleInvocation(Invocation invocation) {
                if (invocation.getMethod().equals("foo")) {
                    return "bar";
                }

                return null;
            }
        };
    }

    @Test
    public void testThatThisWorks() throws Throwable {
        assertEquals("bar", Foo.foo());
    }

    public static class Foo {
        public static String foo() {
            return "notseenever";
        }
    }

}
