package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.EyePatchMockable;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import com.tdrhq.eyepatch.util.Whitebox;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@EyePatchMockable({
  FieldsTests.Foo.class,
  FieldsTests.MultipleFields.class,
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

    @Test
    public void testMultipleFields() throws Throwable {
        // verify this is truly mocked:
        assertEquals(20, MultipleFields.getNumber());

        MultipleFields instance = new MultipleFields();
        instance.f1 = 20;
        instance.f2 = "blah";
        instance.f3 = 30;

        assertEquals(20, instance.f1);
        assertEquals("blah", instance.f2);
        assertEquals(30, instance.f3);

        // Also check with reflection just to make sure it's in sync
        assertEquals(20, Whitebox.getField(instance, MultipleFields.class, "f1"));
        assertEquals("blah", Whitebox.getField(instance, MultipleFields.class, "f2"));
        assertEquals(30, Whitebox.getField(instance, MultipleFields.class, "f3"));
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

    public static class MultipleFields {
        public int f1 = 0;
        public String f2 = null;
        public int f3 = 0;

        public static int getNumber() {
            return 0;
        }
    }
}
