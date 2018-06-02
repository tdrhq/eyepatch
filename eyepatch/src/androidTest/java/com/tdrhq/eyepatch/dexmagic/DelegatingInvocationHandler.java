// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

/**
 * A StaticInvocationHandler that delegates to the
 * StaticInvocationHandler provided by the ClassLoader.
 */
public class DelegatingInvocationHandler extends StaticInvocationHandler {
    @Override
    public Object handleInvocation(Invocation invocation) {
        throw new UnsupportedOperationException("");
    }
}
