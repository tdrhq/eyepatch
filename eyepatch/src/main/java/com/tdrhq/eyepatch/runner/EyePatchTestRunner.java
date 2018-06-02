// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import com.tdrhq.eyepatch.util.ExposedTemporaryFolder;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

public class EyePatchTestRunner extends Runner {
    private Runner delegate;

    private ExposedTemporaryFolder tmpdir = new ExposedTemporaryFolder();

    public EyePatchTestRunner(Class<?> testClass) throws InitializationError {
        EyePatchMockable mockableAnnotation = testClass.getAnnotation(EyePatchMockable.class);
        Class[] mockables = mockableAnnotation.value();

        testClass = tmpdir.generateTestClass(testClass, mockables, getClass().getClassLoader());
        delegate = new JUnit4(testClass);
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
