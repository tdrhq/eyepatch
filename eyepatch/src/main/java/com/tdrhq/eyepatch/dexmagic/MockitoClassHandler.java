// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.dexmagic;

import com.tdrhq.eyepatch.util.Checks;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MockitoClassHandler implements ClassHandler {
    private Class klass;

    private Class companionClass;
    private Object companionMock;

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
    private CompanionBuilder companionBuilder;

    public MockitoClassHandler(Class klass, CompanionBuilder companionBuilder) {
        this.klass = klass;
        this.companionBuilder = Checks.notNull(companionBuilder);

        companionClass = companionBuilder.build(klass, getClass().getClassLoader());
        companionMock = mock(companionClass);
    }

    @Override
    public Object handleInvocation(Invocation invocation) {
        Object[] args = invocation.getArgs();
        Method method = getCompanionMethod(invocation);
        try {
            return method.invoke(
                    companionMock,
                    args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Method getCompanionMethod(Invocation invocation) {
        try {
            return companionClass.getDeclaredMethod(
                    invocation.getMethod(),
                    invocation.getArgTypes());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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

    public void verifyStatic() {
        verify(companionMock);
    }
}
