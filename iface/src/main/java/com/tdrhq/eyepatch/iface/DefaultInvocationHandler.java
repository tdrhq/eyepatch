// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.iface;

public class DefaultInvocationHandler implements StaticInvocationHandler {

    private ClassHandlerProvider classHandlerProvider;

    DefaultInvocationHandler(
            ClassHandlerProvider classHandlerProvider) {

        this.classHandlerProvider = classHandlerProvider;
    }

    @Override
    public Object handleInvocation(Invocation invocation) throws Exception {
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

    public static DefaultInvocationHandler newInstance(ClassHandlerProvider provider) {
        return new DefaultInvocationHandler(provider);
    }
}
