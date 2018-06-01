// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.mock;

public class MockitoClassHandler implements ClassHandler {
    private Class klass;
    private MockDelegateFactory mockDelegateFactory = MockDelegateFactory.getInstance();

    private static class MockKey {
        public String methodName;
        public Object instance;

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof MockKey)) {
                return false;
            }

            MockKey otherKey = (MockKey) other;
            return otherKey.methodName.equals(methodName) &&
                    otherKey.instance == instance;
        }

        @Override
        public int hashCode() {
            return methodName.hashCode();
        }
    }
    private MockDelegate nextMockDelegate;
    private Map<MockKey, MockDelegate> mockDelegates = new HashMap<>();

    public MockitoClassHandler(Class klass) {
        this.klass = klass;
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        MockDelegate mockDelegate = getMockDelegate(invocation.getMethod(), invocation.getInstance());
        Object[] args = invocation.getArgs();
        switch (args.length) {
        case 0:
            return mockDelegate.invoke0();
        case 1:
            return mockDelegate.invoke1(args[0]);
        }

        throw new RuntimeException("unsupported number of arguments");
    }

    private MockDelegate getMockDelegate(String methodName, Object instance) {
        MockKey key = new MockKey();
        key.methodName = methodName;
        key.instance = instance;

        if (mockDelegates.containsKey(key)) {
            return mockDelegates.get(key);
        }

        MockDelegate ret = mockDelegateFactory.create();
        mockDelegates.put(key, ret);

        return ret;
    }

    public static class Reference {
        private Object object;
        public Reference(Object object) {
            this.object = object;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Reference)) {
                return false;
            }

            Reference otherRef = (Reference) other;
            return object == otherRef.object;
        }
    }
}
