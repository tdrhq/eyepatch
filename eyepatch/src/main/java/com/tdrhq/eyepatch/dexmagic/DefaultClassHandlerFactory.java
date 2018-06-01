// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

public class DefaultClassHandlerFactory implements ClassHandlerFactory {
    public DefaultClassHandlerFactory() {
    }

    @Override
    public ClassHandler create(Class klass) {
        return new ClassHandler() {
            @Override
            public Object handleInvocation(Invocation invocation) {
                return null;
            }
        };
    }

}
