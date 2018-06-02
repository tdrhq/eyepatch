// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

/**
 * A StaticInvocationHandler that delegates to the
 * StaticInvocationHandler provided by the ClassLoader.
 */
public class DelegatingInvocationHandler extends StaticInvocationHandler {
    @Override
    public Object handleInvocation(Invocation invocation) {
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
