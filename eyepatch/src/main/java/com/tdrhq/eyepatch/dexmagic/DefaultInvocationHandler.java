// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.classloader.DefaultClassHandlerProvider;
import com.tdrhq.eyepatch.iface.ClassHandler;
import com.tdrhq.eyepatch.iface.Invocation;

public class DefaultInvocationHandler implements StaticInvocationHandler {

    private DefaultClassHandlerProvider classHandlerProvider;

    DefaultInvocationHandler(
            DefaultClassHandlerProvider classHandlerProvider) {

        this.classHandlerProvider = classHandlerProvider;
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        return getClassHandler(invocation.getInstanceClass())
                .handleInvocation(invocation);
    }

    final ClassHandler getClassHandler(Class klass) {
        ClassHandler ret = classHandlerProvider.getClassHandler(klass);

        if (ret == null) {
            throw new RuntimeException("No class handler for class: " + klass.getName());
        }

        return ret;
    }

    public static DefaultInvocationHandler newInstance(DefaultClassHandlerProvider provider) {
        return new DefaultInvocationHandler(provider);
    }
}
