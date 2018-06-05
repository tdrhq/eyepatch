package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  FieldsTests.Foo.class,
})
@RunWith(EyePatchTestRunner.class)
public class FieldsTests {

    public static ClassHandler createClassHandler(final Class klass) {
        return new ClassHandler() {
            @Override
            public Class getResponsibility() {
                return klass;
            }

            @Override
            public Object handleInvocation(Invocation invocation) {
                if (invocation.getMethod().equals("getNumber")) {
                    return 20;
                }
                if (invocation.getMethod().equals("getOtherNumber")) {
                    return 40;
                }
                return null;
            }
        };

    }

    @Test
    public void testPreconditions() throws Throwable {
        assertEquals(0, new Foo().number);
        assertEquals(40, Foo.getOtherNumber());
        assertEquals(20, new Foo().getNumber());
    }

    public static class Foo {
        int number = 0;

        public int getNumber() {
            return 0;
        }

        public static int getOtherNumber() {
            return 10;
        }
    }
}
