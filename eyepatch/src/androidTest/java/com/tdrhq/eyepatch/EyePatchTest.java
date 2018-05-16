package com.tdrhq.eyepatch;

import org.junit.Test;

import static org.junit.Assert.*;

public class EyePatchTest {
    @Test
    public void testBasics() throws Exception {
        EyePatch.patch(Bar.class,
                Bar.class.getMethod("foo"),
                new EyePatch.Callback() {
                    @Override
                    public Object run(Object instance, Object[] arguments) {
                        return "car";
                    }
                });

    }

    public static class Bar {
        public static String foo() {
            return "foo";
        }
    }
}
