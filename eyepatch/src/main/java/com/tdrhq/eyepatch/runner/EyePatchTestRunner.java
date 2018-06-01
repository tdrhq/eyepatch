// (c) 2018 Arnold Noronha <arnold@tdrhq.com>

package com.tdrhq.eyepatch.runner;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import com.tdrhq.eyepatch.classloader.EyePatchClassLoader;
import com.tdrhq.eyepatch.dexmagic.EyePatchClassBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.JUnit4;
import org.junit.runners.model.InitializationError;

public class EyePatchTestRunner extends Runner {
    private Runner delegate;

    public EyePatchTestRunner(Class<?> testClass) throws InitializationError {
        EyePatchClassLoader classLoader = new EyePatchClassLoader(
                getClass().getClassLoader(),
                new EyePatchClassBuilder(null));
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
