package com.tdrhq.eyepatch.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class WhiteboxTest {
    @Test
    public void testInvokeStatic() throws Throwable {
        assertEquals(
                "car",
                Whitebox.invokeStatic(SillyClass.class, "foo"));
    }
}
