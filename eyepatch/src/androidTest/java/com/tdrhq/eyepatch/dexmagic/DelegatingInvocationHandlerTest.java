package com.tdrhq.eyepatch.dexmagic;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DelegatingInvocationHandlerTest {
    private DelegatingInvocationHandler mHandler;

    @Before
    public void before() throws Throwable {
        mHandler = new DelegatingInvocationHandler();
    }

    @Test
    public void testBadClassLoader() throws Throwable {
        try {
            mHandler.handleInvocation(
                    new Invocation(
                            getClass(),
                            this,
                            "foo",
                            new Object[] {}));
            fail("expected exception");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

}
