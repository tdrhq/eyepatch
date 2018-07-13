package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.runner.ClassHandlerProvider;
import com.tdrhq.eyepatch.runner.EyePatchTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

@RunWith(EyePatchTestRunner.class)
public class ShadowClassHandlerTest {

    @ClassHandlerProvider(Foo.class)
    public static ClassHandler createFooClass(final Class klass) {
        return ShadowClassHandler.newShadowClassHandler(klass, FooShadow.class);
    }

    @ClassHandlerProvider(Partial.class)
    public static ClassHandler createPartialClass(final Class klass) {
        return ShadowClassHandler.newPartialClassHandler(klass, PartialShadow.class);
    }

    @Test
    public void testShadowing() throws Throwable {
        Foo foo = new Foo(20);
        assertEquals(20, foo.number());
    }

    @Test
    public void testPartialShadowing() throws Throwable {
        assertEquals(6, Partial.twiceNumber());
    }

    public static class Foo {
        public Foo(int arg) {
        }

        public int number() {
            return -1;
        }
    }

    public static class FooShadow {
        int arg;

        // __construct__ is the shadow function called when the
        // constructor is invoked.
        public void __construct__(int arg) {
            this.arg = arg;
        }

        public int number() {
            return this.arg;
        }
    }

    public static class Partial {
        public static int number() {
            return 2;
        }

        public static int twiceNumber() {
            return 2 * number();
        }
    }

    public static class PartialShadow {
        public static int number() {
            return 3;
        }
    }
}
