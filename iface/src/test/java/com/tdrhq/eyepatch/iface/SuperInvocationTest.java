// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import com.tdrhq.eyepatch.iface.SuperInvocation;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;

public class SuperInvocationTest {
    @Test
    public void testCheckArgId() throws Throwable {
        Assert.assertNotEquals(0,
                SuperInvocation.empty().getConsId());
        Assert.assertEquals(
                SuperInvocation.empty().getConsId(),
                SuperInvocation.empty().getConsId());
    }

    @Test
    public void testAssertDifferent() throws Throwable {
        Assert.assertNotEquals(
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "" }).getConsId(),
                SuperInvocation.empty().getConsId());
        Assert.assertEquals(
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "" }).getConsId(),
                new SuperInvocation(new Class[] { String.class },
                                    new Object[] { "blah" }).getConsId());
    }
}
