// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.EyePatchMockito;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.verify;

public class MockitoClassHandler implements ClassHandler {
    public static Class verifyStaticClass = null;
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

        if (klass == verifyStaticClass) {
            mockDelegate = verify(mockDelegate);
            verifyStaticClass = null;
        }

        Object[] args = invocation.getArgs();
        // See gen-switch.el
        switch (args.length) {
        case 0:
            return mockDelegate.invoke0();
        case 1:
            return mockDelegate.invoke1(args[0]);
        case 2:
            return mockDelegate.invoke2(args[0], args[1]);
        case 3:
            return mockDelegate.invoke3(args[0], args[1], args[2]);
        case 4:
            return mockDelegate.invoke4(args[0], args[1], args[2], args[3]);
        case 5:
            return mockDelegate.invoke5(args[0], args[1], args[2], args[3], args[4]);
        case 6:
            return mockDelegate.invoke6(args[0], args[1], args[2], args[3], args[4], args[5]);
        case 7:
            return mockDelegate.invoke7(args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
        case 8:
            return mockDelegate.invoke8(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
        case 9:
            return mockDelegate.invoke9(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
        case 10:
            return mockDelegate.invoke10(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
        case 11:
            return mockDelegate.invoke11(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
        case 12:
            return mockDelegate.invoke12(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
        case 13:
            return mockDelegate.invoke13(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
        case 14:
            return mockDelegate.invoke14(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
        case 15:
            return mockDelegate.invoke15(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
        case 16:
            return mockDelegate.invoke16(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15]);
        case 17:
            return mockDelegate.invoke17(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16]);
        case 18:
            return mockDelegate.invoke18(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17]);
        case 19:
            return mockDelegate.invoke19(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18]);
        case 20:
            return mockDelegate.invoke20(args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14], args[15], args[16], args[17], args[18], args[19]);

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

    public boolean canHandle(Class klass) {
        return this.klass == klass;
    }
}
