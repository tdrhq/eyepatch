// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.concurrent.atomic.AtomicReference;
import static org.mockito.Mockito.mock;

public class MockitoClassHandler implements ClassHandler {
    private Class klass;
    private MockDelegate mockDelegate;

    public interface MockDelegate {
        public Object invoke(
                String name,
                Object instance,
                Object... arguments);
    }

    public MockitoClassHandler(Class klass) {
        this.klass = klass;
        mockDelegate = mock(MockDelegate.class);
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return mockDelegate.invoke(
                invocation.getMethod(),
                invocation.getInstance(),
                invocation.getArgs());
    }
}
