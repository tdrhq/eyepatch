package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.MockDelegateFactory;
import com.tdrhq.eyepatch.util.ExposedTemporaryFolder;

import org.junit.runners.model.InitializationError;

public class TestController {
    static Class<?> generateTestClass(ExposedTemporaryFolder tmpdir, Class<?> testClass, Class[] mockables, ClassLoader classLoader1) throws InitializationError {
        tmpdir.before();
        EyePatchClassLoader classLoader = new EyePatchClassLoader(
                classLoader1,
                new EyePatchClassBuilder(tmpdir.getRoot()));

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
}
