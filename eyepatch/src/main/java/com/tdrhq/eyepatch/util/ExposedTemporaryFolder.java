package com.tdrhq.eyepatch.util;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.MockDelegateFactory;

import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.InitializationError;

public class ExposedTemporaryFolder extends TemporaryFolder {
    public Class<?> generateTestClass(Class<?> testClass, Class[] mockables, ClassLoader classLoader1) throws InitializationError {
        before();
        EyePatchClassLoader classLoader = new EyePatchClassLoader(
                classLoader1,
                new EyePatchClassBuilder(getRoot()));

        MockDelegateFactory mockDelegateFactory = MockDelegateFactory.getInstance();
        for (Class<?> mockable : mockables) {
            mockDelegateFactory.init(mockable);
        }

        for (Class<?> mockable : mockables) {
            classLoader.addMockable(mockable.getName());
        }

        try {
            testClass = classLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
        return testClass;
    }

    @Override
    public void before() {
        try {
            super.before();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void after() {
        try {
            super.after();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
