// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import org.junit.Test;
import static org.junit.Assert.*;

public class GeneratedMethodTest {
    @Test
    public void testGeneratedMethodIsUnique() throws Throwable {
        assertSame(
                GeneratedMethod.create(Foo.class, "bar", new Class[] {}),
                GeneratedMethod.create(Foo.class, "bar", new Class[] {}));
    }

    public static class Foo {
        public void foo() {

        }
    }
}
