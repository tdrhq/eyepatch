// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

import com.tdrhq.eyepatch.iface.HasStaticInvocationHandler;
import com.tdrhq.eyepatch.iface.Invocation;
import com.tdrhq.eyepatch.iface.StaticInvocationHandler;

/**
 * A Dispatcher that delegates to the
 * Dispatcher provided by the ClassLoader.
 */
public class DelegatingInvocationHandler implements StaticInvocationHandler {
    @Override
    public Object handleInvocation(Invocation invocation) throws Exception {
        ClassLoader classLoader = invocation.getInstanceClass().getClassLoader();
        if (!(classLoader instanceof HasStaticInvocationHandler)) {
            throw new UnsupportedOperationException("");
        }

        HasStaticInvocationHandler hasStaticInvocationHandler =
                (HasStaticInvocationHandler) classLoader;

        return hasStaticInvocationHandler.getStaticInvocationHandler()
                .handleInvocation(invocation);
    }
}
