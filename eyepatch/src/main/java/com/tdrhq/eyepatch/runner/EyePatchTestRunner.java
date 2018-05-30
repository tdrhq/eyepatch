// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.classloader.AndroidClassLoader;
import java.text.Annotation;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

public class EyePatchTestRunner extends Runner {
    private Runner delegate;

    public EyePatchTestRunner(Class<?> testClass) throws InitializationError {
        AndroidClassLoader classLoader = new AndroidClassLoader(getClass().getClassLoader());
        EyePatchMockable mockableAnnotation = testClass.getAnnotation(EyePatchMockable.class);
        for (Class<?> mockable : mockableAnnotation.value()) {
            classLoader.addMockable(mockable.getName());
        }

        try {
            testClass = classLoader.loadClass(testClass.getName());
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
        delegate = new JUnit4(testClass);
    }

    @Override
    public Description getDescription() {
        return delegate.getDescription();
    }

    @Override
    public void run(RunNotifier runNotifier) {
        delegate.run(runNotifier);
    }
}
