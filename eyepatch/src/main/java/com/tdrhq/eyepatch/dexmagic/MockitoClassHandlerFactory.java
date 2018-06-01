// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

public class MockitoClassHandlerFactory implements ClassHandlerFactory {
    public MockitoClassHandlerFactory() {
    }

    @Override
    public ClassHandler create(Class klass) {
        return new MockitoClassHandler(klass);
    }

}
