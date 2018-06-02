// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import com.tdrhq.eyepatch.dexmagic.MockDelegateFactory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

public class EyePatchTestRunner extends Runner {
    private Runner delegate;

    private static class ExposedTemporaryFolder extends TemporaryFolder {
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

    private ExposedTemporaryFolder tmpdir = new ExposedTemporaryFolder();

    public EyePatchTestRunner(Class<?> testClass) throws InitializationError {
        EyePatchMockable mockableAnnotation = testClass.getAnnotation(EyePatchMockable.class);
        Class[] mockables = mockableAnnotation.value();

        testClass = generateTestClass(tmpdir, testClass, mockables, getClass().getClassLoader());
        delegate = new JUnit4(testClass);
    }

    private static Class<?> generateTestClass(ExposedTemporaryFolder tmpdir, Class<?> testClass, Class[] mockables, ClassLoader classLoader1) throws InitializationError {
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

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        delegate.run(runNotifier);
        tmpdir.after();
    }
}
