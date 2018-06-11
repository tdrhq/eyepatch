// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import org.junit.Test;
import static org.junit.Assert.*;

public class SuperInvocationTest {
    @Test
    public void testCheckArgId() throws Throwable {
        assertNotEquals(0,
                SuperInvocation.empty().getConsId());
        assertEquals(
                SuperInvocation.empty().getConsId(),
                SuperInvocation.empty().getConsId());
    }

    @Test
    public void testAssertDifferent() throws Throwable {
        assertNotEquals(
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "" }).getConsId(),
                SuperInvocation.empty().getConsId());
        assertEquals(
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "" }).getConsId(),
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "blah" }).getConsId());
    }
}
